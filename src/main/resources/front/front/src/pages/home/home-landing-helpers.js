const {
  getPrimaryPoster,
} = require('../dianyingxinxi/detail-helpers')

const EMPTY_HERO_SLIDE = {
  placeholder: true,
  placeholderKey: 'hero-empty',
  heroTitle: '电影片库',
  heroSubtitle: '当前暂无可展示的轮播内容。',
  backdropUrl: '',
  actionUrl: '',
  sourceType: 'empty',
}

function normalizeTagline(text) {
  const content = String(text || '').replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
  if (!content) {
    return '影片简介暂未提供。'
  }
  return content.length > 72 ? `${content.slice(0, 72)}...` : content
}

function normalizeConfigSlideTitle(name) {
  const rawName = String(name || '').trim()
  if (!rawName || /^picture[\W_\d-]*$/i.test(rawName)) {
    return ''
  }
  return rawName.replace(/[_-]+/g, ' ').trim()
}

function normalizeConfigSlideSubtitle(item) {
  if (String((item && item.url) || '').trim()) {
    return '点击“立即观看”或“查看详情”将按后台配置跳转。'
  }
  return '当前轮播未配置跳转链接，点击后将回退到影片列表。'
}

function normalizeImageValue(value) {
  const image = String(value || '').split(',')[0].trim()
  if (!image || image === 'null' || image === 'undefined') {
    return ''
  }
  return image
}

function resolveAssetUrl(asset, baseUrl) {
  if (!asset) {
    return ''
  }
  if (/^https?:\/\//i.test(asset)) {
    return asset
  }
  return `${baseUrl || ''}${asset}`
}

function buildPosterUrl(movie, baseUrl) {
  return getPrimaryPoster(movie, baseUrl)
}

function normalizeHomeMovieRecord(item, index, baseUrl) {
  const movie = Object.assign({}, item || {})
  const identifier = movie.id || `fallback-${index}`
  const posterUrl = buildPosterUrl(movie, baseUrl)
  const detailText = movie.synopsis || movie.juqingjianjie || movie.dianyingxiangqing || movie.detailHtml
  return Object.assign(movie, {
    placeholder: false,
    placeholderKey: `movie-${identifier}`,
    sourceType: 'movie',
    heroTitle: movie.title || movie.dianyingmingcheng || '电影片库',
    heroSubtitle: normalizeTagline(detailText),
    coverUrl: posterUrl,
    backdropUrl: posterUrl,
    tagline: normalizeTagline(detailText),
    genre: movie.typeName || movie.dianyingleixing || '未分类',
    area: movie.regionName || movie.quyu || '未知区域',
    runtime: movie.runtime || '待定',
    releaseDate: movie.releaseDate || movie.shangyingshijian || '待定',
  })
}

function normalizeConfigHeroSlides(source, baseUrl) {
  return (Array.isArray(source) ? source : []).map((item, index) => {
    const imageUrl = resolveAssetUrl(normalizeImageValue(item && item.value), baseUrl)
    if (!imageUrl) {
      return null
    }
    const identifier = item && item.id ? item.id : `config-${index}`
    return {
      id: item && item.id ? item.id : null,
      placeholder: false,
      placeholderKey: `config-${identifier}`,
      sourceType: 'config',
      heroTitle: normalizeConfigSlideTitle(item && item.name),
      heroSubtitle: normalizeConfigSlideSubtitle(item),
      backdropUrl: imageUrl,
      coverUrl: imageUrl,
      actionUrl: String((item && item.url) || '').trim(),
      rawName: String((item && item.name) || '').trim(),
    }
  }).filter(Boolean)
}

function hydrateConfigHeroSlide(slide, detailPayload, baseUrl) {
  const detail = normalizeHomeMovieRecord(detailPayload, 0, baseUrl)
  return Object.assign({}, slide, {
    id: slide.id || detail.id,
    detailMovieId: detail.id || null,
    heroTitle: detail.heroTitle || slide.heroTitle || '',
    heroSubtitle: detail.heroSubtitle || slide.heroSubtitle,
    backdropUrl: slide.backdropUrl || detail.backdropUrl || '',
    coverUrl: slide.coverUrl || detail.coverUrl || '',
    fallbackBackdropUrl: detail.backdropUrl || '',
    fallbackCoverUrl: detail.coverUrl || '',
    tagline: detail.tagline || slide.heroSubtitle,
    genre: detail.genre || slide.genre,
    area: detail.area || slide.area,
    runtime: detail.runtime || slide.runtime,
    releaseDate: detail.releaseDate || slide.releaseDate,
    sourceType: 'movie',
  })
}

const exportedHelpers = {
  EMPTY_HERO_SLIDE,
  normalizeTagline,
  normalizeConfigHeroSlides,
  normalizeHomeMovieRecord,
  hydrateConfigHeroSlide,
}

if (typeof module !== 'undefined' && module) {
  const exportsDescriptor = Object.getOwnPropertyDescriptor(module, 'exports')
  if (!exportsDescriptor || exportsDescriptor.writable) {
    module.exports = exportedHelpers
  } else if (module.exports && typeof module.exports === 'object' && !Object.isFrozen(module.exports)) {
    Object.assign(module.exports, exportedHelpers)
  }
}
