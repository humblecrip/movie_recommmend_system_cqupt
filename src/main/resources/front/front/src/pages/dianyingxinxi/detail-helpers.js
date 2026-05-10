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

function getAppPosterInput(detail) {
  if (!detail) {
    return ''
  }
  if (detail.posterUrls !== undefined && detail.posterUrls !== null) {
    return detail.posterUrls
  }
  if (detail.posterUrl !== undefined && detail.posterUrl !== null && detail.posterUrl !== '') {
    return detail.posterUrl
  }
  return detail.haibao
}

function getPosterList(detail) {
  return normalizePosterList(getAppPosterInput(detail))
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

function pickValue(source, keys, fallback) {
  if (!source) {
    return fallback
  }
  for (let index = 0; index < keys.length; index++) {
    const value = source[keys[index]]
    if (value !== undefined && value !== null && value !== '') {
      return value
    }
  }
  return fallback
}

function normalizePosterString(source) {
  const posters = normalizePosterList(pickValue(source, ['posterUrls', 'posterUrl', 'haibao'], ''))
  return posters.join(',')
}

function normalizeAppMovieRecord(source) {
  const movie = Object.assign({}, source || {})
  const posterString = normalizePosterString(movie)
  return Object.assign(movie, {
    id: pickValue(movie, ['id'], movie.id),
    legacyDianyingxinxiId: pickValue(movie, ['legacyDianyingxinxiId'], movie.legacyDianyingxinxiId),
    dianyingmingcheng: pickValue(movie, ['title', 'dianyingmingcheng'], '未命名电影'),
    dianyingleixing: pickValue(movie, ['typeName', 'dianyingleixing'], '未分类'),
    haibao: posterString,
    quyu: pickValue(movie, ['regionName', 'quyu'], ''),
    shangyingshijian: pickValue(movie, ['releaseDate', 'shangyingshijian'], ''),
    daoyan: pickValue(movie, ['directorName', 'daoyan'], ''),
    zhuyan: pickValue(movie, ['castNames', 'zhuyan'], ''),
    juqingjianjie: pickValue(movie, ['synopsis', 'juqingjianjie'], ''),
    dianyingxiangqing: pickValue(movie, ['detailHtml', 'dianyingxiangqing', 'synopsis', 'juqingjianjie'], ''),
    thumbsupnum: Number(pickValue(movie, ['likeCount', 'thumbsupnum'], 0) || 0),
    crazilynum: Number(pickValue(movie, ['dislikeCount', 'crazilynum'], 0) || 0),
    clicknum: Number(pickValue(movie, ['clickCount', 'clicknum'], 0) || 0),
    discussnum: Number(pickValue(movie, ['commentCount', 'discussnum'], 0) || 0),
    storeupnum: Number(pickValue(movie, ['favoriteCount', 'storeupnum'], 0) || 0),
    totalscore: Number(pickValue(movie, ['totalScore', 'totalscore'], 0) || 0),
  })
}

function normalizeAppMovieList(list) {
  return (Array.isArray(list) ? list : []).map(item => normalizeAppMovieRecord(item))
}

function extractAppMovieList(payload) {
  const data = payload && payload.data ? payload.data : payload
  if (Array.isArray(data)) {
    return data
  }
  if (!data || typeof data !== 'object') {
    return []
  }
  if (Array.isArray(data.list)) {
    return data.list
  }
  if (Array.isArray(data.records)) {
    return data.records
  }
  if (Array.isArray(data.rows)) {
    return data.rows
  }
  if (Array.isArray(data.movies)) {
    return data.movies
  }
  if (Array.isArray(data.similarMovies)) {
    return data.similarMovies
  }
  if (Array.isArray(data.types)) {
    return data.types
  }
  return []
}

function extractAppMovieDetail(payload) {
  const data = payload && payload.data ? payload.data : payload
  if (!data || typeof data !== 'object') {
    return {}
  }
  return data.movie || data.detail || data
}

function formatScoreText(score) {
  if (score === undefined || score === null || score === '') {
    return '暂无'
  }
  const value = Number(score)
  if (Number.isNaN(value)) {
    return '暂无'
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
    runtime: '2小时16分钟',
    maturityRating: '建议13岁以上观看',
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
  normalizeAppMovieRecord,
  normalizeAppMovieList,
  extractAppMovieList,
  extractAppMovieDetail,
  formatHeroMeta,
  buildSimilarMovies,
}

export {
  normalizePosterList,
  getPosterList,
  resolvePosterUrl,
  getPrimaryPoster,
  normalizeAppMovieRecord,
  normalizeAppMovieList,
  extractAppMovieList,
  extractAppMovieDetail,
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
