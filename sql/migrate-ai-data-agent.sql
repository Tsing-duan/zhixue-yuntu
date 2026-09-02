-- ============================================
-- AI 数据整理 + 用量追踪 数据库迁移
-- 运行方式: mysql -u root -p online_learn < sql/migrate-ai-data-agent.sql
-- ============================================

-- 1. ape_ai_history 表增加 token 用量字段
ALTER TABLE `ape_ai_history`
  ADD COLUMN `prompt_tokens` int(11) DEFAULT 0 COMMENT '消耗 prompt tokens',
  ADD COLUMN `completion_tokens` int(11) DEFAULT 0 COMMENT '消耗 completion tokens';

-- 2. 管理端菜单: AI 数据整理（顶级菜单）
INSERT INTO `ape_menu` VALUES (
  '1735000000000000001', '0', 'AI 数据整理', '0',
  9, 1,
  '/aiData', '/system/aiData/index', '',
  1, 0, 0,
  'system:aiData:list', 'el-icon-data-analysis',
  '超级管理员', NOW(), '超级管理员', NOW(),
  ''
);
