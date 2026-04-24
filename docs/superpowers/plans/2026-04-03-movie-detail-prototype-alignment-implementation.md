# Movie Detail Prototype Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将前台电影详情页重构为与 `demo/movie_detail/code.html` 的主结构和交互逻辑严格一致的版本，同时最大化复用现有后端字段与收藏/评论/点赞/分享等前端业务逻辑。

**Architecture:** 直接重写现有 `detail.vue` 的模板结构，以原型 HTML 为唯一主结构来源。保留现有详情接口与互动逻辑，新增一个最小 Node 结构回归脚本约束“必须存在的原型区块”和“必须删除的旧新增区块”，再用 `detail-helpers` 纯函数测试与前端构建做回归验证。

**Tech Stack:** Vue 2、Element UI、SCSS、Node `assert`、Spring Boot 现有 `/dianyingxinxi/detail` 与 `/dianyingxinxi/list` 接口

---

## File Structure

- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/detail.vue`
  - 唯一主实现文件
  - 负责原型结构映射、字段绑定、按钮行为映射、相似电影展示、下方业务区保留
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/detail-helpers.js`
  - 仅在需要时微调元信息映射逻辑
  - 继续负责海报解析、主海报 URL、Hero 元信息、Similar Movies 生成
- Test: `src/main/resources/front/front/scripts/detail-helpers.test.js`
  - 纯逻辑 helper 回归
- Create: `src/main/resources/front/front/scripts/detail-structure.test.js`
  - 对 `detail.vue` 源码做最小结构断言
  - 确保原型关键结构存在，且当前多余组件被删除

---

### Task 1: 建立详情页结构回归测试

**Files:**
- Create: `src/main/resources/front/front/scripts/detail-structure.test.js`
- Reference: `demo/movie_detail/code.html`
- Reference: `src/main/resources/front/front/src/pages/dianyingxinxi/detail.vue`

- [ ] **Step 1: 写失败中的结构回归测试脚本**

在 `src/main/resources/front/front/scripts/detail-structure.test.js` 中写入：

```js
const assert = require('assert')
const fs = require('fs')
const path = require('path')

const detailPath = path.join(__dirname, '../src/pages/dianyingxinxi/detail.vue')
const source = fs.readFileSync(detailPath, 'utf8')

function has(text) {
  return source.includes(text)
}

function run() {
  assert.ok(has('class="hero-section"'), '应保留原型 Hero 区')
  assert.ok(has('Overview'), '应保留 Overview 标题')
  assert.ok(has('Similar Movies'), '应保留 Similar Movies 标题')
  assert.ok(has('Watch Trailer'), '应保留 Watch Trailer 按钮')
  assert.ok(has('Add to My List'), '应保留 Add to My List 按钮')

  assert.ok(!has('class="cinematic-topbar"'), '不应保留自定义顶部标题栏')
  assert.ok(!has('class="poster-thumb-row"'), '不应保留海报缩略图条')
  assert.ok(!has('Cast &amp; Crew'), '不应保留演员详情区')
  assert.ok(!has('class="score-panel"'), '不应保留额外统计卡片')

  console.log('detail-structure tests passed')
}

run()
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
node src/main/resources/front/front/scripts/detail-structure.test.js
```

Expected:

```text
AssertionError [ERR_ASSERTION]: 不应保留自定义顶部标题栏
```

或其它与当前多余结构相关的失败信息。

- [ ] **Step 3: 保持 helper 逻辑测试可用**

确认不改动 `detail-helpers.test.js` 当前断言主体，仅作为后续结构改造时的回归保障。当前文件应继续保留以下导入形式：

```js
const {
  getPosterList,
  getPrimaryPoster,
  formatHeroMeta,
  buildSimilarMovies,
} = require(helperPath)
```

- [ ] **Step 4: 先运行 helper 测试建立基线**

Run:

```bash
node src/main/resources/front/front/scripts/detail-helpers.test.js
```

Expected:

```text
detail-helpers tests passed
```

---

### Task 2: 按原型重写 Hero 区并删除多余组件

**Files:**
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/detail.vue`
- Test: `src/main/resources/front/front/scripts/detail-structure.test.js`

- [ ] **Step 1: 先删除与原型冲突的顶部与缩略图结构**

将 `detail.vue` 模板中的这些区块删掉：

```vue
<section class="cinematic-topbar">
  ...
</section>
```

```vue
<div class="poster-thumb-row" v-if="posterList.length > 1">
  ...
</div>
```

并删除对应样式块：

```scss
.cinematic-topbar { ... }
.poster-thumb-row { ... }
.poster-thumb { ... }
```

- [ ] **Step 2: 将 Hero 模板改成与原型一致的单主视觉结构**

把当前 Hero 区改成接近下面的结构：

```vue
<section class="hero-section" :style="heroBackdropStyle">
  <div class="hero-overlay"></div>
  <div class="hero-content">
    <div class="hero-poster-wrap">
      <img :src="primaryPoster || fallbackPoster" :alt="detail.dianyingmingcheng || 'movie poster'">
    </div>
    <div class="hero-copy">
      <div class="hero-meta-row">
        <span class="meta-chip">{{ heroMeta.year }}</span>
        <span class="meta-chip">{{ heroMeta.runtime }}</span>
        <span class="meta-chip">{{ heroMeta.maturityRating }}</span>
        <div class="meta-rating">
          <i class="el-icon-star-on"></i>
          <span>{{ heroMeta.scoreText }}/5</span>
        </div>
      </div>
      <h1 class="hero-title">{{ detail.dianyingmingcheng || '电影详情' }}</h1>
      <div class="hero-actions">
        <el-button class="primary-btn" type="primary" @click="scrollToDetailSection">Watch Trailer</el-button>
        <el-button class="ghost-btn" @click="handleStoreupToggle">{{ isStoreup ? 'Remove from My List' : 'Add to My List' }}</el-button>
      </div>
    </div>
  </div>
</section>
```

- [ ] **Step 3: 删除当前额外统计卡片与演员区模板**

从模板中删除这两个整块：

```vue
<div class="score-panel">
  ...
</div>
```

```vue
<div class="cast-card">
  ...
</div>
```

- [ ] **Step 4: 删掉与演员区和统计卡片相关的状态与方法**

从 `data()` 中删除：

```js
staticCast: [],
```

删除方法：

```js
buildStaticCast(detail) {
  ...
}
```

并删除 `init()` 中这行：

```js
this.staticCast = this.buildStaticCast(this.detail)
```

- [ ] **Step 5: 运行结构测试确认 Hero 区方向正确**

Run:

```bash
node src/main/resources/front/front/scripts/detail-structure.test.js
```

Expected:

```text
detail-structure tests passed
```

---

### Task 3: 将 Overview 与右侧四张信息卡按原型映射到真实字段

**Files:**
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/detail.vue`
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/detail-helpers.js` (only if needed)
- Test: `src/main/resources/front/front/scripts/detail-helpers.test.js`

- [ ] **Step 1: 先写一个 helper 回归用例，固定 Hero 元信息输出**

在 `detail-helpers.test.js` 中保留或补充如下断言，确保后续 Overview / Hero 改造不破坏元信息输出：

```js
assert.deepStrictEqual(
  formatHeroMeta(sampleMovie),
  {
    year: '2025',
    runtime: '2h 16m',
    maturityRating: 'PG-13',
    overview: '测试简介',
    scoreText: '8.5',
    releaseDateText: '2025-01-01',
  },
  '应能格式化首屏元信息'
)
```

- [ ] **Step 2: 运行 helper 测试确认仍为绿色基线**

Run:

```bash
node src/main/resources/front/front/scripts/detail-helpers.test.js
```

Expected:

```text
detail-helpers tests passed
```

- [ ] **Step 3: 在模板中加入原型式 Overview + info cards 区**

在 Hero 后、Similar Movies 前加入类似如下结构：

```vue
<section class="overview-section">
  <div class="overview-grid">
    <div class="overview-copy">
      <h2>Overview</h2>
      <p>{{ heroMeta.overview }}</p>
      <div class="genre-chips">
        <span class="genre-chip" v-for="item in genreList" :key="item">{{ item }}</span>
      </div>
    </div>
    <div class="overview-cards">
      <div class="info-card">
        <span class="info-label">导演</span>
        <span class="info-value">{{ detail.daoyan || '待补充' }}</span>
      </div>
      <div class="info-card">
        <span class="info-label">区域</span>
        <span class="info-value">{{ detail.quyu || '待补充' }}</span>
      </div>
      <div class="info-card">
        <span class="info-label">上映时间</span>
        <span class="info-value">{{ heroMeta.releaseDateText }}</span>
      </div>
      <div class="info-card">
        <span class="info-label">收藏数</span>
        <span class="info-value">{{ detail.storeupnum || 0 }}</span>
      </div>
    </div>
  </div>
</section>
```

- [ ] **Step 4: 在 `data` / `computed` 中补充类型标签映射**

在 `computed` 中加入：

```js
genreList() {
  const raw = String(this.detail.dianyingleixing || '')
  return raw
    .split(/[、,，/]+/)
    .map(item => item.trim())
    .filter(Boolean)
}
```

- [ ] **Step 5: 如无必要，不修改 `formatHeroMeta` 的返回结构**

保持 helper 中这段结构不变，以避免放大改动范围：

```js
return {
  year: getMovieYear(detail && detail.shangyingshijian),
  runtime: '2h 16m',
  maturityRating: 'PG-13',
  overview: (detail && detail.juqingjianjie) || '暂无剧情简介',
  scoreText: formatScoreText(detail && detail.totalscore),
  releaseDateText: (detail && detail.shangyingshijian) || '上映日期待定',
}
```

- [ ] **Step 6: 运行 helper 测试再次确认通过**

Run:

```bash
node src/main/resources/front/front/scripts/detail-helpers.test.js
```

Expected:

```text
detail-helpers tests passed
```

---

### Task 4: 将 Similar Movies 严格收敛到原型卡片结构

**Files:**
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/detail.vue`
- Test: `src/main/resources/front/front/scripts/detail-helpers.test.js`

- [ ] **Step 1: 先确认相似电影排序测试仍覆盖需求**

确认 `detail-helpers.test.js` 中保留如下断言：

```js
assert.deepStrictEqual(
  similarMovies.map(item => item.id),
  [5, 3, 2],
  '相似电影应排除当前电影，优先同类型，再按点击量和评分排序'
)
```

- [ ] **Step 2: 运行测试确认推荐逻辑基线通过**

Run:

```bash
node src/main/resources/front/front/scripts/detail-helpers.test.js
```

Expected:

```text
detail-helpers tests passed
```

- [ ] **Step 3: 重写 Similar Movies 模板为原型卡片结构**

将当前相似电影区改成接近以下结构：

```vue
<section class="similar-section">
  <div class="section-head">
    <h2>Similar Movies</h2>
  </div>
  <div class="similar-grid" v-if="similarMovies.length">
    <div
      v-for="item in similarMovies"
      :key="item.id"
      class="similar-item"
      @click="openSimilarMovie(item)"
    >
      <div class="similar-cover">
        <img :src="getMovieCover(item)" :alt="item.dianyingmingcheng">
      </div>
      <div class="similar-info">
        <h4>{{ item.dianyingmingcheng }}</h4>
        <div class="similar-meta">
          <span>{{ getMovieYearText(item.shangyingshijian) }}</span>
          <span>{{ formatScore(item.totalscore) }}/5</span>
        </div>
      </div>
    </div>
  </div>
</section>
```

- [ ] **Step 4: 在 `methods` 中补一个年份格式化方法**

加入：

```js
getMovieYearText(dateText) {
  if (!dateText) {
    return '未知年份'
  }
  return String(dateText).slice(0, 4)
}
```

- [ ] **Step 5: 保持 `openSimilarMovie` 路由逻辑兼容现有参数**

保留如下逻辑结构：

```js
openSimilarMovie(item) {
  if (!item || !item.id) {
    return
  }
  const query = { id: item.id }
  if (this.centerType) {
    query.centerType = 1
  }
  if (this.storeupType) {
    query.storeupType = 1
  }
  this.$router.push({ path: '/index/dianyingxinxiDetail', query })
}
```

- [ ] **Step 6: 运行 helper 测试确认 Similar Movies 逻辑未回归**

Run:

```bash
node src/main/resources/front/front/scripts/detail-helpers.test.js
```

Expected:

```text
detail-helpers tests passed
```

---

### Task 5: 收敛下方业务区样式并完成最终验证

**Files:**
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/detail.vue`
- Test: `src/main/resources/front/front/scripts/detail-structure.test.js`
- Test: `src/main/resources/front/front/scripts/detail-helpers.test.js`

- [ ] **Step 1: 保留正文 / 评论 / 点赞踩 / 分享，不再新增原型外组件**

确保模板只保留这些业务区：

```vue
<div ref="detailSection" class="detail-anchor"></div>
<el-tabs class="detail-tabs cinematic-tabs" v-model="activeName" type="border-card" v-if="tabsNum > 0">
  <el-tab-pane label="电影详情" name="first">
    <div class="ql-snow ql-editor detail-html" v-html="detail.dianyingxiangqing"></div>
  </el-tab-pane>
  <el-tab-pane label="评论" name="second">
    ...
  </el-tab-pane>
</el-tabs>
```

并保留分享浮层：

```vue
<div class="share_view">
  ...
</div>
```

- [ ] **Step 2: 将业务区样式收敛到原型的深色层次**

将 `detail.vue` 中相关样式整理为类似以下方向：

```scss
.overview-section,
.similar-section,
.engagement-section {
  background: transparent;
  border: none;
  box-shadow: none;
}

.info-card,
.similar-item,
.comment-item {
  background: rgba(19, 27, 46, 0.9);
  border-radius: 24px;
}

.detail-tabs {
  background: rgba(19, 27, 46, 0.95);
}
```

要求：
- 不使用明显分割线组织主要区块
- 通过背景层级和留白分隔内容
- 深色体系与原型一致

- [ ] **Step 3: 运行结构测试确认多余区块没有回流**

Run:

```bash
node src/main/resources/front/front/scripts/detail-structure.test.js
```

Expected:

```text
detail-structure tests passed
```

- [ ] **Step 4: 运行 helper 测试确认逻辑未回归**

Run:

```bash
node src/main/resources/front/front/scripts/detail-helpers.test.js
```

Expected:

```text
detail-helpers tests passed
```

- [ ] **Step 5: 运行前端构建确认模板语法与样式通过**

Run:

```bash
cd src/main/resources/front/front && npm run build
```

Expected:

```text
DONE  Build complete. The dist directory is ready to be deployed.
```

允许保留项目已有的非阻塞 warning，但不允许新增模板编译错误。

- [ ] **Step 6: 做人工回归清单**

手工检查：

- 详情页首屏结构与 `demo/movie_detail/code.html` 是否一致
- 顶部自定义标题栏是否已删除
- 海报缩略图区是否已删除
- 演员详情区是否已删除
- `Watch Trailer` 是否能滚动到正文
- `Add to My List` 是否能切换收藏状态
- Similar Movies 点击后是否能进入目标详情页
- 评论 / 点赞 / 分享是否仍可使用
- 手机宽度下 Hero、Overview、Similar Movies 是否无明显溢出

---

## Self-Review

### Spec coverage

- 原型严格对齐：Task 2、Task 3、Task 4、Task 5
- 删除顶部栏 / 缩略图 / 演员区 / 额外卡片：Task 2、Task 5
- Overview + 四张信息卡字段映射：Task 3
- Similar Movies 真实数据与真实路由：Task 4
- 现有收藏 / 评论 / 点赞 / 分享逻辑保留：Task 2、Task 4、Task 5

### Placeholder scan

- 无 TBD / TODO
- 所有测试命令、文件路径、预期输出均已明确
- 所有代码步骤都给出具体代码块

### Type consistency

- `heroMeta` 继续复用 `formatHeroMeta()` 输出结构
- `openSimilarMovie()`、`handleStoreupToggle()`、`scrollToDetailSection()` 命名与当前实现保持一致
- `detail-structure.test.js` 与 `detail-helpers.test.js` 的职责分离明确

---

## 执行备注

- 当前目录不是 Git 仓库，因此本计划不包含 commit 步骤。
- 当前前端没有现成组件测试框架，因此结构回归使用最小 Node 源码断言脚本，UI 语法与样式通过 `npm run build` 验证，最终视觉一致性通过人工回归确认。
