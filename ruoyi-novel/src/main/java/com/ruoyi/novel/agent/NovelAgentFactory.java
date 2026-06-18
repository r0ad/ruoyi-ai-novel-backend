package com.ruoyi.novel.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;
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

    public ChatClient createForStep(NovelWorkflowStepCode stepCode)
    {
        if (!novelAiModelFactory.isReady())
        {
            throw new ServiceException("未配置激活的 AI 模型，请先在「AI模型管理」中添加并激活模型");
        }
        switch (stepCode)
        {
            case INIT_PROJECT:
                return buildWithExtras(projectTools);
            case WORLD_BUILDING:
            case PLOT_OUTLINE:
                return buildWithExtras(projectTools, settingTools);
            case CHARACTER_DESIGN:
                return buildWithExtras(projectTools, settingTools, metaTools);
            case CHAPTER_PLANNING:
                return buildWithExtras(projectTools, chapterTools, settingTools);
            case WRITE_CHAPTER:
                return buildWithExtras(
                    projectTools, settingTools, chapterTools, contextTools, metaTools, reviewTools);
            case REVIEW_CHAPTER:
            case FINAL_REVIEW:
                return buildWithExtras(
                    projectTools, chapterTools, metaTools, contextTools, reviewTools);
            default:
                return buildWithExtras(projectTools, settingTools);
        }
    }

    private ChatClient buildWithExtras(Object... domainTools)
    {
        ToolCallback[] extras = novelAgentUtilsBridge.getExtraToolCallbacks();
        if (extras.length == 0)
        {
            return novelAiModelFactory.buildAgentClient(domainTools);
        }
        Object[] all = new Object[domainTools.length + extras.length];
        System.arraycopy(domainTools, 0, all, 0, domainTools.length);
        System.arraycopy(extras, 0, all, domainTools.length, extras.length);
        return novelAiModelFactory.buildAgentClient(all);
    }
}
