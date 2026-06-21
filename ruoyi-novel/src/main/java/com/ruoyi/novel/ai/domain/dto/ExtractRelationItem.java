package com.ruoyi.novel.ai.domain.dto;

public class ExtractRelationItem
{
    private String from;
    private String to;
    private String relationType;
    private boolean isNew;

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public boolean isNew() { return isNew; }
    public void setNew(boolean aNew) { isNew = aNew; }
}
