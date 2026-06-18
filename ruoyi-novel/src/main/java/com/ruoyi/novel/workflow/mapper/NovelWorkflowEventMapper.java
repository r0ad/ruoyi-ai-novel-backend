package com.ruoyi.novel.workflow.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.novel.workflow.domain.NovelWorkflowEvent;

public interface NovelWorkflowEventMapper
{
    List<NovelWorkflowEvent> selectEventsByRunIdAfterId(@Param("runId") Long runId, @Param("afterEventId") Long afterEventId);

    int insertNovelWorkflowEvent(NovelWorkflowEvent event);
}
