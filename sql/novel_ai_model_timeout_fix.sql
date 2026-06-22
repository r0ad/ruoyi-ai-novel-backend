-- 流式续写/长文本生成建议更长超时（已有库可选执行）
UPDATE novel_ai_model
SET timeout_ms = 300000
WHERE timeout_ms IS NULL OR timeout_ms < 120000;
