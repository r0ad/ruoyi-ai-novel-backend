package com.ruoyi.novel.ai.task.service;

import java.util.Date;
import java.util.List;
import com.ruoyi.novel.ai.domain.dto.AiTaskApplyRequest;
import com.ruoyi.novel.ai.domain.dto.AiTaskCreateRequest;
import com.ruoyi.novel.ai.domain.dto.ApplyResult;
import com.ruoyi.novel.ai.task.domain.NovelAiTask;

public interface INovelAiTaskService
{
    NovelAiTask createAndRun(AiTaskCreateRequest request, String operator);

    NovelAiTask getTask(Long taskId);

    List<NovelAiTask> listTasks(NovelAiTask query);

    ApplyResult applyTask(Long taskId, AiTaskApplyRequest request, String operator);
}
