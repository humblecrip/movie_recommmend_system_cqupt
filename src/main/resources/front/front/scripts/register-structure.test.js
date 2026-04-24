const assert = require('assert')
const fs = require('fs')
const path = require('path')

const registerPath = path.join(__dirname, '../src/pages/register/register.vue')
const source = fs.readFileSync(registerPath, 'utf8')

function expectStructure(condition, message) {
  assert.ok(condition, `${message} [检查文件: ${registerPath}]`)
}

function run() {
  expectStructure(source.includes('class="cinema-register"'), '应存在新的影院注册页根容器')
  expectStructure(source.includes('class="register-atmosphere"'), '应存在背景氛围层')
  expectStructure(!source.includes('class="register-nav"'), '不应保留顶部导航容器')
  expectStructure(source.includes('class="register-card glass-card"'), '应存在玻璃质感注册卡片')
  expectStructure(source.includes("Join the Curator's Circle"), '应存在原型主标题')
  expectStructure(source.includes('v-model="registerForm.yonghuzhanghao"'), '应保留用户账号字段绑定')
  expectStructure(source.includes('v-model="registerForm.mima"'), '应保留密码字段绑定')
  expectStructure(source.includes('v-model="registerForm.mima2"'), '应保留确认密码字段绑定')
  expectStructure(source.includes('v-model="registerForm.yonghuxingming"'), '应保留用户姓名字段绑定')
  expectStructure(source.includes('v-model="registerForm.xingbie"'), '应保留性别字段绑定')
  expectStructure(source.includes('v-model="registerForm.lianxidianhua"'), '应保留联系电话字段绑定')
  expectStructure(source.includes('v-model="registerForm.shenfenzheng"'), '应保留身份证字段绑定')
  expectStructure(source.includes('<file-upload'), '应保留头像上传组件入口')
  expectStructure(source.includes(':replaceable="true"'), '注册页头像上传应启用可替换上传模式')
  expectStructure(source.includes('class="avatar-upload-layer"'), '注册页头像上传层应显式铺满头像区域以支持再次点击替换')
  expectStructure(source.includes('class="avatar-preview"'), '上传头像后应存在预览占位图')
  expectStructure(source.includes('avatarPreviewUrl'), '注册页应根据上传结果生成头像预览地址')
  expectStructure(source.includes('.avatar-badge') && source.includes('z-index: 4;'), '相机徽标应始终处于头像预览与上传层之上')
  expectStructure(source.includes("submitForm('registerForm')"), '应保留注册提交逻辑入口')
  expectStructure(source.includes('已有账号，直接登录'), '应保留登录入口文案')
  expectStructure(source.includes('class="register-footer"'), '应存在页脚区域')
  expectStructure(source.includes('class="footer-brand footer-home-link"'), '注册页品牌标签应升级为可点击首页入口')
  expectStructure(source.includes('to="/index/home"'), '注册页品牌标签点击后应跳转至首页')

  console.log('register-structure tests passed')
}

run()
