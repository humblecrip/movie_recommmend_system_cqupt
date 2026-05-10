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
  expectStructure(source.includes('>首页<'), '首页 header 应保留首页导航项')
  expectStructure(source.includes('>电影<'), '首页 header 应保留电影导航项')
  expectStructure(source.includes('>我的收藏<'), '首页 header 应保留我的收藏导航项')
  expectStructure(source.includes('智能推荐'), '首页 header 应保留智能推荐导航项')
  expectStructure(source.includes("activeNav === 'my-list'"), 'My List 导航项应支持 activeNav === my-list 激活')
  expectStructure(!source.includes('.discover-link.active'), 'Discover 不应再保留单独金色激活样式')
  expectStructure(!source.includes('#ffc639'), 'header 激活态不应再使用金色常量')

  console.log('cinema-shell-nav-structure tests passed')
}

run()
