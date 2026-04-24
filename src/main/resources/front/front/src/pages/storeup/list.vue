<template>
  <div class="favorite-shell">
    <aside class="center-sidebar fade-slide-up">
      <button class="brand-block" type="button" @click="goHome">
        <div class="brand-mark">
          <i class="el-icon-film"></i>
        </div>
        <div>
          <div class="brand-title">Aether Cinema</div>
          <div class="brand-subtitle">The Director's Cut</div>
        </div>
      </button>

      <nav class="side-nav">
        <button
          v-for="item in sidebarItems"
          :key="item.key"
          class="side-nav-item"
          :class="{ 'side-nav-item-active': isItemActive(item) }"
          type="button"
          @click="selectSidebarItem(item)"
        >
          <i :class="item.icon"></i>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="sidebar-footer">
        <div class="sidebar-meta">Security Level: Gold Tier Member</div>
        <div class="sidebar-meta">Last Updated: {{ lastUpdatedText }}</div>
      </div>
    </aside>

    <main class="favorite-main">
      <div class="hero-haze"></div>
      <div class="favorite-content fade-slide-up delay-1">
        <storeup-panel :hide-header="true"></storeup-panel>
      </div>
    </main>
  </div>
</template>

<script>
import menu from '@/config/menu'
import StoreupPanel from './storeup-panel'

const { buildCenterNavItems } = require('../center/center-helpers')
const { confirmFrontLogout } = require('../../utils/front-logout')

export default {
  components: {
    StoreupPanel,
  },
  data() {
    return {
      menuList: [],
      userTableName: localStorage.getItem('UserTableName'),
    }
  },
  computed: {
    sidebarItems() {
      return buildCenterNavItems(this.menuList).map(item => {
        if (item.icon === 'person') {
          item.icon = 'el-icon-user-solid'
        } else if (item.icon === 'lock') {
          item.icon = 'el-icon-lock'
        } else if (item.icon === 'favorite') {
          item.icon = 'el-icon-star-on'
        } else if (item.icon === 'logout') {
          item.icon = 'el-icon-switch-button'
        } else {
          item.icon = 'el-icon-menu'
        }
        return item
      })
    },
    lastUpdatedText() {
      return new Date().toISOString().slice(0, 10)
    },
  },
  created() {
    this.initMenuList()
  },
  methods: {
    initMenuList() {
      const menus = menu.list()
      for (let x in menus) {
        if (menus[x].tableName === this.userTableName) {
          this.menuList = (menus[x].backMenu || []).filter(item => item.menu !== '考试管理')
        }
      }
    },
    goHome() {
      window.location.hash = '#/index/home'
    },
    isItemActive(item) {
      return item.key === 'storeup'
    },
    selectSidebarItem(item) {
      if (item.key === 'storeup') {
        return
      }

      if (item.type === 'panel') {
        if (item.key === 'profile') {
          this.$router.push('/index/center')
        } else if (item.key === 'password') {
          this.$router.push({ path: '/index/center', query: { section: 'password' } })
        } else if (item.key === 'storeup') {
          this.$router.push({ path: '/index/center', query: { section: 'storeup' } })
        }
        return
      }

      if (item.type === 'route' && item.route) {
        if (item.route.indexOf('?') > -1) {
          const routePath = item.route.split('?')[0]
          const routeQuery = {}
          item.route.split('?')[1].split('&').forEach(pair => {
            const arr = pair.split('=')
            routeQuery[arr[0]] = arr[1]
          })
          this.$router.push({ path: routePath, query: routeQuery })
        } else {
          this.$router.push(item.route)
        }
        return
      }

      if (item.type === 'action' && item.action === 'logout') {
        this.logout()
      }
    },
    logout() {
      return confirmFrontLogout(this, {
        afterClear: () => {
          localStorage.setItem('keyPath', '0')
        },
      })
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.favorite-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 264px minmax(0, 1fr);
  background:
    radial-gradient(circle at top left, rgba(255, 198, 57, 0.07), transparent 24%),
    radial-gradient(circle at right bottom, rgba(63, 92, 188, 0.18), transparent 28%),
    #0b1326;
  color: #dae2fd;
}

.fade-slide-up {
  opacity: 0;
  animation: fadeSlideUp .7s ease forwards;
}

.delay-1 {
  animation-delay: .08s;
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.center-sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 32px 18px 24px;
  background: rgba(5, 10, 20, 0.82);
  backdrop-filter: blur(18px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.38);
  display: flex;
  flex-direction: column;
  z-index: 2;
}

.brand-block {
  border: 0;
  margin-bottom: 30px;
  background: transparent;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 8px;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: transform .25s ease, opacity .25s ease;
}

.brand-block:hover {
  transform: translateX(2px);
}

.brand-block:focus {
  outline: none;
}

.brand-block:focus-visible .brand-mark {
  box-shadow: 0 0 0 3px rgba(255, 198, 57, 0.18), 0 12px 30px rgba(255, 198, 57, 0.28);
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3f2e00;
  background: linear-gradient(135deg, #ffc639, #e1aa12);
  box-shadow: 0 12px 30px rgba(255, 198, 57, 0.28);
  font-size: 18px;
}

.brand-title {
  color: #ffc639;
  font-size: 25px;
  font-weight: 800;
  letter-spacing: -.03em;
  line-height: 1.1;
}

.brand-subtitle {
  color: rgba(188, 199, 222, 0.62);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .28em;
  text-transform: uppercase;
  margin-top: 4px;
}

.side-nav {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 8px;
}

.side-nav-item {
  border: 0;
  border-right: 4px solid transparent;
  padding: 16px 18px;
  border-radius: 18px 0 0 18px;
  background: transparent;
  color: rgba(188, 199, 222, 0.76);
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: .16em;
  text-transform: uppercase;
  transition: color .25s ease, background .25s ease, transform .25s ease, border-color .25s ease;
  text-align: left;
}

.side-nav-item i {
  font-size: 18px;
}

.side-nav-item:hover {
  color: #ffe2a4;
  background: rgba(255, 255, 255, 0.04);
  transform: translateX(2px);
}

.side-nav-item-active {
  color: #ffc639;
  border-right-color: #ffc639;
  background: linear-gradient(90deg, rgba(255, 198, 57, 0.12), rgba(255, 198, 57, 0));
  box-shadow: inset 0 0 0 1px rgba(255, 198, 57, 0.04);
}

.sidebar-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding: 18px 8px 0;
  display: grid;
  gap: 8px;
}

.sidebar-meta {
  color: rgba(188, 199, 222, 0.44);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .14em;
  text-transform: uppercase;
}

.favorite-main {
  position: relative;
  min-width: 0;
  padding: 48px 42px 34px;
  overflow: hidden;
}

.hero-haze {
  position: absolute;
  top: -120px;
  right: -80px;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  background: rgba(255, 198, 57, 0.08);
  filter: blur(120px);
  pointer-events: none;
}

.favorite-content {
  position: relative;
  z-index: 1;
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
  .favorite-shell {
    grid-template-columns: 1fr;
  }

  .center-sidebar {
    position: relative;
    height: auto;
    padding-bottom: 18px;
  }

  .side-nav {
    flex-direction: row;
    overflow-x: auto;
    padding-bottom: 6px;
  }

  .side-nav-item {
    min-width: max-content;
    border-right-width: 0;
    border-bottom: 3px solid transparent;
    border-radius: 16px;
  }

  .side-nav-item-active {
    border-bottom-color: #ffc639;
  }

  .favorite-main {
    padding: 32px 20px 28px;
  }

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

  .favorite-main {
    padding: 24px 14px 24px;
  }

  .toolbar-chip {
    width: 100%;
  }
}
</style>
