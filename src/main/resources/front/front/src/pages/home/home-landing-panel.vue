<template>
  <div class="home-landing-panel">
    <section class="hero-section fade-up delay-2" @mouseenter="stopHeroAutoplay" @mouseleave="startHeroAutoplay">
      <transition name="hero-fade" mode="out-in">
        <img
          :key="currentHeroSlide.placeholderKey"
          class="hero-backdrop"
          :src="heroBackdropSrc"
          alt="hero background"
          @error="heroImageError = true"
        >
      </transition>
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <transition name="hero-copy" mode="out-in">
          <div :key="currentHeroSlide.placeholderKey" class="hero-copy-wrap">
            <h1 class="hero-title">{{ currentHeroSlide.dianyingmingcheng }}</h1>
            <p class="hero-subtitle">{{ currentHeroSlide.tagline }}</p>
          </div>
        </transition>
        <div class="hero-actions">
          <button class="hero-button hero-button-primary glow-pulse" type="button" @click="watchFeatured">Watch Now</button>
          <button class="hero-button hero-button-secondary" type="button" @click="showFeaturedInfo">More Info</button>
        </div>
        <div class="hero-dots">
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
          <div class="section-title">Recommended for You</div>
          <div class="poster-row poster-row-top">
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
        </section>

        <section class="shelf shelf-trending">
          <div class="section-title">Trending Now</div>
          <div class="poster-row poster-row-bottom">
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
        </section>
      </div>

      <section class="week-column">
        <div class="section-title section-title-caps">MOVIE OF THE WEEK</div>
        <button class="week-card" type="button" @click="toDetail(weekMovie)">
          <div class="week-poster-box">
            <img :src="getMovieCover(weekMovie)" :alt="weekMovie.dianyingmingcheng" @error="markImageError(getItemKey(weekMovie, 'week'))">
          </div>
          <div class="week-content">
            <div class="week-kicker">MOVIE OF THE WEEK</div>
            <div class="week-name">{{ weekMovie.dianyingmingcheng }}</div>
            <div class="week-meta">Release Date: {{ weekMovie.releaseDate }}</div>
            <div class="week-meta">Rating: {{ getScore(weekMovie) }}/5</div>
            <div class="week-meta">Genre: {{ weekMovie.genre }}</div>
            <div class="week-meta">Area: {{ weekMovie.area }}</div>
            <div class="week-meta">Runtime: {{ weekMovie.runtime }}</div>
          </div>
        </button>
      </section>

      <aside class="editors-column">
        <div class="section-title section-title-caps">EDITORS' CHOICE</div>
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
      </aside>
    </section>
  </div>
</template>

<script>
const FALLBACK_HERO_SLIDES = [
  {
    placeholder: true,
    placeholderKey: 'hero-midnight-sky',
    dianyingmingcheng: 'THE MIDNIGHT SKY',
    tagline: 'A dystopian thriller.',
    backdropUrl: 'https://upload.wikimedia.org/wikipedia/commons/6/68/Majestic_Cinema%2C_City_Square%2C_interior_view%2C_1923.jpg',
  },
  {
    placeholder: true,
    placeholderKey: 'hero-forbidden-planet',
    dianyingmingcheng: 'FORBIDDEN PLANET',
    tagline: 'A radiant sci-fi classic.',
    backdropUrl: 'https://upload.wikimedia.org/wikipedia/commons/5/50/Forbiddenplanetposter.jpg',
  },
  {
    placeholder: true,
    placeholderKey: 'hero-sunset-boulevard',
    dianyingmingcheng: 'SUNSET BOULEVARD',
    tagline: 'A dark Hollywood noir.',
    backdropUrl: 'https://upload.wikimedia.org/wikipedia/commons/1/14/Sunset_Boulevard_%281950_poster%29.jpg',
  },
]

const FALLBACK_RECOMMENDED_MOVIES = [
  { placeholder: true, placeholderKey: 'poster-dracula', dianyingmingcheng: 'Dracula', totalscore: 4.8, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/a/a6/Dracula_%281931_film_poster_-_Style_A%29.jpg' },
  { placeholder: true, placeholderKey: 'poster-wonderful-life', dianyingmingcheng: 'It\'s a Wonderful Life', totalscore: 4.9, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/2/25/It%27s_a_Wonderful_Life_%281946_poster%29.jpeg' },
  { placeholder: true, placeholderKey: 'poster-creature', dianyingmingcheng: 'Creature from the Black Lagoon', totalscore: 4.8, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/5/55/Creature_from_the_Black_Lagoon_poster.jpg' },
  { placeholder: true, placeholderKey: 'poster-forbidden-planet', dianyingmingcheng: 'Forbidden Planet', totalscore: 4.8, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/5/50/Forbiddenplanetposter.jpg' },
  { placeholder: true, placeholderKey: 'poster-rebel', dianyingmingcheng: 'Rebel Without a Cause', totalscore: 4.8, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/6/67/Rebel_Without_a_Cause_%281955_poster%29.jpg' },
  { placeholder: true, placeholderKey: 'poster-day-earth', dianyingmingcheng: 'The Day the Earth Stood Still', totalscore: 4.8, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/6/63/The_Day_the_Earth_Stood_Still_%281951_poster%29.jpeg' },
]

const FALLBACK_TRENDING_MOVIES = [
  { placeholder: true, placeholderKey: 'poster-miracle-34', dianyingmingcheng: 'Miracle on 34th Street', totalscore: 4.8, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/0/0f/Miracle_on_34th_Street_%281947_film_poster%29.jpg' },
  { placeholder: true, placeholderKey: 'poster-jaws', dianyingmingcheng: 'Jaws', totalscore: 4.8, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/9/93/Jaws_movie_poster.png' },
  { placeholder: true, placeholderKey: 'poster-wizard-oz', dianyingmingcheng: 'Wizard of Oz', totalscore: 4.8, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/6/69/Wizard_of_oz_movie_poster.jpg' },
  { placeholder: true, placeholderKey: 'poster-plan9', dianyingmingcheng: 'Plan 9 from Outer Space', totalscore: 4.8, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/b/bf/Plan_9_Alternative_poster.jpg' },
  { placeholder: true, placeholderKey: 'poster-attack-50', dianyingmingcheng: 'Attack of the 50 Foot Woman', totalscore: 4.8, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/e/e5/Attackofthe50ftwoman.jpg' },
  { placeholder: true, placeholderKey: 'poster-sunset', dianyingmingcheng: 'Sunset Boulevard', totalscore: 4.8, coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/1/14/Sunset_Boulevard_%281950_poster%29.jpg' },
]

const FALLBACK_WEEK_MOVIE = {
  placeholder: true,
  placeholderKey: 'week-forbidden-planet',
  dianyingmingcheng: 'FORBIDDEN PLANET',
  totalscore: 4.8,
  genre: 'Sci-Fi',
  area: 'United States',
  runtime: '98 min',
  releaseDate: '1956-03-15',
  coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/5/50/Forbiddenplanetposter.jpg',
}

const FALLBACK_EDITORS_CHOICE = [
  { placeholder: true, placeholderKey: 'editor-dracula', dianyingmingcheng: 'Dracula', coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/a/a6/Dracula_%281931_film_poster_-_Style_A%29.jpg' },
  { placeholder: true, placeholderKey: 'editor-wonderful-life', dianyingmingcheng: 'It\'s a Wonderful Life', coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/2/25/It%27s_a_Wonderful_Life_%281946_poster%29.jpeg' },
  { placeholder: true, placeholderKey: 'editor-creature', dianyingmingcheng: 'Creature from the Black Lagoon', coverUrl: 'https://upload.wikimedia.org/wikipedia/commons/5/55/Creature_from_the_Black_Lagoon_poster.jpg' },
]

function cloneItems(list) {
  return (list || []).map(item => Object.assign({}, item))
}

function normalizeBackdropUrl(item, baseUrl) {
  if (!item || !item.haibao) {
    return ''
  }
  const poster = String(item.haibao).split(',')[0]
  if (!poster) {
    return ''
  }
  if (/^https?:\/\//.test(poster)) {
    return poster
  }
  return `${baseUrl}${poster}`
}

function normalizeTagline(text) {
  const content = String(text || '').replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
  if (!content) {
    return 'A featured movie from our curated collection.'
  }
  return content.length > 72 ? `${content.slice(0, 72)}...` : content
}

function normalizeMovieRecord(item, index, baseUrl) {
  const movie = Object.assign({}, item || {})
  const identifier = movie.id || `fallback-${index}`
  return Object.assign(movie, {
    placeholder: false,
    placeholderKey: `movie-${identifier}`,
    coverUrl: normalizeBackdropUrl(movie, baseUrl),
    backdropUrl: normalizeBackdropUrl(movie, baseUrl),
    tagline: normalizeTagline(movie.juqingjianjie || movie.dianyingxiangqing),
    genre: movie.dianyingleixing || '未分类',
    area: movie.quyu || '未知区域',
    runtime: movie.runtime || 'TBA',
    releaseDate: movie.shangyingshijian || '待定',
  })
}

function takeLoopItems(source, count, startIndex) {
  if (!Array.isArray(source) || !source.length || count <= 0) {
    return []
  }
  const list = []
  for (let index = 0; index < count; index++) {
    list.push(source[(startIndex + index) % source.length])
  }
  return list
}

export default {
  data() {
    return {
      baseUrl: '',
      imageErrors: {},
      heroImageError: false,
      heroIndex: 0,
      heroTimer: null,
      fallbackHero: require('@/assets/login-bg.jpg'),
      fallbackPoster: require('@/assets/chapter.jpg'),
      heroSlides: cloneItems(FALLBACK_HERO_SLIDES),
      recommendedMovies: cloneItems(FALLBACK_RECOMMENDED_MOVIES),
      trendingMovies: cloneItems(FALLBACK_TRENDING_MOVIES),
      weekMovie: Object.assign({}, FALLBACK_WEEK_MOVIE),
      editorsChoice: cloneItems(FALLBACK_EDITORS_CHOICE),
    }
  },
  computed: {
    currentHeroSlide() {
      return this.heroSlides[this.heroIndex] || this.heroSlides[0]
    },
    heroBackdropSrc() {
      if (this.heroImageError) {
        return this.fallbackHero
      }
      return this.currentHeroSlide.backdropUrl || this.fallbackHero
    },
  },
  created() {
    this.baseUrl = this.$config.baseUrl
    this.fetchHomeMovies()
  },
  mounted() {
    this.startHeroAutoplay()
  },
  beforeDestroy() {
    this.stopHeroAutoplay()
  },
  methods: {
    async fetchHomeMovies() {
      try {
        const res = await this.$http.get('dianyingxinxi/list', {
          params: {
            page: 1,
            limit: 18,
            sort: 'clicknum',
            order: 'desc',
          },
        })
        const list = (((res || {}).data || {}).data || {}).list || []
        if (res && res.data && res.data.code === 0 && list.length) {
          this.applyHomeMovieCollections(list)
          return
        }
      } catch (error) {}
      this.resetHomeMovieCollections()
    },
    resetHomeMovieCollections() {
      this.heroSlides = cloneItems(FALLBACK_HERO_SLIDES)
      this.recommendedMovies = cloneItems(FALLBACK_RECOMMENDED_MOVIES)
      this.trendingMovies = cloneItems(FALLBACK_TRENDING_MOVIES)
      this.weekMovie = Object.assign({}, FALLBACK_WEEK_MOVIE)
      this.editorsChoice = cloneItems(FALLBACK_EDITORS_CHOICE)
    },
    applyHomeMovieCollections(list) {
      const normalizedList = (list || []).map((item, index) => normalizeMovieRecord(item, index, this.baseUrl))
      this.heroSlides = takeLoopItems(normalizedList, 3, 0)
      this.recommendedMovies = takeLoopItems(normalizedList, 6, 0)
      this.trendingMovies = takeLoopItems(normalizedList, 6, Math.min(6, Math.max(normalizedList.length - 1, 0)))
      this.weekMovie = takeLoopItems(normalizedList, 1, 0)[0] || Object.assign({}, FALLBACK_WEEK_MOVIE)
      this.editorsChoice = takeLoopItems(normalizedList, 3, Math.min(3, Math.max(normalizedList.length - 1, 0)))
      this.heroIndex = 0
      this.heroImageError = false
    },
    getItemKey(item, prefix) {
      if (!item) {
        return prefix + '-fallback'
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
      if (item && item.totalscore) {
        return Number(item.totalscore).toFixed(1)
      }
      return '4.8'
    },
    startHeroAutoplay() {
      this.stopHeroAutoplay()
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
      this.heroIndex = index
      this.heroImageError = false
      this.startHeroAutoplay()
    },
    watchFeatured() {
      this.toDetail(this.currentHeroSlide)
    },
    showFeaturedInfo() {
      this.toDetail(this.currentHeroSlide)
    },
    toDetail(item) {
      if (item && item.id && !item.placeholder) {
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
