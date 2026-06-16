package com.ruoyi.novel.mapper;

import java.util.List;
import com.ruoyi.novel.domain.NovelMetaEntity;

public interface NovelMetaEntityMapper
{
    public NovelMetaEntity selectNovelMetaEntityByEntityId(Long entityId);

    public List<NovelMetaEntity> selectNovelMetaEntityList(NovelMetaEntity entity);

    public int insertNovelMetaEntity(NovelMetaEntity entity);

    public int updateNovelMetaEntity(NovelMetaEntity entity);

    public int deleteNovelMetaEntityByEntityIds(Long[] entityIds);
}