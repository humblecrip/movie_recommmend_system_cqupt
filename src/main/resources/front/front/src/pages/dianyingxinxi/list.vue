<template>
  <div class="movie-route-redirect"></div>
</template>

<script>
function normalizeRedirectQuery(query) {
  const nextQuery = Object.assign({}, query || {}, {
    view: 'movies',
  })

  Object.keys(nextQuery).forEach(key => {
    const value = nextQuery[key]
    if (value === undefined || value === null || value === '') {
      delete nextQuery[key]
    }
  })

  return nextQuery
}

export default {
  created() {
    this.$router.replace({
      path: '/index/home',
      query: normalizeRedirectQuery(this.$route.query),
    })
  },
}
</script>
