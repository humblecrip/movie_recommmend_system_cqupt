const assert = require('assert')
const fs = require('fs')
const path = require('path')
const vm = require('vm')

const helperPath = path.join(__dirname, '../src/pages/dianyingxinxi/detail-helpers.js')
const {
  getPosterList,
  getPrimaryPoster,
  formatHeroMeta,
  buildSimilarMovies,
} = require(helperPath)

const sampleMovie = {
  id: 1,
  haibao: 'upload/a.jpg,upload/b.jpg',
  shangyingshijian: '2025-01-01',
  totalscore: 8.5,
  juqingjianjie: '测试简介',
  dianyingleixing: '动作',
  clicknum: 88,
}

function run() {
  const helperSource = fs.readFileSync(helperPath, 'utf8')
  const sourceWithoutEsmExport = helperSource.replace(/export\s*\{[\s\S]*?\}\s*/m, '')
  const sandbox = {
    module: {},
  }
  Object.defineProperty(sandbox.module, 'exports', {
    value: {},
    writable: false,
    configurable: true,
  })

  assert.doesNotThrow(
    () => vm.runInNewContext(`'use strict';\n${sourceWithoutEsmExport}`, sandbox),
    'helper 在只读 exports 环境中不应抛错'
  )

  assert.deepStrictEqual(
    getPosterList(sampleMovie),
    ['upload/a.jpg', 'upload/b.jpg'],
    '应能从 haibao 拆出海报数组'
  )

  assert.strictEqual(
    getPrimaryPoster(sampleMovie, 'http://localhost:8080/springbootdo4wek3z/'),
    'http://localhost:8080/springbootdo4wek3z/upload/a.jpg',
    '应能得到首图完整地址'
  )

  assert.deepStrictEqual(
    formatHeroMeta(sampleMovie),
    {
      year: '2025',
      runtime: '2小时16分钟',
      maturityRating: '建议13岁以上观看',
      overview: '测试简介',
      scoreText: '8.5',
      releaseDateText: '2025-01-01',
    },
    '应能格式化首屏元信息'
  )

  const similarMovies = buildSimilarMovies(
    sampleMovie,
    [
      sampleMovie,
      { id: 2, dianyingleixing: '动作', clicknum: 10, totalscore: 9.3 },
      { id: 3, dianyingleixing: '动作', clicknum: 105, totalscore: 8.0 },
      { id: 4, dianyingleixing: '喜剧', clicknum: 999, totalscore: 9.9 },
      { id: 5, dianyingleixing: '动作', clicknum: 105, totalscore: 8.8 },
    ],
    3
  )

  assert.deepStrictEqual(
    similarMovies.map(item => item.id),
    [5, 3, 2],
    '相似电影应排除当前电影，优先同类型，再按点击量和评分排序'
  )

  console.log('detail-helpers tests passed')
}

run()
