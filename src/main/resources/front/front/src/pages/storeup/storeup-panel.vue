<template>
  <div class="storeup-panel" :class="{ 'storeup-panel-embedded': embedded }">
    <header v-if="!hideHeader" class="page-header">
      <div>
        <span class="header-kicker">Personal Collection</span>
        <h2>My List</h2>
      </div>

      <div class="header-actions">
        <button
          v-for="option in sortOptions"
          :key="option.key"
          class="sort-button"
          :class="{ 'sort-button-active': activeSortKey === option.key }"
          type="button"
          @click="changeSort(option.key)"
        >
          <i class="el-icon-sort"></i>
          <span>{{ option.label }}</span>
        </button>
      </div>
    </header>

    <section class="toolbar">
      <div class="toolbar-chip toolbar-chip-active">All Favorites</div>

      <label class="search-box">
        <i class="el-icon-search"></i>
        <input
          v-model="formSearch.name"
          type="text"
          placeholder="Search your collection..."
          @keyup.enter="getStoreupList(1)"
        >
      </label>
    </section>

    <section v-if="storeupList.length" class="card-grid">
      <article
        v-for="item in storeupList"
        :key="item.id"
        class="favorite-card"
        @click="toDetail(item)"
      >
        <div class="poster-shell">
          <img :src="getStoreupPicture(item)" :alt="item.name">
          <div class="poster-overlay"></div>
          <button
            class="favorite-toggle"
            type="button"
            @click.stop="removeStoreup(item)"
          >
            <i class="el-icon-star-on"></i>
          </button>
        </div>

        <div class="card-copy">
          <h3>{{ item.name }}</h3>
          <p>{{ getItemMeta(item) }}</p>
        </div>
      </article>
    </section>

    <section v-else class="empty-panel">
      <div class="empty-icon">
        <i class="el-icon-folder-opened"></i>
      </div>
      <h3>Collection Empty</h3>
      <p>{{ emptyText }}</p>
      <button class="empty-action" type="button" @click="goDiscover">去发现影片</button>
    </section>

    <footer class="pagination-shell">
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

      <p class="pagination-meta" v-if="total > 0">
        Showing {{ storeupList.length }} of {{ total }} items
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
      pageSize: 8,
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
    getItemMeta(item) {
      const typeMap = {
        dianyingxinxi: 'Movie Favorite',
      }
      if (item && item.addtime) {
        return `${typeMap[item.tablename] || 'Favorite Item'} • ${String(item.addtime).slice(0, 10)}`
      }
      return typeMap[item.tablename] || 'Favorite Item'
    },
    buildListParams(page) {
      const sortOption = this.activeSortOption
      const params = {
        page,
        limit: this.pageSize,
        type: this.storeupType,
        userid: Number(localStorage.getItem('frontUserid')),
        sort: sortOption.sort,
        order: sortOption.order,
      }
      if (this.formSearch.name.trim()) {
        params.name = `%${this.formSearch.name.trim()}%`
      }
      return params
    },
    getStoreupList(page) {
      this.currentPage = page
      this.$http.get('storeup/list', { params: this.buildListParams(page) }).then(res => {
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
      if (!item || !item.id) {
        return
      }
      this.$confirm('是否取消该收藏？').then(() => {
        this.$http.post('storeup/delete', [item.id]).then(res => {
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
      this.$router.push({ path: `/index/${item.tablename}Detail`, query: { id: item.refid, storeupType: 1 } })
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.storeup-panel {
  position: relative;
  z-index: 1;
}

.storeup-panel-embedded {
  min-width: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
  margin-bottom: 28px;
}

.header-kicker {
  display: block;
  margin-bottom: 8px;
  color: #ffc639;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: .22em;
  text-transform: uppercase;
}

.page-header h2 {
  margin: 0;
  color: #dae2fd;
  font-size: clamp(38px, 4vw, 54px);
  font-weight: 900;
  letter-spacing: -.04em;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12px;
}

.sort-button {
  border: 0;
  padding: 0 16px;
  height: 42px;
  border-radius: 999px;
  background: rgba(45, 52, 73, 0.42);
  color: rgba(208, 197, 175, 0.72);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: .12em;
  text-transform: uppercase;
  transition: color .25s ease, background .25s ease, transform .25s ease;
}

.sort-button:hover {
  color: #ffc639;
  background: rgba(45, 52, 73, 0.72);
}

.sort-button-active {
  color: #3f2e00;
  background: linear-gradient(135deg, #ffc639, #e1aa12);
}

.toolbar {
  border: 1px solid rgba(77, 70, 53, 0.14);
  border-radius: 999px;
  padding: 8px;
  margin-bottom: 34px;
  background: rgba(19, 27, 46, 0.52);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.toolbar-chip {
  min-width: 146px;
  height: 44px;
  border-radius: 999px;
  padding: 0 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: .16em;
  text-transform: uppercase;
}

.toolbar-chip-active {
  color: #3f2e00;
  background: #ffc639;
}

.search-box {
  position: relative;
  width: min(100%, 360px);
}

.search-box i {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(208, 197, 175, 0.68);
}

.search-box input {
  width: 100%;
  height: 48px;
  border: 1px solid transparent;
  border-radius: 999px;
  padding: 0 18px 0 46px;
  background: rgba(45, 52, 73, 0.86);
  color: #dae2fd;
  font-size: 14px;
  outline: none;
  transition: border-color .25s ease, box-shadow .25s ease;
}

.search-box input::placeholder {
  color: rgba(208, 197, 175, 0.46);
}

.search-box input:focus {
  border-color: rgba(255, 198, 57, 0.72);
  box-shadow: 0 0 0 3px rgba(255, 198, 57, 0.12);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 30px 24px;
}

.favorite-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  cursor: pointer;
}

.poster-shell {
  position: relative;
  aspect-ratio: 2 / 3;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.4);
}

.poster-shell img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform .5s ease;
}

.favorite-card:hover .poster-shell img {
  transform: scale(1.08);
}

.poster-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(11, 19, 38, 0.04), rgba(11, 19, 38, 0.38) 55%, rgba(11, 19, 38, 0.84));
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
  color: #ffc639;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.22);
  transition: background .25s ease, color .25s ease, transform .25s ease;
}

.favorite-toggle:hover {
  background: #ffc639;
  color: #3f2e00;
  transform: scale(1.08);
}

.card-copy h3 {
  margin: 0 0 8px;
  color: #dae2fd;
  font-size: 20px;
  font-weight: 800;
  line-height: 1.2;
  transition: color .25s ease;
}

.favorite-card:hover .card-copy h3 {
  color: #ffc639;
}

.card-copy p {
  margin: 0;
  color: rgba(208, 197, 175, 0.7);
  font-size: 13px;
  line-height: 1.5;
}

.empty-panel {
  border-radius: 28px;
  padding: 48px 28px;
  background: rgba(19, 27, 46, 0.48);
  border: 1px solid rgba(77, 70, 53, 0.14);
  text-align: center;
}

.empty-icon {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  margin: 0 auto 18px;
  background: rgba(255, 198, 57, 0.12);
  color: #ffc639;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

.empty-panel h3 {
  margin: 0 0 10px;
  color: #dae2fd;
  font-size: 26px;
  font-weight: 800;
}

.empty-panel p {
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
  background: linear-gradient(135deg, #ffc639, #e1aa12);
  color: #3f2e00;
  font-size: 13px;
  font-weight: 900;
  letter-spacing: .16em;
  text-transform: uppercase;
}

.pagination-shell {
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
  color: #ffc639 !important;
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
  letter-spacing: .18em;
  text-transform: uppercase;
}

@media (max-width: 1080px) {
  .page-header,
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: flex-start;
  }

  .search-box {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 22px 14px;
  }

  .card-copy h3 {
    font-size: 16px;
  }

  .toolbar-chip {
    width: 100%;
  }
}
</style>
