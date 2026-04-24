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
  assert.ok(has('Overview'), '应保留 Overview 标题')
  assert.ok(has('Similar Movies'), '应保留 Similar Movies 标题')
  assert.ok(has('Watch Trailer'), '应保留 Watch Trailer 按钮')
  assert.ok(has('Add to My List'), '应保留 Add to My List 按钮')

  assert.ok(!has('class="cinematic-topbar"'), '不应保留自定义顶部标题栏')
  assert.ok(!has('class="poster-thumb-row"'), '不应保留海报缩略图条')
  assert.ok(!has('Cast &amp; Crew'), '不应保留演员详情区')
  assert.ok(!has('class="score-panel"'), '不应保留额外统计卡片')

  console.log('detail-structure tests passed')
}

run()
