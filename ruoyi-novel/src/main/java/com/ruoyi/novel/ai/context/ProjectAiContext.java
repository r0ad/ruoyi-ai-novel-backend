package com.ruoyi.novel.ai.context;

public class ProjectAiContext
{
    private String systemPrompt;
    private String userPrompt;

    public ProjectAiContext(String systemPrompt, String userPrompt)
    {
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
    }

    public String getSystemPrompt() { return systemPrompt; }
    public String getUserPrompt() { return userPrompt; }
}
