const assert = require('assert')

const {
  resolveStoreupPicture,
  getStoreupSortOptions,
  getStoreupEmptyText,
} = require('../src/pages/storeup/storeup-helpers')

function runTest(name, testFn) {
  try {
    testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  }
}

runTest('http 海报地址保持原样', () => {
  const result = resolveStoreupPicture({ picture: 'https://cdn.example.com/poster.png' }, 'http://localhost:8080/', '/fallback.png')
  assert.strictEqual(result, 'https://cdn.example.com/poster.png')
})

runTest('相对路径海报地址会拼接 baseUrl', () => {
  const result = resolveStoreupPicture({ picture: 'upload/poster.png' }, 'http://localhost:8080/', '/fallback.png')
  assert.strictEqual(result, 'http://localhost:8080/upload/poster.png')
})

runTest('缺失海报时回退默认图', () => {
  const result = resolveStoreupPicture({}, 'http://localhost:8080/', '/fallback.png')
  assert.strictEqual(result, '/fallback.png')
})

runTest('排序配置包含最新收藏和名称排序', () => {
  const result = getStoreupSortOptions()
  assert.deepStrictEqual(result.map(item => item.key), ['latest', 'oldest', 'name'])
})

runTest('空态文案会根据搜索词变化', () => {
  assert.strictEqual(getStoreupEmptyText(''), '暂无收藏内容')
  assert.strictEqual(getStoreupEmptyText('星际'), '没有找到与“星际”相关的收藏')
})
