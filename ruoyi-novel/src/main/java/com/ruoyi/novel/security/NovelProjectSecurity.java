package com.ruoyi.novel.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.novel.ai.task.domain.NovelAiTask;
import com.ruoyi.novel.ai.task.mapper.NovelAiTaskMapper;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.domain.NovelMetaEntity;
import com.ruoyi.novel.domain.NovelMetaRelation;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.mapper.NovelChapterMapper;
import com.ruoyi.novel.mapper.NovelMetaEntityMapper;
import com.ruoyi.novel.mapper.NovelMetaRelationMapper;
import com.ruoyi.novel.mapper.NovelProjectMapper;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRun;
import com.ruoyi.novel.workflow.mapper.NovelWorkflowRunMapper;

/**
 * 小说项目数据权限校验
 *
 * <p>项目绑定归属用户，章节/设定/Meta/任务/工作流等子资源通过所属项目间接隔离。
 * 管理员可访问全部数据，普通用户仅能访问自己的项目及其子资源。</p>
 *
 * <p>仅在请求线程（Controller 层）调用；工作流异步线程、Agent 工具等内部流程不在此校验，
 * 避免因缺少安全上下文而中断。</p>
 *
 * @author novel
 */
@Component
public class NovelProjectSecurity
{
    @Autowired
    private NovelProjectMapper novelProjectMapper;

    @Autowired
    private NovelChapterMapper novelChapterMapper;

    @Autowired
    private NovelMetaEntityMapper novelMetaEntityMapper;

    @Autowired
    private NovelMetaRelationMapper novelMetaRelationMapper;

    @Autowired
    private NovelAiTaskMapper novelAiTaskMapper;

    @Autowired
    private NovelWorkflowRunMapper novelWorkflowRunMapper;

    /**
     * 当前登录用户ID
     */
    public Long currentUserId()
    {
        Long userId = SecurityUtils.getUserId();
        if (userId == null)
        {
            throw new ServiceException("未获取到当前用户信息");
        }
        return userId;
    }

    /**
     * 当前用户是否为管理员
     */
    public boolean isAdmin()
    {
        return SecurityUtils.isAdmin(currentUserId());
    }

    /**
     * 为项目列表查询附加归属过滤：管理员不限制，普通用户仅查自己的
     */
    public void scopeQuery(NovelProject query)
    {
        if (query == null)
        {
            return;
        }
        if (!isAdmin())
        {
            query.setUserId(currentUserId());
        }
    }

    /**
     * 新建项目时设置归属用户
     */
    public void assignOwner(NovelProject project)
    {
        if (project != null)
        {
            project.setUserId(currentUserId());
        }
    }

    /**
     * 校验是否有权访问指定项目
     */
    public void checkProject(Long projectId)
    {
        if (isAdmin())
        {
            return;
        }
        if (projectId == null)
        {
            throw new ServiceException("缺少项目ID");
        }
        NovelProject project = novelProjectMapper.selectNovelProjectByProjectId(projectId);
        if (project == null)
        {
            throw new ServiceException("项目不存在");
        }
        if (project.getUserId() == null || !currentUserId().equals(project.getUserId()))
        {
            throw new ServiceException("无权访问该项目");
        }
    }

    /**
     * 批量校验项目
     */
    public void checkProjects(Long[] projectIds)
    {
        if (isAdmin() || projectIds == null)
        {
            return;
        }
        for (Long projectId : projectIds)
        {
            checkProject(projectId);
        }
    }

    /**
     * 校验是否有权访问指定章节（按所属项目判断）
     */
    public void checkChapter(Long chapterId)
    {
        if (isAdmin())
        {
            return;
        }
        if (chapterId == null)
        {
            throw new ServiceException("缺少章节ID");
        }
        NovelChapter chapter = novelChapterMapper.selectNovelChapterByChapterId(chapterId);
        if (chapter == null)
        {
            throw new ServiceException("章节不存在");
        }
        checkProject(chapter.getProjectId());
    }

    /**
     * 批量校验章节
     */
    public void checkChapters(Long[] chapterIds)
    {
        if (isAdmin() || chapterIds == null)
        {
            return;
        }
        for (Long chapterId : chapterIds)
        {
            checkChapter(chapterId);
        }
    }

    /**
     * 校验是否有权访问指定 Meta 实体
     */
    public void checkMetaEntity(Long entityId)
    {
        if (isAdmin())
        {
            return;
        }
        if (entityId == null)
        {
            throw new ServiceException("缺少实体ID");
        }
        NovelMetaEntity entity = novelMetaEntityMapper.selectNovelMetaEntityByEntityId(entityId);
        if (entity == null)
        {
            throw new ServiceException("实体不存在");
        }
        checkProject(entity.getProjectId());
    }

    /**
     * 批量校验 Meta 实体
     */
    public void checkMetaEntities(Long[] entityIds)
    {
        if (isAdmin() || entityIds == null)
        {
            return;
        }
        for (Long entityId : entityIds)
        {
            checkMetaEntity(entityId);
        }
    }

    /**
     * 批量校验 Meta 关系
     */
    public void checkMetaRelations(Long[] relationIds)
    {
        if (isAdmin() || relationIds == null)
        {
            return;
        }
        for (Long relationId : relationIds)
        {
            NovelMetaRelation relation = novelMetaRelationMapper.selectNovelMetaRelationByRelationId(relationId);
            if (relation == null)
            {
                throw new ServiceException("关系不存在");
            }
            checkProject(relation.getProjectId());
        }
    }

    /**
     * 校验是否有权访问指定 AI 任务
     */
    public void checkTask(Long taskId)
    {
        if (isAdmin())
        {
            return;
        }
        if (taskId == null)
        {
            throw new ServiceException("缺少任务ID");
        }
        NovelAiTask task = novelAiTaskMapper.selectNovelAiTaskByTaskId(taskId);
        if (task == null)
        {
            throw new ServiceException("任务不存在");
        }
        checkProject(task.getProjectId());
    }

    /**
     * 校验是否有权访问指定工作流运行
     */
    public void checkRun(Long runId)
    {
        if (isAdmin())
        {
            return;
        }
        if (runId == null)
        {
            throw new ServiceException("缺少运行ID");
        }
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        if (run == null)
        {
            throw new ServiceException("工作流运行不存在");
        }
        checkProject(run.getProjectId());
    }
}
