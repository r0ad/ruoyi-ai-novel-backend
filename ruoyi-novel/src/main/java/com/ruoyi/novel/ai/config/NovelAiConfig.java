package com.ruoyi.novel.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient 配置
 * 依赖 spring.ai.openai.* 配置项，见 application.yml
 */
@Configuration
public class NovelAiConfig
{
    @Bean
    @ConditionalOnBean(OpenAiChatModel.class)
    public ChatClient novelChatClient(OpenAiChatModel chatModel)
    {
        return ChatClient.builder(chatModel)
            .defaultSystem("你是一位专业网络小说创作助手，请用中文回答。")
            .build();
    }
}
