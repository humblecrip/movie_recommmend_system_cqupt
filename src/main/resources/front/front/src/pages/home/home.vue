<template>
  <cinema-shell
    :active-nav="activeView === 'movies' ? 'movies' : 'home'"
    :search-keyword.sync="searchKeyword"
    @search="handleShellSearch"
    @open-home="openHomeView"
    @open-movies="openMoviesView"
  >
    <home-landing-panel
      v-if="activeView === 'home'"
      @open-movies="openMoviesView"
    />

    <movie-discover-panel v-else />
  </cinema-shell>
</template>

<script>
import CinemaShell from '../../components/CinemaShell'
import HomeLandingPanel from './home-landing-panel'
import MovieDiscoverPanel from '../dianyingxinxi/movie-discover-panel'

function normalizeHomeQuery(query) {
  const nextQuery = Object.assign({}, query || {})
  Object.keys(nextQuery).forEach(key => {
    const value = nextQuery[key]
    if (value === undefined || value === null || value === '') {
      delete nextQuery[key]
    }
  })
  return nextQuery
}

export default {
  components: {
    CinemaShell,
    HomeLandingPanel,
    MovieDiscoverPanel,
  },
  data() {
    return {
      activeView: 'home',
      searchKeyword: '',
    }
  },
  created() {
    this.syncViewState(this.$route)
  },
  watch: {
    $route(newRoute) {
      this.syncViewState(newRoute)
    },
  },
  methods: {
    syncViewState(route) {
      const query = (route && route.query) || {}
      this.activeView = query.view === 'movies' ? 'movies' : 'home'
      this.searchKeyword = query.indexQueryCondition ? String(query.indexQueryCondition) : ''
    },
    pushHomeQuery(query) {
      this.$router.push({
        path: '/index/home',
        query: normalizeHomeQuery(query),
      })
    },
    openHomeView() {
      this.pushHomeQuery({})
    },
    openMoviesView(extraQuery) {
      const currentQuery = Object.assign({}, this.$route.query || {})
      const nextQuery = Object.assign({}, currentQuery, extraQuery || {}, {
        view: 'movies',
      })
      this.pushHomeQuery(nextQuery)
    },
    handleShellSearch() {
      const keyword = this.searchKeyword.trim()
      if (keyword) {
        this.openMoviesView({
          indexQueryCondition: keyword,
        })
        return
      }
      this.openMoviesView({
        indexQueryCondition: undefined,
      })
    },
  },
}
</script>
