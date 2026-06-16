package com.ruoyi.novel.service.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.mapper.NovelProjectMapper;
import com.ruoyi.novel.service.INovelProjectService;

/**
 * 小说项目 服务层实现
 *
 * @author novel
 */
@Service
public class NovelProjectServiceImpl implements INovelProjectService
{
    @Autowired
    private NovelProjectMapper novelProjectMapper;

    @Override
    public NovelProject selectNovelProjectByProjectId(Long projectId)
    {
        return novelProjectMapper.selectNovelProjectByProjectId(projectId);
    }

    @Override
    public NovelProject selectNovelProjectByUuid(String projectUuid)
    {
        return novelProjectMapper.selectNovelProjectByUuid(projectUuid);
    }

    @Override
    public List<NovelProject> selectNovelProjectList(NovelProject novelProject)
    {
        return novelProjectMapper.selectNovelProjectList(novelProject);
    }

    @Override
    public int insertNovelProject(NovelProject novelProject)
    {
        if (StringUtils.isEmpty(novelProject.getProjectUuid()))
        {
            novelProject.setProjectUuid(UUID.randomUUID().toString());
        }
        if (StringUtils.isEmpty(novelProject.getStatus()))
        {
            novelProject.setStatus("0");
        }
        if (novelProject.getWordCount() == null)
        {
            novelProject.setWordCount(0L);
        }
        if (novelProject.getChapterCount() == null)
        {
            novelProject.setChapterCount(0);
        }
        return novelProjectMapper.insertNovelProject(novelProject);
    }

    @Override
    public int updateNovelProject(NovelProject novelProject)
    {
        return novelProjectMapper.updateNovelProject(novelProject);
    }

    @Override
    public int deleteNovelProjectByProjectIds(Long[] projectIds)
    {
        return novelProjectMapper.deleteNovelProjectByProjectIds(projectIds);
    }

    @Override
    public int deleteNovelProjectByProjectId(Long projectId)
    {
        return novelProjectMapper.deleteNovelProjectByProjectId(projectId);
    }
}
