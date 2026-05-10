<template>
  <div class="discover-page">
    <div v-if="centerType" class="back-box">
      <el-button class="back-button" size="mini" @click="backClick">
        <span class="icon iconfont icon-jiantou33"></span>
        <span class="text">返回</span>
      </el-button>
    </div>

    <main class="discover-main">
      <section class="page-heading">
        <nav class="breadcrumb">
          <span>首页</span>
          <span class="breadcrumb-sep">/</span>
          <span class="current">影片探索</span>
        </nav>

        <div class="heading-copy">
          <p class="eyebrow">Explore Our Collection</p>
          <h1>影片探索</h1>
          <p class="sr-only">类型 上映时间 评分 排序方式</p>
          <p class="heading-desc">
            按类型、年份、评分和排序快速筛选，只保留纯海报视图。
          </p>
        </div>

        <div class="heading-summary">
          <span class="summary-label">可见影片</span>
          <strong>{{ total }}</strong>
        </div>
      </section>

      <section class="filter-bar">
        <div class="filter-item filter-chip-genre" :class="{ 'is-active': filters.genre !== '全部' }">
          <label>类型 Genre</label>
          <p v-if="categoryLoadError" class="filter-hint error-text">{{ categoryLoadError }}</p>
          <div class="select-shell">
            <el-select
              class="discover-select"
              v-model="filters.genre"
              :disabled="!!categoryLoadError"
              popper-class="discover-select-popper"
              @change="handleGenreChange"
            >
              <el-option label="全部" value="全部"></el-option>
              <el-option
                v-for="item in categoryOptions"
                :key="item"
                :label="item"
                :value="item"
              ></el-option>
            </el-select>
          </div>
        </div>

        <div class="filter-item filter-chip-release" :class="{ 'is-active': filters.year !== '不限' }">
          <label>上映时间 Release</label>
          <div class="select-shell">
            <el-select
              class="discover-select"
              v-model="filters.year"
              popper-class="discover-select-popper"
              @change="handleYearChange"
            >
              <el-option label="不限" value="不限"></el-option>
              <el-option
                v-for="item in yearOptions"
                :key="item"
                :label="item"
                :value="item"
              ></el-option>
            </el-select>
          </div>
        </div>

        <div class="filter-item filter-chip-rating" :class="{ 'is-active': filters.rating !== '不限' }">
          <label>评分 Rating</label>
          <div class="select-shell">
            <el-select
              class="discover-select"
              v-model="filters.rating"
              popper-class="discover-select-popper"
              @change="handleRatingChange"
            >
              <el-option
                v-for="item in ratingOptions"
                :key="item"
                :label="item"
                :value="item"
              ></el-option>
            </el-select>
          </div>
        </div>

        <div class="filter-item filter-chip-sort" :class="{ 'is-active': filters.sortBy !== '热门优先' }">
          <label>排序方式 Sort By</label>
          <div class="select-shell">
            <el-select
              class="discover-select"
              v-model="filters.sortBy"
              popper-class="discover-select-popper"
              @change="handleSortChange"
            >
              <el-option
                v-for="item in sortOptions"
                :key="item"
                :label="item"
                :value="item"
              ></el-option>
            </el-select>
          </div>
        </div>
      </section>

      <section class="status-panel">
        <div class="status-copy">
          <span class="status-title">精选海报墙</span>
          <span class="status-subtitle">
            第 {{ page }} / {{ totalPage || 1 }} 页
          </span>
        </div>
        <div class="status-tags">
          <span class="status-tag">{{ filters.genre }}</span>
          <span class="status-tag">{{ filters.year }}</span>
          <span class="status-tag">{{ filters.rating }}</span>
          <span class="status-tag">{{ filters.sortBy }}</span>
        </div>
      </section>

      <section class="poster-grid">
        <div v-if="loading" class="state-card">正在加载精选影片...</div>
        <div v-else-if="loadError" class="state-card error-state">{{ loadError }}</div>
        <div v-else-if="!pageList.length" class="state-card">暂无符合当前筛选条件的电影。</div>
        <template v-else>
          <article
            v-for="item in pageList"
            :key="item.id"
            class="poster-card"
            role="button"
            tabindex="0"
            :aria-label="`打开${item.dianyingmingcheng || '未命名电影'}详情`"
            @click="toDetail(item)"
            @keydown="handlePosterCardKeydown($event, item)"
          >
            <div class="poster-media">
              <img :src="getMovieCover(item)" :alt="item.dianyingmingcheng">
              <div class="poster-overlay">
                <button class="quick-view-btn" @click.stop="toDetail(item)">Quick View</button>
              </div>
            </div>

            <div class="poster-meta">
              <h3>{{ item.dianyingmingcheng || '未命名电影' }}</h3>
              <div class="meta-row">
                <span>{{ extractMovieYearLabel(item) }}</span>
                <span>{{ formatCardScore(item.totalscore) }}</span>
              </div>
            </div>
          </article>
        </template>
      </section>

      <nav class="movie-pagination">
        <button
          type="button"
          class="page-button"
          :disabled="page <= 1 || loading"
          @click="changePage(page - 1)"
        >
          上一页
        </button>

        <div class="page-indicator">
          <span class="page-current">{{ page }}</span>
          <span class="page-divider">/</span>
          <span class="page-total">{{ totalPage || 1 }}</span>
        </div>

        <button
          type="button"
          class="page-button"
          :disabled="page >= totalPage || !totalPage || loading"
          @click="changePage(page + 1)"
        >
          下一页
        </button>
      </nav>
    </main>
  </div>
</template>

<script>
const {
  extractMovieYear,
  normalizeMovieScore,
  buildYearOptions,
  filterMovieList,
  paginateMovieList,
  resolveSortRequest,
} = require('./list-helpers')
const {
  extractAppMovieList,
  getPrimaryPoster,
  normalizeAppMovieList,
} = require('./detail-helpers')

const RATING_OPTIONS = ['不限', '9.0+', '8.0+', '7.0+']
const SORT_OPTIONS = ['热门优先', '最新上映', '高分优先', '名称排序']

function normalizeCenterType(value) {
  return !!(value && value !== 0 && value !== '0')
}

function buildLoadErrorMessage(error) {
  if (error && error.response && error.response.data && error.response.data.msg) {
    return error.response.data.msg
  }
  if (error && error.message) {
    return error.message
  }
  return '电影列表加载失败，请稍后重试。'
}

function normalizeAppSortField(field) {
  const fieldMap = {
    clicknum: 'clickCount',
    shangyingshijian: 'releaseDate',
    addtime: 'releaseDate',
    totalscore: 'totalScore',
    dianyingmingcheng: 'title',
  }
  return fieldMap[field] || field
}

export default {
  data() {
    return {
      filters: {
        keyword: '',
        genre: '全部',
        year: '不限',
        rating: '不限',
        sortBy: '热门优先',
      },
      fenlei: [],
      feileiColumn: 'typeName',
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
      categoryLoadError: '',
      centerType: false,
      baseUrl: '',
      fallbackPoster: require('@/assets/chapter.jpg'),
      ratingOptions: RATING_OPTIONS,
      sortOptions: SORT_OPTIONS,
      rawListRequestToken: 0,
    }
  },
  computed: {
    categoryOptions() {
      return Array.from(
        new Set(
          this.fenlei
            .map(item => {
              if (typeof item === 'string') {
                return item.trim()
              }
              const value = item && (item[this.feileiColumn] || item.name || item.dianyingleixing)
              return value ? String(value).trim() : ''
            })
            .filter(Boolean)
        )
      )
    },
  },
  async created() {
    this.baseUrl = this.$config.baseUrl
    this.syncRouteState(this.$route)
    await this.fetchCategories()
    this.fetchRawList()
  },
  watch: {
    $route(newRoute) {
      this.syncRouteState(newRoute)
      this.fetchRawList()
    },
  },
  methods: {
    syncRouteState(route) {
      const query = (route && route.query) || {}
      this.centerType = normalizeCenterType(query.centerType)
      this.filters.keyword = query.indexQueryCondition ? String(query.indexQueryCondition) : ''
      this.filters.genre = query.homeFenlei ? String(query.homeFenlei) : '全部'
      this.filters.year = '不限'
      this.filters.rating = '不限'
      this.filters.sortBy = '热门优先'
      this.page = 1
    },
    async fetchCategories() {
      try {
        const res = await this.$http.get('appmovie/types')
        if (res.data && res.data.code !== undefined && res.data.code != 0) {
          throw new Error((res.data && res.data.msg) || '电影分类加载失败，请稍后重试。')
        }
        this.fenlei = extractAppMovieList((res.data && res.data.data) || res.data || {})
        this.categoryLoadError = ''
      } catch (error) {
        this.fenlei = []
        this.categoryLoadError = buildLoadErrorMessage(error)
      }
    },
    async fetchRawList() {
      const requestToken = ++this.rawListRequestToken
      this.loading = true
      this.loadError = ''
      const sortRequest = resolveSortRequest(this.filters.sortBy)
      const params = {
        page: 1,
        limit: 1000,
        sort: normalizeAppSortField(sortRequest.sort),
        order: sortRequest.order,
      }

      if (this.filters.keyword) {
        params.title = this.filters.keyword
      }
      if (this.filters.genre !== '全部') {
        params.typeName = this.filters.genre
      }

      try {
        const res = await this.$http.get('appmovie/front/list', {
          params,
        })
        if (requestToken !== this.rawListRequestToken) {
          return
        }
        if (res.data && res.data.code !== undefined && res.data.code != 0) {
          throw new Error((res.data && res.data.msg) || '电影列表加载失败，请稍后重试。')
        }
        this.rawList = normalizeAppMovieList(extractAppMovieList((res.data && res.data.data) || res.data || {}))
        this.loadError = ''
        this.refreshDerivedList()
      } catch (error) {
        if (requestToken !== this.rawListRequestToken) {
          return
        }
        this.rawList = []
        this.yearOptions = []
        this.filteredList = []
        this.pageList = []
        this.total = 0
        this.totalPage = 0
        this.loadError = buildLoadErrorMessage(error)
      } finally {
        if (requestToken === this.rawListRequestToken) {
          this.loading = false
        }
      }
    },
    refreshDerivedList() {
      this.yearOptions = buildYearOptions(this.rawList)
      this.filteredList = filterMovieList(this.rawList, {
        year: this.filters.year,
        rating: this.filters.rating,
      })

      let pagination = paginateMovieList(this.filteredList, this.page, this.pageSize)
      if (pagination.totalPage > 0 && this.page > pagination.totalPage) {
        this.page = 1
        pagination = paginateMovieList(this.filteredList, this.page, this.pageSize)
      }

      this.total = pagination.total
      this.totalPage = pagination.totalPage
      this.pageList = pagination.pageList
    },
    handleGenreChange() {
      this.page = 1
      this.fetchRawList()
    },
    handleSortChange() {
      this.page = 1
      this.fetchRawList()
    },
    handleYearChange() {
      this.page = 1
      this.refreshDerivedList()
    },
    handleRatingChange() {
      this.page = 1
      this.refreshDerivedList()
    },
    changePage(nextPage) {
      if (nextPage < 1) {
        return
      }
      this.page = nextPage
      this.refreshDerivedList()
    },
    getMovieCover(item) {
      if (!item) {
        return this.fallbackPoster
      }
      return getPrimaryPoster(item, this.baseUrl) || this.fallbackPoster
    },
    extractMovieYearLabel(item) {
      return extractMovieYear(item)
    },
    formatCardScore(score) {
      if (score === '' || score === null || score === undefined) {
        return '暂无'
      }
      const parsedScore = Number(score)
      if (!Number.isFinite(parsedScore)) {
        return '暂无'
      }
      const numericScore = normalizeMovieScore(score)
      return Number.isFinite(numericScore) ? numericScore.toFixed(1) : '暂无'
    },
    handlePosterCardKeydown(event, item) {
      if (!event) {
        return
      }
      const key = event.key
      if (key === 'Enter' || key === ' ' || key === 'Spacebar') {
        event.preventDefault()
        this.toDetail(item)
      }
    },
    toDetail(item) {
      const query = {
        id: item.id,
      }
      if (this.centerType) {
        query.centerType = 1
      }
      this.$router.push({
        path: '/index/dianyingxinxiDetail',
        query,
      })
    },
    backClick() {
      this.$router.push({ path: '/index/center' })
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.discover-page {
  --page-bg: #0b1326;
  --surface-low: rgba(19, 27, 46, 0.9);
  --surface-high: rgba(34, 42, 61, 0.94);
  --surface-card: rgba(6, 14, 32, 0.94);
  --text-main: #dae2fd;
  --text-muted: rgba(218, 226, 253, 0.7);
  --text-soft: #d0c5af;
  --accent: #ffc639;
  --accent-deep: #e1aa12;
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(255, 198, 57, 0.16), transparent 28%),
    radial-gradient(circle at top right, rgba(73, 108, 170, 0.18), transparent 30%),
    linear-gradient(180deg, #0b1326 0%, #08101d 100%);
  color: var(--text-main);
  font-family: 'Manrope', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.back-box {
  position: fixed;
  top: 18px;
  left: 18px;
  z-index: 60;
}

.back-button {
  border-radius: 999px;
  border: 1px solid rgba(153, 144, 124, 0.22);
  background: rgba(11, 19, 38, 0.82);
  color: var(--text-main);
  backdrop-filter: blur(16px);
}

.discover-main {
  max-width: 1520px;
  margin: 0 auto;
  padding: 48px 32px 96px;
}

.page-heading {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 24px;
  align-items: end;
  margin-bottom: 36px;
}

.breadcrumb {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-soft);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.breadcrumb .current {
  color: var(--accent);
}

.heading-copy {
  max-width: 820px;
}

.eyebrow {
  margin: 0 0 14px;
  color: var(--text-soft);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.page-heading h1 {
  margin: 0;
  font-family: 'Epilogue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 60px;
  line-height: 0.96;
  font-weight: 900;
  letter-spacing: -0.05em;
}

.heading-desc {
  margin: 18px 0 0;
  color: var(--text-muted);
  font-size: 16px;
  line-height: 1.8;
}

.heading-summary,
.status-panel,
.filter-bar {
  background: var(--surface-low);
}

.heading-summary {
  min-width: 168px;
  padding: 22px 24px;
  border-radius: 28px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-label,
.status-title {
  color: var(--text-soft);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.heading-summary strong {
  font-size: 34px;
  line-height: 1;
  color: var(--accent);
}

.filter-bar {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  padding: 14px;
  border-radius: 32px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
  padding: 10px;
  border-radius: 22px;
  border: 1px solid transparent;
  transition: border-color 0.24s ease, background-color 0.24s ease, transform 0.24s ease;
}

.filter-item:hover {
  border-color: rgba(255, 198, 57, 0.22);
  background: rgba(255, 255, 255, 0.02);
}

.filter-item.is-active {
  border-color: rgba(255, 198, 57, 0.34);
  background: rgba(255, 198, 57, 0.06);
}

.filter-hint {
  margin: -4px 0 0;
  padding-left: 12px;
  font-size: 12px;
  line-height: 1.4;
}

.error-text {
  color: #ffb4ab;
}

.filter-item label {
  padding-left: 12px;
  color: var(--text-soft);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.select-shell {
  position: relative;
  min-width: 0;
}

.select-shell::before {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: 999px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.02)),
    linear-gradient(180deg, rgba(40, 50, 72, 0.96), rgba(24, 32, 50, 0.96));
  pointer-events: none;
}

.select-shell::after {
  content: '';
  position: absolute;
  inset: -1px;
  border-radius: 999px;
  border: 1px solid rgba(255, 198, 57, 0.2);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    0 10px 24px rgba(4, 8, 20, 0.32);
  pointer-events: none;
  transition: border-color 0.24s ease, box-shadow 0.24s ease, opacity 0.24s ease;
}

.select-shell:hover::after {
  border-color: rgba(255, 198, 57, 0.42);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.05),
    0 14px 30px rgba(4, 8, 20, 0.4),
    0 0 0 3px rgba(255, 198, 57, 0.06);
}

.select-shell:focus-within::after {
  border-color: rgba(255, 198, 57, 0.72);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.06),
    0 16px 34px rgba(4, 8, 20, 0.42),
    0 0 0 4px rgba(255, 198, 57, 0.12);
}

.discover-select {
  position: relative;
  z-index: 1;
  display: block;
}

.discover-select ::v-deep .el-input__inner {
  height: 54px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--text-main);
  padding: 0 52px 0 20px;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 0.01em;
}

.discover-select ::v-deep .el-input__inner::placeholder {
  color: rgba(218, 226, 253, 0.42);
}

.discover-select ::v-deep .el-input.is-disabled .el-input__inner {
  background: transparent;
  color: rgba(218, 226, 253, 0.42);
  cursor: not-allowed;
}

.discover-select ::v-deep .el-input__suffix {
  right: 16px;
  display: flex;
  align-items: center;
}

.discover-select ::v-deep .el-input__suffix-inner {
  display: flex;
  align-items: center;
}

.discover-select ::v-deep .el-select__caret {
  color: var(--accent);
  font-size: 15px;
  font-weight: 700;
  transition: transform 0.24s ease, color 0.24s ease;
}

.discover-select ::v-deep .is-focus .el-select__caret,
.select-shell:hover .discover-select ::v-deep .el-select__caret {
  color: #ffd977;
}

.discover-select ::v-deep .el-input.is-focus .el-input__inner {
  border: 0;
}

.discover-select ::v-deep .el-select__caret.is-reverse {
  transform: rotateZ(180deg);
}

.status-panel {
  margin-top: 22px;
  margin-bottom: 28px;
  border-radius: 28px;
  padding: 18px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.status-copy {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.status-subtitle {
  color: var(--text-muted);
  font-size: 14px;
}

.status-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.status-tag,
.meta-row span {
  border-radius: 999px;
  background: rgba(34, 42, 61, 0.92);
  color: var(--text-main);
}

.status-tag {
  padding: 8px 14px;
  font-size: 12px;
  font-weight: 700;
}

.poster-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 30px 22px;
  min-height: 260px;
}

.poster-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  cursor: pointer;
  animation: card-reveal 0.46s ease both;
  transition: transform 0.3s ease, filter 0.3s ease;
}

.poster-card:hover {
  transform: translateY(-8px) scale(1.02);
}

.poster-card:focus-visible {
  outline: 2px solid rgba(255, 198, 57, 0.92);
  outline-offset: 4px;
  transform: translateY(-4px) scale(1.01);
}

.poster-media {
  position: relative;
  aspect-ratio: 2 / 3;
  border-radius: 24px;
  overflow: hidden;
  background: var(--surface-card);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.45);
  transition: box-shadow 0.3s ease;
}

.poster-card:hover .poster-media {
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.58);
}

.poster-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.poster-card:hover .poster-media img {
  transform: scale(1.06);
}

.poster-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(180deg, rgba(11, 19, 38, 0.02), rgba(11, 19, 38, 0.86));
  opacity: 0;
  transition: opacity 0.28s ease, background 0.28s ease;
}

.poster-card:hover .poster-overlay,
.poster-card:focus-within .poster-overlay {
  opacity: 1;
  background: linear-gradient(180deg, rgba(11, 19, 38, 0.08), rgba(11, 19, 38, 0.92));
}

.quick-view-btn {
  min-width: 132px;
  height: 42px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--accent), var(--accent-deep));
  color: #3f2e00;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  transform: translateY(12px);
  opacity: 0.9;
  box-shadow: 0 10px 28px rgba(225, 170, 18, 0.36);
  transition: transform 0.28s ease, opacity 0.28s ease, box-shadow 0.28s ease;
  cursor: pointer;
}

.poster-card:hover .quick-view-btn,
.poster-card:focus-within .quick-view-btn {
  transform: translateY(0);
  opacity: 1;
  box-shadow: 0 14px 34px rgba(225, 170, 18, 0.48);
}

.quick-view-btn:focus-visible {
  outline: 2px solid rgba(255, 198, 57, 0.92);
  outline-offset: 2px;
  box-shadow: 0 14px 34px rgba(225, 170, 18, 0.48);
}

.poster-meta h3 {
  margin: 0;
  font-family: 'Epilogue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 19px;
  font-weight: 800;
  line-height: 1.25;
  transition: color 0.2s ease;
}

.poster-card:hover .poster-meta h3 {
  color: var(--accent);
}

.meta-row {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.meta-row span {
  padding: 7px 12px;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.state-card {
  grid-column: 1 / -1;
  min-height: 220px;
  border-radius: 28px;
  background: var(--surface-low);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  color: var(--text-muted);
  font-size: 15px;
  text-align: center;
}

.error-state {
  color: #ffb4ab;
}

.movie-pagination {
  margin-top: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.page-button {
  min-width: 120px;
  height: 46px;
  border-radius: 999px;
  border: 1px solid rgba(153, 144, 124, 0.2);
  background: var(--surface-high);
  color: var(--text-main);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  transition: color 0.2s ease, transform 0.2s ease, border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
}

.page-button:hover:not(:disabled) {
  color: var(--accent);
  transform: translateY(-1px);
  border-color: rgba(255, 198, 57, 0.5);
  background: rgba(255, 198, 57, 0.08);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.26);
}

.page-button:active:not(:disabled) {
  transform: translateY(0);
}

.page-button:focus-visible {
  outline: 2px solid rgba(255, 198, 57, 0.92);
  outline-offset: 2px;
  border-color: rgba(255, 198, 57, 0.6);
  color: var(--accent);
}

.page-button:disabled {
  opacity: 0.38;
  cursor: not-allowed;
}

.page-indicator {
  min-width: 112px;
  height: 52px;
  border-radius: 999px;
  padding: 0 22px;
  background: rgba(255, 198, 57, 0.1);
  color: var(--accent);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 800;
  transition: box-shadow 0.25s ease, transform 0.25s ease;
  box-shadow: 0 8px 22px rgba(255, 198, 57, 0.2);
}

.movie-pagination:hover .page-indicator {
  transform: translateY(-1px);
  box-shadow: 0 12px 28px rgba(255, 198, 57, 0.3);
}

.page-divider {
  color: rgba(255, 198, 57, 0.5);
}

@media (max-width: 1360px) {
  .poster-grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .discover-main,
  .page-heading,
  .status-panel {
    padding-left: 20px;
    padding-right: 20px;
  }

  .page-heading,
  .status-panel {
    grid-template-columns: 1fr;
    flex-wrap: wrap;
  }

  .filter-bar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .poster-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .page-heading h1 {
    font-size: 48px;
  }
}

@media (max-width: 768px) {
  .page-heading h1 {
    font-size: 38px;
  }

  .filter-bar,
  .poster-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .poster-overlay {
    opacity: 1;
    background: linear-gradient(180deg, rgba(11, 19, 38, 0.08), rgba(11, 19, 38, 0.9));
  }

  .quick-view-btn {
    transform: translateY(0);
    opacity: 1;
  }

  .movie-pagination {
    flex-wrap: wrap;
  }
}

@media (max-width: 520px) {
  .filter-bar,
  .poster-grid {
    grid-template-columns: 1fr;
  }
}

@media (hover: none) {
  .poster-overlay {
    opacity: 1;
    background: linear-gradient(180deg, rgba(11, 19, 38, 0.08), rgba(11, 19, 38, 0.9));
  }

  .quick-view-btn {
    transform: translateY(0);
    opacity: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .poster-card {
    animation: none;
    transition: none;
  }

  .poster-card:hover,
  .poster-card:focus-visible {
    transform: none;
  }

  .poster-media,
  .poster-media img,
  .poster-overlay,
  .quick-view-btn,
  .page-button,
  .page-indicator {
    transition: none;
  }
}

@keyframes card-reveal {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.985);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>

<style rel="stylesheet/scss" lang="scss">
.discover-select-popper {
  margin-top: 12px !important;
  border: 1px solid rgba(255, 198, 57, 0.28) !important;
  border-radius: 24px !important;
  background:
    radial-gradient(circle at top, rgba(255, 198, 57, 0.1), transparent 42%),
    linear-gradient(180deg, rgba(25, 33, 52, 0.98), rgba(15, 22, 37, 0.98)) !important;
  box-shadow:
    0 24px 60px rgba(0, 0, 0, 0.46),
    0 0 0 1px rgba(255, 198, 57, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.05) !important;
  overflow: hidden;
}

.discover-select-popper[x-placement^='bottom'] .popper__arrow,
.discover-select-popper[x-placement^='bottom'] .popper__arrow::after,
.discover-select-popper[x-placement^='top'] .popper__arrow,
.discover-select-popper[x-placement^='top'] .popper__arrow::after {
  border-top-color: rgba(25, 33, 52, 0.98) !important;
  border-bottom-color: rgba(25, 33, 52, 0.98) !important;
}

.discover-select-popper .el-scrollbar__view {
  padding: 10px 8px;
}

.discover-select-popper .el-select-dropdown__item {
  height: 44px;
  line-height: 44px;
  margin: 3px 0;
  border-radius: 14px;
  color: #dae2fd;
  font-size: 14px;
  font-weight: 700;
  transition: background-color 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.discover-select-popper .el-select-dropdown__item.hover,
.discover-select-popper .el-select-dropdown__item:hover {
  background: rgba(255, 198, 57, 0.12) !important;
  color: #ffe7a6 !important;
  transform: translateX(2px);
}

.discover-select-popper .el-select-dropdown__item.selected {
  background: linear-gradient(135deg, rgba(59, 116, 230, 0.96), rgba(41, 93, 190, 0.96)) !important;
  color: #ffffff !important;
  box-shadow:
    0 10px 22px rgba(41, 93, 190, 0.34),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.discover-select-popper .el-select-dropdown__item.selected::after {
  display: none;
}
</style>
