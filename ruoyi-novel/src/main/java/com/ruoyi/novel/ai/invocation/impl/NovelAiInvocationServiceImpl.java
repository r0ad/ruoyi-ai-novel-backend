package com.ruoyi.novel.ai.invocation.impl;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.config.NovelAiModelFactory;
import com.ruoyi.novel.ai.domain.NovelAiModel;
import com.ruoyi.novel.ai.invocation.INovelAiInvocationService;
import com.ruoyi.novel.ai.invocation.domain.AiInvocationRecord;

@Service
public class NovelAiInvocationServiceImpl implements INovelAiInvocationService
{
    private static final Logger log = LoggerFactory.getLogger(NovelAiInvocationServiceImpl.class);

    private static final ThreadLocal<AiInvocationRecord> LAST_INVOCATION = new ThreadLocal<AiInvocationRecord>();

    @Autowired
    private NovelAiModelFactory novelAiModelFactory;

    @Value("${novel.ai.debug.log-prompts:true}")
    private boolean logPrompts;

    @Override
    public String invoke(String source, String systemPrompt, String userPrompt)
    {
        ChatClient client = novelAiModelFactory.getChatClient(SecurityUtils.getUserId());
        if (client == null)
        {
            throw new ServiceException(NovelAiModelFactory.AI_MODEL_NOT_CONFIGURED);
        }

        AiInvocationRecord record = new AiInvocationRecord();
        record.setTraceId(UUID.randomUUID().toString());
        record.setSource(source);
        record.setSystemPrompt(systemPrompt);
        record.setUserPrompt(userPrompt);
        fillModelInfo(record);

        long start = System.currentTimeMillis();
        try
        {
            ChatClient.CallResponseSpec call = client.prompt().system(systemPrompt).user(userPrompt).call();
            String content = call.content();
            record.setRawResponse(content);
            record.setLatencyMs(System.currentTimeMillis() - start);
            fillTokenUsage(record, call.chatResponse());
            LAST_INVOCATION.set(record);
            logInvocation(record);
            if (StringUtils.isEmpty(content))
            {
                throw new ServiceException("AI 返回内容为空");
            }
            return content;
        }
        catch (Exception ex)
        {
            record.setLatencyMs(System.currentTimeMillis() - start);
            record.setErrorMessage(ex.getMessage());
            LAST_INVOCATION.set(record);
            logInvocation(record);
            if (ex instanceof ServiceException)
            {
                throw (ServiceException) ex;
            }
            throw new ServiceException("AI 调用失败：" + ex.getMessage());
        }
    }

    @Override
    public AiInvocationRecord getLastInvocation()
    {
        return LAST_INVOCATION.get();
    }

    @Override
    public void clearLastInvocation()
    {
        LAST_INVOCATION.remove();
    }

    private void fillModelInfo(AiInvocationRecord record)
    {
        NovelAiModel model = novelAiModelFactory.getActiveModel(SecurityUtils.getUserId());
        if (model == null)
        {
            return;
        }
        record.setModelCode(model.getModelCode());
        record.setModelName(model.getModelName());
        record.setProviderType(model.getProviderType());
    }

    private void fillTokenUsage(AiInvocationRecord record, ChatResponse chatResponse)
    {
        if (chatResponse == null || chatResponse.getMetadata() == null)
        {
            return;
        }
        Usage usage = chatResponse.getMetadata().getUsage();
        if (usage != null && usage.getTotalTokens() != null)
        {
            record.setTokenUsage(usage.getTotalTokens().intValue());
        }
    }

    private void logInvocation(AiInvocationRecord record)
    {
        if (!logPrompts || !log.isDebugEnabled())
        {
            return;
        }
        log.debug("AI invoke traceId={} source={} model={} latencyMs={} tokenUsage={} systemLen={} userLen={} responseLen={} error={}",
            record.getTraceId(),
            record.getSource(),
            record.getModelCode(),
            record.getLatencyMs(),
            record.getTokenUsage(),
            textLength(record.getSystemPrompt()),
            textLength(record.getUserPrompt()),
            textLength(record.getRawResponse()),
            record.getErrorMessage());
    }

    private int textLength(String text)
    {
        return text == null ? 0 : text.length();
    }
}
