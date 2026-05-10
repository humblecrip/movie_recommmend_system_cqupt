const assert = require('assert')
const fs = require('fs')
const path = require('path')

function read(relativePath) {
  return fs.readFileSync(path.join(__dirname, '..', relativePath), 'utf8')
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

const detailSource = read('src/pages/dianyingxinxi/detail.vue')
const storeupPanelSource = read('src/pages/storeup/storeup-panel.vue')
const legacyDiscussDetailSource = read('src/pages/discussdianyingxinxi/detail.vue')

runTest('电影详情页电影域写路径切到 appmovie 新接口', () => {
  assert.ok(detailSource.includes('appmovie/actions/status'))
  assert.ok(detailSource.includes('appmovie/actions/toggle'))
  assert.ok(detailSource.includes('appmovie/comments/page'))
  assert.ok(detailSource.includes('appmovie/comments/add'))
  assert.ok(detailSource.includes('appmovie/comments/delete'))
  assert.ok(detailSource.includes('appmovie/comments/vote'))
  assert.ok(!detailSource.includes("'storeup/"))
  assert.ok(!detailSource.includes("'discussdianyingxinxi/"))
})

runTest('我的收藏面板使用 appmovie 收藏接口', () => {
  assert.ok(storeupPanelSource.includes('appmovie/favorites/page'))
  assert.ok(storeupPanelSource.includes('appmovie/favorites/cancel'))
  assert.ok(storeupPanelSource.includes('getStoreupTitle(item)'))
  assert.ok(!storeupPanelSource.includes('{{ item.name }}'))
  assert.ok(!storeupPanelSource.includes("'storeup/"))
  assert.ok(storeupPanelSource.includes('/index/dianyingxinxiDetail'))
})

runTest('旧评论详情页继续走 discuss 兼容接口且不再使用错误别名', () => {
  assert.ok(legacyDiscussDetailSource.includes("'discussdianyingxinxi/list'"))
  assert.ok(legacyDiscussDetailSource.includes("'discussdianyingxinxi/update'"))
  assert.ok(legacyDiscussDetailSource.includes("'discussdianyingxinxi/delete'"))
  assert.ok(legacyDiscussDetailSource.includes("'discussdianyingxinxi/add'"))
  assert.ok(legacyDiscussDetailSource.includes('refid: this.detail.refid'))
  assert.ok(!legacyDiscussDetailSource.includes('discussdiscussdianyingxinxi/'))
})
