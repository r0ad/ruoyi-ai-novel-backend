package com.ruoyi.novel.workflow.enums;

/**
 * 完整创作流程步骤编码
 */
public enum NovelWorkflowStepCode
{
    INIT_PROJECT("init_project", "项目立项", 1),
    WORLD_BUILDING("world_building", "世界观构建", 2),
    CHARACTER_DESIGN("character_design", "角色设计", 3),
    PLOT_OUTLINE("plot_outline", "故事大纲", 4),
    CHAPTER_PLANNING("chapter_planning", "章节规划", 5),
    WRITE_CHAPTER("write_chapter", "章节写作", 6),
    REVIEW_CHAPTER("review_chapter", "章节审查", 7),
    FINAL_REVIEW("final_review", "全书审查", 8);

    private final String code;
    private final String label;
    private final int order;

    NovelWorkflowStepCode(String code, String label, int order)
    {
        this.code = code;
        this.label = label;
        this.order = order;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public int getOrder()
    {
        return order;
    }

    public static NovelWorkflowStepCode fromCode(String code)
    {
        if (code == null)
        {
            return null;
        }
        for (NovelWorkflowStepCode step : values())
        {
            if (step.code.equals(code))
            {
                return step;
            }
        }
        return null;
    }

    public NovelWorkflowStepCode next()
    {
        NovelWorkflowStepCode[] steps = values();
        for (int i = 0; i < steps.length; i++)
        {
            if (steps[i] == this && i + 1 < steps.length)
            {
                return steps[i + 1];
            }
        }
        return null;
    }
}
