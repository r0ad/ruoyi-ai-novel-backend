package com.ruoyi.novel.workflow.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStep;

public interface NovelWorkflowStepMapper
{
    NovelWorkflowStep selectNovelWorkflowStepByStepId(Long stepId);

    List<NovelWorkflowStep> selectStepsByRunId(Long runId);

    NovelWorkflowStep selectLatestStepByRunIdAndCode(@Param("runId") Long runId, @Param("stepCode") String stepCode);

    int insertNovelWorkflowStep(NovelWorkflowStep step);

    int updateNovelWorkflowStep(NovelWorkflowStep step);
}
