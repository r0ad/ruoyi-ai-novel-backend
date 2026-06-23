-- AI模型配置表升级脚本（已有 novel_assistant 库执行）
drop table if exists novel_ai_model;
create table novel_ai_model (
  model_id        bigint(20)      not null auto_increment    comment '模型配置ID',
  user_id         bigint(20)      not null                   comment '所属用户ID',
  model_name      varchar(100)    not null                   comment '显示名称',
  provider_type   varchar(32)     not null                   comment '协议类型（openai/anthropic）',
  base_url        varchar(500)    not null                   comment 'API Base URL',
  api_key         varchar(500)    not null                   comment 'API Key（AES加密）',
  model_code      varchar(100)    not null                   comment '模型标识',
  temperature     decimal(3,2)    default 0.70               comment '默认温度',
  max_tokens      int(11)         default 4096               comment '默认最大Token',
  timeout_ms      int(11)         default 300000             comment '请求超时毫秒',
  is_active       char(1)         default '0'                comment '是否激活（0否 1是）',
  is_default      char(1)         default '0'                comment '是否默认',
  sort_order      int(11)         default 0                  comment '排序',
  status          char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (model_id),
  key idx_user_active (user_id, is_active),
  key idx_provider (provider_type, status)
) engine=innodb auto_increment=1 comment = 'AI模型配置表';

insert into sys_menu values('2050', 'AI模型管理', '2000', '6', 'ai-model', 'novel/ai-model/index', '', '', 1, 0, 'C', '0', '0', 'novel:aimodel:list', 'server', 'admin', sysdate(), '', null, 'AI模型配置');
insert into sys_menu values('2051', '模型查询', '2050', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2052', '模型新增', '2050', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2053', '模型修改', '2050', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2054', '模型删除', '2050', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2055', '模型激活', '2050', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:activate', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2056', '连通性测试', '2050', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:test', '#', 'admin', sysdate(), '', null, '');
insert into sys_role_menu values ('2', '2050');
insert into sys_role_menu values ('2', '2051');
insert into sys_role_menu values ('2', '2052');
insert into sys_role_menu values ('2', '2053');
insert into sys_role_menu values ('2', '2054');
insert into sys_role_menu values ('2', '2055');
insert into sys_role_menu values ('2', '2056');