package com.ape.apesystem.service.ai;

/**
 * Internal result of one generative AI provider call.
 * Provider-only fields such as reasoning content are intentionally absent.
 */
public final class AiGenerationResult {

    public enum ErrorType {
        NOT_CONFIGURED,
        BAD_REQUEST,
        INVALID_CREDENTIAL,
        PAYMENT_REQUIRED,
        UNPROCESSABLE_REQUEST,
        RATE_LIMITED,
        PROVIDER_UNAVAILABLE,
        TIMEOUT,
        MALFORMED_RESPONSE,
        EMPTY_CONTENT,
        INVALID_USAGE,
        INCOMPLETE_RESPONSE
    }

    private final boolean success;
    private final String content;
    private final int promptTokens;
    private final int completionTokens;
    private final int totalTokens;
    private final String finishReason;
    private final ErrorType errorType;
    private final String userMessage;

    private AiGenerationResult(boolean success, String content, int promptTokens,
                               int completionTokens, int totalTokens, String finishReason,
                               ErrorType errorType, String userMessage) {
        this.success = success;
        this.content = content;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.finishReason = finishReason;
        this.errorType = errorType;
        this.userMessage = userMessage;
    }

    public static AiGenerationResult success(String content, int promptTokens,
                                             int completionTokens, int totalTokens,
                                             String finishReason) {
        return new AiGenerationResult(true, content, promptTokens, completionTokens,
                totalTokens, finishReason, null, null);
    }

    public static AiGenerationResult failure(ErrorType errorType, String userMessage) {
        return new AiGenerationResult(false, null, 0, 0, 0, null, errorType, userMessage);
    }

    public boolean isSuccess() { return success; }
    public String getContent() { return content; }
    public int getPromptTokens() { return promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public int getTotalTokens() { return totalTokens; }
    public String getFinishReason() { return finishReason; }
    public ErrorType getErrorType() { return errorType; }
    public String getUserMessage() { return userMessage; }

    @Override
    public String toString() {
        return "AiGenerationResult{" +
                "success=" + success +
                ", promptTokens=" + promptTokens +
                ", completionTokens=" + completionTokens +
                ", totalTokens=" + totalTokens +
                ", finishReason='" + finishReason + '\'' +
                ", errorType=" + errorType +
                '}';
    }
}
