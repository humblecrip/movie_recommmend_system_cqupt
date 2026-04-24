const assert = require('assert')
const fs = require('fs')
const path = require('path')

const packagePath = path.join(__dirname, '../package.json')
const mainPath = path.join(__dirname, '../src/main.js')
const detailPath = path.join(__dirname, '../src/pages/dianyingxinxi/detail.vue')
const loginPath = path.join(__dirname, '../src/pages/login/login.vue')
const storePath = path.join(__dirname, '../src/stores/comment-draft.js')

const packageSource = fs.readFileSync(packagePath, 'utf8')
const mainSource = fs.readFileSync(mainPath, 'utf8')
const detailSource = fs.readFileSync(detailPath, 'utf8')
const loginSource = fs.readFileSync(loginPath, 'utf8')

function expectStructure(condition, message, filePath) {
  assert.ok(condition, `${message} [检查文件: ${filePath}]`)
}

function run() {
  expectStructure(packageSource.includes('"pinia"'), '前端依赖应接入 pinia', packagePath)
  expectStructure(fs.existsSync(storePath), '应新增评论草稿 pinia store', storePath)

  if (fs.existsSync(storePath)) {
    const storeSource = fs.readFileSync(storePath, 'utf8')
    expectStructure(storeSource.includes('defineStore'), '评论草稿 store 应使用 defineStore 定义', storePath)
    expectStructure(storeSource.includes('saveDraft'), '评论草稿 store 应提供保存草稿方法', storePath)
    expectStructure(storeSource.includes('clearDraft'), '评论草稿 store 应提供清理草稿方法', storePath)
  }

  expectStructure(mainSource.includes('createPinia'), '应用入口应创建 pinia 实例', mainPath)
  expectStructure(mainSource.includes('pinia'), '应用入口应挂载 pinia', mainPath)

  expectStructure(detailSource.includes('useCommentDraftStore'), '电影详情页应接入评论草稿 pinia store', detailPath)
  expectStructure(detailSource.includes('restoreCommentDraft'), '电影详情页应在返回后恢复评论草稿', detailPath)
  expectStructure(detailSource.includes('persistCommentDraft'), '电影详情页应在登录跳转前暂存评论草稿', detailPath)
  expectStructure(detailSource.includes('clearCommentDraft'), '电影详情页应在提交成功或重置后清空评论草稿', detailPath)

  expectStructure(loginSource.includes("this.$route.query.redirect || '/'"), '登录页应继续使用 redirect 回跳', loginPath)

  console.log('comment-draft-pinia-structure tests passed')
}

run()
