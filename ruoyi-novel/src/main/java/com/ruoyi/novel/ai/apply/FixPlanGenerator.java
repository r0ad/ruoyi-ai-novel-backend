package com.ruoyi.novel.ai.apply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.domain.dto.ReviewIssue;
import com.ruoyi.novel.ai.domain.dto.ReviewResult;

/**
 * 为审查结果补充可执行的修复动作，并标记 autoFixable
 */
@Component
public class FixPlanGenerator
{
    private static final Pattern NAME_PAIR = Pattern.compile(
        "设定[^「]*「([^」]+)」[^「]*正文[^「]*「([^」]+)」|"
            + "设定[^\"]*\"([^\"]+)\"[^\"]*正文[^\"]*\"([^\"]+)\"");

    public void enrichReviewResult(ReviewResult result)
    {
        if (result == null || result.getIssues() == null)
        {
            return;
        }
        for (ReviewIssue issue : result.getIssues())
        {
            enrichIssue(issue);
        }
        boolean hasBlocker = false;
        for (ReviewIssue issue : result.getIssues())
        {
            String sev = issue.getSeverity();
            if ("critical".equalsIgnoreCase(sev) || "major".equalsIgnoreCase(sev))
            {
                hasBlocker = true;
                break;
            }
        }
        result.setPassed(!hasBlocker);
    }

    private void enrichIssue(ReviewIssue issue)
    {
        if (issue.getSuggestedActions() != null && !issue.getSuggestedActions().isEmpty())
        {
            if ("critical".equalsIgnoreCase(issue.getSeverity())
                || "character_name".equals(issue.getCategory()))
            {
                issue.setAutoFixable(true);
            }
            return;
        }
        if ("character_name".equals(issue.getCategory()))
        {
            String[] names = extractNamePair(issue.getSummary());
            if (names != null)
            {
                List<Map<String, Object>> actions = new ArrayList<Map<String, Object>>();
                Map<String, Object> settingAction = new HashMap<String, Object>();
                settingAction.put("type", "update_setting");
                settingAction.put("settingType", "characters");
                settingAction.put("oldName", names[0]);
                settingAction.put("newName", names[1]);
                settingAction.put("hint", "以正文「" + names[1] + "」为准更新角色设定");
                actions.add(settingAction);
                Map<String, Object> metaAction = new HashMap<String, Object>();
                metaAction.put("type", "update_meta_entity");
                metaAction.put("entityName", names[0]);
                metaAction.put("newName", names[1]);
                actions.add(metaAction);
                issue.setSuggestedActions(actions);
                issue.setAutoFixable(true);
            }
        }
        else if ("setting_missing".equals(issue.getCategory()))
        {
            Map<String, Object> action = new HashMap<String, Object>();
            action.put("type", "append_setting");
            action.put("settingType", "outline");
            action.put("hint", issue.getSummary());
            issue.setSuggestedActions(java.util.Collections.singletonList(action));
            issue.setAutoFixable(false);
        }
    }

    private String[] extractNamePair(String summary)
    {
        if (StringUtils.isEmpty(summary))
        {
            return null;
        }
        Matcher m = NAME_PAIR.matcher(summary);
        if (m.find())
        {
            String oldName = StringUtils.isNotEmpty(m.group(1)) ? m.group(1) : m.group(3);
            String newName = StringUtils.isNotEmpty(m.group(2)) ? m.group(2) : m.group(4);
            if (StringUtils.isNotEmpty(oldName) && StringUtils.isNotEmpty(newName)
                && !oldName.equals(newName))
            {
                return new String[] { oldName, newName };
            }
        }
        return null;
    }

    public boolean hasCriticalOrMajor(ReviewResult result)
    {
        if (result == null || result.getIssues() == null)
        {
            return false;
        }
        for (ReviewIssue issue : result.getIssues())
        {
            String sev = issue.getSeverity();
            if ("critical".equalsIgnoreCase(sev) || "major".equalsIgnoreCase(sev))
            {
                return true;
            }
        }
        return false;
    }
}
