package com.ruoyi.novel.ai.domain.dto;

import java.util.HashMap;
import java.util.Map;

public class SettingDraftResult
{
    private String taskType;
    private String settingType;
    private String title;
    private String content;
    private String rawText;
    private Map<String, Object> target = new HashMap<String, Object>();

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getSettingType() { return settingType; }
    public void setSettingType(String settingType) { this.settingType = settingType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public Map<String, Object> getTarget() { return target; }
    public void setTarget(Map<String, Object> target) { this.target = target; }
}
