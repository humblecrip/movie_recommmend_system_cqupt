const assert = require('assert')
const fs = require('fs')
const path = require('path')

const panelPath = path.join(__dirname, '../src/pages/dianyingxinxi/movie-discover-panel.vue')
const source = fs.readFileSync(panelPath, 'utf8')

function expectStructure(condition, message) {
  assert.ok(condition, `${message} [检查文件: ${panelPath}]`)
}

function run() {
  const selectMatches = source.match(/<el-select/g) || []
  expectStructure(selectMatches.length === 4, '电影筛选栏应统一使用 4 个 el-select 组件')
  expectStructure(source.includes("popper-class=\"discover-select-popper\""), '电影筛选栏下拉面板应使用统一的弹层样式类')
  expectStructure(source.includes('discover-select'), '电影筛选栏应包含统一的下拉视觉样式标识')

  console.log('movie-discover-filter-structure tests passed')
}

run()
