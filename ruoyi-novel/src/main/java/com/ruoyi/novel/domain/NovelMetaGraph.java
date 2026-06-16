package com.ruoyi.novel.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Meta图谱数据（实体 + 关系）
 */
public class NovelMetaGraph
{
    private List<NovelMetaEntity> entities = new ArrayList<NovelMetaEntity>();
    private List<NovelMetaRelation> relations = new ArrayList<NovelMetaRelation>();

    public List<NovelMetaEntity> getEntities() { return entities; }
    public void setEntities(List<NovelMetaEntity> entities) { this.entities = entities; }
    public List<NovelMetaRelation> getRelations() { return relations; }
    public void setRelations(List<NovelMetaRelation> relations) { this.relations = relations; }
}