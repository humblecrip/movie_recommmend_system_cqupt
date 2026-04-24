function normalizePosterList(input) {
  if (!input) {
    return []
  }
  if (Array.isArray(input)) {
    return input
      .map(item => String(item || '').trim())
      .filter(Boolean)
  }
  return String(input)
    .split(',')
    .map(item => item.trim())
    .filter(Boolean)
}

function getPosterList(detail) {
  return normalizePosterList(detail && detail.haibao)
}

function resolvePosterUrl(poster, baseUrl) {
  if (!poster) {
    return ''
  }
  if (String(poster).substr(0, 4) === 'http') {
    return poster
  }
  return (baseUrl || '') + poster
}

function getPrimaryPoster(detail, baseUrl) {
  const posterList = getPosterList(detail)
  return resolvePosterUrl(posterList[0], baseUrl)
}

function formatScoreText(score) {
  if (score === undefined || score === null || score === '') {
    return 'N/A'
  }
  const value = Number(score)
  if (Number.isNaN(value)) {
    return 'N/A'
  }
  return value.toFixed(1)
}

function getMovieYear(dateText) {
  if (!dateText) {
    return '未知年份'
  }
  return String(dateText).slice(0, 4)
}

function formatHeroMeta(detail) {
  return {
    year: getMovieYear(detail && detail.shangyingshijian),
    runtime: '2h 16m',
    maturityRating: 'PG-13',
    overview: (detail && detail.juqingjianjie) || '暂无剧情简介',
    scoreText: formatScoreText(detail && detail.totalscore),
    releaseDateText: (detail && detail.shangyingshijian) || '上映日期待定',
  }
}

function sortByHotAndScore(movieA, movieB) {
  const clickDiff = Number(movieB.clicknum || 0) - Number(movieA.clicknum || 0)
  if (clickDiff !== 0) {
    return clickDiff
  }
  const scoreDiff = Number(movieB.totalscore || 0) - Number(movieA.totalscore || 0)
  if (scoreDiff !== 0) {
    return scoreDiff
  }
  return Number(movieB.id || 0) - Number(movieA.id || 0)
}

function buildSimilarMovies(currentMovie, movieList, limit) {
  const max = Number(limit || 6)
  const list = Array.isArray(movieList) ? movieList.slice() : []
  const currentId = currentMovie && currentMovie.id
  const currentType = currentMovie && currentMovie.dianyingleixing

  const sameType = list
    .filter(item => item && item.id !== currentId && item.dianyingleixing === currentType)
    .sort(sortByHotAndScore)

  if (sameType.length >= max) {
    return sameType.slice(0, max)
  }

  const usedIds = {}
  sameType.forEach(item => {
    usedIds[item.id] = true
  })

  const fallback = list
    .filter(item => item && item.id !== currentId && !usedIds[item.id])
    .sort(sortByHotAndScore)

  return sameType.concat(fallback).slice(0, max)
}

const exportedHelpers = {
  normalizePosterList,
  getPosterList,
  resolvePosterUrl,
  getPrimaryPoster,
  formatHeroMeta,
  buildSimilarMovies,
}

export {
  normalizePosterList,
  getPosterList,
  resolvePosterUrl,
  getPrimaryPoster,
  formatHeroMeta,
  buildSimilarMovies,
}

if (typeof module !== 'undefined' && module) {
  const exportsDescriptor = Object.getOwnPropertyDescriptor(module, 'exports')
  if (!exportsDescriptor || exportsDescriptor.writable) {
    module.exports = exportedHelpers
  } else if (module.exports && typeof module.exports === 'object' && !Object.isFrozen(module.exports)) {
    Object.assign(module.exports, exportedHelpers)
  }
}
