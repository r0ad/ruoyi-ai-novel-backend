package com.ruoyi.novel.workflow.domain;

/**
 * 启动工作流请求
 */
public class NovelWorkflowStartRequest
{
    private Long projectId;
    private String userIdea;
    private String workflowType;

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public String getUserIdea()
    {
        return userIdea;
    }

    public void setUserIdea(String userIdea)
    {
        this.userIdea = userIdea;
    }

    public String getWorkflowType()
    {
        return workflowType;
    }

    public void setWorkflowType(String workflowType)
    {
        this.workflowType = workflowType;
    }
}
