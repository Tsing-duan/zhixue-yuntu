package com.ape.apeadmin.security;

import com.ape.apesystem.service.ai.AiGenerationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

/** Shared, atomic admission independent of deletable conversation history. */
@Service
public class AiQuotaGuard {
    private final StringRedisTemplate redis;
    private final int leaseSeconds;
    private final DefaultRedisScript<Long> reserve = script("reserve-budget.lua");
    private final DefaultRedisScript<Long> complete = script("complete-budget.lua");

    public AiQuotaGuard(StringRedisTemplate redis,
                        @Value("${deepseek.read-timeout-ms:120000}") int readTimeout,
                        @Value("${deepseek.connect-timeout-ms:5000}") int connectTimeout) {
        this.redis = redis;
        this.leaseSeconds = (readTimeout + connectTimeout) / 1000 + 60;
    }
    private static DefaultRedisScript<Long> script(String name) {
        DefaultRedisScript<Long> result = new DefaultRedisScript<>();
        result.setLocation(new ClassPathResource("ai/" + name));
        result.setResultType(Long.class);
        return result;
    }
    public Ticket acquire(String userId, int callLimit, int tokenLimit, int cooldown,
                          int reservedTokens, int historicalCalls, int historicalTokens) {
        LocalDate day = LocalDate.now();
        String prefix = "ai:budget:{" + userId + "}:";
        List<String> keys = Arrays.asList(prefix + day, prefix + "in-flight", prefix + "cooldown");
        String owner = UUID.randomUUID().toString();
        long ttl = Math.max(3600, Duration.between(Instant.now(), day.plusDays(2)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()).getSeconds());
        final Long code;
        try {
            code = redis.execute(reserve, keys, owner, String.valueOf(leaseSeconds), String.valueOf(cooldown),
                    String.valueOf(ttl), String.valueOf(callLimit), String.valueOf(tokenLimit),
                    String.valueOf(reservedTokens), String.valueOf(historicalCalls), String.valueOf(historicalTokens));
        } catch (RuntimeException unavailable) {
            return new Ticket(keys, owner, reservedTokens, "AI 用量保护暂时不可用，请稍后再试。");
        }
        String error = code == null ? "AI 用量保护暂时不可用，请稍后再试。"
                : code == 1 ? "已有 AI 请求正在生成，请等待完成后再试。"
                : code == 2 ? "AI 请求过于频繁，请稍后再试。"
                : code == 3 ? "今日 AI 请求次数已达上限，请明天再试。"
                : code == 4 ? "今日 AI 剩余额度不足，请缩小资料范围或明天再试。" : code == 0 ? null : "AI 用量保护暂时不可用，请稍后再试。";
        return new Ticket(keys, owner, reservedTokens, error);
    }
    public void finish(Ticket ticket, AiGenerationResult result) {
        String outcome = result != null && result.isSuccess() ? "success"
                : result != null && result.getErrorType() == AiGenerationResult.ErrorType.NOT_CONFIGURED
                ? "not-configured" : "failure";
        try {
            redis.execute(complete, ticket.keys, ticket.owner, String.valueOf(ticket.reservedTokens),
                    String.valueOf(result == null ? 0 : result.getTotalTokens()), outcome);
        } catch (RuntimeException unavailable) {
            // Retain the conservative reservation. The lease expires; do not expose payloads in logs.
        }
    }
    public static class Ticket {
        private final List<String> keys;
        private final String owner;
        private final int reservedTokens;
        private final String error;
        public Ticket(List<String> keys, String owner, int reservedTokens, String error) {
            this.keys = keys; this.owner = owner; this.reservedTokens = reservedTokens; this.error = error;
        }
        public String getError() { return error; }
    }
}
