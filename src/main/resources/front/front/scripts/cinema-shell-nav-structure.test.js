const assert = require('assert')
const fs = require('fs')
const path = require('path')

const shellPath = path.join(__dirname, '../src/components/CinemaShell.vue')
const source = fs.readFileSync(shellPath, 'utf8')

function expectStructure(condition, message) {
  assert.ok(condition, `${message} [检查文件: ${shellPath}]`)
}

function run() {
  expectStructure(!source.includes('>Categories<'), '首页 header 不应再展示 Categories 导航项')
  expectStructure(source.includes('>Home<'), '首页 header 应保留 Home 导航项')
  expectStructure(source.includes('>Movies<'), '首页 header 应保留 Movies 导航项')
  expectStructure(source.includes('>My List<'), '首页 header 应保留 My List 导航项')

  console.log('cinema-shell-nav-structure tests passed')
}

run()
