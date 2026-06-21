package com.ruoyi.novel.ai.domain.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReviewIssue
{
    private String id;
    private String severity;
    private String category;
    private String summary;
    private List<Map<String, Object>> suggestedActions = new ArrayList<Map<String, Object>>();
    private boolean autoFixable;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<Map<String, Object>> getSuggestedActions() { return suggestedActions; }
    public void setSuggestedActions(List<Map<String, Object>> suggestedActions) { this.suggestedActions = suggestedActions; }
    public boolean isAutoFixable() { return autoFixable; }
    public void setAutoFixable(boolean autoFixable) { this.autoFixable = autoFixable; }
}
