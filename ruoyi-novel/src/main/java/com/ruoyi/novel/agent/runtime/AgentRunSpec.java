package com.ruoyi.novel.agent.runtime;

import com.ruoyi.novel.agent.NovelToolContext;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRun;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepCode;

public class AgentRunSpec
{
    private Long runId;

    private Long stepId;

    private NovelWorkflowRun run;

    private NovelWorkflowStepCode stepCode;

    private Long sessionId;

    private NovelToolContext.Context toolContext;

    public static AgentRunSpec workflow(Long runId, Long stepId, NovelWorkflowRun run,
        NovelWorkflowStepCode stepCode, Long sessionId, NovelToolContext.Context toolContext)
    {
        AgentRunSpec spec = new AgentRunSpec();
        spec.setRunId(runId);
        spec.setStepId(stepId);
        spec.setRun(run);
        spec.setStepCode(stepCode);
        spec.setSessionId(sessionId);
        spec.setToolContext(toolContext);
        return spec;
    }

    public Long getRunId()
    {
        return runId;
    }

    public void setRunId(Long runId)
    {
        this.runId = runId;
    }

    public Long getStepId()
    {
        return stepId;
    }

    public void setStepId(Long stepId)
    {
        this.stepId = stepId;
    }

    public NovelWorkflowRun getRun()
    {
        return run;
    }

    public void setRun(NovelWorkflowRun run)
    {
        this.run = run;
    }

    public NovelWorkflowStepCode getStepCode()
    {
        return stepCode;
    }

    public void setStepCode(NovelWorkflowStepCode stepCode)
    {
        this.stepCode = stepCode;
    }

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public NovelToolContext.Context getToolContext()
    {
        return toolContext;
    }

    public void setToolContext(NovelToolContext.Context toolContext)
    {
        this.toolContext = toolContext;
    }
}
