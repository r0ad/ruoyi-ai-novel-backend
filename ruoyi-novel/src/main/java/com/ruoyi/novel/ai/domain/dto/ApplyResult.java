package com.ruoyi.novel.ai.domain.dto;

public class ApplyResult
{
    private int entitiesInserted;
    private int entitiesUpdated;
    private int relationsInserted;
    private String message;

    public int getEntitiesInserted() { return entitiesInserted; }
    public void setEntitiesInserted(int entitiesInserted) { this.entitiesInserted = entitiesInserted; }
    public int getEntitiesUpdated() { return entitiesUpdated; }
    public void setEntitiesUpdated(int entitiesUpdated) { this.entitiesUpdated = entitiesUpdated; }
    public int getRelationsInserted() { return relationsInserted; }
    public void setRelationsInserted(int relationsInserted) { this.relationsInserted = relationsInserted; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
