package com.ape.apeadmin.ai;

import com.alibaba.fastjson2.JSONObject;
import com.ape.apesystem.service.ai.AiGenerationResult;
import com.ape.apesystem.service.ai.DeepSeekClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeepSeekClientContractTest {

    private static final String TEST_KEY = "test-only-deepseek-key";

    private HttpServer server;
    private AtomicInteger attempts;
    private AtomicReference<String> requestBody;
    private AtomicReference<String> authorization;
    private AtomicReference<String> requestPath;

    @BeforeEach
    void setUp() throws IOException {
        attempts = new AtomicInteger();
        requestBody = new AtomicReference<>();
        authorization = new AtomicReference<>();
        requestPath = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void successfulResponseUsesDeepSeekContractAndExposesOnlyFinalContent() {
        respond(200, successBody("最终答案", "不得暴露的推理", 11, 7, 18, "stop"));

        AiGenerationResult result = client(1000).generate("系统提示", "用户问题");

        assertThat(attempts.get()).isEqualTo(1);
        assertThat(requestPath.get()).isEqualTo("/chat/completions");
        assertThat(authorization.get()).isEqualTo("Bearer " + TEST_KEY);
        JSONObject body = JSONObject.parseObject(requestBody.get());
        assertThat(body.getString("model")).isEqualTo("deepseek-v4-flash");
        assertThat(body.getBooleanValue("stream")).isFalse();
        assertThat(body.getJSONObject("thinking").getString("type")).isEqualTo("enabled");
        assertThat(body.getString("reasoning_effort")).isEqualTo("high");
        assertThat(body.getIntValue("max_tokens")).isEqualTo(4096);
        assertThat(body.getJSONArray("messages")).hasSize(2);
        assertThat(body.getJSONArray("messages").getJSONObject(0).getString("content")).isEqualTo("系统提示");
        assertThat(body.getJSONArray("messages").getJSONObject(1).getString("content")).isEqualTo("用户问题");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getContent()).isEqualTo("最终答案");
        assertThat(result.getPromptTokens()).isEqualTo(11);
        assertThat(result.getCompletionTokens()).isEqualTo(7);
        assertThat(result.getTotalTokens()).isEqualTo(18);
        assertThat(result.toString()).doesNotContain("不得暴露的推理", "reasoning_content");
    }

    @ParameterizedTest
    @MethodSource("providerErrors")
    void providerHttpErrorsAreClassifiedWithoutRetry(int status, AiGenerationResult.ErrorType expectedType) {
        respond(status, "{\"error\":{\"message\":\"provider-internal-detail\"}}");

        AiGenerationResult result = client(1000).generate("系统提示", "用户问题");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorType()).isEqualTo(expectedType);
        assertThat(result.getUserMessage()).doesNotContain("provider-internal-detail", TEST_KEY);
        assertThat(attempts.get()).isEqualTo(1);
    }

    static Stream<Arguments> providerErrors() {
        return Stream.of(
                Arguments.of(400, AiGenerationResult.ErrorType.BAD_REQUEST),
                Arguments.of(401, AiGenerationResult.ErrorType.INVALID_CREDENTIAL),
                Arguments.of(403, AiGenerationResult.ErrorType.INVALID_CREDENTIAL),
                Arguments.of(402, AiGenerationResult.ErrorType.PAYMENT_REQUIRED),
                Arguments.of(422, AiGenerationResult.ErrorType.UNPROCESSABLE_REQUEST),
                Arguments.of(429, AiGenerationResult.ErrorType.RATE_LIMITED),
                Arguments.of(500, AiGenerationResult.ErrorType.PROVIDER_UNAVAILABLE),
                Arguments.of(503, AiGenerationResult.ErrorType.PROVIDER_UNAVAILABLE));
    }

    @ParameterizedTest
    @MethodSource("incompleteFinishReasons")
    void nonStopFinishReasonsAreRejected(String finishReason) {
        respond(200, successBody("可能不完整", "推理", 4, 3, 7, finishReason));

        AiGenerationResult result = client(1000).generate("系统提示", "用户问题");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorType()).isEqualTo(AiGenerationResult.ErrorType.INCOMPLETE_RESPONSE);
        assertThat(attempts.get()).isEqualTo(1);
    }

    static Stream<String> incompleteFinishReasons() {
        return Stream.of("length", "content_filter", "tool_calls", "insufficient_system_resource");
    }

    @Test
    void toolCallsAreRejectedEvenWhenFinishReasonClaimsStop() {
        respond(200, "{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"final\",\"tool_calls\":[{\"id\":\"call-1\"}]}}],"
                + "\"usage\":{\"prompt_tokens\":4,\"completion_tokens\":2,\"total_tokens\":6}}");

        AiGenerationResult result = client(1000).generate("系统提示", "用户问题");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorType()).isEqualTo(AiGenerationResult.ErrorType.INCOMPLETE_RESPONSE);
    }

    @Test
    void malformedJsonIsRejected() {
        respond(200, "not-json");

        AiGenerationResult result = client(1000).generate("系统提示", "用户问题");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorType()).isEqualTo(AiGenerationResult.ErrorType.MALFORMED_RESPONSE);
        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void emptyContentIsRejected() {
        respond(200, successBody("  ", "推理", 4, 2, 6, "stop"));

        AiGenerationResult result = client(1000).generate("系统提示", "用户问题");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorType()).isEqualTo(AiGenerationResult.ErrorType.EMPTY_CONTENT);
    }

    @Test
    void missingUsageIsRejected() {
        respond(200, "{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"final\"}}]}");

        AiGenerationResult result = client(1000).generate("系统提示", "用户问题");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorType()).isEqualTo(AiGenerationResult.ErrorType.INVALID_USAGE);
    }

    @Test
    void inconsistentUsageIsRejected() {
        respond(200, successBody("final", "推理", 4, 2, 99, "stop"));

        AiGenerationResult result = client(1000).generate("系统提示", "用户问题");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorType()).isEqualTo(AiGenerationResult.ErrorType.INVALID_USAGE);
    }

    @Test
    void readTimeoutIsClassifiedWithoutRetry() {
        server.createContext("/chat/completions", exchange -> {
            capture(exchange);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            write(exchange, 200, successBody("late", "推理", 4, 2, 6, "stop"));
        });
        server.start();

        AiGenerationResult result = client(50).generate("系统提示", "用户问题");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorType()).isEqualTo(AiGenerationResult.ErrorType.TIMEOUT);
        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void missingCredentialFailsBeforeNetwork() {
        respond(200, successBody("final", "推理", 4, 2, 6, "stop"));
        DeepSeekClient client = new DeepSeekClient("", baseUrl(), "deepseek-v4-flash", "high", 4096, 5000, 1000, "test");

        AiGenerationResult result = client.generate("系统提示", "用户问题");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorType()).isEqualTo(AiGenerationResult.ErrorType.NOT_CONFIGURED);
        assertThat(attempts.get()).isZero();
    }

    @Test
    void normalRuntimeRejectsNonOfficialOrInsecureBaseUrls() {
        assertThatThrownBy(() -> new DeepSeekClient(TEST_KEY, "http://api.deepseek.com", "deepseek-v4-flash", "high", 4096, 5000, 1000, "dev"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DeepSeekClient(TEST_KEY, "https://example.invalid", "deepseek-v4-flash", "high", 4096, 5000, 1000, "dev"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DeepSeekClient(TEST_KEY, baseUrl(), "deepseek-v4-flash", "high", 4096, 5000, 1000, "dev"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DeepSeekClient(TEST_KEY, "https://api.deepseek.com?redirect=invalid", "deepseek-v4-flash", "high", 4096, 5000, 1000, "dev"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DeepSeekClient(TEST_KEY, "https://user@api.deepseek.com", "deepseek-v4-flash", "high", 4096, 5000, 1000, "dev"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private DeepSeekClient client(int readTimeoutMs) {
        return new DeepSeekClient(TEST_KEY, baseUrl(), "deepseek-v4-flash", "high", 4096, 5000, readTimeoutMs, "test");
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private void respond(int status, String body) {
        server.createContext("/chat/completions", exchange -> {
            capture(exchange);
            write(exchange, status, body);
        });
        server.start();
    }

    private void capture(HttpExchange exchange) throws IOException {
        attempts.incrementAndGet();
        authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        requestPath.set(exchange.getRequestURI().getPath());
        requestBody.set(readAll(exchange.getRequestBody()));
    }

    private void write(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private String readAll(InputStream input) throws IOException {
        byte[] buffer = new byte[1024];
        StringBuilder text = new StringBuilder();
        int count;
        while ((count = input.read(buffer)) != -1) {
            text.append(new String(buffer, 0, count, StandardCharsets.UTF_8));
        }
        return text.toString();
    }

    private String successBody(String content, String reasoning, int prompt, int completion, int total, String finishReason) {
        JSONObject body = new JSONObject();
        JSONObject message = new JSONObject();
        message.put("content", content);
        message.put("reasoning_content", reasoning);
        JSONObject choice = new JSONObject();
        choice.put("finish_reason", finishReason);
        choice.put("message", message);
        body.put("choices", java.util.Collections.singletonList(choice));
        JSONObject usage = new JSONObject();
        usage.put("prompt_tokens", prompt);
        usage.put("completion_tokens", completion);
        usage.put("total_tokens", total);
        body.put("usage", usage);
        return body.toJSONString();
    }
}
