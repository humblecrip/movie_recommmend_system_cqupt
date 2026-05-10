-- phase1 验证库 schema 补齐模板
-- 用途：
-- 1. 补齐验证库落后于当前 `db/phase1_compatibility_normalization.sql` 的缺口
-- 2. 只补当前确认缺失的对象：
--    - app_admin_user
--    - app_config
--    - app_sensitive_word
--    - app_auth_session.app_admin_user_id
-- 3. 允许在旧表已改名为 `_bak` 后继续补齐
--
-- 注意：
-- - 本文件不直接写 `USE`，由受限 JDBC 执行器绑定到 `ssmf7s0a_phase1`
-- - `__USERS_SOURCE__` / `__CONFIG_SOURCE__` / `__SENSITIVEWORDS_SOURCE__`
--   会在执行前由 helper 替换成真实存在的源表名（优先旧表，否则 *_bak）

CREATE TABLE IF NOT EXISTS `app_admin_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `legacy_users_id` bigint(20) DEFAULT NULL COMMENT '旧表 users.id',
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '后台登录账号',
  `password` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '当前运行时密码值',
  `image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `role` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '管理员' COMMENT '角色',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_admin_user_legacy_id` (`legacy_users_id`),
  UNIQUE KEY `uk_app_admin_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 后台用户主表';

CREATE TABLE IF NOT EXISTS `app_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `legacy_config_id` bigint(20) DEFAULT NULL COMMENT '旧表 config.id',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名',
  `value` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置值',
  `url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置地址',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_config_legacy_id` (`legacy_config_id`),
  UNIQUE KEY `uk_app_config_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 系统配置表';

CREATE TABLE IF NOT EXISTS `app_sensitive_word` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `legacy_sensitivewords_id` bigint(20) DEFAULT NULL COMMENT '旧表 sensitivewords.id',
  `content` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '敏感词内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_sensitive_word_legacy_id` (`legacy_sensitivewords_id`),
  UNIQUE KEY `uk_app_sensitive_word_content` (`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 敏感词表';

INSERT INTO `app_admin_user` (
  `legacy_users_id`,
  `username`,
  `password`,
  `image`,
  `role`,
  `created_at`,
  `updated_at`
)
SELECT
  u.id,
  u.username,
  u.password,
  u.image,
  COALESCE(NULLIF(u.role, ''), '管理员'),
  COALESCE(u.addtime, CURRENT_TIMESTAMP),
  COALESCE(u.addtime, CURRENT_TIMESTAMP)
FROM `__USERS_SOURCE__` u
ON DUPLICATE KEY UPDATE
  `username` = VALUES(`username`),
  `password` = VALUES(`password`),
  `image` = VALUES(`image`),
  `role` = VALUES(`role`),
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `app_config` (
  `legacy_config_id`,
  `name`,
  `value`,
  `url`
)
SELECT
  c.id,
  c.name,
  c.value,
  c.url
FROM `__CONFIG_SOURCE__` c
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `value` = VALUES(`value`),
  `url` = VALUES(`url`),
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `app_sensitive_word` (
  `legacy_sensitivewords_id`,
  `content`,
  `created_at`,
  `updated_at`
)
SELECT
  s.id,
  s.content,
  COALESCE(s.addtime, CURRENT_TIMESTAMP),
  COALESCE(s.addtime, CURRENT_TIMESTAMP)
FROM `__SENSITIVEWORDS_SOURCE__` s
WHERE TRIM(COALESCE(s.content, '')) <> ''
ON DUPLICATE KEY UPDATE
  `content` = VALUES(`content`),
  `updated_at` = CURRENT_TIMESTAMP;

UPDATE `app_auth_session` s
LEFT JOIN `app_admin_user` aau
  ON aau.`legacy_users_id` = s.`legacy_subject_id`
SET s.`app_admin_user_id` = aau.`id`
WHERE s.`subject_table_name` = 'users';

INSERT IGNORE INTO `schema_refactor_migration_log` (
  `migration_key`,
  `description`
) VALUES (
  'phase1_validation_schema_gap_fill',
  '补齐验证库 phase1 漂移：app_admin_user/app_config/app_sensitive_word 与 app_auth_session.app_admin_user_id'
);
