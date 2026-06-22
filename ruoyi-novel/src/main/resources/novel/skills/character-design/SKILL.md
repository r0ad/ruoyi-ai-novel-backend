# 角色设计技能

## 目标
设计推动情节的主要角色与重要配角，并**写入数据库**。

## 步骤
1. **首轮必须立即**调用 getSetting(world) 与 getSetting(characters)，禁止仅口头说将要查库
2. 确定主角、反派、核心配角（至少3人）
3. 每位角色包含：姓名、身份、性格、动机、成长弧
4. **必须调用 saveSetting(characters) 保存完整 Markdown 角色档案**
5. 为主要角色调用 saveMetaEntity(entityType=character)

## 禁止
- 禁止只回复「现在开始设计角色」「先保存角色档案：」而不实际调用工具
- 禁止在未 saveSetting 前结束对话

## 质量要求
- 角色动机清晰，与大纲方向一致
- 避免功能重复的角色
- 名称易记、符合世界观
