package com.ruoyi.novel.agent.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.novel.agent.capability.NovelAgentCapabilityRegistry;

@Component
public class NovelToolRegistry
{
    public static final String PROJECT = "local.project";

    public static final String SETTING = "local.setting";

    public static final String CHAPTER = "local.chapter";

    public static final String META = "local.meta";

    public static final String CONTEXT = "local.context";

    public static final String REVIEW = "local.review";

    @Autowired
    private ProjectTools projectTools;

    @Autowired
    private SettingTools settingTools;

    @Autowired
    private ChapterTools chapterTools;

    @Autowired
    private MetaTools metaTools;

    @Autowired
    private ContextTools contextTools;

    @Autowired
    private ReviewTools reviewTools;

    @Autowired
    private NovelAgentCapabilityRegistry capabilityRegistry;

    public List<Object> resolveTools(List<String> names)
    {
        Map<String, Object> localTools = localTools();
        List<Object> tools = new ArrayList<Object>();
        if (names == null)
        {
            return tools;
        }
        for (String name : names)
        {
            Object local = localTools.get(name);
            if (local != null)
            {
                tools.add(local);
                continue;
            }
            Object capabilityTool = capabilityRegistry.getTool(name);
            if (capabilityTool != null)
            {
                tools.add(capabilityTool);
            }
        }
        return tools;
    }

    private Map<String, Object> localTools()
    {
        Map<String, Object> tools = new LinkedHashMap<String, Object>();
        tools.put(PROJECT, projectTools);
        tools.put(SETTING, settingTools);
        tools.put(CHAPTER, chapterTools);
        tools.put(META, metaTools);
        tools.put(CONTEXT, contextTools);
        tools.put(REVIEW, reviewTools);
        return tools;
    }
}
