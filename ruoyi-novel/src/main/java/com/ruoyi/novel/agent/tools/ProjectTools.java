package com.ruoyi.novel.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.agent.NovelToolContext;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.service.INovelProjectService;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;

@Component
public class ProjectTools
{
    @Autowired
    private INovelProjectService novelProjectService;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    @Tool(description = "更新小说项目基本信息（书名、类型、简介、风格指南）")
    public String updateProject(
        @ToolParam(description = "书名") String title,
        @ToolParam(description = "类型或题材") String genre,
        @ToolParam(description = "简介") String summary,
        @ToolParam(description = "风格指南") String styleGuide)
    {
        NovelToolContext.requireWriteAllowed();
        Long projectId = NovelToolContext.getProjectId();
        NovelProject project = novelProjectService.selectNovelProjectByProjectId(projectId);
        if (project == null)
        {
            return "项目不存在";
        }
        if (StringUtils.isNotEmpty(title))
        {
            project.setTitle(title);
        }
        if (StringUtils.isNotEmpty(genre))
        {
            project.setGenre(genre);
        }
        if (summary != null)
        {
            project.setSummary(summary);
        }
        if (styleGuide != null)
        {
            project.setStyleGuide(styleGuide);
        }
        String operator = NovelToolContext.getOperator();
        project.setUpdateBy(StringUtils.isNotEmpty(operator) ? operator : SecurityUtils.getUsername());
        novelProjectService.updateNovelProject(project);
        publishTool("updateProject", project.getTitle());
        return "已更新项目：" + project.getTitle();
    }

    @Tool(description = "获取当前小说项目详情")
    public String getProjectInfo()
    {
        Long projectId = NovelToolContext.getProjectId();
        if (projectId == null)
        {
            return "错误：工具上下文丢失(projectId 为空)，请重试本步骤";
        }
        NovelProject project = novelProjectService.selectNovelProjectByProjectId(projectId);
        if (project == null)
        {
            return "项目不存在";
        }
        publishTool("getProjectInfo", project.getTitle());
        return JSON.toJSONString(project);
    }

    private void publishTool(String tool, Object detail)
    {
        NovelToolContext.Context ctx = NovelToolContext.get();
        if (ctx != null)
        {
            workflowEventPublisher.publish(ctx.runId, ctx.stepId,
                NovelWorkflowEventType.TOOL_CALL.getCode(),
                JSON.parseObject("{\"tool\":\"" + tool + "\",\"detail\":" + JSON.toJSONString(detail) + "}"));
        }
    }
}