package com.ruoyi.novel.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Meta实体 novel_meta_entity
 */
public class NovelMetaEntity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long entityId;
    private Long projectId;
    private String entityType;
    private String name;
    private String aliases;
    private String attributes;
    private String description;
    private Long firstChapterId;
    private Long lastChapterId;

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAliases() { return aliases; }
    public void setAliases(String aliases) { this.aliases = aliases; }
    public String getAttributes() { return attributes; }
    public void setAttributes(String attributes) { this.attributes = attributes; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getFirstChapterId() { return firstChapterId; }
    public void setFirstChapterId(Long firstChapterId) { this.firstChapterId = firstChapterId; }
    public Long getLastChapterId() { return lastChapterId; }
    public void setLastChapterId(Long lastChapterId) { this.lastChapterId = lastChapterId; }
}