<template>
  <cinema-shell
    active-nav="my-list"
    @open-home="openHomeView"
    @open-movies="openMoviesView"
  >
    <div class="storeup-content-page">
      <storeup-panel :hide-header="true"></storeup-panel>
    </div>
  </cinema-shell>
</template>

<script>
import CinemaShell from '../../components/CinemaShell'
import StoreupPanel from './storeup-panel'

export default {
  components: {
    CinemaShell,
    StoreupPanel,
  },
  methods: {
    openHomeView() {
      this.$router.push({
        path: '/index/home',
      })
    },
    openMoviesView() {
      this.$router.push({
        path: '/index/home',
        query: {
          view: 'movies',
        },
      })
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.storeup-content-page {
  min-height: calc(100vh - 90px);
  padding: 48px 32px 88px;
  background:
    radial-gradient(circle at top left, rgba(255, 198, 57, 0.12), transparent 28%),
    radial-gradient(circle at right bottom, rgba(63, 92, 188, 0.16), transparent 30%),
    linear-gradient(180deg, #0b1326 0%, #08101d 100%);
}

@media (max-width: 768px) {
  .storeup-content-page {
    min-height: calc(100vh - 72px);
    padding: 28px 18px 64px;
  }
}
</style>
