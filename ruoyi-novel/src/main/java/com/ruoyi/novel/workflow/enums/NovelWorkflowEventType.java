package com.ruoyi.novel.workflow.enums;

public enum NovelWorkflowEventType
{
    STEP_STARTED("step_started"),
    STEP_COMPLETED("step_completed"),
    STEP_FAILED("step_failed"),
    TOOL_CALL("tool_call"),
    TOKEN("token"),
    TODO_UPDATE("todo_update"),
    USER_CONFIRM("user_confirm"),
    RUN_STATUS("run_status");

    private final String code;

    NovelWorkflowEventType(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }
}
