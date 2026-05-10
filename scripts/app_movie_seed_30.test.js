const assert = require('assert')
const path = require('path')

const seedFile = path.join(__dirname, 'app_movie_seed_30.json')
const movies = require(seedFile)

const existingTitles = new Set([
  '哪吒之魔童降世',
  '星际穿越',
  '我不是药神',
  '热辣滚烫',
  '寻梦环游记',
  '让子弹飞',
  '周处除三害',
  '你好，李焕英',
  '消失的她',
  '飞驰人生2',
  '铃芽之旅',
  '满江红',
  '封神第一部：朝歌风云',
  '长安三万里',
  '独行月球',
  '三大队',
  '忠犬八公',
  '八角笼中',
  '热烈',
  '流浪地球2',
])

function runTest(name, testFn) {
  try {
    testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  }
}

runTest('补数清单应包含 30 条真实且不重复的电影', () => {
  assert.strictEqual(movies.length, 30)

  const titles = movies.map(item => item.title)
  assert.strictEqual(new Set(titles).size, 30)

  titles.forEach(title => {
    assert.ok(!existingTitles.has(title), `片名不应与现有电影重复: ${title}`)
  })
})

runTest('每条补数记录都应包含最小详情字段', () => {
  movies.forEach((movie, index) => {
    const label = `第 ${index + 1} 条 ${movie.title || '未命名'}`
    assert.ok(movie.title && movie.title.trim(), `${label} 缺少 title`)
    assert.ok(movie.type && movie.type.trim(), `${label} 缺少 type`)
    assert.ok(movie.region && movie.region.trim(), `${label} 缺少 region`)
    assert.ok(movie.releaseDate && movie.releaseDate.trim(), `${label} 缺少 releaseDate`)
    assert.ok(movie.director && movie.director.trim(), `${label} 缺少 director`)
    assert.ok(movie.cast && movie.cast.trim(), `${label} 缺少 cast`)
    assert.ok(movie.synopsis && movie.synopsis.trim(), `${label} 缺少 synopsis`)
    assert.ok(movie.detailHtml && movie.detailHtml.trim(), `${label} 缺少 detailHtml`)
  })
})
