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
  assert.ok(has('class="back_box prototype-back-box"'), '详情页应保留返回按钮容器')
  assert.ok(!has('class="back_box prototype-back-box" v-if="centerType || storeupType"'), '返回按钮不应只在中心页或收藏页场景显示')
  assert.ok(has('hasHistoryBack()'), '普通详情页返回应判断是否存在可用浏览历史')
  assert.ok(has("getDefaultBackRoute()"), '普通详情页返回应提供首页电影视图兜底路由')
  assert.ok(has("path: '/index/home'"), '普通详情页返回无历史时应兜底到首页')
  assert.ok(has("view: 'movies'"), '首页兜底应进入电影视图')
  assert.ok(has('.back_box.prototype-back-box'), '详情页返回容器应覆写全局 back_box 样式')
  assert.ok(has('width: auto;'), '详情页返回容器不应继承全局整行宽度')
  assert.ok(has('background: transparent;'), '详情页返回容器不应继承全局白色背景')
  assert.ok(has('padding: 0;'), '详情页返回容器不应继承全局大 padding')
  assert.ok(has('.prototype-back-box .backBtn.el-button'), '详情页返回按钮应使用局部玻璃按钮样式')
  assert.ok(has('backdrop-filter: blur(14px);'), '详情页返回按钮应保留玻璃质感')
  assert.ok(has('border-radius: 999px;'), '详情页返回按钮应保留胶囊圆角')

  assert.ok(!has('class="cinematic-topbar"'), '不应保留自定义顶部标题栏')
  assert.ok(!has('class="poster-thumb-row"'), '不应保留海报缩略图条')
  assert.ok(!has('Cast &amp; Crew'), '不应保留演员详情区')
  assert.ok(!has('class="score-panel"'), '不应保留额外统计卡片')

  console.log('detail-structure tests passed')
}

run()
