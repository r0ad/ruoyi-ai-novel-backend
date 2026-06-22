-- Agent 工作流（世界观/大纲等）建议 >= 10 分钟整请求超时；read 间隔超时由代码关闭
UPDATE novel_ai_model
SET timeout_ms = 900000
WHERE timeout_ms IS NULL OR timeout_ms < 900000;
