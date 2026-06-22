package com.ruoyi.novel.workflow.mapper;

import java.util.List;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRun;

public interface NovelWorkflowRunMapper
{
    NovelWorkflowRun selectNovelWorkflowRunByRunId(Long runId);

    NovelWorkflowRun selectActiveRunByProjectId(Long projectId);

    NovelWorkflowRun selectLatestRunByProjectId(Long projectId);

    List<NovelWorkflowRun> selectNovelWorkflowRunList(NovelWorkflowRun run);

    int insertNovelWorkflowRun(NovelWorkflowRun run);

    int updateNovelWorkflowRun(NovelWorkflowRun run);
}
