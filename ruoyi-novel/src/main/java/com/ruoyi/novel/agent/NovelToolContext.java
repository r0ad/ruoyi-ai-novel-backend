package com.ruoyi.novel.agent;

/**
 * Agent 工具调用上下文（ThreadLocal + 工作流步骤快照）
 */
public final class NovelToolContext
{
    private static final ThreadLocal<Context> HOLDER = new ThreadLocal<Context>();

    private NovelToolContext()
    {
    }

    public static void set(Long runId, Long projectId, Long stepId, boolean writeAllowed)
    {
        set(runId, projectId, stepId, writeAllowed, null);
    }

    public static void set(Long runId, Long projectId, Long stepId, boolean writeAllowed, String operator)
    {
        Context ctx = new Context();
        ctx.runId = runId;
        ctx.projectId = projectId;
        ctx.stepId = stepId;
        ctx.writeAllowed = writeAllowed;
        ctx.operator = operator;
        HOLDER.set(ctx);
    }

    /** 将已有上下文绑定到当前线程（工具调用线程使用） */
    public static void apply(Context ctx)
    {
        if (ctx != null)
        {
            HOLDER.set(ctx);
        }
    }

    public static Context get()
    {
        return HOLDER.get();
    }

    public static Long getProjectId()
    {
        Context ctx = HOLDER.get();
        return ctx != null ? ctx.projectId : null;
    }

    public static String getOperator()
    {
        Context ctx = HOLDER.get();
        return ctx != null ? ctx.operator : null;
    }

    public static void clearThreadLocal()
    {
        HOLDER.remove();
    }

    public static void clear()
    {
        HOLDER.remove();
    }

    public static void requireWriteAllowed()
    {
        Context ctx = HOLDER.get();
        if (ctx == null || !ctx.writeAllowed)
        {
            throw new IllegalStateException("当前上下文不允许写入操作");
        }
    }

    public static class Context
    {
        public Long runId;
        public Long projectId;
        public Long stepId;
        public boolean writeAllowed;
        public String operator;
    }
}
