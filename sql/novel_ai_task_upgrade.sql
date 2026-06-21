-- AI 任务表（预览与应用）
DROP TABLE IF EXISTS novel_ai_task;
CREATE TABLE novel_ai_task (
  task_id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  project_id      BIGINT       NOT NULL COMMENT '项目ID',
  task_type       VARCHAR(64)  NOT NULL COMMENT '任务类型',
  target_type     VARCHAR(32)  DEFAULT NULL COMMENT '目标类型',
  target_id       VARCHAR(64)  DEFAULT NULL COMMENT '目标ID',
  status          VARCHAR(32)  NOT NULL DEFAULT 'pending' COMMENT 'pending/running/completed/applied/failed',
  input_json      TEXT         COMMENT '请求参数',
  result_json     LONGTEXT     COMMENT '结构化结果',
  error_message   VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  session_id      BIGINT       DEFAULT NULL COMMENT '关联AI会话',
  applied_at      DATETIME     DEFAULT NULL COMMENT '应用时间',
  applied_by      VARCHAR(64)  DEFAULT NULL COMMENT '应用人',
  create_by       VARCHAR(64)  DEFAULT NULL,
  create_time     DATETIME     DEFAULT NULL,
  update_time     DATETIME     DEFAULT NULL,
  PRIMARY KEY (task_id),
  KEY idx_project_type (project_id, task_type),
  KEY idx_status (status)
) ENGINE=InnoDB COMMENT='AI 任务（预览与应用）';

-- 菜单与权限
INSERT INTO sys_menu VALUES('2070', 'AI任务', '2000', '8', 'ai-task', 'novel/ai-task/index', '', '', 1, 0, 'C', '0', '0', 'novel:ai:task', 'code', 'admin', sysdate(), '', NULL, 'AI任务历史');
INSERT INTO sys_menu VALUES('2071', '创建AI任务', '2070', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:ai:task', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2072', '应用AI结果', '2070', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:ai:apply', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_role_menu VALUES ('2', '2070');
INSERT INTO sys_role_menu VALUES ('2', '2071');
INSERT INTO sys_role_menu VALUES ('2', '2072');
