package com.ruoyi.novel.ai.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContextOptions
{
    private Long projectId;
    private Long chapterId;
    private List<Long> chapterIds = new ArrayList<Long>();
    private String settingType;
    private List<String> includeSettings = new ArrayList<String>(Arrays.asList("characters", "world", "outline"));
    private boolean includeMetaGraph = true;
    private String userMessage;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getChapterId() { return chapterId; }
    public void setChapterId(Long chapterId) { this.chapterId = chapterId; }
    public List<Long> getChapterIds() { return chapterIds; }
    public void setChapterIds(List<Long> chapterIds) { this.chapterIds = chapterIds; }
    public String getSettingType() { return settingType; }
    public void setSettingType(String settingType) { this.settingType = settingType; }
    public List<String> getIncludeSettings() { return includeSettings; }
    public void setIncludeSettings(List<String> includeSettings) { this.includeSettings = includeSettings; }
    public boolean isIncludeMetaGraph() { return includeMetaGraph; }
    public void setIncludeMetaGraph(boolean includeMetaGraph) { this.includeMetaGraph = includeMetaGraph; }
    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
}
