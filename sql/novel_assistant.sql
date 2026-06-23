-- ----------------------------
-- 网络小说创作辅助系统 - 业务表
-- 依赖：先执行 ry_20260417.sql 初始化若依基础库
-- 使用：在 ry-vue 或 novel_assistant 库中执行本脚本
-- ----------------------------

-- ----------------------------
-- 1、小说项目表
-- ----------------------------
drop table if exists novel_project;
create table novel_project (
  project_id      bigint(20)      not null auto_increment    comment '项目ID',
  project_uuid    varchar(36)     not null                   comment '对外UUID，兼容MCP导入',
  title           varchar(200)    not null                   comment '书名',
  genre           varchar(64)     default null               comment '类型',
  status          char(1)         default '0'                comment '状态（0草稿 1连载 2完结）',
  word_count      bigint(20)      default 0                  comment '总字数',
  chapter_count   int(11)         default 0                  comment '章节数',
  cover_url       varchar(500)    default null               comment '封面地址',
  summary         text                                       comment '简介',
  style_guide     text                                       comment '风格指南',
  del_flag        char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (project_id),
  unique key uk_project_uuid (project_uuid),
  key idx_title (title),
  key idx_create_by (create_by)
) engine=innodb auto_increment=1 comment = '小说项目表';

-- ----------------------------
-- 2、项目成员表
-- ----------------------------
drop table if exists novel_project_member;
create table novel_project_member (
  id              bigint(20)      not null auto_increment    comment '主键',
  project_id      bigint(20)      not null                   comment '项目ID',
  user_id         bigint(20)      not null                   comment '用户ID',
  role_type       varchar(20)     default 'author'           comment '角色（owner/author/viewer）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  primary key (id),
  unique key uk_project_user (project_id, user_id),
  key idx_user_id (user_id)
) engine=innodb auto_increment=1 comment = '项目成员表';

-- ----------------------------
-- 3、章节元数据表
-- ----------------------------
drop table if exists novel_chapter;
create table novel_chapter (
  chapter_id        bigint(20)      not null auto_increment    comment '章节ID',
  project_id        bigint(20)      not null                   comment '项目ID',
  parent_id         bigint(20)      default 0                  comment '父章节ID',
  chapter_number    int(11)         not null                   comment '章节序号',
  title             varchar(200)    not null                   comment '章节标题',
  summary           text                                       comment '章节摘要',
  word_count        int(11)         default 0                  comment '字数',
  status            char(1)         default '0'                comment '状态（0草稿 1已发布）',
  sort_order        int(11)         default 0                  comment '排序',
  version_no        int(11)         default 1                  comment '当前版本号',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (chapter_id),
  key idx_project (project_id),
  key idx_project_parent (project_id, parent_id),
  key idx_project_number (project_id, chapter_number)
) engine=innodb auto_increment=1 comment = '章节元数据表';

-- ----------------------------
-- 4、章节正文表
-- ----------------------------
drop table if exists novel_chapter_content;
create table novel_chapter_content (
  chapter_id      bigint(20)      not null                   comment '章节ID',
  content         longtext        not null                   comment 'Markdown正文',
  content_html    longtext                                   comment 'HTML渲染缓存',
  primary key (chapter_id)
) engine=innodb comment = '章节正文表';

-- ----------------------------
-- 5、章节版本历史表
-- ----------------------------
drop table if exists novel_chapter_version;
create table novel_chapter_version (
  version_id      bigint(20)      not null auto_increment    comment '版本ID',
  chapter_id      bigint(20)      not null                   comment '章节ID',
  version_no      int(11)         not null                   comment '版本号',
  title           varchar(200)    default null               comment '标题',
  content         longtext                                   comment '正文',
  summary         text                                       comment '摘要',
  change_log      varchar(500)    default null               comment '变更说明',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  primary key (version_id),
  key idx_chapter_version (chapter_id, version_no)
) engine=innodb auto_increment=1 comment = '章节版本历史表';

-- ----------------------------
-- 6、设定文档表
-- ----------------------------
drop table if exists novel_setting;
create table novel_setting (
  setting_id      bigint(20)      not null auto_increment    comment '设定ID',
  project_id      bigint(20)      not null                   comment '项目ID',
  setting_type    varchar(32)     not null                   comment '类型（characters/world/outline/metrics/style/scene）',
  title           varchar(200)    default null               comment '标题',
  content         longtext                                   comment 'Markdown内容',
  content_html    longtext                                   comment 'HTML缓存',
  version_no      int(11)         default 1                  comment '版本号',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  primary key (setting_id),
  unique key uk_project_type (project_id, setting_type)
) engine=innodb auto_increment=1 comment = '设定文档表';

-- ----------------------------
-- 7、Meta实体表
-- ----------------------------
drop table if exists novel_meta_entity;
create table novel_meta_entity (
  entity_id         bigint(20)      not null auto_increment    comment '实体ID',
  project_id        bigint(20)      not null                   comment '项目ID',
  entity_type       varchar(32)     not null                   comment '类型（character/location/item/event/theme）',
  name              varchar(200)    not null                   comment '名称',
  aliases           json                                       comment '别名列表',
  attributes        json                                       comment '扩展属性',
  description       text                                       comment '描述',
  first_chapter_id  bigint(20)      default null               comment '首次出现章节',
  last_chapter_id   bigint(20)      default null               comment '最近出现章节',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (entity_id),
  key idx_project_type (project_id, entity_type),
  key idx_project_name (project_id, name)
) engine=innodb auto_increment=1 comment = 'Meta实体表';

-- ----------------------------
-- 8、Meta关系表
-- ----------------------------
drop table if exists novel_meta_relation;
create table novel_meta_relation (
  relation_id     bigint(20)      not null auto_increment    comment '关系ID',
  project_id      bigint(20)      not null                   comment '项目ID',
  source_id       bigint(20)      not null                   comment '源实体ID',
  target_id       bigint(20)      not null                   comment '目标实体ID',
  relation_type   varchar(64)     not null                   comment '关系类型',
  weight          decimal(5,2)    default 1.00               comment '权重',
  evidence        text                                       comment '依据说明',
  chapter_id      bigint(20)      default null               comment '关联章节',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  primary key (relation_id),
  key idx_project (project_id),
  key idx_source (source_id),
  key idx_target (target_id)
) engine=innodb auto_increment=1 comment = 'Meta关系表';

-- ----------------------------
-- 9、故事板表
-- ----------------------------
drop table if exists novel_storyboard;
create table novel_storyboard (
  storyboard_id   bigint(20)      not null auto_increment    comment '故事板ID',
  project_id      bigint(20)      not null                   comment '项目ID',
  title           varchar(200)    not null                   comment '标题',
  content         longtext                                   comment '内容',
  sort_order      int(11)         default 0                  comment '排序',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  primary key (storyboard_id),
  key idx_project (project_id)
) engine=innodb auto_increment=1 comment = '故事板表';

-- ----------------------------
-- 10、创作指标表
-- ----------------------------
drop table if exists novel_metrics;
create table novel_metrics (
  metrics_id      bigint(20)      not null auto_increment    comment '指标ID',
  project_id      bigint(20)      not null                   comment '项目ID',
  metrics_type    varchar(32)     default null               comment '类型（target/chapter/project）',
  ref_id          bigint(20)      default null               comment '关联ID',
  metrics_json    json            not null                   comment '指标JSON',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  primary key (metrics_id),
  key idx_project (project_id),
  key idx_project_type (project_id, metrics_type)
) engine=innodb auto_increment=1 comment = '创作指标表';

-- ----------------------------
-- 11、AI会话表
-- ----------------------------
drop table if exists novel_ai_session;
create table novel_ai_session (
  session_id      bigint(20)      not null auto_increment    comment '会话ID',
  project_id      bigint(20)      not null                   comment '项目ID',
  user_id         bigint(20)      not null                   comment '用户ID',
  session_type    varchar(32)     default null               comment '类型（continue/polish/review/qa）',
  chapter_id      bigint(20)      default null               comment '关联章节',
  title           varchar(200)    default null               comment '会话标题',
  create_time     datetime                                   comment '创建时间',
  update_time     datetime                                   comment '更新时间',
  primary key (session_id),
  key idx_project_user (project_id, user_id)
) engine=innodb auto_increment=1 comment = 'AI会话表';

-- ----------------------------
-- 12、AI消息表
-- ----------------------------
drop table if exists novel_ai_message;
create table novel_ai_message (
  message_id      bigint(20)      not null auto_increment    comment '消息ID',
  session_id      bigint(20)      not null                   comment '会话ID',
  role            varchar(16)     not null                   comment '角色（user/assistant/system）',
  content         longtext        not null                   comment '消息内容',
  token_usage     int(11)         default 0                  comment 'Token消耗',
  create_time     datetime                                   comment '创建时间',
  primary key (message_id),
  key idx_session (session_id)
) engine=innodb auto_increment=1 comment = 'AI消息表';

-- ----------------------------
-- 13、AI模型配置表
-- ----------------------------
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
  timeout_ms      int(11)         default 60000              comment '请求超时毫秒',
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

-- ----------------------------
-- 14、RAG文本块表
-- ----------------------------
drop table if exists novel_rag_chunk;
create table novel_rag_chunk (
  chunk_id        bigint(20)      not null auto_increment    comment '块ID',
  project_id      bigint(20)      not null                   comment '项目ID',
  source_type     varchar(32)     default null               comment '来源类型（chapter/setting/meta）',
  source_id       bigint(20)      not null                   comment '来源ID',
  chunk_index     int(11)         not null                   comment '块序号',
  content         text            not null                   comment '文本内容',
  vector_id       varchar(64)     default null               comment '向量库文档ID',
  create_time     datetime                                   comment '创建时间',
  update_time     datetime                                   comment '更新时间',
  primary key (chunk_id),
  key idx_project_source (project_id, source_type, source_id)
) engine=innodb auto_increment=1 comment = 'RAG文本块表';

-- ----------------------------
-- 15、创作模板表
-- ----------------------------
drop table if exists novel_template;
create table novel_template (
  template_id     bigint(20)      not null auto_increment    comment '模板ID',
  template_code   varchar(64)     not null                   comment '模板编码',
  template_name   varchar(100)    not null                   comment '模板名称',
  template_type   varchar(32)     not null                   comment '类型（chapter/setting/prompt）',
  content         longtext        not null                   comment '模板内容',
  status          char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (template_id),
  unique key uk_template_code (template_code)
) engine=innodb auto_increment=1 comment = '创作模板表';

-- ----------------------------
-- 初始化-创作模板（迁移自 MCP templates）
-- ----------------------------
insert into novel_template values(1, 'chapter_first', '首章模板', 'chapter', '# 第{chapter_number}章 {title}\n\n{content}', '0', 'admin', sysdate(), '', null, '首章写作模板');
insert into novel_template values(2, 'chapter_continuation', '续章模板', 'chapter', '# 第{chapter_number}章 {title}\n\n{content}', '0', 'admin', sysdate(), '', null, '续章写作模板');
insert into novel_template values(3, 'characters', '角色设定模板', 'setting', '# 角色设定\n\n## 主要角色\n\n', '0', 'admin', sysdate(), '', null, '角色设定');
insert into novel_template values(4, 'world', '世界观模板', 'setting', '# 世界观设定\n\n', '0', 'admin', sysdate(), '', null, '世界观设定');
insert into novel_template values(5, 'outline', '大纲模板', 'setting', '# 故事大纲\n\n', '0', 'admin', sysdate(), '', null, '故事大纲');

-- ----------------------------
-- 16、小说创作菜单（若依 sys_menu）
-- ----------------------------
insert into sys_menu values('2000', '小说创作', '0', '5', 'novel', null, '', '', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), '', null, '小说创作目录');
insert into sys_menu values('2001', '项目管理', '2000', '1', 'project', 'novel/project/index', '', '', 1, 0, 'C', '0', '0', 'novel:project:list', 'list', 'admin', sysdate(), '', null, '小说项目管理');
insert into sys_menu values('2002', '项目查询', '2001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:project:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2003', '项目新增', '2001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:project:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2004', '项目修改', '2001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:project:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2005', '项目删除', '2001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:project:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2006', '项目导出', '2001', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:project:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2010', '章节管理', '2000', '2', 'chapter', 'novel/chapter/index', '', '', 1, 0, 'C', '0', '0', 'novel:chapter:list', 'edit', 'admin', sysdate(), '', null, '章节管理');
insert into sys_menu values('2011', '章节查询', '2010', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:chapter:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2012', '章节新增', '2010', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:chapter:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2013', '章节修改', '2010', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:chapter:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2014', '章节删除', '2010', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:chapter:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2020', '设定中心', '2000', '3', 'setting', 'novel/setting/index', '', '', 1, 0, 'C', '0', '0', 'novel:setting:list', 'form', 'admin', sysdate(), '', null, '设定中心');
insert into sys_menu values('2021', '设定查询', '2020', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:setting:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2022', '设定修改', '2020', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:setting:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2030', 'Meta图谱', '2000', '4', 'meta', 'novel/meta/index', '', '', 1, 0, 'C', '0', '0', 'novel:meta:list', 'tree', 'admin', sysdate(), '', null, 'Meta关系图谱');
insert into sys_menu values('2031', 'Meta查询', '2030', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:meta:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2032', 'Meta新增', '2030', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:meta:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2033', 'Meta修改', '2030', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:meta:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2034', 'Meta删除', '2030', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:meta:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2040', 'AI助手', '2000', '5', 'ai', 'novel/ai/index', '', '', 1, 0, 'C', '0', '0', 'novel:ai:chat', 'message', 'admin', sysdate(), '', null, 'AI创作助手');
insert into sys_menu values('2041', 'AI续写', '2040', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:ai:continue', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2042', 'AI审查', '2040', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:ai:review', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2050', 'AI模型管理', '2000', '6', 'ai-model', 'novel/ai-model/index', '', '', 1, 0, 'C', '0', '0', 'novel:aimodel:list', 'server', 'admin', sysdate(), '', null, 'AI模型配置');
insert into sys_menu values('2051', '模型查询', '2050', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2052', '模型新增', '2050', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2053', '模型修改', '2050', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2054', '模型删除', '2050', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2055', '模型激活', '2050', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:activate', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2056', '连通性测试', '2050', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'novel:aimodel:test', '#', 'admin', sysdate(), '', null, '');

-- 普通角色（role_id=2）授权小说创作菜单
insert into sys_role_menu values ('2', '2000');
insert into sys_role_menu values ('2', '2001');
insert into sys_role_menu values ('2', '2002');
insert into sys_role_menu values ('2', '2003');
insert into sys_role_menu values ('2', '2004');
insert into sys_role_menu values ('2', '2005');
insert into sys_role_menu values ('2', '2006');
insert into sys_role_menu values ('2', '2010');
insert into sys_role_menu values ('2', '2011');
insert into sys_role_menu values ('2', '2012');
insert into sys_role_menu values ('2', '2013');
insert into sys_role_menu values ('2', '2014');
insert into sys_role_menu values ('2', '2020');
insert into sys_role_menu values ('2', '2021');
insert into sys_role_menu values ('2', '2022');
insert into sys_role_menu values ('2', '2030');
insert into sys_role_menu values ('2', '2031');
insert into sys_role_menu values ('2', '2032');
insert into sys_role_menu values ('2', '2033');
insert into sys_role_menu values ('2', '2034');
insert into sys_role_menu values ('2', '2040');
insert into sys_role_menu values ('2', '2041');
insert into sys_role_menu values ('2', '2042');
insert into sys_role_menu values ('2', '2050');
insert into sys_role_menu values ('2', '2051');
insert into sys_role_menu values ('2', '2052');
insert into sys_role_menu values ('2', '2053');
insert into sys_role_menu values ('2', '2054');
insert into sys_role_menu values ('2', '2055');
insert into sys_role_menu values ('2', '2056');

-- ----------------------------
-- 17、字典类型（小说类型）
-- ----------------------------
insert into sys_dict_type values(100, '小说类型', 'novel_genre', '0', 'admin', sysdate(), '', null, '网络小说类型');
insert into sys_dict_data values(100, 1, '玄幻', 'fantasy', 'novel_genre', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(101, 2, '都市', 'urban', 'novel_genre', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(102, 3, '科幻', 'scifi', 'novel_genre', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(103, 4, '历史', 'history', 'novel_genre', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(104, 5, '言情', 'romance', 'novel_genre', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');
