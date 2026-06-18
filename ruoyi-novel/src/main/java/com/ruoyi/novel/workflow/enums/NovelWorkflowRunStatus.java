package com.ruoyi.novel.workflow.enums;

public enum NovelWorkflowRunStatus
{
    RUNNING("running"),
    PAUSED("paused"),
    WAITING_CONFIRM("waiting_confirm"),
    COMPLETED("completed"),
    FAILED("failed");

    private final String code;

    NovelWorkflowRunStatus(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }
}
