package com.ruoyi.novel.ai.session.domain;

import java.util.Date;

public class NovelAiMessage
{
    private Long messageId;
    private Long sessionId;
    private String role;
    private String content;
    private Integer tokenUsage;
    private Date createTime;

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(Integer tokenUsage) { this.tokenUsage = tokenUsage; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
