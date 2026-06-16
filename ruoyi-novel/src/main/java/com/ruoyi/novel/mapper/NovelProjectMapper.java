package com.ruoyi.novel.mapper;

import java.util.List;
import com.ruoyi.novel.domain.NovelProject;

/**
 * 小说项目 数据层
 *
 * @author novel
 */
public interface NovelProjectMapper
{
    /**
     * 查询项目
     */
    public NovelProject selectNovelProjectByProjectId(Long projectId);

    /**
     * 根据UUID查询项目
     */
    public NovelProject selectNovelProjectByUuid(String projectUuid);

    /**
     * 查询项目列表
     */
    public List<NovelProject> selectNovelProjectList(NovelProject novelProject);

    /**
     * 新增项目
     */
    public int insertNovelProject(NovelProject novelProject);

    /**
     * 修改项目
     */
    public int updateNovelProject(NovelProject novelProject);

    /**
     * 删除项目
     */
    public int deleteNovelProjectByProjectId(Long projectId);

    /**
     * 批量删除项目
     */
    public int deleteNovelProjectByProjectIds(Long[] projectIds);
}
