# Movie List Prototype Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将前台电影列表页重构为与 `demo/movie_list/code.html` 的主结构、筛选交互和核心动效严格对齐的版本，同时最大化复用现有 `/dianyingxinxi/*` 与 `/dianyingleixing/list` 接口。

**Architecture:** 当前 `list.vue` 已被改造成错误的详情页型结构，本次直接以 `movie_list` 原型为唯一页面骨架来源重写列表页。为保证“年份/评分前端过滤 + 前端分页”可测且稳定，新增一个最小 `list-helpers.js` 提取纯逻辑，并用 Node 断言脚本分别约束结构和筛选逻辑；页面仍复用现有后端列表、分类、热门和详情路由能力。

**Tech Stack:** Vue 2、Element UI、SCSS、Node `assert`、Spring Boot 现有 `/dianyingxinxi/list|page|autoSort|detail` 与 `/dianyingleixing/list`

---

## File Structure

- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/list.vue`
  - 列表页唯一主实现
  - 负责原型结构映射、请求协调、状态管理、动效类名与详情跳转
- Create: `src/main/resources/front/front/src/pages/dianyingxinxi/list-helpers.js`
  - 负责年份提取、评分归一化、筛选、前端分页、年份选项构建等纯逻辑
- Create: `src/main/resources/front/front/scripts/list-helpers.test.js`
  - 纯逻辑回归测试
- Modify: `src/main/resources/front/front/scripts/list-structure.test.js`
  - 改成 `movie_list` 原型结构断言

---

### Task 1: 建立列表筛选纯逻辑回归测试

**Files:**
- Create: `src/main/resources/front/front/scripts/list-helpers.test.js`
- Create: `src/main/resources/front/front/src/pages/dianyingxinxi/list-helpers.js`

- [ ] **Step 1: 先写失败中的 helper 测试脚本**

在 `src/main/resources/front/front/scripts/list-helpers.test.js` 中写入并覆盖这些断言：

```js
const assert = require('assert')
const path = require('path')

const helperPath = path.join(__dirname, '../src/pages/dianyingxinxi/list-helpers.js')
const {
  extractMovieYear,
  normalizeMovieScore,
  buildYearOptions,
  filterMovieList,
  paginateMovieList,
  resolveSortRequest,
} = require(helperPath)

const sampleList = [
  { id: 1, shangyingshijian: '2025-04-02', addtime: '2025-04-03 12:00:00', totalscore: 8.6, dianyingleixing: '动作' },
  { id: 2, shangyingshijian: '', addtime: '2024-03-01 09:00:00', totalscore: 7.2, dianyingleixing: '剧情' },
  { id: 3, shangyingshijian: 'invalid', addtime: '', totalscore: null, dianyingleixing: '动作' },
]

function run() {
  assert.strictEqual(extractMovieYear(sampleList[0]), '2025', '应优先从上映时间提取年份')
  assert.strictEqual(extractMovieYear(sampleList[1]), '2024', '上映时间缺失时应回退 addtime')
  assert.strictEqual(extractMovieYear(sampleList[2]), '未知年份', '无法提取年份时应返回未知年份')

  assert.strictEqual(normalizeMovieScore(8.6), 8.6, '数字评分应保持数值')
  assert.strictEqual(normalizeMovieScore('7.5'), 7.5, '字符串评分应转成数字')
  assert.strictEqual(normalizeMovieScore(null), 0, '空评分过滤时按 0 处理')

  assert.deepStrictEqual(
    buildYearOptions(sampleList),
    ['2025', '2024'],
    '年份选项应去重、排除未知年份并按倒序输出'
  )

  assert.deepStrictEqual(
    filterMovieList(sampleList, { genre: '动作', year: '2025', rating: '8.0+' }).map(item => item.id),
    [1],
    '应能叠加类型、年份和评分筛选'
  )

  assert.deepStrictEqual(
    filterMovieList(sampleList, { genre: 'All', year: 'Any', rating: '9.0+' }).map(item => item.id),
    [],
    '9.0+ 阈值应正确过滤掉不足 9 分的数据'
  )

  assert.deepStrictEqual(
    paginateMovieList([{ id: 1 }, { id: 2 }, { id: 3 }], 2, 2),
    {
      total: 3,
      totalPage: 2,
      pageList: [{ id: 3 }],
    },
    '前端分页应返回总数、总页数和当前页数据'
  )

  assert.deepStrictEqual(
    resolveSortRequest('Popularity'),
    { sort: 'clicknum', order: 'desc' },
    'Popularity 应映射到点击量倒序'
  )

  assert.deepStrictEqual(
    resolveSortRequest('Newest First'),
    { sort: 'addtime', order: 'desc' },
    'Newest First 应映射到新增时间倒序'
  )

  assert.deepStrictEqual(
    resolveSortRequest('Highest Rated'),
    { sort: 'totalscore', order: 'desc' },
    'Highest Rated 应映射到评分倒序'
  )

  assert.deepStrictEqual(
    resolveSortRequest('A-Z'),
    { sort: 'dianyingmingcheng', order: 'asc' },
    'A-Z 应映射到电影名称正序'
  )

  console.log('list-helpers tests passed')
}

run()
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
node src/main/resources/front/front/scripts/list-helpers.test.js
```

Expected:

```text
Error: Cannot find module
```

或 helper 缺失导致的等价失败。

- [ ] **Step 3: 写最小 helper 实现使测试通过**

在 `src/main/resources/front/front/src/pages/dianyingxinxi/list-helpers.js` 中实现：

```js
function extractMovieYear(movie = {}) {
  const primary = String(movie.shangyingshijian || '').match(/\d{4}/)
  if (primary) return primary[0]
  const fallback = String(movie.addtime || '').match(/\d{4}/)
  if (fallback) return fallback[0]
  return '未知年份'
}

function normalizeMovieScore(score) {
  const normalized = Number(score)
  return Number.isFinite(normalized) ? normalized : 0
}

function buildYearOptions(list = []) {
  return Array.from(
    new Set(list.map(item => extractMovieYear(item)).filter(year => year !== '未知年份'))
  ).sort((a, b) => Number(b) - Number(a))
}

function filterMovieList(list = [], filters = {}) {
  return list.filter(item => {
    const matchesGenre = !filters.genre || filters.genre === 'All' || item.dianyingleixing === filters.genre
    const movieYear = extractMovieYear(item)
    const matchesYear = !filters.year || filters.year === 'Any' || movieYear === filters.year
    const score = normalizeMovieScore(item.totalscore)
    const matchesRating =
      !filters.rating ||
      filters.rating === 'Any' ||
      (filters.rating === '9.0+' && score >= 9) ||
      (filters.rating === '8.0+' && score >= 8) ||
      (filters.rating === '7.0+' && score >= 7)
    return matchesGenre && matchesYear && matchesRating
  })
}

function paginateMovieList(list = [], page = 1, pageSize = 24) {
  const safePageSize = pageSize > 0 ? pageSize : 24
  const total = list.length
  const totalPage = total === 0 ? 0 : Math.ceil(total / safePageSize)
  const safePage = totalPage === 0 ? 1 : Math.min(Math.max(page, 1), totalPage)
  const start = (safePage - 1) * safePageSize
  return {
    total,
    totalPage,
    pageList: list.slice(start, start + safePageSize),
  }
}

function resolveSortRequest(sortBy = 'Popularity') {
  const mapping = {
    Popularity: { sort: 'clicknum', order: 'desc' },
    'Newest First': { sort: 'addtime', order: 'desc' },
    'Highest Rated': { sort: 'totalscore', order: 'desc' },
    'A-Z': { sort: 'dianyingmingcheng', order: 'asc' },
  }
  return mapping[sortBy] || mapping.Popularity
}
```

要求：

- `extractMovieYear()` 按 spec 中的优先级与规则解析
- `normalizeMovieScore()` 将空值/非法值统一为 `0`
- `filterMovieList()` 支持 `genre/year/rating`
- `paginateMovieList()` 只做纯前端切片，不依赖外部状态
- `resolveSortRequest()` 固定支持：
  - `Popularity` -> `{ sort: 'clicknum', order: 'desc' }`
  - `Newest First` -> `{ sort: 'addtime', order: 'desc' }`
  - `Highest Rated` -> `{ sort: 'totalscore', order: 'desc' }`
  - `A-Z` -> `{ sort: 'dianyingmingcheng', order: 'asc' }`
- 模块方式固定使用 CommonJS
  - `list-helpers.js` 只使用 `module.exports = { extractMovieYear, normalizeMovieScore, buildYearOptions, filterMovieList, paginateMovieList, resolveSortRequest }`
  - `list.vue` 通过 `require('./list-helpers')` 引入
  - 不再使用 ESModule `export`

- [ ] **Step 4: 运行 helper 测试确认通过**

Run:

```bash
node src/main/resources/front/front/scripts/list-helpers.test.js
```

Expected:

```text
list-helpers tests passed
```

---

### Task 2: 把结构回归测试切换到 `movie_list` 原型

**Files:**
- Modify: `src/main/resources/front/front/scripts/list-structure.test.js`
- Reference: `demo/movie_list/code.html`
- Reference: `src/main/resources/front/front/src/pages/dianyingxinxi/list.vue`

- [ ] **Step 1: 先把结构测试改成新的原型断言**

将 `list-structure.test.js` 改成断言这些结构必须存在：

```js
assert.ok(has('Explore Our Collection'), '应保留原型标题区')
assert.ok(has('class="topbar"') || has('class="top-nav"') || has('class="discover-topbar"'), '应保留顶部导航骨架')
assert.ok(has('Quick View'), '应保留 Quick View 按钮文案')
assert.ok(has('Genre'), '应保留 Genre 筛选')
assert.ok(has('filter-chip-genre'), '应保留 Genre 筛选容器类名')
assert.ok(has('Release'), '应保留 Release 筛选')
assert.ok(has('filter-chip-release'), '应保留 Release 筛选容器类名')
assert.ok(has('Rating'), '应保留 Rating 筛选')
assert.ok(has('filter-chip-rating'), '应保留 Rating 筛选容器类名')
assert.ok(has('Sort By'), '应保留排序筛选')
assert.ok(has('filter-chip-sort'), '应保留排序筛选容器类名')
assert.ok(has('class="movie-grid"') || has('class="poster-grid"'), '应保留纯海报网格')
assert.ok(has('class="pagination-shell"') || has('class="movie-pagination"'), '应保留原型风格分页容器')
```

并断言当前错误方向结构必须删除：

```js
assert.ok(!has('Overview'), '不应保留详情页式 Overview 区')
assert.ok(!has('Similar Movies'), '不应保留详情页式 Similar Movies 区')
assert.ok(!has('Watch Trailer'), '不应保留详情页式 Watch Trailer CTA')
assert.ok(!has('Movie Library'), '不应保留当前业务区标题')
assert.ok(!has('class="business-layout"'), '不应保留当前双栏业务布局')
```

- [ ] **Step 2: 运行结构测试确认失败**

Run:

```bash
node src/main/resources/front/front/scripts/list-structure.test.js
```

Expected:

```text
AssertionError [ERR_ASSERTION]: 应保留原型标题区
```

或任一与当前错误布局相关的失败。

---

### Task 3: 重写列表页数据模型与接口编排

**Files:**
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/list.vue`
- Create: `src/main/resources/front/front/src/pages/dianyingxinxi/list-helpers.js`
- Test: `src/main/resources/front/front/scripts/list-helpers.test.js`

- [ ] **Step 1: 在列表页中引入新的纯逻辑 helper**

从 `list-helpers.js` 通过 CommonJS 导入：

```js
const {
  extractMovieYear,
  normalizeMovieScore,
  buildYearOptions,
  filterMovieList,
  paginateMovieList,
  resolveSortRequest,
} = require('./list-helpers')
```

继续保留并复用现有海报能力：

```js
const { getPrimaryPoster } = require('./detail-helpers')
```

- [ ] **Step 2: 在 `data()` 中补齐新的页面状态**

新增并保留这些核心状态：

```js
filters: {
  keyword: '',
  genre: 'All',
  year: 'Any',
  rating: 'Any',
  sortBy: 'Popularity',
},
fenlei: [],
feileiColumn: 'dianyingleixing',
rawList: [],
filteredList: [],
pageList: [],
yearOptions: [],
page: 1,
pageSize: 24,
total: 0,
totalPage: 0,
loading: false,
loadError: '',
```

同时删除当前错误方向状态：

- `featuredMovie` 依赖链对应的数据准备逻辑
- `similarMovieList`
- `posterBoardList`
- 旧的 `formSearch.daoyan/zhuyan`
- 旧的“详情页型”区块状态

- [ ] **Step 3: 补齐分类数据请求与页面初始化**

新增独立分类加载方法，例如：

```js
async fetchCategories() {
  try {
    const res = await this.$http.get('dianyingleixing/list', {
      params: {
        page: 1,
        limit: 1000,
      },
    })
    this.fenlei = (res.data.data && res.data.data.list) || []
  } catch (error) {
    this.fenlei = []
  }
}
```

并在页面初始化中显式串联：

```js
async mounted() {
  await this.fetchCategories()
  await this.fetchRawList()
}
```

要求：

- `feileiColumn` 固定初始化为 `'dianyingleixing'`
- 页面初始化时先执行 `fetchCategories()`
- `fetchCategories()` 成功时写入 `this.fenlei = res.data.data.list || []`
- `fetchCategories()` 失败时必须回退为 `this.fenlei = []`
- 分类请求只负责筛选项来源，不参与年份/评分计算

- [ ] **Step 4: 固定基础列表请求协议**

将列表请求重构为一个明确方法，例如：

```js
async fetchRawList() {
  const endpoint = this.centerType ? 'dianyingxinxi/page' : 'dianyingxinxi/list'
  // resolveSortRequest() 只能返回以下四组固定映射：
  // Popularity -> clicknum desc
  // Newest First -> addtime desc
  // Highest Rated -> totalscore desc
  // A-Z -> dianyingmingcheng asc
  const sortRequest = resolveSortRequest(this.filters.sortBy)
  const params = {
    page: 1,
    limit: 1000,
    sort: sortRequest.sort,
    order: sortRequest.order,
  }
  if (this.filters.keyword) {
    params.dianyingmingcheng = `%${this.filters.keyword}%`
  }
  if (this.filters.genre !== 'All') {
    params.dianyingleixing = this.filters.genre
  }
  this.loading = true
  try {
    const res = await this.$http.get(endpoint, { params })
    // 已核对后端 controller，/list 与 /page 都返回 R.ok().put("data", PageUtils)
    // 因此前端固定按 res.data.data.list 取列表即可，不需要双分支猜测响应结构
    const responseData = res.data.data || {}
    this.rawList = responseData.list || []
    this.loadError = ''
    this.refreshDerivedList()
  } catch (error) {
    this.rawList = []
    this.filteredList = []
    this.pageList = []
    this.total = 0
    this.totalPage = 0
    this.loadError = '电影列表加载失败，请稍后重试'
  } finally {
    this.loading = false
  }
}
```

要求：

- 排序切换时必须重新请求后端
- 已核对 `DianyingxinxiController`，`/dianyingxinxi/list` 与 `/dianyingxinxi/page` 都固定按 `res.data.data.list` 读取结果
- `keyword` 固定映射 `dianyingmingcheng`
- `genre` 固定映射 `dianyingleixing`
- 成功后立即执行 `refreshDerivedList()`
- 失败时必须清空 `rawList / filteredList / pageList / total / totalPage`
- 失败时写入 `loadError`
- 请求中维护 `loading`
- 排序映射必须固定为：
  - `Popularity` -> `clicknum desc`
  - `Newest First` -> `addtime desc`
  - `Highest Rated` -> `totalscore desc`
  - `A-Z` -> `dianyingmingcheng asc`

- [ ] **Step 5: 将年份/评分过滤与前端分页收敛到单个刷新流程**

新增统一刷新方法，例如：

```js
refreshDerivedList() {
  this.yearOptions = buildYearOptions(this.rawList)
  this.filteredList = filterMovieList(this.rawList, {
    genre: this.filters.genre,
    year: this.filters.year,
    rating: this.filters.rating,
  })
  const pageData = paginateMovieList(this.filteredList, this.page, this.pageSize)
  this.total = pageData.total
  this.totalPage = pageData.totalPage
  this.pageList = pageData.pageList
}
```

要求：

- `year/rating` 只在前端过滤
- 页面总数与分页严格以 `filteredList` 为准
- 若切换筛选导致当前页超界，应自动回到第一页再切片

- [ ] **Step 6: 明确筛选与分页事件分流**

将页面交互触发逻辑固定为以下三类：

```js
onKeywordSubmit() {
  this.page = 1
  this.fetchRawList()
}

onGenreChange() {
  this.page = 1
  this.fetchRawList()
}

onSortChange() {
  this.page = 1
  this.fetchRawList()
}

onYearChange() {
  this.page = 1
  this.refreshDerivedList()
}

onRatingChange() {
  this.page = 1
  this.refreshDerivedList()
}

onPageChange(page) {
  this.page = page
  this.refreshDerivedList()
}
```

要求：

- `keyword / genre / sortBy` 变化后统一走 `page = 1 + fetchRawList()`
- `year / rating` 变化后统一走 `page = 1 + refreshDerivedList()`
- 分页切换只更新 `page`，随后执行 `refreshDerivedList()`
- 初始化流程固定为：`await fetchCategories()` 后再执行 `fetchRawList()`

- [ ] **Step 7: 运行 helper 测试确认逻辑层仍然通过**

Run:

```bash
node src/main/resources/front/front/scripts/list-helpers.test.js
```

Expected:

```text
list-helpers tests passed
```

---

### Task 4: 将模板严格改成 `movie_list` 原型结构

**Files:**
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/list.vue`
- Test: `src/main/resources/front/front/scripts/list-structure.test.js`

- [ ] **Step 1: 先删除当前详情页化区块**

从模板中删除这些大区块：

- Hero 首屏详情结构
- Overview 区块
- Similar Movies 区块
- 右侧热门信息侧栏
- 当前业务区标题与双栏布局

- [ ] **Step 2: 搭建原型化顶部导航与标题区**

在模板顶部加入：

- sticky 顶部导航栏
- `Discover / Library / Watchlist`
- 顶部搜索框
- 通知与头像视觉占位
- 面包屑
- `Explore Our Collection` 标题

要求：

- 顶部搜索绑定 `filters.keyword`
- 搜索提交后调用基础列表重新请求
- `Discover` 作为当前激活态

- [ ] **Step 3: 搭建原型化四项筛选条**

筛选条只保留 4 项：

- `Genre`
- `Release`
- `Rating`
- `Sort By`

推荐使用 `el-select` 或原生 `select`，但 DOM 结构与样式要贴近原型的一行胶囊条。

要求：

- 类型选项来源于 `fenlei`
- 类型下拉必须显式使用 `v-for="item in fenlei"` 渲染，并以 `item[feileiColumn]` 作为 `label/value`
- 年份选项来源于 `yearOptions`
- 评分选项固定为 `Any / 9.0+ / 8.0+ / 7.0+`
- 排序选项固定写死为：
  - `Popularity`
  - `Newest First`
  - `Highest Rated`
  - `A-Z`

- [ ] **Step 4: 搭建纯海报网格与 `Quick View`**

网格区改成原型卡片结构：

```vue
<section class="movie-grid">
  <article v-for="item in pageList" :key="item.id" class="poster-card group" @click="toDetail(item)">
    <div class="poster-media">
      <img :src="getMovieCover(item)" :alt="item.dianyingmingcheng">
      <div class="poster-overlay">
        <button class="quick-view-btn" @click.stop="toDetail(item)">Quick View</button>
      </div>
    </div>
    <div class="poster-meta">
      <h3>{{ item.dianyingmingcheng }}</h3>
      <div class="meta-row">
        <span>{{ extractMovieYear(item) }}</span>
        <span>{{ formatCardScore(item.totalscore) }}</span>
      </div>
    </div>
  </article>
</section>
```

要求：

- 不再渲染导演/主演/简介/业务 badge
- `Quick View` 与卡片点击都进入详情页
- `totalscore` 空值显示 `N/A`
- 模板中用到的辅助函数需要在 `methods` 或 `computed` 中显式暴露
  - `getMovieCover()` 继续基于 `getPrimaryPoster()` 包装
  - `formatCardScore()` 在页面内显式定义，负责把空值显示成 `N/A`

- [ ] **Step 5: 搭建原型风格分页与状态区**

页面底部加入：

- 原型风格分页
- loading 状态
- 空状态
- 错误提示区

要求：

- 分页按钮操作前端 `page`
- 当前页数据来自 `pageList`
- 无结果时显示空状态而不是空白
- 接口失败时有错误文案

- [ ] **Step 6: 运行结构测试确认通过**

Run:

```bash
node src/main/resources/front/front/scripts/list-structure.test.js
```

Expected:

```text
list-structure tests passed
```

---

### Task 5: 对齐动效、响应式和最终验证

**Files:**
- Modify: `src/main/resources/front/front/src/pages/dianyingxinxi/list.vue`
- Test: `src/main/resources/front/front/scripts/list-helpers.test.js`
- Test: `src/main/resources/front/front/scripts/list-structure.test.js`

- [ ] **Step 1: 将样式收敛到 `movie_list` 原型视觉语言**

样式方向必须对齐：

- 深色影院背景
- 顶栏玻璃态
- 海报卡圆角 + 柔和阴影
- 金色强调色
- 无明显分割线主导布局

- [ ] **Step 2: 实现卡片 hover 动效**

为卡片与按钮加入：

- `scale`
- `translateY`
- 阴影增强
- 遮罩渐显
- `Quick View` 滑入

要求：

- 桌面端 hover 明显
- 移动端不依赖 hover 才能使用

- [ ] **Step 3: 实现导航、筛选条与分页的过渡反馈**

包括：

- 导航 hover 与当前态
- 筛选条激活态
- 分页 hover 与当前页高亮

- [ ] **Step 4: 运行所有回归测试**

Run:

```bash
node src/main/resources/front/front/scripts/list-helpers.test.js
node src/main/resources/front/front/scripts/list-structure.test.js
```

Expected:

```text
list-helpers tests passed
list-structure tests passed
```

- [ ] **Step 5: 运行前端构建**

Run:

```bash
cd src/main/resources/front/front && npm run build
```

Expected:

```text
DONE  Build complete. The dist directory is ready to be deployed.
```

允许保留项目已有 warning，但不允许新增模板编译错误。

- [ ] **Step 6: 做人工回归清单**

手工检查：

- `/index/dianyingxinxi` 主结构是否与 `demo/movie_list/code.html` 一致
- 顶栏、面包屑、标题区是否正确
- 四项主筛选是否可用
- 导航搜索是否可触发电影名称查询
- `Quick View` 是否进入详情页
- hover 动效是否完整
- 空状态 / 错误态 / loading 是否存在
- 手机宽度下卡片与筛选条是否无明显溢出

---

## Self-Review

### Spec coverage

- 原型主结构切换：Task 2、Task 4、Task 5
- 主筛选收敛为 4 项：Task 3、Task 4
- 复用现有后端 API：Task 3
- 前端年份/评分过滤：Task 1、Task 3
- `Quick View` 进入详情页：Task 4
- 动效整体对齐：Task 5

### Placeholder scan

- 无 TBD / TODO
- 所有新增文件、命令、断言目标均已明确
- 已明确当前目录不是 Git 仓库，因此本计划不包含 commit 步骤

### Type consistency

- `keyword` 固定映射 `dianyingmingcheng`
- `genre` 固定映射 `dianyingleixing`
- `year/rating` 固定前端过滤
- 排序切换固定重新请求后端

---

## 执行备注

- 当前目录不是 Git 仓库，因此本计划不包含 commit 步骤。
- 当前页此前已经被改成错误方向结构，执行时应直接重写为 `movie_list` 原型骨架，不要尝试在旧结构上继续叠补丁。
