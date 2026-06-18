package com.ruoyi.novel.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.domain.NovelAiModel;
import com.ruoyi.novel.ai.mapper.NovelAiModelMapper;
import jakarta.annotation.PostConstruct;

/**
 * ????????????????????? ChatClient
 */
@Component
public class NovelAiModelFactory
{
    private static final Logger log = LoggerFactory.getLogger(NovelAiModelFactory.class);

    private static final String DEFAULT_SYSTEM = "?????¦Ë??????§³????????????????????";

    @Autowired
    private NovelAiModelMapper novelAiModelMapper;

    @Autowired
    private NovelAiKeyCrypto novelAiKeyCrypto;

    private volatile ChatClient chatClient;

    private volatile NovelAiModel activeModel;

    @PostConstruct
    public void init()
    {
        reload();
    }

    public synchronized void reload()
    {
        NovelAiModel model = novelAiModelMapper.selectActiveNovelAiModel();
        if (model == null)
        {
            chatClient = null;
            activeModel = null;
            log.info("¦Ä???????? AI ???????");
            return;
        }
        try
        {
            ChatModel chatModel = buildChatModel(model, novelAiKeyCrypto.decrypt(model.getApiKey()));
            chatClient = ChatClient.builder(chatModel).defaultSystem(DEFAULT_SYSTEM).build();
            activeModel = copyWithoutKey(model);
            log.info("AI ?????????{} ({}/{})", model.getModelName(), model.getProviderType(), model.getModelCode());
        }
        catch (Exception ex)
        {
            chatClient = null;
            activeModel = null;
            log.error("???? AI ???????{}", model.getModelName(), ex);
        }
    }

    public ChatClient getChatClient()
    {
        return chatClient;
    }

    public NovelAiModel getActiveModel()
    {
        return activeModel;
    }

    public boolean isReady()
    {
        return chatClient != null;
    }

    public String testModel(NovelAiModel model, String plainApiKey)
    {
        ChatModel chatModel = buildChatModel(model, plainApiKey);
        ChatResponse response = chatModel.call(new Prompt("??????pong"));
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null)
        {
            throw new IllegalStateException("?????????");
        }
        String text = response.getResult().getOutput().getText();
        if (StringUtils.isEmpty(text))
        {
            throw new IllegalStateException("?????????????");
        }
        return text.trim();
    }

    public ChatModel buildChatModel(NovelAiModel model, String plainApiKey)
    {
        if (model == null)
        {
            throw new IllegalArgumentException("??????¨°??????");
        }
        if (StringUtils.isEmpty(plainApiKey))
        {
            throw new IllegalArgumentException("API Key ???????");
        }
        double temperature = model.getTemperature() != null ? model.getTemperature().doubleValue() : 0.7D;
        int maxTokens = model.getMaxTokens() != null ? model.getMaxTokens() : 4096;
        String provider = StringUtils.defaultString(model.getProviderType()).toLowerCase();
        if (NovelAiModel.PROVIDER_ANTHROPIC.equals(provider))
        {
            return buildAnthropicChatModel(model, plainApiKey, temperature, maxTokens);
        }
        return buildOpenAiChatModel(model, plainApiKey, temperature, maxTokens);
    }

    private ChatModel buildOpenAiChatModel(NovelAiModel model, String plainApiKey, double temperature, int maxTokens)
    {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .apiKey(plainApiKey)
            .baseUrl(normalizeBaseUrl(model.getBaseUrl()))
            .model(model.getModelCode())
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();
        return OpenAiChatModel.builder().options(options).build();
    }

    private ChatModel buildAnthropicChatModel(NovelAiModel model, String plainApiKey, double temperature, int maxTokens)
    {
        AnthropicChatOptions.Builder optionsBuilder = AnthropicChatOptions.builder()
            .apiKey(plainApiKey)
            .model(model.getModelCode())
            .temperature(temperature)
            .maxTokens(maxTokens);
        if (StringUtils.isNotEmpty(model.getBaseUrl()))
        {
            optionsBuilder.baseUrl(normalizeBaseUrl(model.getBaseUrl()));
        }
        return AnthropicChatModel.builder().options(optionsBuilder.build()).build();
    }

    private String normalizeBaseUrl(String baseUrl)
    {
        if (StringUtils.isEmpty(baseUrl))
        {
            return baseUrl;
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private NovelAiModel copyWithoutKey(NovelAiModel source)
    {
        NovelAiModel copy = new NovelAiModel();
        copy.setModelId(source.getModelId());
        copy.setModelName(source.getModelName());
        copy.setProviderType(source.getProviderType());
        copy.setBaseUrl(source.getBaseUrl());
        copy.setModelCode(source.getModelCode());
        copy.setTemperature(source.getTemperature());
        copy.setMaxTokens(source.getMaxTokens());
        copy.setTimeoutMs(source.getTimeoutMs());
        copy.setIsActive(source.getIsActive());
        copy.setStatus(source.getStatus());
        copy.setRemark(source.getRemark());
        return copy;
    }
}