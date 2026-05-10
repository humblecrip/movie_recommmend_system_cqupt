# Phase-1 数据库兼容迁移说明

## 目标

这轮只落 phase-1 数据库重构工件，不改现有 Java/MyBatis/前端的运行表名。

对应 SQL 文件：

- `db/phase1_compatibility_normalization.sql`

## 旧表与新表映射

- `dianyingleixing` -> `app_movie_type`
- `dianyingxinxi` -> `app_movie` + `app_movie_media` + `app_movie_type_rel`
- `yonghu` -> `app_user`
- `discussdianyingxinxi` -> `app_movie_comment` + `app_movie_comment_vote`
- `storeup` -> `app_user_movie_action`
- `dianyingdingdan` -> `app_movie_order`
- `wodedianying` -> `app_user_movie_library`
- `token` -> `app_auth_session`

每张新表都保留 `legacy_*_id`，这样后续代码切换时可以稳定回查旧记录。

## 兼容策略

1. 旧表不删、不改名，当前代码继续使用旧表。
2. 新表全部使用 `CREATE TABLE IF NOT EXISTS`，允许脚本重复执行。
3. 回填使用 `INSERT ... SELECT ... ON DUPLICATE KEY UPDATE`，允许增量重跑。
4. `storeup.type` 当前按已验证前端语义映射：
   - `1` -> `favorite`
   - `21` -> `like`
   - `22` -> `dislike`
5. `discussdianyingxinxi.tuserids/cuserids` 会被拆到 `app_movie_comment_vote`。
6. `dianyingdingdan` 会进入 `app_movie_order`，保留订单号、支付状态和用户/电影快照；能映射时再补 `user_id` / `movie_id`。
7. `wodedianying` 会进入 `app_user_movie_library`，保留订单号、电影链接和用户/电影快照。
8. `token` 会按 `tablename` 做主体映射：`yonghu` 映射到 `app_user_id`，`users` 映射到 `app_admin_user_id`；两类会话都会继续保留旧 subject 快照。
9. `storeup.remark` 不再作为 phase-1 回填前置条件；因为 `ssmf7s0a.sql` 真值源里没有该列，本轮先不从旧表读取备注，避免脚本直接失败。

## 本轮补充结构说明

### 1. `app_movie_type_rel`

新增 `app_movie_type_rel` 作为电影-类型多对多关系表，字段包括：

- `id`
- `movie_id`
- `type_id`
- `is_primary`
- `sort_order`
- `created_at`
- `updated_at`

约束：

- 外键到 `app_movie(id)` / `app_movie_type(id)`
- 唯一约束：`(movie_id, type_id)`
- 辅助索引：`(type_id)`、`(movie_id, is_primary, sort_order)`

首轮回填策略：

- 当前旧库还是单类型字符串，因此先按 `app_movie.movie_type_id` 回填一条 relation
- `is_primary = 1`
- `sort_order = 1`
- 继续保留 `app_movie.movie_type_id`，作为兼容期主类型 / 默认类型引用
- 脚本重复执行时，会把 relation 主类型重新对齐到当前 `app_movie.movie_type_id`，并清掉历史残留的 `is_primary = 1`

### 2. `app_movie.movie_type_id` 的 phase-1 语义

`app_movie.movie_type_id` 本轮**不删除、不改名**，但它不再代表“电影只有一个类型”的最终模型。

当前语义明确为：

- 兼容期主类型 / 默认类型引用
- 方便当前仍按单类型读取的 runtime 继续工作
- 最终多类型表达以 `app_movie_type_rel` 为准

### 3. `app_movie` 统计字段语义澄清

以下字段在 phase-1 中继续保留，但统一视为**汇总快照字段**，不是最终真值源：

- `like_count`
- `dislike_count`
- `click_count`
- `comment_count`
- `favorite_count`
- `total_score`
- `last_clicked_at`

对应真值说明：

- `like_count` / `dislike_count` / `favorite_count`
  真值源为 `app_user_movie_action`
- `comment_count` / `total_score`
  真值源为 `app_movie_comment`
- `click_count` / `last_clicked_at`
  本轮仍仅保留快照语义，phase-1 尚未引入独立点击事件真值表

### 4. 为什么这轮没有新增 `app_movie_view_log`

这轮**没有**把 `app_movie_view_log` 落进主迁移脚本，原因是：

- 当前旧库没有可稳定复用的点击事件明细表可供首轮回填
- 这轮目标是“兼容式数据库工件重构”，优先补齐已确认的类型关系表达
- 如果现在只加一个空事件表，会增加 schema 漂移，但不能形成可验证的首轮闭环

因此本轮对点击仍保持：

- `app_movie.click_count`
- `app_movie.last_clicked_at`

作为兼容快照字段保留，后续如果决定把点击真值也事件化，再单独补 `app_movie_view_log` 与 runtime 写入链路。

## 执行方式

在目标 MySQL 库执行：

```sql
SOURCE db/phase1_compatibility_normalization.sql;
```

或：

```bash
mysql -u <user> -p <database> < db/phase1_compatibility_normalization.sql
```

脚本默认 `USE ssmf7s0a;`。如果实际数据库名不同，先改首行再执行。

## 执行后建议核对

```sql
SELECT COUNT(*) FROM app_movie;
SELECT COUNT(*) FROM app_movie_type_rel;
SELECT COUNT(*) FROM app_user;
SELECT action_type, COUNT(*) FROM app_user_movie_action GROUP BY action_type;
SELECT payment_status, COUNT(*) FROM app_movie_order GROUP BY payment_status;
SELECT subject_kind, COUNT(*) FROM app_auth_session GROUP BY subject_kind;
SELECT COUNT(*) FROM app_user_movie_library;
SELECT COUNT(*) FROM app_movie_comment_vote;
SELECT is_primary, COUNT(*) FROM app_movie_type_rel GROUP BY is_primary;
SELECT movie_id, COUNT(*) AS primary_count
FROM app_movie_type_rel
WHERE is_primary = 1
GROUP BY movie_id
HAVING COUNT(*) > 1;
```

## 已知边界

- 这轮没有改运行时代码，所以新表不会自动实时同步旧表写入。
- 如果旧表在执行后继续发生新增/修改，需要重跑脚本做增量回填。
- 这轮只把多类型 schema 表达落到数据库工件；runtime 何时改成真正消费 `app_movie_type_rel`，要放到后续代码切换任务里处理。
- `app_movie_media` 采用 CSV 拆分回填；如果单条媒体数量异常大，需要把 SQL 里的序列上限继续放大。
- `dianyingdingdan.dianyingbianhao` / `wodedianying.dianyingbianhao` 在旧库里是字符串快照，不保证都能映射成 `app_movie.movie_id`；映射失败时会继续保留快照字段。
- `storeup.remark` 仅出现在 `db/springbootdo4wek3z.sql`，不在 `ssmf7s0a.sql` 中；phase-1 脚本不再依赖这个列存在。
- 这轮已经补了 `app_admin_user` / `app_auth_session.app_admin_user_id` 这层兼容映射，但还没有开始让运行时代码实际切到管理员规范表。
