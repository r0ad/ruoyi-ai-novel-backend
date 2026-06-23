package com.ruoyi.web.controller.novel;

import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.rag.service.INovelContextService;
import com.ruoyi.novel.security.NovelProjectSecurity;
import com.ruoyi.novel.service.INovelChapterService;
import com.ruoyi.novel.service.INovelChapterVersionService;

/**
 * 小说章节 Controller
 *
 * @author novel
 */
@RestController
@RequestMapping("/novel/chapter")
public class NovelChapterController extends BaseController
{
    @Autowired
    private INovelChapterService novelChapterService;

    @Autowired
    private INovelChapterVersionService novelChapterVersionService;

    @Autowired
    private INovelContextService novelContextService;

    @Autowired
    private NovelProjectSecurity novelProjectSecurity;

    /**
     * 查询章节列表
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:list')")
    @GetMapping("/list")
    public TableDataInfo list(NovelChapter novelChapter)
    {
        novelProjectSecurity.checkProject(novelChapter.getProjectId());
        startPage();
        List<NovelChapter> list = novelChapterService.selectNovelChapterList(novelChapter);
        return getDataTable(list);
    }

    /**
     * 查询项目章节树
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:list')")
    @GetMapping("/tree/{projectId}")
    public AjaxResult tree(@PathVariable Long projectId)
    {
        novelProjectSecurity.checkProject(projectId);
        return success(novelChapterService.selectNovelChapterTreeByProjectId(projectId));
    }

    /**
     * 查询章节版本历史
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:query')")
    @GetMapping("/{chapterId}/versions")
    public AjaxResult versions(@PathVariable Long chapterId)
    {
        novelProjectSecurity.checkChapter(chapterId);
        return success(novelChapterVersionService.selectVersionsByChapterId(chapterId));
    }

    /**
     * 对比两个版本
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:query')")
    @GetMapping("/{chapterId}/versions/compare")
    public AjaxResult compareVersions(@PathVariable Long chapterId,
            @RequestParam Integer fromVersionNo, @RequestParam Integer toVersionNo)
    {
        novelProjectSecurity.checkChapter(chapterId);
        return success(novelChapterVersionService.compareVersions(chapterId, fromVersionNo, toVersionNo));
    }

    /**
     * 获取指定版本详情
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:query')")
    @GetMapping("/{chapterId}/versions/{versionNo}")
    public AjaxResult versionDetail(@PathVariable Long chapterId, @PathVariable Integer versionNo)
    {
        novelProjectSecurity.checkChapter(chapterId);
        return success(novelChapterVersionService.selectVersion(chapterId, versionNo));
    }

    /**
     * 回滚到指定版本
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:edit')")
    @Log(title = "章节版本回滚", businessType = BusinessType.UPDATE)
    @PostMapping("/{chapterId}/revert/{versionNo}")
    public AjaxResult revert(@PathVariable Long chapterId, @PathVariable Integer versionNo)
    {
        novelProjectSecurity.checkChapter(chapterId);
        return toAjax(novelChapterVersionService.revertToVersion(chapterId, versionNo, getUsername()));
    }

    /**
     * 获取章节详细信息
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:query')")
    @GetMapping(value = "/{chapterId}")
    public AjaxResult getInfo(@PathVariable Long chapterId)
    {
        novelProjectSecurity.checkChapter(chapterId);
        return success(novelChapterService.selectNovelChapterByChapterId(chapterId));
    }

    /**
     * 新增章节
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:add')")
    @Log(title = "小说章节", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody NovelChapter novelChapter)
    {
        novelProjectSecurity.checkProject(novelChapter.getProjectId());
        novelChapter.setCreateBy(getUsername());
        novelChapter.setUpdateBy(getUsername());
        int rows = novelChapterService.insertNovelChapter(novelChapter);
        return rows > 0 ? success(novelChapter) : error();
    }

    /**
     * 批量规划章节（仅需标题/摘要/序号，不含正文）
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:add')")
    @Log(title = "批量规划章节", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchAdd(@RequestBody List<NovelChapter> chapters)
    {
        if (chapters == null || chapters.isEmpty())
        {
            return error("章节列表不能为空");
        }
        for (NovelChapter chapter : chapters)
        {
            novelProjectSecurity.checkProject(chapter.getProjectId());
        }
        List<NovelChapter> saved = novelChapterService.batchInsertNovelChapters(chapters, getUsername());
        return success(saved);
    }

    /**
     * 修改章节
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:edit')")
    @Log(title = "小说章节", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody NovelChapter novelChapter)
    {
        novelProjectSecurity.checkChapter(novelChapter.getChapterId());
        novelChapter.setUpdateBy(getUsername());
        return toAjax(novelChapterService.updateNovelChapter(novelChapter));
    }

    @PreAuthorize("@ss.hasPermi('novel:chapter:query')")
    @GetMapping("/{chapterId}/context")
    public AjaxResult getWritingContext(@PathVariable Long chapterId,
        @RequestParam(defaultValue = "1") int rangeBefore)
    {
        novelProjectSecurity.checkChapter(chapterId);
        NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(chapterId);
        if (chapter == null)
        {
            return error("章节不存在");
        }
        int chapterNumber = chapter.getChapterNumber() != null ? chapter.getChapterNumber() : 1;
        String context = novelContextService.buildWritingContext(
            chapter.getProjectId(), chapterNumber, rangeBefore);
        return success(java.util.Collections.singletonMap("context", context));
    }

    /**
     * 删除章节
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:remove')")
    @Log(title = "小说章节", businessType = BusinessType.DELETE)
    @DeleteMapping("/{chapterIds}")
    public AjaxResult remove(@PathVariable Long[] chapterIds)
    {
        novelProjectSecurity.checkChapters(chapterIds);
        return toAjax(novelChapterService.deleteNovelChapterByChapterIds(chapterIds));
    }
}
