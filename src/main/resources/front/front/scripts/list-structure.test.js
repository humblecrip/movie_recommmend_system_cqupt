const assert = require('assert')
const fs = require('fs')
const path = require('path')

const listPath = path.join(__dirname, '../src/pages/dianyingxinxi/list.vue')
const source = fs.readFileSync(listPath, 'utf8')

function escapeRegExp(text) {
  return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function has(text) {
  return source.includes(text)
}

function hasClass(className) {
  const pattern = new RegExp(`class=["'][^"']*\\b${escapeRegExp(className)}\\b[^"']*["']`)
  return pattern.test(source)
}

function hasTagWithClass(tagName, className) {
  const pattern = new RegExp(
    `<${escapeRegExp(tagName)}\\b[^>]*class=["'][^"']*\\b${escapeRegExp(className)}\\b[^"']*["']`,
    'i'
  )
  return pattern.test(source)
}

function getTagFragmentByClass(tagName, className) {
  const pattern = new RegExp(
    `(<${escapeRegExp(tagName)}\\b[^>]*class=["'][^"']*\\b${escapeRegExp(className)}\\b[^"']*["'][^>]*>[\\s\\S]*?<\\/${escapeRegExp(tagName)}>)`,
    'i'
  )
  const match = source.match(pattern)
  return match ? match[1] : ''
}

function fragmentHasClass(fragment, className) {
  const pattern = new RegExp(`class=["'][^"']*\\b${escapeRegExp(className)}\\b[^"']*["']`)
  return pattern.test(fragment)
}

function expectStructure(condition, message) {
  assert.ok(condition, `${message} [检查文件: ${listPath}]`)
}

function run() {
  const panelPath = path.join(__dirname, '../src/pages/dianyingxinxi/movie-discover-panel.vue')
  const panelSource = fs.readFileSync(panelPath, 'utf8')
  const originalSource = source

  function panelHas(text) {
    return panelSource.includes(text)
  }

  function panelHasClass(className) {
    const pattern = new RegExp(`class=["'][^"']*\\b${escapeRegExp(className)}\\b[^"']*["']`)
    return pattern.test(panelSource)
  }

  function panelHasTagWithClass(tagName, className) {
    const pattern = new RegExp(
      `<${escapeRegExp(tagName)}\\b[^>]*class=["'][^"']*\\b${escapeRegExp(className)}\\b[^"']*["']`,
      'i'
    )
    return pattern.test(panelSource)
  }

  function panelGetTagFragmentByClass(tagName, className) {
    const pattern = new RegExp(
      `(<${escapeRegExp(tagName)}\\b[^>]*class=["'][^"']*\\b${escapeRegExp(className)}\\b[^"']*["'][^>]*>[\\s\\S]*?<\\/${escapeRegExp(tagName)}>)`,
      'i'
    )
    const match = panelSource.match(pattern)
    return match ? match[1] : ''
  }

  function panelFragmentHasClass(fragment, className) {
    const pattern = new RegExp(`class=["'][^"']*\\b${escapeRegExp(className)}\\b[^"']*["']`)
    return pattern.test(fragment)
  }

  const filterBarFragment = panelGetTagFragmentByClass('section', 'filter-bar')

  expectStructure(panelHas('Explore Our Collection'), '电影发现面板应保留原型标题区')
  expectStructure(!panelHas('<cinema-shell'), '电影发现面板不应再自行包裹前台壳子')
  expectStructure(Boolean(filterBarFragment), '应保留统一筛选区容器')
  expectStructure(filterBarFragment.includes('Genre'), '筛选区内应保留 Genre 筛选项')
  expectStructure(panelFragmentHasClass(filterBarFragment, 'filter-chip-genre'), '筛选区内应保留 Genre 筛选容器类名')
  expectStructure(filterBarFragment.includes('Release'), '筛选区内应保留 Release 筛选项')
  expectStructure(panelFragmentHasClass(filterBarFragment, 'filter-chip-release'), '筛选区内应保留 Release 筛选容器类名')
  expectStructure(filterBarFragment.includes('Rating'), '筛选区内应保留 Rating 筛选项')
  expectStructure(panelFragmentHasClass(filterBarFragment, 'filter-chip-rating'), '筛选区内应保留 Rating 筛选容器类名')
  expectStructure(filterBarFragment.includes('Sort By'), '筛选区内应保留 Sort By 筛选项')
  expectStructure(panelFragmentHasClass(filterBarFragment, 'filter-chip-sort'), '筛选区内应保留排序筛选容器类名')
  expectStructure(panelHas('Quick View'), '应保留 Quick View 按钮文案')
  expectStructure(panelHasTagWithClass('section', 'poster-grid'), '应保留纯海报网格容器')
  expectStructure(panelHasTagWithClass('nav', 'movie-pagination'), '应保留原型风格分页容器')

  expectStructure(!panelHasTagWithClass('header', 'discover-topbar'), '不应保留列表页自带静态顶部导航')
  expectStructure(!panelHasClass('hero-section'), '不应保留详情页式 Hero 主容器')
  expectStructure(!panelHasClass('hero-content'), '不应保留详情页式 Hero 内容容器')
  expectStructure(!panelHasClass('overview-section'), '不应保留详情页式 Overview 区容器')
  expectStructure(!panelHasClass('similar-section'), '不应保留详情页式 Similar Movies 区容器')
  expectStructure(!panelHasClass('engagement-section'), '不应保留当前业务区主容器')
  expectStructure(!panelHasClass('business-layout'), '不应保留当前双栏业务布局')
  expectStructure(!originalSource.includes('<cinema-shell'), '旧电影列表入口页不应继续承担页面渲染')
  expectStructure(originalSource.includes("$router.replace"), '旧电影列表入口页应改为兼容跳转')

  console.log('list-structure tests passed')
}

run()
