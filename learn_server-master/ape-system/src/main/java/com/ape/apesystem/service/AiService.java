package com.ape.apesystem.service;

import com.ape.apesystem.service.ai.AiGenerationResult;

/**
 * AI 服务接口
 */
public interface AiService {

    /**
     * 调用统一生成型 AI 服务聊天
     * @param role    用户角色 admin / teacher / student
     * @param message 用户消息
     * @return provider generation result
     */
    AiGenerationResult chat(String role, String message);

    /**
     * 调用统一生成型 AI 服务完成具体学习 Agent 任务
     * @param role      用户角色 admin / teacher / student
     * @param agentName Agent 名称
     * @param task      用户任务描述
     * @param context   平台业务上下文
     * @return provider generation result
     */
    AiGenerationResult runAgent(String role, String agentName, String task, String context);
}
