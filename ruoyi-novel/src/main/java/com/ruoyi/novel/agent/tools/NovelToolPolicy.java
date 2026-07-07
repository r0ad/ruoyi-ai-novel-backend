package com.ruoyi.novel.agent.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import com.ruoyi.novel.agent.capability.NovelAgentCapabilityRegistry;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepCode;

@Component
public class NovelToolPolicy
{
    public List<String> toolNamesFor(NovelWorkflowStepCode stepCode)
    {
        List<String> names = new ArrayList<String>();
        if (stepCode == null)
        {
            names.add(NovelToolRegistry.PROJECT);
            names.add(NovelToolRegistry.SETTING);
            addAgentUtilities(names);
            return names;
        }

        switch (stepCode)
        {
            case INIT_PROJECT:
                names.add(NovelToolRegistry.PROJECT);
                break;
            case WORLD_BUILDING:
            case PLOT_OUTLINE:
                names.add(NovelToolRegistry.PROJECT);
                names.add(NovelToolRegistry.SETTING);
                break;
            case CHARACTER_DESIGN:
                names.add(NovelToolRegistry.PROJECT);
                names.add(NovelToolRegistry.SETTING);
                names.add(NovelToolRegistry.META);
                break;
            case CHAPTER_PLANNING:
                names.add(NovelToolRegistry.PROJECT);
                names.add(NovelToolRegistry.CHAPTER);
                names.add(NovelToolRegistry.SETTING);
                break;
            case WRITE_CHAPTER:
                names.add(NovelToolRegistry.PROJECT);
                names.add(NovelToolRegistry.SETTING);
                names.add(NovelToolRegistry.CHAPTER);
                names.add(NovelToolRegistry.CONTEXT);
                names.add(NovelToolRegistry.META);
                names.add(NovelToolRegistry.REVIEW);
                break;
            case REVIEW_CHAPTER:
            case FINAL_REVIEW:
                names.add(NovelToolRegistry.PROJECT);
                names.add(NovelToolRegistry.SETTING);
                names.add(NovelToolRegistry.CHAPTER);
                names.add(NovelToolRegistry.META);
                names.add(NovelToolRegistry.CONTEXT);
                names.add(NovelToolRegistry.REVIEW);
                break;
            default:
                names.add(NovelToolRegistry.PROJECT);
                names.add(NovelToolRegistry.SETTING);
                break;
        }
        addAgentUtilities(names);
        return Collections.unmodifiableList(names);
    }

    private void addAgentUtilities(List<String> names)
    {
        names.add(NovelAgentCapabilityRegistry.TODO_WRITE);
        names.add(NovelAgentCapabilityRegistry.ASK_USER);
        names.add(NovelAgentCapabilityRegistry.SKILLS);
    }
}
