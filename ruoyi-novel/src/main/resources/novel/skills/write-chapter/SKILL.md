# 章节写作技能

## 目标
撰写符合设定、风格一致的网络小说章节正文。

## 步骤
1. 调用 getWritingContext 获取上下文
2. 根据章节摘要撰写 2000-3000 字正文（网文节奏，对话与描写结合）
3. 调用 saveChapter 保存正文
4. 调用 extractMetaFromChapter 抽取新实体

## 质量要求
- 承接前文，不突兀跳转
- 遵守 styleGuide 与角色性格
- 章末留悬念或推进主线
- 审查步骤使用 reviewChapterConsistency
