<template>
  <div class="cinema-register">
    <div class="register-atmosphere">
      <div class="atmosphere-mask"></div>
      <img class="atmosphere-image" :src="backgroundImage" alt="影院氛围背景">
    </div>

    <main class="register-main">
      <el-form
        v-if="pageFlag == 'register'"
        ref="registerForm"
        :model="registerForm"
        :rules="rules"
        class="register-card glass-card"
        @submit.native.prevent
      >
        <header class="register-header">
          <h1>加入观影俱乐部</h1>
          <p>创建你的账号，开启专属的电影探索与收藏之旅。</p>
        </header>

        <section class="avatar-section" v-if="tableName == 'yonghu'">
          <div class="avatar-shell">
            <div class="avatar-ring" :class="{ 'has-avatar': avatarPreviewUrl }">
              <img v-if="avatarPreviewUrl" class="avatar-preview" :src="avatarPreviewUrl" alt="已上传头像">
              <file-upload
                class="avatar-upload-layer"
                tip="点击上传头像"
                action="file/upload"
                :limit="1"
                :multiple="true"
                :replaceable="true"
                :fileUrls="registerForm.touxiang ? registerForm.touxiang : ''"
                @change="yonghutouxiangUploadChange"
              ></file-upload>
            </div>
            <div class="avatar-badge">
              <i class="el-icon-camera"></i>
            </div>
          </div>
          <span class="avatar-label">头像</span>
        </section>

        <section class="register-grid">
          <el-form-item class="field-row field-span-2" v-if="tableName == 'yonghu'" prop="yonghuzhanghao">
            <label class="field-label" :class="{ required: changeRules('yonghuzhanghao') }">用户账号</label>
            <el-input v-model="registerForm.yonghuzhanghao" placeholder="请输入用户账号" />
          </el-form-item>

          <el-form-item class="field-row" v-if="tableName == 'yonghu'" prop="mima">
            <label class="field-label" :class="{ required: changeRules('mima') }">密码</label>
            <el-input v-model="registerForm.mima" type="password" placeholder="请输入密码" />
          </el-form-item>

          <el-form-item class="field-row" v-if="tableName == 'yonghu'" prop="mima2">
            <label class="field-label" :class="{ required: changeRules('mima') }">确认密码</label>
            <el-input v-model="registerForm.mima2" type="password" placeholder="请再次输入密码" />
          </el-form-item>

          <el-form-item class="field-row field-span-2" v-if="tableName == 'yonghu'" prop="yonghuxingming">
            <label class="field-label" :class="{ required: changeRules('yonghuxingming') }">姓名</label>
            <el-input v-model="registerForm.yonghuxingming" placeholder="请输入用户姓名" />
          </el-form-item>

          <el-form-item class="field-row" v-if="tableName == 'yonghu'" prop="xingbie">
            <label class="field-label" :class="{ required: changeRules('xingbie') }">性别</label>
            <el-select v-model="registerForm.xingbie" placeholder="请选择性别">
              <el-option
                v-for="(item, index) in yonghuxingbieOptions"
                :key="index"
                :label="item"
                :value="item"
              ></el-option>
            </el-select>
          </el-form-item>

          <el-form-item class="field-row" v-if="tableName == 'yonghu'" prop="lianxidianhua">
            <label class="field-label" :class="{ required: changeRules('lianxidianhua') }">联系电话</label>
            <el-input v-model="registerForm.lianxidianhua" placeholder="请输入联系电话" />
          </el-form-item>

          <el-form-item class="field-row field-span-2" v-if="tableName == 'yonghu'" prop="shenfenzheng">
            <label class="field-label" :class="{ required: changeRules('shenfenzheng') }">身份证号</label>
            <el-input v-model="registerForm.shenfenzheng" placeholder="请输入身份证" />
          </el-form-item>
        </section>

        <div class="register-actions">
          <button class="register-submit" type="button" @click="submitForm('registerForm')">立即注册</button>
        </div>

        <div class="signin-section">
          <p>
            <router-link class="signin-link" to="/login">已有账号，直接登录</router-link>
          </p>
        </div>
      </el-form>
    </main>

    <footer class="register-footer">
      <div class="footer-links">
        <a href="javascript:void(0);">服务条款</a>
        <a href="javascript:void(0);">隐私政策</a>
        <a href="javascript:void(0);">帮助中心</a>
        <a href="javascript:void(0);">无障碍说明</a>
      </div>
      <router-link class="footer-brand footer-home-link" to="/index/home">以太影院</router-link>
      <p class="footer-copy">© 2024 以太影院 · 导演剪辑版</p>
    </footer>
  </div>
</template>

<script>
import 'animate.css'
import { useRegisterDraftStore } from '@/stores/register-draft'

function buildDefaultRegisterForm() {
  return {
    yonghuzhanghao: '',
    mima: '',
    mima2: '',
    yonghuxingming: '',
    touxiang: '',
    xingbie: '',
    lianxidianhua: '',
    shenfenzheng: '',
  }
}

export default {
  data() {
    return {
      pageFlag: '',
      tableName: '',
      registerForm: {},
      forgetForm: {},
      rules: {},
      requiredRules: {},
      yonghuxingbieOptions: [],
      backgroundImage: require('@/assets/login-bg.jpg'),
    }
  },
  mounted() {
    if (this.$route.query.pageFlag == 'register') {
      this.tableName = this.$route.query.role
      if (this.tableName == 'yonghu') {
        this.registerForm = buildDefaultRegisterForm()
      }
      if ('yonghu' == this.tableName) {
        this.rules.yonghuzhanghao = [{ required: true, message: '请输入用户账号', trigger: 'blur' }]
        this.requiredRules.yonghuzhanghao = [{ required: true, message: '请输入用户账号', trigger: 'blur' }]
      }
      if ('yonghu' == this.tableName) {
        this.rules.mima = [{ required: true, message: '请输入密码', trigger: 'blur' }]
        this.requiredRules.mima = [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
      if ('yonghu' == this.tableName) {
        this.rules.yonghuxingming = [{ required: true, message: '请输入用户姓名', trigger: 'blur' }]
        this.requiredRules.yonghuxingming = [{ required: true, message: '请输入用户姓名', trigger: 'blur' }]
      }
      this.yonghuxingbieOptions = '男,女'.split(',')
      if ('yonghu' == this.tableName) {
        this.rules.lianxidianhua = [{ required: true, validator: this.$validate.isMobile, trigger: 'blur' }]
      }
      if ('yonghu' == this.tableName) {
        this.rules.shenfenzheng = [{ required: true, validator: this.$validate.isIdCard, trigger: 'blur' }]
      }
      this.restoreRegisterDraft()
    }
  },
  created() {
    this.pageFlag = this.$route.query.pageFlag
  },
  watch: {
    registerForm: {
      deep: true,
      handler() {
        this.persistRegisterDraft()
      },
    },
    'registerForm.touxiang'() {
      this.persistRegisterDraft()
    },
  },
  computed: {
    avatarPreviewUrl() {
      if (!this.registerForm || !this.registerForm.touxiang) {
        return ''
      }
      const avatar = this.registerForm.touxiang.split(',')[0]
      if (!avatar) {
        return ''
      }
      if (/^https?:\/\//.test(avatar)) {
        return avatar
      }
      return `${this.$config.baseUrl}${avatar}`
    },
  },
  methods: {
    encryptPasswordValue(value) {
      return value ? this.encryptAes(value) : value
    },
    getRegisterDraftStore() {
      return useRegisterDraftStore()
    },
    restoreRegisterDraft() {
      if (this.tableName !== 'yonghu') {
        return
      }
      this.registerForm = Object.assign(buildDefaultRegisterForm(), this.getRegisterDraftStore().getDraft())
    },
    persistRegisterDraft() {
      if (this.pageFlag !== 'register' || this.tableName !== 'yonghu' || !this.registerForm) {
        return
      }
      this.getRegisterDraftStore().saveDraft(this.registerForm)
    },
    clearRegisterDraft() {
      this.getRegisterDraftStore().clearDraft()
    },
    changeRules(name) {
      if (this.requiredRules[name]) {
        return true
      }
      return false
    },
    getUUID() {
      return new Date().getTime()
    },
    yonghutouxiangUploadChange(fileUrls) {
      this.registerForm.touxiang = fileUrls.replace(new RegExp(this.$config.baseUrl, 'g'), '')
      this.persistRegisterDraft()
    },
    submitForm(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          const url = this.tableName + '/register'
          if (`yonghu` == this.tableName && this.registerForm.mima != this.registerForm.mima2) {
            this.$message.error(`两次密码输入不一致`)
            return
          }
          const payload = {
            ...this.registerForm,
            mima: this.encryptPasswordValue(this.registerForm.mima),
            mima2: this.encryptPasswordValue(this.registerForm.mima2),
          }
          this.$http.post(url, payload).then(res => {
            if (res.data.code === 0) {
              this.clearRegisterDraft()
              this.$message({
                message: '注册成功',
                type: 'success',
                duration: 1500,
                onClose: () => {
                  this.$router.push('/login')
                },
              })
            } else {
              this.$message.error(res.data.msg)
            }
          })
        } else {
          return false
        }
      })
    },
    resetForm(formName) {
      this.$refs[formName].resetFields()
      this.clearRegisterDraft()
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.cinema-register {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: #0b1326;
  color: #dae2fd;
  font-family: 'Manrope', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.register-atmosphere {
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
    linear-gradient(180deg, rgba(11, 19, 38, 0.38), rgba(11, 19, 38, 0.76) 34%, rgba(11, 19, 38, 0.96));
}

.register-main,
.register-footer {
  position: relative;
  z-index: 2;
}

.register-main {
  min-height: calc(100vh - 220px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 20px 72px;
}

.register-card {
  width: 100%;
  max-width: 860px;
  border-radius: 28px;
  border: 1px solid rgba(153, 144, 124, 0.15);
  background: rgba(45, 52, 73, 0.38);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.36);
  padding: 36px 36px 30px;
}

.glass-card {
  backdrop-filter: blur(34px);
  -webkit-backdrop-filter: blur(34px);
}

.register-header {
  margin-bottom: 26px;
  text-align: center;
}

.register-header h1 {
  margin: 0 0 10px;
  color: #ffc639;
  font-family: 'Epilogue', 'PingFang SC', sans-serif;
  font-size: 44px;
  font-weight: 800;
  letter-spacing: -0.04em;
}

.register-header p {
  margin: 0;
  color: rgba(208, 197, 175, 0.82);
  font-size: 17px;
  line-height: 1.7;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  margin-bottom: 30px;
}

.avatar-shell {
  position: relative;
}

.avatar-ring {
  position: relative;
  width: 112px;
  height: 112px;
  border: 2px dashed rgba(255, 198, 57, 0.38);
  border-radius: 50%;
  background: rgba(34, 42, 61, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-ring.has-avatar {
  border-style: solid;
  border-color: rgba(255, 198, 57, 0.7);
}

.avatar-preview {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  z-index: 1;
  pointer-events: none;
}

.avatar-badge {
  position: absolute;
  right: 0;
  bottom: 0;
  z-index: 4;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffc639, #e1aa12);
  color: #3f2e00;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 14px 28px rgba(225, 170, 18, 0.22);
}

.avatar-label {
  color: #d0c5af;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: .16em;
  text-transform: uppercase;
}

.avatar-upload-layer {
  position: absolute;
  inset: 0;
  z-index: 2;
  display: block;
}

.avatar-upload-layer ::v-deep > div,
.avatar-upload-layer ::v-deep .upload {
  width: 100%;
  height: 100%;
}

.avatar-upload-layer ::v-deep .el-upload {
  width: 100%;
  height: 100%;
}

.avatar-upload-layer ::v-deep .el-upload--picture-card,
.avatar-upload-layer ::v-deep .upload-img,
.avatar-upload-layer ::v-deep .el-upload .el-icon-plus {
  width: 112px !important;
  height: 112px !important;
  margin: 0 !important;
  border: 0 !important;
  border-radius: 50% !important;
  background: transparent !important;
  color: #ffc639 !important;
  line-height: 112px !important;
}

.avatar-ring.has-avatar .avatar-upload-layer ::v-deep .el-upload--picture-card,
.avatar-ring.has-avatar .avatar-upload-layer ::v-deep .el-upload .el-icon-plus,
.avatar-ring.has-avatar .avatar-upload-layer ::v-deep .el-upload__tip {
  opacity: 0 !important;
}

.avatar-upload-layer ::v-deep .el-upload-list {
  display: none;
}

.avatar-upload-layer ::v-deep .el-upload__tip {
  display: none;
}

.register-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 22px 20px;
}

.field-span-2 {
  grid-column: 1 / -1;
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

.field-label.required::after {
  content: ' *';
  color: #ffb4ab;
}

.field-row ::v-deep .el-input__inner,
.field-row ::v-deep .el-select .el-input__inner {
  width: 100%;
  height: 54px;
  border: 1px solid rgba(77, 70, 53, 0.3);
  border-radius: 16px;
  background: rgba(34, 42, 61, 0.76);
  color: #dae2fd;
  font-size: 15px;
  transition: border-color .24s ease, box-shadow .24s ease, background-color .24s ease;
}

.field-row ::v-deep .el-input__inner::placeholder,
.field-row ::v-deep .el-select .el-input__inner::placeholder {
  color: rgba(208, 197, 175, 0.42);
}

.field-row ::v-deep .el-input__inner:focus,
.field-row ::v-deep .el-select .el-input.is-focus .el-input__inner,
.field-row ::v-deep .el-select .el-input__inner:focus {
  border-color: rgba(255, 198, 57, 0.82);
  box-shadow: 0 0 0 3px rgba(255, 198, 57, 0.1);
  background: rgba(34, 42, 61, 0.9);
}

.field-row ::v-deep .el-input__icon {
  color: rgba(208, 197, 175, 0.7);
  line-height: 54px;
}

.field-row ::v-deep .el-form-item__error {
  padding-top: 6px;
  color: #ffb4ab;
}

.register-actions {
  margin-top: 28px;
}

.register-submit {
  width: 100%;
  height: 58px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ffc639, #e1aa12);
  color: #3f2e00;
  font-size: 18px;
  font-weight: 900;
  letter-spacing: .08em;
  cursor: pointer;
  box-shadow: 0 18px 36px rgba(225, 170, 18, 0.18);
  transition: transform .2s ease, box-shadow .2s ease;
}

.register-submit:hover {
  transform: translateY(-1px) scale(1.01);
  box-shadow: 0 22px 40px rgba(225, 170, 18, 0.26);
}

.signin-section {
  padding-top: 18px;
  text-align: center;
}

.signin-section p {
  margin: 0;
  color: rgba(218, 226, 253, 0.62);
  font-size: 14px;
}

.signin-link {
  margin-left: 6px;
  color: #ffc639;
  font-weight: 800;
  text-decoration: none;
}

.register-footer {
  width: 100%;
  padding: 16px 20px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px 28px;
}

.footer-links a,
.footer-copy {
  color: rgba(218, 226, 253, 0.5);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .14em;
  text-transform: uppercase;
  text-decoration: none;
}

.footer-brand {
  color: #ffc639;
  font-family: 'Epilogue', 'PingFang SC', sans-serif;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: .22em;
}

.footer-home-link {
  text-decoration: none;
  transition: color .24s ease, opacity .24s ease;
}

.footer-home-link:hover {
  color: #ffd566;
}

@media (max-width: 768px) {
  .register-main {
    min-height: auto;
    padding: 28px 14px 42px;
  }

  .register-card {
    padding: 28px 18px 22px;
    border-radius: 24px;
  }

  .register-header h1 {
    font-size: 34px;
  }

  .register-header p {
    font-size: 15px;
  }

  .register-grid {
    grid-template-columns: 1fr;
  }

  .field-span-2 {
    grid-column: auto;
  }
}
</style>
