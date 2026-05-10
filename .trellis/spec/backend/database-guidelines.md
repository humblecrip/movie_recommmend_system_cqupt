# Database Guidelines

> Database patterns and conventions for this project.

---

## Overview

<!--
Document your project's database conventions here.

Questions to answer:
- What ORM/query library do you use?
- How are migrations managed?
- What are the naming conventions for tables/columns?
- How do you handle transactions?
-->

(To be filled by the team)

## Scenario: compatibility-first schema refactor

### 1. Scope / Trigger

- Trigger: 运行时代码已经直接绑定旧表名、旧实体和旧 MyBatis XML，但数据库结构需要开始归一化。
- Applies to: 涉及表结构调整、迁移脚本、新旧字段并存、旧表保留运行兼容的场景。

### 2. Signatures

- Migration file location: `db/*.sql`
- Preferred script shape:
  - `CREATE TABLE IF NOT EXISTS <new_table>`
  - `INSERT INTO <new_table> ... SELECT ... FROM <legacy_table> ON DUPLICATE KEY UPDATE ...`
- New normalized tables must keep a stable legacy pointer:
  - `legacy_<old_table>_id`

### 3. Contracts

- Contract 1: 旧运行时表先保留，不做首轮硬切表名。
- Contract 2: 新表必须能从旧表重复回填，不能要求“只执行一次”。
- Contract 3: 当旧表里存在快照字段时，新表允许短期保留快照列，避免切换阶段丢上下文。
- Contract 4: 对混合语义表，先拆成行为表或关系表，再逐步让代码切新结构。
- Contract 5: PRD 已经点名纳入首批范围的核心域，至少要落目标表和回填骨架，不能只在说明里提“后续再做”。
- Contract 6: 引用旧列前必须先在仓库真值源 SQL 或明确列出的兼容补丁里确认该列存在；不能假设线上库已经额外补过列。
- Contract 7: 真值源优先级固定为 `ssmf7s0a.sql` > 同仓库其他 SQL 快照；其他快照只能作为运行现状参考，不能反向扩张 phase-1 脚本依赖。
- Contract 8: 旧表允许为空、为空串或仅保存字符串快照的字段，在 phase-1 新表中不能盲目收紧为强外键或强非空；要么保留可空快照列，要么在回填 SQL 里显式 `COALESCE` 并写明默认值语义。

### 4. Validation & Error Matrix

- 旧表仍被运行时代码依赖 -> 不允许直接 `DROP` / `RENAME` 旧表
- 新表没有 `legacy_*_id` -> 不允许进入首轮迁移，后续无法回查映射
- 回填脚本不可重跑 -> 不满足兼容迁移要求
- 旧表字段是逗号串/混合类型 -> 必须在新表中拆为规范关系或明确保留快照列
- 回填脚本直接引用仓库真值源里不存在的旧列 -> 视为阻断问题，必须先改成可兼容写法或补前置迁移说明
- PRD 已纳入的核心域没有目标表/回填骨架 -> 视为范围未闭环，不能当作 phase-1 完成

### 5. Good / Base / Bad Cases

- Good: 新建 `app_*` 目标表，保留 `legacy_*_id`，旧表继续跑，迁移脚本可重复执行
- Base: 只新建目标表和回填脚本，运行时仍完全读旧表
- Bad: 直接改旧表列名或直接让 Java 实体切英文新表，导致 mapper/controller/front 同时失效

### 6. Tests Required

- `git diff --check`
- 逐个核对迁移脚本引用的旧列是否存在于 `ssmf7s0a.sql` 或文档明确列出的兼容补丁
- 至少检查迁移脚本里是否覆盖目标旧表、新表、核心映射字段
- 至少抽查新表的 `NOT NULL` / 外键列，确认旧表脏数据、空值、字符串快照不会让整批回填直接失败
- 有数据库副本时，执行：
  - 行数核对：旧表 vs 新表
  - 枚举核对：如 `storeup.type` -> `action_type`
  - 抽样核对：`legacy_*_id` 是否能回查到旧记录
  - 订单域核对：`dianyingdingdan` / `wodedianying` 是否都有目标表和可回查的快照字段

### 7. Wrong vs Correct

#### Wrong

- 直接在旧表上做破坏性改名
- 不保留 legacy id
- 用一次性脚本写死迁移顺序，二次执行就失败
- 直接引用只存在于另一份 SQL 快照里的旧列，导致首轮脚本在真值源 schema 上执行失败
- 旧表字段本来允许为空，却把新表对应列直接收紧成 `NOT NULL` / 必须映射外键，导致脏数据环境里整批迁移失败
- PRD 已写明要覆盖订单域，结果脚本完全没给订单目标表和回填骨架

#### Correct

- 先保留旧表
- 新表带 `legacy_*_id`
- 回填用可重跑的 `INSERT ... ON DUPLICATE KEY UPDATE`
- 先完成数据库兼容迁移，再逐模块切换代码
- 先对照仓库真值源确认旧列真实存在，再决定是回填该列还是先保留为 NULL/快照
- 对旧表的可空字段和字符串快照字段，优先保留可空快照列；确实要非空时，在 SQL 中显式补默认值并在说明文档里写清语义
- 对 PRD 点名的每个核心域，至少补齐“目标表 + 回填路径 + 验证查询”

---

## Scenario: promote single-type movie schema to multi-type relation without breaking runtime

### 1. Scope / Trigger

- Trigger: 当前 runtime 仍按 `app_movie.movie_type_id` 单类型读取，但业务语义已经升级为“一部电影可对应多个类型”。
- Applies to:
  - `app_movie`
  - `app_movie_type`
  - `app_movie_type_rel`
  - `db/phase1_compatibility_normalization.sql`
  - 与电影统计口径相关的 schema 说明

### 2. Signatures

- Compatibility column:
  - `app_movie.movie_type_id`
- Final relation table:
  - `app_movie_type_rel(id, movie_id, type_id, is_primary, sort_order, created_at, updated_at)`
- Required constraints:
  - `UNIQUE (movie_id, type_id)`
  - `FK movie_id -> app_movie.id`
  - `FK type_id -> app_movie_type.id`
- Snapshot fields that must not be treated as truth:
  - `app_movie.like_count`
  - `app_movie.dislike_count`
  - `app_movie.click_count`
  - `app_movie.comment_count`
  - `app_movie.favorite_count`
  - `app_movie.total_score`
  - `app_movie.last_clicked_at`

### 3. Contracts

- Contract 1: 首轮不能直接删除或重命名 `app_movie.movie_type_id`；它必须保留为“兼容期主类型 / 默认类型引用”。
- Contract 2: 最终多类型表达固定落在 `app_movie_type_rel`，不能继续把 `movie_type_id` 描述成唯一类型真值。
- Contract 3: `app_movie_type_rel` 的首轮回填至少要为每条有 `movie_type_id` 的电影补一条 relation，且 `is_primary=1`、`sort_order=1`。
- Contract 4: relation 回填必须可重跑；当 `app_movie.movie_type_id` 变化时，重跑后不能残留多个 `is_primary=1` 的主类型关系。
- Contract 5: `app_movie` 中的点赞、点踩、收藏、评论、评分、点击相关字段只能标注为“汇总快照”，真值源必须在行为表或评论表。
- Contract 6: phase-1 如果没有稳定旧事件明细可回填，不要为了“看起来更规范”空加点击事件真值表；要明确写成后续扩展项。

### 4. Validation & Error Matrix

- 新增了 `app_movie_type_rel`，但 `movie_type_id` 注释仍写成唯一类型真值 -> 视为合同冲突
- relation 首轮回填只插入不校正主类型 -> 重跑后可能出现多个 `is_primary=1`，视为阻断问题
- 统计字段文档仍把 `app_movie.like_count/comment_count/...` 写成真值 -> 会误导 runtime 和 dashboard，视为口径错误
- 为点击真值补了空表，却没有旧数据来源、没有写入链路、没有说明 -> 视为 schema 漂移，不算闭环

### 5. Good / Base / Bad Cases

- Good:
  - 保留 `movie_type_id` 兼容列
  - 新增 `app_movie_type_rel`
  - 首轮 relation 可重跑回填且幂等校正主类型
  - `app_movie` 统计字段明确降级为快照
- Base:
  - 只新增 relation 表和一轮回填
  - 但没有补充主类型校正或快照真值说明
- Bad:
  - 直接删 `movie_type_id`
  - 或继续把 `movie_type_id` 当唯一类型真值
  - 或把主表统计快照继续当行为真值

### 6. Tests Required

- `git diff --check`
- 文本核对：
  - `app_movie_type_rel` DDL 存在
  - `movie_type_id` 注释明确为兼容期主类型
  - 统计字段注释明确为快照
- 回填核对：
  - relation 首轮回填存在
  - relation 主类型校正 SQL 存在
  - 文档与 SQL 对 `users -> app_admin_user_id`、`yonghu -> app_user_id` 的会话语义一致
- 有数据库副本时建议执行：
  - `SELECT COUNT(*) FROM app_movie_type_rel;`
  - `SELECT movie_id, COUNT(*) FROM app_movie_type_rel WHERE is_primary = 1 GROUP BY movie_id HAVING COUNT(*) > 1;`

### 7. Wrong vs Correct

#### Wrong

```text
1. 新增 app_movie_type_rel
2. 但 app_movie.movie_type_id 仍写成“唯一类型”
3. relation 回填只插入，不校正历史主类型
4. 重跑后同一电影可能出现多个主类型 relation
```

#### Correct

```text
1. 保留 app_movie.movie_type_id 作为兼容期主类型引用
2. 多类型统一落到 app_movie_type_rel
3. relation 首轮回填后再按当前 movie_type_id 校正 is_primary/sort_order
4. app_movie 统计字段全部标成汇总快照，不再冒充真值
```

---

## Scenario: validation database deployment for schema refactor

### 1. Scope / Trigger

- Trigger: 需要把兼容迁移脚本实际落到本机 MySQL 验证库，证明脚本可执行，而不是只交付 SQL 文件。
- Applies to: `ssmf7s0a.sql` 基础库导入、`db/phase1_*.sql` 迁移执行、验证库执行记录。

### 2. Signatures

- Runtime database: `ssmf7s0a`
- Preferred validation database: `ssmf7s0a_phase1`
- Runtime config source: `src/main/resources/application.yml`
- Baseline SQL source: `ssmf7s0a.sql`
- Migration SQL source: `db/phase1_compatibility_normalization.sql`
- Verification artifact: `.trellis/tasks/<task>/execution-results.md`

### 3. Contracts

- Contract 1: 不允许把验证执行直接落到运行库 `ssmf7s0a`。
- Contract 2: 执行前必须处理可执行 SQL 中的数据库引用，包括 `USE ssmf7s0a;`、`CREATE DATABASE` / `DROP DATABASE` 目标库、以及 `` `ssmf7s0a`.表名 `` 这类库名前缀，确保基础 dump 和 phase-1 脚本都指向同一个验证库。
- Contract 3: 基础 dump 中的 `DROP DATABASE` 只允许作用于验证库，不能保留原库名执行。
- Contract 4: PATH 上的 `mysql` 客户端版本不等于服务端可用性；旧 MySQL 5.x CLI 遇到 MySQL 8 `caching_sha2_password` 失败时，应改用新版 CLI 或项目已有 JDBC 驱动继续验证。
- Contract 5: 执行结果必须记录目标库、执行条数、验证查询结果和失败点。
- Contract 6: SQL 重写不能把普通注释或字符串字面量中的历史库名文本当成数据库引用改写；重写后必须检查仍指向运行库的可执行数据库引用并阻断执行。

### 4. Validation & Error Matrix

- Target DB equals `ssmf7s0a` -> 阻断执行
- 可执行 SQL 仍包含指向 `` `ssmf7s0a` `` 的 `USE` / `CREATE DATABASE` / `DROP DATABASE` / 库名前缀引用，且目标是验证库 -> 阻断执行
- MySQL CLI 报 `caching_sha2_password` -> 先判断为客户端过旧，不直接判定服务端不可用
- `schema_refactor_migration_log` 无记录 -> phase-1 未闭环
- phase-1 目标表数量不完整 -> 表创建未闭环

### 5. Good / Base / Bad Cases

- Good: 新建/重置 `ssmf7s0a_phase1`，导入基础 dump，再执行 phase-1，最后写入验证记录。
- Base: 生成可复现执行器和文档，即使本机 MySQL 不可用，也记录准确失败点。
- Bad: 直接运行带 `USE ssmf7s0a;` 的迁移脚本，误写当前运行库。

### 6. Tests Required

- `Test-NetConnection 127.0.0.1:3308` 或等价端口检查
- MySQL 认证连通性检查，优先使用当前可用的新客户端或 JDBC
- 验证库当前库名：`SELECT DATABASE()`
- 新表创建：从 `information_schema.tables` 统计 phase-1 目标表
- 迁移日志：查询 `schema_refactor_migration_log`
- 核心回填核对：`app_movie`、`app_user`、`app_user_movie_action`、`app_movie_order`、`app_auth_session`
- `git diff --check`

### 7. Wrong vs Correct

#### Wrong

```sql
USE `ssmf7s0a`;
SOURCE db/phase1_compatibility_normalization.sql;
```

#### Correct

```text
1. Rewrite baseline dump database name to `ssmf7s0a_phase1`
2. Import baseline schema/data into `ssmf7s0a_phase1`
3. Rewrite phase-1 `USE` target to `ssmf7s0a_phase1`
4. Execute migration and record verification queries
```

---

## Scenario: phase1 validation schema drift gap fill after `_bak` rename

### 1. Scope / Trigger

- Trigger: 验证库 `ssmf7s0a_phase1` 已执行过 phase-1、且旧表已被改名为
  `*_bak`，随后发现验证库结构落后于当前
  `db/phase1_compatibility_normalization.sql`。
- Applies to:
  - `app_admin_user`
  - `app_config`
  - `app_sensitive_word`
  - `app_auth_session.app_admin_user_id`
  - 与上述对象关联的索引、外键、回填和验证 helper

### 2. Signatures

- Drift inspector:
  - `.trellis/tasks/04-25-runtime-app-table-cutover/Phase1SchemaDriftInspector.java`
- Gap-fill SQL:
  - `db/phase1_validation_schema_gap_fill.sql`
- Gap-fill executor:
  - `.trellis/tasks/04-25-runtime-app-table-cutover/Phase1SchemaGapFillExecutor.java`
- Allowed target DB:
  - `ssmf7s0a_phase1`
- Required post-fix smoke:
  - `.trellis/tasks/04-25-runtime-app-table-cutover/legacy-table-bak-smoke.ps1`

### 3. Contracts

- Contract 1: 发现 phase1 漂移时，不能直接把整份
  `db/phase1_compatibility_normalization.sql` 对 `_bak` 状态的验证库重跑；
  必须改走单独的 gap-fill SQL/执行器。
- Contract 2: gap-fill executor 只允许目标库 `ssmf7s0a_phase1`，禁止写
  运行库 `ssmf7s0a`。
- Contract 3: gap-fill 必须兼容源表已改名为 `_bak` 的情况；源表解析规则固定为：
  - 优先 `<table>`
  - 缺失时回退 `<table>_bak`
- Contract 4: 当前 phase1 验证合同必须覆盖：
  - 表：`app_admin_user`、`app_config`、`app_sensitive_word`
  - 列：`app_auth_session.app_admin_user_id`
  - 索引：`idx_app_auth_session_app_admin_user_id`
  - 外键：`fk_app_auth_session_app_admin_user_id`
- Contract 5: gap-fill SQL 必须可重跑；回填一律使用
  `INSERT ... ON DUPLICATE KEY UPDATE` 或等价幂等写法。
- Contract 6: 补齐完成后，不能只以 DDL 成功作为通过依据；必须重新执行
  `_bak` 实烟，至少覆盖登录/会话、电影读链路、收藏和评论 compat。

### 4. Validation & Error Matrix

- 验证库缺 `app_admin_user` / `app_config` / `app_sensitive_word`
  -> 视为 phase1 漂移，不能声称 phase1 已完整落地
- `app_auth_session` 缺 `app_admin_user_id`
  -> 后台/前台登录链路都可能阻断，不能继续删表结论
- 只检查列存在，不检查索引/外键
  -> 视为验证合同不完整
- 直接重跑整份 phase1 SQL，而验证库源表已变成 `*_bak`
  -> 会因 `FROM users/config/sensitivewords/...` 等旧表名失效，执行方式无效
- gap-fill 写到了 `ssmf7s0a`
  -> 阻断级错误
- gap-fill 后未重跑 `_bak` smoke
  -> 只能算“结构补齐”，不能算“运行验证通过”

### 5. Good / Base / Bad Cases

- Good:
  - 先用 drift inspector 识别缺表/缺列/缺索引/缺外键
  - 再用只允许写 `ssmf7s0a_phase1` 的 gap-fill executor 补齐
  - 最后重跑 `_bak` smoke 并记录真实结果
- Base:
  - 仅补齐当前已确认缺口
  - 不顺手扩其他 schema
  - 结果明确写“schema 已补齐，但未证明可物理删表”
- Bad:
  - 拿 phase1 SQL 原样重跑 `_bak` 验证库
  - 只看 `phase1_table_count`，却漏检扩展表/索引/外键
  - 把 schema 补齐误写成“全仓旧表零依赖已证明”

### 6. Tests Required

- `git diff --check`
- `javac` 编译：
  - `Phase1SchemaDriftInspector.java`
  - `Phase1SchemaGapFillExecutor.java`
  - `Phase1ValidationDeploy.java`
- 漂移巡检断言：
  - `app_admin_user`
  - `app_config`
  - `app_sensitive_word`
  - `app_auth_session.app_admin_user_id`
  - `idx_app_auth_session_app_admin_user_id`
  - `fk_app_auth_session_app_admin_user_id`
- gap-fill 执行断言：
  - 当前库名为 `ssmf7s0a_phase1`
  - 回填源表解析结果正确打印为旧表或 `_bak`
- `_bak` 实烟断言至少包含：
  - `/users/login` + `/users/session`
  - `/yonghu/login` + `/yonghu/session`
  - `option/follow` 分类联动
  - `dianyingxinxi` compat 列表/详情
  - `appmovie/front/list` + `detail`
  - `storeup` / `discussdianyingxinxi` compat

### 7. Wrong vs Correct

#### Wrong

```text
1. 旧表已经改成 *_bak
2. 发现验证库缺 app_admin_user
3. 直接重跑 db/phase1_compatibility_normalization.sql
4. 因源表 users/config/sensitivewords 已不存在而失败，或误以为 phase1 已完整
```

#### Correct

```text
1. 先用 drift inspector 确认缺表/缺列/缺索引/缺外键
2. 用只允许写 ssmf7s0a_phase1 的 gap-fill executor 补齐
3. 源表优先解析旧表，缺失时回退 *_bak
4. 补齐后重跑 _bak smoke
5. 通过后只可得出“schema 缺口已清”，不能直接得出“可物理删表”
```

---

## Scenario: AI chat conversation and LLM fallback persistence

### 1. Scope / Trigger
- Trigger: any change to AI chat conversation/message persistence, user preference storage, feedback storage, or remote LLM fallback behavior.
- Applies to:
  - `app_ai_conversation`
  - `app_ai_message`
  - `app_user_preference`
  - `app_recommendation_feedback`
  - `AiChatController`
  - `AiChatServiceImpl`
  - `LlmClient`
- Reason: the chat feature is a cross-layer write path. The controller must authenticate the front user, the service must persist the same assistant text the user saw, and the client must preserve provider diagnostics without leaking secrets.

### 2. Signatures
- Conversation create:
  ```text
  POST /springbootdo4wek3z/ai-chat/conversation/create
  Body: {"title": string?, "isColdStart": int?}
  ```
- Stream send:
  ```text
  GET /springbootdo4wek3z/ai-chat/stream?conversationId=<Long>&message=<String>&token=<front-user-token>
  ```
- Preference save:
  ```text
  POST /springbootdo4wek3z/ai-chat/preference/save
  Body: {
    "favoriteGenres": string?,
    "favoriteYears": string?,
    "favoriteRegions": string?,
    "moodPreferences": string?,
    "watchFrequency": string?,
    "dislikedGenres": string?,
    "markColdStartCompleted": boolean|string?
  }
  ```
- Feedback save:
  ```text
  POST /springbootdo4wek3z/ai-chat/feedback
  Body: {
    "conversationId": Long,
    "messageId": Long,
    "movieId": Long,
    "feedbackType": string,
    "feedbackReason": string?
  }
  ```
- Provider API signatures:
  ```java
  Flux<String> LlmClient.streamChat(List<JSONObject> messages)
  Mono<String> LlmClient.chat(List<JSONObject> messages)
  ```

### 3. Contracts
- `GET /ai-chat/stream` must accept the front-user token via query parameter because browser `EventSource` cannot set custom headers.
- The token lookup must resolve to table name `yonghu`; any other table name must be rejected as unauthenticated for the chat flow.
- Chat messages are persisted in `app_ai_message` after the stream completes or the fallback answer is chosen.
- The assistant row must store the text actually delivered to the user, not a hidden provider response that was never sent.
- `app_ai_conversation` creation must store `isColdStart` and the current user id.
- Preference save must persist user taste fields plus the cold-start completion flag.
- Feedback save must bind to an existing conversation/message pair and should only accept assistant-message feedback for the movie that the message recommended.
- Remote provider configuration is driven by `.env` / environment variables:
  - `LLM_API_URL`
  - `LLM_API_KEY`
  - `LLM_MODEL`
  - `LLM_PROVIDER`
  - `LLM_ANTHROPIC_VERSION`
- `LLM_API_URL` is a base URL, not the full chat endpoint. The implementation appends `/chat/completions` for OpenAI-compatible providers and `/v1/messages` for Anthropic providers.
- Provider error bodies and request ids may be logged, but secrets must be sanitized.

### 4. Validation & Error Matrix
- Missing token -> `请先登录`, then `[DONE]`, no LLM call, no message persistence for user/assistant pair.
- Token exists but not a `yonghu` token -> same as unauthenticated.
- `conversationId` does not belong to current user -> reject by service boundary; do not persist assistant message.
- Remote stream returns HTTP 500 / `do_request_failed` before any chunk -> try remote non-stream, then local fallback.
- Remote stream completes empty -> try remote non-stream, then local fallback.
- Remote stream emits content and later errors -> do not append fallback text; persist the partial streamed answer only if that is what the client saw.
- Remote non-stream returns blank -> local fallback.
- Remote non-stream fails to connect / times out -> local fallback.
- Provider connection failure during startup or smoke is an environment issue if MySQL is unavailable; do not misclassify it as AI code failure.

### 5. Good / Base / Bad Cases
- Good:
  - User sends a chat message; the same assistant text that was streamed is saved into `app_ai_message`.
  - Stream fails before content; the non-stream provider call returns a full answer; that answer is delivered and saved.
- Base:
  - Provider fails on both stream and non-stream; local fallback is delivered and saved.
  - User preference is missing; the service creates a default preference row and continues.
- Bad:
  - Saving `fallbackAnswer` when the client only received `[DONE]` and no answer.
  - Persisting a remote answer that was not delivered because the stream was canceled.
  - Treating `do_request_failed` as proof that the app skipped the remote API.
  - Writing a full provider URL into `LLM_API_URL` and then appending `/chat/completions` again.

### 6. Tests Required
- DB/schema tests:
  - `app_ai_conversation`, `app_ai_message`, `app_user_preference`, `app_recommendation_feedback` exist after migration.
  - `conversation_id` / `message_id` foreign-key relationships are respected by the service layer.
- Service tests:
  - remote stream emits content -> no fallback appended.
  - remote stream empty/error before content -> non-stream remote is attempted.
  - remote non-stream blank/failure -> local fallback is used.
  - saved assistant content equals delivered content.
- Controller smoke tests:
  - unauthenticated stream returns `请先登录` and `[DONE]`.
  - authenticated stream can create/load/list conversation and persist messages.
- Environment tests:
  - `LLM_API_URL` base URL is combined with provider path correctly.
  - `.env` loading honors system environment precedence.

### 7. Wrong vs Correct
#### Wrong
```text
1. streamChat emits no nonblank chunks
2. service skips remote non-stream fallback
3. doFinally persists local fallback text
4. client only saw [DONE]
```

#### Correct
```text
1. streamChat emits no nonblank chunks
2. service tries LlmClient.chat(messages)
3. if remote chat fails or returns blank, use local fallback
4. persist exactly the text delivered to the client
```

---

## Scenario: AI chat response transport and provider diagnostics

### 1. Scope / Trigger
- Trigger: any change to provider logging, timeout handling, stream chunk parsing, or relay failure diagnosis for AI chat.
- Applies to `LlmClient` only.
- Reason: provider relays may return `do_request_failed`, SSL EOF, connection timeout, or malformed SSE frames even when the app code is correct. The client must distinguish those failures from local SSE framing bugs.

### 2. Signatures
- OpenAI-compatible stream:
  - `POST {LLM_API_URL}/chat/completions`
- OpenAI-compatible non-stream:
  - `POST {LLM_API_URL}/chat/completions`
- Anthropic stream/non-stream:
  - `POST {LLM_API_URL}/v1/messages`
- Retry policy:
  - transient network errors only; do not retry non-2xx provider responses indefinitely.

### 3. Contracts
- OpenAI-compatible stream parser accepts either raw JSON lines or `data:` SSE payloads.
- OpenAI-compatible stream parser must ignore `[DONE]` and reasoning-only deltas without exposing chain-of-thought text to users.
- Anthropic stream parser must accept `content_block_delta` + `text_delta` payloads.
- Non-stream parsing must be tolerant of blank body / missing content and return empty so the service can choose fallback.
- Provider error messages must sanitize Bearer tokens and API keys before logging.
- `buildRemoteErrorMessage(...)` should describe the request path and provider accurately enough for escalation.

### 4. Validation & Error Matrix
- `do_request_failed` from the relay -> record request id and status; try fallback, do not assume local parsing is broken.
- SSL EOF / handshake timeout / connect timeout -> treated as transient network failures for retry policy.
- Provider returns 4xx with a structured body -> surface sanitized body, do not retry endlessly.
- Parser sees malformed SSE chunk -> skip chunk, continue later chunks.

### 5. Good / Base / Bad Cases
- Good: stream parser returns only user-visible text chunks; reasoning-only chunks are filtered out.
- Base: provider is slow or flaky; client retries once for transient failures and then service fallback handles the remainder.
- Bad: logging the full API key, treating provider `500` as a local SSE bug, or failing to parse raw JSON stream lines.

### 6. Tests Required
- parser unit tests for:
  - raw JSON line
  - `data: {...}` line
  - `[DONE]`
  - reasoning-only chunk
  - Anthropic text delta
- error-path test for sanitized logging
- timeout/retry test for transient network classification

### 7. Wrong vs Correct
#### Wrong
```java
logger.error("provider failure: {}", responseBody); // raw body may include secrets
```

#### Correct
```java
logger.error("provider failure: {}", sanitizeResponseBody(responseBody));
```

### 1. Scope / Trigger

- Trigger: phase-1 规范化表已经落地，运行时代码开始把电影读路径从
  旧拼音表切到 `app_movie` / `app_movie_type` / `app_movie_media`。
- Applies to:
  - `/appmovie/*` 新只读接口
  - 前台首页 / 发现页 / 详情基础读取
  - 后台首页电影统计
  - “读新写旧”混合链路

### 2. Signatures

- New read controller base path: `/appmovie`
- Required endpoints:
  - `GET /appmovie/types`
  - `GET /appmovie/front/list`
  - `GET /appmovie/front/detail/{id}`
  - `GET /appmovie/admin/page`
  - `GET /appmovie/admin/dashboard/overview`
- Required source tables:
  - `app_movie`
  - `app_movie_type`
  - `app_movie_media`
- Required legacy bridge field for detail DTO:
  - `legacyDianyingxinxiId`

### 3. Contracts

- Contract 1: 新电影读接口必须直接读 `app_*` 表，不允许继续从
  `dianyingxinxi` / `dianyingleixing` 拼装新 DTO。
- Contract 2: 新 DTO 默认输出规范化英文字段，不再把
  `dianyingmingcheng`、`dianyingleixing` 这类拼音字段继续外溢为
  新接口契约。
- Contract 3: `GET /appmovie/front/detail/{id}` 中的 `id` 语义固定为
  `app_movie.id`，如果前端页面后续仍要调用旧评论/收藏/点赞/旧更新链路，
  则后端必须同时返回 `legacyDianyingxinxiId`。
- Contract 4: “读新写旧”页面中，旧接口的 `refid` / 旧表 `id`
  一律优先使用 `legacyDianyingxinxiId`，不能把 `app_movie.id`
  直接当旧表主键使用。
- Contract 5: 后台首页切到 `/appmovie/admin/dashboard/overview` 后，
  页面消费字段必须与真实 DTO 命名一致，例如：
  `movieTotal`、`totalFavoriteCount`、`topClickedMovies`、
  `topLikedMovies`、`topFavoritedMovies`、`typeDistribution`、
  `releaseYearDistribution`。
- Contract 6: 空库/空结果时，新接口必须返回可渲染的空结构：
  - 列表接口返回空 `PageUtils`
  - 详情接口允许返回 `null`
  - dashboard 返回 `0` 和空数组，而不是 500
- Contract 7: 在仍存在旧写路径或范围外旧模块时，不允许删除旧拼音表；
  旧表只能在“无运行时读写依赖”被逐项验证后再考虑下线。
- Contract 8: 兼容保留中的旧统计接口（如 `/dianyingxinxi/value*`、`/group`、
  `/count`）必须始终通过 compat service 从 `app_*` 真值聚合；不允许再保留
  “若本地 JSON 夹具存在则优先返回”的运行分支，否则后台统计会脱离真实数据。

### 4. Validation & Error Matrix

- 新详情 DTO 不返回 `legacyDianyingxinxiId`，但页面仍保留旧评论/收藏链路
  -> 阻断发布，真实数据下会把新表 id 错当旧表 id
- 后台首页继续按 `count/value` 旧字段名消费新 dashboard DTO
  -> 视为阻断问题，接口 200 但页面统计会错误归零
- 新 `/appmovie/front/list` 读空库返回 500
  -> 视为阻断问题，违反空结果兼容契约
- 旧 `dianyingxinxi` / `storeup` / `discussdianyingxinxi` /
  `yonghu` / `token` 任一运行路径仍在用旧表，但计划直接删旧表
  -> 阻断执行，不允许删表

### 5. Good / Base / Bad Cases

- Good:
  - 新读接口只读 `app_*`
  - 新 DTO 输出英文字段
  - 详情同时带 `id` 与 `legacyDianyingxinxiId`
  - 页面“读新写旧”时旧写路径明确回落到 legacy id
- Base:
  - 前台/后台读路径已切到新接口
  - 评论/收藏/点赞/登录态仍走旧接口
  - 旧拼音表继续保留
- Bad:
  - 新详情接口只返回 `app_movie.id`，前端继续把它传给旧评论/收藏接口
  - 后台首页换了新接口，但仍按旧 `count/value` 字段取数
  - 在 `storeup`、`discussdianyingxinxi`、`yonghu`、`token`
    仍未迁移前直接删除旧拼音表

### 6. Tests Required

- `git diff --check`
- 后端编译：
  - `mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" -DskipTests compile`
- 运行态 smoke:
  - `GET /appmovie/types` -> `200`
  - `GET /appmovie/front/list` -> `200`
  - `GET /appmovie/front/detail/{validAppMovieId}` -> `200`
  - `GET /appmovie/admin/dashboard/overview` -> 未登录时至少验证鉴权边界，
    带后台 token 时验证真实 DTO
- 至少一次真实数据闭环：
  - 先从 `/appmovie/front/list` 拿到真实 `app_movie.id`
  - 再请求 `/appmovie/front/detail/{id}`
  - 确认详情 DTO 中同时存在 `id` 与 `legacyDianyingxinxiId`
- 删表前必须逐项验证：
  - 不再有运行时 API / 页面 / mapper / service 读写旧拼音表
  - 不再有旧 `refid` / 旧表 `id` 写回链路
- compat 旧统计接口回归：
  - 即使工作目录出现同名 JSON 夹具文件，也必须继续走 compat service，
    不能绕过 `app_*` 真值聚合

### 7. Wrong vs Correct

#### Wrong

```text
1. /appmovie/front/detail/15 返回 id=15
2. 前端直接把 this.detail.id 传给 discussdianyingxinxi/storeup/dianyingxinxi/update
3. 旧链路把 15 当成旧 dianyingxinxi.id 使用
4. 真实数据下评论/收藏/点赞绑错电影
```

#### Correct

```text
1. /appmovie/front/detail/15 返回:
   id=15, legacyDianyingxinxiId=203
2. 前端详情页保留 id 作为新详情路由和相似电影跳转主键
3. 所有旧评论/收藏/点赞/旧更新链路统一使用 legacyDianyingxinxiId
4. 旧拼音表继续保留，直到旧写路径全部迁走后再评估删表
```

---

## Scenario: runtime movie write cutover onto `app_*` tables

### 1. Scope / Trigger

- Trigger: 电影详情页和“我的收藏”已经切到 `app_movie` 读模型，接下来要把
  电影域互动写路径从旧 `storeup` / `discussdianyingxinxi` 切到
  `app_user_movie_action` / `app_movie_comment` / `app_movie_comment_vote`。
- Applies to:
  - `/appmovie/actions/*`
  - `/appmovie/favorites/*`
  - `/appmovie/comments/*`
  - 前台电影详情页收藏/赞踩/评论/评论投票
  - 前台“我的收藏”面板

### 2. Signatures

- New write/read-write controller base path: `/appmovie`
- Required endpoints:
  - `GET /appmovie/actions/status`
  - `POST /appmovie/actions/toggle`
  - `GET /appmovie/favorites/page`
  - `POST /appmovie/favorites/cancel`
  - `GET /appmovie/comments/page`
  - `POST /appmovie/comments/add`
  - `POST /appmovie/comments/delete`
  - `POST /appmovie/comments/vote`
- Required source tables:
  - `app_user_movie_action`
  - `app_movie_comment`
  - `app_movie_comment_vote`
  - `app_user`

### 3. Contracts

- Contract 1: 电影域新增互动接口必须直接读写 `app_*` 表，不允许继续把
  `/storeup/*` 或 `/discussdianyingxinxi/*` 包一层后当成“新接口”。
- Contract 2: 前台详情页和“我的收藏”一旦切到 `/appmovie/*` 互动接口，
  就不允许再保留电影域旧 `storeup` / `discussdianyingxinxi` 请求作为
  实际写路径。
- Contract 3: 仍沿用旧登录态 / session 时，后端必须把 session 里的
  `legacy userId` 映射到 `app_user.legacy_yonghu_id`，而不是要求前端先切
  `token` / `yonghu` 新结构。
- Contract 4: `GET /appmovie/comments/page` 若标记为 `@IgnoreAuth`，
  但页面仍要回显“当前用户是否点过赞/踩、是否是本人评论”，则必须允许
  `legacyUserId` 作为显式参数兜底；不能假设拦截器一定会把 session 写入请求。
- Contract 5: 新评论如果还没有真实旧表 `legacy_discussdianyingxinxi_id`，
  评论投票表里的 `legacy_comment_id` 不能直接 fallback 到正数 `c.id`；
  必须使用与真实旧评论 id 不冲突的占位策略，例如负数 `-c.id`。
- Contract 6: “我的收藏”前端展示契约要么直接消费 `title`，要么显式兼容
  `title/name`；不能默认沿用旧 `storeup` DTO 的 `name` 字段。
- Contract 7: 新增评论时必须检查实际插入行数；`movieId` 无效或插入失败时，
  不允许继续依赖 `LAST_INSERT_ID()` 伪造成功结果。
- Contract 8: 在 `storeup-panel.vue` 跳转电影详情时，路由参数必须使用
  `app_movie.id`，不能再把收藏记录里的旧 `refid` 当详情页主键。
- Contract 9: 旧 `/storeup/*`、`/discussdianyingxinxi/*` 兼容桥接如果仍接收
  legacy `refid` / `id`，不允许把它们直接 fallback 成 `app_movie.id` /
  `app_movie_comment.id` 使用；映射缺失时必须显式失败。

### 4. Validation & Error Matrix

- 详情页名义上已切新接口，但仍调用旧 `storeup/*` / `discussdianyingxinxi/*`
  -> 视为阻断问题，旧拼音表仍被电影域运行时写路径依赖
- 评论分页接口是 `@IgnoreAuth`，却没有任何 `legacyUserId` 兜底
  -> 页面无法回显当前用户点赞/点踩/本人状态，视为阻断问题
- 新评论投票把 `legacy_comment_id = c.id`
  -> 与真实旧评论 id 存在冲突风险，视为数据映射缺陷
- 收藏面板改到新接口后仍按 `item.name` 渲染
  -> 页面标题/alt 为空，视为阻断 UI 回归
- 旧评论/收藏 compat bridge 在 legacy 映射缺失时偷偷改用 `app_*` 主键继续写
  -> 视为阻断问题，会把旧契约语义和新表主键揉在一起，后续无法稳定回查
- Maven targeted test `BUILD SUCCESS`，但项目配置固定 `skipTests=true`
  -> 不能当作真实单测执行证据，必须作为验证缺口单独记录

### 5. Good / Base / Bad Cases

- Good:
  - 电影详情页收藏/赞踩/评论都走 `/appmovie/*`
  - 收藏面板列表/取消收藏都走 `/appmovie/favorites/*`
  - 评论分页带 `legacyUserId` 兜底回显当前用户状态
  - 新评论投票使用不与旧评论冲突的 `legacy_comment_id` 占位
  - 旧 compat bridge 映射缺失时明确报错，不偷用新表主键兜底
- Base:
  - 后端已切新互动表
  - 前端已切电影域新接口
  - 用户 / token / admin 范围外模块仍保留旧实现
- Bad:
  - 只新增 Controller/VO 骨架，但前端仍保留旧接口
  - 只跑 `mvn -Dtest=... test`，忽略 `skipTests=true` 导致测试根本没执行
  - 收藏面板或详情页仍依赖旧 `name/refid` 语义
  - 旧 `refid` / 旧评论 `id` 映射不到新表时，直接把参数当 `app_*` 主键使用

### 6. Tests Required

- `git diff --check`
- 后端编译：
  - `mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" -DskipTests compile`
- 前端结构验证：
  - `node src/main/resources/front/front/scripts/movie-interaction-cutover-structure.test.js`
- 至少核对以下前端调用点已经切新：
  - `detail.vue` 不再出现电影域旧 `storeup/*`
  - `detail.vue` 不再出现电影域旧 `discussdianyingxinxi/*`
  - `storeup-panel.vue` 不再出现旧 `storeup/*`
- 如果本地运行进程是本轮新代码，补运行态 smoke：
  - `GET /appmovie/comments/page?movieId=<appMovieId>` -> `200`
  - `GET /appmovie/favorites/page` / `GET /appmovie/actions/status`
    -> 至少验证鉴权边界或带登录态验证真实返回
- 如果 targeted Maven test 显示 `BUILD SUCCESS`，还要额外确认：
  - `pom.xml` / surefire 没有把测试全局跳过
  - 或者明确记录“本轮无法证明单测真实执行”
- 对仍保留旧 URL 的 compat bridge，至少补一条负向测试：
  - 旧电影 `refid` 未映射时拒绝写评论/收藏
  - 旧评论 compat id 未映射时拒绝更新/删除

### 7. Wrong vs Correct

#### Wrong

```text
1. 新增了 AppMovieInteractionController 和 VO
2. detail.vue 仍调用 storeup/list、discussdianyingxinxi/add
3. Maven -Dtest=... 返回 BUILD SUCCESS
4. 团队误以为电影域写路径已经切完
```

#### Correct

```text
1. 详情页和收藏面板全部改调 /appmovie/*
2. 收藏/赞踩写 app_user_movie_action
3. 评论/评论投票写 app_movie_comment / app_movie_comment_vote
4. 若 surefire 全局 skipTests=true，明确把单测执行证明记为验证缺口
```

```text
1. 旧 /discussdianyingxinxi/add 收到 refid=15
2. 兼容层查不到 app_movie.legacy_dianyingxinxi_id=15
3. 代码把 15 当成 app_movie.id 继续写新评论
4. 旧契约和新主键语义混淆，后续评论回查/删除都不稳定
```

```text
1. 旧 /discussdianyingxinxi/add 收到 legacy refid
2. 兼容层只按 legacy 映射找 app_movie
3. 映射缺失时直接返回明确失败
4. 不允许把 legacy 参数偷改成 app_* 主键继续写
```

---

## Scenario: runtime session cutover onto `app_auth_session` / `app_user`

### 1. Scope / Trigger

- Trigger: 运行时代码开始把 `yonghu/users` 登录态与鉴权会话从旧 `token`
  表切到 `app_auth_session`，并把前台用户主体逐步映射到 `app_user`。
- Applies to:
  - `/yonghu/login`、`/yonghu/session`
  - `/users/login`、`/users/session`
  - `AuthorizationInterceptor`
  - `TokenServiceImpl`
  - `YonghuController` 的注册/保存/新增/修改/重置密码

### 2. Signatures

- Runtime session source table: `app_auth_session`
- Front-user subject table: `app_user`
- Legacy compatibility bridge:
  - `app_auth_session.legacy_subject_id`
  - `app_auth_session.subject_table_name`
  - `app_auth_session.subject_login_name`
  - `app_user.legacy_yonghu_id`
- Required compatibility endpoints:
  - `POST /yonghu/login`
  - `GET/POST /yonghu/session`
  - `POST /users/login`
  - `GET/POST /users/session`

### 3. Contracts

- Contract 1: 运行时登录态真值源切到 `app_auth_session` 后，
  `AuthorizationInterceptor` 仍必须返回兼容旧链路的 `TokenEntity`
  形状，不能要求现有 controller / page 改 token 解析逻辑。
- Contract 2: `TokenService.generateToken(...)` 不允许继续写旧 `token`
  表；新 token 只允许写 `app_auth_session`。
- Contract 3: 前台用户登录生成 session 前，必须保证
  `app_user.legacy_yonghu_id -> yonghu.id` 映射已存在；如果 phase-1
  回填不能覆盖所有运行时新增用户，则 `YonghuController` 登录/注册/保存/
  新增/修改/重置密码链路必须补 `app_user` upsert。
- Contract 4: `YonghuController` 中同时写 `yonghu` 与 `app_user`
  的入口必须放在同一事务内，避免新旧主体表出现部分成功。
- Contract 5: 当 `yonghu` / `users` 登录账号被修改后，必须同步更新
  `app_auth_session.subject_login_name`，否则拦截器回写到 session 的
  `username` 会变成过期快照。
- Contract 6: `app_auth_session` 更新现有会话时，只允许更新“该主体最新的一条”
  活跃记录，不能一次覆盖同主体多条历史记录。
- Contract 7: 旧 `token` / `yonghu` 表仍被范围外模块或兼容链路依赖时，
  不允许删表；删表前必须先验证没有运行时 API / mapper / service /
  前端页面再读写这些旧表。
- Contract 8: 若 Maven / surefire 全局配置仍把测试跳过，则 targeted test
  的 `BUILD SUCCESS` 不能当作真实单测执行证明，必须单独记录为验证缺口。
- Contract 9: `generateToken(...)` 为前台 `yonghu` 生成会话时，如果
  `app_user.legacy_yonghu_id` 映射缺失，必须显式失败；不允许写出
  `app_auth_session.app_user_id = NULL` 的脏会话。

### 4. Validation & Error Matrix

- `generateToken(...)` 仍插入旧 `token`
  -> 阻断发布，会话域并未真正切表
- 前台用户登录成功，但 `app_user_id` 仍为空且仓库要求映射前台主体
  -> 视为主体映射未闭环，后续新表用户行为无法稳定关联
- `yonghu` 修改账号后，`app_auth_session.subject_login_name` 未同步
  -> 拦截器注入的 `username` 与真实账号不一致，视为阻断兼容问题
- 前台 `yonghu` 登录成功，但 `app_user` 映射缺失时仍创建 session
  -> 视为阻断问题，会在新表里留下无法稳定关联用户主体的脏会话
- `updateActiveSession` 一次更新多行
  -> 会污染历史 token，会话主键语义不稳定，视为高风险数据缺陷
- 项目 `BUILD SUCCESS`，但 surefire 仍全局 `skipTests=true`
  -> 不能宣称单测通过，只能记为编译通过 + 测试验证缺口
- 仍存在 `token` 旧表运行时依赖，却计划直接删表
  -> 阻断执行，不允许删表

### 5. Good / Base / Bad Cases

- Good:
  - 登录写 `app_auth_session`
  - 拦截器从 `app_auth_session` 读兼容 `TokenEntity`
  - `yonghu` 与 `app_user` 双写事务化
  - 用户名变更后同步刷新会话快照
- Base:
  - 登录/鉴权已切新表
  - 范围外旧模块仍保留旧 `token` / `yonghu` 兼容结构
  - 旧表暂不删除
- Bad:
  - 只改 `getTokenEntity(...)` 读取新表，但 `generateToken(...)`
    仍写旧 `token`
  - `yonghu` 更新后只改旧表，不同步 `app_user`
  - targeted test 看起来成功，但实际上根本没执行

### 6. Tests Required

- `git diff --check`
- 后端编译：
  - `mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" -DskipTests compile`
- 至少一轮真实登录态 smoke：
  - `POST /yonghu/login` -> `200` + 返回 token
  - 带 token 访问 `/yonghu/session` -> `200`
  - `POST /users/login` -> `200` + 返回 token
  - 带 token 访问 `/users/session` -> `200`
- 数据库抽查：
  - `app_auth_session` 新增/更新了对应 session
  - 前台用户登录后 `app_auth_session.app_user_id` 能回指 `app_user.id`
- 如果 targeted Maven test 显示 `BUILD SUCCESS`，还要额外确认：
  - `pom.xml` / surefire 没有把测试全局跳过
  - 或者明确记录“本轮无法证明单测真实执行”

### 7. Wrong vs Correct

#### Wrong

```text
1. /yonghu/login 返回了 token
2. generateToken 实际还在写 token 旧表
3. 拦截器读取 app_auth_session 查不到新 token
4. /yonghu/session 真实运行报 401
```

#### Correct

```text
1. /yonghu/login 返回 token
2. 新 token 落到 app_auth_session
3. AuthorizationInterceptor 通过 getTokenEntity(token) 从新表回读兼容 TokenEntity
4. /yonghu/session 继续按旧契约返回用户信息
```

---

## Scenario: old-table runtime truth zeroing before table offlining

### 1. Scope / Trigger

- Trigger: 任务目标从“compatibility-first 可运行”升级为“准备废弃旧表”。
- Applies to:
  - 仍保留旧 URL 的 compat controller
  - `CommonController` 这类动态表名入口
  - MyBatis-Plus `@TableName` / 通用 CRUD fallback
  - mapper XML / service / controller / page 对旧表名的残留依赖

### 2. Signatures

- Runtime truth proof must cover at least:
  - Java annotations: `@TableName("<legacy_table>")`
  - SQL text in `src/main/resources/mapper/**/*.xml`
  - Dynamic table entrypoints:
    - `GET /option/{tableName}/{columnName}`
    - `GET /follow/{tableName}/{columnName}`
    - `/group/*`, `/value/*`, `/remind/*`, `/cal/*`
- Stable zero-dependency grep:
  ```powershell
  rg -n -S "FROM (dianyingxinxi|dianyingleixing|storeup|discussdianyingxinxi|yonghu|users|config|sensitivewords|token)\b|INSERT INTO (dianyingxinxi|dianyingleixing|storeup|discussdianyingxinxi|yonghu|users|config|sensitivewords|token)\b|UPDATE (dianyingxinxi|dianyingleixing|storeup|discussdianyingxinxi|yonghu|users|config|sensitivewords|token)\b|DELETE FROM (dianyingxinxi|dianyingleixing|storeup|discussdianyingxinxi|yonghu|users|config|sensitivewords|token)\b" src/main/resources/mapper src/main/java
  ```

### 3. Contracts

- Contract 1: “旧 URL 还存在” 不等于 “旧表仍是运行真值”；判断删表前提时，必须分开审查 URL、service、mapper、实体和动态通用入口。
- Contract 2: 如果页面仍调用旧表名语义的动态接口，例如 `option/dianyingleixing/dianyingleixing`，则 `CommonController` 不能把该请求直接透传为真实旧表查询，必须桥接到 `app_*` 新表。
- Contract 3: 如果 controller 已切到 compat service，但实体 `@TableName`、TokenEntity、通用 mapper XML 仍指向旧表，则旧表依赖未清零，不能声称可直接废弃。
- Contract 4: 对仍保留旧外部契约的 compat 场景，允许继续输出旧拼音字段，但底层必须映射到 `app_*` 真值；不得让通用 CRUD fallback 回落到旧表。
- Contract 5: 宣称“某张旧表可下线”前，至少要同时满足：
  - repo grep 不再命中该旧表的直接 SQL
  - 活跃 controller/service 不再把它当真值
  - 动态 tableName 入口不再能直通它
  - targeted compile/test 通过
- Contract 6: 真正执行删表前，推荐先做 `_bak` 改名验证；只有在应用真实启动和核心链路 smoke 通过后，才允许物理删除。
- Contract 7: `CommonController` / `CommonDao.xml` 这类动态 `${table}` 入口、以及前后台仍公开的旧 URL / 旧 `tableName` 语义，本身首先证明的是“源码层仍可达”或“兼容契约仍在”，不单独等于“旧表仍是已验证运行真值”；但在未补负向证明或显式封堵前，仍然阻断“可物理删表”结论。
- Contract 8: 对动态 `tableName` 黑名单判断，必须先做输入规范化后再判定，至少覆盖：
  - `trim`
  - `lowercase`
  - 统一错误文案中的表名输出
  否则 `Dianyingxinxi`、` dianyingxinxi ` 这类变体仍可能绕过旧表封堵。
- Contract 9: 对保留中的 compat 白名单分支，例如
  `option/follow -> dianyingleixing/dianyingleixing`，只允许放行**精确契约**；
  不允许把大小写变体、首尾空白变体也当成 compat 请求放行。兼容白名单要比旧表黑名单更严格。
- Contract 10: 不在当前 legacy 黑名单里的旧表，也不能仅凭“前后台菜单未挂载”就判定可删。
  只要满足以下任一条件，结论都必须至少降级为“条件删除”：
  - 仍被迁移设计/验证文档当作来源表或承接映射表引用
  - 仍可通过 `CommonController` 的非 legacy 动态 tableName 入口访问
  - 仍缺数据库侧的行数、最近更新时间、依赖对象或真实调用审计

### 4. Validation & Error Matrix

- controller 已切 compat，但 `@TableName("legacy")` 仍存在且可能参与通用 CRUD
  -> 视为条件下线，不可直接删表
- mapper XML 已不在主流程调用，但 repo grep 仍命中旧表 SQL
  -> 视为源码残留，至少要先清理或明确标注死代码
- `CommonController` 仍能通过 `tableName` 参数查旧表
  -> 视为真实运行阻断，不能删表
- 动态旧表黑名单未做输入规范化，导致大小写/空白变体可绕过
  -> 视为真实运行阻断，不能删表
- compat 白名单按规范化输入放行，导致旧表变体请求绕过黑名单
  -> 视为封口不完整，必须先改为“精确 compat + 宽拦截”
- 旧 URL / 旧菜单 / 动态 `tableName` 语义仍存在，但没有真实调用证据
  -> 不能直接写成“旧表仍是真值”；应写成“源码层仍可达，删表证据不足”
- targeted test 通过，但没有对动态入口 / compat 桥接做回归覆盖
  -> 只能算部分验证，不能直接给出“完全废弃旧表”结论
- 表没有命中专用 controller / mapper / 页面 grep，但仍在迁移 SQL、部署校验器或
  `CommonController` 非 legacy 动态入口中出现
  -> 不能写“可删”；只能写“暂无活跃专用代码引用，但仍缺删表级负向证明”

### 5. Good / Base / Bad Cases

- Good:
  - compat URL 继续保留
  - controller/service/mapper/entity/dynamic entry 全部改为新表真值
  - grep 不再命中旧表 SQL
  - 测试覆盖 compat 入口
- Base:
  - 主流程已切新表
  - 旧表 SQL 只剩明确未调用的残留文件
  - 旧 URL 或动态 tableName 只剩源码层可达，尚未补负向证明
  - 结论只能写“条件下线”
- Bad:
  - 仅凭页面能跑、controller 已切或 compile 通过，就宣称旧表可以删
  - 忽略 `CommonController` 动态表名入口
  - 忽略实体 `@TableName` 和通用 Token/CRUD fallback

### 6. Tests Required

- `git diff --check`
- 旧表 SQL grep 必须为空，或对每一处残留给出死代码证明
- 对未命中专用运行代码的旧表，也要额外核对：
  - 是否出现在迁移 SQL / 迁移说明 / 验证工具中
  - 是否仍能通过 `CommonController` 非 legacy 动态入口访问
  - 数据库侧是否还有行数、最近更新时间或依赖对象
- 至少一轮 targeted compile/test：
  - movie/type compat
  - storeup/discuss compat
  - yonghu/token compat
  - dynamic option/follow compat
- dynamic option/follow compat 回归必须同时覆盖：
  - 精确 compat 请求仍放行
  - 大小写变体旧表名被拦截
  - 首尾空白变体旧表名被拦截
  - 非旧表请求仍透传到 `commonService`
- 对本轮准备继续下线的 legacy 表，至少补一轮代表性封堵回归：
  - `group`
  - `option/follow`
  - `cal/value/valueDay`
  - `remind/sh`
  证明新增黑名单不是只在单一路径生效
- 删表前 smoke:
  - 旧页面主要入口可用
  - 登录 / 会话 / 详情 / 评论 / 收藏 / 分类联动可用
- 推荐执行：
  - 先把旧表改名 `_bak`
  - 启动应用并做核心链路冒烟
  - 通过后再删表

### 7. Wrong vs Correct

#### Wrong

```text
1. /dianyingxinxi/list 能返回数据
2. controller 也已经走 compat service
3. 就直接判断 dianyingxinxi / dianyingleixing / token 等旧表都能删
4. 实际 CommonController 和通用 CRUD 仍可能回落旧表
```

#### Correct

```text
1. 逐项检查 controller / service / mapper / entity / dynamic entry
2. 清掉 repo 中对旧表的直接 SQL 和 @TableName 依赖
3. 跑 targeted compile/test 和动态入口回归
4. 先改名 _bak 做实跑验证
5. 验证通过后再删旧表
```

#### Wrong

```text
1. 旧表黑名单先把 tableName 做 trim/lowercase
2. compat 白名单也按同一规则判断 dianyingleixing/dianyingleixing
3. /option/DianyingLeixing/dianyingleixing 被当成 compat 放行
4. 黑名单没有真正兜住所有旧表变体输入
```

#### Correct

```text
1. 旧表黑名单对外部输入做 trim/lowercase 后统一拦截
2. compat 白名单只接受精确契约字符串
3. 大小写/空白变体不视为 compat，请求回落到黑名单
4. 结果是“白名单更窄，黑名单更宽”
```

---

## Scenario: promote phase1 runtime database to business-named runtime schema

### 1. Scope / Trigger

- Trigger: `ssmf7s0a_phase1` 已不再只是验证库，而是当前真实运行库，且需要把
  库名切换为更贴近业务系统的名字。
- Applies to:
  - `src/main/resources/application.yml`
  - 仍会继续复用的 JDBC helper / smoke helper
  - 本机 MySQL 运行库克隆与切换验证

### 2. Signatures

- Legacy runtime DB:
  - `ssmf7s0a`
- Promoted source runtime DB:
  - `ssmf7s0a_phase1`
- Current business runtime DB:
  - `movie_recommend_system_cqupt`
- Runtime datasource:
  - `jdbc:mysql://127.0.0.1:3308/movie_recommend_system_cqupt?...`
- Runtime clone helper:
  - `.trellis/tasks/04-25-runtime-app-table-cutover/RuntimeDatabaseCloneHelper.java`

### 3. Contracts

- Contract 1: 当 `ssmf7s0a_phase1` 已承接真实 runtime 后，后续 helper /
  文档不能继续把它表述为“默认验证库”。
- Contract 2: 库名切换优先使用“新建业务库并克隆当前 runtime 数据”的方式，
  不直接删旧库；旧库至少保留到新库 smoke 通过。
- Contract 3: `application.yml`、`target/classes/application.yml` 与仍会继续
  复用的 helper 目标库常量必须同步到同一个业务库名。
- Contract 4: clone helper 默认只允许：
  - 从当前业务 runtime 库读取
  - 克隆到显式的非运行态目标库
  - 拒绝读写历史旧库 `ssmf7s0a`
- Contract 5: 历史 phase1 结果文档可以保留，但不能再作为“当前 runtime 库名”
  的唯一证明；需要有新库的 inventory / sample / smoke 证据。

### 4. Validation & Error Matrix

- `application.yml` 已切新库，但 helper 仍写死 `ssmf7s0a_phase1`
  -> 视为未闭环；后续 task 仍可能误写旧命名库
- 新业务库不存在
  -> 不允许声称 runtime 已切库
- 新业务库表数或核心表行数少于 source runtime
  -> 视为克隆失败
- 直接把 clone helper 默认 source 仍保留成 `ssmf7s0a_phase1`
  -> 视为语义漂移；helper 默认行为与当前 runtime 状态不一致
- 只改源码配置，不做至少一轮 inventory 或接口 smoke
  -> 证据不足，不能声称切换完成

### 5. Good / Base / Bad Cases

- Good:
  - 新建 `movie_recommend_system_cqupt`
  - 从当前 runtime 库完整克隆
  - datasource 与活跃 helper 全部切到新库
  - 有 inventory + 核心接口 smoke 证据
- Base:
  - 仅完成 datasource 切换和数据库克隆
  - 但仍缺少应用级 smoke 或 helper 文档同步
- Bad:
  - 只改 `application.yml`
  - 或继续把 `ssmf7s0a_phase1` 当默认 runtime/validation 混用
  - 或直接删除旧库后再验证

### 6. Tests Required

- `git diff --check`
- helper 编译：
  - `javac --release 8 -encoding UTF-8`
- 新库 inventory：
  - schema 存在
  - base table 数量对齐
  - `app_movie` / `app_user` / `app_auth_session` / `app_config`
    行数存在且与 source runtime 对齐
- 配置核对：
  - `src/main/resources/application.yml`
  - `target/classes/application.yml`
- 至少一轮应用 smoke：
  - `GET /appmovie/front/list`
  - 推荐追加 `GET /appmovie/front/detail/{id}`

### 7. Wrong vs Correct

#### Wrong

```text
1. phase1 跑通后直接把 application.yml 改到新库名
2. helper 仍默认指向 ssmf7s0a_phase1
3. 没有新库 inventory 和接口 smoke
4. 后续 task 很容易继续把 phase1 当默认运行库
```

#### Correct

```text
1. 先确认当前真实 runtime 来源库
2. 克隆到业务命名库 movie_recommend_system_cqupt
3. 同步 datasource 与活跃 helper 常量
4. 跑 inventory、核心表样本和接口 smoke
5. 保留旧库直到新库验证通过
```

---

## Scenario: runtime-safe incremental schema cutover on the active database

### 1. Scope / Trigger

- Trigger: 需要把单个 phase-1 增量结构直接落到当前运行库，而不是先落验证库。
- Applies to:
  - `movie_recommend_system_cqupt`
  - `db/runtime_*.sql`
  - `.trellis/tasks/**/Runtime*Helper.java`
  - 任何会直接写运行库的受控增量 helper

### 2. Signatures

- Runtime target database: `movie_recommend_system_cqupt`
- Allowed endpoint: `127.0.0.1:3308` / `localhost:3308`
- Preferred command shape:
  - `... Runtime*Helper <host> <port> <user> <password> <db> verify`
  - `... Runtime*Helper <host> <port> <user> <password> <db> apply APPLY_CONFIRMED`
- Preferred result artifact:
  - `.trellis/tasks/<task>/runtime-*-results.md`

### 3. Contracts

- Contract 1: 运行库直写必须使用“运行库专用增量 SQL + 受控 helper”组合，不能把整份 `db/phase1_compatibility_normalization.sql` 直接打到运行库。
- Contract 2: 运行库增量 SQL 不允许包含 `USE <db>`，目标库必须由 helper 或操作者显式传参绑定。
- Contract 3: 运行库增量 SQL 不允许读取旧拼音表；只允许触达本次目标新表及其直接依赖的新表。
- Contract 4: helper 必须限制 host、port 和 target database，并显式拒绝 `ssmf7s0a`、`ssmf7s0a_phase1` 这类非目标库。
- Contract 5: 运行流程固定为 `compile/self-test -> verify -> apply -> verify`，缺任何一步都不算闭环。
- Contract 6: `apply` 必须额外要求确认 token，避免误执行。
- Contract 7: 结果必须落盘到任务目录，至少记录落库前状态、apply 输出、落库后 verify 输出和最终结论。

### 4. Validation & Error Matrix

- 直接对运行库执行 phase-1 全量脚本 -> 阻断
- 运行库增量 SQL 包含 `USE` 或旧表读取 -> 阻断
- helper 不限制目标库/端口 -> 视为高风险实现，不允许执行
- `verify` 前未编译或未跑 self-test -> 不允许执行 `apply`
- `apply` 后 `primary_conflict_movies != 0` 或 `missing_primary_relations != 0` -> 视为落库失败

### 5. Good / Base / Bad Cases

- Good:
  - 新增运行库专用 SQL
  - helper 只允许当前运行库
  - `verify -> apply -> verify` 全流程留痕
- Base:
  - 已有运行库专用 SQL
  - 但没有 helper guardrail 或没有结果文档
- Bad:
  - 直接手工把 phase-1 全量脚本跑到运行库
  - 或让 helper 同时允许 runtime / validation 多个数据库

### 6. Tests Required

- helper 编译通过：
  - `javac --release 8 -encoding UTF-8`
- helper 自检通过：
  - `Runtime*HelperSelfTest`
- 落库前 verify：
  - `SELECT DATABASE()` 等价输出正确
- 落库后 verify：
  - 目标表存在
  - 行数非负且符合预期
  - `primary_conflict_movies=0`
  - `duplicate_movie_type_pairs=0`
  - `missing_primary_relations=0`
- 结果文档检查：
  - 命令、输出、结论三者一致

### 7. Wrong vs Correct

#### Wrong

```text
1. 直接把 phase1_compatibility_normalization.sql 跑到 movie_recommend_system_cqupt
2. SQL 自带 USE，helper 也不限制目标库
3. apply 前不做 verify/self-test
4. 执行后没有结果文档，只靠口头说明
```

#### Correct

```text
1. 为运行库单独准备最小增量 SQL
2. helper 显式限制 host/port/db，并拒绝 validation/legacy 库
3. 先 compile/self-test，再 verify
4. apply 使用确认 token
5. apply 后再次 verify，并把全过程写入任务结果文档
```

---

## Scenario: normalize movie director/cast snapshots into person relations

### 1. Scope / Trigger

- Trigger: `app_movie.director_name` / `app_movie.cast_names` 已经承接运行时真值，但仍是字符串快照，需要进一步规范成人物维表和关系表。
- Applies to:
  - `app_movie`
  - `app_movie_person`
  - `app_movie_person_rel`
  - `db/runtime_movie_person_rel_cutover.sql`
  - 相关 runtime cutover helper

### 2. Signatures

- Person dimension:
  - `app_movie_person(id, person_name, normalized_name, source_note, created_at, updated_at)`
- Person relation:
  - `app_movie_person_rel(id, movie_id, person_id, relation_type, sort_order, person_name_snapshot, created_at, updated_at)`
- Allowed relation types:
  - `director`
  - `cast`
- Required uniqueness:
  - `app_movie_person.normalized_name`
  - `app_movie_person_rel(movie_id, relation_type, person_id)`

### 3. Contracts

- Contract 1: 当前旧库没有独立的人物主表，因此 `app_movie_person` 不允许伪造 `legacy_*_id`；幂等键固定使用 `normalized_name`。
- Contract 2: `app_movie.director_name` / `cast_names` 在过渡期仍保留为快照字段；人物真值关系新增到 `app_movie_person_rel`，而不是直接删除快照列。
- Contract 3: `app_movie_person_rel.relation_type` 当前只允许 `director` 和 `cast`，helper verify 必须对其他值报错。
- Contract 4: 回填必须可重跑，并且要同时处理：
  - 新增关系写入
  - 当前快照已不存在的陈旧 relation 清理
  - 无 relation 引用的孤儿 `app_movie_person` 清理
- Contract 5: 人名拆分规则必须显式列出并在 SQL 中统一处理；当前允许的稳定分隔符为：
  - `,`
  - `，`
  - `、`
  - `;`
  - `；`
  - `/`
  - `|`
- Contract 6: 运行库 verify 通过口径至少包括：
  - `duplicate_movie_person_pairs=0`
  - `missing_director_relations=0`
  - `missing_cast_relations=0`
  - `unexpected_relation_types=0`
- Contract 7: `region_name` 若仍是单值快照且没有多值失真证据，本轮不要顺手扩成地区维表，避免范围漂移。

### 4. Validation & Error Matrix

- 人物维表试图补 `legacy_*_id`，但旧库并无稳定 person 主键 -> 设计错误
- `relation_type` 出现 `director/cast` 之外的值 -> 阻断
- 只补写入、不清理陈旧 relation -> 重跑后关系会偏脏，视为不闭环
- verify 打印了 `missing_*_relations`，但非零不失败 -> guardrail 不完整，不能申请运行库写入
- 新增人物关系时顺带扩地区维表，但没有明确多值关系需求 -> 视为 scope drift

### 5. Good / Base / Bad Cases

- Good:
  - 人名按 `normalized_name` 幂等去重
  - 电影-人物关系拆到 `app_movie_person_rel`
  - 快照保留，关系真值新增
  - 回填、清理、verify 口径都完整
- Base:
  - 只补了人物表和关系表
  - 但没有清理陈旧 relation 或没有 verify 失败口径
- Bad:
  - 继续只依赖 `director_name/cast_names` 字符串
  - 或为不存在的 legacy person 主表硬造 `legacy_person_id`

### 6. Tests Required

- `git diff --check`
- helper 编译通过：
  - `javac --release 8 -encoding UTF-8`
- helper 自检通过：
  - `RuntimeMoviePersonRelCutoverHelperSelfTest`
- 文本检查：
  - runtime SQL 不含 `USE`
  - runtime SQL 不含旧拼音表读取
  - verify 文档写清通过口径
- 运行库 apply 前后建议执行：
  - `verify`
  - `SELECT relation_type, COUNT(*) FROM app_movie_person_rel GROUP BY relation_type`

### 7. Wrong vs Correct

#### Wrong

```text
1. 新增人物关系表
2. 但仍把回填完整性交给人工肉眼判断
3. helper 不会因 missing_director_relations/missing_cast_relations 非零而失败
4. 结果是“看起来跑完了”，实际关系可能没补全
```

#### Correct

```text
1. 保留 director_name/cast_names 作为快照
2. 新增 app_movie_person 与 app_movie_person_rel
3. 运行库 helper 对缺失关系、重复关系、异常 relation_type 全部阻断
4. apply 前后都跑 verify，并以 0 缺失为通过口径
```

---

## Scenario: personalized home recommendation on `appmovie` read chain

### 1. Scope / Trigger

- Trigger: 首页 `Recommended for You` 需要从假排行榜切到真正的协同过滤推荐，同时接口仍保持匿名可访问。
- Applies to:
  - `GET /appmovie/front/recommended`
  - `AppMovieController`
  - `AppMovieService`
  - `app_user_movie_action`
  - 首页 `home-landing-panel.vue` 的 `recommended` 分区

### 2. Signatures

- API signature:
  - `GET /appmovie/front/recommended?page=1&limit=6&sort=totalScore&order=desc`
- Optional request header:
  - `Token`
- Training data source:
  - `app_user_movie_action(legacy_userid, movie_id, action_type)`
- Positive action types:
  - `favorite`
  - `like`

### 3. Contracts

- Contract 1: `/appmovie/front/recommended` 必须直接读 `app_*` 新表链路；不允许把首页再回退到旧 `/dianyingxinxi/autoSort2`。
- Contract 2: 接口可以保留 `@IgnoreAuth`，但只要需要登录态个性化，就必须自行解析请求头 `Token` 并通过 `TokenService` 取 `legacy user id`；不能依赖 `request.getSession().getAttribute("userId")`。
- Contract 3: 协同过滤训练样本固定优先使用 `app_user_movie_action` 的正向行为，并按 `(legacy_userid, movie_id)` 去重；同一用户同一电影存在多条正向行为时，保留更强偏好分值。
- Contract 4: 首页推荐 fallback 固定为稳定榜单，不允许返回空白：
  - 未登录 -> fallback
  - token 无效 -> fallback
  - 当前用户无正向行为 -> fallback
  - CF 结果为空 -> fallback
- Contract 5: fallback 的默认排序固定为高分优先（`m.total_score desc`），并且推荐结果回填到 DTO 时必须保持推荐 id 顺序，不允许被 SQL `IN` 查询的自然顺序打乱。
- Contract 6: 本接口只服务首页 `recommended` 分区；`Top Rated / Most Discussed / Latest Release / Most Favorited` 及详情页 `similarMovies` 语义不受影响。

### 4. Validation & Error Matrix

- `@IgnoreAuth` 接口直接读 session userId -> 视为合同违背；前端带 `Token` 时会静默丢失个性化身份
- 正向样本继续从旧 `storeup` / `dianyingxinxi` 读 -> 视为切表回退
- 当前用户没有样本且接口返回空列表 -> 视为首页阻断
- CF 命中电影顺序被 `IN (...)` 查询打乱 -> 视为推荐结果错序
- 把首页 `recommended` 的改动外溢到 `similarMovies` 或其他榜单 -> 视为 scope drift

### 5. Good / Base / Bad Cases

- Good:
  - `Token` 头可选
  - 登录用户走 `app_user_movie_action` CF
  - 无法个性化时稳定回退到高分榜
  - 前端只把 `recommended` 分区切到新接口
- Base:
  - 新接口已落地
  - 但只做了登录用户 CF，没有补齐空结果 fallback
- Bad:
  - 继续按 `totalScore` 冒充“推荐”
  - 或匿名接口错误依赖 session userId
  - 或把详情页 `similarMovies` 一并改成同一推荐逻辑

### 6. Tests Required

- `git diff --check`
- 后端定向测试至少覆盖：
  - 无 token -> fallback
  - 有 token 但当前用户无正向行为 -> fallback
  - 有正向行为 -> 返回 CF 推荐
  - `Token` 头能被 controller 正确解析成 legacy user id
- 前端定向 lint：
  - `home-landing-panel.vue`
- 前端回归点：
  - `recommended` 分区调 `/appmovie/front/recommended`
  - 其他分区继续调 `/appmovie/front/list`

### 7. Wrong vs Correct

#### Wrong

```text
1. 新接口保持 @IgnoreAuth
2. controller 继续读 session.userId
3. 前端虽然带了 Token，但推荐链路拿不到登录用户
4. 首页推荐静默退化成匿名高分榜
```

#### Correct

```text
1. 新接口保持 @IgnoreAuth
2. controller 主动解析请求头 Token
3. service 用 app_user_movie_action 正向行为做 CF
4. 命不中个性化条件时明确回退到高分榜
5. 前端只替换首页 recommended 分区的数据源
```

---

## Query Patterns

<!-- How should queries be written? Batch operations? -->

(To be filled by the team)

---

## Migrations

<!-- How to create and run migrations -->

(To be filled by the team)

---

## Naming Conventions

<!-- Table names, column names, index names -->

(To be filled by the team)

---

## Common Mistakes

<!-- Database-related mistakes your team has made -->

(To be filled by the team)
