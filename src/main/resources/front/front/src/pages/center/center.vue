<template>
  <div class="center-shell">
    <aside class="center-sidebar fade-slide-up">
      <button class="brand-block" type="button" @click="goHome">
        <div class="brand-mark">
          <i class="el-icon-film"></i>
        </div>
        <div>
          <div class="brand-title">
            <span class="brand-main">电影推荐</span>
            <span class="brand-accent">系统</span>
          </div>
          <div class="brand-subtitle">个性化电影推荐</div>
        </div>
      </button>

      <nav class="side-nav">
        <button
          v-for="item in sidebarItems"
          :key="item.key"
          class="side-nav-item"
          :class="{ 'side-nav-item-active': isItemActive(item) }"
          type="button"
          @click="selectSidebarItem(item)"
        >
          <i :class="item.icon"></i>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="sidebar-footer">
        <div class="sidebar-meta">安全等级：金卡会员</div>
        <div class="sidebar-meta">最近更新：{{ lastUpdatedText }}</div>
      </div>
    </aside>

    <main class="center-main">
      <div class="hero-backdrop"></div>
      <div class="hero-glow hero-glow-left"></div>
      <div class="hero-glow hero-glow-right"></div>

      <div class="center-content fade-slide-up delay-1">
        <header class="page-header">
          <h2>{{ pageTitle }}</h2>
          <p>{{ pageSubtitle }}</p>
        </header>

        <section class="panel-card">
          <template v-if="activeSection === 'profile'">
            <div class="profile-identity">
              <div class="avatar-stack">
                <div class="avatar-ring">
                  <img :src="profileAvatar" alt="用户头像">
                </div>
                <div class="avatar-upload">
                  <file-upload
                    tip=""
                    action="file/upload"
                    :limit="1"
                    :multiple="true"
                    :fileUrls="sessionForm.touxiang ? sessionForm.touxiang : ''"
                    @change="yonghutouxiangHandleAvatarSuccess"
                  ></file-upload>
                </div>
              </div>

              <div class="identity-copy">
                <div class="identity-kicker">个人身份</div>
                <h3>{{ profileName }}</h3>
                <p>更新头像与个人展示信息。</p>
              </div>
            </div>

            <el-form
              ref="sessionForm"
              class="profile-form"
              :model="sessionForm"
              :rules="rules"
              label-position="top"
            >
              <div class="form-grid" v-if="userTableName === 'yonghu'">
                <el-form-item class="field-block field-readonly" label="用户账号" prop="yonghuzhanghao">
                  <el-input v-model="sessionForm.yonghuzhanghao" placeholder="用户账号" readonly></el-input>
                </el-form-item>

                <el-form-item class="field-block" label="用户姓名" prop="yonghuxingming">
                  <el-input v-model="sessionForm.yonghuxingming" placeholder="用户姓名"></el-input>
                </el-form-item>

                <el-form-item class="field-block" label="性别" prop="xingbie">
                  <el-select v-model="sessionForm.xingbie" placeholder="请选择性别">
                    <el-option
                      v-for="(item, index) in dynamicProp.xingbie"
                      :key="index"
                      :label="item"
                      :value="item"
                    ></el-option>
                  </el-select>
                </el-form-item>

                <el-form-item class="field-block" label="联系电话" prop="lianxidianhua">
                  <el-input v-model="sessionForm.lianxidianhua" placeholder="联系电话"></el-input>
                </el-form-item>

                <el-form-item class="field-block field-span-2" label="身份证号" prop="shenfenzheng">
                  <el-input v-model="sessionForm.shenfenzheng" placeholder="身份证"></el-input>
                </el-form-item>
              </div>

              <div class="form-grid" v-else>
                <div class="fallback-card">
                  当前角色暂无可编辑的个人资料字段。
                </div>
              </div>

              <div class="action-row">
                <button class="secondary-btn" type="button" @click="logout">退出登录</button>
                <button class="primary-btn" type="button" @click="onSubmit('sessionForm')">确定</button>
              </div>
            </el-form>
          </template>

          <template v-else-if="activeSection === 'password'">
            <div class="password-header">
              <div class="identity-kicker">账户安全</div>
              <h3>修改密码</h3>
              <p>使用当前密码设置新的安全密码。</p>
            </div>

            <el-form
              ref="passwordForm"
              class="profile-form password-form"
              :model="passwordForm"
              :rules="passwordRules"
              label-position="top"
            >
              <div class="form-grid password-grid">
                <el-form-item class="field-block field-span-2" label="当前密码" prop="password">
                  <el-input v-model="passwordForm.password" type="password" placeholder="原密码"></el-input>
                </el-form-item>

                <el-form-item class="field-block" label="新密码" prop="newpassword">
                  <el-input v-model="passwordForm.newpassword" type="password" placeholder="新密码"></el-input>
                </el-form-item>

                <el-form-item class="field-block" label="确认密码" prop="repassword">
                  <el-input v-model="passwordForm.repassword" type="password" placeholder="确认密码"></el-input>
                </el-form-item>

                <el-form-item class="field-block" label="验证码" prop="captcha">
                  <el-input v-model="passwordForm.captcha" placeholder="请输入验证码"></el-input>
                </el-form-item>

                <div class="field-block captcha-panel">
                  <span class="captcha-label">当前验证码</span>
                  <div class="captcha-row">
                    <span class="captcha-code">{{ passwordCaptcha || '----' }}</span>
                    <button class="secondary-btn captcha-refresh" type="button" @click="fetchPasswordCaptcha">刷新验证码</button>
                  </div>
                </div>
              </div>

              <div class="action-row">
                <button class="secondary-btn" type="button" @click="activeSection = 'profile'">返回资料</button>
                <button class="primary-btn" type="button" @click="updatePassword">修改密码</button>
              </div>
            </el-form>
          </template>

          <template v-else-if="activeSection === 'storeup'">
            <storeup-panel embedded></storeup-panel>
          </template>
        </section>

        <footer class="page-footer fade-slide-up delay-2">
          <p>{{ footerLeftText }}</p>
          <p>{{ footerRightText }}</p>
        </footer>
      </div>
    </main>
  </div>
</template>

<script>
import config from '@/config/config'
import menu from '@/config/menu'
import StoreupPanel from '../storeup/storeup-panel'

const {
  resolveCenterAvatar,
  buildCenterNavItems,
} = require('./center-helpers')
const {
  confirmFrontLogout,
  dispatchFrontAvatarChanged,
} = require('../../utils/front-logout')

export default {
  components: {
    StoreupPanel,
  },
  data() {
    return {
      title: '个人中心',
      activeSection: 'profile',
      baseUrl: config.baseUrl,
      fallbackAvatar: require('@/assets/avator.png'),
      sessionForm: {},
      passwordForm: {
        password: '',
        newpassword: '',
        repassword: '',
        captcha: '',
      },
      passwordCaptcha: '',
      passwordRules: {
        password: [
          { required: true, message: '密码不能为空', trigger: 'blur' },
        ],
        newpassword: [
          { required: true, message: '新密码不能为空', trigger: 'blur' },
        ],
        repassword: [
          { required: true, message: '确认密码不能为空', trigger: 'blur' },
        ],
        captcha: [
          { required: true, message: '验证码不能为空', trigger: 'blur' },
        ],
      },
      rules: {},
      menuList: [],
      userTableName: localStorage.getItem('UserTableName'),
      dynamicProp: {},
    }
  },
  computed: {
    sidebarItems() {
      return buildCenterNavItems(this.menuList).map(item => {
        if (item.icon === 'person') {
          item.icon = 'el-icon-user-solid'
        } else if (item.icon === 'lock') {
          item.icon = 'el-icon-lock'
        } else if (item.icon === 'favorite') {
          item.icon = 'el-icon-star-on'
        } else if (item.icon === 'logout') {
          item.icon = 'el-icon-switch-button'
        } else {
          item.icon = 'el-icon-menu'
        }
        return item
      })
    },
    profileAvatar() {
      return resolveCenterAvatar(this.sessionForm, this.baseUrl, this.fallbackAvatar)
    },
    profileName() {
      return this.sessionForm.yonghuxingming || this.sessionForm.yonghuzhanghao || '影院会员'
    },
    pageTitle() {
      if (this.activeSection === 'password') {
        return '账户安全'
      }
      if (this.activeSection === 'storeup') {
        return '我的收藏'
      }
      return '个人中心'
    },
    pageSubtitle() {
      if (this.activeSection === 'password') {
        return '管理账号凭证，保护你的个人信息安全。'
      }
      if (this.activeSection === 'storeup') {
        return '浏览、排序并管理你收藏的影片。'
      }
      return '编辑个人资料，完善你的观影身份。'
    },
    lastUpdatedText() {
      return new Date().toISOString().slice(0, 10)
    },
    footerLeftText() {
      return '安全等级：金卡会员'
    },
    footerRightText() {
      return `最近更新：${this.lastUpdatedText}`
    },
  },
  created() {
    this.initMenuList()
    this.initSessionForm()
    this.initRules()
    this.init()
    this.syncSectionFromRoute()
  },
  watch: {
    '$route.query.section'() {
      this.syncSectionFromRoute()
    },
    activeSection(next) {
      if (next === 'password') {
        this.fetchPasswordCaptcha()
      }
    },
  },
  methods: {
    encryptPasswordValue(value) {
      return value ? this.encryptAes(value) : value
    },
    sanitizeSessionForm(sessionForm) {
      if (!sessionForm || typeof sessionForm !== 'object') {
        return {}
      }
      const sanitized = { ...sessionForm }
      delete sanitized.password
      delete sanitized.mima
      return sanitized
    },
    normalizeSection(section) {
      if (section === 'password' || section === 'storeup') {
        return section
      }
      return 'profile'
    },
    syncSectionFromRoute() {
      this.activeSection = this.normalizeSection(this.$route.query.section)
    },
    initMenuList() {
      const menus = menu.list()
      for (let x in menus) {
        if (menus[x].tableName === this.userTableName) {
          this.menuList = (menus[x].backMenu || []).filter(item => item.menu !== '考试管理')
        }
      }
    },
    initSessionForm() {
      const rawSession = localStorage.getItem('sessionForm')
      if (!rawSession) {
        this.sessionForm = {}
        return
      }

      try {
        this.sessionForm = this.sanitizeSessionForm(JSON.parse(rawSession) || {})
      } catch (error) {
        this.sessionForm = {}
      }
    },
    initRules() {
      if (this.userTableName === 'yonghu') {
        this.$set(this.sessionForm, 'yonghuzhanghao', this.sessionForm.yonghuzhanghao || null)
        this.$set(this.sessionForm, 'mima', this.sessionForm.mima || null)
        this.$set(this.sessionForm, 'yonghuxingming', this.sessionForm.yonghuxingming || null)
        this.$set(this.sessionForm, 'touxiang', this.sessionForm.touxiang || null)
        this.$set(this.sessionForm, 'xingbie', this.sessionForm.xingbie || null)
        this.$set(this.sessionForm, 'lianxidianhua', this.sessionForm.lianxidianhua || null)
        this.$set(this.sessionForm, 'shenfenzheng', this.sessionForm.shenfenzheng || null)

        this.$set(this.rules, 'yonghuzhanghao', [{ required: true, message: '请输入用户账号', trigger: 'blur' }])
        this.$set(this.rules, 'mima', [{ required: true, message: '请输入密码', trigger: 'blur' }])
        this.$set(this.rules, 'yonghuxingming', [{ required: true, message: '请输入用户姓名', trigger: 'blur' }])
        this.$set(this.rules, 'lianxidianhua', [{ required: false, validator: this.$validate.isMobile, trigger: 'blur' }])
        this.$set(this.rules, 'shenfenzheng', [{ required: false, validator: this.$validate.isIdCard, trigger: 'blur' }])
      }
    },
    init() {
      if (this.userTableName === 'yonghu') {
        this.dynamicProp.xingbie = '男,女'.split(',')
      }
    },
    goHome() {
      window.location.hash = '#/index/home'
    },
    isItemActive(item) {
      if (item.key === 'profile') {
        return this.activeSection === 'profile'
      }
      if (item.key === 'password') {
        return this.activeSection === 'password'
      }
      if (item.key === 'storeup') {
        return this.activeSection === 'storeup'
      }
      return false
    },
    selectSidebarItem(item) {
      if (item.type === 'panel') {
        this.activeSection = item.key
        if (item.key === 'password') {
          this.resetPasswordForm()
        }
        return
      }

      if (item.key === 'storeup') {
        localStorage.setItem('storeupType', 1)
      }

      if (item.type === 'route' && item.route) {
        if (item.route.indexOf('?') > -1) {
          const routePath = item.route.split('?')[0]
          const routeQuery = {}
          item.route.split('?')[1].split('&').forEach(pair => {
            const arr = pair.split('=')
            routeQuery[arr[0]] = arr[1]
          })
          this.$router.push({ path: routePath, query: routeQuery })
        } else {
          this.$router.push(item.route)
        }
        return
      }

      if (item.type === 'action' && item.action === 'logout') {
        this.logout()
      }
    },
    setSession() {
      const sanitizedSessionForm = this.sanitizeSessionForm(this.sessionForm)
      this.sessionForm = sanitizedSessionForm
      localStorage.setItem('sessionForm', JSON.stringify(sanitizedSessionForm))
      const avatar = sanitizedSessionForm.touxiang || ''
      if (avatar) {
        localStorage.setItem('frontHeadportrait', avatar.replace(new RegExp(this.$config.baseUrl, 'g'), ''))
      } else {
        localStorage.removeItem('frontHeadportrait')
      }
      dispatchFrontAvatarChanged(sanitizedSessionForm, localStorage.getItem('frontHeadportrait') || '')
    },
    notifyAvatarChanged(sessionForm, cachedAvatar) {
      dispatchFrontAvatarChanged(sessionForm, cachedAvatar)
    },
    onSubmit(formName) {
      if (this.userTableName === 'yonghu' && this.sessionForm.touxiang != null) {
        this.sessionForm.touxiang = this.sessionForm.touxiang.replace(new RegExp(this.$config.baseUrl, 'g'), '')
      }
      this.$refs[formName].validate(valid => {
        if (!valid) {
          return false
        }
        this.$http.post(this.userTableName + '/update', this.sessionForm).then(res => {
          if (res.data.code == 0) {
            this.setSession()
            this.$message({
              message: '更新成功',
              type: 'success',
              duration: 1500,
            })
          }
        })
      })
    },
    yonghutouxiangHandleAvatarSuccess(fileUrls) {
      this.sessionForm.touxiang = fileUrls
    },
    resetPasswordForm() {
      this.passwordForm = {
        password: '',
        newpassword: '',
        repassword: '',
        captcha: '',
      }
      this.$nextTick(() => {
        if (this.$refs.passwordForm) {
          this.$refs.passwordForm.clearValidate()
        }
      })
    },
    fetchPasswordCaptcha() {
      if (!this.userTableName) {
        return
      }
      this.$http.get(`${this.userTableName}/changePasswordCaptcha`).then(({ data }) => {
        if (data && data.code === 0 && data.data) {
          this.passwordCaptcha = data.data.captcha || data.data.code || ''
          return
        }
        this.passwordCaptcha = ''
        if (data && data.msg) {
          this.$message.error(data.msg)
        }
      }).catch(() => {
        this.passwordCaptcha = ''
      })
    },
    async updatePassword() {
      this.$refs.passwordForm.validate(async valid => {
        if (!valid) {
          return
        }
        if (this.passwordForm.newpassword != this.passwordForm.repassword) {
          this.$message.error('两次密码输入不一致')
          return
        }
        if (this.passwordForm.newpassword == this.passwordForm.password) {
          this.$message.error('新密码与原密码相同！')
          return
        }
        this.$http.post(`${this.userTableName}/changePassword`, {
          oldPassword: this.encryptPasswordValue(this.passwordForm.password),
          newPassword: this.encryptPasswordValue(this.passwordForm.newpassword),
          captcha: this.passwordForm.captcha,
        }).then(({ data }) => {
          if (data && data.code === 0) {
            this.$message({
              message: '修改密码成功',
              type: 'success',
              duration: 1500,
            })
            this.setSession()
            this.resetPasswordForm()
            this.fetchPasswordCaptcha()
          } else {
            this.$message.error(data.msg)
          }
        })
      })
    },
    logout() {
      return confirmFrontLogout(this, {
        afterClear: () => {
          localStorage.setItem('keyPath', '0')
        },
      })
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.center-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 264px minmax(0, 1fr);
  background:
    radial-gradient(circle at top left, rgba(255, 198, 57, 0.08), transparent 26%),
    radial-gradient(circle at right 20%, rgba(69, 110, 255, 0.14), transparent 24%),
    #0b1326;
  color: #dae2fd;
  overflow: hidden;
}

.fade-slide-up {
  opacity: 0;
  animation: fadeSlideUp .7s ease forwards;
}

.delay-1 {
  animation-delay: .08s;
}

.delay-2 {
  animation-delay: .16s;
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.center-sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 32px 18px 24px;
  background: rgba(5, 10, 20, 0.82);
  backdrop-filter: blur(18px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.38);
  display: flex;
  flex-direction: column;
  z-index: 2;
}

.brand-block {
  border: 0;
  margin-bottom: 30px;
  background: transparent;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 8px;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: transform .25s ease, opacity .25s ease;
}

.brand-block:hover {
  transform: translateX(2px);
}

.brand-block:focus {
  outline: none;
}

.brand-block:focus-visible .brand-mark {
  box-shadow: 0 0 0 3px rgba(47, 200, 255, 0.16), 0 12px 30px rgba(47, 200, 255, 0.24);
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #04111a;
  background: linear-gradient(135deg, #2fc8ff, #2f6dff);
  box-shadow: 0 12px 30px rgba(47, 200, 255, 0.24);
  font-size: 18px;
}

.brand-title {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  color: #f6f7fb;
  line-height: 1;
}

.brand-main {
  font-size: 19px;
  font-weight: 300;
  letter-spacing: 0.4px;
}

.brand-accent {
  color: #2fc8ff;
  font-size: 19px;
  font-weight: 700;
  text-shadow: 0 0 16px rgba(47, 200, 255, 0.55);
}

.brand-subtitle {
  color: rgba(188, 199, 222, 0.62);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .28em;
  text-transform: uppercase;
  margin-top: 4px;
}

.side-nav {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 8px;
}

.side-nav-item {
  border: 0;
  border-right: 4px solid transparent;
  padding: 16px 18px;
  border-radius: 18px 0 0 18px;
  background: transparent;
  color: rgba(188, 199, 222, 0.76);
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: .16em;
  text-transform: uppercase;
  transition: color .25s ease, background .25s ease, transform .25s ease, border-color .25s ease;
  text-align: left;
}

.side-nav-item i {
  font-size: 18px;
}

.side-nav-item:hover {
  color: #ffe2a4;
  background: rgba(255, 255, 255, 0.04);
  transform: translateX(2px);
}

.side-nav-item-active {
  color: #ffc639;
  border-right-color: #ffc639;
  background: linear-gradient(90deg, rgba(255, 198, 57, 0.12), rgba(255, 198, 57, 0));
  box-shadow: inset 0 0 0 1px rgba(255, 198, 57, 0.04);
}

.sidebar-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding: 18px 8px 0;
  display: grid;
  gap: 8px;
}

.sidebar-meta {
  color: rgba(188, 199, 222, 0.44);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .14em;
  text-transform: uppercase;
}

.center-main {
  position: relative;
  min-width: 0;
  overflow: auto;
}

.hero-backdrop {
  position: absolute;
  inset: 0 auto auto 0;
  width: 100%;
  height: 380px;
  background:
    linear-gradient(180deg, rgba(8, 14, 28, 0.18), rgba(11, 19, 38, 0.96)),
    url('https://lh3.googleusercontent.com/aida-public/AB6AXuBWtXy0rTFXLSfRTV4ROKEk8kqa9cJYGy_BQCCEVl0ouryZI0auboD2ORSZQ10bftjfQxZ6chYfDuPBiyno7pWDs7zI0yQxnH2xnml73l0oe6RbeWqPNgYfLhjq1IRc54YgkA3Kdc9Ce1iPQBtqAdeAJ33Wap84Ecqq2Xl6V2mh0mm8izE_0qc-zIir09uoV2e9CbYAhhh6OE-I_t8o1Ks6BobeyTPtxDDncM5MDFhnjzku04QTL6UJ_fv4nkndveVOH2ajapWPxe8')
      center/cover no-repeat;
  opacity: .22;
  pointer-events: none;
}

.hero-glow {
  position: fixed;
  border-radius: 999px;
  filter: blur(120px);
  pointer-events: none;
}

.hero-glow-left {
  left: -120px;
  top: 60px;
  width: 260px;
  height: 260px;
  background: rgba(255, 198, 57, 0.14);
}

.hero-glow-right {
  right: -100px;
  bottom: -120px;
  width: 320px;
  height: 320px;
  background: rgba(57, 107, 255, 0.18);
}

.center-content {
  position: relative;
  z-index: 1;
  max-width: 1120px;
  padding: 52px 48px 44px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 28px;
}

.page-header h2 {
  margin: 0 0 10px;
  color: #dae2fd;
  font-size: clamp(34px, 4vw, 54px);
  font-weight: 900;
  letter-spacing: -.04em;
}

.page-header p {
  margin: 0;
  color: rgba(208, 197, 175, 0.82);
  font-size: 15px;
  font-weight: 600;
}

.panel-card {
  border: 1px solid rgba(153, 144, 124, 0.12);
  border-radius: 32px;
  padding: 34px 34px 30px;
  background: rgba(23, 31, 51, 0.52);
  backdrop-filter: blur(24px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.38);
}

.profile-identity,
.password-header {
  display: flex;
  align-items: center;
  gap: 28px;
  margin-bottom: 30px;
}

.avatar-stack {
  position: relative;
  width: 132px;
  height: 132px;
  flex: 0 0 132px;
}

.avatar-ring {
  width: 132px;
  height: 132px;
  padding: 6px;
  border-radius: 50%;
  background: linear-gradient(180deg, rgba(255, 198, 57, 0.26), rgba(255, 198, 57, 0.04));
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.34);
}

.avatar-ring img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

.avatar-upload {
  position: absolute;
  right: -2px;
  bottom: -2px;
}

.identity-kicker {
  color: rgba(255, 198, 57, 0.88);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: .18em;
  text-transform: uppercase;
  margin-bottom: 10px;
}

.identity-copy h3,
.password-header h3 {
  margin: 0 0 8px;
  color: #dae2fd;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -.03em;
}

.identity-copy p,
.password-header p {
  margin: 0;
  color: rgba(208, 197, 175, 0.72);
  font-size: 14px;
  font-weight: 600;
}

.profile-form {
  margin-top: 10px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px 28px;
}

.password-grid {
  align-items: start;
}

.field-span-2 {
  grid-column: 1 / -1;
}

.fallback-card {
  grid-column: 1 / -1;
  border-radius: 24px;
  padding: 22px;
  background: rgba(6, 14, 32, 0.66);
  color: rgba(218, 226, 253, 0.76);
}

.action-row {
  margin-top: 34px;
  padding-top: 28px;
  border-top: 1px solid rgba(153, 144, 124, 0.12);
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.captcha-panel {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 10px;
}

.captcha-label {
  font-size: 13px;
  color: rgba(218, 226, 253, 0.7);
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 48px;
}

.captcha-code {
  min-width: 92px;
  padding: 10px 16px;
  border-radius: 14px;
  background: rgba(10, 18, 36, 0.72);
  border: 1px solid rgba(120, 160, 255, 0.22);
  color: #8eb7ff;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 4px;
  text-align: center;
}

.captcha-refresh {
  height: 44px;
  padding: 0 18px;
  min-width: 132px;
}

.primary-btn,
.secondary-btn {
  border-radius: 999px;
  min-width: 144px;
  height: 50px;
  padding: 0 28px;
  font-size: 13px;
  font-weight: 900;
  letter-spacing: .18em;
  text-transform: uppercase;
  transition: transform .25s ease, box-shadow .25s ease, border-color .25s ease, background .25s ease;
}

.primary-btn {
  border: 0;
  color: #3f2e00;
  background: linear-gradient(135deg, #ffc639, #e1aa12);
  box-shadow: 0 10px 30px rgba(255, 198, 57, 0.28);
}

.primary-btn:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 14px 34px rgba(255, 198, 57, 0.34);
}

.secondary-btn {
  border: 1px solid rgba(153, 144, 124, 0.42);
  color: #dae2fd;
  background: transparent;
}

.secondary-btn:hover {
  transform: translateY(-2px);
  border-color: rgba(255, 198, 57, 0.48);
  background: rgba(255, 255, 255, 0.04);
}

.page-footer {
  margin-top: 22px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  color: rgba(188, 199, 222, 0.34);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .18em;
  text-transform: uppercase;
}

.field-block {
  margin-bottom: 0;
}

.field-block ::v-deep .el-form-item__label {
  padding: 0 0 8px;
  color: rgba(208, 197, 175, 0.74);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: .16em;
  text-transform: uppercase;
  line-height: 1.4;
}

.field-block ::v-deep .el-form-item__content {
  line-height: 1;
}

.field-block ::v-deep .el-input__inner,
.field-block ::v-deep .el-select .el-input__inner {
  border: 1px solid rgba(153, 144, 124, 0.18);
  border-radius: 20px;
  height: 54px;
  padding: 0 18px;
  background: rgba(45, 52, 73, 0.88);
  color: #dae2fd;
  font-size: 16px;
  font-weight: 700;
  box-shadow: none;
  transition: border-color .25s ease, box-shadow .25s ease, background .25s ease;
}

.field-block ::v-deep .el-input__inner::placeholder {
  color: rgba(218, 226, 253, 0.28);
}

.field-block ::v-deep .el-input__inner:focus,
.field-block ::v-deep .el-select .el-input.is-focus .el-input__inner,
.field-block ::v-deep .el-select .el-input__inner:focus {
  border-color: rgba(255, 198, 57, 0.82);
  box-shadow: 0 0 0 3px rgba(255, 198, 57, 0.12);
}

.field-readonly ::v-deep .el-input__inner[readonly='readonly'] {
  border-color: rgba(153, 144, 124, 0.12);
  background: rgba(6, 14, 32, 0.72);
  color: rgba(218, 226, 253, 0.52);
  cursor: not-allowed;
}

.field-block ::v-deep .el-form-item__error {
  padding-top: 8px;
  color: #ffb4ab;
}

.avatar-upload ::v-deep .el-upload {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-upload ::v-deep .el-upload--picture-card,
.avatar-upload ::v-deep .el-upload .el-icon-plus {
  width: 42px;
  height: 42px;
  border: 0;
  border-radius: 50%;
  line-height: 42px;
  background: linear-gradient(135deg, #ffc639, #e1aa12);
  color: #3f2e00;
  font-size: 20px;
  box-shadow: 0 8px 20px rgba(255, 198, 57, 0.3);
}

.avatar-upload ::v-deep .el-upload-list {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.avatar-upload ::v-deep .el-upload-list__item {
  display: none;
}

.avatar-upload ::v-deep .el-upload__tip {
  display: none;
}

@media (max-width: 1100px) {
  .center-shell {
    grid-template-columns: 1fr;
  }

  .center-sidebar {
    position: relative;
    height: auto;
    padding-bottom: 18px;
  }

  .side-nav {
    flex-direction: row;
    overflow-x: auto;
    padding-bottom: 6px;
  }

  .side-nav-item {
    min-width: max-content;
    border-right-width: 0;
    border-bottom: 3px solid transparent;
    border-radius: 16px;
  }

  .side-nav-item-active {
    border-bottom-color: #ffc639;
  }

  .center-content {
    padding: 34px 20px 28px;
  }
}

@media (max-width: 760px) {
  .panel-card {
    padding: 24px 18px 20px;
    border-radius: 26px;
  }

  .profile-identity,
  .password-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .field-span-2 {
    grid-column: auto;
  }

  .action-row,
  .page-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .primary-btn,
  .secondary-btn {
    width: 100%;
  }
}
</style>
