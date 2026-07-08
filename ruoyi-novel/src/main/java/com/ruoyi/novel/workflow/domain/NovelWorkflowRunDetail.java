package com.ruoyi.novel.workflow.domain;

import java.util.List;
import com.ruoyi.novel.ai.session.domain.NovelAiMessage;

/**
 * 工作流运行详情 DTO
 */
public class NovelWorkflowRunDetail
{
    private NovelWorkflowRun run;
    private List<NovelWorkflowStep> steps;
    private NovelWorkflowStep currentStepRecord;
    private List<NovelWorkflowEvent> events;
    private List<NovelAiMessage> stepMessages;
    private List<NovelWorkflowStepArtifact> artifacts;
    private NovelWorkflowStepReadiness stepReadiness;

    public NovelWorkflowRun getRun()
    {
        return run;
    }

    public void setRun(NovelWorkflowRun run)
    {
        this.run = run;
    }

    public List<NovelWorkflowStep> getSteps()
    {
        return steps;
    }

    public void setSteps(List<NovelWorkflowStep> steps)
    {
        this.steps = steps;
    }

    public NovelWorkflowStep getCurrentStepRecord()
    {
        return currentStepRecord;
    }

    public void setCurrentStepRecord(NovelWorkflowStep currentStepRecord)
    {
        this.currentStepRecord = currentStepRecord;
    }

    public List<NovelWorkflowEvent> getEvents()
    {
        return events;
    }

    public void setEvents(List<NovelWorkflowEvent> events)
    {
        this.events = events;
    }

    public List<NovelAiMessage> getStepMessages()
    {
        return stepMessages;
    }

    public void setStepMessages(List<NovelAiMessage> stepMessages)
    {
        this.stepMessages = stepMessages;
    }

    public List<NovelWorkflowStepArtifact> getArtifacts()
    {
        return artifacts;
    }

    public void setArtifacts(List<NovelWorkflowStepArtifact> artifacts)
    {
        this.artifacts = artifacts;
    }

    public NovelWorkflowStepReadiness getStepReadiness()
    {
        return stepReadiness;
    }

    public void setStepReadiness(NovelWorkflowStepReadiness stepReadiness)
    {
        this.stepReadiness = stepReadiness;
    }
}
