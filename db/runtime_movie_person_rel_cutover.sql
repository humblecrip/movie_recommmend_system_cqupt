-- runtime-safe incremental cutover for movie director/cast normalization
-- target schema is bound by external helper or explicit operator selection
-- this script only touches app_movie/app_movie_person/app_movie_person_rel

CREATE TABLE IF NOT EXISTS `app_movie_person` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `person_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '人物名称展示值',
  `normalized_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '去空白/小写后的幂等键',
  `source_note` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_movie_person_normalized_name` (`normalized_name`),
  KEY `idx_app_movie_person_name` (`person_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='runtime 电影人物维表';

CREATE TABLE IF NOT EXISTS `app_movie_person_rel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `movie_id` bigint(20) NOT NULL COMMENT '关联 app_movie.id',
  `person_id` bigint(20) NOT NULL COMMENT '关联 app_movie_person.id',
  `relation_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'director / cast',
  `sort_order` int(11) NOT NULL DEFAULT '1' COMMENT '展示顺序，从 1 开始',
  `person_name_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关系来源的人名快照',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_movie_person_rel_movie_role_person` (`movie_id`,`relation_type`,`person_id`),
  KEY `idx_app_movie_person_rel_person_id` (`person_id`),
  KEY `idx_app_movie_person_rel_role_sort` (`movie_id`,`relation_type`,`sort_order`),
  CONSTRAINT `fk_app_movie_person_rel_movie_id` FOREIGN KEY (`movie_id`) REFERENCES `app_movie` (`id`),
  CONSTRAINT `fk_app_movie_person_rel_person_id` FOREIGN KEY (`person_id`) REFERENCES `app_movie_person` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='runtime 电影-人物关系表';

SET @runtime_schema_name = DATABASE();

SET @runtime_movie_person_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_person'
      AND index_name = 'uk_app_movie_person_normalized_name'
  ),
  'SELECT ''SKIP uk_app_movie_person_normalized_name''',
  'ALTER TABLE `app_movie_person` ADD UNIQUE KEY `uk_app_movie_person_normalized_name` (`normalized_name`)'
);
PREPARE runtime_stmt FROM @runtime_movie_person_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_person_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_person'
      AND index_name = 'idx_app_movie_person_name'
  ),
  'SELECT ''SKIP idx_app_movie_person_name''',
  'ALTER TABLE `app_movie_person` ADD KEY `idx_app_movie_person_name` (`person_name`)'
);
PREPARE runtime_stmt FROM @runtime_movie_person_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_person_rel_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_person_rel'
      AND index_name = 'uk_app_movie_person_rel_movie_role_person'
  ),
  'SELECT ''SKIP uk_app_movie_person_rel_movie_role_person''',
  'ALTER TABLE `app_movie_person_rel` ADD UNIQUE KEY `uk_app_movie_person_rel_movie_role_person` (`movie_id`,`relation_type`,`person_id`)'
);
PREPARE runtime_stmt FROM @runtime_movie_person_rel_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_person_rel_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_person_rel'
      AND index_name = 'idx_app_movie_person_rel_person_id'
  ),
  'SELECT ''SKIP idx_app_movie_person_rel_person_id''',
  'ALTER TABLE `app_movie_person_rel` ADD KEY `idx_app_movie_person_rel_person_id` (`person_id`)'
);
PREPARE runtime_stmt FROM @runtime_movie_person_rel_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_person_rel_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_person_rel'
      AND index_name = 'idx_app_movie_person_rel_role_sort'
  ),
  'SELECT ''SKIP idx_app_movie_person_rel_role_sort''',
  'ALTER TABLE `app_movie_person_rel` ADD KEY `idx_app_movie_person_rel_role_sort` (`movie_id`,`relation_type`,`sort_order`)'
);
PREPARE runtime_stmt FROM @runtime_movie_person_rel_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_person_rel_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_person_rel'
      AND constraint_type = 'FOREIGN KEY'
      AND constraint_name = 'fk_app_movie_person_rel_movie_id'
  ),
  'SELECT ''SKIP fk_app_movie_person_rel_movie_id''',
  'ALTER TABLE `app_movie_person_rel` ADD CONSTRAINT `fk_app_movie_person_rel_movie_id` FOREIGN KEY (`movie_id`) REFERENCES `app_movie` (`id`)'
);
PREPARE runtime_stmt FROM @runtime_movie_person_rel_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

SET @runtime_movie_person_rel_ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_schema = @runtime_schema_name
      AND table_name = 'app_movie_person_rel'
      AND constraint_type = 'FOREIGN KEY'
      AND constraint_name = 'fk_app_movie_person_rel_person_id'
  ),
  'SELECT ''SKIP fk_app_movie_person_rel_person_id''',
  'ALTER TABLE `app_movie_person_rel` ADD CONSTRAINT `fk_app_movie_person_rel_person_id` FOREIGN KEY (`person_id`) REFERENCES `app_movie_person` (`id`)'
);
PREPARE runtime_stmt FROM @runtime_movie_person_rel_ddl;
EXECUTE runtime_stmt;
DEALLOCATE PREPARE runtime_stmt;

INSERT INTO `app_movie_person` (
  `person_name`,
  `normalized_name`,
  `source_note`,
  `created_at`,
  `updated_at`
)
SELECT
  src.`person_name`,
  src.`normalized_name`,
  src.`source_note`,
  src.`created_at`,
  CURRENT_TIMESTAMP
FROM (
  SELECT
    MIN(raw.`person_name`) AS `person_name`,
    raw.`normalized_name`,
    MIN(raw.`source_note`) AS `source_note`,
    MIN(raw.`created_at`) AS `created_at`
  FROM (
    SELECT
      TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1)) AS `person_name`,
      LOWER(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1))) AS `normalized_name`,
      'app_movie.director_name' AS `source_note`,
      COALESCE(am.`created_at`, CURRENT_TIMESTAMP) AS `created_at`
    FROM `app_movie` am
    INNER JOIN (
      SELECT ones.n + tens.n * 10 + 1 AS n
      FROM (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
      ) ones
      CROSS JOIN (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
      ) tens
    ) seq
      ON seq.n <= 1 + LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ''))
        - LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','), ',', ''))
    WHERE TRIM(COALESCE(am.`director_name`, '')) <> ''
      AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1)) <> ''

    UNION ALL

    SELECT
      TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1)) AS `person_name`,
      LOWER(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1))) AS `normalized_name`,
      'app_movie.cast_names' AS `source_note`,
      COALESCE(am.`created_at`, CURRENT_TIMESTAMP) AS `created_at`
    FROM `app_movie` am
    INNER JOIN (
      SELECT ones.n + tens.n * 10 + 1 AS n
      FROM (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
      ) ones
      CROSS JOIN (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
      ) tens
    ) seq
      ON seq.n <= 1 + LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ''))
        - LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','), ',', ''))
    WHERE TRIM(COALESCE(am.`cast_names`, '')) <> ''
      AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1)) <> ''
  ) raw
  WHERE raw.`normalized_name` <> ''
  GROUP BY raw.`normalized_name`
) src
ON DUPLICATE KEY UPDATE
  `person_name` = VALUES(`person_name`),
  `source_note` = VALUES(`source_note`),
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `app_movie_person_rel` (
  `movie_id`,
  `person_id`,
  `relation_type`,
  `sort_order`,
  `person_name_snapshot`,
  `created_at`,
  `updated_at`
)
SELECT
  src.`movie_id`,
  amp.`id`,
  src.`relation_type`,
  src.`sort_order`,
  src.`person_name`,
  src.`created_at`,
  CURRENT_TIMESTAMP
FROM (
  SELECT
    raw.`movie_id`,
    raw.`relation_type`,
    raw.`normalized_name`,
    MIN(raw.`sort_order`) AS `sort_order`,
    MIN(raw.`person_name`) AS `person_name`,
    MIN(raw.`created_at`) AS `created_at`
  FROM (
    SELECT
      am.`id` AS `movie_id`,
      'director' AS `relation_type`,
      seq.n AS `sort_order`,
      TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1)) AS `person_name`,
      LOWER(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1))) AS `normalized_name`,
      COALESCE(am.`created_at`, CURRENT_TIMESTAMP) AS `created_at`
    FROM `app_movie` am
    INNER JOIN (
      SELECT ones.n + tens.n * 10 + 1 AS n
      FROM (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
      ) ones
      CROSS JOIN (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
      ) tens
    ) seq
      ON seq.n <= 1 + LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ''))
        - LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','), ',', ''))
    WHERE TRIM(COALESCE(am.`director_name`, '')) <> ''
      AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1)) <> ''

    UNION ALL

    SELECT
      am.`id` AS `movie_id`,
      'cast' AS `relation_type`,
      seq.n AS `sort_order`,
      TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1)) AS `person_name`,
      LOWER(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1))) AS `normalized_name`,
      COALESCE(am.`created_at`, CURRENT_TIMESTAMP) AS `created_at`
    FROM `app_movie` am
    INNER JOIN (
      SELECT ones.n + tens.n * 10 + 1 AS n
      FROM (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
      ) ones
      CROSS JOIN (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
      ) tens
    ) seq
      ON seq.n <= 1 + LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ''))
        - LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','), ',', ''))
    WHERE TRIM(COALESCE(am.`cast_names`, '')) <> ''
      AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
        ',',
        seq.n
      ), ',', -1)) <> ''
  ) raw
  WHERE raw.`normalized_name` <> ''
  GROUP BY raw.`movie_id`, raw.`relation_type`, raw.`normalized_name`
) src
INNER JOIN `app_movie_person` amp
  ON amp.`normalized_name` = src.`normalized_name`
ON DUPLICATE KEY UPDATE
  `sort_order` = VALUES(`sort_order`),
  `person_name_snapshot` = VALUES(`person_name_snapshot`),
  `updated_at` = CURRENT_TIMESTAMP;

DELETE rel
FROM `app_movie_person_rel` rel
INNER JOIN `app_movie_person` amp
  ON amp.`id` = rel.`person_id`
WHERE rel.`relation_type` IN ('director', 'cast')
  AND NOT EXISTS (
    SELECT 1
    FROM (
      SELECT
        raw.`movie_id`,
        raw.`relation_type`,
        raw.`normalized_name`
      FROM (
        SELECT
          am.`id` AS `movie_id`,
          'director' AS `relation_type`,
          LOWER(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
            REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
            ',',
            seq.n
          ), ',', -1))) AS `normalized_name`
        FROM `app_movie` am
        INNER JOIN (
          SELECT ones.n + tens.n * 10 + 1 AS n
          FROM (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
          ) ones
          CROSS JOIN (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
          ) tens
        ) seq
          ON seq.n <= 1 + LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ''))
            - LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','), ',', ''))
        WHERE TRIM(COALESCE(am.`director_name`, '')) <> ''
          AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
            REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`director_name`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
            ',',
            seq.n
          ), ',', -1)) <> ''

        UNION ALL

        SELECT
          am.`id` AS `movie_id`,
          'cast' AS `relation_type`,
          LOWER(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
            REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
            ',',
            seq.n
          ), ',', -1))) AS `normalized_name`
        FROM `app_movie` am
        INNER JOIN (
          SELECT ones.n + tens.n * 10 + 1 AS n
          FROM (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
          ) ones
          CROSS JOIN (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
          ) tens
        ) seq
          ON seq.n <= 1 + LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ''))
            - LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','), ',', ''))
        WHERE TRIM(COALESCE(am.`cast_names`, '')) <> ''
          AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(
            REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(am.`cast_names`, ''), '，', ','), '、', ','), ';', ','), '；', ','), '/', ','), '|', ','),
            ',',
            seq.n
          ), ',', -1)) <> ''
      ) raw
      WHERE raw.`normalized_name` <> ''
      GROUP BY raw.`movie_id`, raw.`relation_type`, raw.`normalized_name`
    ) src
    WHERE src.`movie_id` = rel.`movie_id`
      AND src.`relation_type` = rel.`relation_type`
      AND src.`normalized_name` = amp.`normalized_name`
  );

DELETE amp
FROM `app_movie_person` amp
LEFT JOIN `app_movie_person_rel` rel
  ON rel.`person_id` = amp.`id`
WHERE rel.`id` IS NULL;
