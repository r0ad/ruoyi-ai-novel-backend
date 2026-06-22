<p align="center">
	<img alt="logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png">
</p>

<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">AI 小说创作 Web 版 · 后端</h1>
<h4 align="center">基于 RuoYi-Vue + Spring Boot 4 的网络小说 AI 辅助创作平台</h4>

<p align="center">
	<a href="https://gitee.com/r0ad/ruoyi-ai-novel-backend"><img src="https://img.shields.io/badge/Gitee-ruoyi--ai--novel--backend-C71D23?logo=gitee"></a>
	<a href="https://github.com/r0ad/ruoyi-ai-novel-backend"><img src="https://img.shields.io/badge/GitHub-ruoyi--ai--novel--backend-181717?logo=github"></a>
	<a href="https://gitee.com/y_project/RuoYi-Vue/blob/master/LICENSE"><img src="https://img.shields.io/github/license/mashape/apistatus.svg"></a>
</p>

## 项目简介

本项目是 **AI 小说创作 Web 版** 的后端服务，在 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) 快速开发框架基础上扩展网络小说创作与 AI 辅助能力，提供项目管理、章节编辑、设定维护、AI 对话续写、审查与 Agent 工作流等能力。

配套前端仓库：[ruoyi-ai-novel-frontend](https://gitee.com/r0ad/ruoyi-ai-novel-frontend)

## 源码仓库

前后端分别独立维护，支持 Gitee / GitHub 多平台同步：

| 平台 | 后端 | 前端 |
| :--- | :--- | :--- |
| **Gitee** | [ruoyi-ai-novel-backend](https://gitee.com/r0ad/ruoyi-ai-novel-backend) | [ruoyi-ai-novel-frontend](https://gitee.com/r0ad/ruoyi-ai-novel-frontend) |
| **GitHub** | [ruoyi-ai-novel-backend](https://github.com/r0ad/ruoyi-ai-novel-backend) | [ruoyi-ai-novel-frontend](https://github.com/r0ad/ruoyi-ai-novel-frontend) |

## 技术栈

* 基础框架：Spring Boot 4.x、Spring Security、MyBatis、Redis、JWT
* AI 能力：Spring AI（OpenAI / Anthropic 等，运行时由数据库动态配置）
* Agent：Spring AI Agent Utils（Skills / Todo / AskUser）
* 业务模块：`ruoyi-novel` 网络小说创作模块
* JDK：17+

## 核心功能

### 小说创作

1. **项目管理**：创建与管理小说项目
2. **章节管理**：章节编写、排序与内容维护
3. **设定管理**：世界观、人物、势力等设定维护
4. **Meta 管理**：从章节抽取并维护结构化元数据
5. **创作工作台**：集成编辑与 AI 辅助的一体化写作界面

### AI 能力

1. **AI 对话**：基于项目上下文的创作助手对话
2. **流式续写**：SSE 流式章节续写
3. **内容审查**：单章 / 全书一致性审查与修复建议
4. **Meta 抽取**：从章节内容自动抽取实体与关系
5. **Agent 工作流**：可配置 AI 任务与 Prompt 模板
6. **模型管理**：多 AI 模型与能力配置

### 系统管理

继承 RuoYi 内置能力：用户 / 角色 / 菜单 / 部门 / 字典 / 参数 / 日志 / 定时任务 / 代码生成 / 系统监控等。

## 快速开始

### 环境要求

* JDK 17+
* Maven 3.8+
* MySQL 8.x
* Redis

### 克隆项目

```bash
# Gitee
git clone https://gitee.com/r0ad/ruoyi-ai-novel-backend.git

# GitHub
git clone https://github.com/r0ad/ruoyi-ai-novel-backend.git
```

### 初始化数据库

按顺序执行 `sql/` 目录下脚本：

1. `ry_20260417.sql` — RuoYi 基础表
2. `quartz.sql` — 定时任务（可选）
3. `novel_assistant.sql` — 小说业务表
4. 其余 `novel_*.sql` — AI / 工作流等增量升级脚本

### 修改配置

编辑 `ruoyi-admin/src/main/resources/application-druid.yml`，配置 MySQL 与 Redis 连接信息。

AI 模型密钥与 Provider 可在系统运行后于 **小说管理 → AI 模型** 中配置，无需写死在配置文件中。

### 启动服务

```bash
cd ruoyi-admin
mvn spring-boot:run
```

默认端口：`8080`

### 启动前端

参见 [前端 README](https://gitee.com/r0ad/ruoyi-ai-novel-frontend)。

## 项目结构

```
novel-backend/
├── ruoyi-admin/      # 启动入口与 Web 控制器
├── ruoyi-framework/  # 框架核心
├── ruoyi-system/     # 系统管理
├── ruoyi-novel/      # 小说创作与 AI 业务模块
├── ruoyi-common/     # 通用工具
├── ruoyi-quartz/     # 定时任务
├── ruoyi-generator/  # 代码生成
└── sql/              # 数据库脚本
```

## 致谢

本项目基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) 开源框架二次开发，感谢若依团队的开源贡献。
