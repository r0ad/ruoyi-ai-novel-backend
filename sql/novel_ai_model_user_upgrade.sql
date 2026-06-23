-- AI 模型配置按用户隔离升级脚本
-- 每个用户独立维护自己的模型列表与激活项

ALTER TABLE novel_ai_model
  ADD COLUMN user_id bigint(20) DEFAULT NULL COMMENT '所属用户ID' AFTER model_id;

UPDATE novel_ai_model m
LEFT JOIN sys_user u ON u.user_name = m.create_by
SET m.user_id = COALESCE(u.user_id, 1)
WHERE m.user_id IS NULL;

ALTER TABLE novel_ai_model
  MODIFY COLUMN user_id bigint(20) NOT NULL COMMENT '所属用户ID';

ALTER TABLE novel_ai_model DROP INDEX idx_active;
ALTER TABLE novel_ai_model ADD INDEX idx_user_active (user_id, is_active);
