const assert = require('assert')
const path = require('path')

const helpersPath = path.join(__dirname, '../src/pages/home/home-landing-helpers.js')
const {
  normalizeHomeMovieRecord,
  hydrateConfigHeroSlide,
} = require(helpersPath)

function runTest(name, testFn) {
  try {
    testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  }
}

runTest('现代 appmovie 详情字段应被首页轮播正确归一化', () => {
  const movie = normalizeHomeMovieRecord({
    id: 20,
    title: '星际穿越',
    posterUrl: 'upload/movie_interstellar.jpg',
    posterUrls: ['upload/movie_interstellar.jpg'],
    synopsis: '宇航员穿越虫洞为人类寻找新的栖居地',
    typeName: '科幻',
    regionName: '美国',
    releaseDate: '2014-11-12',
    totalScore: 9.4,
  }, 0, 'http://localhost:8080/springbootdo4wek3z/')

  assert.equal(movie.heroTitle, '星际穿越')
  assert.equal(movie.heroSubtitle, '宇航员穿越虫洞为人类寻找新的栖居地')
  assert.equal(movie.coverUrl, 'http://localhost:8080/springbootdo4wek3z/upload/movie_interstellar.jpg')
  assert.equal(movie.backdropUrl, 'http://localhost:8080/springbootdo4wek3z/upload/movie_interstellar.jpg')
  assert.equal(movie.genre, '科幻')
  assert.equal(movie.area, '美国')
  assert.equal(movie.releaseDate, '2014-11-12')
})

runTest('config 轮播补全时应优先保留配置图并回填真实标题简介', () => {
  const slide = hydrateConfigHeroSlide({
    id: 2,
    heroTitle: '',
    heroSubtitle: '占位简介',
    backdropUrl: 'https://upload.wikimedia.org/wikipedia/en/0/0f/TheWanderingEarth2.jpg',
    coverUrl: 'https://upload.wikimedia.org/wikipedia/en/0/0f/TheWanderingEarth2.jpg',
    actionUrl: '#/index/dianyingxinxiDetail?id=2',
    rawName: 'picture2',
  }, {
    id: 2,
    title: '流浪地球2',
    posterUrl: 'upload/phase1_liulangdiqiu2.jpg',
    synopsis: '太阳即将毁灭，人类建造行星发动机寻找新家园。',
    typeName: '科幻',
    regionName: '中国大陆',
    releaseDate: '2023-01-22',
  }, 'http://localhost:8080/springbootdo4wek3z/')

  assert.equal(slide.heroTitle, '流浪地球2')
  assert.equal(slide.heroSubtitle, '太阳即将毁灭，人类建造行星发动机寻找新家园。')
  assert.equal(slide.backdropUrl, 'https://upload.wikimedia.org/wikipedia/en/0/0f/TheWanderingEarth2.jpg')
  assert.equal(slide.fallbackBackdropUrl, 'http://localhost:8080/springbootdo4wek3z/upload/phase1_liulangdiqiu2.jpg')
  assert.equal(slide.fallbackCoverUrl, 'http://localhost:8080/springbootdo4wek3z/upload/phase1_liulangdiqiu2.jpg')
})
