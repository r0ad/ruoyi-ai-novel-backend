package com.ruoyi.novel.ai.config;

import java.time.Duration;
import org.springframework.ai.anthropic.http.okhttp.AnthropicHttpClientBuilderCustomizer;
import org.springframework.ai.openai.http.okhttp.OpenAiHttpClientBuilderCustomizer;

/**
 * 为 OpenAI / Anthropic SDK 的 OkHttp 客户端配置超时。
 * Agent 工具调用期间 SSE 可能长时间无 token，需关闭 read 间隔超时，仅保留整请求上限。
 */
public final class NovelAiHttpTimeoutCustomizer
{
    private static final int DEFAULT_TIMEOUT_MS = 600_000;

    private static final int DEFAULT_AGENT_TIMEOUT_MS = 900_000;

    private static final int MIN_TIMEOUT_MS = 180_000;

    private static final int MIN_AGENT_TIMEOUT_MS = 600_000;

    private NovelAiHttpTimeoutCustomizer()
    {
    }

    public static Duration requestDuration(int timeoutMs)
    {
        return Duration.ofMillis(timeoutMs);
    }

    public static int resolveTimeoutMs(Integer configuredTimeoutMs)
    {
        return resolveTimeoutMs(configuredTimeoutMs, DEFAULT_TIMEOUT_MS, MIN_TIMEOUT_MS);
    }

    public static int resolveAgentTimeoutMs(Integer configuredTimeoutMs)
    {
        return resolveTimeoutMs(configuredTimeoutMs, DEFAULT_AGENT_TIMEOUT_MS, MIN_AGENT_TIMEOUT_MS);
    }

    private static int resolveTimeoutMs(Integer configuredTimeoutMs, int defaultMs, int minMs)
    {
        int timeout = (configuredTimeoutMs != null && configuredTimeoutMs > 0)
            ? configuredTimeoutMs
            : defaultMs;
        return Math.max(timeout, minMs);
    }

    public static OpenAiHttpClientBuilderCustomizer openAi(int timeoutMs)
    {
        return builder -> builder.timeout(buildOpenAiTimeout(timeoutMs));
    }

    public static AnthropicHttpClientBuilderCustomizer anthropic(int timeoutMs)
    {
        return builder -> builder.timeout(buildAnthropicTimeout(timeoutMs));
    }

    private static com.openai.core.Timeout buildOpenAiTimeout(int timeoutMs)
    {
        Duration requestTimeout = Duration.ofMillis(timeoutMs);
        return com.openai.core.Timeout.builder()
            .request(requestTimeout)
            .connect(Duration.ofSeconds(30))
            .write(requestTimeout)
            .read(Duration.ZERO)
            .build();
    }

    private static com.anthropic.core.Timeout buildAnthropicTimeout(int timeoutMs)
    {
        Duration requestTimeout = Duration.ofMillis(timeoutMs);
        return com.anthropic.core.Timeout.builder()
            .request(requestTimeout)
            .connect(Duration.ofSeconds(30))
            .write(requestTimeout)
            .read(Duration.ZERO)
            .build();
    }
}
