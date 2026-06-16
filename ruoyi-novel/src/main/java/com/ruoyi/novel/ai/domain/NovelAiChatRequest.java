package com.ruoyi.novel.ai.domain;

public class NovelAiChatRequest
{
    private Long projectId;
    private Long chapterId;
    private String sessionType;
    private String message;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getChapterId() { return chapterId; }
    public void setChapterId(Long chapterId) { this.chapterId = chapterId; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
