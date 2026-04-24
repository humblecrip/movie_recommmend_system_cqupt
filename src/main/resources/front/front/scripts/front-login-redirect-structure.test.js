const assert = require('assert')
const fs = require('fs')
const path = require('path')

const detailPath = path.join(__dirname, '../src/pages/dianyingxinxi/detail.vue')
const loginPath = path.join(__dirname, '../src/pages/login/login.vue')
const mainPath = path.join(__dirname, '../src/main.js')

const detailSource = fs.readFileSync(detailPath, 'utf8')
const loginSource = fs.readFileSync(loginPath, 'utf8')
const mainSource = fs.readFileSync(mainPath, 'utf8')

function expectStructure(condition, message, filePath) {
  assert.ok(condition, `${message} [检查文件: ${filePath}]`)
}

function run() {
  expectStructure(detailSource.includes("tab === 'second' ? 'second' : 'first'"), '电影详情页应能根据路由参数恢复评论 Tab', detailPath)
  expectStructure(detailSource.includes("this.redirectToFrontLogin('second', '请先登录后再发表评论')"), '未登录评论时应携带当前电影详情评论页作为登录回跳地址', detailPath)
  expectStructure(detailSource.includes('if (!localStorage.getItem(\'frontToken\'))'), '评论提交前应先检查前台登录态', detailPath)

  expectStructure(loginSource.includes("this.$route.query.redirect || '/'"), '登录页应继续支持按 redirect 参数回跳', loginPath)
  expectStructure(mainSource.includes('redirect: router.currentRoute.fullPath'), '全局 401/403 登录跳转应保留当前页面回跳地址', mainPath)

  console.log('front-login-redirect-structure tests passed')
}

run()
