package com.ruoyi.novel.agent.capability;

import org.springframework.ai.tool.ToolCallback;

public class NovelAgentCapability
{
    private final String name;

    private final boolean available;

    private final Object tool;

    private final String detail;

    private NovelAgentCapability(String name, boolean available, Object tool, String detail)
    {
        this.name = name;
        this.available = available;
        this.tool = tool;
        this.detail = detail;
    }

    public static NovelAgentCapability available(String name, Object tool, String detail)
    {
        return new NovelAgentCapability(name, true, tool, detail);
    }

    public static NovelAgentCapability unavailable(String name, String detail)
    {
        return new NovelAgentCapability(name, false, null, detail);
    }

    public String getName()
    {
        return name;
    }

    public boolean isAvailable()
    {
        return available;
    }

    public Object getTool()
    {
        return tool;
    }

    public ToolCallback getToolCallback()
    {
        return tool instanceof ToolCallback ? (ToolCallback) tool : null;
    }

    public String getDetail()
    {
        return detail;
    }
}
