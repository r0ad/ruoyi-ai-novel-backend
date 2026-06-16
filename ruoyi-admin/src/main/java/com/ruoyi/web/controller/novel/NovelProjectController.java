package com.ruoyi.web.controller.novel;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.service.INovelProjectService;

/**
 * 小说项目 Controller
 *
 * @author novel
 */
@RestController
@RequestMapping("/novel/project")
public class NovelProjectController extends BaseController
{
    @Autowired
    private INovelProjectService novelProjectService;

    /**
     * 查询小说项目列表
     */
    @PreAuthorize("@ss.hasPermi('novel:project:list')")
    @GetMapping("/list")
    public TableDataInfo list(NovelProject novelProject)
    {
        startPage();
        List<NovelProject> list = novelProjectService.selectNovelProjectList(novelProject);
        return getDataTable(list);
    }

    /**
     * 导出小说项目列表
     */
    @PreAuthorize("@ss.hasPermi('novel:project:export')")
    @Log(title = "小说项目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NovelProject novelProject)
    {
        List<NovelProject> list = novelProjectService.selectNovelProjectList(novelProject);
        ExcelUtil<NovelProject> util = new ExcelUtil<NovelProject>(NovelProject.class);
        util.exportExcel(response, list, "小说项目数据");
    }

    /**
     * 获取小说项目详细信息
     */
    @PreAuthorize("@ss.hasPermi('novel:project:query')")
    @GetMapping(value = "/{projectId}")
    public AjaxResult getInfo(@PathVariable Long projectId)
    {
        return success(novelProjectService.selectNovelProjectByProjectId(projectId));
    }

    /**
     * 新增小说项目
     */
    @PreAuthorize("@ss.hasPermi('novel:project:add')")
    @Log(title = "小说项目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody NovelProject novelProject)
    {
        novelProject.setCreateBy(getUsername());
        return toAjax(novelProjectService.insertNovelProject(novelProject));
    }

    /**
     * 修改小说项目
     */
    @PreAuthorize("@ss.hasPermi('novel:project:edit')")
    @Log(title = "小说项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody NovelProject novelProject)
    {
        novelProject.setUpdateBy(getUsername());
        return toAjax(novelProjectService.updateNovelProject(novelProject));
    }

    /**
     * 删除小说项目
     */
    @PreAuthorize("@ss.hasPermi('novel:project:remove')")
    @Log(title = "小说项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{projectIds}")
    public AjaxResult remove(@PathVariable Long[] projectIds)
    {
        return toAjax(novelProjectService.deleteNovelProjectByProjectIds(projectIds));
    }
}
