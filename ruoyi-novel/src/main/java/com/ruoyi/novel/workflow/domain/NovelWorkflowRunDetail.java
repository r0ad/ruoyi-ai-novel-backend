package com.ruoyi.novel.workflow.domain;

import java.util.List;

/**
 * 工作流运行详情 DTO
 */
public class NovelWorkflowRunDetail
{
    private NovelWorkflowRun run;
    private List<NovelWorkflowStep> steps;
    private NovelWorkflowStep currentStepRecord;
    private List<NovelWorkflowEvent> events;

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
}
