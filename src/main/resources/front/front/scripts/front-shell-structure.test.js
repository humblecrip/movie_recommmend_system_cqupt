const assert = require('assert')
const fs = require('fs')
const path = require('path')

function read(filePath) {
  return fs.readFileSync(filePath, 'utf8')
}

function runTest(name, testFn) {
  try {
    testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  }
}

const homeSource = read(path.join(__dirname, '../src/pages/home/home.vue'))
const listSource = read(path.join(__dirname, '../src/pages/dianyingxinxi/list.vue'))
const storeupSource = read(path.join(__dirname, '../src/pages/storeup/list.vue'))
const storeupPanelSource = read(path.join(__dirname, '../src/pages/storeup/storeup-panel.vue'))
const shellSource = read(path.join(__dirname, '../src/components/CinemaShell.vue'))

runTest('共享前台壳子组件包含统一头部与头像同步逻辑', () => {
  assert.ok(shellSource.includes('class="topbar fade-slide-down"'))
  assert.ok(shellSource.includes('getFrontIdentityState'))
  assert.ok(shellSource.includes('front-avatar-updated'))
  assert.ok(shellSource.includes('openMovies'))
  assert.ok(shellSource.includes('goProfile'))
})

runTest('首页作为统一前台容器承载首页与电影视图', () => {
  assert.ok(homeSource.includes('<cinema-shell'))
  assert.ok(homeSource.includes('CinemaShell'))
  assert.ok(homeSource.includes('activeView'))
  assert.ok(homeSource.includes('home-landing-panel'))
  assert.ok(homeSource.includes('movie-discover-panel'))
  assert.ok(homeSource.includes('@open-movies'))
})

runTest('旧电影列表路由退化为回首页的兼容跳转壳', () => {
  assert.ok(!listSource.includes('<cinema-shell'))
  assert.ok(listSource.includes("$router.replace"))
  assert.ok(listSource.includes("path: '/index/home'"))
  assert.ok(listSource.includes("view: 'movies'"))
})

runTest('收藏独立页应隐藏收藏内容页头块', () => {
  assert.ok(storeupSource.includes(':hide-header="true"'))
  assert.ok(storeupPanelSource.includes('hideHeader'))
  assert.ok(storeupPanelSource.includes('v-if="!hideHeader"'))
})
