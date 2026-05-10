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

const centerSource = read(path.join(__dirname, '../src/pages/center/center.vue'))
const storeupSource = read(path.join(__dirname, '../src/pages/storeup/list.vue'))

runTest('个人中心品牌区可点击跳首页', () => {
  assert.ok(centerSource.includes('<button class="brand-block" type="button" @click="goHome">'))
  assert.ok(centerSource.includes("window.location.hash = '#/index/home'"))
})

runTest('收藏页品牌区可点击跳首页', () => {
  assert.ok(storeupSource.includes('<cinema-shell'))
  assert.ok(storeupSource.includes('@open-home="openHomeView"'))
  assert.ok(storeupSource.includes("path: '/index/home'"))
})
