package com.ruoyi.novel.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.novel.domain.NovelMetaEntity;
import com.ruoyi.novel.domain.NovelMetaGraph;
import com.ruoyi.novel.domain.NovelMetaRelation;
import com.ruoyi.novel.mapper.NovelMetaEntityMapper;
import com.ruoyi.novel.mapper.NovelMetaRelationMapper;
import com.ruoyi.novel.service.INovelMetaService;

@Service
public class NovelMetaServiceImpl implements INovelMetaService
{
    @Autowired
    private NovelMetaEntityMapper novelMetaEntityMapper;

    @Autowired
    private NovelMetaRelationMapper novelMetaRelationMapper;

    @Override
    public NovelMetaGraph selectGraphByProjectId(Long projectId)
    {
        NovelMetaGraph graph = new NovelMetaGraph();
        NovelMetaEntity entityQuery = new NovelMetaEntity();
        entityQuery.setProjectId(projectId);
        graph.setEntities(novelMetaEntityMapper.selectNovelMetaEntityList(entityQuery));
        NovelMetaRelation relationQuery = new NovelMetaRelation();
        relationQuery.setProjectId(projectId);
        graph.setRelations(novelMetaRelationMapper.selectNovelMetaRelationList(relationQuery));
        return graph;
    }

    @Override
    public List<NovelMetaEntity> selectEntityList(NovelMetaEntity entity)
    {
        return novelMetaEntityMapper.selectNovelMetaEntityList(entity);
    }

    @Override
    public NovelMetaEntity selectEntityById(Long entityId)
    {
        return novelMetaEntityMapper.selectNovelMetaEntityByEntityId(entityId);
    }

    @Override
    public int insertEntity(NovelMetaEntity entity)
    {
        return novelMetaEntityMapper.insertNovelMetaEntity(entity);
    }

    @Override
    public int updateEntity(NovelMetaEntity entity)
    {
        return novelMetaEntityMapper.updateNovelMetaEntity(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteEntityByIds(Long[] entityIds)
    {
        if (entityIds == null || entityIds.length == 0)
        {
            return 0;
        }
        novelMetaRelationMapper.deleteNovelMetaRelationByEntityIds(entityIds);
        return novelMetaEntityMapper.deleteNovelMetaEntityByEntityIds(entityIds);
    }

    @Override
    public List<NovelMetaRelation> selectRelationList(NovelMetaRelation relation)
    {
        return novelMetaRelationMapper.selectNovelMetaRelationList(relation);
    }

    @Override
    public int insertRelation(NovelMetaRelation relation)
    {
        if (relation.getWeight() == null)
        {
            relation.setWeight(new java.math.BigDecimal("1.00"));
        }
        return novelMetaRelationMapper.insertNovelMetaRelation(relation);
    }

    @Override
    public int updateRelation(NovelMetaRelation relation)
    {
        return novelMetaRelationMapper.updateNovelMetaRelation(relation);
    }

    @Override
    public int deleteRelationByIds(Long[] relationIds)
    {
        return novelMetaRelationMapper.deleteNovelMetaRelationByRelationIds(relationIds);
    }
}