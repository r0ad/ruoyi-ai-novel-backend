package com.ruoyi.novel.rag.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.novel.rag.domain.NovelRagChunk;

public interface NovelRagChunkMapper
{
    List<NovelRagChunk> selectByProjectId(Long projectId);

    List<NovelRagChunk> searchByKeyword(@Param("projectId") Long projectId, @Param("keyword") String keyword, @Param("limit") int limit);

    int deleteByProjectAndSource(@Param("projectId") Long projectId, @Param("sourceType") String sourceType, @Param("sourceId") Long sourceId);

    int insertNovelRagChunk(NovelRagChunk chunk);
}
