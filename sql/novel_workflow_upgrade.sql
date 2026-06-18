-- 工作流与 Agent 创作流程升级脚本
-- 执行前请确保已执行 novel_assistant.sql

-- ----------------------------
-- 工作流运行实例
-- ----------------------------
drop table if exists novel_workflow_event;
drop table if exists novel_workflow_step;
drop table if exists novel_workflow_run;

create table novel_workflow_run (
  run_id          bigint(20)      not null auto_increment    comment '运行ID',
  project_id      bigint(20)      not null                   comment '项目ID',
  user_id         bigint(20)      not null                   comment '用户ID',
  workflow_type   varchar(32)     not null default 'full_creation' comment '工作流类型',
  status          varchar(32)     not null default 'running' comment '状态',
  current_step    varchar(64)     default null               comment '当前步骤编码',
  context_json    json            default null               comment '运行上下文',
  user_idea       text            default null               comment '用户创意输入',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  primary key (run_id),
  key idx_project (project_id),
  key idx_user (user_id),
  key idx_status (status)
) engine=innodb auto_increment=1 comment = '工作流运行实例';

create table novel_workflow_step (
  step_id           bigint(20)      not null auto_increment    comment '步骤ID',
  run_id            bigint(20)      not null                   comment '运行ID',
  step_code         varchar(64)     not null                   comment '步骤编码',
  status            varchar(32)     not null default 'pending' comment '步骤状态',
  input_json        json            default null               comment '输入参数',
  output_snapshot   longtext        default null               comment '产出快照',
  agent_session_id  bigint(20)      default null               comment 'Agent会话ID',
  error_message     varchar(1000)   default null               comment '错误信息',
  started_at        datetime        default null               comment '开始时间',
  finished_at       datetime        default null               comment '结束时间',
  create_time       datetime                                   comment '创建时间',
  primary key (step_id),
  key idx_run (run_id),
  key idx_run_code (run_id, step_code)
) engine=innodb auto_increment=1 comment = '工作流步骤记录';

create table novel_workflow_event (
  event_id        bigint(20)      not null auto_increment    comment '事件ID',
  run_id          bigint(20)      not null                   comment '运行ID',
  step_id         bigint(20)      default null               comment '步骤ID',
  event_type      varchar(32)     not null                   comment '事件类型',
  payload_json    json            default null               comment '事件载荷',
  create_time     datetime                                   comment '创建时间',
  primary key (event_id),
  key idx_run_time (run_id, create_time)
) engine=innodb auto_increment=1 comment = '工作流事件';

-- ----------------------------
-- 菜单：项目工作台与工作流
-- ----------------------------
insert into sys_menu values('2060', '项目工作台', '2000', '7', 'workbench', 'novel/workbench/index', '', '', 1, 0, 'C', '1', '0', 'novel:workflow:view', 'guide', 'admin', sysdate(), '', null, '项目创作工作台');
insert into sys_menu values('2061', '启动工作流', '2060', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:workflow:start', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2062', '确认步骤', '2060', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:workflow:confirm', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2063', '查看工作流', '2060', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:workflow:view', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu values ('2', '2060');
insert into sys_role_menu values ('2', '2061');
insert into sys_role_menu values ('2', '2062');
insert into sys_role_menu values ('2', '2063');

-- ----------------------------
-- 字典：项目状态 / 工作流状态 / 步骤 / 事件类型
-- ----------------------------
insert into sys_dict_type values(105, '项目状态', 'novel_project_status', '0', 'admin', sysdate(), '', null, '小说项目状态');
insert into sys_dict_data values(105, 1, '草稿', '0', 'novel_project_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(106, 2, '连载', '1', 'novel_project_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(107, 3, '完结', '2', 'novel_project_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');

insert into sys_dict_type values(106, '工作流状态', 'novel_workflow_status', '0', 'admin', sysdate(), '', null, '创作工作流运行状态');
insert into sys_dict_data values(108, 1, '执行中', 'running', 'novel_workflow_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(109, 2, '待确认', 'waiting_confirm', 'novel_workflow_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(110, 3, '已完成', 'completed', 'novel_workflow_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(111, 4, '失败', 'failed', 'novel_workflow_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(112, 5, '已暂停', 'paused', 'novel_workflow_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');

insert into sys_dict_type values(107, '工作流步骤', 'novel_workflow_step', '0', 'admin', sysdate(), '', null, '创作工作流步骤编码');
insert into sys_dict_data values(113, 1, '项目立项', 'init_project', 'novel_workflow_step', '', 'default', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(114, 2, '世界观构建', 'world_building', 'novel_workflow_step', '', 'default', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(115, 3, '角色设计', 'character_design', 'novel_workflow_step', '', 'default', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(116, 4, '故事大纲', 'plot_outline', 'novel_workflow_step', '', 'default', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(117, 5, '章节规划', 'chapter_planning', 'novel_workflow_step', '', 'default', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(118, 6, '章节写作', 'write_chapter', 'novel_workflow_step', '', 'default', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(119, 7, '章节审查', 'review_chapter', 'novel_workflow_step', '', 'default', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(120, 8, '全书审查', 'final_review', 'novel_workflow_step', '', 'default', 'N', '0', 'admin', sysdate(), '', null, '');

insert into sys_dict_type values(108, '工作流事件', 'novel_workflow_event_type', '0', 'admin', sysdate(), '', null, '创作工作流 SSE 事件类型');
insert into sys_dict_data values(121, 1, '步骤开始', 'step_started', 'novel_workflow_event_type', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(122, 2, '步骤完成', 'step_completed', 'novel_workflow_event_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(123, 3, '步骤失败', 'step_failed', 'novel_workflow_event_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(124, 4, '工具调用', 'tool_call', 'novel_workflow_event_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(125, 5, '待办更新', 'todo_update', 'novel_workflow_event_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(126, 6, '用户确认', 'user_confirm', 'novel_workflow_event_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(127, 7, '运行状态', 'run_status', 'novel_workflow_event_type', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(128, 8, '流式输出', 'token', 'novel_workflow_event_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
