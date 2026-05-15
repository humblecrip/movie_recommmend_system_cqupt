<template>
  <div class="center-shell">
    <main class="center-main">
      <div class="hero-backdrop"></div>
      <div class="hero-glow hero-glow-left"></div>
      <div class="hero-glow hero-glow-right"></div>

      <div class="center-content fade-slide-up">
        <header class="page-header">
          <h2>{{ activeSection === 'password' ? '账户安全' : '个人中心' }}</h2>
          <p>{{ activeSection === 'password' ? '管理账号凭证，保护你的个人信息安全。' : '编辑个人资料，完善你的管理身份。' }}</p>
        </header>

        <section class="panel-card">
          <template v-if="activeSection === 'profile'">
            <div class="profile-identity">
              <div class="avatar-stack">
                <div class="avatar-ring">
                  <img :src="profileAvatar" alt="头像">
                </div>
                <div class="avatar-upload">
                  <file-upload
                    tip=""
                    action="file/upload"
                    :limit="1"
                    :multiple="false"
                    :replaceable="true"
                    :fileUrls="avatarField ? avatarField : ''"
                    @change="onAvatarChange"
                  ></file-upload>
                </div>
              </div>
              <div class="identity-copy">
                <div class="identity-kicker">{{ flag === 'users' ? '管理员' : '个人身份' }}</div>
                <h3>{{ profileName }}</h3>
                <p>更新头像与个人展示信息。</p>
              </div>
            </div>

            <el-form ref="ruleForm" class="profile-form" :model="ruleForm" label-position="top">
              <div class="form-grid" v-if="flag === 'users'">
                <el-form-item class="field-block" label="用户名" prop="username">
                  <el-input v-model="ruleForm.username" placeholder="用户名"></el-input>
                </el-form-item>
              </div>

              <div class="form-grid" v-if="flag === 'yonghu'">
                <el-form-item class="field-block field-readonly" label="用户账号" prop="yonghuzhanghao">
                  <el-input v-model="ruleForm.yonghuzhanghao" placeholder="用户账号" readonly></el-input>
                </el-form-item>
                <el-form-item class="field-block" label="用户姓名" prop="yonghuxingming">
                  <el-input v-model="ruleForm.yonghuxingming" placeholder="用户姓名"></el-input>
                </el-form-item>
                <el-form-item class="field-block" label="性别" prop="xingbie">
                  <el-select v-model="ruleForm.xingbie" placeholder="请选择性别">
                    <el-option v-for="(item, index) in yonghuxingbieOptions" :key="index" :label="item" :value="item"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item class="field-block" label="联系电话" prop="lianxidianhua">
                  <el-input v-model="ruleForm.lianxidianhua" placeholder="联系电话"></el-input>
                </el-form-item>
                <el-form-item class="field-block field-span-2" label="身份证号" prop="shenfenzheng">
                  <el-input v-model="ruleForm.shenfenzheng" placeholder="身份证"></el-input>
                </el-form-item>
              </div>

              <div class="action-row">
                <button class="secondary-btn" type="button" @click="activeSection = 'password'; fetchCaptcha()">修改密码</button>
                <button class="primary-btn" type="button" @click="onUpdateHandler">保存修改</button>
              </div>
            </el-form>
          </template>

          <template v-if="activeSection === 'password'">
            <div class="password-header">
              <div class="identity-kicker">账户安全</div>
              <h3>修改密码</h3>
              <p>使用当前密码设置新的安全密码。</p>
            </div>

            <el-form ref="passwordForm" class="profile-form password-form" :model="passwordForm" :rules="passwordRules" label-position="top">
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
                    <span class="captcha-code">{{ captchaCode || '----' }}</span>
                    <button class="secondary-btn captcha-refresh" type="button" @click="fetchCaptcha">刷新验证码</button>
                  </div>
                </div>
              </div>

              <div class="action-row">
                <button class="secondary-btn" type="button" @click="activeSection = 'profile'">返回资料</button>
                <button class="primary-btn" type="button" @click="updatePassword">修改密码</button>
              </div>
            </el-form>
          </template>
        </section>
      </div>
    </main>
  </div>
</template>

<script>
import { isMobile, checkIdCard } from "@/utils/validate";

export default {
  data() {
    return {
      activeSection: 'profile',
      ruleForm: {},
      flag: '',
      yonghuxingbieOptions: [],
      passwordForm: { password: '', newpassword: '', repassword: '', captcha: '' },
      passwordRules: {
        password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
        newpassword: [{ required: true, message: '新密码不能为空', trigger: 'blur' }],
        repassword: [{ required: true, message: '确认密码不能为空', trigger: 'blur' }],
        captcha: [{ required: true, message: '验证码不能为空', trigger: 'blur' }],
      },
      captchaCode: '',
    };
  },
  computed: {
    todayDate() {
      return new Date().toISOString().slice(0, 10);
    },
    avatarField() {
      return this.flag === 'users' ? this.ruleForm.image : this.ruleForm.touxiang;
    },
    profileAvatar() {
      const raw = this.avatarField;
      if (!raw) return '';
      if (raw.startsWith('http')) return raw.split(',')[0];
      return this.$base.url + raw.split(',')[0];
    },
    profileName() {
      if (this.flag === 'users') return this.ruleForm.username || '管理员';
      return this.ruleForm.yonghuxingming || this.ruleForm.yonghuzhanghao || '用户';
    },
  },
  mounted() {
    this.flag = this.$storage.get("sessionTable");
    this.$http({
      url: `${this.flag}/session`,
      method: "get"
    }).then(({ data }) => {
      if (data && data.code === 0) {
        this.ruleForm = data.data;
      } else {
        this.$message.error(data.msg);
      }
    });
    this.yonghuxingbieOptions = "男,女".split(',');
  },
  methods: {
    encryptPasswordValue(value) {
      return value ? this.encryptAes(value) : value;
    },
    onAvatarChange(fileUrls) {
      if (this.flag === 'users') {
        this.ruleForm.image = fileUrls;
      } else {
        this.ruleForm.touxiang = fileUrls;
      }
    },
    onUpdateHandler() {
      if (this.flag === 'yonghu') {
        if (!this.ruleForm.yonghuzhanghao) { this.$message.error('用户账号不能为空'); return; }
        if (!this.ruleForm.yonghuxingming) { this.$message.error('用户姓名不能为空'); return; }
        if (this.ruleForm.lianxidianhua && !isMobile(this.ruleForm.lianxidianhua)) { this.$message.error('联系电话应输入手机格式'); return; }
        if (this.ruleForm.shenfenzheng && !checkIdCard(this.ruleForm.shenfenzheng)) { this.$message.error('身份证应输入身份证格式'); return; }
        if (this.ruleForm.touxiang != null) {
          this.ruleForm.touxiang = this.ruleForm.touxiang.replace(new RegExp(this.$base.url, "g"), "");
        }
      }
      if (this.flag === 'users') {
        if (!this.ruleForm.username || !this.ruleForm.username.trim()) { this.$message.error('用户名不能为空'); return; }
        if (this.ruleForm.image) {
          this.ruleForm.image = this.ruleForm.image.replace(new RegExp(this.$base.url, "g"), "");
        }
      }
      this.$http({
        url: `${this.flag}/update`,
        method: "post",
        data: this.ruleForm
      }).then(({ data }) => {
        if (data && data.code === 0) {
          if (this.flag === 'users') {
            this.$storage.set('headportrait', this.ruleForm.image);
          }
          this.$message({ message: "修改信息成功", type: "success", duration: 1500, onClose: () => { window.location.reload(); } });
        } else {
          this.$message.error(data.msg);
        }
      });
    },
    fetchCaptcha() {
      this.$http({ url: `${this.flag}/changePasswordCaptcha`, method: "get" }).then(({ data }) => {
        if (data && data.code === 0 && data.data) {
          this.captchaCode = data.data.captcha || data.data.code || '';
        } else {
          this.captchaCode = '';
        }
      }).catch(() => { this.captchaCode = ''; });
    },
    updatePassword() {
      this.$refs.passwordForm.validate(valid => {
        if (!valid) return;
        if (this.passwordForm.newpassword !== this.passwordForm.repassword) { this.$message.error('两次密码输入不一致'); return; }
        if (this.passwordForm.newpassword === this.passwordForm.password) { this.$message.error('新密码与原密码相同！'); return; }
        this.$http({
          url: `${this.flag}/changePassword`,
          method: "post",
          data: {
            oldPassword: this.encryptPasswordValue(this.passwordForm.password),
            newPassword: this.encryptPasswordValue(this.passwordForm.newpassword),
            captcha: this.passwordForm.captcha,
          }
        }).then(({ data }) => {
          if (data && data.code === 0) {
            this.$message({ message: '修改密码成功', type: 'success', duration: 1500 });
            this.passwordForm = { password: '', newpassword: '', repassword: '', captcha: '' };
            this.fetchCaptcha();
          } else {
            this.$message.error(data.msg);
          }
        });
      });
    },
  }
};
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.center-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr;
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
.delay-1 { animation-delay: .08s; }

@keyframes fadeSlideUp {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}


.center-main {
  position: relative;
  padding: 48px 56px;
  overflow-y: auto;
  max-height: 100vh;
}

.hero-backdrop {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 50% 0%, rgba(47, 108, 255, 0.08), transparent 60%);
  pointer-events: none;
}
.hero-glow {
  position: absolute;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}
.hero-glow-left { top: -60px; left: -40px; background: rgba(47, 200, 255, 0.06); }
.hero-glow-right { bottom: -80px; right: -60px; background: rgba(255, 198, 57, 0.04); }

.center-content { position: relative; z-index: 1; }

.page-header {
  margin-bottom: 36px;
  h2 { font-size: 28px; font-weight: 800; color: #f6f7fb; margin: 0 0 8px; }
  p { color: rgba(188, 199, 222, 0.62); font-size: 14px; margin: 0; }
}

.panel-card {
  background: rgba(12, 20, 40, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  padding: 40px;
  backdrop-filter: blur(12px);
}

.profile-identity {
  display: flex;
  align-items: center;
  gap: 28px;
  margin-bottom: 36px;
  padding-bottom: 28px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.avatar-stack { position: relative; }
.avatar-ring {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  border: 3px solid rgba(47, 200, 255, 0.4);
  overflow: hidden;
  background: rgba(47, 200, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  img { width: 100%; height: 100%; object-fit: cover; }
}
.avatar-upload {
  position: absolute;
  bottom: -4px;
  right: -4px;
  ::v-deep .el-upload--picture-card {
    width: 32px;
    height: 32px;
    line-height: 32px;
    border-radius: 50%;
    background: #2fc8ff;
    border: 2px solid #0b1326;
    i { font-size: 14px; color: #04111a; }
  }
  ::v-deep .el-upload-list { display: none; }
}

.identity-copy {
  .identity-kicker { color: #2fc8ff; font-size: 11px; font-weight: 700; letter-spacing: .2em; text-transform: uppercase; margin-bottom: 6px; }
  h3 { font-size: 22px; font-weight: 700; color: #f6f7fb; margin: 0 0 4px; }
  p { color: rgba(188, 199, 222, 0.55); font-size: 13px; margin: 0; }
}

.profile-form {
  ::v-deep .el-form-item__label { color: rgba(188, 199, 222, 0.72); font-size: 12px; font-weight: 600; letter-spacing: .06em; padding-bottom: 6px; }
  ::v-deep .el-input__inner, ::v-deep .el-select .el-input__inner {
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    color: #dae2fd;
    height: 44px;
    &:focus { border-color: rgba(47, 200, 255, 0.5); }
  }
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px 28px;
}
.field-block { min-width: 0; }
.field-span-2 { grid-column: span 2; }
.field-readonly ::v-deep .el-input__inner { opacity: 0.5; cursor: not-allowed; }

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.primary-btn {
  border: 0;
  padding: 12px 32px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2fc8ff, #2f6dff);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: transform .2s ease, box-shadow .2s ease;
  &:hover { transform: translateY(-1px); box-shadow: 0 8px 24px rgba(47, 200, 255, 0.3); }
}

.secondary-btn {
  border: 1px solid rgba(255, 255, 255, 0.12);
  padding: 12px 24px;
  border-radius: 12px;
  background: transparent;
  color: rgba(188, 199, 222, 0.76);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: border-color .2s ease, color .2s ease;
  &:hover { border-color: rgba(47, 200, 255, 0.4); color: #2fc8ff; }
}

.password-header {
  display: flex;
  align-items: center;
  gap: 28px;
  margin-bottom: 30px;
  .identity-kicker { color: #2fc8ff; font-size: 11px; font-weight: 700; letter-spacing: .2em; text-transform: uppercase; margin-bottom: 6px; }
  h3 { font-size: 22px; font-weight: 700; color: #f6f7fb; margin: 0 0 4px; }
  p { color: rgba(188, 199, 222, 0.55); font-size: 13px; margin: 0; }
}

.password-grid { grid-template-columns: 1fr 1fr; align-items: start; }

.captcha-panel {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 10px;
}
.captcha-label { font-size: 12px; font-weight: 600; color: rgba(188, 199, 222, 0.72); }
.captcha-row { display: flex; align-items: center; gap: 12px; min-height: 44px; }
.captcha-code {
  min-width: 80px;
  padding: 8px 16px;
  border-radius: 12px;
  background: rgba(10, 18, 36, 0.72);
  border: 1px solid rgba(120, 160, 255, 0.22);
  color: #8eb7ff;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 4px;
  text-align: center;
}
.captcha-refresh { height: 40px; padding: 0 16px; font-size: 12px; min-width: 110px; }

@media (max-width: 900px) {
  .center-main { padding: 24px 20px; }
  .form-grid { grid-template-columns: 1fr; }
  .field-span-2 { grid-column: span 1; }
}
</style>
