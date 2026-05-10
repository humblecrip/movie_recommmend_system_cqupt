const assert = require('assert')
const fs = require('fs')
const path = require('path')

const panelPath = path.join(__dirname, '../src/pages/home/home-landing-panel.vue')
const source = fs.readFileSync(panelPath, 'utf8')

function expectStructure(condition, message) {
  assert.ok(condition, `${message} [检查文件: ${panelPath}]`)
}

function run() {
  expectStructure(source.includes('fetchMovieCollections'), '首页主面板应提供真实电影取数方法')
  expectStructure(source.includes("this.$http.get('appmovie/front/list'"), '首页主面板应通过后端电影接口拉取真实数据')
  expectStructure(source.includes('applyHomeMovieCollections'), '首页主面板应将真实电影数据分配到首页各模块')
  expectStructure(source.includes('this.fetchHomeContent()'), '首页创建后应主动拉取首页内容')
  expectStructure(source.includes("path: '/index/dianyingxinxiDetail'"), '首页电影卡片点击后应跳转电影详情路由')
  expectStructure(source.includes('this.openHeroTarget(this.currentHeroSlide)'), '首页主视觉按钮应复用当前轮播跳转逻辑')
  expectStructure(source.includes("this.$http.get(`appmovie/front/detail/${movieId}`)"), 'config 轮播应按详情链接反查电影详情补全标题和海报')
  expectStructure(source.includes('parseDetailRouteMovieId'), '首页应解析 config 轮播 actionUrl/url 中的电影详情 id')
  expectStructure(source.includes('hydrateConfigHeroSlides'), '首页应对 config 轮播做电影详情补全')
  expectStructure(source.includes("require('./home-landing-helpers')"), '首页主面板应复用独立的轮播数据归一化 helper')
  expectStructure(!source.includes('FEATURED ${index + 1}'), 'config 轮播不应继续回退为 FEATURED n 占位标题')
  expectStructure(source.includes('立即观看'), '首页主视觉主按钮应中文化')
  expectStructure(source.includes('查看详情'), '首页主视觉次按钮应中文化')
  expectStructure(source.includes('为你推荐'), '首页推荐标题应中文化')
  expectStructure(source.includes('热议影片'), '首页热议标题应中文化')
  expectStructure(source.includes('最新上映'), '首页最新上映标题应中文化')
  expectStructure(source.includes('高收藏影片'), '首页高收藏标题应中文化')

  console.log('home-landing-structure tests passed')
}

run()
