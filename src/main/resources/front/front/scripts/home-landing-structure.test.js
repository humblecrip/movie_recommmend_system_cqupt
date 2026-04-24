const assert = require('assert')
const fs = require('fs')
const path = require('path')

const panelPath = path.join(__dirname, '../src/pages/home/home-landing-panel.vue')
const source = fs.readFileSync(panelPath, 'utf8')

function expectStructure(condition, message) {
  assert.ok(condition, `${message} [检查文件: ${panelPath}]`)
}

function run() {
  expectStructure(source.includes('fetchHomeMovies'), '首页主面板应提供真实电影取数方法')
  expectStructure(source.includes("this.$http.get('dianyingxinxi/list'"), '首页主面板应通过后端电影接口拉取真实数据')
  expectStructure(source.includes('applyHomeMovieCollections'), '首页主面板应将真实电影数据分配到首页各模块')
  expectStructure(source.includes('this.fetchHomeMovies()'), '首页创建后应主动拉取电影数据')
  expectStructure(source.includes("path: '/index/dianyingxinxiDetail'"), '首页电影卡片点击后应跳转电影详情路由')
  expectStructure(source.includes('this.toDetail(this.currentHeroSlide)'), '首页主视觉按钮应复用当前电影详情跳转逻辑')

  console.log('home-landing-structure tests passed')
}

run()
