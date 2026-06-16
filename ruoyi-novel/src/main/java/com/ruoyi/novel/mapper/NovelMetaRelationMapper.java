package com.ruoyi.novel.mapper;

import java.util.List;
import com.ruoyi.novel.domain.NovelMetaRelation;

public interface NovelMetaRelationMapper
{
    public NovelMetaRelation selectNovelMetaRelationByRelationId(Long relationId);

    public List<NovelMetaRelation> selectNovelMetaRelationList(NovelMetaRelation relation);

    public int insertNovelMetaRelation(NovelMetaRelation relation);

    public int updateNovelMetaRelation(NovelMetaRelation relation);

    public int deleteNovelMetaRelationByRelationIds(Long[] relationIds);

    public int deleteNovelMetaRelationByEntityIds(Long[] entityIds);
}