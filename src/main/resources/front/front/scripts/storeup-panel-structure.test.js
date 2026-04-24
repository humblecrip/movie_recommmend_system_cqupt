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
const storeupPageSource = read(path.join(__dirname, '../src/pages/storeup/list.vue'))
const storeupPanelSource = read(path.join(__dirname, '../src/pages/storeup/storeup-panel.vue'))

runTest('个人中心嵌入收藏组件面板', () => {
  assert.ok(centerSource.includes("v-else-if=\"activeSection === 'storeup'\""))
  assert.ok(centerSource.includes('<storeup-panel embedded></storeup-panel>'))
  assert.ok(centerSource.includes('StoreupPanel'))
})

runTest('收藏独立页复用收藏组件', () => {
  assert.ok(storeupPageSource.includes('<storeup-panel :hide-header="true"></storeup-panel>'))
  assert.ok(storeupPageSource.includes('StoreupPanel'))
})

runTest('收藏组件保留核心交互区', () => {
  assert.ok(storeupPanelSource.includes('class="page-header"'))
  assert.ok(storeupPanelSource.includes('class="toolbar"'))
  assert.ok(storeupPanelSource.includes('class="card-grid"'))
  assert.ok(storeupPanelSource.includes('class="favorite-pagination"'))
})
