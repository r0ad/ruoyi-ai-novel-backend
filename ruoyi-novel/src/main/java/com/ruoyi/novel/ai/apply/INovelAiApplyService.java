package com.ruoyi.novel.ai.apply;

import com.ruoyi.novel.ai.domain.dto.AiTaskApplyRequest;
import com.ruoyi.novel.ai.domain.dto.ApplyResult;
import com.ruoyi.novel.ai.task.domain.NovelAiTask;

public interface INovelAiApplyService
{
    ApplyResult apply(NovelAiTask task, AiTaskApplyRequest request, String operator);
}
