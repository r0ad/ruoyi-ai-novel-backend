package com.ruoyi.novel.agent;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.novel.agent.tools.NovelToolPolicy;
import com.ruoyi.novel.agent.tools.NovelToolRegistry;
import com.ruoyi.novel.ai.config.NovelAiModelFactory;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepCode;

@Component
public class NovelAgentFactory
{
    @Autowired
    private NovelAiModelFactory novelAiModelFactory;

    @Autowired
    private NovelToolPolicy novelToolPolicy;

    @Autowired
    private NovelToolRegistry novelToolRegistry;

    @Autowired
    private NovelToolContextPropagator novelToolContextPropagator;

    public ChatClient createForStep(NovelWorkflowStepCode stepCode)
    {
        return createForStep(stepCode, null);
    }

    public ChatClient createForStep(NovelWorkflowStepCode stepCode, NovelToolContext.Context toolContext)
    {
        Long userId = resolveUserId(toolContext);
        if (!novelAiModelFactory.isReady(userId))
        {
            throw new ServiceException(NovelAiModelFactory.AI_MODEL_NOT_CONFIGURED);
        }
        List<String> toolNames = novelToolPolicy.toolNamesFor(stepCode);
        List<Object> tools = novelToolRegistry.resolveTools(toolNames);
        return buildClient(userId, toolContext, tools);
    }

    private ChatClient buildClient(Long userId, NovelToolContext.Context toolContext, List<Object> selectedTools)
    {
        List<Object> tools = selectedTools != null ? selectedTools : java.util.Collections.emptyList();
        if (toolContext != null)
        {
            tools = wrapTools(tools, toolContext);
        }
        return novelAiModelFactory.buildAgentClient(userId, tools.toArray(new Object[tools.size()]));
    }

    private List<Object> wrapTools(List<Object> selectedTools, NovelToolContext.Context toolContext)
    {
        List<Object> wrapped = new ArrayList<Object>();
        for (Object tool : selectedTools)
        {
            if (tool instanceof ToolCallback)
            {
                wrapped.add(novelToolContextPropagator.wrapToolCallback((ToolCallback) tool, toolContext));
            }
            else
            {
                wrapped.add(novelToolContextPropagator.wrapToolBean(tool, toolContext));
            }
        }
        return wrapped;
    }

    private Long resolveUserId(NovelToolContext.Context toolContext)
    {
        if (toolContext != null && toolContext.userId != null)
        {
            return toolContext.userId;
        }
        return SecurityUtils.getUserId();
    }
}
