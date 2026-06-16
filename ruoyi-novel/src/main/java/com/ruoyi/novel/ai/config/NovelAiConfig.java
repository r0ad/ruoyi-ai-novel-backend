package com.ruoyi.novel.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Spring AI ChatClient 配置
 * 优先使用 starter 自动配置；若未生效则根据 spring.ai.openai.* 手动创建。
 */
@Configuration
public class NovelAiConfig
{
    private static final Logger log = LoggerFactory.getLogger(NovelAiConfig.class);

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.openai", name = "api-key")
    @ConditionalOnMissingBean(OpenAiChatModel.class)
    public OpenAiChatModel novelFallbackOpenAiChatModel(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${spring.ai.openai.chat.model:deepseek-chat}") String model,
            @Value("${spring.ai.openai.chat.temperature:0.7}") Double temperature,
            @Value("${spring.ai.openai.chat.max-tokens:4096}") Integer maxTokens)
    {
        if (!StringUtils.hasText(apiKey))
        {
            throw new IllegalStateException("spring.ai.openai.api-key 不能为空");
        }
        log.info("使用手动配置创建 OpenAiChatModel，baseUrl={}, model={}", baseUrl, model);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .model(model)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();
        return OpenAiChatModel.builder()
            .options(options)
            .build();
    }

    @Bean
    public ChatClient novelChatClient(OpenAiChatModel chatModel)
    {
        return ChatClient.builder(chatModel)
            .defaultSystem("你是一位专业网络小说创作助手，请用中文回答。")
            .build();
    }
}
