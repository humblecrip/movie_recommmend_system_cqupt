const assert = require('assert')

const {
  requiresFrontLogin,
  shouldRedirectAuthFailure,
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

runTest('公共首页接口鉴权失败不应全局跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: 'appmovie/front/recommended' },
      { path: '/index/home', query: { view: 'movies' } }
    ),
    false
  )
})

runTest('公共电影发现接口鉴权失败不应全局跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: 'appmovie/front/list' },
      { path: '/index/dianyingxinxi', query: {} }
    ),
    false
  )
})

runTest('公共电影类型接口鉴权失败不应全局跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: 'appmovie/types' },
      { path: '/index/dianyingxinxi', query: {} }
    ),
    false
  )
})

runTest('公共电影详情接口鉴权失败不应全局跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: '/springbootdo4wek3z/appmovie/front/detail/8' },
      { path: '/index/dianyingxinxiDetail', query: { id: 8 } }
    ),
    false
  )
})

runTest('公共接口即使在登录页上下文鉴权失败也不应全局跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: 'appmovie/front/list' },
      { path: '/index/storeup', query: {} }
    ),
    false
  )
})

runTest('当前路由要求前台登录且非公共接口鉴权失败应跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: 'unknown/private-api' },
      { path: '/index/storeup', query: {} }
    ),
    true
  )
})

runTest('收藏动作接口鉴权失败应跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: 'appmovie/actions/toggle' },
      { path: '/index/dianyingxinxiDetail', query: { id: 1 } }
    ),
    true
  )
})

runTest('评论分页公共接口鉴权失败不应全局跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: 'appmovie/comments/page' },
      { path: '/index/dianyingxinxiDetail', query: { id: 1 } }
    ),
    false
  )
})

runTest('发表评论接口鉴权失败应跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: 'appmovie/comments/add' },
      { path: '/index/dianyingxinxiDetail', query: { id: 1 } }
    ),
    true
  )
})

runTest('评论删除接口鉴权失败应跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: 'appmovie/comments/delete' },
      { path: '/index/dianyingxinxiDetail', query: { id: 1 } }
    ),
    true
  )
})

runTest('用户会话接口鉴权失败应跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: '/springbootdo4wek3z/yonghu/session' },
      { path: '/index/home', query: {} }
    ),
    true
  )
})

runTest('用户改密接口鉴权失败应跳登录', () => {
  assert.strictEqual(
    shouldRedirectAuthFailure(
      { url: 'yonghu/changePassword' },
      { path: '/index/center', query: { section: 'password' } }
    ),
    true
  )
})
