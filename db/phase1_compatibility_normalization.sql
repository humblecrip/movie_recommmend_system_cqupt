USE `ssmf7s0a`;

-- ======================================================
-- phase-1 兼容迁移：运行时表归一化首批落地
-- 目标：
-- 1. 保留旧表继续服务当前 Java/MyBatis/前端代码
-- 2. 新建规范化目标表，使用 legacy_*_id 与旧表建立稳定映射
-- 3. 提供可重复执行的首轮回填骨架，后续代码再逐模块切换
-- 兼容：MySQL 5.7+
-- 当前范围：
--   dianyingxinxi / dianyingleixing / discussdianyingxinxi
--   yonghu / users / config / sensitivewords
--   storeup / dianyingdingdan / wodedianying / token
-- ======================================================

CREATE TABLE IF NOT EXISTS `schema_refactor_migration_log` (
  `migration_key` varchar(100) NOT NULL COMMENT '迁移唯一标识',
  `description` varchar(255) NOT NULL COMMENT '迁移说明',
  `executed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次执行时间',
  PRIMARY KEY (`migration_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库重构迁移日志';

-- ------------------------------------------------------
-- 1. 电影类型：从字符串表过渡到规范化维表
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `app_movie_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `legacy_dianyingleixing_id` bigint(20) DEFAULT NULL COMMENT '旧表 dianyingleixing.id',
  `type_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规范化类型名',
  `source_note` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_movie_type_legacy_id` (`legacy_dianyingleixing_id`),
  UNIQUE KEY `uk_app_movie_type_name` (`type_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 电影类型维表';

-- ------------------------------------------------------
-- 1.1 电影地区维表：从单值地区字符串快照抽取去重
--      当前没有稳定旧地区主表，因此以 normalized_name 去重。
-- ------------------------------------------------------
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 电影地区维表';

-- ------------------------------------------------------
-- 2. 电影主表：保留旧表 id 和旧类型字符串，挂到新类型/地区维表
--    注意：movie_type_id 仅表示兼容期主类型 / 默认类型引用，
--    最终多类型表达以 app_movie_type_rel 为准。
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `app_movie` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `legacy_dianyingxinxi_id` bigint(20) DEFAULT NULL COMMENT '旧表 dianyingxinxi.id',
  `movie_type_id` bigint(20) DEFAULT NULL COMMENT '兼容期主类型/默认类型引用，最终多类型表达见 app_movie_type_rel',
  `legacy_type_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '旧表电影类型字符串',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '电影标题',
  `poster_urls_csv` longtext COLLATE utf8mb4_unicode_ci COMMENT '旧表海报串，保留兼容回放',
  `region_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地区快照；兼容期继续保留',
  `region_id` bigint(20) DEFAULT NULL COMMENT '规范地区引用；当前单地区能力落到 app_movie_region',
  `release_date` date DEFAULT NULL COMMENT '上映时间',
  `director_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '导演快照；规范关系见 app_movie_person_rel(relation_type=director)',
  `cast_names` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主演快照；规范关系见 app_movie_person_rel(relation_type=cast)',
  `synopsis` longtext COLLATE utf8mb4_unicode_ci COMMENT '剧情简介',
  `detail_html` longtext COLLATE utf8mb4_unicode_ci COMMENT '电影详情 HTML',
  `like_count` int(11) NOT NULL DEFAULT '0' COMMENT '点赞汇总快照；真值源为 app_user_movie_action(action_type=like)',
  `dislike_count` int(11) NOT NULL DEFAULT '0' COMMENT '点踩汇总快照；真值源为 app_user_movie_action(action_type=dislike)',
  `click_count` int(11) NOT NULL DEFAULT '0' COMMENT '点击汇总快照；phase-1 暂未引入独立点击事件真值表',
  `comment_count` int(11) NOT NULL DEFAULT '0' COMMENT '评论汇总快照；真值源为 app_movie_comment',
  `favorite_count` int(11) NOT NULL DEFAULT '0' COMMENT '收藏汇总快照；真值源为 app_user_movie_action(action_type=favorite)',
  `total_score` double DEFAULT '0' COMMENT '评分汇总快照；真值源为 app_movie_comment.rating 聚合',
  `last_clicked_at` datetime DEFAULT NULL COMMENT '最近点击时间快照；phase-1 暂保留兼容字段',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_movie_legacy_id` (`legacy_dianyingxinxi_id`),
  KEY `idx_app_movie_type_id` (`movie_type_id`),
  KEY `idx_app_movie_region_id` (`region_id`),
  KEY `idx_app_movie_title` (`title`),
  CONSTRAINT `fk_app_movie_type_id` FOREIGN KEY (`movie_type_id`) REFERENCES `app_movie_type` (`id`),
  CONSTRAINT `fk_app_movie_region_id` FOREIGN KEY (`region_id`) REFERENCES `app_movie_region` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 电影主表';

-- ------------------------------------------------------
-- 3. 电影媒体：拆分旧表 haibao 的逗号分隔串
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `app_movie_media` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `movie_id` bigint(20) NOT NULL COMMENT '关联 app_movie.id',
  `legacy_dianyingxinxi_id` bigint(20) NOT NULL COMMENT '旧表 dianyingxinxi.id',
  `sort_order` int(11) NOT NULL DEFAULT '1' COMMENT '展示顺序，从 1 开始',
  `media_role` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'poster' COMMENT '媒体角色',
  `media_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '媒体地址',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_movie_media_legacy_order` (`legacy_dianyingxinxi_id`,`media_role`,`sort_order`),
  KEY `idx_app_movie_media_movie_id` (`movie_id`),
  CONSTRAINT `fk_app_movie_media_movie_id` FOREIGN KEY (`movie_id`) REFERENCES `app_movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 电影媒体表';

-- ------------------------------------------------------
-- 3.1 电影-类型关系：兼容期先从单类型回填一条主关系，
--     后续多类型能力统一扩展到本表，不破坏 app_movie.movie_type_id。
-- ------------------------------------------------------
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 电影-类型关系表';

-- ------------------------------------------------------
-- 3.2 电影人物维表：从导演/主演字符串快照抽取名字去重
--     当前无稳定 legacy person id，因此以 normalized_name 去重。
-- ------------------------------------------------------
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 电影人物维表';

-- ------------------------------------------------------
-- 3.3 电影-人物关系：当前只承接导演 / 主演两类关系。
--     同一人物允许在同一电影上同时承担不同 relation_type。
-- ------------------------------------------------------
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 电影-人物关系表';

-- ------------------------------------------------------
-- 4. 用户主表：从 yonghu 迁移为规范化账号资料
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `app_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `legacy_yonghu_id` bigint(20) DEFAULT NULL COMMENT '旧表 yonghu.id',
  `login_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `display_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展示名称',
  `password_value` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '当前运行时密码值，后续再切 hash',
  `avatar_url` longtext COLLATE utf8mb4_unicode_ci COMMENT '头像',
  `gender` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '性别',
  `phone_number` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `national_id_number` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '身份证',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_legacy_id` (`legacy_yonghu_id`),
  UNIQUE KEY `uk_app_user_login_name` (`login_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 用户主表';

-- ------------------------------------------------------
-- 4.1 后台用户主表：承接 users
-- ------------------------------------------------------
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

-- ------------------------------------------------------
-- 4.2 系统配置表：承接 config
-- ------------------------------------------------------
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

-- ------------------------------------------------------
-- 4.3 敏感词表：承接 sensitivewords
-- ------------------------------------------------------
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

-- ------------------------------------------------------
-- 5. 评论主表：旧表快照字段保留，赞踩用户拆到独立表
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `app_movie_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `legacy_discussdianyingxinxi_id` bigint(20) DEFAULT NULL COMMENT '旧表 discussdianyingxinxi.id',
  `movie_id` bigint(20) DEFAULT NULL COMMENT '关联 app_movie.id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '关联 app_user.id',
  `legacy_refid` bigint(20) NOT NULL COMMENT '旧表 refid',
  `legacy_userid` bigint(20) NOT NULL COMMENT '旧表 userid',
  `author_avatar_snapshot` longtext COLLATE utf8mb4_unicode_ci COMMENT '评论时头像快照',
  `author_name_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评论时昵称快照',
  `content_html` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `rating` double DEFAULT NULL COMMENT '评分',
  `reply_html` longtext COLLATE utf8mb4_unicode_ci COMMENT '回复内容',
  `like_count` int(11) NOT NULL DEFAULT '0' COMMENT '赞数量',
  `dislike_count` int(11) NOT NULL DEFAULT '0' COMMENT '踩数量',
  `is_pinned` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_movie_comment_legacy_id` (`legacy_discussdianyingxinxi_id`),
  KEY `idx_app_movie_comment_movie_id` (`movie_id`),
  KEY `idx_app_movie_comment_user_id` (`user_id`),
  CONSTRAINT `fk_app_movie_comment_movie_id` FOREIGN KEY (`movie_id`) REFERENCES `app_movie` (`id`),
  CONSTRAINT `fk_app_movie_comment_user_id` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 电影评论表';

CREATE TABLE IF NOT EXISTS `app_movie_comment_vote` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `comment_id` bigint(20) NOT NULL COMMENT '关联 app_movie_comment.id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '关联 app_user.id',
  `legacy_comment_id` bigint(20) NOT NULL COMMENT '旧表 discussdianyingxinxi.id',
  `legacy_vote_user_id` bigint(20) NOT NULL COMMENT '逗号串中的历史用户 id',
  `vote_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'like / dislike',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_movie_comment_vote_legacy` (`legacy_comment_id`,`vote_type`,`legacy_vote_user_id`),
  KEY `idx_app_movie_comment_vote_comment_id` (`comment_id`),
  KEY `idx_app_movie_comment_vote_user_id` (`user_id`),
  CONSTRAINT `fk_app_movie_comment_vote_comment_id` FOREIGN KEY (`comment_id`) REFERENCES `app_movie_comment` (`id`),
  CONSTRAINT `fk_app_movie_comment_vote_user_id` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 评论赞踩关系表';

-- ------------------------------------------------------
-- 6. 用户-电影行为表：拆分 storeup 混合语义
--    现有已验证：
--    1  = favorite
--    21 = like
--    22 = dislike
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `app_user_movie_action` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `legacy_storeup_id` bigint(20) DEFAULT NULL COMMENT '旧表 storeup.id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '关联 app_user.id',
  `movie_id` bigint(20) DEFAULT NULL COMMENT '关联 app_movie.id',
  `legacy_userid` bigint(20) NOT NULL COMMENT '旧表 userid',
  `legacy_refid` bigint(20) DEFAULT NULL COMMENT '旧表 refid',
  `legacy_tablename` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '旧表 tablename',
  `action_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'favorite / like / dislike / legacy_unknown:*',
  `target_name_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标名称快照',
  `target_picture_snapshot` longtext COLLATE utf8mb4_unicode_ci COMMENT '目标图片快照',
  `recommend_type` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐类型',
  `remark` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_movie_action_legacy_id` (`legacy_storeup_id`),
  KEY `idx_app_user_movie_action_user_id` (`user_id`),
  KEY `idx_app_user_movie_action_movie_id` (`movie_id`),
  KEY `idx_app_user_movie_action_type` (`action_type`),
  CONSTRAINT `fk_app_user_movie_action_user_id` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`),
  CONSTRAINT `fk_app_user_movie_action_movie_id` FOREIGN KEY (`movie_id`) REFERENCES `app_movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 用户电影行为表';

-- ------------------------------------------------------
-- 7. 电影订单：保留订单与用户/电影映射，并保留旧快照字段
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `app_movie_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `legacy_dianyingdingdan_id` bigint(20) DEFAULT NULL COMMENT '旧表 dianyingdingdan.id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '关联 app_user.id',
  `movie_id` bigint(20) DEFAULT NULL COMMENT '关联 app_movie.id',
  `order_no` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '订单编号',
  `legacy_movie_code` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '旧表电影编号快照',
  `movie_title_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电影名称快照',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '订单价格快照',
  `user_login_name_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名快照',
  `user_display_name_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓名快照',
  `phone_number_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号快照',
  `purchased_at` datetime DEFAULT NULL COMMENT '购买时间',
  `payment_status` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '未支付' COMMENT '支付状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_movie_order_legacy_id` (`legacy_dianyingdingdan_id`),
  UNIQUE KEY `uk_app_movie_order_order_no` (`order_no`),
  KEY `idx_app_movie_order_user_id` (`user_id`),
  KEY `idx_app_movie_order_movie_id` (`movie_id`),
  CONSTRAINT `fk_app_movie_order_user_id` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`),
  CONSTRAINT `fk_app_movie_order_movie_id` FOREIGN KEY (`movie_id`) REFERENCES `app_movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 电影订单表';

-- ------------------------------------------------------
-- 8. 用户电影资产：承接 wodedianying，保留订单号与访问链接快照
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `app_user_movie_library` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `legacy_wodedianying_id` bigint(20) DEFAULT NULL COMMENT '旧表 wodedianying.id',
  `movie_order_id` bigint(20) DEFAULT NULL COMMENT '关联 app_movie_order.id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '关联 app_user.id',
  `movie_id` bigint(20) DEFAULT NULL COMMENT '关联 app_movie.id',
  `legacy_order_no` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '旧表订单编号快照',
  `legacy_movie_code` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '旧表电影编号快照',
  `movie_title_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电影名称快照',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '价格快照',
  `user_login_name_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名快照',
  `user_display_name_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓名快照',
  `phone_number_snapshot` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号快照',
  `movie_link_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电影链接快照',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_movie_library_legacy_id` (`legacy_wodedianying_id`),
  KEY `idx_app_user_movie_library_order_id` (`movie_order_id`),
  KEY `idx_app_user_movie_library_user_id` (`user_id`),
  KEY `idx_app_user_movie_library_movie_id` (`movie_id`),
  CONSTRAINT `fk_app_user_movie_library_order_id` FOREIGN KEY (`movie_order_id`) REFERENCES `app_movie_order` (`id`),
  CONSTRAINT `fk_app_user_movie_library_user_id` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`),
  CONSTRAINT `fk_app_user_movie_library_movie_id` FOREIGN KEY (`movie_id`) REFERENCES `app_movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 用户电影资产表';

-- ------------------------------------------------------
-- 9. 会话表：token 保留旧 subject 快照，同时给 yonghu/users 映射规范化主体 id
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `app_auth_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `legacy_token_id` bigint(20) DEFAULT NULL COMMENT '旧表 token.id',
  `app_user_id` bigint(20) DEFAULT NULL COMMENT '关联 app_user.id，仅 yonghu 可映射',
  `app_admin_user_id` bigint(20) DEFAULT NULL COMMENT '关联 app_admin_user.id，仅 users 可映射',
  `legacy_subject_id` bigint(20) NOT NULL COMMENT '旧表 token.userid',
  `subject_table_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '旧表 tablename',
  `subject_role_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '旧表 role',
  `subject_kind` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'front_user / admin_user / legacy:*',
  `subject_login_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录名快照',
  `session_token` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'token 值',
  `issued_at` datetime NOT NULL COMMENT '签发时间',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_auth_session_legacy_id` (`legacy_token_id`),
  UNIQUE KEY `uk_app_auth_session_token` (`session_token`),
  KEY `idx_app_auth_session_app_user_id` (`app_user_id`),
  KEY `idx_app_auth_session_app_admin_user_id` (`app_admin_user_id`),
  CONSTRAINT `fk_app_auth_session_app_user_id` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`id`),
  CONSTRAINT `fk_app_auth_session_app_admin_user_id` FOREIGN KEY (`app_admin_user_id`) REFERENCES `app_admin_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='phase-1 会话表';

-- ------------------------------------------------------
-- 回填 1：电影类型
-- ------------------------------------------------------
INSERT INTO `app_movie_type` (
  `legacy_dianyingleixing_id`,
  `type_name`,
  `source_note`,
  `created_at`,
  `updated_at`
)
SELECT
  t.id,
  TRIM(t.dianyingleixing),
  'dianyingleixing',
  t.addtime,
  t.addtime
FROM `dianyingleixing` t
WHERE TRIM(COALESCE(t.dianyingleixing, '')) <> ''
ON DUPLICATE KEY UPDATE
  `type_name` = VALUES(`type_name`),
  `source_note` = VALUES(`source_note`),
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `app_movie_type` (
  `legacy_dianyingleixing_id`,
  `type_name`,
  `source_note`
)
SELECT
  NULL,
  TRIM(m.dianyingleixing),
  'dianyingxinxi.distinct'
FROM `dianyingxinxi` m
WHERE TRIM(COALESCE(m.dianyingleixing, '')) <> ''
  AND NOT EXISTS (
    SELECT 1
    FROM `app_movie_type` mt
    WHERE mt.`type_name` = TRIM(m.dianyingleixing)
  );

-- ------------------------------------------------------
-- 回填 1.1：电影地区维表
-- ------------------------------------------------------
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
    MIN(TRIM(m.`quyu`)) AS `region_name`,
    LOWER(TRIM(m.`quyu`)) AS `normalized_name`,
    'dianyingxinxi.quyu' AS `source_note`,
    MIN(COALESCE(m.`addtime`, CURRENT_TIMESTAMP)) AS `created_at`
  FROM `dianyingxinxi` m
  WHERE TRIM(COALESCE(m.`quyu`, '')) <> ''
  GROUP BY LOWER(TRIM(m.`quyu`))
) src
ON DUPLICATE KEY UPDATE
  `region_name` = VALUES(`region_name`),
  `source_note` = VALUES(`source_note`),
  `updated_at` = CURRENT_TIMESTAMP;

-- ------------------------------------------------------
-- 回填 2：电影主表
-- ------------------------------------------------------
INSERT INTO `app_movie` (
  `legacy_dianyingxinxi_id`,
  `movie_type_id`,
  `legacy_type_name`,
  `title`,
  `poster_urls_csv`,
  `region_name`,
  `region_id`,
  `release_date`,
  `director_name`,
  `cast_names`,
  `synopsis`,
  `detail_html`,
  `like_count`,
  `dislike_count`,
  `click_count`,
  `comment_count`,
  `favorite_count`,
  `total_score`,
  `last_clicked_at`,
  `created_at`,
  `updated_at`
)
SELECT
  m.id,
  mt.id,
  m.dianyingleixing,
  m.dianyingmingcheng,
  m.haibao,
  m.quyu,
  mr.id,
  m.shangyingshijian,
  m.daoyan,
  m.zhuyan,
  m.juqingjianjie,
  m.dianyingxiangqing,
  COALESCE(m.thumbsupnum, 0),
  COALESCE(m.crazilynum, 0),
  COALESCE(m.clicknum, 0),
  COALESCE(m.discussnum, 0),
  COALESCE(m.storeupnum, 0),
  COALESCE(m.totalscore, 0),
  m.clicktime,
  m.addtime,
  m.addtime
FROM `dianyingxinxi` m
LEFT JOIN `app_movie_type` mt
  ON mt.`type_name` = TRIM(COALESCE(m.dianyingleixing, ''))
LEFT JOIN `app_movie_region` mr
  ON mr.`normalized_name` = LOWER(TRIM(COALESCE(m.quyu, '')))
ON DUPLICATE KEY UPDATE
  `movie_type_id` = VALUES(`movie_type_id`),
  `legacy_type_name` = VALUES(`legacy_type_name`),
  `title` = VALUES(`title`),
  `poster_urls_csv` = VALUES(`poster_urls_csv`),
  `region_name` = VALUES(`region_name`),
  `region_id` = VALUES(`region_id`),
  `release_date` = VALUES(`release_date`),
  `director_name` = VALUES(`director_name`),
  `cast_names` = VALUES(`cast_names`),
  `synopsis` = VALUES(`synopsis`),
  `detail_html` = VALUES(`detail_html`),
  `like_count` = VALUES(`like_count`),
  `dislike_count` = VALUES(`dislike_count`),
  `click_count` = VALUES(`click_count`),
  `comment_count` = VALUES(`comment_count`),
  `favorite_count` = VALUES(`favorite_count`),
  `total_score` = VALUES(`total_score`),
  `last_clicked_at` = VALUES(`last_clicked_at`),
  `updated_at` = CURRENT_TIMESTAMP;

-- ------------------------------------------------------
-- 回填 2.1：电影-类型关系
-- 兼容期先按 app_movie.movie_type_id 回填一条主关系，
-- is_primary=1, sort_order=1；重复执行时重新对齐当前主类型，
-- 并清理历史残留的 is_primary=1，避免同一电影出现多个主类型。
-- ------------------------------------------------------
INSERT INTO `app_movie_type_rel` (
  `movie_id`,
  `type_id`,
  `is_primary`,
  `sort_order`,
  `created_at`,
  `updated_at`
)
SELECT
  am.id,
  am.movie_type_id,
  1,
  1,
  COALESCE(am.created_at, CURRENT_TIMESTAMP),
  COALESCE(am.updated_at, CURRENT_TIMESTAMP)
FROM `app_movie` am
WHERE am.`movie_type_id` IS NOT NULL
ON DUPLICATE KEY UPDATE
  `is_primary` = VALUES(`is_primary`),
  `sort_order` = VALUES(`sort_order`),
  `updated_at` = CURRENT_TIMESTAMP;

UPDATE `app_movie_type_rel` amtr
INNER JOIN `app_movie` am
  ON am.`id` = amtr.`movie_id`
SET
  amtr.`is_primary` = CASE
    WHEN am.`movie_type_id` IS NOT NULL AND amtr.`type_id` = am.`movie_type_id` THEN 1
    ELSE 0
  END,
  amtr.`sort_order` = CASE
    WHEN am.`movie_type_id` IS NOT NULL AND amtr.`type_id` = am.`movie_type_id` THEN 1
    ELSE amtr.`sort_order`
  END,
  amtr.`updated_at` = CURRENT_TIMESTAMP
WHERE amtr.`is_primary` <> CASE
    WHEN am.`movie_type_id` IS NOT NULL AND amtr.`type_id` = am.`movie_type_id` THEN 1
    ELSE 0
  END
  OR (
    am.`movie_type_id` IS NOT NULL
    AND amtr.`type_id` = am.`movie_type_id`
    AND amtr.`sort_order` <> 1
  );

-- ------------------------------------------------------
-- 回填 2.2：电影人物维表
-- 旧导演/主演字段当前是字符串快照；这里按常见分隔符拆分后去重。
-- 如后续发现单条记录人物数量 > 100，需要同步放大数字序列表上限。
-- ------------------------------------------------------
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

-- ------------------------------------------------------
-- 回填 2.3：电影-人物关系
--     关系真值从 app_movie 的导演/主演快照拆分而来；
--     重跑时会更新顺序和快照名称。
-- ------------------------------------------------------
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

-- ------------------------------------------------------
-- 回填 2.4：清理已不在当前快照中的导演/主演关系，避免重跑后残留陈旧映射。
-- ------------------------------------------------------
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

-- ------------------------------------------------------
-- 回填 3：电影媒体（拆分 haibao CSV）
-- ------------------------------------------------------
INSERT INTO `app_movie_media` (
  `movie_id`,
  `legacy_dianyingxinxi_id`,
  `sort_order`,
  `media_role`,
  `media_url`,
  `created_at`,
  `updated_at`
)
SELECT
  am.id,
  m.id,
  seq.n,
  'poster',
  TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(m.haibao, ',', seq.n), ',', -1)),
  m.addtime,
  m.addtime
FROM `dianyingxinxi` m
INNER JOIN `app_movie` am
  ON am.`legacy_dianyingxinxi_id` = m.id
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
  ON seq.n <= 1 + LENGTH(COALESCE(m.haibao, '')) - LENGTH(REPLACE(COALESCE(m.haibao, ''), ',', ''))
WHERE TRIM(COALESCE(m.haibao, '')) <> ''
  AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(m.haibao, ',', seq.n), ',', -1)) <> ''
ON DUPLICATE KEY UPDATE
  `media_url` = VALUES(`media_url`),
  `updated_at` = CURRENT_TIMESTAMP;

-- ------------------------------------------------------
-- 回填 4：用户主表
-- ------------------------------------------------------
INSERT INTO `app_user` (
  `legacy_yonghu_id`,
  `login_name`,
  `display_name`,
  `password_value`,
  `avatar_url`,
  `gender`,
  `phone_number`,
  `national_id_number`,
  `created_at`,
  `updated_at`
)
SELECT
  y.id,
  y.yonghuzhanghao,
  y.yonghuxingming,
  y.mima,
  y.touxiang,
  y.xingbie,
  y.lianxidianhua,
  y.shenfenzheng,
  y.addtime,
  y.addtime
FROM `yonghu` y
ON DUPLICATE KEY UPDATE
  `login_name` = VALUES(`login_name`),
  `display_name` = VALUES(`display_name`),
  `password_value` = VALUES(`password_value`),
  `avatar_url` = VALUES(`avatar_url`),
  `gender` = VALUES(`gender`),
  `phone_number` = VALUES(`phone_number`),
  `national_id_number` = VALUES(`national_id_number`),
  `updated_at` = CURRENT_TIMESTAMP;

-- ------------------------------------------------------
-- 回填 4.1：后台用户主表
-- ------------------------------------------------------
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
FROM `users` u
ON DUPLICATE KEY UPDATE
  `username` = VALUES(`username`),
  `password` = VALUES(`password`),
  `image` = VALUES(`image`),
  `role` = VALUES(`role`),
  `updated_at` = CURRENT_TIMESTAMP;

-- ------------------------------------------------------
-- 回填 4.2：系统配置表
-- ------------------------------------------------------
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
FROM `config` c
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `value` = VALUES(`value`),
  `url` = VALUES(`url`),
  `updated_at` = CURRENT_TIMESTAMP;

-- ------------------------------------------------------
-- 回填 4.3：敏感词表
-- ------------------------------------------------------
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
FROM `sensitivewords` s
WHERE TRIM(COALESCE(s.content, '')) <> ''
ON DUPLICATE KEY UPDATE
  `content` = VALUES(`content`),
  `updated_at` = CURRENT_TIMESTAMP;

-- ------------------------------------------------------
-- 回填 5：评论主表
-- ------------------------------------------------------
INSERT INTO `app_movie_comment` (
  `legacy_discussdianyingxinxi_id`,
  `movie_id`,
  `user_id`,
  `legacy_refid`,
  `legacy_userid`,
  `author_avatar_snapshot`,
  `author_name_snapshot`,
  `content_html`,
  `rating`,
  `reply_html`,
  `like_count`,
  `dislike_count`,
  `is_pinned`,
  `created_at`,
  `updated_at`
)
SELECT
  d.id,
  am.id,
  au.id,
  d.refid,
  d.userid,
  d.avatarurl,
  d.nickname,
  d.content,
  d.score,
  d.reply,
  COALESCE(d.thumbsupnum, 0),
  COALESCE(d.crazilynum, 0),
  COALESCE(d.istop, 0),
  d.addtime,
  d.addtime
FROM `discussdianyingxinxi` d
LEFT JOIN `app_movie` am
  ON am.`legacy_dianyingxinxi_id` = d.refid
LEFT JOIN `app_user` au
  ON au.`legacy_yonghu_id` = d.userid
ON DUPLICATE KEY UPDATE
  `movie_id` = VALUES(`movie_id`),
  `user_id` = VALUES(`user_id`),
  `legacy_refid` = VALUES(`legacy_refid`),
  `legacy_userid` = VALUES(`legacy_userid`),
  `author_avatar_snapshot` = VALUES(`author_avatar_snapshot`),
  `author_name_snapshot` = VALUES(`author_name_snapshot`),
  `content_html` = VALUES(`content_html`),
  `rating` = VALUES(`rating`),
  `reply_html` = VALUES(`reply_html`),
  `like_count` = VALUES(`like_count`),
  `dislike_count` = VALUES(`dislike_count`),
  `is_pinned` = VALUES(`is_pinned`),
  `updated_at` = CURRENT_TIMESTAMP;

-- ------------------------------------------------------
-- 回填 6：评论赞踩关系
-- MySQL 5.7 无递归 CTE，这里使用 1..100 的数字序列拆分逗号串。
-- 如果单条评论赞踩用户超过 100 个，需要把序列上限继续放大。
-- ------------------------------------------------------
INSERT INTO `app_movie_comment_vote` (
  `comment_id`,
  `user_id`,
  `legacy_comment_id`,
  `legacy_vote_user_id`,
  `vote_type`,
  `created_at`
)
SELECT
  ac.id,
  au.id,
  d.id,
  CAST(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(d.tuserids, ',', seq.n), ',', -1)) AS UNSIGNED),
  'like',
  d.addtime
FROM `discussdianyingxinxi` d
INNER JOIN `app_movie_comment` ac
  ON ac.`legacy_discussdianyingxinxi_id` = d.id
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
  ON seq.n <= 1 + LENGTH(COALESCE(d.tuserids, '')) - LENGTH(REPLACE(COALESCE(d.tuserids, ''), ',', ''))
LEFT JOIN `app_user` au
  ON au.`legacy_yonghu_id` = CAST(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(d.tuserids, ',', seq.n), ',', -1)) AS UNSIGNED)
WHERE TRIM(COALESCE(d.tuserids, '')) <> ''
  AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(d.tuserids, ',', seq.n), ',', -1)) <> ''
ON DUPLICATE KEY UPDATE
  `user_id` = VALUES(`user_id`);

INSERT INTO `app_movie_comment_vote` (
  `comment_id`,
  `user_id`,
  `legacy_comment_id`,
  `legacy_vote_user_id`,
  `vote_type`,
  `created_at`
)
SELECT
  ac.id,
  au.id,
  d.id,
  CAST(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(d.cuserids, ',', seq.n), ',', -1)) AS UNSIGNED),
  'dislike',
  d.addtime
FROM `discussdianyingxinxi` d
INNER JOIN `app_movie_comment` ac
  ON ac.`legacy_discussdianyingxinxi_id` = d.id
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
  ON seq.n <= 1 + LENGTH(COALESCE(d.cuserids, '')) - LENGTH(REPLACE(COALESCE(d.cuserids, ''), ',', ''))
LEFT JOIN `app_user` au
  ON au.`legacy_yonghu_id` = CAST(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(d.cuserids, ',', seq.n), ',', -1)) AS UNSIGNED)
WHERE TRIM(COALESCE(d.cuserids, '')) <> ''
  AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(d.cuserids, ',', seq.n), ',', -1)) <> ''
ON DUPLICATE KEY UPDATE
  `user_id` = VALUES(`user_id`);

-- ------------------------------------------------------
-- 回填 7：用户电影行为
-- ------------------------------------------------------
INSERT INTO `app_user_movie_action` (
  `legacy_storeup_id`,
  `user_id`,
  `movie_id`,
  `legacy_userid`,
  `legacy_refid`,
  `legacy_tablename`,
  `action_type`,
  `target_name_snapshot`,
  `target_picture_snapshot`,
  `recommend_type`,
  `created_at`,
  `updated_at`
)
SELECT
  s.id,
  au.id,
  am.id,
  s.userid,
  s.refid,
  s.tablename,
  CASE
    WHEN s.type = '1' THEN 'favorite'
    WHEN s.type = '21' THEN 'like'
    WHEN s.type = '22' THEN 'dislike'
    ELSE CONCAT('legacy_unknown:', COALESCE(s.type, 'null'))
  END,
  s.name,
  s.picture,
  s.inteltype,
  s.addtime,
  s.addtime
FROM `storeup` s
LEFT JOIN `app_user` au
  ON au.`legacy_yonghu_id` = s.userid
LEFT JOIN `app_movie` am
  ON am.`legacy_dianyingxinxi_id` = s.refid
 AND s.tablename = 'dianyingxinxi'
ON DUPLICATE KEY UPDATE
  `user_id` = VALUES(`user_id`),
  `movie_id` = VALUES(`movie_id`),
  `legacy_userid` = VALUES(`legacy_userid`),
  `legacy_refid` = VALUES(`legacy_refid`),
  `legacy_tablename` = VALUES(`legacy_tablename`),
  `action_type` = VALUES(`action_type`),
  `target_name_snapshot` = VALUES(`target_name_snapshot`),
  `target_picture_snapshot` = VALUES(`target_picture_snapshot`),
  `recommend_type` = VALUES(`recommend_type`),
  `updated_at` = CURRENT_TIMESTAMP;

-- ------------------------------------------------------
-- 回填 8：电影订单
-- ------------------------------------------------------
INSERT INTO `app_movie_order` (
  `legacy_dianyingdingdan_id`,
  `user_id`,
  `movie_id`,
  `order_no`,
  `legacy_movie_code`,
  `movie_title_snapshot`,
  `unit_price`,
  `user_login_name_snapshot`,
  `user_display_name_snapshot`,
  `phone_number_snapshot`,
  `purchased_at`,
  `payment_status`,
  `created_at`,
  `updated_at`
)
SELECT
  o.id,
  au.id,
  am.id,
  o.dingdanbianhao,
  o.dianyingbianhao,
  o.dianyingmingcheng,
  CAST(o.jiage AS DECIMAL(10,2)),
  o.yonghuming,
  o.xingming,
  o.shoujihao,
  o.goumaishijian,
  COALESCE(NULLIF(o.ispay, ''), '未支付'),
  o.addtime,
  o.addtime
FROM `dianyingdingdan` o
LEFT JOIN `app_user` au
  ON au.`login_name` = o.yonghuming
LEFT JOIN `app_movie` am
  ON CAST(am.`legacy_dianyingxinxi_id` AS CHAR(32)) = o.dianyingbianhao
ON DUPLICATE KEY UPDATE
  `user_id` = VALUES(`user_id`),
  `movie_id` = VALUES(`movie_id`),
  `order_no` = VALUES(`order_no`),
  `legacy_movie_code` = VALUES(`legacy_movie_code`),
  `movie_title_snapshot` = VALUES(`movie_title_snapshot`),
  `unit_price` = VALUES(`unit_price`),
  `user_login_name_snapshot` = VALUES(`user_login_name_snapshot`),
  `user_display_name_snapshot` = VALUES(`user_display_name_snapshot`),
  `phone_number_snapshot` = VALUES(`phone_number_snapshot`),
  `purchased_at` = VALUES(`purchased_at`),
  `payment_status` = VALUES(`payment_status`),
  `updated_at` = CURRENT_TIMESTAMP;

-- ------------------------------------------------------
-- 回填 9：用户电影资产
-- ------------------------------------------------------
INSERT INTO `app_user_movie_library` (
  `legacy_wodedianying_id`,
  `movie_order_id`,
  `user_id`,
  `movie_id`,
  `legacy_order_no`,
  `legacy_movie_code`,
  `movie_title_snapshot`,
  `unit_price`,
  `user_login_name_snapshot`,
  `user_display_name_snapshot`,
  `phone_number_snapshot`,
  `movie_link_url`,
  `created_at`,
  `updated_at`
)
SELECT
  l.id,
  amo.id,
  au.id,
  am.id,
  l.dingdanbianhao,
  l.dianyingbianhao,
  l.dianyingmingcheng,
  CAST(l.jiage AS DECIMAL(10,2)),
  l.yonghuming,
  l.xingming,
  l.shoujihao,
  l.dianyinglianjie,
  l.addtime,
  l.addtime
FROM `wodedianying` l
LEFT JOIN `app_movie_order` amo
  ON amo.`order_no` = l.dingdanbianhao
LEFT JOIN `app_user` au
  ON au.`login_name` = l.yonghuming
LEFT JOIN `app_movie` am
  ON CAST(am.`legacy_dianyingxinxi_id` AS CHAR(32)) = l.dianyingbianhao
ON DUPLICATE KEY UPDATE
  `movie_order_id` = VALUES(`movie_order_id`),
  `user_id` = VALUES(`user_id`),
  `movie_id` = VALUES(`movie_id`),
  `legacy_order_no` = VALUES(`legacy_order_no`),
  `legacy_movie_code` = VALUES(`legacy_movie_code`),
  `movie_title_snapshot` = VALUES(`movie_title_snapshot`),
  `unit_price` = VALUES(`unit_price`),
  `user_login_name_snapshot` = VALUES(`user_login_name_snapshot`),
  `user_display_name_snapshot` = VALUES(`user_display_name_snapshot`),
  `phone_number_snapshot` = VALUES(`phone_number_snapshot`),
  `movie_link_url` = VALUES(`movie_link_url`),
  `updated_at` = CURRENT_TIMESTAMP;

-- ------------------------------------------------------
-- 回填 10：会话表
-- 当前按 tablename 把 yonghu 映射到 app_user_id，
-- 把 users 映射到 app_admin_user_id；两类会话都保留旧 subject 快照。
-- ------------------------------------------------------
INSERT INTO `app_auth_session` (
  `legacy_token_id`,
  `app_user_id`,
  `app_admin_user_id`,
  `legacy_subject_id`,
  `subject_table_name`,
  `subject_role_name`,
  `subject_kind`,
  `subject_login_name`,
  `session_token`,
  `issued_at`,
  `expires_at`,
  `created_at`,
  `updated_at`
)
SELECT
  t.id,
  au.id,
  aau.id,
  t.userid,
  t.tablename,
  t.role,
  CASE
    WHEN t.tablename = 'yonghu' THEN 'front_user'
    WHEN t.tablename = 'users' THEN 'admin_user'
    ELSE CONCAT('legacy:', COALESCE(t.tablename, 'unknown'))
  END,
  t.username,
  t.token,
  t.addtime,
  t.expiratedtime,
  t.addtime,
  t.addtime
FROM `token` t
LEFT JOIN `app_user` au
  ON au.`legacy_yonghu_id` = t.userid
 AND t.tablename = 'yonghu'
LEFT JOIN `app_admin_user` aau
  ON aau.`legacy_users_id` = t.userid
 AND t.tablename = 'users'
ON DUPLICATE KEY UPDATE
  `app_user_id` = VALUES(`app_user_id`),
  `app_admin_user_id` = VALUES(`app_admin_user_id`),
  `legacy_subject_id` = VALUES(`legacy_subject_id`),
  `subject_table_name` = VALUES(`subject_table_name`),
  `subject_role_name` = VALUES(`subject_role_name`),
  `subject_kind` = VALUES(`subject_kind`),
  `subject_login_name` = VALUES(`subject_login_name`),
  `session_token` = VALUES(`session_token`),
  `issued_at` = VALUES(`issued_at`),
  `expires_at` = VALUES(`expires_at`),
  `updated_at` = CURRENT_TIMESTAMP;

INSERT IGNORE INTO `schema_refactor_migration_log` (
  `migration_key`,
  `description`
) VALUES (
  'phase1_compatibility_normalization',
  '创建 phase-1 规范化目标表，并从运行时旧表执行首轮兼容回填'
);

-- ------------------------------------------------------
-- 验证建议（执行后手工核对）
-- ------------------------------------------------------
-- SELECT COUNT(*) FROM app_movie;
-- SELECT COUNT(*) FROM app_user;
-- SELECT action_type, COUNT(*) FROM app_user_movie_action GROUP BY action_type;
-- SELECT payment_status, COUNT(*) FROM app_movie_order GROUP BY payment_status;
-- SELECT subject_kind, COUNT(*) FROM app_auth_session GROUP BY subject_kind;
-- SELECT COUNT(*) FROM app_user_movie_library;
-- SELECT COUNT(*) FROM app_movie_comment_vote;
-- SELECT COUNT(*) FROM app_movie_person;
-- SELECT COUNT(*) FROM app_movie_region;
-- SELECT COUNT(*) FROM app_movie WHERE TRIM(COALESCE(region_name, '')) <> '' AND region_id IS NULL;
-- SELECT relation_type, COUNT(*) FROM app_movie_person_rel GROUP BY relation_type;
-- SELECT movie_id, relation_type, person_id, COUNT(*) AS duplicate_count
-- FROM app_movie_person_rel
-- GROUP BY movie_id, relation_type, person_id
-- HAVING COUNT(*) > 1;
-- SELECT am.id, am.title
-- FROM app_movie am
-- WHERE TRIM(COALESCE(am.director_name, '')) <> ''
--   AND NOT EXISTS (
--     SELECT 1
--     FROM app_movie_person_rel rel
--     WHERE rel.movie_id = am.id
--       AND rel.relation_type = 'director'
--   );
-- SELECT am.id, am.title
-- FROM app_movie am
-- WHERE TRIM(COALESCE(am.cast_names, '')) <> ''
--   AND NOT EXISTS (
--     SELECT 1
--     FROM app_movie_person_rel rel
--     WHERE rel.movie_id = am.id
--       AND rel.relation_type = 'cast'
--   );
-- SELECT movie_id, COUNT(*) AS primary_count
-- FROM app_movie_type_rel
-- WHERE is_primary = 1
-- GROUP BY movie_id
-- HAVING COUNT(*) > 1;
--
-- 后续切换建议：
-- 1. 运行时代码继续读写旧表，避免一次性重命名。
-- 2. 若旧表持续发生新写入，重复执行本脚本即可做增量回填。
-- 3. 如果要做到实时双写，再在下一阶段补充触发器或服务层双写。
