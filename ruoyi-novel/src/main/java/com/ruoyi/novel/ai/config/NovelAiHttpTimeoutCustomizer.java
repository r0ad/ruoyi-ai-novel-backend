package com.ruoyi.novel.ai.config;

import java.time.Duration;
import org.springframework.ai.anthropic.http.okhttp.AnthropicHttpClientBuilderCustomizer;
import org.springframework.ai.openai.http.okhttp.OpenAiHttpClientBuilderCustomizer;

/**
 * 为 OpenAI / Anthropic SDK 的 OkHttp 客户端配置超时（流式长文本生成需要足够长的 read/call timeout）
 */
public final class NovelAiHttpTimeoutCustomizer
{
    private NovelAiHttpTimeoutCustomizer()
    {
    }

    public static int resolveTimeoutMs(Integer configuredTimeoutMs)
    {
        int timeout = (configuredTimeoutMs != null && configuredTimeoutMs > 0)
            ? configuredTimeoutMs
            : 300_000;
        return Math.max(timeout, 120_000);
    }

    public static OpenAiHttpClientBuilderCustomizer openAi(int timeoutMs)
    {
        Duration timeout = Duration.ofMillis(timeoutMs);
        return builder -> builder.timeout(timeout);
    }

    public static AnthropicHttpClientBuilderCustomizer anthropic(int timeoutMs)
    {
        Duration timeout = Duration.ofMillis(timeoutMs);
        return builder -> builder.timeout(timeout);
    }
}
