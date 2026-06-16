package com.ruoyi.novel.service;

import java.util.List;
import com.ruoyi.novel.domain.NovelProject;

/**
 * 小说项目 服务层
 *
 * @author novel
 */
public interface INovelProjectService
{
    public NovelProject selectNovelProjectByProjectId(Long projectId);

    public NovelProject selectNovelProjectByUuid(String projectUuid);

    public List<NovelProject> selectNovelProjectList(NovelProject novelProject);

    public int insertNovelProject(NovelProject novelProject);

    public int updateNovelProject(NovelProject novelProject);

    public int deleteNovelProjectByProjectIds(Long[] projectIds);

    public int deleteNovelProjectByProjectId(Long projectId);
}
