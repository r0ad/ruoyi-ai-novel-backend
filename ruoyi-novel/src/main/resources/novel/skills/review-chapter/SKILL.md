# 章节审查与修复技能

## 目标
对当前章节进行一致性审查，并对 Critical/Major 问题调用工具修复。

## 步骤
1. 使用【待审查章节ID】调用 `reviewChapterConsistency(chapterId)` 获取 JSON 问题列表
2. 对 **Critical** 与 **Major** 问题：
   - 角色名不一致 → `saveSetting(characters)` 更新 + `saveMetaEntity` 重命名
   - 设定缺失 → `saveSetting` 补充对应类型
   - 正文与大纲冲突 → 优先更新 `outline` 待办说明，或修正章节 `saveChapter`（需用户确认时仅改设定）
3. 修复后再次调用 `reviewChapterConsistency` 验证
4. 输出审查摘要（含 passed 状态与剩余 Minor 问题）

## 约束
- 最多 3 轮修复，仍不通过则输出报告供用户确认
- 必须调用工具写入，不要只给 Markdown 建议
- 不要跳过 reviewChapterConsistency
