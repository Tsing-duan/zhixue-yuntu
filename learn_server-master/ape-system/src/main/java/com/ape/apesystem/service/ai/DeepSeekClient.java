package com.ape.apesystem.service.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Locale;

@Slf4j
@Component
public class DeepSeekClient {

    private static final String PROVIDER = "deepseek";
    private static final String OFFICIAL_HOST = "api.deepseek.com";
    private static final String REQUIRED_MODEL = "deepseek-v4-flash";
    private static final String REQUIRED_REASONING_EFFORT = "high";

    @Value("${deepseek.enabled:true}")
    private boolean enabled = true;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final String reasoningEffort;
    private final int maxTokens;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final String providerHost;

    public DeepSeekClient(
            @Value("${deepseek.api-key:}") String apiKey,
            @Value("${deepseek.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${deepseek.model:deepseek-v4-flash}") String model,
            @Value("${deepseek.reasoning-effort:high}") String reasoningEffort,
            @Value("${deepseek.max-tokens:4096}") int maxTokens,
            @Value("${deepseek.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${deepseek.read-timeout-ms:120000}") int readTimeoutMs,
            @Value("${spring.profiles.active:}") String activeProfiles) {
        this.apiKey = trim(apiKey);
        this.baseUrl = normalizeAndValidateBaseUrl(baseUrl, activeProfiles);
        this.model = requireExact(trim(model), REQUIRED_MODEL, "DeepSeek model");
        this.reasoningEffort = requireExact(trim(reasoningEffort), REQUIRED_REASONING_EFFORT,
                "DeepSeek reasoning effort");
        if (maxTokens <= 0 || maxTokens > 8192 || connectTimeoutMs <= 0 || connectTimeoutMs > 30000 || readTimeoutMs <= 0 || readTimeoutMs > 180000) {
            throw new IllegalArgumentException("DeepSeek settings require max tokens 1–8192, connect timeout 1–30000ms, read timeout 1–180000ms");
        }
        this.maxTokens = maxTokens;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.providerHost = URI.create(this.baseUrl).getHost();
    }

    public AiGenerationResult generate(String systemPrompt, String userMessage) {
        if (!enabled) return AiGenerationResult.failure(AiGenerationResult.ErrorType.NOT_CONFIGURED, "AI 生成功能已关闭，请联系管理员。");
        if (apiKey.isEmpty()) {
            return AiGenerationResult.failure(AiGenerationResult.ErrorType.NOT_CONFIGURED,
                    "AI 服务未配置，请联系管理员设置 DEEPSEEK_API_KEY 环境变量。");
        }

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("stream", false);
        body.put("max_tokens", maxTokens);
        JSONObject thinking = new JSONObject();
        thinking.put("type", "enabled");
        body.put("thinking", thinking);
        body.put("reasoning_effort", reasoningEffort);

        JSONArray messages = new JSONArray();
        messages.add(message("system", systemPrompt));
        messages.add(message("user", userMessage));
        body.put("messages", messages);

        long startedAt = System.currentTimeMillis();
        log.info("AI provider attempt -> provider={}, host={}, model={}, attempt=1",
                PROVIDER, providerHost, model);
        HttpResponse response = null;
        try {
            response = HttpRequest.post(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body.toJSONString())
                    .setConnectionTimeout(connectTimeoutMs)
                    .setReadTimeout(readTimeoutMs)
                    .execute();

            int status = response.getStatus();
            if (status != 200) {
                AiGenerationResult failure = mapHttpError(status);
                logOutcome(status, failure.getErrorType().name(), startedAt);
                return failure;
            }

            AiGenerationResult result = parseSuccessResponse(response.body());
            logOutcome(status, result.isSuccess() ? "SUCCESS" : result.getErrorType().name(), startedAt);
            return result;
        } catch (Exception error) {
            AiGenerationResult failure;
            if (isTimeout(error)) {
                failure = AiGenerationResult.failure(AiGenerationResult.ErrorType.TIMEOUT,
                        "AI 服务响应超时，请稍后再试。");
            } else {
                failure = AiGenerationResult.failure(AiGenerationResult.ErrorType.PROVIDER_UNAVAILABLE,
                        "AI 服务暂时不可用，请稍后再试。");
            }
            logOutcome(0, failure.getErrorType().name(), startedAt);
            return failure;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private AiGenerationResult parseSuccessResponse(String responseBody) {
        final JSONObject response;
        try {
            response = JSONObject.parseObject(responseBody);
        } catch (Exception ignored) {
            return malformedResponse();
        }
        if (response == null) {
            return malformedResponse();
        }

        JSONArray choices = response.getJSONArray("choices");
        if (choices == null || choices.size() != 1) {
            return malformedResponse();
        }
        JSONObject choice = choices.getJSONObject(0);
        if (choice == null) {
            return malformedResponse();
        }
        String finishReason = trim(choice.getString("finish_reason"));
        if (!"stop".equals(finishReason)) {
            return AiGenerationResult.failure(AiGenerationResult.ErrorType.INCOMPLETE_RESPONSE,
                    incompleteMessage(finishReason));
        }

        JSONObject message = choice.getJSONObject("message");
        if (message == null) {
            return malformedResponse();
        }
        JSONArray toolCalls = message.getJSONArray("tool_calls");
        if (toolCalls != null && !toolCalls.isEmpty()) {
            return AiGenerationResult.failure(AiGenerationResult.ErrorType.INCOMPLETE_RESPONSE,
                    "AI 服务返回了非最终答案，请稍后再试。");
        }
        String content = trim(message.getString("content"));
        if (content.isEmpty()) {
            return AiGenerationResult.failure(AiGenerationResult.ErrorType.EMPTY_CONTENT,
                    "AI 未返回有效内容，请稍后再试。");
        }

        JSONObject usage = response.getJSONObject("usage");
        Integer promptTokens = positiveInteger(usage, "prompt_tokens");
        Integer completionTokens = positiveInteger(usage, "completion_tokens");
        Integer totalTokens = positiveInteger(usage, "total_tokens");
        if (promptTokens == null || completionTokens == null || totalTokens == null
                || totalTokens.intValue() != promptTokens.intValue() + completionTokens.intValue()) {
            return AiGenerationResult.failure(AiGenerationResult.ErrorType.INVALID_USAGE,
                    "AI 服务用量信息异常，请稍后再试。");
        }

        return AiGenerationResult.success(content, promptTokens, completionTokens, totalTokens, finishReason);
    }

    private AiGenerationResult mapHttpError(int status) {
        switch (status) {
            case 400:
                return AiGenerationResult.failure(AiGenerationResult.ErrorType.BAD_REQUEST,
                        "AI 请求与服务协议不兼容，请联系管理员。");
            case 401:
            case 403:
                return AiGenerationResult.failure(AiGenerationResult.ErrorType.INVALID_CREDENTIAL,
                        "AI 服务凭据无效，请联系管理员。");
            case 402:
                return AiGenerationResult.failure(AiGenerationResult.ErrorType.PAYMENT_REQUIRED,
                        "AI 服务额度不足，请联系管理员。");
            case 422:
                return AiGenerationResult.failure(AiGenerationResult.ErrorType.UNPROCESSABLE_REQUEST,
                        "AI 请求暂时无法处理，请稍后再试。");
            case 429:
                return AiGenerationResult.failure(AiGenerationResult.ErrorType.RATE_LIMITED,
                        "AI 服务当前繁忙，请稍后再试。");
            case 503:
                return AiGenerationResult.failure(AiGenerationResult.ErrorType.PROVIDER_UNAVAILABLE,
                        "AI 服务资源暂时不足，请稍后再试。");
            default:
                return AiGenerationResult.failure(AiGenerationResult.ErrorType.PROVIDER_UNAVAILABLE,
                        "AI 服务暂时不可用，请稍后再试。");
        }
    }

    private JSONObject message(String role, String content) {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content == null ? "" : content);
        return message;
    }

    private Integer positiveInteger(JSONObject object, String key) {
        if (object == null) {
            return null;
        }
        Object value = object.get(key);
        if (!(value instanceof Number)) {
            return null;
        }
        Number number = (Number) value;
        long longValue = number.longValue();
        if (longValue <= 0 || longValue > Integer.MAX_VALUE
                || number.doubleValue() != (double) longValue) {
            return null;
        }
        return (int) longValue;
    }

    private AiGenerationResult malformedResponse() {
        return AiGenerationResult.failure(AiGenerationResult.ErrorType.MALFORMED_RESPONSE,
                "AI 服务响应格式异常，请稍后再试。");
    }

    private String incompleteMessage(String finishReason) {
        if ("length".equals(finishReason)) {
            return "AI 输出达到长度限制，未生成完整答案，请稍后重试。";
        }
        if ("content_filter".equals(finishReason)) {
            return "AI 请求未能生成安全的最终答案，请调整内容后重试。";
        }
        if ("insufficient_system_resource".equals(finishReason)) {
            return "AI 服务资源暂时不足，请稍后再试。";
        }
        return "AI 服务未返回最终答案，请稍后再试。";
    }

    private void logOutcome(int status, String outcome, long startedAt) {
        log.info("AI provider result -> provider={}, host={}, model={}, status={}, outcome={}, elapsedMs={}",
                PROVIDER, providerHost, model, status, outcome,
                Math.max(0, System.currentTimeMillis() - startedAt));
    }

    private boolean isTimeout(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current.getClass().getName().toLowerCase(Locale.ROOT).contains("timeout")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String normalizeAndValidateBaseUrl(String value, String activeProfiles) {
        String normalized = trim(value);
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        final URI uri;
        try {
            uri = URI.create(normalized);
        } catch (Exception error) {
            throw new IllegalArgumentException("Invalid DeepSeek base URL", error);
        }
        String host = uri.getHost();
        boolean cleanBase = (uri.getPath() == null || uri.getPath().isEmpty())
                && uri.getRawQuery() == null
                && uri.getRawFragment() == null
                && uri.getUserInfo() == null;
        boolean official = "https".equalsIgnoreCase(uri.getScheme())
                && OFFICIAL_HOST.equalsIgnoreCase(host)
                && (uri.getPort() == -1 || uri.getPort() == 443)
                && cleanBase;
        boolean loopbackTest = hasTestProfile(activeProfiles)
                && ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                && isLoopbackHost(host)
                && cleanBase;
        if (!official && !loopbackTest) {
            throw new IllegalArgumentException("DeepSeek base URL must use the official HTTPS host");
        }
        return normalized;
    }

    private boolean hasTestProfile(String activeProfiles) {
        for (String profile : trim(activeProfiles).split("[,\\s]+")) {
            if ("test".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLoopbackHost(String host) {
        return "127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host) || "::1".equals(host);
    }

    private String requireExact(String value, String expected, String label) {
        if (!expected.equals(value)) {
            throw new IllegalArgumentException(label + " must be " + expected);
        }
        return value;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
