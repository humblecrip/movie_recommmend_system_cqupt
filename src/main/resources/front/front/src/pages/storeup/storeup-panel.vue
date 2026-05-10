<template>
  <div class="collection-panel" :class="{ 'collection-panel-embedded': embedded }">
    <section v-if="!hideHeader" class="collection-hero">
      <div class="collection-hero-copy">
        <p class="collection-eyebrow">你想长期保留的影片清单</p>
        <h1>我的收藏</h1>
        <p class="collection-hero-desc">
          保留收藏语义与接口，只把展示骨架切成更接近电影发现页的海报墙体验。
        </p>
      </div>

      <div class="collection-hero-summary">
        <span class="summary-label">收藏影片</span>
        <strong>{{ total }}</strong>
        <span class="summary-hint">{{ summaryHint }}</span>
      </div>
    </section>

    <section class="collection-filter-bar">
      <div class="collection-filter-card collection-filter-search" :class="{ 'is-active': !!formSearch.name.trim() }">
        <label>搜索</label>
        <div class="search-shell">
          <i class="el-icon-search"></i>
          <input
            v-model="formSearch.name"
            type="text"
            placeholder="搜索收藏电影名称"
            @keyup.enter="getStoreupList(1)"
          >
        </div>
      </div>

      <div class="collection-filter-card collection-filter-sort" :class="{ 'is-active': !!activeSortOption }">
        <label>排序方式</label>
        <div class="sort-chip-row">
          <button
            v-for="option in sortOptions"
            :key="option.key"
            type="button"
            class="sort-chip"
            :class="{ 'sort-chip-active': activeSortKey === option.key }"
            @click="changeSort(option.key)"
          >
            {{ option.label }}
          </button>
        </div>
      </div>
    </section>

    <section class="collection-status-bar">
      <div class="status-copy">
        <span class="status-title">收藏海报墙</span>
        <span class="status-subtitle">第 {{ currentPage }} / {{ totalPage || 1 }} 页</span>
      </div>

      <div class="status-tags">
        <span class="status-tag">{{ activeSortOption ? activeSortOption.label : '最新收藏' }}</span>
        <span class="status-tag">{{ formSearch.name.trim() ? `搜索: ${formSearch.name.trim()}` : '全部收藏' }}</span>
        <span class="status-tag">共 {{ total }} 部</span>
      </div>
    </section>

    <section class="collection-poster-grid">
      <div v-if="storeupList.length === 0" class="collection-state-card collection-empty-card">
        <div class="empty-icon">
          <i class="el-icon-folder-opened"></i>
        </div>
        <h3>收藏夹还是空的</h3>
        <p>{{ emptyText }}</p>
        <button class="empty-action" type="button" @click="goDiscover">去发现影片</button>
      </div>

      <article
        v-for="item in storeupList"
        :key="item.id"
        class="collection-poster-card"
        role="button"
        tabindex="0"
        :aria-label="`打开${getStoreupTitle(item)}详情`"
        @click="toDetail(item)"
        @keydown.enter.prevent="toDetail(item)"
        @keydown.space.prevent="toDetail(item)"
      >
        <div class="collection-poster-media">
          <img :src="getStoreupPicture(item)" :alt="getStoreupTitle(item)">
          <div class="collection-poster-overlay">
            <button class="quick-view-btn" type="button" @click.stop="toDetail(item)">查看详情</button>
          </div>
          <button
            class="favorite-toggle"
            type="button"
            title="取消收藏"
            aria-label="取消收藏"
            @click.stop="removeStoreup(item)"
          >
            <i class="el-icon-star-on"></i>
          </button>
        </div>

        <div class="collection-poster-meta">
          <h3>{{ getStoreupTitle(item) }}</h3>
          <div class="meta-row">
            <span>{{ item.typeName || '收藏影片' }}</span>
            <span>{{ formatCreatedAt(item.createdAt) }}</span>
          </div>
        </div>
      </article>
    </section>

    <footer class="collection-pagination-shell">
      <div class="pagination-divider"></div>

      <el-pagination
        v-if="total > 0"
        background
        class="favorite-pagination"
        :pager-count="7"
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="pageSizes"
        :hide-on-single-page="false"
        layout="prev, pager, next, sizes"
        :total="total"
        @current-change="curChange"
        @prev-click="prevClick"
        @size-change="sizeChange"
        @next-click="nextClick"
      ></el-pagination>

      <p v-if="total > 0" class="pagination-meta">
        当前页展示 {{ storeupList.length }} / 全部 {{ total }} 部收藏
      </p>
    </footer>
  </div>
</template>

<script>
import config from '@/config/config'

const {
  resolveStoreupPicture,
  getStoreupSortOptions,
  getStoreupEmptyText,
} = require('./storeup-helpers')

export default {
  props: {
    embedded: {
      type: Boolean,
      default: false,
    },
    hideHeader: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      baseUrl: config.baseUrl,
      fallbackPicture: require('@/assets/chapter.jpg'),
      formSearch: {
        name: '',
      },
      storeupType: 1,
      storeupList: [],
      total: 0,
      pageSize: 12,
      pageSizes: [],
      totalPage: 1,
      currentPage: 1,
      activeSortKey: 'latest',
      sortOptions: getStoreupSortOptions(),
    }
  },
  computed: {
    activeSortOption() {
      return this.sortOptions.find(item => item.key === this.activeSortKey) || this.sortOptions[0]
    },
    emptyText() {
      return getStoreupEmptyText(this.formSearch.name)
    },
    summaryHint() {
      if (this.formSearch.name.trim()) {
        return '按当前关键词筛选'
      }
      return '按你的收藏记录持续整理'
    },
  },
  created() {
    this.storeupType = Number(localStorage.getItem('storeupType') || 1)
    this.getStoreupList(1)
  },
  methods: {
    goDiscover() {
      this.$router.push('/index/dianyingxinxi')
    },
    getStoreupPicture(item) {
      return resolveStoreupPicture(item, this.baseUrl, this.fallbackPicture)
    },
    getStoreupTitle(item) {
      return (item && (item.title || item.name)) || '未命名电影'
    },
    formatCreatedAt(value) {
      if (!value) {
        return '收藏中'
      }
      return String(value).slice(0, 10)
    },
    buildListParams(page) {
      const sortOption = this.activeSortOption
      const params = {
        page,
        limit: this.pageSize,
        sort: sortOption.sort,
        order: sortOption.order,
      }
      if (this.formSearch.name.trim()) {
        params.name = this.formSearch.name.trim()
      }
      return params
    },
    getStoreupList(page) {
      this.currentPage = page
      this.$http.get('appmovie/favorites/page', { params: this.buildListParams(page) }).then(res => {
        if (res.data.code == 0) {
          this.storeupList = res.data.data.list || []
          this.total = res.data.data.total
          this.pageSize = Number(res.data.data.pageSize)
          this.totalPage = res.data.data.totalPage
          if (this.pageSizes.length === 0) {
            this.pageSizes = [this.pageSize, this.pageSize * 2, this.pageSize * 3, this.pageSize * 5]
          }
        }
      })
    },
    changeSort(sortKey) {
      this.activeSortKey = sortKey
      this.getStoreupList(1)
    },
    removeStoreup(item) {
      if (!item || !item.movieId) {
        return
      }
      this.$confirm('是否取消该收藏？').then(() => {
        this.$http.post('appmovie/favorites/cancel', { movieId: item.movieId }).then(res => {
          if (res.data && res.data.code == 0) {
            const targetPage = this.storeupList.length === 1 && this.currentPage > 1 ? this.currentPage - 1 : this.currentPage
            this.$message({
              type: 'success',
              message: '已取消收藏',
              duration: 1200,
            })
            this.getStoreupList(targetPage)
          }
        })
      }).catch(() => {})
    },
    curChange(page) {
      this.getStoreupList(page)
    },
    prevClick(page) {
      this.getStoreupList(page)
    },
    sizeChange(size) {
      this.pageSize = size
      this.getStoreupList(1)
    },
    nextClick(page) {
      this.getStoreupList(page)
    },
    toDetail(item) {
      this.$router.push({ path: '/index/dianyingxinxiDetail', query: { id: item.movieId, storeupType: 1 } })
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.collection-panel {
  --page-bg: #0b1326;
  --surface-low: rgba(19, 27, 46, 0.9);
  --surface-high: rgba(34, 42, 61, 0.94);
  --surface-card: rgba(6, 14, 32, 0.94);
  --text-main: #dae2fd;
  --text-muted: rgba(218, 226, 253, 0.7);
  --text-soft: #d0c5af;
  --accent: #ffc639;
  --accent-deep: #e1aa12;
  color: var(--text-main);
  font-family: 'Manrope', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.collection-panel-embedded {
  min-width: 0;
}

.collection-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 24px;
  align-items: end;
  margin-bottom: 34px;
}

.collection-hero-copy {
  max-width: 820px;
}

.collection-eyebrow {
  margin: 0 0 14px;
  color: var(--text-soft);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.collection-hero h1 {
  margin: 0;
  font-family: 'Epilogue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: clamp(42px, 4vw, 60px);
  line-height: 0.96;
  font-weight: 900;
  letter-spacing: -0.05em;
}

.collection-hero-desc {
  margin: 18px 0 0;
  color: var(--text-muted);
  font-size: 16px;
  line-height: 1.8;
}

.collection-hero-summary,
.collection-status-bar,
.collection-filter-bar {
  background: var(--surface-low);
}

.collection-hero-summary {
  min-width: 180px;
  padding: 22px 24px;
  border-radius: 28px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-label,
.status-title,
.collection-filter-card label {
  color: var(--text-soft);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.collection-hero-summary strong {
  font-size: 34px;
  line-height: 1;
  color: var(--accent);
}

.summary-hint {
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.6;
}

.collection-filter-bar {
  display: grid;
  grid-template-columns: minmax(280px, 1.2fr) minmax(0, 1fr);
  gap: 14px;
  padding: 14px;
  border-radius: 32px;
}

.collection-filter-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
  padding: 12px;
  border-radius: 22px;
  border: 1px solid transparent;
  transition: border-color 0.24s ease, background-color 0.24s ease, transform 0.24s ease;
}

.collection-filter-card:hover {
  border-color: rgba(255, 198, 57, 0.22);
  background: rgba(255, 255, 255, 0.02);
}

.collection-filter-card.is-active {
  border-color: rgba(255, 198, 57, 0.34);
  background: rgba(255, 198, 57, 0.06);
}

.search-shell {
  position: relative;
}

.search-shell i {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(208, 197, 175, 0.68);
}

.search-shell input {
  width: 100%;
  height: 54px;
  border: 1px solid rgba(255, 198, 57, 0.18);
  border-radius: 999px;
  padding: 0 18px 0 48px;
  background:
    linear-gradient(180deg, rgba(40, 50, 72, 0.96), rgba(24, 32, 50, 0.96));
  color: var(--text-main);
  font-size: 15px;
  font-weight: 700;
  outline: none;
  transition: border-color 0.24s ease, box-shadow 0.24s ease;
}

.search-shell input::placeholder {
  color: rgba(218, 226, 253, 0.42);
}

.search-shell input:focus {
  border-color: rgba(255, 198, 57, 0.72);
  box-shadow: 0 0 0 4px rgba(255, 198, 57, 0.12);
}

.sort-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sort-chip {
  min-height: 42px;
  border: 1px solid rgba(153, 144, 124, 0.2);
  border-radius: 999px;
  padding: 0 16px;
  background: rgba(34, 42, 61, 0.92);
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  transition: color 0.2s ease, transform 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}

.sort-chip:hover {
  color: var(--accent);
  border-color: rgba(255, 198, 57, 0.38);
  transform: translateY(-1px);
}

.sort-chip-active {
  color: #3f2e00;
  border-color: transparent;
  background: linear-gradient(135deg, var(--accent), var(--accent-deep));
}

.collection-status-bar {
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

.collection-poster-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 30px 22px;
  min-height: 260px;
}

.collection-poster-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  cursor: pointer;
  animation: card-reveal 0.46s ease both;
  transition: transform 0.3s ease, filter 0.3s ease;
}

.collection-poster-card:hover {
  transform: translateY(-8px) scale(1.02);
}

.collection-poster-card:focus-visible {
  outline: 2px solid rgba(255, 198, 57, 0.92);
  outline-offset: 4px;
  transform: translateY(-4px) scale(1.01);
}

.collection-poster-media {
  position: relative;
  aspect-ratio: 2 / 3;
  border-radius: 24px;
  overflow: hidden;
  background: var(--surface-card);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.45);
  transition: box-shadow 0.3s ease;
}

.collection-poster-card:hover .collection-poster-media {
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.58);
}

.collection-poster-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.collection-poster-card:hover .collection-poster-media img {
  transform: scale(1.06);
}

.collection-poster-overlay {
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

.collection-poster-card:hover .collection-poster-overlay,
.collection-poster-card:focus-within .collection-poster-overlay {
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
}

.collection-poster-card:hover .quick-view-btn,
.collection-poster-card:focus-within .quick-view-btn {
  transform: translateY(0);
  opacity: 1;
  box-shadow: 0 14px 34px rgba(225, 170, 18, 0.48);
}

.favorite-toggle {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 42px;
  height: 42px;
  border: 0;
  border-radius: 50%;
  background: rgba(45, 52, 73, 0.66);
  backdrop-filter: blur(12px);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.22);
  transition: background .25s ease, color .25s ease, transform .25s ease;
}

.favorite-toggle:hover {
  background: var(--accent);
  color: #3f2e00;
  transform: scale(1.08);
}

.collection-poster-meta h3 {
  margin: 0;
  font-family: 'Epilogue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 19px;
  font-weight: 800;
  line-height: 1.25;
  transition: color 0.2s ease;
}

.collection-poster-card:hover .collection-poster-meta h3 {
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

.collection-state-card {
  grid-column: 1 / -1;
  min-height: 260px;
  border-radius: 28px;
  background: var(--surface-low);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 34px 24px;
  color: var(--text-muted);
  text-align: center;
}

.empty-icon {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  margin: 0 auto 18px;
  background: rgba(255, 198, 57, 0.12);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

.collection-empty-card h3 {
  margin: 0 0 10px;
  color: var(--text-main);
  font-size: 26px;
  font-weight: 800;
}

.collection-empty-card p {
  margin: 0 0 22px;
  color: rgba(208, 197, 175, 0.74);
  font-size: 14px;
}

.empty-action {
  border: 0;
  min-width: 160px;
  height: 48px;
  padding: 0 24px;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--accent), var(--accent-deep));
  color: #3f2e00;
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.collection-pagination-shell {
  margin-top: 42px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.pagination-divider {
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(77, 70, 53, 0.34), transparent);
}

.favorite-pagination ::v-deep .btn-prev,
.favorite-pagination ::v-deep .btn-next,
.favorite-pagination ::v-deep .el-pager li,
.favorite-pagination ::v-deep .el-select .el-input__inner {
  border: 1px solid rgba(77, 70, 53, 0.2) !important;
  border-radius: 999px !important;
  background: transparent !important;
  color: rgba(208, 197, 175, 0.78) !important;
}

.favorite-pagination ::v-deep .el-pager li,
.favorite-pagination ::v-deep .btn-prev,
.favorite-pagination ::v-deep .btn-next {
  min-width: 42px;
  height: 42px;
  line-height: 40px;
  margin: 0 4px;
}

.favorite-pagination ::v-deep .el-pager li.active {
  border-color: rgba(255, 198, 57, 0.3) !important;
  background: rgba(255, 198, 57, 0.1) !important;
  color: var(--accent) !important;
}

.favorite-pagination ::v-deep .el-select .el-input__inner {
  height: 42px;
  padding-left: 16px;
}

.favorite-pagination ::v-deep .el-pagination__sizes,
.favorite-pagination ::v-deep .el-pagination__total {
  color: rgba(208, 197, 175, 0.62);
}

.pagination-meta {
  margin: 0;
  color: rgba(208, 197, 175, 0.5);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

@media (max-width: 1100px) {
  .collection-hero,
  .collection-status-bar {
    grid-template-columns: 1fr;
    flex-wrap: wrap;
  }

  .collection-filter-bar {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .collection-hero h1 {
    font-size: 38px;
  }

  .collection-poster-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 22px 14px;
  }

  .collection-poster-overlay {
    opacity: 1;
    background: linear-gradient(180deg, rgba(11, 19, 38, 0.08), rgba(11, 19, 38, 0.9));
  }

  .quick-view-btn {
    transform: translateY(0);
    opacity: 1;
  }
}

@media (max-width: 520px) {
  .collection-poster-grid {
    grid-template-columns: 1fr;
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
