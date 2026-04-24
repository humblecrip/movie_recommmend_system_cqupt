const assert = require('assert')

const {
  requiresFrontLogin,
} = require('../src/utils/front-access')

function runTest(name, testFn) {
  try {
    testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  }
}

runTest('个人中心路由必须登录', () => {
  assert.strictEqual(requiresFrontLogin({ path: '/index/center', query: {} }), true)
})

runTest('收藏路由必须登录', () => {
  assert.strictEqual(requiresFrontLogin({ path: '/index/storeup', query: {} }), true)
})

runTest('带个人中心上下文的列表必须登录', () => {
  assert.strictEqual(requiresFrontLogin({ path: '/index/dianyingxinxi', query: { centerType: '1' } }), true)
})

runTest('带收藏上下文的详情必须登录', () => {
  assert.strictEqual(requiresFrontLogin({ path: '/index/dianyingxinxiDetail', query: { storeupType: 1 } }), true)
})

runTest('用户信息详情页必须登录', () => {
  assert.strictEqual(requiresFrontLogin({ path: '/index/yonghuDetail', query: { id: 3 } }), true)
})

runTest('普通首页路由不要求登录', () => {
  assert.strictEqual(requiresFrontLogin({ path: '/index/home', query: {} }), false)
})

runTest('普通电影列表路由不要求登录', () => {
  assert.strictEqual(requiresFrontLogin({ path: '/index/dianyingxinxi', query: {} }), false)
})
