-- runtime-safe incremental cutover for movie region normalization
-- target schema is bound by external helper or explicit operator selection
-- this script only touches app_movie/app_movie_region

CREATE TABLE IF NOT EXISTS `app_movie_region` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `region_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地区名称展示值',
  `normalized_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '去空白/小写后的幂等键',
  `source_note` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_movie_region_normalized_name` (`normalized_name`),
  KEY `idx_app_movie_region_name` (`region_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='runtime 电影地区维表';

SET @runtime_schema_name = DATABASE();

SET @runtime_movie_region_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_region'
      AND index_name = 'uk_app_movie_region_normalized_name'
  ),
  'SELECT ''SKIP uk_app_movie_region_normalized_name''',
  'ALTER TABLE `app_movie_region` ADD UNIQUE KEY `uk_app_movie_region_normalized_name` (`normalized_name`)'
);
PREPARE runtime_stmt FROM @runtime_movie_region_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_region_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_region'
      AND index_name = 'idx_app_movie_region_name'
  ),
  'SELECT ''SKIP idx_app_movie_region_name''',
  'ALTER TABLE `app_movie_region` ADD KEY `idx_app_movie_region_name` (`region_name`)'
);
PREPARE runtime_stmt FROM @runtime_movie_region_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_region_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie'
      AND column_name = 'region_id'
  ),
  'SELECT ''SKIP app_movie.region_id''',
  'ALTER TABLE `app_movie` ADD COLUMN `region_id` bigint(20) DEFAULT NULL COMMENT ''规范地区引用；当前单地区能力落到 app_movie_region'' AFTER `region_name`'
);
PREPARE runtime_stmt FROM @runtime_movie_region_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_region_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie'
      AND index_name = 'idx_app_movie_region_id'
  ),
  'SELECT ''SKIP idx_app_movie_region_id''',
  'ALTER TABLE `app_movie` ADD KEY `idx_app_movie_region_id` (`region_id`)'
);
PREPARE runtime_stmt FROM @runtime_movie_region_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

INSERT INTO `app_movie_region` (
  `region_name`,
  `normalized_name`,
  `source_note`,
  `created_at`,
  `updated_at`
)
SELECT
  src.`region_name`,
  src.`normalized_name`,
  src.`source_note`,
  src.`created_at`,
  CURRENT_TIMESTAMP
FROM (
  SELECT
    MIN(TRIM(am.`region_name`)) AS `region_name`,
    LOWER(TRIM(am.`region_name`)) AS `normalized_name`,
    'app_movie.region_name' AS `source_note`,
    MIN(COALESCE(am.`created_at`, CURRENT_TIMESTAMP)) AS `created_at`
  FROM `app_movie` am
  WHERE TRIM(COALESCE(am.`region_name`, '')) <> ''
  GROUP BY LOWER(TRIM(am.`region_name`))
) src
ON DUPLICATE KEY UPDATE
  `region_name` = VALUES(`region_name`),
  `source_note` = VALUES(`source_note`),
  `updated_at` = CURRENT_TIMESTAMP;

UPDATE `app_movie` am
INNER JOIN `app_movie_region` amr
  ON amr.`normalized_name` = LOWER(TRIM(COALESCE(am.`region_name`, '')))
SET
  am.`region_id` = amr.`id`,
  am.`updated_at` = CURRENT_TIMESTAMP
WHERE TRIM(COALESCE(am.`region_name`, '')) <> ''
  AND (am.`region_id` IS NULL OR am.`region_id` <> amr.`id`);

UPDATE `app_movie`
SET
  `region_id` = NULL,
  `updated_at` = CURRENT_TIMESTAMP
WHERE TRIM(COALESCE(`region_name`, '')) = ''
  AND `region_id` IS NOT NULL;

DELETE amr
FROM `app_movie_region` amr
LEFT JOIN `app_movie` am
  ON am.`region_id` = amr.`id`
WHERE am.`id` IS NULL;

SET @runtime_movie_region_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie'
      AND constraint_type = 'FOREIGN KEY'
      AND constraint_name = 'fk_app_movie_region_id'
  ),
  'SELECT ''SKIP fk_app_movie_region_id''',
  'ALTER TABLE `app_movie` ADD CONSTRAINT `fk_app_movie_region_id` FOREIGN KEY (`region_id`) REFERENCES `app_movie_region` (`id`)'
);
PREPARE runtime_stmt FROM @runtime_movie_region_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;
