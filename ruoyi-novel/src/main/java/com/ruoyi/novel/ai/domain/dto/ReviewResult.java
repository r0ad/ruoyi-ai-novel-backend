package com.ruoyi.novel.ai.domain.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReviewResult
{
    private String taskType = "review_chapter";
    private Map<String, Object> target;
    private boolean passed = true;
    private List<ReviewIssue> issues = new ArrayList<ReviewIssue>();
    private String rawText;

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public Map<String, Object> getTarget() { return target; }
    public void setTarget(Map<String, Object> target) { this.target = target; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public List<ReviewIssue> getIssues() { return issues; }
    public void setIssues(List<ReviewIssue> issues) { this.issues = issues; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
}
