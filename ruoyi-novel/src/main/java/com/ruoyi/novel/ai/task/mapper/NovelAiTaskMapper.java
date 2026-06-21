package com.ruoyi.novel.ai.task.mapper;

import java.util.List;
import com.ruoyi.novel.ai.task.domain.NovelAiTask;

public interface NovelAiTaskMapper
{
    NovelAiTask selectNovelAiTaskByTaskId(Long taskId);

    List<NovelAiTask> selectNovelAiTaskList(NovelAiTask query);

    int insertNovelAiTask(NovelAiTask task);

    int updateNovelAiTask(NovelAiTask task);
}
