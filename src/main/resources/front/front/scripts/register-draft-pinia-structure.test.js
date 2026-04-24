const assert = require('assert')
const fs = require('fs')
const path = require('path')

const registerPath = path.join(__dirname, '../src/pages/register/register.vue')
const storePath = path.join(__dirname, '../src/stores/register-draft.js')

const registerSource = fs.readFileSync(registerPath, 'utf8')

function expectStructure(condition, message, filePath) {
  assert.ok(condition, `${message} [检查文件: ${filePath}]`)
}

function run() {
  expectStructure(fs.existsSync(storePath), '应新增注册草稿 pinia store', storePath)

  if (fs.existsSync(storePath)) {
    const storeSource = fs.readFileSync(storePath, 'utf8')
    expectStructure(storeSource.includes('defineStore'), '注册草稿 store 应使用 defineStore 定义', storePath)
    expectStructure(storeSource.includes('saveDraft'), '注册草稿 store 应提供保存草稿方法', storePath)
    expectStructure(storeSource.includes('clearDraft'), '注册草稿 store 应提供清理草稿方法', storePath)
  }

  expectStructure(registerSource.includes('useRegisterDraftStore'), '注册页应接入注册草稿 pinia store', registerPath)
  expectStructure(registerSource.includes('restoreRegisterDraft'), '注册页应在进入时恢复草稿', registerPath)
  expectStructure(registerSource.includes('persistRegisterDraft'), '注册页应在输入变化时暂存草稿', registerPath)
  expectStructure(registerSource.includes('clearRegisterDraft'), '注册页应在注册成功后清空草稿', registerPath)
  expectStructure(registerSource.includes("'registerForm.touxiang'"), '注册页应对头像草稿变化做同步暂存', registerPath)

  console.log('register-draft-pinia-structure tests passed')
}

run()
