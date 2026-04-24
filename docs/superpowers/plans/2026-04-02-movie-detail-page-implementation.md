# Movie Detail Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将前台电影详情页重构为接近原型图的电影感详情页，保留现有收藏、点赞、评论、分享等功能，并新增同类型相似电影展示。

**Architecture:** 直接在现有详情页组件上重构，不新增并行路由。把页面拆成“首屏 Hero + Similar Movies + 下方扩展功能区”三层，并把可测试的映射/推荐逻辑抽到独立 helper 中，用最小 Node 断言脚本做逻辑回归，样式和布局通过构建与人工校验完成。

**Tech Stack:** Vue 2、Vue Router、Element UI、Swiper 5、SCSS、Spring Boot 现有详情/列表接口

---

### Task 1: 抽离详情页可测试逻辑

**Files:**
- Create: `src/main/resources/front/front/src/pages/dianyingxinxi/detail-helpers.js`
- Create: `src/main/resources/front/front/scripts/detail-helpers.test.js`

- [ ] **Step 1: 写失败中的逻辑测试脚本**

在 `src/main/resources/front/front/scripts/detail-helpers.test.js` 中写 Node `assert` 测试，覆盖：

- `getPosterList(detail)` 能从 `haibao` 拆出图片数组
- `getPrimaryPoster(detail, baseUrl)` 能得到首图 URL
- `formatHeroMeta(detail)` 能返回年份、评分、Overview
- `buildSimilarMovies(currentMovie, movieList, limit)` 能按同类型、排除自身、按点击量/评分排序

- [ ] **Step 2: 运行测试确认失败**

Run:

```powershell
node src/main/resources/front/front/scripts/detail-helpers.test.js
```

Expected:

- 因 helper 文件不存在或方法未导出而失败

- [ ] **Step 3: 写最小 helper 实现**

在 `detail-helpers.js` 中实现：

- `normalizePosterList`
- `getPrimaryPoster`
- `formatHeroMeta`
- `buildSimilarMovies`

保持纯函数，不依赖 Vue 实例。

- [ ] **Step 4: 运行测试确认通过**

Run:

```powershell
node src/main/resources/front/front/scripts/detail-helpers.test.js
```

Expected:

- 全部断言通过

---

### Task 2: 重构详情页数据模型与首屏结构

**Files:**
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/detail.vue`
- Reference: `src/main/resources/front/front/src/pages/home/home.vue`
- Reference: `src/main/resources/front/front/src/pages/index.vue`

- [ ] **Step 1: 在详情页中接入 helper 并补齐新状态**

给详情页新增状态：

- `posterList`
- `primaryPoster`
- `similarMovies`
- `heroMeta`
- `staticCast`
- `detailSectionRef` 相关滚动标识

- [ ] **Step 2: 先保留旧功能方法，改造 `init()` 数据流**

在 `init()` 成功拉取详情后：

- 生成首屏图片和元信息
- 生成 Similar Movies 数据
- 保留现有评论、收藏、点赞状态拉取逻辑

- [ ] **Step 3: 重写模板首屏结构**

将旧的：

- 面包屑主视觉
- 左右详情字段区
- 双层海报轮播

替换为：

- 顶部导航栏
- Hero 首屏
- Similar Movies 横向区

- [ ] **Step 4: 运行前端构建确认模板无语法错误**

Run:

```powershell
npm run build
```

Working directory:

```text
src/main/resources/front/front
```

Expected:

- 构建成功，无 Vue 模板语法错误

---

### Task 3: 接入主按钮与相似电影交互

**Files:**
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/detail.vue`

- [ ] **Step 1: 实现 `Watch Trailer` 滚动到详情正文**

为电影正文区增加锚点 / `ref`，点击按钮时平滑滚动到正文模块。

- [ ] **Step 2: 将 `Add to My List` 绑定到现有收藏逻辑**

按钮状态需映射：

- 未收藏 -> 添加收藏
- 已收藏 -> 取消收藏

- [ ] **Step 3: 实现 Similar Movies 跳转**

点击推荐卡片跳转到：

```text
/index/dianyingxinxiDetail?id=目标电影ID
```

并确保切换详情后重新加载首屏和推荐数据。

- [ ] **Step 4: 手工验证交互链路**

验证：

- 当前详情页可进入
- `Watch Trailer` 能滚动
- 收藏状态切换正常
- Similar Movies 点击后进入新详情页

---

### Task 4: 下方扩展功能区电影化重排

**Files:**
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/detail.vue`

- [ ] **Step 1: 保留现有功能模块，重排到首屏下方**

保留：

- 详情正文
- 点赞 / 点踩
- 评论列表与评论提交
- 分享

改为新的区块顺序和容器结构。

- [ ] **Step 2: 去掉首屏不再需要的旧布局依赖**

删除或停用：

- 旧字段行布局
- 旧 tabs 主外观
- 旧轮播主结构

避免旧样式与新首屏并存。

- [ ] **Step 3: 统一下方区块视觉语言**

调整为：

- 深色背景
- 高对比文字
- 圆角卡片
- 与首屏一致的边框、阴影、按钮风格

- [ ] **Step 4: 运行构建再次确认**

Run:

```powershell
npm run build
```

Expected:

- 构建成功

---

### Task 5: 响应式与最终回归

**Files:**
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/detail.vue`

- [ ] **Step 1: 增加桌面 / 平板 / 手机断点样式**

确保：

- 桌面端贴近原型
- 平板端 Hero 可上下堆叠
- 手机端 Similar Movies 和功能区不溢出

- [ ] **Step 2: 做页面级人工回归清单**

检查：

- 首屏布局是否贴近原型
- 图片与文案是否都能容错
- 无海报 / 无评分 / 无评论时不崩
- `centerType` 场景仍能返回

- [ ] **Step 3: 运行最终构建验证**

Run:

```powershell
npm run build
```

Expected:

- 构建成功

- [ ] **Step 4: 记录未覆盖风险**

如果未接入真实演员数据、预告片 URL、片长 / 分级字段，需要在交付说明中明确标注为静态占位。

---

## 执行备注

- 当前目录不是 Git 仓库，因此本计划不包含 `git commit` 步骤。
- 当前前端无现成单测框架，因此仅为纯逻辑 helper 增加最小 Node 断言测试；UI 布局通过 `npm run build` 与人工验收回归。
