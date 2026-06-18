package com.ruoyi.novel.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.agent.NovelToolContext;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.domain.NovelSetting;
import com.ruoyi.novel.service.INovelSettingService;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;

@Component
public class SettingTools
{
    @Autowired
    private INovelSettingService novelSettingService;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    @Tool(description = "保存设定文档，类型包括 characters/world/outline/style/scene/metrics")
    public String saveSetting(
        @ToolParam(description = "设定类型") String settingType,
        @ToolParam(description = "Markdown 内容") String content,
        @ToolParam(description = "标题，可选") String title)
    {
        NovelToolContext.requireWriteAllowed();
        Long projectId = NovelToolContext.getProjectId();
        NovelSetting setting = new NovelSetting();
        setting.setProjectId(projectId);
        setting.setSettingType(settingType);
        setting.setContent(content);
        if (StringUtils.isNotEmpty(title))
        {
            setting.setTitle(title);
        }
        novelSettingService.saveNovelSetting(setting);
        publishTool("saveSetting", settingType);
        return "设定已保存：" + settingType;
    }

    @Tool(description = "获取指定类型的设定文档")
    public String getSetting(@ToolParam(description = "设定类型") String settingType)
    {
        Long projectId = NovelToolContext.getProjectId();
        NovelSetting setting = novelSettingService.selectNovelSettingByProjectAndType(projectId, settingType);
        publishTool("getSetting", settingType);
        if (setting == null || StringUtils.isEmpty(setting.getContent()))
        {
            return "（暂无内容）";
        }
        return setting.getContent();
    }

    private void publishTool(String tool, Object detail)
    {
        NovelToolContext.Context ctx = NovelToolContext.get();
        if (ctx != null)
        {
            workflowEventPublisher.publish(ctx.runId, ctx.stepId,
                NovelWorkflowEventType.TOOL_CALL.getCode(),
                JSON.parseObject("{\"tool\":\"" + tool + "\",\"detail\":" + JSON.toJSONString(String.valueOf(detail)) + "}"));
        }
    }
}
