USE `ssmf7s0a`;

-- ======================================================
-- 增量兼容补丁
-- 目的：
-- 1. 补齐当前代码缺失的表
-- 2. 修正已有表与当前代码不一致的字段
-- 3. 不重建数据库，不删除现有数据
-- 兼容：MySQL 5.7
-- ======================================================

-- ------------------------------------------------------
-- config 表补充 url 字段
-- ------------------------------------------------------
SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'config'
        AND COLUMN_NAME = 'url'
    ),
    'SELECT ''config.url already exists''',
    'ALTER TABLE `config` ADD COLUMN `url` varchar(255) DEFAULT NULL COMMENT ''url'' AFTER `value`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------
-- users 表补充 image 字段
-- ------------------------------------------------------
SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'users'
        AND COLUMN_NAME = 'image'
    ),
    'SELECT ''users.image already exists''',
    'ALTER TABLE `users` ADD COLUMN `image` varchar(255) DEFAULT NULL COMMENT ''头像'' AFTER `password`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------
-- yonghu 表补充当前代码需要的字段
-- ------------------------------------------------------
SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'yonghu'
        AND COLUMN_NAME = 'yonghuzhanghao'
    ),
    'SELECT ''yonghu.yonghuzhanghao already exists''',
    'ALTER TABLE `yonghu` ADD COLUMN `yonghuzhanghao` varchar(200) DEFAULT NULL COMMENT ''用户账号'' AFTER `addtime`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'yonghu'
        AND COLUMN_NAME = 'yonghuxingming'
    ),
    'SELECT ''yonghu.yonghuxingming already exists''',
    'ALTER TABLE `yonghu` ADD COLUMN `yonghuxingming` varchar(200) DEFAULT NULL COMMENT ''用户姓名'' AFTER `mima`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'yonghu'
        AND COLUMN_NAME = 'touxiang'
    ),
    'SELECT ''yonghu.touxiang already exists''',
    'ALTER TABLE `yonghu` ADD COLUMN `touxiang` varchar(500) DEFAULT NULL COMMENT ''头像'' AFTER `yonghuxingming`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'yonghu'
        AND COLUMN_NAME = 'lianxidianhua'
    ),
    'SELECT ''yonghu.lianxidianhua already exists''',
    'ALTER TABLE `yonghu` ADD COLUMN `lianxidianhua` varchar(200) DEFAULT NULL COMMENT ''联系电话'' AFTER `xingbie`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'yonghu'
        AND COLUMN_NAME = 'shenfenzheng'
    ),
    'SELECT ''yonghu.shenfenzheng already exists''',
    'ALTER TABLE `yonghu` ADD COLUMN `shenfenzheng` varchar(200) DEFAULT NULL COMMENT ''身份证'' AFTER `lianxidianhua`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 老字段向新字段迁移
UPDATE `yonghu`
SET
  `yonghuzhanghao` = COALESCE(`yonghuzhanghao`, `yonghuming`),
  `yonghuxingming` = COALESCE(`yonghuxingming`, `xingming`),
  `lianxidianhua` = COALESCE(`lianxidianhua`, `shoujihao`);

-- yonghu 用户账号唯一索引
SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'yonghu'
        AND INDEX_NAME = 'uk_yonghuzhanghao'
    ),
    'SELECT ''uk_yonghuzhanghao already exists''',
    'ALTER TABLE `yonghu` ADD UNIQUE KEY `uk_yonghuzhanghao` (`yonghuzhanghao`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------
-- 缺失表：dianyingleixing
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `dianyingleixing` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `dianyingleixing` varchar(200) DEFAULT NULL COMMENT '电影类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='电影类型';

-- ------------------------------------------------------
-- 缺失表：dianyingxinxi
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `dianyingxinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `dianyingmingcheng` varchar(200) DEFAULT NULL COMMENT '电影名称',
  `haibao` varchar(500) DEFAULT NULL COMMENT '海报',
  `dianyingleixing` varchar(200) DEFAULT NULL COMMENT '电影类型',
  `quyu` varchar(200) DEFAULT NULL COMMENT '区域',
  `shangyingshijian` date DEFAULT NULL COMMENT '上映时间',
  `daoyan` varchar(200) DEFAULT NULL COMMENT '导演',
  `zhuyan` varchar(500) DEFAULT NULL COMMENT '主演',
  `juqingjianjie` longtext COMMENT '剧情简介',
  `dianyingxiangqing` longtext COMMENT '电影详情',
  `thumbsupnum` int(11) DEFAULT '0' COMMENT '赞',
  `crazilynum` int(11) DEFAULT '0' COMMENT '踩',
  `clicktime` datetime DEFAULT NULL COMMENT '最近点击时间',
  `clicknum` int(11) DEFAULT '0' COMMENT '点击次数',
  `discussnum` int(11) DEFAULT '0' COMMENT '评论数',
  `totalscore` double DEFAULT '0' COMMENT '评分',
  `storeupnum` int(11) DEFAULT '0' COMMENT '收藏数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='电影信息';

-- ------------------------------------------------------
-- 缺失表：discussdianyingxinxi
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `discussdianyingxinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `refid` bigint(20) DEFAULT NULL COMMENT '关联表id',
  `userid` bigint(20) DEFAULT NULL COMMENT '用户id',
  `avatarurl` varchar(500) DEFAULT NULL COMMENT '头像',
  `nickname` varchar(200) DEFAULT NULL COMMENT '用户名',
  `content` longtext COMMENT '评论内容',
  `score` double DEFAULT NULL COMMENT '评分',
  `reply` longtext COMMENT '回复内容',
  `thumbsupnum` int(11) DEFAULT '0' COMMENT '赞',
  `crazilynum` int(11) DEFAULT '0' COMMENT '踩',
  `istop` int(11) DEFAULT '0' COMMENT '置顶(1:置顶,0:非置顶)',
  `tuserids` longtext COMMENT '赞用户ids',
  `cuserids` longtext COMMENT '踩用户ids',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='电影信息评论表';

-- ------------------------------------------------------
-- 缺失表：sensitivewords
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sensitivewords` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `content` varchar(255) DEFAULT NULL COMMENT '内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='敏感词';

-- ------------------------------------------------------
-- 初始化最小数据
-- ------------------------------------------------------
INSERT INTO `sensitivewords` (`id`, `content`)
SELECT 1, '违规词示例'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sensitivewords` WHERE `id` = 1
);

-- 可选：给首页准备基础数据
INSERT INTO `dianyingleixing` (`dianyingleixing`)
SELECT '动作'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `dianyingleixing` WHERE `dianyingleixing` = '动作'
);

INSERT INTO `dianyingleixing` (`dianyingleixing`)
SELECT '剧情'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `dianyingleixing` WHERE `dianyingleixing` = '剧情'
);

INSERT INTO `dianyingxinxi`
(`dianyingmingcheng`,`haibao`,`dianyingleixing`,`quyu`,`shangyingshijian`,`daoyan`,`zhuyan`,`juqingjianjie`,`dianyingxiangqing`,`thumbsupnum`,`crazilynum`,`clicknum`,`discussnum`,`totalscore`,`storeupnum`)
SELECT
  '测试电影A',
  'upload/test-a.jpg',
  '动作',
  '中国',
  '2025-01-01',
  '导演A',
  '主演A',
  '简介A',
  '详情A',
  0,0,0,0,8.5,0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `dianyingxinxi` WHERE `dianyingmingcheng` = '测试电影A'
);
