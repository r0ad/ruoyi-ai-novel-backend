package com.ruoyi.novel.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.agent.NovelToolContext;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.domain.NovelMetaEntity;
import com.ruoyi.novel.domain.NovelMetaGraph;
import com.ruoyi.novel.service.INovelMetaService;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;

@Component
public class MetaTools
{
    @Autowired
    private INovelMetaService novelMetaService;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    @Tool(description = "保存 Meta 实体（角色/地点/物品等）")
    public String saveMetaEntity(
        @ToolParam(description = "实体类型 character/location/item/event/theme") String entityType,
        @ToolParam(description = "名称") String name,
        @ToolParam(description = "描述") String description)
    {
        NovelToolContext.requireWriteAllowed();
        Long projectId = NovelToolContext.getProjectId();
        NovelMetaEntity entity = new NovelMetaEntity();
        entity.setProjectId(projectId);
        entity.setEntityType(entityType);
        entity.setName(name);
        entity.setDescription(description);
        novelMetaService.insertEntity(entity);
        publishTool("saveMetaEntity", name);
        return "Meta 实体已保存：" + name;
    }

    @Tool(description = "获取项目 Meta 关系图谱")
    public String getMetaGraph()
    {
        Long projectId = NovelToolContext.getProjectId();
        NovelMetaGraph graph = novelMetaService.selectGraphByProjectId(projectId);
        publishTool("getMetaGraph", projectId);
        return JSON.toJSONString(graph);
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
