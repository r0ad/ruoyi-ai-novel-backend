package com.ruoyi.novel.workflow.domain;

/**
 * 工作流步骤内 Agent 对话请求
 */
public class NovelWorkflowChatRequest
{
    private String message;
    private String clientMessageId;

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getClientMessageId()
    {
        return clientMessageId;
    }

    public void setClientMessageId(String clientMessageId)
    {
        this.clientMessageId = clientMessageId;
    }
}
