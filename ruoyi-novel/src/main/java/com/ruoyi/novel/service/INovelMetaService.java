package com.ruoyi.novel.service;

import java.util.List;
import com.ruoyi.novel.domain.NovelMetaEntity;
import com.ruoyi.novel.domain.NovelMetaGraph;
import com.ruoyi.novel.domain.NovelMetaRelation;

public interface INovelMetaService
{
    public NovelMetaGraph selectGraphByProjectId(Long projectId);

    public List<NovelMetaEntity> selectEntityList(NovelMetaEntity entity);

    public NovelMetaEntity selectEntityById(Long entityId);

    public int insertEntity(NovelMetaEntity entity);

    public int updateEntity(NovelMetaEntity entity);

    public int deleteEntityByIds(Long[] entityIds);

    public List<NovelMetaRelation> selectRelationList(NovelMetaRelation relation);

    public int insertRelation(NovelMetaRelation relation);

    public int updateRelation(NovelMetaRelation relation);

    public int deleteRelationByIds(Long[] relationIds);
}