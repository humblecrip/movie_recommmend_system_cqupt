const assert = require('assert')
const fs = require('fs')
const path = require('path')

const loginPath = path.join(__dirname, '../src/pages/login/login.vue')
const source = fs.readFileSync(loginPath, 'utf8')

function expectStructure(condition, message) {
  assert.ok(condition, `${message} [检查文件: ${loginPath}]`)
}

function run() {
  expectStructure(source.includes('class="cinema-login"'), '应存在新的影院登录页根容器')
  expectStructure(source.includes('class="login-atmosphere"'), '应存在背景氛围层')
  expectStructure(source.includes('Aether Cinema'), '应存在品牌标题')
  expectStructure(source.includes('class="brand-text brand-link"'), '品牌标题应升级为可点击首页入口')
  expectStructure(source.includes('to="/index/home"'), '品牌标题点击后应跳转至首页')
  expectStructure(source.includes('The Screen Awaits'), '应存在原型主标题')
  expectStructure(source.includes('class="login-card glass-card"'), '应存在玻璃质感登录卡片')
  expectStructure(source.includes('v-model="loginForm.username"'), '应保留账号输入绑定')
  expectStructure(source.includes('v-model="loginForm.password"'), '应保留密码输入绑定')
  expectStructure(source.includes("submitForm('loginForm')"), '应保留原登录提交逻辑入口')
  expectStructure(source.includes("v-if=\"roles.length > 1\""), '多角色时应保留角色选择区域')
  expectStructure(source.includes('v-for="(item, index) in roles"'), '应保留基于角色列表的注册入口渲染')
  expectStructure(source.includes("showPassword ? 'text' : 'password'"), '应保留密码显隐逻辑')
  expectStructure(source.includes('class="login-footer"'), '应存在页脚区域')
  expectStructure(source.includes("customClass: 'front-theme-message front-theme-message-login'"), '登录成功提示应使用前台主题消息样式')
  expectStructure(!source.includes("type: 'success'"), '登录成功提示不应再使用默认 success 类型外观')

  console.log('login-structure tests passed')
}

run()
