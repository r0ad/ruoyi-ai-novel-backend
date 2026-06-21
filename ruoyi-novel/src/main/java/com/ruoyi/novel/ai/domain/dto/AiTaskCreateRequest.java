package com.ruoyi.novel.ai.domain.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AiTaskCreateRequest
{
    private Long projectId;
    private String taskType;
    private String targetType;
    private String targetId;
    private Map<String, Object> options;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public Map<String, Object> getOptions() { return options; }
    public void setOptions(Map<String, Object> options) { this.options = options; }

    public Long resolveChapterId()
    {
        if (targetId == null || targetId.isEmpty())
        {
            return null;
        }
        try
        {
            return Long.valueOf(targetId);
        }
        catch (NumberFormatException ex)
        {
            return null;
        }
    }
}
