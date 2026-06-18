package com.ruoyi.novel.workflow.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工作流运行实例 novel_workflow_run
 */
public class NovelWorkflowRun extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long runId;
    private Long projectId;
    private Long userId;
    private String workflowType;
    private String status;
    private String currentStep;
    private String contextJson;
    private String userIdea;

    public Long getRunId()
    {
        return runId;
    }

    public void setRunId(Long runId)
    {
        this.runId = runId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getWorkflowType()
    {
        return workflowType;
    }

    public void setWorkflowType(String workflowType)
    {
        this.workflowType = workflowType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getCurrentStep()
    {
        return currentStep;
    }

    public void setCurrentStep(String currentStep)
    {
        this.currentStep = currentStep;
    }

    public String getContextJson()
    {
        return contextJson;
    }

    public void setContextJson(String contextJson)
    {
        this.contextJson = contextJson;
    }

    public String getUserIdea()
    {
        return userIdea;
    }

    public void setUserIdea(String userIdea)
    {
        this.userIdea = userIdea;
    }
}
