-- runtime-safe incremental cutover for movie multi-type relation
-- target schema is bound by external helper or explicit operator selection
-- this script only touches app_movie/app_movie_type/app_movie_type_rel

CREATE TABLE IF NOT EXISTS `app_movie_type_rel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `movie_id` bigint(20) NOT NULL COMMENT '关联 app_movie.id',
  `type_id` bigint(20) NOT NULL COMMENT '关联 app_movie_type.id',
  `is_primary` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为主类型',
  `sort_order` int(11) NOT NULL DEFAULT '1' COMMENT '展示顺序，从 1 开始',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_movie_type_rel_movie_type` (`movie_id`,`type_id`),
  KEY `idx_app_movie_type_rel_type_id` (`type_id`),
  KEY `idx_app_movie_type_rel_primary` (`movie_id`,`is_primary`,`sort_order`),
  CONSTRAINT `fk_app_movie_type_rel_movie_id` FOREIGN KEY (`movie_id`) REFERENCES `app_movie` (`id`),
  CONSTRAINT `fk_app_movie_type_rel_type_id` FOREIGN KEY (`type_id`) REFERENCES `app_movie_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='runtime 电影-类型关系表';

SET @runtime_schema_name = DATABASE();

SET @runtime_movie_type_rel_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_type_rel'
      AND index_name = 'uk_app_movie_type_rel_movie_type'
  ),
  'SELECT ''SKIP uk_app_movie_type_rel_movie_type''',
  'ALTER TABLE `app_movie_type_rel` ADD UNIQUE KEY `uk_app_movie_type_rel_movie_type` (`movie_id`,`type_id`)'
);
PREPARE runtime_stmt FROM @runtime_movie_type_rel_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_type_rel_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_type_rel'
      AND index_name = 'idx_app_movie_type_rel_type_id'
  ),
  'SELECT ''SKIP idx_app_movie_type_rel_type_id''',
  'ALTER TABLE `app_movie_type_rel` ADD KEY `idx_app_movie_type_rel_type_id` (`type_id`)'
);
PREPARE runtime_stmt FROM @runtime_movie_type_rel_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_type_rel_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_type_rel'
      AND index_name = 'idx_app_movie_type_rel_primary'
  ),
  'SELECT ''SKIP idx_app_movie_type_rel_primary''',
  'ALTER TABLE `app_movie_type_rel` ADD KEY `idx_app_movie_type_rel_primary` (`movie_id`,`is_primary`,`sort_order`)'
);
PREPARE runtime_stmt FROM @runtime_movie_type_rel_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_type_rel_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_type_rel'
      AND constraint_type = 'FOREIGN KEY'
      AND constraint_name = 'fk_app_movie_type_rel_movie_id'
  ),
  'SELECT ''SKIP fk_app_movie_type_rel_movie_id''',
  'ALTER TABLE `app_movie_type_rel` ADD CONSTRAINT `fk_app_movie_type_rel_movie_id` FOREIGN KEY (`movie_id`) REFERENCES `app_movie` (`id`)'
);
PREPARE runtime_stmt FROM @runtime_movie_type_rel_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_type_rel_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_type_rel'
      AND constraint_type = 'FOREIGN KEY'
      AND constraint_name = 'fk_app_movie_type_rel_type_id'
  ),
  'SELECT ''SKIP fk_app_movie_type_rel_type_id''',
  'ALTER TABLE `app_movie_type_rel` ADD CONSTRAINT `fk_app_movie_type_rel_type_id` FOREIGN KEY (`type_id`) REFERENCES `app_movie_type` (`id`)'
);
PREPARE runtime_stmt FROM @runtime_movie_type_rel_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

INSERT INTO `app_movie_type_rel` (
  `movie_id`,
  `type_id`,
  `is_primary`,
  `sort_order`,
  `created_at`,
  `updated_at`
)
SELECT
  am.`id`,
  amt.`id`,
  1,
  1,
  COALESCE(am.`created_at`, CURRENT_TIMESTAMP),
  CURRENT_TIMESTAMP
FROM `app_movie` am
INNER JOIN `app_movie_type` amt
  ON amt.`id` = am.`movie_type_id`
WHERE am.`movie_type_id` IS NOT NULL
ON DUPLICATE KEY UPDATE
  `is_primary` = VALUES(`is_primary`),
  `sort_order` = VALUES(`sort_order`),
  `updated_at` = CURRENT_TIMESTAMP;

UPDATE `app_movie_type_rel` rel
INNER JOIN `app_movie` am
  ON am.`id` = rel.`movie_id`
SET
  rel.`is_primary` = CASE
    WHEN am.`movie_type_id` IS NOT NULL AND rel.`type_id` = am.`movie_type_id` THEN 1
    ELSE 0
  END,
  rel.`sort_order` = CASE
    WHEN am.`movie_type_id` IS NOT NULL AND rel.`type_id` = am.`movie_type_id` THEN 1
    WHEN rel.`sort_order` IS NULL OR rel.`sort_order` < 2 THEN 2
    ELSE rel.`sort_order`
  END,
  rel.`updated_at` = CURRENT_TIMESTAMP
WHERE rel.`is_primary` <> CASE
    WHEN am.`movie_type_id` IS NOT NULL AND rel.`type_id` = am.`movie_type_id` THEN 1
    ELSE 0
  END
  OR rel.`sort_order` <> CASE
    WHEN am.`movie_type_id` IS NOT NULL AND rel.`type_id` = am.`movie_type_id` THEN 1
    WHEN rel.`sort_order` IS NULL OR rel.`sort_order` < 2 THEN 2
    ELSE rel.`sort_order`
  END;
