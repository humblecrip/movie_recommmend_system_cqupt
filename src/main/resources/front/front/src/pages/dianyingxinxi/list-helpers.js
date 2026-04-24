function extractMovieYear(movie = {}) {
  const primary = String(movie.shangyingshijian || '').match(/\d{4}/)
  if (primary) return primary[0]
  const fallback = String(movie.addtime || '').match(/\d{4}/)
  if (fallback) return fallback[0]
  return '未知年份'
}

function normalizeMovieScore(score) {
  const normalized = Number(score)
  return Number.isFinite(normalized) ? normalized : 0
}

function buildYearOptions(list = []) {
  return Array.from(
    new Set(list.map(item => extractMovieYear(item)).filter(year => year !== '未知年份'))
  ).sort((a, b) => Number(b) - Number(a))
}

function filterMovieList(list = [], filters = {}) {
  const normalizeFilterValue = value => (value === null || value === undefined ? '' : String(value).trim())
  const normalizedGenre = normalizeFilterValue(filters.genre)
  const normalizedYear = normalizeFilterValue(filters.year)
  const normalizedRating = normalizeFilterValue(filters.rating)

  return list.filter(item => {
    const matchesGenre =
      !normalizedGenre ||
      normalizedGenre === 'All' ||
      String(item.dianyingleixing || '') === normalizedGenre
    const movieYear = extractMovieYear(item)
    const matchesYear = !normalizedYear || normalizedYear === 'Any' || movieYear === normalizedYear
    const score = normalizeMovieScore(item.totalscore)
    const matchesRating =
      !normalizedRating ||
      normalizedRating === 'Any' ||
      (normalizedRating === '9.0+' && score >= 9) ||
      (normalizedRating === '8.0+' && score >= 8) ||
      (normalizedRating === '7.0+' && score >= 7) ||
      !['9.0+', '8.0+', '7.0+'].includes(normalizedRating)
    return matchesGenre && matchesYear && matchesRating
  })
}

function paginateMovieList(list = [], page = 1, pageSize = 24) {
  const parsedPageSize = Number(pageSize)
  const safePageSize =
    Number.isFinite(parsedPageSize) && Number.isInteger(parsedPageSize) && parsedPageSize >= 1
      ? parsedPageSize
      : 24
  const total = list.length
  const totalPage = total === 0 ? 0 : Math.ceil(total / safePageSize)
  const parsedPage = Number(page)
  const requestedPage = Number.isFinite(parsedPage) ? Math.floor(parsedPage) : 1
  const safePage = totalPage === 0 ? 1 : Math.min(Math.max(requestedPage, 1), totalPage)
  const start = (safePage - 1) * safePageSize
  return {
    total,
    totalPage,
    pageList: list.slice(start, start + safePageSize),
  }
}

function resolveSortRequest(sortBy = 'Popularity') {
  const mapping = {
    Popularity: { sort: 'clicknum', order: 'desc' },
    'Newest First': { sort: 'addtime', order: 'desc' },
    'Highest Rated': { sort: 'totalscore', order: 'desc' },
    'A-Z': { sort: 'dianyingmingcheng', order: 'asc' },
  }
  return mapping[sortBy] || mapping.Popularity
}

module.exports = {
  extractMovieYear,
  normalizeMovieScore,
  buildYearOptions,
  filterMovieList,
  paginateMovieList,
  resolveSortRequest,
}
