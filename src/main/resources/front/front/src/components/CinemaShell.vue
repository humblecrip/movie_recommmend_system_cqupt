<template>
  <div class="cinema-shell">
    <div class="ambient ambient-one"></div>
    <div class="ambient ambient-two"></div>
    <div class="ambient ambient-three"></div>

    <header class="topbar fade-slide-down">
      <button class="brand" type="button" @click="goHome">
        <span class="brand-main">电影推荐</span>
        <span class="brand-accent">系统</span>
      </button>

      <nav class="nav-links">
        <button class="nav-link" :class="{ active: activeNav === 'home' }" type="button" @click="goHome">首页</button>
        <button class="nav-link" :class="{ active: activeNav === 'movies' }" type="button" @click="openMovies">电影</button>
        <button class="nav-link" :class="{ active: activeNav === 'my-list' }" type="button" @click="openMyList">我的收藏</button>
        <button class="nav-link discover-link" :class="{ active: activeNav === 'discover' }" type="button" @click="goDiscover">
          <span class="material-symbols-outlined nav-icon">explore</span>
          智能推荐
        </button>
      </nav>

      <div class="topbar-actions">
        <button class="icon-button" type="button" @click="emitSearch">
          <i class="el-icon-search"></i>
        </button>
        <button
          class="avatar-button"
          :class="{ 'avatar-button-placeholder': userIdentity.mode === 'placeholder' }"
          type="button"
          @click="goProfile"
        >
          <span v-if="userIdentity.mode === 'placeholder'" class="avatar-login-text">{{ userIdentity.text }}</span>
          <img v-else :src="userIdentity.src" alt="avatar">
        </button>
      </div>
    </header>

    <div class="search-shell fade-slide-down delay-1" v-if="showSearch">
      <input
        :value="searchKeyword"
        class="search-input"
        type="text"
        :placeholder="searchPlaceholder"
        @input="handleSearchInput"
        @keyup.enter="emitSearch"
      >
    </div>

    <slot></slot>
  </div>
</template>

<script>
const {
  getFrontIdentityState,
  parseSessionForm,
} = require('../utils/front-avatar')

export default {
  props: {
    activeNav: {
      type: String,
      default: 'home',
    },
    searchKeyword: {
      type: String,
      default: '',
    },
    searchPlaceholder: {
      type: String,
      default: '搜索电影、导演或演员',
    },
    showSearch: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      avatarState: {
        sessionForm: {},
        cachedAvatar: '',
      },
      fallbackAvatar: require('@/assets/avator.png'),
    }
  },
  computed: {
    userIdentity() {
      return getFrontIdentityState({
        hasToken: !!localStorage.getItem('frontToken'),
        sessionForm: this.avatarState.sessionForm,
        cachedAvatar: this.avatarState.cachedAvatar,
        baseUrl: this.$config.baseUrl,
        fallbackAvatar: this.fallbackAvatar,
        placeholderText: '去登录',
      })
    },
  },
  created() {
    this.syncAvatarState()
  },
  mounted() {
    window.addEventListener('front-avatar-updated', this.handleAvatarUpdated)
  },
  beforeDestroy() {
    window.removeEventListener('front-avatar-updated', this.handleAvatarUpdated)
  },
  methods: {
    hasListener(name) {
      return !!(this.$listeners && this.$listeners[name])
    },
    syncAvatarState() {
      this.avatarState = {
        sessionForm: parseSessionForm(localStorage.getItem('sessionForm')),
        cachedAvatar: localStorage.getItem('frontHeadportrait') || '',
      }
    },
    handleAvatarUpdated(event) {
      const detail = event && event.detail ? event.detail : {}
      this.avatarState = {
        sessionForm: parseSessionForm(detail.sessionForm || localStorage.getItem('sessionForm')),
        cachedAvatar: detail.cachedAvatar !== undefined ? detail.cachedAvatar : (localStorage.getItem('frontHeadportrait') || ''),
      }
    },
    handleSearchInput(event) {
      const value = event && event.target ? event.target.value : ''
      this.$emit('update:searchKeyword', value)
    },
    emitSearch() {
      this.$emit('search')
    },
    goHome() {
      if (this.hasListener('open-home')) {
        this.$emit('open-home')
        return
      }
      this.$router.push('/index/home')
    },
    openMovies() {
      if (this.hasListener('open-movies')) {
        this.$emit('open-movies')
        return
      }
      this.$router.push('/index/dianyingxinxi')
    },
    openMyList() {
      if (localStorage.getItem('frontToken')) {
        this.$router.push('/index/storeup')
      } else {
        this.$router.push('/login')
      }
    },
    goProfile() {
      if (localStorage.getItem('frontToken')) {
        this.$router.push('/index/center')
      } else {
        this.$router.push('/login')
      }
    },
    goDiscover() {
      this.$router.push('/index/ai-recommend')
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.cinema-shell {
  --bg: #090f18;
  --line: rgba(255, 255, 255, 0.18);
  --text: #f6f7fb;
  --muted: rgba(255, 255, 255, 0.74);
  --blue: #2fc8ff;
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: radial-gradient(circle at top, rgba(42, 208, 255, 0.1), transparent 28%), #090f18;
  color: var(--text);
  font-family: 'Segoe UI', 'PingFang SC', sans-serif;
  padding: 18px 20px 30px;
}

.fade-slide-down {
  opacity: 0;
  animation-duration: 0.7s;
  animation-fill-mode: forwards;
  animation-timing-function: ease;
  animation-name: fadeSlideDown;
}

.delay-1 {
  animation-delay: 0.08s;
}

@keyframes fadeSlideDown {
  from { opacity: 0; transform: translateY(-16px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes floatAmbient {
  0%, 100% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(0, 10px, 0); }
}

.ambient {
  position: absolute;
  border-radius: 999px;
  filter: blur(74px);
  pointer-events: none;
  animation: floatAmbient 8s ease-in-out infinite;
}

.ambient-one { top: -60px; left: 20%; width: 180px; height: 90px; background: rgba(37, 201, 255, 0.14); }
.ambient-two { top: 10px; right: 28%; width: 150px; height: 80px; background: rgba(37, 201, 255, 0.16); animation-delay: 1.4s; }
.ambient-three { top: 320px; left: 58%; width: 160px; height: 90px; background: rgba(37, 201, 255, 0.1); animation-delay: 2.6s; }

.topbar {
  position: relative;
  z-index: 4;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 24px;
  margin-bottom: 12px;
}

.brand {
  border: 0;
  background: transparent;
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  color: inherit;
  padding: 0;
}

.brand-main { font-size: 19px; font-weight: 300; letter-spacing: 0.4px; }
.brand-accent { color: var(--blue); font-size: 19px; font-weight: 700; text-shadow: 0 0 16px rgba(47, 200, 255, 0.55); }

.nav-links { display: flex; align-items: center; justify-content: center; gap: 18px; }

.nav-link {
  border: 0;
  background: transparent;
  color: rgba(255, 255, 255, 0.82);
  padding: 8px 10px 14px;
  font-size: 15px;
  position: relative;
}

.nav-link.active { color: var(--blue); }
.nav-link.active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 2px;
  width: 42px;
  height: 4px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(47, 200, 255, 0.2), var(--blue), rgba(47, 200, 255, 0.2));
  transform: translateX(-50%);
  box-shadow: 0 0 18px rgba(47, 200, 255, 0.6);
}

.discover-link {
  display: flex;
  align-items: center;
  gap: 6px;
}

.discover-link .nav-icon {
  font-size: 16px;
}

.topbar-actions { display: flex; align-items: center; gap: 14px; }
.icon-button, .avatar-button, .nav-link, .brand { cursor: pointer; }
.icon-button, .avatar-button { border: 0; background: transparent; color: #fff; padding: 0; }
.icon-button { font-size: 22px; }
.avatar-button img { width: 34px; height: 34px; border-radius: 50%; object-fit: cover; border: 1px solid rgba(255, 255, 255, 0.22); }
.avatar-button-placeholder {
  min-width: 76px;
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.06);
}
.avatar-login-text {
  color: rgba(255, 255, 255, 0.92);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: .12em;
}

.search-shell { position: relative; z-index: 4; margin-bottom: 14px; }
.search-input {
  width: 100%;
  height: 42px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(16px);
  color: #fff;
  padding: 0 20px;
  font-size: 15px;
}
.search-input:focus {
  outline: none;
  border-color: rgba(47, 200, 255, 0.35);
  box-shadow: 0 0 0 1px rgba(47, 200, 255, 0.15), 0 0 18px rgba(47, 200, 255, 0.16);
}

@media (max-width: 980px) {
  .cinema-shell { padding: 16px 14px 24px; }
  .topbar { grid-template-columns: 1fr; gap: 12px; }
  .nav-links { justify-content: flex-start; flex-wrap: wrap; gap: 8px 12px; }
  .topbar-actions { position: absolute; top: 0; right: 0; }
}

@media (max-width: 560px) {
  .brand-main, .brand-accent { font-size: 16px; }
  .nav-link { font-size: 14px; }
}
</style>
