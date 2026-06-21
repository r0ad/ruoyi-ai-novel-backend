package com.ruoyi.novel.ai.domain.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExtractResult
{
    private String taskType = "extract_meta";
    private Map<String, Object> target;
    private List<ExtractEntityItem> entities = new ArrayList<ExtractEntityItem>();
    private List<ExtractRelationItem> relations = new ArrayList<ExtractRelationItem>();
    private List<ExtractConflictItem> conflicts = new ArrayList<ExtractConflictItem>();
    private String rawText;

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public Map<String, Object> getTarget() { return target; }
    public void setTarget(Map<String, Object> target) { this.target = target; }
    public List<ExtractEntityItem> getEntities() { return entities; }
    public void setEntities(List<ExtractEntityItem> entities) { this.entities = entities; }
    public List<ExtractRelationItem> getRelations() { return relations; }
    public void setRelations(List<ExtractRelationItem> relations) { this.relations = relations; }
    public List<ExtractConflictItem> getConflicts() { return conflicts; }
    public void setConflicts(List<ExtractConflictItem> conflicts) { this.conflicts = conflicts; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
}
