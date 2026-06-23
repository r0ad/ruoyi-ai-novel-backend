-- 小说项目按用户隔离升级脚本
-- 项目绑定归属用户，章节/设定/Meta/任务/工作流等子资源通过项目归属间接隔离
-- 管理员（user_id=1）可查看全部，普通用户仅能查看自己的项目

ALTER TABLE novel_project
  ADD COLUMN user_id bigint(20) DEFAULT NULL COMMENT '归属用户ID' AFTER project_uuid;

-- 按 create_by 回填归属用户
UPDATE novel_project p
LEFT JOIN sys_user u ON u.user_name = p.create_by
SET p.user_id = COALESCE(u.user_id, 1)
WHERE p.user_id IS NULL;

-- 未匹配到的历史数据归属管理员
UPDATE novel_project SET user_id = 1 WHERE user_id IS NULL;

ALTER TABLE novel_project
  MODIFY COLUMN user_id bigint(20) NOT NULL COMMENT '归属用户ID';

ALTER TABLE novel_project ADD INDEX idx_project_user (user_id);
