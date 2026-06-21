package com.ruoyi.novel.ai.task.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class NovelAiTask
{
    private Long taskId;
    private Long projectId;
    private String taskType;
    private String targetType;
    private String targetId;
    private String status;
    private String inputJson;
    private String resultJson;
    private String errorMessage;
    private Long sessionId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date appliedAt;
    private String appliedBy;
    private String createBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInputJson() { return inputJson; }
    public void setInputJson(String inputJson) { this.inputJson = inputJson; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Date getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Date appliedAt) { this.appliedAt = appliedAt; }
    public String getAppliedBy() { return appliedBy; }
    public void setAppliedBy(String appliedBy) { this.appliedBy = appliedBy; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

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
