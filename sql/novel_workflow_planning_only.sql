-- 创作流程方案 A：止于章节规划，默认 workflow_type 为 planning_only
-- 可选：更新字典说明（write/review/final 仍供 AI 任务使用，不再出现在默认流程 Stepper）

update sys_dict_data set remark = '流程末步（planning_only）' where dict_type = 'novel_workflow_step' and dict_value = 'chapter_planning';
update sys_dict_data set remark = '已移出默认流程，请使用章节管理' where dict_type = 'novel_workflow_step' and dict_value in ('write_chapter', 'review_chapter', 'final_review');
