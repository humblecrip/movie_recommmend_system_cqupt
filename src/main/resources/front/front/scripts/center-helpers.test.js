const assert = require('assert')

const {
  resolveCenterAvatar,
  buildCenterNavItems,
} = require('../src/pages/center/center-helpers')

function runTest(name, testFn) {
  try {
    testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  }
}

runTest('用户头像为空时返回兜底图', () => {
  const result = resolveCenterAvatar({}, 'http://localhost:8080/', '/assets/fallback.png')
  assert.strictEqual(result, '/assets/fallback.png')
})

runTest('相对路径头像会拼接 baseUrl', () => {
  const result = resolveCenterAvatar({ touxiang: 'upload/avatar.png' }, 'http://localhost:8080/', '/assets/fallback.png')
  assert.strictEqual(result, 'http://localhost:8080/upload/avatar.png')
})

runTest('http 头像保持原样', () => {
  const result = resolveCenterAvatar({ touxiang: 'https://cdn.test/avatar.png' }, 'http://localhost:8080/', '/assets/fallback.png')
  assert.strictEqual(result, 'https://cdn.test/avatar.png')
})

runTest('侧栏导航会过滤我的收藏管理并追加固定入口', () => {
  const result = buildCenterNavItems([
    {
      menu: '影片管理',
      child: [{ menu: '电影信息', tableName: 'dianyingxinxi' }],
    },
    {
      menu: '我的收藏管理',
      child: [{ menu: '我的收藏管理', tableName: 'storeup' }],
    },
  ])

  assert.deepStrictEqual(result.map(item => item.key), [
    'profile',
    'password',
    'backend-dianyingxinxi',
    'storeup',
    'logout',
  ])

  const storeupItem = result.find(item => item.key === 'storeup')
  assert.deepStrictEqual(storeupItem, {
    key: 'storeup',
    label: '我的收藏',
    type: 'panel',
    icon: 'favorite',
  })
})
