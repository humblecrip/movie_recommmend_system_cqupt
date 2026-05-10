const assert = require('assert')
const path = require('path')

const {
  routeLoadingState,
  routeLoadingTiming,
  beginRouteLoading,
  finishRouteLoading,
  failRouteLoading,
  __unsafeResetRouteLoadingForTests,
} = require(path.join(__dirname, '../src/utils/route-loading.js'))

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

async function runTest(name, testFn) {
  try {
    await testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  } finally {
    __unsafeResetRouteLoadingForTests()
  }
}

async function run() {
  await runTest('极短跳转不应把幕布显示出来', async () => {
    const navigationId = beginRouteLoading({ fullPath: '/index/home' })
    finishRouteLoading(navigationId)
    await sleep(routeLoadingTiming.SHOW_DELAY_MS + 30)
    assert.strictEqual(routeLoadingState.visible, false)
    assert.strictEqual(routeLoadingState.active, false)
  })

  await runTest('正常跳转应显示幕布并满足最小时长后再关闭', async () => {
    const navigationId = beginRouteLoading({ fullPath: '/index/dianyingxinxi' })
    await sleep(routeLoadingTiming.SHOW_DELAY_MS + 30)
    assert.strictEqual(routeLoadingState.visible, true)
    assert.strictEqual(routeLoadingState.phase, 'visible')

    finishRouteLoading(navigationId)
    assert.strictEqual(routeLoadingState.visible, true)

    await sleep(routeLoadingTiming.MIN_VISIBLE_MS + 40)
    assert.strictEqual(routeLoadingState.visible, false)
    assert.strictEqual(routeLoadingState.phase, 'idle')
  })

  await runTest('异常跳转也应在最小时长后正确收口', async () => {
    const navigationId = beginRouteLoading({ fullPath: '/index/ai-recommend' })
    await sleep(routeLoadingTiming.SHOW_DELAY_MS + 30)
    assert.strictEqual(routeLoadingState.visible, true)
    assert.strictEqual(routeLoadingState.phase, 'visible')

    failRouteLoading(navigationId)
    assert.strictEqual(routeLoadingState.visible, true)
    assert.strictEqual(routeLoadingState.phase, 'error')

    await sleep(routeLoadingTiming.MIN_VISIBLE_MS + 40)
    assert.strictEqual(routeLoadingState.visible, false)
    assert.strictEqual(routeLoadingState.active, false)
    assert.strictEqual(routeLoadingState.phase, 'idle')
  })
}

run().catch(error => {
  console.error(error)
  process.exit(1)
})
