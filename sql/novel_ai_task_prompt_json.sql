-- 为已有库增加 AI 任务 prompt 调试字段
ALTER TABLE novel_ai_task
  ADD COLUMN prompt_json LONGTEXT NULL COMMENT 'AI 调用记录（system/user/response 等）' AFTER result_json;
