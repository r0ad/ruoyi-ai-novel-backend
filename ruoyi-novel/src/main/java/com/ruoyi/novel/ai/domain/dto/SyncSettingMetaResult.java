package com.ruoyi.novel.ai.domain.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncSettingMetaResult
{
    private String taskType;
    private String rawText;
    private Map<String, Object> target = new HashMap<String, Object>();
    private List<Map<String, Object>> syncActions = new ArrayList<Map<String, Object>>();

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public Map<String, Object> getTarget() { return target; }
    public void setTarget(Map<String, Object> target) { this.target = target; }
    public List<Map<String, Object>> getSyncActions() { return syncActions; }
    public void setSyncActions(List<Map<String, Object>> syncActions) { this.syncActions = syncActions; }
}
