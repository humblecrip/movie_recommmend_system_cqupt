<template>
  <div class="cinema-login">
    <div class="login-atmosphere">
      <div class="atmosphere-mask"></div>
      <img class="atmosphere-image" :src="backgroundImage" alt="影院背景">
    </div>

    <header class="login-nav">
      <router-link class="brand-text brand-link" to="/index/home">电影推荐系统</router-link>
      <button class="help-button" type="button" aria-label="帮助">
        <i class="el-icon-question"></i>
      </button>
    </header>

    <main class="login-main">
      <section class="login-card glass-card">
        <div class="login-header">
          <h1>银幕已就绪</h1>
          <p>登录后查看你的专属观影收藏。</p>
        </div>

        <el-form ref="loginForm" :model="loginForm" :rules="rules" class="login-form" @submit.native.prevent>
          <el-form-item v-if="loginType == 1" prop="username" class="field-row">
            <label class="field-label">账号</label>
            <input
              v-model="loginForm.username"
              class="field-input"
              type="text"
              placeholder="请输入账号"
              autocomplete="username"
            >
          </el-form-item>

          <el-form-item v-if="loginType == 1" prop="password" class="field-row">
            <label class="field-label">密码</label>
            <div class="password-field">
              <input
                v-model="loginForm.password"
                class="field-input"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                autocomplete="current-password"
              >
              <button
                class="password-toggle"
                type="button"
                :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                @click="showPassword = !showPassword"
              >
                <i :class="showPassword ? 'el-icon-view' : 'el-icon-view'"></i>
              </button>
            </div>
          </el-form-item>

          <el-form-item v-if="roles.length > 1" class="field-row field-row-select">
            <label class="field-label">角色</label>
            <el-select v-model="loginForm.tableName" placeholder="请选择角色" @change="selectChange">
              <el-option
                v-for="item,index in roles"
                :key="index"
                :label="item.roleName"
                :value="item.tableName"
              />
            </el-select>
          </el-form-item>

          <div class="login-actions">
            <button class="login-submit" type="button" @click="submitForm('loginForm')">立即登录</button>
          </div>
        </el-form>

        <div class="register-section">
          <p class="register-copy">
            还没有账号？
            <span> 立即注册 </span>
          </p>
          <div class="register-links">
            <router-link
              v-for="(item, index) in filteredRegisterRoles"
              :key="index"
              class="register-link"
              :to="{path: '/register', query: {role: item.tableName, pageFlag: 'register'}}"
            >
              注册{{ item.roleName.replace('注册','') }}
            </router-link>
          </div>
        </div>
      </section>
    </main>

    <footer class="login-footer">
      <div class="footer-copy">© 2024 电影推荐系统 · 保留所有权利</div>
      <div class="footer-links">
        <a href="javascript:void(0);">服务条款</a>
        <a href="javascript:void(0);">隐私政策</a>
        <a href="javascript:void(0);">Cookie 设置</a>
        <a href="javascript:void(0);">联系支持</a>
      </div>
    </footer>
  </div>
</template>

<script>
import menu from '@/config/menu'

const { dispatchFrontAvatarChanged } = require('../../utils/front-logout')

export default {
  data() {
    return {
      loginType: 1,
      roleMenus: [],
      loginForm: {
        username: '',
        password: '',
        tableName: '',
      },
      role: '',
      roles: [],
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ],
      },
      showPassword: false,
      backgroundImage: require('@/assets/login-bg.jpg'),
    }
  },
  created() {
    this.roleMenus = menu.list()
    for (let item in this.roleMenus) {
      if (this.roleMenus[item].hasFrontLogin == '是') {
        this.roles.push(this.roleMenus[item])
      }
    }
  },
  computed: {
    filteredRegisterRoles() {
      return this.roles.filter(item => item.hasFrontRegister == '是')
    },
  },
  methods: {
    encryptPasswordValue(value) {
      return value ? this.encryptAes(value) : value
    },
    notifyAvatarChanged(sessionForm, cachedAvatar) {
      dispatchFrontAvatarChanged(sessionForm, cachedAvatar)
    },
    showLoginSuccessMessage() {
      this.$message({
        message: '登录成功',
        customClass: 'front-theme-message front-theme-message-login',
        iconClass: 'el-icon-star-on',
        duration: 1500,
        offset: 28,
      })
    },
    resetFrontUserCache() {
      localStorage.removeItem('sessionForm')
      localStorage.removeItem('frontHeadportrait')
      localStorage.removeItem('frontUserid')
      localStorage.removeItem('vip')
      this.notifyAvatarChanged({}, '')
    },
    selectChange(value) {
      for (let x in this.roles) {
        if (this.roles[x].tableName == value) {
          this.role = this.roles[x].roleName
        }
      }
    },
    submitForm(formName) {
      if (this.roles.length != 1) {
        if (!this.role) {
          this.$message.error('请选择登录用户类型')
          return false
        }
      } else {
        this.role = this.roles[0].roleName
        this.loginForm.tableName = this.roles[0].tableName
      }
      if (!this.loginForm.username) {
        this.$message.error('请输入用户名')
        return
      }
      if (!this.loginForm.password) {
        this.$message.error('请输入密码')
        return
      }

      this.loginPost(formName)
    },
    loginPost(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          const payload = {
            ...this.loginForm,
            password: this.encryptPasswordValue(this.loginForm.password),
          }
          this.$http.get(`${this.loginForm.tableName}/login`, { params: payload }).then(res => {
            if (res.data.code === 0) {
              this.resetFrontUserCache()
              localStorage.setItem('frontToken', res.data.token)
              localStorage.setItem('UserTableName', this.loginForm.tableName)
              localStorage.setItem('username', this.loginForm.username)
              localStorage.setItem('frontSessionTable', this.loginForm.tableName)
              localStorage.setItem('frontRole', this.role)
              localStorage.setItem('keyPath', 0)
              this.$router.push(this.$route.query.redirect || '/')
              this.showLoginSuccessMessage()
            } else {
              this.$message.error(res.data.msg)
            }
          })
        }
      })
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.cinema-login {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: #0b1326;
  color: #dae2fd;
  font-family: 'Manrope', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.login-atmosphere {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

.atmosphere-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: blur(10px) saturate(0.78) brightness(0.52);
  transform: scale(1.06);
}

.atmosphere-mask {
  position: absolute;
  inset: 0;
  z-index: 1;
  background:
    radial-gradient(circle at top, rgba(255, 198, 57, 0.12), transparent 24%),
    linear-gradient(180deg, rgba(11, 19, 38, 0.38), rgba(11, 19, 38, 0.78) 38%, rgba(11, 19, 38, 0.96));
}

.login-nav,
.login-main,
.login-footer {
  position: relative;
  z-index: 2;
}

.login-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26px 32px;
}

.brand-text {
  color: #ffc639;
  font-family: 'Epilogue', 'PingFang SC', sans-serif;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.04em;
  text-transform: uppercase;
}

.brand-link {
  text-decoration: none;
  transition: color .25s ease, opacity .25s ease;
}

.brand-link:hover {
  color: #ffd566;
}

.help-button {
  width: 42px;
  height: 42px;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: rgba(218, 226, 253, 0.82);
  cursor: pointer;
  transition: color .25s ease, transform .25s ease;
}

.help-button:hover {
  color: #ffc639;
  transform: translateY(-1px);
}

.login-main {
  min-height: calc(100vh - 152px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 24px 88px;
}

.login-card {
  width: 100%;
  max-width: 480px;
  padding: 42px 38px 34px;
  border-radius: 30px;
  border: 1px solid rgba(153, 144, 124, 0.15);
  background: rgba(45, 52, 73, 0.38);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.36);
}

.glass-card {
  backdrop-filter: blur(34px);
  -webkit-backdrop-filter: blur(34px);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  margin: 0 0 10px;
  color: #dae2fd;
  font-family: 'Epilogue', 'PingFang SC', sans-serif;
  font-size: 40px;
  font-weight: 800;
  letter-spacing: -0.04em;
}

.login-header p {
  margin: 0;
  color: rgba(208, 197, 175, 0.82);
  font-size: 15px;
  line-height: 1.7;
}

.login-form {
  display: grid;
  gap: 18px;
}

.field-row {
  margin-bottom: 0;
}

.field-label {
  display: block;
  margin: 0 0 8px 2px;
  color: #d0c5af;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: .18em;
  text-transform: uppercase;
}

.field-input,
.field-row-select ::v-deep .el-input__inner {
  width: 100%;
  height: 54px;
  border: 1px solid rgba(77, 70, 53, 0.3);
  border-radius: 16px;
  background: rgba(34, 42, 61, 0.76);
  color: #dae2fd;
  font-size: 15px;
  transition: border-color .24s ease, box-shadow .24s ease, background-color .24s ease;
}

.field-input {
  padding: 0 18px;
  outline: none;
}

.field-input::placeholder,
.field-row-select ::v-deep .el-input__inner::placeholder {
  color: rgba(208, 197, 175, 0.42);
}

.field-input:focus,
.field-row-select ::v-deep .el-input.is-focus .el-input__inner,
.field-row-select ::v-deep .el-input__inner:focus {
  border-color: rgba(255, 198, 57, 0.82);
  box-shadow: 0 0 0 3px rgba(255, 198, 57, 0.1);
  background: rgba(34, 42, 61, 0.9);
}

.password-field {
  position: relative;
}

.password-field .field-input {
  padding-right: 52px;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 14px;
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  color: rgba(208, 197, 175, 0.78);
  cursor: pointer;
  transition: color .2s ease;
}

.password-toggle:hover {
  color: #ffc639;
}

.field-row-select ::v-deep .el-select {
  display: block;
}

.field-row-select ::v-deep .el-input__icon {
  color: rgba(208, 197, 175, 0.7);
  line-height: 54px;
}

.login-actions {
  margin-top: 4px;
}

.login-submit {
  width: 100%;
  height: 58px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ffc639, #e1aa12);
  color: #3f2e00;
  font-size: 17px;
  font-weight: 900;
  letter-spacing: .08em;
  cursor: pointer;
  box-shadow: 0 18px 36px rgba(225, 170, 18, 0.18);
  transition: transform .2s ease, box-shadow .2s ease, filter .2s ease;
}

.login-submit:hover {
  transform: translateY(-1px) scale(1.01);
  box-shadow: 0 22px 40px rgba(225, 170, 18, 0.26);
}

.register-section {
  margin-top: 28px;
  text-align: center;
}

.register-copy {
  margin: 0;
  color: rgba(208, 197, 175, 0.78);
  font-size: 14px;
}

.register-copy span {
  color: #ffc639;
  font-weight: 800;
}

.register-links {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 10px 14px;
}

.register-link {
  color: #dae2fd;
  font-size: 13px;
  font-weight: 700;
  text-decoration: none;
  transition: color .2s ease, opacity .2s ease;
}

.register-link:hover {
  color: #ffc639;
}

.login-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 32px;
}

.footer-copy,
.footer-links a {
  color: rgba(218, 226, 253, 0.62);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .16em;
  text-transform: uppercase;
  text-decoration: none;
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 14px 24px;
}

.footer-links a:hover {
  color: #ffc639;
}

.field-row ::v-deep .el-form-item__content,
.field-row ::v-deep .el-form-item__error {
  line-height: normal;
}

.field-row ::v-deep .el-form-item__error {
  padding-top: 6px;
  color: #ffb4ab;
}

@media (max-width: 768px) {
  .login-nav {
    padding: 22px 18px;
  }

  .brand-text {
    font-size: 22px;
  }

  .login-main {
    min-height: calc(100vh - 130px);
    padding: 18px 16px 96px;
  }

  .login-card {
    padding: 34px 22px 28px;
    border-radius: 24px;
  }

  .login-header h1 {
    font-size: 32px;
  }

  .login-footer {
    position: static;
    flex-direction: column;
    padding: 10px 18px 24px;
    text-align: center;
  }
}
</style>
