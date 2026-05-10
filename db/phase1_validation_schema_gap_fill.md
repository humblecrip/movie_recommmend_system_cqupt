# phase1 验证库 schema 补齐说明

## 适用场景

- 目标库：`ssmf7s0a_phase1`
- 目的：
  - 补齐验证库相对当前 `db/phase1_compatibility_normalization.sql`
    的落地漂移
  - 在旧表已经改名为 `*_bak` 后，继续安全重跑补丁
- 明确禁止：
  - 不能对运行库 `ssmf7s0a` 执行

## 当前补齐范围

本轮只补当前已确认的验证库缺口：

1. `app_admin_user`
2. `app_config`
3. `app_sensitive_word`
4. `app_auth_session.app_admin_user_id`
   - 含索引 `idx_app_auth_session_app_admin_user_id`
   - 含外键 `fk_app_auth_session_app_admin_user_id`

## 为什么不用直接重跑整份 phase1 SQL

当前 `ssmf7s0a_phase1` 中的旧表已经处于 `_bak` 状态，例如：

- `users_bak`
- `config_bak`
- `sensitivewords_bak`
- `token_bak`

如果直接重跑 `db/phase1_compatibility_normalization.sql`，其中
`FROM users/config/sensitivewords/...` 的回填语句会因为源表名已经被改成
`*_bak` 而失败。

因此本轮采用：

- 单独的增量 SQL 模板：
  `db/phase1_validation_schema_gap_fill.sql`
- 历史一次性 Java 执行器已随本地代理工作流目录清理，不再作为仓库资产保留

执行补丁时仍需保持以下约束：

1. 强制只允许目标库 `ssmf7s0a_phase1`
2. 自动解析源表名：
   - 优先旧表
   - 旧表不存在时回退到 `*_bak`
3. 条件补齐 `app_auth_session.app_admin_user_id` 列和索引
4. 执行可重跑的 `INSERT ... ON DUPLICATE KEY UPDATE`
5. 最后补外键，并输出最终列 / 索引 / 外键状态
6. 漂移巡检 helper 会同步核对 `app_auth_session.app_admin_user_id`
   的列 / 索引 / 外键，而不只看列是否存在

## 执行方式

当前仓库只保留 `db/phase1_validation_schema_gap_fill.sql` 模板，不再保留
历史一次性 Java 执行器源码。

因此这里的执行要求明确为：

1. 不能直接把该 SQL 文件裸执行到数据库
2. 执行前必须先由受限 helper 完成以下预处理：
   - 绑定目标库为 `ssmf7s0a_phase1`
   - 将 `__USERS_SOURCE__` / `__CONFIG_SOURCE__` /
     `__SENSITIVEWORDS_SOURCE__` 替换为真实存在的源表名
   - 真实存在的源表名解析规则仍为“优先旧表，否则 `*_bak`”
3. helper 执行完成后，仍需补做真实 `_bak` smoke 和漂移巡检

也就是说，这份文档保留的是补丁约束与执行前提，不再把已删除的
`.trellis` 路径或失效命令当作可直接复用的仓库资产。

## 设计原则

1. 只补验证库，不碰运行库
2. 只补已确认缺口，不顺手扩其他 schema
3. 可重跑
4. 对 `_bak` 源表兼容
5. 执行后必须再做真实 `_bak` smoke，而不是只看 DDL 成功
