package com.ruoyi.novel.ai.domain.dto;

public class ExtractConflictItem
{
    private String name;
    private String reason;
    private String suggestion;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
}
