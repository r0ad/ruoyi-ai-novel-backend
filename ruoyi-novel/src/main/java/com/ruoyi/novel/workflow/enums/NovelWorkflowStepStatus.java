package com.ruoyi.novel.workflow.enums;

public enum NovelWorkflowStepStatus
{
    PENDING("pending"),
    RUNNING("running"),
    WAITING_CONFIRM("waiting_confirm"),
    COMPLETED("completed"),
    FAILED("failed");

    private final String code;

    NovelWorkflowStepStatus(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }
}
