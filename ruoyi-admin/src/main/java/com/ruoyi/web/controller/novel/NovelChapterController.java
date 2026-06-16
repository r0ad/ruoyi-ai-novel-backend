package com.ruoyi.web.controller.novel;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.service.INovelChapterService;

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

    /**
     * 查询章节列表
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:list')")
    @GetMapping("/list")
    public TableDataInfo list(NovelChapter novelChapter)
    {
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
        return success(novelChapterService.selectNovelChapterTreeByProjectId(projectId));
    }

    /**
     * 获取章节详细信息
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:query')")
    @GetMapping(value = "/{chapterId}")
    public AjaxResult getInfo(@PathVariable Long chapterId)
    {
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
        novelChapter.setCreateBy(getUsername());
        novelChapter.setUpdateBy(getUsername());
        return toAjax(novelChapterService.insertNovelChapter(novelChapter));
    }

    /**
     * 修改章节
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:edit')")
    @Log(title = "小说章节", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody NovelChapter novelChapter)
    {
        novelChapter.setUpdateBy(getUsername());
        return toAjax(novelChapterService.updateNovelChapter(novelChapter));
    }

    /**
     * 删除章节
     */
    @PreAuthorize("@ss.hasPermi('novel:chapter:remove')")
    @Log(title = "小说章节", businessType = BusinessType.DELETE)
    @DeleteMapping("/{chapterIds}")
    public AjaxResult remove(@PathVariable Long[] chapterIds)
    {
        return toAjax(novelChapterService.deleteNovelChapterByChapterIds(chapterIds));
    }
}
