# Quality Guidelines

> Code quality standards for backend development.

---

## Overview

<!--
Document your project's quality standards here.

Questions to answer:
- What patterns are forbidden?
- What linting rules do you enforce?
- What are your testing requirements?
- What code review standards apply?
-->

(To be filled by the team)

---

## Forbidden Patterns

<!-- Patterns that should never be used and why -->

(To be filled by the team)

---

## Required Patterns

<!-- Patterns that must always be used -->

(To be filled by the team)

---

## Testing Requirements

<!-- What level of testing is expected -->

### Scenario: Maven Targeted Test Execution Contract

#### 1. Scope / Trigger
- Scope: Backend Maven targeted test runs, especially commands that use
  `-Dtest=<ClassName>` or `-Dtest=<ClassName>#<methodName>`.
- Trigger: Any change to backend test execution, Maven Surefire configuration,
  or verification evidence for a targeted regression test.
- Goal: A targeted test is only considered executed when Maven Surefire reports
  a real test summary. Build success alone is not enough.

#### 2. Signatures
- Stable targeted-test command on the current Windows machine:
  ```powershell
  $env:JAVA_TOOL_OPTIONS='-Xms128m -Xmx512m -XX:ParallelGCThreads=1'
  mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" -DforkCount=0 -DskipTests=false -Dtest=<TestClassName> test
  ```
- Optional method-level form:
  ```powershell
  $env:JAVA_TOOL_OPTIONS='-Xms128m -Xmx512m -XX:ParallelGCThreads=1'
  mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" -DforkCount=0 -DskipTests=false -Dtest=<TestClassName>#<methodName> test
  ```
- `pom.xml` Surefire configuration must not hard-code:
  ```xml
  <skipTests>true</skipTests>
  ```
- If Surefire skip behavior is configured in `pom.xml`, the default contract is
  effectively false and command-line override remains available:
  ```xml
  <skipTests>${skipTests}</skipTests>
  ```

#### 3. Contracts
- `skipTests` default: false. Tests should run by default.
- `-DskipTests=<true|false>` remains the command-line override. Use
  `-DskipTests=false` in targeted verification commands to make intent explicit.
- `pom.xml` must never force `skipTests=true`, because that makes a targeted test
  command look successful while executing zero tests.
- Real execution evidence must include the Surefire summary line, for example:
  `Tests run: <n>, Failures: <n>, Errors: <n>, Skipped: <n>`.
- On the current machine, the default Maven/Surefire fork mode can fail during
  heap allocation. The stable contract is:
  `JAVA_TOOL_OPTIONS='-Xms128m -Xmx512m -XX:ParallelGCThreads=1'` plus
  `-DforkCount=0`.

#### 4. Validation & Error Matrix
- `pom.xml` contains `<skipTests>true</skipTests>` -> invalid; targeted tests can
  be silently skipped.
- Command omits `-DskipTests=false` while debugging skip behavior -> ambiguous;
  add the flag so the verification intent is explicit.
- Maven exits successfully but no Surefire `Tests run:` summary appears ->
  invalid evidence; do not claim the test executed.
- Maven output says tests are skipped -> invalid for targeted regression
  verification unless the task explicitly asked to skip tests.
- Maven fails with heap allocation or forked JVM startup errors -> retry with the
  stable command: constrained `JAVA_TOOL_OPTIONS` and `-DforkCount=0`.
- Surefire summary appears with `Failures > 0` or `Errors > 0` -> real execution
  happened, but verification failed.

#### 5. Good/Base/Bad Cases
- Good: `pom.xml` does not force `skipTests=true`; command uses
  `-DskipTests=false`, constrained `JAVA_TOOL_OPTIONS`, `-DforkCount=0`, and the
  log includes `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.
- Base: Full `mvn test` runs with the same default `skipTests=false` behavior and
  produces a Surefire summary for the relevant test suite.
- Bad: `pom.xml` hard-codes `<skipTests>true</skipTests>` and `mvn ... -Dtest=...`
  reports build success without any Surefire `Tests run:` summary.

#### 6. Tests Required
- Configuration assertion: inspect `pom.xml` and confirm Surefire no longer
  hard-codes `<skipTests>true</skipTests>`.
- Targeted execution assertion: run the targeted Maven command with
  `-DskipTests=false`, constrained `JAVA_TOOL_OPTIONS`, and `-DforkCount=0`.
- Evidence assertion: capture the Surefire `Tests run:` summary in the final
  verification note. Without that line, the check is not accepted as executed.
- Override assertion: when intentionally validating skip behavior, confirm
  `-DskipTests=true` skips tests and `-DskipTests=false` runs them.

#### 7. Wrong vs Correct
##### Wrong
```xml
<skipTests>true</skipTests>
```

```powershell
mvn.cmd -Dtest=YonghuControllerRegisterCompatibilityTest test
```

This can return build success while running no tests, and default fork settings
may fail with heap allocation errors on the current machine.

##### Correct
```xml
<skipTests>${skipTests}</skipTests>
```

```powershell
$env:JAVA_TOOL_OPTIONS='-Xms128m -Xmx512m -XX:ParallelGCThreads=1'
mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" -DforkCount=0 -DskipTests=false -Dtest=YonghuControllerRegisterCompatibilityTest test
```

Accept the result only after the log includes a Surefire `Tests run:` summary.

---

### Scenario: Backend Targeted Test In Resource-Heavy Repo

#### 1. Scope / Trigger
- Scope: 只验证后端 Java 逻辑，但项目 `src/main/resources/**` 下同时挂着
  大量前端资源或 `node_modules`。
- Trigger: `maven-resources-plugin` 在 targeted test / compile 阶段复制
  `src/main/resources/**` 失败，而失败点与当前后端 Java 改动无关。
- Goal: 区分“默认全量资源复制失败”和“后端 Java 逻辑验证失败”，避免把
  资源目录噪音误判成本轮 Java 回归。

#### 2. Signatures
- 当前仓库稳定的后端 targeted test 绕行命令：
  ```powershell
  $env:JAVA_TOOL_OPTIONS='-Xms128m -Xmx512m -XX:ParallelGCThreads=1'
  mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" "-Dmaven.resources.skip=true" -DforkCount=0 -DskipTests=false -Dtest=<TestClassName> test
  ```
- 典型失败信号：
  ```text
  Failed to copy full contents from src/main/resources/.../node_modules/...
  ```

#### 3. Contracts
- `-Dmaven.resources.skip=true` 只用于“当前轮次目标是验证后端 Java 改动”
  的 targeted compile/test。
- 使用该绕行命令时，最终结论必须明确写成：
  - “Java 编译 / targeted test 通过”
  - “默认资源复制链路仍未恢复”
- 不能把 `-Dmaven.resources.skip=true` 下的成功，冒充成默认 `mvn test`
  或默认 `mvn package` 全绿。

#### 4. Validation & Error Matrix
- 默认 `mvn test` 在 `maven-resources-plugin` 复制前端资源时报错
  -> 先记录为资源链路问题，不直接判定后端 Java 逻辑回归
- `-Dmaven.resources.skip=true` 下 targeted test 通过
  -> 可以证明当前后端 Java 改动通过，但仍要单列默认资源链路风险
- `-Dmaven.resources.skip=true` 下 targeted test 仍失败
  -> 才能把问题归因到当前后端 Java 逻辑或测试本身

#### 5. Good / Base / Bad Cases
- Good:
  - 默认资源复制失败被单独记录
  - 使用 `-Dmaven.resources.skip=true` 跑 targeted test
  - 最终说明同时包含“Java 验证结论”和“资源链路残余风险”
- Base:
  - 只证明当前后端 Java 变更通过
  - 不声称默认 Maven 全链路已恢复
- Bad:
  - 资源复制失败后直接宣称“测试跑不通”
  - 或者在 `-Dmaven.resources.skip=true` 通过后宣称“默认构建已恢复”

#### 6. Tests Required
- 先尝试默认 targeted Maven 命令，确认失败点是否在 `maven-resources-plugin`
- 再用 `-Dmaven.resources.skip=true` 重跑相同 targeted test
- 最终记录两类证据：
  - 默认资源阶段失败摘要
  - 绕行后 `Tests run: <n>, Failures: 0, Errors: 0, Skipped: 0`

#### 7. Wrong vs Correct
##### Wrong
```powershell
mvn.cmd "-Dtest=StoreupControllerCompatibilityTest" test
```

资源复制失败后，直接得出“当前后端迁移未验证”的结论。

##### Correct
```powershell
$env:JAVA_TOOL_OPTIONS='-Xms128m -Xmx512m -XX:ParallelGCThreads=1'
mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" "-Dmaven.resources.skip=true" -DforkCount=0 -DskipTests=false -Dtest=StoreupControllerCompatibilityTest test
```

接受结论时明确写出：这是后端 Java targeted verification，不代表默认资源复制链路已经恢复。

---

### Scenario: Proving Old-Table Runtime Dependency Is Truly Zero

#### 1. Scope / Trigger
- Scope: 声称“旧表已完全废弃”或“可以直接删旧表”的 backend/database cutover 任务。
- Trigger: controller 已切 compat、mapper 已改新表、用户开始追问是否可以完全抛弃旧表。
- Goal: 防止把“主路径看起来已切完”误判成“所有运行时依赖都已清零”。

#### 2. Signatures
- 必查对象：
  - controller/service 主流程
  - mapper XML 直接 SQL
  - MyBatis-Plus `@TableName`
  - `CommonController` 动态 `tableName` 入口
  - Token / session / 通用 CRUD fallback
- 最低证据命令：
  ```powershell
  rg -n -S "@TableName\\(\"(dianyingxinxi|dianyingleixing|storeup|discussdianyingxinxi|yonghu|users|config|sensitivewords|token)\"\\)" src/main/java
  ```
  ```powershell
  rg -n -S "FROM (dianyingxinxi|dianyingleixing|storeup|discussdianyingxinxi|yonghu|users|config|sensitivewords|token)\b|INSERT INTO (dianyingxinxi|dianyingleixing|storeup|discussdianyingxinxi|yonghu|users|config|sensitivewords|token)\b|UPDATE (dianyingxinxi|dianyingleixing|storeup|discussdianyingxinxi|yonghu|users|config|sensitivewords|token)\b|DELETE FROM (dianyingxinxi|dianyingleixing|storeup|discussdianyingxinxi|yonghu|users|config|sensitivewords|token)\b" src/main/resources/mapper src/main/java
  ```

#### 3. Contracts
- 不能把以下任一单点证据当作“可删表”证明：
  - compile 通过
  - targeted tests 通过
  - controller 已切 compat service
  - 前台页面还能返回数据
- 如果动态接口仍接收旧表名语义参数，就必须验证它们是否被桥接到新表，而不是默认当“无关路径”忽略。
- 若检查结论仍含“死代码残留”“未覆盖动态入口”“实体 fallback 未清”，最终结论只能是：
  - `条件下线`
  - 不能写成 `可直接废弃`
- 在 review/汇报中，必须把问题分层：
  - 旧 URL 是否仍暴露
  - 旧表是否仍是运行真值
  - 源码里是否仍有旧表残留

#### 4. Validation & Error Matrix
- grep 仍命中旧表 SQL，但汇报写“旧表已完全废弃”

---

### Scenario: Honest Naming For Ranked Home Sections

#### 1. Scope / Trigger
- Scope: 前台首页或列表页展示多个电影栏目，但底层数据都来自同一个通用列表接口。
- Trigger: 页面想展示 `Recommended`、`Editors' Choice`、`Trending`、`Movie of the Week` 一类栏目名时。
- Goal: 防止把“同源排序切片”包装成“个性化推荐”或“人工精选”，避免产生伪功能。

#### 2. Signatures
- 当前仓库真实只读列表接口：
  ```text
  GET /appmovie/front/list?page=<n>&limit=<n>&sort=<field>&order=<asc|desc>
  ```
- 当前可用排序字段至少包括：
  ```text
  clickCount | totalScore | commentCount | favoriteCount | releaseDate | createdAt
  ```
- 当前首页实现文件：
  ```text
  src/main/resources/front/front/src/pages/home/home-landing-panel.vue
  ```

#### 3. Contracts
- 如果栏目只是按 `sort` 字段取 Top N，栏目命名必须直接反映真实口径，例如：
  - `Top Rated` -> `sort=totalScore`
  - `Most Discussed` -> `sort=commentCount`
  - `Most Favorited` -> `sort=favoriteCount`
  - `Latest Release` -> `sort=releaseDate`
- 当系统没有真实的用户画像、协同过滤、人工编排来源时，禁止使用以下误导性命名：
  - `Recommended for You`
  - `Editors' Choice`
  - `For You`
  - `猜你喜欢`
  - 其他暗示“个性化”或“人工精选”的文案
- 如果多个栏目都来自同一个接口结果集，不允许通过循环切片、轮转偏移、伪去重等方式伪造成不同业务来源。
- 当某个真实榜单没有数据时，只能显示诚实空态，不能回退到静态假数据或无来源海报集合。

#### 4. Validation & Error Matrix
- 同一份列表按下标切片后命名为 `Recommended for You` -> 违规；必须改名或改成真实推荐来源。
- 没有人工编辑后台，却展示 `Editors' Choice` -> 违规；必须改成真实可解释榜单名。
- 某榜单请求失败后回退到静态电影海报数组 -> 违规；必须显示空态或错误态。
- 栏目名与排序字段不一致，例如 `Top Rated` 实际用 `clickCount` -> 违规；必须统一口径。

#### 5. Good / Base / Bad Cases
- Good:
  - 首页分别请求 `clickCount`、`totalScore`、`commentCount`、`favoriteCount`、`releaseDate`
  - 每个栏目名与排序口径一致
  - 空数据时展示诚实空态
- Base:
  - 暂时只有一个通用列表接口
  - 页面仍使用真实榜单名，不再伪装个性化或编辑推荐
- Bad:
  - 同源列表通过 `takeLoopItems(...)` 切成多个栏目
  - 使用静态 fallback 海报和英文片名假装真实业务结果
  - 没有真实推荐模型却展示 `For You`

#### 6. Tests Required
- 代码检索断言：
  ```powershell
  rg -n "Recommended for You|Editors' Choice|猜你喜欢|takeLoopItems|FALLBACK_" src/main/resources/front/front/src/pages/home
  ```
- 实现断言：
  - 首页栏目请求参数里的 `sort` 与栏目标题一一对应
  - 不再依赖静态假电影数组作为栏目回退
- 校验断言：
  - 定向 eslint 通过
  - `git diff --check` 通过

#### 7. Wrong vs Correct
##### Wrong
```text
1. 先请求一次 clickCount 排序列表
2. 再用循环切片拆成 Recommended / Trending / Editors' Choice
3. 某栏为空时回退到内置假电影海报
```

##### Correct
```text
1. 每个栏目按自己的真实排序口径独立取数
2. 栏目标题直接反映真实来源
3. 没有真实推荐能力时，不使用个性化或人工精选命名
4. 没数据就显示空态，不伪造内容
```

---

### Scenario: Homepage Banner Must Follow Managed Config

#### 1. Scope / Trigger
- Scope: 前台首页头图轮播、后台轮播图管理、以及 `config/app_config` 相关配置链路。
- Trigger: 用户要求“后台轮播图可管理首页头图”，或前台首页出现 banner / hero 轮播改造。
- Goal: 防止出现“后台轮播图管理页是真实 CRUD，但首页仍读取别的数据源”的断链状态。

#### 2. Signatures
- 后台配置实体：
  ```text
  app_config(legacy_config_id, name, value, url)
  ```
- 当前公开读取接口：
  ```text
  GET /config/list?page=<n>&limit=<n>&name=%picture%
  ```
- 当前首页实现文件：
  ```text
  src/main/resources/front/front/src/pages/home/home-landing-panel.vue
  ```

#### 3. Contracts
- 首页头图轮播的第一数据源必须是轮播配置，而不是电影榜单。
- 轮播配置至少使用：
  - `name`：轮播项名称/展示标题候选
  - `value`：图片地址或逗号串图片地址
  - `url`：点击跳转链接
- 当 `value` 无有效图片时，该轮播项不能进入首页头图集合。
- `url` 跳转必须分流：
  - `http/https` -> 外链打开
  - 站内 path/hash -> 前端路由跳转
  - 空值或无法识别 -> 稳妥降级，不报错
- 只有在轮播配置为空、无图、或请求失败时，首页头图才允许降级到电影 hero 数据。
- 降级只能使用真实电影数据，禁止回退到写死 banner 数组。

#### 4. Validation & Error Matrix
- 后台能改 `config`，但首页仍只读 `appmovie/front/list` 作为头图 -> 违规；属于“管理未接通”。
- `config` 有记录，但 `value` 无图仍渲染为空白 banner -> 违规；必须过滤掉无图项。
- `url` 是外链却直接走 `router.push` -> 违规；必须按链接类型分流。
- `config` 为空时回退到静态图片数组 -> 违规；必须回退到真实电影 hero 或空态。

#### 5. Good / Base / Bad Cases
- Good:
  - 首页先拉 `config/list?name=%picture%`
  - 有图就用 config 轮播
  - 无图/无配置时降级到真实电影 hero
  - 下方电影栏目逻辑保持独立
- Base:
  - 只接通首页头图
  - 不扩数据库、不加新后端接口
- Bad:
  - 后台可维护轮播图，但首页完全不读
  - 用电影榜单伪装成轮播图管理已生效
  - 配置无图仍渲染空 banner

#### 6. Tests Required
- 代码检索断言：
  ```powershell
  rg -n "config/list|movieHeroSlides|applyHeroSlides|actionUrl" src/main/resources/front/front/src/pages/home/home-landing-panel.vue
  ```
- 实现断言：
  - 首页头图存在“config 优先，movie 降级”的显式逻辑
  - 无写死 banner fallback 数组
  - `url` 跳转分流逻辑存在
- 校验断言：
  - 定向 eslint 通过
  - `git diff --check` 通过

#### 7. Wrong vs Correct
##### Wrong
```text
1. 后台维护 config 里的 picture 记录
2. 首页 hero 仍只请求电影榜单
3. 对外宣称“首页轮播已可后台管理”
```

##### Correct
```text
1. 首页 hero 优先请求 config/list 的轮播配置
2. 只消费有图的轮播项
3. 点击按 url 类型跳转
4. config 为空时才降级到真实电影 hero
```
  -> 结论无效
- `@TableName("legacy_table")` 仍存在，且类仍参与运行时查询
  -> 结论无效
- 只验证 compat controller，不验证动态入口 `option/follow/group/value`
  -> 证据不足
- 测试只覆盖 happy path，不覆盖 legacy 映射缺失/动态桥接
  -> 只能算部分通过

#### 5. Good / Base / Bad Cases
- Good:
  - 分层证明主流程、动态入口、通用 fallback 都已切新表
  - grep 结果为空
  - targeted tests 覆盖 compat + dynamic bridge
- Base:
  - 主流程无问题
  - 仍有残留待清
  - 结论明确写 `条件下线`
- Bad:
  - 直接输出“当前能完全废弃旧表”
  - 不区分运行真值与 URL 兼容层

#### 6. Tests Required
- `git diff --check`
- compile
- 覆盖 compat 和 dynamic bridge 的 targeted tests
- grep 旧表 SQL / `@TableName` 结果审计
- 若准备删表，再做 `_bak` 改名实跑验证

#### 7. Wrong vs Correct
##### Wrong
```text
1. 旧页面能用
2. controller 已切 compat
3. 因此旧表都可以删
```

##### Correct
```text
1. 先证明旧表不再是运行真值
2. 再证明动态入口和通用 fallback 不回旧表
3. 最后把 grep / tests / smoke 证据一起给出
4. 只有全部满足时，才给“可下线”结论
```

---

### Scenario: account password change must be server-validated and session-safe

#### 1. Scope / Trigger
- Scope: `users` / `yonghu` 账号安全链路，尤其是“修改密码”“session 回包”“前台本地会话缓存”。
- Trigger: 新增或修改改密接口、个人中心改密流程、关闭公开找回密码、或调整 `/session` 返回结构时。
- Goal: 防止把“前端本地比对旧密码”误当成真实改密能力，也防止密码继续通过 session 或本地缓存泄露。

#### 2. Signatures
- Required password-change endpoints:
  - `POST /users/changePassword`
  - `POST /yonghu/changePassword`
- Request body:
  ```json
  {
    "oldPassword": "string",
    "newPassword": "string"
  }
  ```
- Session fields used for authorization:
  - `userId`
  - `tableName`
- Related compatibility endpoints:
  - `GET /users/session`
  - `GET /yonghu/session`
- Explicitly disabled public reset entrypoints:
  - `GET/POST /users/resetPass`
  - `GET/POST /yonghu/resetPass`

#### 3. Contracts
- Contract 1: 旧密码真值校验必须在服务端完成；前端只能做“新密码确认一致”“新旧密码不能相同”这类体验校验。
- Contract 2: 改密接口必须从当前 session 读取 `userId` 和 `tableName`；不能信任前端传回的 `sessionForm.password` 或 `sessionForm.mima`。
- Contract 3: `tableName` 与接口主体不匹配时必须失败，例如 `users` 会话不能调用 `yonghu/changePassword`。
- Contract 4: `oldPassword` 和 `newPassword` 为空、空白或缺失时必须失败，不能静默落到 `/update`。
- Contract 5: `/update` 只能改资料字段，不能接受密码变更；若 payload 带了 `password` / `mima`，后端必须恢复为持久化中的原值。
- Contract 6: `/users/session` 与 `/yonghu/session` 返回给前端前，必须清空密码字段；前端本地 `sessionForm` 落缓存前也必须剔除 `password` / `mima`。
- Contract 7: phase-1 当前不提供公开自助密码重置；`resetPass` 必须明确返回关闭提示，不能再硬编码重置为 `123456`。
- Contract 8: `yonghu` 改密后，仍需保持 compat/session 同步语义，例如继续刷新 `appUserSessionDao` 快照。

#### 4. Validation & Error Matrix
- session 中没有 `userId`
  -> 返回 `未登录或会话已失效`
- session 的 `tableName` 与接口不匹配
  -> 返回 `当前会话不允许修改该账户密码`
- `oldPassword` / `newPassword` 为空或空白
  -> 返回 `原密码和新密码不能为空`
- 旧密码与持久化密码不一致
  -> 返回 `原密码错误`
- `resetPass` 仍返回“已重置为 123456”
  -> 阻断，视为高风险写死逻辑回归
- `/update` 仍能把 payload 中的 `password` / `mima` 写入数据库
  -> 阻断，说明专用改密旁路未封住
- `/session` 或本地 `sessionForm` 仍含密码字段
  -> 阻断，说明凭证仍在前端暴露

#### 5. Good / Base / Bad Cases
- Good:
  - 前端走专用 `changePassword`
  - 后端按 session + 旧密码校验
  - `/update` 不能改密码
  - `/session` 与本地缓存都不再带密码
- Base:
  - 后端已补专用改密接口
  - 但前端尚未切换，或 session 仍返回密码字段
- Bad:
  - 前端直接拿本地缓存密码比对
  - 然后把新密码通过 `/update` 写回
  - 或公开 `resetPass` 继续把密码写死成 `123456`

#### 6. Tests Required
- `git diff --check`
- targeted controller tests 至少覆盖：
  - 未登录改密失败
  - `tableName` 不匹配失败
  - 空原密码 / 空新密码失败
  - 旧密码错误失败
  - 旧密码正确时改密成功
  - `/update` 不能直接改密码
  - `resetPass` 已关闭
  - `/session` 回包不再暴露密码字段
- 当前仓库稳定命令可用：
  ```powershell
  $env:JAVA_TOOL_OPTIONS='-Xms128m -Xmx512m -XX:ParallelGCThreads=1'
  mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" "-Dmaven.resources.skip=true" -DforkCount=0 -DskipTests=false '-Dtest=UsersControllerAccountSecurityTest,YonghuControllerAccountSecurityTest' test
  ```
- 前端至少做单文件 lint：
  ```powershell
  .\node_modules\.bin\eslint.cmd src\pages\center\center.vue
  ```

#### 7. Wrong vs Correct
##### Wrong
```text
1. center.vue 读取本地 sessionForm.mima
2. 浏览器里直接判断“原密码是否正确”
3. 把新密码塞回 /update payload
4. /session 再把密码返回给前端缓存
```

##### Correct
```text
1. center.vue 只收集 oldPassword/newPassword
2. POST 到 /users/changePassword 或 /yonghu/changePassword
3. 服务端基于 session + 持久化密码做校验
4. /update 保留原密码，不接受资料修改顺带改密
5. /session 和本地 sessionForm 都剔除 password/mima
```

---

### Scenario: AES-compatible password transport and storage cutover

#### 1. Scope / Trigger
- Scope: `users` / `yonghu` 的登录、注册、保存、新增、改密，以及前后台提交密码的页面。
- Trigger: 需要把密码提交改为 AES 传输，同时不能打断库里已有的明文存量账号。
- Goal: 前端统一发 AES 密码，后端统一兼容解密并把新写入收口成 AES 密文，同时保留旧明文登录兼容。

#### 2. Signatures
- Shared backend utility:
  - `EncryptUtil.normalizeIncomingPassword(String)`
  - `EncryptUtil.encryptPasswordForStorage(String)`
  - `EncryptUtil.passwordMatches(String incomingPassword, String persistedPassword)`
- Front/admin password submit entrypoints:
  - `front/src/pages/login/login.vue`
  - `front/src/pages/register/register.vue`
  - `front/src/pages/center/center.vue`
  - `admin/src/views/login.vue`
  - `admin/src/views/register.vue`
- Required backend write entrypoints:
  - `POST /users/register`
  - `POST /yonghu/register`
  - `POST /users/save`
  - `POST /yonghu/save`
  - `POST /yonghu/add`
  - `POST /users/changePassword`
  - `POST /yonghu/changePassword`
- Required login entrypoints:
  - `/users/login`
  - `/yonghu/login`

#### 3. Contracts
- Contract 1: 前端和后台页面提交密码前，必须先调用现有 AES 能力；不要在页面里手写另一套 key/iv 或算法。
- Contract 2: 后端收到密码后，必须先走 `normalizeIncomingPassword`；如果是 AES 密文则先解密，解不出来才按原值处理。
- Contract 3: 新注册、新增、保存、改密后的持久化密码必须统一走 `encryptPasswordForStorage`，不能继续把明文新写入数据库。
- Contract 4: 登录和旧密码校验必须统一走 `passwordMatches`，同时兼容：
  - 库里仍是旧明文
  - 库里已经是 AES 密文
- Contract 5: `/update` 不能借机把密码字段重新写成明文；密码变更仍只允许走专用改密接口。
- Contract 6: `/session` 与前端本地缓存不允许返回或保存密码字段；AES 改造不能回退这条安全约束。
- Contract 7: admin 侧若页面调用 `this.encryptAes`，则 `admin/src/main.js` 必须显式挂载 `encryptAes/decryptAes`；不能只改页面不补全局挂载。
- Contract 8: front 侧若继续依赖 Vue prototype 上的 AES 能力，则 `front/src/main.js` 必须保持现有挂载语义不变。

#### 4. Validation & Error Matrix
- 前端已开始发 AES，但后端仍直接拿入参和库里值做字符串相等比较
  -> 登录/改密会在新链路下失败，视为阻断
- 后端已兼容 AES 入参，但注册/保存仍把新密码明文写库
  -> 视为半切换状态，不满足本轮合同
- `passwordMatches` 只支持“明文入参 + 密文库”或只支持“密文入参 + 明文库”其中一种
  -> 视为兼容逻辑不完整
- 页面调用 `this.encryptAes`，但对应 `main.js` 没有挂载
  -> 页面运行时直接报 `is not a function`，视为阻断
- `/update` 在 AES 改造后又允许改密码
  -> 视为回归阻断
- `/session` 或本地 `sessionForm` 再次带出密码字段
  -> 视为回归阻断

#### 5. Good / Base / Bad Cases
- Good:
  - 前后端都复用现有 AES 工具
  - 入参兼容解密
  - 新写入统一落 AES 密文
  - 旧明文存量仍可登录
  - `/update` 和 `/session` 的安全约束保持不变
- Base:
  - 登录已兼容 AES
  - 但新注册/新改密还没有统一落密文
- Bad:
  - 前端发 AES，后端仍按明文比较
  - 或后端新写入继续保存明文
  - 或只改页面，不补 `main.js` 全局挂载

#### 6. Tests Required
- `git diff --check`
- targeted Maven tests 至少覆盖：
  - AES 入参 + 旧明文存储仍能登录成功
  - AES 入参 + AES 密文存储能登录成功
  - register / save / add / changePassword 最终写入的是 AES 密文
  - `/session` 不暴露密码
  - `/update` 不能直接改密码
  - `resetPass` 仍关闭
- 推荐 targeted Maven 命令：
  ```powershell
  $env:JAVA_TOOL_OPTIONS='-Xms128m -Xmx512m -XX:ParallelGCThreads=1'
  mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" "-Dmaven.resources.skip=true" -DforkCount=0 -DskipTests=false '-Dtest=UsersControllerAccountSecurityTest,YonghuControllerAccountSecurityTest,YonghuControllerRegisterCompatibilityTest,EncryptUtilPasswordCompatibilityTest' test
  ```
- 前端最小校验：
  - admin AES 相关文件 lint 通过
  - front AES 相关文件至少做定向 lint，并把既有噪音与本轮新增问题分开说明

#### 7. Wrong vs Correct
##### Wrong
```text
1. front/admin 页面开始调用 encryptAes
2. users/yonghu 登录仍直接比较入参与库中密码
3. register/save/changePassword 继续把明文写进库
4. admin main.js 没有挂载 encryptAes
```

##### Correct
```text
1. 页面提交前统一做 AES
2. 后端统一 normalizeIncomingPassword
3. 登录/旧密码校验统一 passwordMatches
4. 新写入统一 encryptPasswordForStorage
5. /update 不改密码，/session 不回密码
6. front/admin 的 main.js 都具备页面所需 AES 能力
```

---

## Scenario: AI chat SSE and remote LLM fallback

### 1. Scope / Trigger
- Trigger: any change to AI chat streaming endpoints, LLM provider calls, SSE response framing, or LLM env wiring.
- Applies to:
  - `GET /ai-chat/stream`
  - `LlmClient.streamChat(List<JSONObject>)`
  - `LlmClient.chat(List<JSONObject>)`
  - `AiChatServiceImpl.streamChat(Long, String, Long)`
  - MVC async/SSE configuration.
- Reason: Spring MVC, Reactor, browser `EventSource`, database persistence, and remote LLM transport share one cross-layer contract. A small mismatch can produce duplicate `data:` frames, empty assistant responses, leaked subscriptions, or silent local fallback.

### 2. Signatures
- Controller API:
  ```text
  GET /springbootdo4wek3z/ai-chat/stream?conversationId=<Long>&message=<String>&token=<front-user-token>
  Accept: text/event-stream
  Response: text/event-stream, data:<chunk> frames, final data:[DONE]
  ```
- Controller return type:
  ```java
  public SseEmitter streamChat(@RequestParam Long conversationId,
                               @RequestParam String message,
                               HttpServletRequest request)
  ```
- LLM client signatures:
  ```java
  public Flux<String> streamChat(List<JSONObject> messages)
  public Mono<String> chat(List<JSONObject> messages)
  ```
- Required env keys:
  ```text
  LLM_API_URL=<provider base URL, e.g. https://host/v1>
  LLM_API_KEY=<secret>
  LLM_MODEL=<model name>
  LLM_PROVIDER=openai|anthropic|local
  LLM_ANTHROPIC_VERSION=2023-06-01
  ```

### 3. Contracts
- `/ai-chat/stream` must return `SseEmitter`, not `Flux<String>`, in this Spring MVC app.
- `SseEmitter.event().data(...)` owns SSE framing; service/client layers must emit plain text chunks, not preformatted `data: ...\n\n` strings.
- Browser `EventSource` cannot set custom headers, so the stream endpoint must accept the token query parameter and validate it as a front-user `yonghu` token.
- OpenAI-compatible streaming request:
  ```json
  {"model":"<LLM_MODEL>","messages":[...],"stream":true,"max_tokens":1000}
  ```
  Response parser must read `choices[0].delta.content` and ignore reasoning-only deltas such as `reasoning_content`.
- OpenAI-compatible non-stream fallback request:
  ```json
  {"model":"<LLM_MODEL>","messages":[...],"stream":false,"max_tokens":1000}
  ```
  Response parser must read `choices[0].message.content`.
- Anthropic streaming request must use `/v1/messages`, `x-api-key`, `anthropic-version`, and parse `content_block_delta` / `text_delta`.
- Anthropic non-stream fallback must parse text blocks from the `content` array.
- Remote fallback order:
  ```text
  remote stream -> remote non-stream completion -> local fallback
  ```
- If remote stream has already emitted any nonblank chunk, a later stream error must not append remote non-stream or local fallback to the partial assistant response.
- If remote stream errors before nonblank content or completes empty, the service must attempt remote non-stream before local fallback.
- The assistant message saved to `app_ai_message` must match the text streamed or the chosen fallback text.

### 4. Validation & Error Matrix
- Missing/invalid token -> stream `请先登录`, then `[DONE]`, without invoking the LLM.
- Token exists but `tablename != yonghu` -> treat as unauthenticated.
- Remote stream returns non-2xx -> log sanitized status/body/request id; if no chunk was emitted, try non-stream remote completion.
- Remote stream times out or has SSL/connect failure -> retry only transient network errors within the client policy, then apply fallback order.
- Remote stream completes with zero parsed content -> try non-stream remote completion, then local fallback.
- Remote stream emits content then errors -> complete the stream without appending fallback text.
- Remote non-stream returns blank -> local fallback.
- Remote non-stream fails -> local fallback.
- `do_request_failed` / `upstream error` from an API relay -> classify as provider/relay failure, not as proof that local code skipped the API.
- Client disconnect/timeout -> dispose the Reactor subscription and stop sending chunks.

### 5. Good/Base/Bad Cases
- Good:
  - Runtime log shows `Starting remote LLM stream ...`; endpoint sends provider chunks and `data:[DONE]`; database stores the same assistant text.
  - Stream fails before content; log shows stream failure, then `Starting remote LLM completion ...`; non-stream text is sent and stored.
- Base:
  - Remote stream and non-stream both fail because the provider/relay is unavailable; endpoint sends local fallback and `[DONE]`; logs include sanitized provider failure details.
  - Local provider is selected or no key is configured; endpoint uses local fallback without remote calls.
- Bad:
  - Controller returns `Flux<String>` and Spring logs `Streaming through a reactive type requires an Executor`.
  - Controller maps chunks to strings starting with `data:` before sending through Spring SSE support, causing nested/duplicated SSE frames.
  - Stream completes empty and the client receives only `[DONE]` while the database stores a fallback answer.
  - A partial remote answer is followed by an unrelated fallback answer.
  - Provider error logs include the full API key.

### 6. Tests Required
- Compile/type gate:
  ```bash
  mvn -q -DskipTests compile
  ```
- Targeted code checks:
  - grep or inspect that `/ai-chat/stream` returns `SseEmitter`, not `Flux<String>`.
  - grep logs/runtime output for absence of `Returning: reactor.core.publisher.Flux<java.lang.String>`.
  - grep focus files for mojibake patterns after editing Chinese messages.
- Runtime smoke when DB is available:
  - Unauthenticated stream returns exactly a `请先登录` event followed by `[DONE]`.
  - Authenticated stream creates a conversation, sends a message, receives non-empty content and `[DONE]`, and persists an assistant message.
  - Force or simulate remote stream empty/error before content and assert non-stream fallback is attempted before local fallback.
  - Force remote stream partial content then error and assert no fallback text is appended.
- Provider diagnostics:
  - Directly test the configured `LLM_API_URL + /chat/completions` with the same model/key when diagnosing provider failures.
  - Record relay request ids from sanitized logs for service-provider escalation.

### 7. Wrong vs Correct
#### Wrong
```java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> streamChat(...) {
    return aiChatService.streamChat(...)
            .map(content -> "data: " + content + "\n\n")
            .concatWith(Flux.just("data: [DONE]\n\n"));
}
```

#### Correct
```java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamChat(...) {
    SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MILLIS);
    Disposable subscription = aiChatService.streamChat(...)
            .subscribe(
                    content -> emitter.send(SseEmitter.event().data(content)),
                    error -> completeWithUnavailableMessage(emitter),
                    () -> sendDoneAndComplete(emitter)
            );
    emitter.onCompletion(subscription::dispose);
    emitter.onTimeout(subscription::dispose);
    emitter.onError(error -> subscription.dispose());
    return emitter;
}
```

---

## Code Review Checklist

<!-- What reviewers should check -->

(To be filled by the team)
