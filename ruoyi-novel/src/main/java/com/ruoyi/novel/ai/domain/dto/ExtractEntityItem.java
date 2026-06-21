package com.ruoyi.novel.ai.domain.dto;

public class ExtractEntityItem
{
    private String entityType;
    private String name;
    private String description;
    private boolean isNew;

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isNew() { return isNew; }
    public void setNew(boolean aNew) { isNew = aNew; }
}
