package com.ruoyi.novel.workflow.domain;

import java.util.Date;

/**
 * 工作流步骤记录 novel_workflow_step
 */
public class NovelWorkflowStep
{
    private Long stepId;
    private Long runId;
    private String stepCode;
    private String status;
    private String inputJson;
    private String outputSnapshot;
    private Long agentSessionId;
    private String errorMessage;
    private Date startedAt;
    private Date finishedAt;
    private Date createTime;

    public Long getStepId()
    {
        return stepId;
    }

    public void setStepId(Long stepId)
    {
        this.stepId = stepId;
    }

    public Long getRunId()
    {
        return runId;
    }

    public void setRunId(Long runId)
    {
        this.runId = runId;
    }

    public String getStepCode()
    {
        return stepCode;
    }

    public void setStepCode(String stepCode)
    {
        this.stepCode = stepCode;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getInputJson()
    {
        return inputJson;
    }

    public void setInputJson(String inputJson)
    {
        this.inputJson = inputJson;
    }

    public String getOutputSnapshot()
    {
        return outputSnapshot;
    }

    public void setOutputSnapshot(String outputSnapshot)
    {
        this.outputSnapshot = outputSnapshot;
    }

    public Long getAgentSessionId()
    {
        return agentSessionId;
    }

    public void setAgentSessionId(Long agentSessionId)
    {
        this.agentSessionId = agentSessionId;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public Date getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt(Date startedAt)
    {
        this.startedAt = startedAt;
    }

    public Date getFinishedAt()
    {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt)
    {
        this.finishedAt = finishedAt;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
