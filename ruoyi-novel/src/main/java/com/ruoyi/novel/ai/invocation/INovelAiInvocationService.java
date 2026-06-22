package com.ruoyi.novel.ai.invocation;

import com.ruoyi.novel.ai.invocation.domain.AiInvocationRecord;

/**
 * 统一 AI 调用入口，记录 prompt/response 供调试
 */
public interface INovelAiInvocationService
{
    String SOURCE_CAPABILITY = "capability";

    /**
     * 执行一次 AI 调用并记录 invocation（线程内可通过 getLastInvocation 读取）
     */
    String invoke(String source, String systemPrompt, String userPrompt);

    AiInvocationRecord getLastInvocation();

    void clearLastInvocation();
}
