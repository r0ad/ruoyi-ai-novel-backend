package com.ruoyi.novel.workflow.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.novel.workflow.domain.NovelWorkflowEvent;

public interface NovelWorkflowEventMapper
{
    List<NovelWorkflowEvent> selectEventsByRunIdAfterId(@Param("runId") Long runId, @Param("afterEventId") Long afterEventId);

    /**
     * 查询结构化事件（排除瞬时的 token 事件），用于 SSE 增量重放与详情返回
     */
    List<NovelWorkflowEvent> selectReplayEventsByRunIdAfterId(@Param("runId") Long runId, @Param("afterEventId") Long afterEventId);

    int insertNovelWorkflowEvent(NovelWorkflowEvent event);
}
