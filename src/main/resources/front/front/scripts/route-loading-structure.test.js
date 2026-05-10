const assert = require('assert')
const fs = require('fs')
const path = require('path')

function read(relativePath) {
  return fs.readFileSync(path.join(__dirname, relativePath), 'utf8')
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

const routerSource = read('../src/router/router.js')
const appSource = read('../src/App.vue')
const curtainSource = read('../src/components/RouteLoadingCurtain.vue')

runTest('路由层应接入全局幕布状态控制', () => {
  assert.ok(routerSource.includes('beginRouteLoading'))
  assert.ok(routerSource.includes('finishRouteLoading'))
  assert.ok(routerSource.includes('failRouteLoading'))
  assert.ok(routerSource.includes('shouldTrackRouteLoading'))
  assert.ok(routerSource.includes('router.onError'))
})

runTest('App 根组件应挂载全局幕布组件', () => {
  assert.ok(appSource.includes('<route-loading-curtain'))
  assert.ok(appSource.includes('routeLoadingState.visible'))
  assert.ok(appSource.includes('RouteLoadingCurtain'))
})

runTest('幕布组件应包含品牌文案与全屏遮罩结构', () => {
  assert.ok(curtainSource.includes('class="route-loading-curtain"'))
  assert.ok(curtainSource.includes('CINEMA'))
  assert.ok(curtainSource.includes('正在切换影院场景'))
  assert.ok(curtainSource.includes('curtain-panel'))
})
