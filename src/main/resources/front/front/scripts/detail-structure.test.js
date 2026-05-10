const assert = require('assert')
const fs = require('fs')
const path = require('path')

const detailPath = path.join(__dirname, '../src/pages/dianyingxinxi/detail.vue')
const source = fs.readFileSync(detailPath, 'utf8')

function has(text) {
  return source.includes(text)
}

function run() {
  assert.ok(has('class="hero-section"'), '应保留原型 Hero 区')
  assert.ok(has('影片概览'), '详情页应保留中文概览标题')
  assert.ok(has('相似影片'), '详情页应保留中文相似影片标题')
  assert.ok(has('查看详情'), '详情页应保留中文主按钮')
  assert.ok(has('加入收藏') || has('取消收藏'), '详情页应保留中文收藏按钮')
  assert.ok(has('更多信息'), '详情页应保留中文更多信息标题')

  assert.ok(!has('class="cinematic-topbar"'), '不应保留自定义顶部标题栏')
  assert.ok(!has('class="poster-thumb-row"'), '不应保留海报缩略图条')
  assert.ok(!has('Cast &amp; Crew'), '不应保留演员详情区')
  assert.ok(!has('class="score-panel"'), '不应保留额外统计卡片')

  console.log('detail-structure tests passed')
}

run()
