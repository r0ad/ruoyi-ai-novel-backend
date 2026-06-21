package com.ruoyi.novel.ai.domain.dto;

import java.util.ArrayList;
import java.util.List;

public class AiTaskApplyRequest
{
    private List<String> selectedIssueIds = new ArrayList<String>();
    private List<Integer> selectedEntityIndexes = new ArrayList<Integer>();
    private List<Integer> selectedRelationIndexes = new ArrayList<Integer>();
    private String overwriteMode = "merge";

    public List<String> getSelectedIssueIds() { return selectedIssueIds; }
    public void setSelectedIssueIds(List<String> selectedIssueIds) { this.selectedIssueIds = selectedIssueIds; }
    public List<Integer> getSelectedEntityIndexes() { return selectedEntityIndexes; }
    public void setSelectedEntityIndexes(List<Integer> selectedEntityIndexes) { this.selectedEntityIndexes = selectedEntityIndexes; }
    public List<Integer> getSelectedRelationIndexes() { return selectedRelationIndexes; }
    public void setSelectedRelationIndexes(List<Integer> selectedRelationIndexes) { this.selectedRelationIndexes = selectedRelationIndexes; }
    public String getOverwriteMode() { return overwriteMode; }
    public void setOverwriteMode(String overwriteMode) { this.overwriteMode = overwriteMode; }
}
