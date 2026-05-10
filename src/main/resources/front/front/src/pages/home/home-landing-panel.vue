<template>
  <div class="home-landing-panel">
    <section class="hero-section fade-up delay-2" @mouseenter="stopHeroAutoplay" @mouseleave="startHeroAutoplay">
      <transition name="hero-fade" mode="out-in">
        <img
          :key="currentHeroSlide.placeholderKey"
          class="hero-backdrop"
          :src="heroBackdropSrc"
          alt="首页主视觉背景"
          @error="heroImageError = true"
        >
      </transition>
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <transition name="hero-copy" mode="out-in">
          <div :key="currentHeroSlide.placeholderKey" class="hero-copy-wrap">
            <h1 class="hero-title">{{ currentHeroTitle }}</h1>
            <p class="hero-subtitle">{{ currentHeroSubtitle }}</p>
          </div>
        </transition>
        <div class="hero-actions">
          <template v-if="hasHeroData">
            <button class="hero-button hero-button-primary glow-pulse" type="button" @click="watchFeatured">立即观看</button>
            <button class="hero-button hero-button-secondary" type="button" @click="showFeaturedInfo">查看详情</button>
          </template>
          <div v-else class="hero-empty-note">{{ homeStatusMessage }}</div>
        </div>
        <div v-if="hasHeroData && heroSlides.length > 1" class="hero-dots">
          <button
            v-for="(slide, index) in heroSlides"
            :key="slide.placeholderKey"
            class="dot"
            :class="{ active: index === heroIndex }"
            type="button"
            @click="setHeroSlide(index)"
          ></button>
        </div>
      </div>
    </section>

    <section class="content-grid fade-up delay-3">
      <div class="left-column">
        <section class="shelf">
          <div class="section-title">为你推荐</div>
          <div v-if="recommendedMovies.length" class="poster-row poster-row-top">
            <button
              v-for="(item, index) in recommendedMovies"
              :key="getItemKey(item, 'recommended')"
              class="poster-card"
              :class="{ 'poster-card-active': index === 3 }"
              type="button"
              @click="toDetail(item)"
            >
              <div class="poster-cover">
                <img :src="getMovieCover(item)" :alt="item.dianyingmingcheng" @error="markImageError(getItemKey(item, 'recommended'))">
              </div>
              <div class="poster-rating">
                <i class="el-icon-star-on"></i>
                <span>{{ getScore(item) }}</span>
              </div>
            </button>
          </div>
          <div v-else class="section-empty">当前暂无高分电影</div>
        </section>

        <section class="shelf shelf-trending">
          <div class="section-title">热议影片</div>
          <div v-if="trendingMovies.length" class="poster-row poster-row-bottom">
            <button
              v-for="(item, index) in trendingMovies"
              :key="getItemKey(item, 'trending')"
              class="poster-card"
              :class="{ 'poster-card-active': index === 1 }"
              type="button"
              @click="toDetail(item)"
            >
              <div class="poster-cover">
                <img :src="getMovieCover(item)" :alt="item.dianyingmingcheng" @error="markImageError(getItemKey(item, 'trending'))">
              </div>
              <div class="poster-rating">
                <i class="el-icon-star-on"></i>
                <span>{{ getScore(item) }}</span>
              </div>
            </button>
          </div>
          <div v-else class="section-empty">当前暂无热议电影</div>
        </section>
      </div>

      <section class="week-column">
        <div class="section-title section-title-caps">最新上映</div>
        <button v-if="weekMovie" class="week-card" type="button" @click="toDetail(weekMovie)">
          <div class="week-poster-box">
            <img :src="getMovieCover(weekMovie)" :alt="weekMovie.dianyingmingcheng" @error="markImageError(getItemKey(weekMovie, 'week'))">
          </div>
          <div class="week-content">
            <div class="week-kicker">最新上映</div>
            <div class="week-name">{{ weekMovie.dianyingmingcheng }}</div>
            <div class="week-meta">上映时间：{{ weekMovie.releaseDate }}</div>
            <div class="week-meta">评分：{{ getScore(weekMovie) }}/5</div>
            <div class="week-meta">类型：{{ weekMovie.genre }}</div>
            <div class="week-meta">地区：{{ weekMovie.area }}</div>
            <div class="week-meta">时长：{{ weekMovie.runtime }}</div>
          </div>
        </button>
        <div v-else class="section-empty">当前暂无最新上映影片</div>
      </section>

      <aside class="editors-column">
        <div class="section-title section-title-caps">高收藏影片</div>
        <template v-if="editorsChoice.length">
          <button
            v-for="item in editorsChoice"
            :key="getItemKey(item, 'editor')"
            class="editor-card"
            type="button"
            @click="toDetail(item)"
          >
            <div class="editor-cover">
              <img :src="getMovieCover(item)" :alt="item.dianyingmingcheng" @error="markImageError(getItemKey(item, 'editor'))">
            </div>
            <div class="editor-name">{{ item.dianyingmingcheng }}</div>
          </button>
        </template>
        <div v-else class="section-empty section-empty-tight">当前暂无高收藏影片</div>
      </aside>
    </section>
  </div>
</template>

<script>
const {
  extractAppMovieList,
  normalizeAppMovieList,
} = require('../dianyingxinxi/detail-helpers')
const {
  EMPTY_HERO_SLIDE,
  normalizeConfigHeroSlides,
  normalizeHomeMovieRecord,
  hydrateConfigHeroSlide,
} = require('./home-landing-helpers')

const HOME_SECTION_REQUESTS = {
  hero: { sort: 'clickCount', order: 'desc', limit: 3 },
  recommended: { sort: 'totalScore', order: 'desc', limit: 6 },
  trending: { sort: 'commentCount', order: 'desc', limit: 6 },
  week: { sort: 'releaseDate', order: 'desc', limit: 1 },
  editorsChoice: { sort: 'favoriteCount', order: 'desc', limit: 3 },
}

function takeTopItems(source, count) {
  if (!Array.isArray(source) || !source.length || count <= 0) {
    return []
  }
  return source.slice(0, count)
}

function extractPageList(payload) {
  if (Array.isArray(payload)) {
    return payload
  }
  if (payload && Array.isArray(payload.list)) {
    return payload.list
  }
  return []
}

function parseDetailRouteMovieId(target) {
  const normalizedTarget = String(target || '').trim()
  if (!normalizedTarget) {
    return null
  }
  const match = normalizedTarget.match(/(?:^|[?#&])id=(\d+)/i)
  if (!match) {
    return null
  }
  const movieId = Number(match[1])
  return Number.isFinite(movieId) ? movieId : null
}

export default {
  data() {
    return {
      baseUrl: '',
      imageErrors: {},
      heroImageError: false,
      heroIndex: 0,
      heroTimer: null,
      homeStatusMessage: '当前暂无可展示的轮播内容。',
      fallbackHero: require('@/assets/login-bg.jpg'),
      fallbackPoster: require('@/assets/chapter.jpg'),
      heroSlides: [],
      movieHeroSlides: [],
      recommendedMovies: [],
      trendingMovies: [],
      weekMovie: null,
      editorsChoice: [],
    }
  },
  computed: {
    hasHeroData() {
      return this.heroSlides.length > 0
    },
    currentHeroSlide() {
      return this.heroSlides[this.heroIndex] || EMPTY_HERO_SLIDE
    },
    currentHeroTitle() {
      return this.currentHeroSlide.heroTitle || this.currentHeroSlide.dianyingmingcheng || EMPTY_HERO_SLIDE.heroTitle
    },
    currentHeroSubtitle() {
      return this.currentHeroSlide.heroSubtitle || this.currentHeroSlide.tagline || EMPTY_HERO_SLIDE.heroSubtitle
    },
    heroBackdropSrc() {
      if (this.heroImageError) {
        return this.currentHeroSlide.fallbackBackdropUrl || this.currentHeroSlide.fallbackCoverUrl || this.fallbackHero
      }
      return this.currentHeroSlide.backdropUrl
        || this.currentHeroSlide.fallbackBackdropUrl
        || this.currentHeroSlide.fallbackCoverUrl
        || this.fallbackHero
    },
  },
  created() {
    this.baseUrl = this.$config.baseUrl
    this.fetchHomeContent()
  },
  mounted() {
    this.startHeroAutoplay()
  },
  beforeDestroy() {
    this.stopHeroAutoplay()
  },
  methods: {
    async fetchHomeContent() {
      let configHeroSlides = []
      let configErrorMessage = ''
      let moviePayload = {
        sectionLists: {},
        errorMessage: '',
      }
      const [configResult, movieResult] = await Promise.allSettled([
        this.fetchConfigHeroSlides(),
        this.fetchMovieCollections(),
      ])
      if (configResult.status === 'fulfilled') {
        configHeroSlides = configResult.value
      } else {
        configErrorMessage = (configResult.reason && configResult.reason.message) || '首页轮播加载失败，请稍后重试。'
      }
      if (movieResult.status === 'fulfilled') {
        moviePayload = movieResult.value
      } else {
        moviePayload.errorMessage = (movieResult.reason && movieResult.reason.message) || '电影数据加载失败，请稍后重试。'
      }
      const { sectionLists, errorMessage } = moviePayload
      const hasMovieCollections = Object.keys(HOME_SECTION_REQUESTS).some(sectionName => {
        return ((sectionLists && sectionLists[sectionName]) || []).length > 0
      })
      if (hasMovieCollections) {
        this.applyHomeMovieCollections(sectionLists)
      } else {
        this.resetMovieCollections()
      }
      this.applyHeroSlides(configHeroSlides, configErrorMessage || errorMessage || '当前暂无可展示的内容。')
      if (!this.heroSlides.length && !this.recommendedMovies.length && !this.trendingMovies.length && !this.weekMovie && !this.editorsChoice.length) {
        this.homeStatusMessage = configErrorMessage || errorMessage || '当前暂无可展示的内容。'
      }
    },
    async fetchConfigHeroSlides() {
      const res = await this.$http.get('config/list', {
        params: {
          page: 1,
          limit: 8,
          sort: 'id',
          order: 'desc',
          name: '%picture%',
        },
      })
      if (res && res.data && res.data.code !== undefined && res.data.code !== 0) {
        throw new Error(res.data.msg || '首页轮播加载失败')
      }
      const responseData = res && res.data && res.data.data !== undefined ? res.data.data : ((res || {}).data || {})
      const slides = normalizeConfigHeroSlides(extractPageList(responseData), this.baseUrl)
      return this.hydrateConfigHeroSlides(slides)
    },
    async hydrateConfigHeroSlides(slides) {
      const sourceSlides = Array.isArray(slides) ? slides : []
      const hydratedSlides = await Promise.all(sourceSlides.map(async slide => {
        const movieId = parseDetailRouteMovieId(slide && (slide.actionUrl || slide.url))
        if (!movieId) {
          return slide
        }
        try {
          const res = await this.$http.get(`appmovie/front/detail/${movieId}`)
          if (res && res.data && res.data.code !== undefined && res.data.code !== 0) {
            return slide
          }
          const detailPayload = res && res.data && res.data.data !== undefined ? res.data.data : ((res || {}).data || {})
          return hydrateConfigHeroSlide(Object.assign({}, slide, {
            detailMovieId: movieId,
          }), detailPayload, this.baseUrl)
        } catch (error) {
          return slide
        }
      }))
      return hydratedSlides.map((slide, index) => {
        if (!slide) {
          return slide
        }
        if (slide.heroTitle) {
          return slide
        }
        return Object.assign({}, slide, {
          heroTitle: slide.rawName && !/^picture[\W_\d-]*$/i.test(slide.rawName)
            ? slide.rawName.replace(/[_-]+/g, ' ').trim()
            : `精选影片 ${index + 1}`,
        })
      })
    },
    async fetchMovieCollections() {
      const sectionNames = Object.keys(HOME_SECTION_REQUESTS)
      const results = await Promise.allSettled(
        sectionNames.map(sectionName => {
          if (sectionName === 'recommended') {
            return this.fetchRecommendedSection(HOME_SECTION_REQUESTS[sectionName])
          }
          return this.fetchMovieSection(HOME_SECTION_REQUESTS[sectionName])
        })
      )
      const sectionLists = {}
      let errorMessage = ''
      results.forEach((result, index) => {
        const sectionName = sectionNames[index]
        if (result.status === 'fulfilled') {
          sectionLists[sectionName] = result.value
          return
        }
        sectionLists[sectionName] = []
        if (!errorMessage) {
          errorMessage = (result.reason && result.reason.message) || '电影数据加载失败，请稍后重试。'
        }
      })
      return {
        sectionLists,
        errorMessage,
      }
    },
    async fetchRecommendedSection(sectionConfig) {
      const res = await this.$http.get('appmovie/front/recommended', {
        params: {
          page: 1,
          limit: sectionConfig.limit,
          sort: sectionConfig.sort,
          order: sectionConfig.order,
        },
      })
      if (res && res.data && res.data.code !== undefined && res.data.code !== 0) {
        throw new Error(res.data.msg || '个性化推荐加载失败')
      }
      const responseData = res && res.data && res.data.data !== undefined ? res.data.data : ((res || {}).data || {})
      return normalizeAppMovieList(extractAppMovieList(responseData))
    },
    async fetchMovieSection(sectionConfig) {
      const res = await this.$http.get('appmovie/front/list', {
        params: {
          page: 1,
          limit: sectionConfig.limit,
          sort: sectionConfig.sort,
          order: sectionConfig.order,
        },
      })
      if (res && res.data && res.data.code !== undefined && res.data.code !== 0) {
        throw new Error(res.data.msg || '电影列表加载失败')
      }
      const responseData = res && res.data && res.data.data !== undefined ? res.data.data : ((res || {}).data || {})
      return normalizeAppMovieList(extractAppMovieList(responseData))
    },
    resetMovieCollections() {
      this.movieHeroSlides = []
      this.recommendedMovies = []
      this.trendingMovies = []
      this.weekMovie = null
      this.editorsChoice = []
    },
    applyHomeMovieCollections(sectionLists) {
      const normalizedSections = {}
      Object.keys(HOME_SECTION_REQUESTS).forEach(sectionName => {
        const sectionConfig = HOME_SECTION_REQUESTS[sectionName]
        const rawList = Array.isArray(sectionLists && sectionLists[sectionName]) ? sectionLists[sectionName] : []
        normalizedSections[sectionName] = takeTopItems(
          rawList.map((item, index) => normalizeHomeMovieRecord(item, index, this.baseUrl)),
          sectionConfig.limit
        )
      })
      this.movieHeroSlides = normalizedSections.hero || []
      this.recommendedMovies = normalizedSections.recommended || []
      this.trendingMovies = normalizedSections.trending || []
      this.weekMovie = (normalizedSections.week || [])[0] || null
      this.editorsChoice = normalizedSections.editorsChoice || []
    },
    applyHeroSlides(configHeroSlides, message) {
      const nextHeroSlides = configHeroSlides.length ? configHeroSlides : this.movieHeroSlides
      this.heroSlides = nextHeroSlides
      this.homeStatusMessage = nextHeroSlides.length ? '' : (message || '当前暂无可展示的轮播内容。')
      this.heroIndex = 0
      this.heroImageError = false
      if (nextHeroSlides.length) {
        this.startHeroAutoplay()
        return
      }
      this.stopHeroAutoplay()
    },
    getItemKey(item, prefix) {
      if (!item) {
        return prefix + '-empty'
      }
      return item.id ? prefix + '-' + item.id : prefix + '-' + item.placeholderKey
    },
    getMovieCover(item) {
      const key = this.getItemKey(item, 'cover')
      if (this.imageErrors[key]) {
        return this.fallbackPoster
      }
      if (item && item.coverUrl) {
        return item.coverUrl
      }
      if (item && item.haibao) {
        if (item.haibao.substr(0, 4) === 'http') {
          return item.haibao.split(',')[0]
        }
        return this.baseUrl + item.haibao.split(',')[0]
      }
      return this.fallbackPoster
    },
    markImageError(key) {
      if (key) {
        this.$set(this.imageErrors, key, true)
      }
    },
    getScore(item) {
      const score = Number(item && item.totalscore)
      if (Number.isFinite(score)) {
        return score.toFixed(1)
      }
      return '--'
    },
    startHeroAutoplay() {
      this.stopHeroAutoplay()
      if (this.heroSlides.length <= 1) {
        return
      }
      this.heroTimer = setInterval(() => {
        this.heroIndex = (this.heroIndex + 1) % this.heroSlides.length
        this.heroImageError = false
      }, 3500)
    },
    stopHeroAutoplay() {
      if (this.heroTimer) {
        clearInterval(this.heroTimer)
        this.heroTimer = null
      }
    },
    setHeroSlide(index) {
      if (!this.heroSlides.length) {
        return
      }
      this.heroIndex = index
      this.heroImageError = false
      this.startHeroAutoplay()
    },
    watchFeatured() {
      if (!this.hasHeroData) {
        return
      }
      this.openHeroTarget(this.currentHeroSlide)
    },
    showFeaturedInfo() {
      if (!this.hasHeroData) {
        return
      }
      this.openHeroTarget(this.currentHeroSlide)
    },
    resolveInternalHeroTarget(target) {
      const normalizedTarget = String(target || '').trim()
      if (!normalizedTarget || /^https?:\/\//i.test(normalizedTarget)) {
        return ''
      }
      if (normalizedTarget.startsWith('#')) {
        return normalizedTarget.replace(/^#+/, '/')
      }
      if (normalizedTarget.startsWith('/')) {
        return normalizedTarget
      }
      if (/^(index|front)(\/|$)/i.test(normalizedTarget)) {
        return `/${normalizedTarget}`
      }
      return ''
    },
    openHeroTarget(item) {
      const actionUrl = String((item && item.actionUrl) || '').trim()
      if (actionUrl) {
        if (/^https?:\/\//i.test(actionUrl)) {
          window.open(actionUrl, '_blank', 'noopener')
          return
        }
        const routeTarget = this.resolveInternalHeroTarget(actionUrl)
        if (routeTarget) {
          this.$router.push(routeTarget)
          return
        }
      }
      this.toDetail(item)
    },
    toDetail(item) {
      if (item && item.sourceType === 'movie' && item.id && !item.placeholder) {
        this.$router.push({ path: '/index/dianyingxinxiDetail', query: { id: item.id } })
        return
      }
      this.$emit('open-movies')
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.fade-up {
  opacity: 0;
  animation-duration: 0.7s;
  animation-fill-mode: forwards;
  animation-timing-function: ease;
  animation-name: fadeUp;
}

.delay-2 {
  animation-delay: 0.16s;
}

.delay-3 {
  animation-delay: 0.24s;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(22px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes glowPulse {
  0%, 100% { box-shadow: 0 0 0 1px rgba(112, 223, 255, 0.18), 0 0 18px rgba(46, 191, 255, 0.42); }
  50% { box-shadow: 0 0 0 1px rgba(112, 223, 255, 0.24), 0 0 28px rgba(46, 191, 255, 0.68); }
}

@keyframes heroDrift {
  from { transform: scale(1.02) translateX(-1.5%); }
  to { transform: scale(1.08) translateX(1.5%); }
}

.hero-section {
  position: relative;
  z-index: 1;
  min-height: 388px;
  margin: -84px 0 24px;
  border-radius: 0 0 24px 24px;
  overflow: hidden;
}

.hero-backdrop {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: saturate(0.72) brightness(0.58);
  animation: heroDrift 16s ease-in-out infinite alternate;
}

.hero-fade-enter-active,
.hero-fade-leave-active {
  transition: opacity .65s ease, transform .65s ease;
}

.hero-fade-enter,
.hero-fade-leave-to {
  opacity: 0;
  transform: scale(1.03);
}

.hero-copy-enter-active,
.hero-copy-leave-active {
  transition: opacity .45s ease, transform .45s ease;
}

.hero-copy-enter,
.hero-copy-leave-to {
  opacity: 0;
  transform: translateY(14px);
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(7, 11, 18, 0.18), rgba(10, 14, 22, 0.48) 42%, rgba(10, 14, 22, 0.88));
}

.hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: 388px;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  padding: 112px 20px 26px;
}

.hero-copy-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-title {
  margin: 0;
  color: #fff;
  font-family: Impact, 'Arial Narrow', sans-serif;
  font-size: clamp(66px, 7.4vw, 108px);
  font-weight: 900;
  letter-spacing: 1px;
  line-height: 0.94;
  text-transform: uppercase;
  text-align: center;
  text-shadow: 0 8px 24px rgba(0, 0, 0, 0.72);
}

.hero-subtitle { margin: 10px 0 0; color: rgba(255, 255, 255, 0.86); font-size: 22px; font-weight: 300; }
.hero-actions { display: flex; gap: 16px; margin-top: 20px; }
.hero-empty-note {
  max-width: 520px;
  color: rgba(255, 255, 255, 0.88);
  font-size: 16px;
  line-height: 1.7;
  text-align: center;
}

.hero-button {
  min-width: 206px;
  height: 46px;
  border-radius: 999px;
  padding: 0 28px;
  font-size: 18px;
  transition: transform .25s ease, box-shadow .25s ease, border-color .25s ease;
}

.hero-button:hover,
.poster-card:hover,
.week-card:hover,
.editor-card:hover { transform: translateY(-3px); }

.hero-button-primary {
  border: 1px solid rgba(73, 212, 255, 0.85);
  background: linear-gradient(180deg, rgba(58, 191, 255, 0.98), rgba(15, 118, 255, 0.88));
  color: #fff;
}

.glow-pulse { animation: glowPulse 3s ease-in-out infinite; }

.hero-button-secondary {
  border: 1px solid rgba(255, 255, 255, 0.42);
  background: rgba(18, 22, 33, 0.26);
  color: #edf0f6;
  backdrop-filter: blur(8px);
}
.hero-button-secondary:hover { border-color: rgba(255, 255, 255, 0.65); }

.hero-dots { display: flex; align-items: center; gap: 10px; margin-top: 18px; }
.dot {
  width: 10px;
  height: 10px;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.36);
  transition: width .25s ease, background .25s ease, box-shadow .25s ease;
}
.dot.active {
  width: 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 0 12px rgba(255,255,255,.22);
}

.content-grid {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: minmax(0, 1.78fr) minmax(420px, 1.15fr) minmax(180px, 0.48fr);
  gap: 18px;
  align-items: start;
}

.left-column { display: flex; flex-direction: column; gap: 10px; }
.shelf { min-width: 0; }
.shelf-trending { margin-top: 2px; }
.section-title { margin-bottom: 12px; color: rgba(255, 255, 255, 0.96); font-size: 17px; font-weight: 500; }
.section-title.section-title-caps { font-size: 18px; text-transform: uppercase; }
.section-empty {
  border: 1px dashed rgba(255, 255, 255, 0.22);
  border-radius: 18px;
  padding: 22px 18px;
  color: rgba(255, 255, 255, 0.76);
  background: rgba(255, 255, 255, 0.04);
  font-size: 14px;
  line-height: 1.7;
}
.section-empty-tight {
  min-height: 104px;
  display: flex;
  align-items: center;
}
.poster-row { display: grid; gap: 14px; }
.poster-row-top, .poster-row-bottom { grid-template-columns: repeat(6, minmax(88px, 1fr)); }

.poster-card {
  border: 0;
  background: transparent;
  padding: 0;
  text-align: left;
  transition: transform .28s ease, filter .28s ease;
}
.poster-card:hover { filter: brightness(1.06); }

.poster-cover {
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 18px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.04);
  height: 150px;
  transition: border-color .28s ease, box-shadow .28s ease, transform .28s ease;
}
.poster-row-bottom .poster-cover { height: 126px; }
.poster-cover img, .week-poster-box img, .editor-cover img { width: 100%; height: 100%; object-fit: cover; display: block; }
.poster-card-active .poster-cover, .poster-card:hover .poster-cover {
  border-color: rgba(47, 200, 255, 0.92);
  box-shadow: 0 0 0 1px rgba(47, 200, 255, 0.14), 0 0 24px rgba(47, 200, 255, 0.55);
}
.poster-rating { display: flex; align-items: center; gap: 4px; margin-top: 7px; color: rgba(255, 255, 255, 0.76); font-size: 13px; }
.poster-rating i { color: #ffba45; font-size: 12px; }

.week-column { padding-top: 8px; }
.week-card {
  width: 100%;
  border: 1px solid rgba(47, 200, 255, 0.55);
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(38, 46, 60, 0.94), rgba(29, 35, 46, 0.98));
  box-shadow: 0 0 0 1px rgba(47, 200, 255, 0.08), 0 0 26px rgba(47, 200, 255, 0.34), inset 0 0 26px rgba(47, 200, 255, 0.05);
  display: grid;
  grid-template-columns: 206px 1fr;
  gap: 20px;
  align-items: center;
  min-height: 278px;
  padding: 18px;
  text-align: left;
  color: inherit;
  transition: transform .3s ease, box-shadow .3s ease, border-color .3s ease;
}
.week-card:hover {
  border-color: rgba(47, 200, 255, 0.78);
  box-shadow: 0 0 0 1px rgba(47, 200, 255, 0.14), 0 0 34px rgba(47, 200, 255, 0.42), inset 0 0 28px rgba(47, 200, 255, 0.08);
}
.week-poster-box { border-radius: 20px; overflow: hidden; background: #fff; height: 246px; }
.week-content { padding-right: 8px; }
.week-kicker { color: rgba(47, 200, 255, 0.95); font-size: 15px; margin-bottom: 10px; text-transform: uppercase; }
.week-name { color: #fff; font-size: 22px; line-height: 1.08; font-weight: 600; margin-bottom: 16px; }
.week-meta { color: rgba(255, 255, 255, 0.74); font-size: 15px; line-height: 1.62; }

.editors-column { display: flex; flex-direction: column; padding-top: 8px; }
.editor-card {
  border: 0;
  background: transparent;
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 12px;
  align-items: center;
  padding: 0;
  margin-bottom: 18px;
  color: inherit;
  text-align: left;
  transition: transform .25s ease;
}
.editor-cover {
  border-radius: 14px;
  overflow: hidden;
  height: 104px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.14);
  transition: border-color .25s ease, box-shadow .25s ease;
}
.editor-card:hover .editor-cover {
  border-color: rgba(47, 200, 255, 0.78);
  box-shadow: 0 0 22px rgba(47, 200, 255, 0.28);
}
.editor-name { color: rgba(255, 255, 255, 0.9); font-size: 16px; line-height: 1.25; }

@media (max-width: 1320px) {
  .content-grid {
    grid-template-columns: minmax(0, 1.45fr) minmax(360px, 1fr);
    grid-template-areas: 'left week' 'editors editors';
  }
  .left-column { grid-area: left; }
  .week-column { grid-area: week; }
  .editors-column {
    grid-area: editors;
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
    align-items: start;
  }
  .editors-column > .section-title {
    grid-column: 1 / -1;
    margin-bottom: 2px;
  }
  .editor-card { margin-bottom: 0; }
}

@media (max-width: 980px) {
  .hero-section { margin-top: -26px; min-height: 338px; }
  .hero-content { min-height: 338px; padding: 98px 16px 20px; }
  .hero-title { font-size: 58px; }
  .hero-subtitle { font-size: 18px; text-align: center; }
  .hero-actions { width: 100%; flex-direction: column; align-items: center; }
  .hero-button { width: min(100%, 260px); }
  .content-grid { grid-template-columns: 1fr; grid-template-areas: 'left' 'week' 'editors'; }
  .editors-column { display: grid; grid-template-columns: 1fr; }
  .editors-column > .section-title {
    grid-column: 1;
    margin-bottom: 4px;
  }
  .poster-row-top, .poster-row-bottom { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .poster-cover, .poster-row-bottom .poster-cover { height: 180px; }
  .week-card { grid-template-columns: 1fr; }
  .week-poster-box { height: 320px; }
}

@media (max-width: 560px) {
  .hero-title { font-size: 46px; }
  .hero-subtitle { font-size: 16px; }
  .poster-row-top, .poster-row-bottom { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .poster-cover, .poster-row-bottom .poster-cover { height: 170px; }
}
</style>
