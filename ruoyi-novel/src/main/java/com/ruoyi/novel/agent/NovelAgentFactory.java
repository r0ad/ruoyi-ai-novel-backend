package com.ruoyi.novel.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.novel.agent.tools.ChapterTools;
import com.ruoyi.novel.agent.tools.ContextTools;
import com.ruoyi.novel.agent.tools.MetaTools;
import com.ruoyi.novel.agent.tools.ProjectTools;
import com.ruoyi.novel.agent.tools.ReviewTools;
import com.ruoyi.novel.agent.tools.SettingTools;
import com.ruoyi.novel.ai.config.NovelAiModelFactory;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepCode;

/**
 * 按工作流步骤装配 Tools 并构建 Agent ChatClient
 */
@Component
public class NovelAgentFactory
{
    @Autowired
    private NovelAiModelFactory novelAiModelFactory;

    @Autowired
    private ProjectTools projectTools;

    @Autowired
    private SettingTools settingTools;

    @Autowired
    private ChapterTools chapterTools;

    @Autowired
    private MetaTools metaTools;

    @Autowired
    private ContextTools contextTools;

    @Autowired
    private ReviewTools reviewTools;

    @Autowired
    private NovelAgentUtilsBridge novelAgentUtilsBridge;

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
        switch (stepCode)
        {
            case INIT_PROJECT:
                return buildWithExtras(toolContext, projectTools);
            case WORLD_BUILDING:
            case PLOT_OUTLINE:
                return buildWithExtras(toolContext, projectTools, settingTools);
            case CHARACTER_DESIGN:
                return buildWithExtras(toolContext, projectTools, settingTools, metaTools);
            case CHAPTER_PLANNING:
                return buildWithExtras(toolContext, projectTools, chapterTools, settingTools);
            case WRITE_CHAPTER:
                return buildWithExtras(toolContext,
                    projectTools, settingTools, chapterTools, contextTools, metaTools, reviewTools);
            case REVIEW_CHAPTER:
            case FINAL_REVIEW:
                return buildWithExtras(toolContext,
                    projectTools, settingTools, chapterTools, metaTools, contextTools, reviewTools);
            default:
                return buildWithExtras(toolContext, projectTools, settingTools);
        }
    }

    private ChatClient buildWithExtras(NovelToolContext.Context toolContext, Object... domainTools)
    {
        Long userId = resolveUserId(toolContext);
        Object[] tools = domainTools;
        if (toolContext != null)
        {
            tools = new Object[domainTools.length];
            for (int i = 0; i < domainTools.length; i++)
            {
                tools[i] = novelToolContextPropagator.wrapToolBean(domainTools[i], toolContext);
            }
        }
        ToolCallback[] extras = novelAgentUtilsBridge.getExtraToolCallbacks();
        if (extras.length == 0)
        {
            return novelAiModelFactory.buildAgentClient(userId, tools);
        }
        ToolCallback[] wrappedExtras = extras;
        if (toolContext != null)
        {
            wrappedExtras = new ToolCallback[extras.length];
            for (int i = 0; i < extras.length; i++)
            {
                wrappedExtras[i] = novelToolContextPropagator.wrapToolCallback(extras[i], toolContext);
            }
        }
        Object[] all = new Object[tools.length + wrappedExtras.length];
        System.arraycopy(tools, 0, all, 0, tools.length);
        System.arraycopy(wrappedExtras, 0, all, tools.length, wrappedExtras.length);
        return novelAiModelFactory.buildAgentClient(userId, all);
    }

    /**
     * 优先从 toolContext 中取 userId（异步线程场景），
     * toolContext 中无 userId 时才回退到 SecurityContextHolder（HTTP 请求线程场景）。
     */
    private Long resolveUserId(NovelToolContext.Context toolContext)
    {
        if (toolContext != null && toolContext.userId != null)
        {
            return toolContext.userId;
        }
        return SecurityUtils.getUserId();
    }

    private ChatClient buildWithExtras(Object... domainTools)
    {
        return buildWithExtras(null, domainTools);
    }
}
