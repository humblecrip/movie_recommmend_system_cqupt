<template>
	<div class="admin-login-page">
		<header class="login-header">
			<div class="brand-mark">CineAdmin</div>
		</header>

		<main class="login-main">
			<div class="atmosphere-layer layer-dark"></div>
			<div class="atmosphere-layer layer-gold"></div>
			<div class="glow-orb orb-left"></div>
			<div class="glow-orb orb-right"></div>

			<section class="login-card">
				<div class="card-top">
					<div class="brand-badge">
						<i class="el-icon-film"></i>
					</div>
					<h1>Aether Cinema</h1>
					<p>输入管理员账号与密码，进入电影管理后台。</p>
				</div>

				<form class="admin-login-form" @submit.prevent="login">
					<div class="field-block">
						<label for="admin-username">账号 (Username)</label>
						<div class="field-shell">
							<i class="field-icon el-icon-user"></i>
							<el-input
								id="admin-username"
								v-model="rulesForm.username"
								placeholder="请输入管理员账号"
								clearable
								@keyup.enter.native="login"
							></el-input>
						</div>
					</div>

					<div class="field-block">
						<label for="admin-password">密码 (Password)</label>
						<div class="field-shell">
							<i class="field-icon el-icon-lock"></i>
							<el-input
								id="admin-password"
								v-model="rulesForm.password"
								:type="showPassword ? 'text' : 'password'"
								placeholder="请输入密码"
								@keyup.enter.native="login"
							></el-input>
							<button
								type="button"
								class="password-toggle"
								@click="showPassword = !showPassword"
							>
								<i class="el-icon-view"></i>
							</button>
						</div>
					</div>

					<div class="login-tools">
						<span class="role-tag">{{ adminEntry.roleName }}</span>
						<button type="button" class="forgot-link" @click="forgotDialogVisible = true">
							忘记密码？
						</button>
					</div>

					<el-button
						class="login-button"
						type="primary"
						native-type="submit"
						:loading="submitting"
						:disabled="submitting"
					>
						<span>登录 (Login)</span>
						<i class="el-icon-right"></i>
					</el-button>
				</form>

				<div class="support-block">
					<p>如需技术支持，请联系系统超级管理员处理账号与权限问题。</p>
				</div>
			</section>
		</main>

		<footer class="login-footer">
			<div>© 2024 Digital Curator Cinema Systems. All rights reserved.</div>
			<div class="footer-status">
				<i class="el-icon-lock"></i>
				<span>Security Protocol</span>
			</div>
		</footer>

		<el-dialog
			custom-class="password-tip-dialog"
			:visible.sync="forgotDialogVisible"
			width="420px"
			append-to-body
			:show-close="false"
		>
			<div class="dialog-body">
				<div class="dialog-icon">
					<i class="el-icon-warning-outline"></i>
				</div>
				<h3>忘记密码</h3>
				<p>请联系超级管理员重置当前后台账号密码。</p>
				<el-button class="dialog-button" type="primary" @click="forgotDialogVisible = false">
					我知道了
				</el-button>
			</div>
		</el-dialog>
	</div>
</template>

<script>
import menu from '@/utils/menu'

export default {
	data() {
		return {
			baseUrl: this.$base.url,
			showPassword: false,
			submitting: false,
			forgotDialogVisible: false,
			adminEntry: {
				roleName: '管理员',
				tableName: 'users',
			},
			rulesForm: {
				username: '',
				password: '',
				role: '',
			},
			tableName: 'users',
		}
	},
	created() {
		const adminMenu = menu.list().find(item => item.hasBackLogin === '是')
		if (adminMenu) {
			this.adminEntry = {
				roleName: adminMenu.roleName || '管理员',
				tableName: adminMenu.tableName || 'users',
			}
			this.tableName = this.adminEntry.tableName
			this.rulesForm.role = this.adminEntry.roleName
		}
	},
	methods: {
		encryptPasswordValue(value) {
			return value ? this.encryptAes(value) : value
		},
		normalizeCredentials() {
			this.rulesForm.username = (this.rulesForm.username || '').trim()
			this.rulesForm.password = (this.rulesForm.password || '').trim()
		},
		clearSessionCache() {
			['Token', 'role', 'sessionTable', 'adminName', 'headportrait', 'userForm', 'userid'].forEach(key => {
				this.$storage.remove(key)
			})
		},
		resolveHeadportrait(userInfo) {
			if (!userInfo) {
				return ''
			}
			if (this.tableName === 'yonghu') {
				return userInfo.touxiang || ''
			}
			if (this.tableName === 'users') {
				return userInfo.image || ''
			}
			return userInfo.image || userInfo.touxiang || ''
		},
		async login() {
			if (this.submitting) {
				return
			}
			this.normalizeCredentials()
			if (!this.rulesForm.username) {
				this.$message.error('请输入管理员账号')
				return
			}
			if (!this.rulesForm.password) {
				this.$message.error('请输入密码')
				return
			}

			this.tableName = this.adminEntry.tableName
			this.rulesForm.role = this.adminEntry.roleName
			this.submitting = true

			try {
				await this.loginPost()
			} finally {
				this.submitting = false
			}
		},
		async loginPost() {
			try {
				const encryptedPassword = this.encryptPasswordValue(this.rulesForm.password)
				const loginResponse = await this.$http({
					url: `${this.tableName}/login?username=${encodeURIComponent(this.rulesForm.username)}&password=${encodeURIComponent(encryptedPassword)}`,
					method: 'post'
				})
				const loginData = loginResponse.data

				if (!(loginData && loginData.code === 0)) {
					this.$message.error((loginData && loginData.msg) || '登录失败，请检查账号密码')
					return
				}

				this.$storage.set('Token', loginData.token)
				this.$storage.set('role', this.rulesForm.role)
				this.$storage.set('sessionTable', this.tableName)
				this.$storage.set('adminName', this.rulesForm.username)

				const sessionResponse = await this.$http({
					url: `${this.tableName}/session`,
					method: 'get'
				})
				const sessionData = sessionResponse.data

				if (!(sessionData && sessionData.code === 0 && sessionData.data)) {
					this.clearSessionCache()
					this.$message.error((sessionData && sessionData.msg) || '获取管理员信息失败，请重新登录')
					return
				}

				this.$storage.set('headportrait', this.resolveHeadportrait(sessionData.data))
				this.$storage.set('userForm', JSON.stringify(sessionData.data))
				this.$storage.set('userid', sessionData.data.id || '')
				this.$router.replace({ path: '/' })
			} catch (error) {
				this.clearSessionCache()
				this.$message.error('登录请求失败，请稍后重试')
			}
		},
	}
}
</script>

<style lang="scss" scoped>
@import url('https://fonts.googleapis.com/css2?family=Epilogue:wght@400;700;900&family=Manrope:wght@400;500;600;700&display=swap');

.admin-login-page {
	position: relative;
	min-height: 100vh;
	background:
		radial-gradient(circle at 20% 30%, rgba(255, 198, 57, 0.05), transparent 40%),
		radial-gradient(circle at 80% 70%, rgba(66, 76, 95, 0.14), transparent 40%),
		linear-gradient(to bottom, rgba(11, 19, 38, 0.38), rgba(11, 19, 38, 0.92)),
		url('https://lh3.googleusercontent.com/aida-public/AB6AXuCRJa8cf5gRDepQzEzUet0cPvPV8eJJHnq0pbdWeV64ONn3amnQ7lH4bv2b-i5OG080JDZUYblJkQOnLcLNKeF4yHMX-QzPPU-fhAR49dFkqOG_dE2g4BXMh4ovO9b4Wd5fsUAjpzF672rV1CLjuDqyHKXrY6YduuPoMX2xayDtPekT3eVn7dyQIcQs2tsLPaOQrrhlMBQd60RMCvh_hAN6f43ifemK-MfCsfFYLfnkzOJwuOM2AoeyTrpU_F2YS6eG3LC8e9-ThVk');
	background-size: cover;
	background-position: center;
	color: #dae2fd;
	font-family: 'Manrope', sans-serif;
	overflow: hidden;
}

.login-header,
.login-footer {
	position: fixed;
	left: 0;
	z-index: 3;
	width: 100%;
	padding: 28px 32px;
	display: flex;
	align-items: center;
}

.login-header {
	top: 0;
	justify-content: center;
}

.brand-mark {
	color: #ffc639;
	font-family: 'Epilogue', sans-serif;
	font-size: 32px;
	font-weight: 900;
	letter-spacing: -0.04em;
	text-shadow: 0 10px 24px rgba(0, 0, 0, 0.35);
}

.login-main {
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 100vh;
	padding: 120px 16px 108px;
}

.atmosphere-layer,
.glow-orb {
	position: absolute;
	pointer-events: none;
}

.layer-dark {
	inset: 0;
	background: rgba(0, 0, 0, 0.24);
}

.layer-gold {
	top: -25%;
	left: -10%;
	width: 120%;
	height: 150%;
	background: radial-gradient(circle, rgba(255, 198, 57, 0.08) 0%, transparent 52%);
	filter: blur(36px);
	animation: lightLeak 15s ease-in-out infinite;
}

.glow-orb {
	width: 420px;
	height: 420px;
	border-radius: 999px;
	filter: blur(140px);
}

.orb-left {
	left: -120px;
	top: 22%;
	background: rgba(255, 198, 57, 0.15);
}

.orb-right {
	right: -120px;
	bottom: 16%;
	background: rgba(188, 199, 222, 0.12);
}

.login-card {
	position: relative;
	z-index: 2;
	width: 100%;
	max-width: 500px;
	padding: 40px;
	border-radius: 40px;
	background: rgba(11, 19, 38, 0.48);
	backdrop-filter: blur(40px);
	border: 1px solid rgba(255, 198, 57, 0.14);
	box-shadow:
		0 10px 15px -3px rgba(0, 0, 0, 0.5),
		0 4px 6px -2px rgba(0, 0, 0, 0.3),
		0 0 0 1px rgba(255, 255, 255, 0.04),
		inset 0 0 20px rgba(255, 255, 255, 0.02);
}

.card-top {
	text-align: center;
	margin-bottom: 34px;
}

.brand-badge {
	width: 84px;
	height: 84px;
	margin: 0 auto 22px;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 26px;
	background: linear-gradient(135deg, rgba(255, 198, 57, 0.2), rgba(255, 198, 57, 0.02));
	border: 1px solid rgba(255, 198, 57, 0.18);
	color: #ffc639;
	font-size: 38px;
}

.card-top h1 {
	margin: 0 0 10px;
	color: #fff;
	font-family: 'Epilogue', sans-serif;
	font-size: 38px;
	font-weight: 700;
	letter-spacing: -0.04em;
}

.card-top p {
	margin: 0;
	color: rgba(208, 197, 175, 0.88);
	font-size: 14px;
	font-weight: 500;
}

.admin-login-form {
	display: flex;
	flex-direction: column;
	gap: 22px;
}

.field-block label {
	display: block;
	margin: 0 0 10px 4px;
	color: rgba(208, 197, 175, 0.92);
	font-size: 11px;
	font-weight: 700;
	letter-spacing: 0.15em;
	text-transform: uppercase;
}

.field-shell {
	position: relative;
}

.field-icon {
	position: absolute;
	top: 50%;
	left: 16px;
	z-index: 2;
	transform: translateY(-50%);
	color: rgba(208, 197, 175, 0.6);
	font-size: 20px;
}

.field-shell ::v-deep .el-input {
	width: 100%;
}

.field-shell ::v-deep .el-input__inner {
	height: 58px;
	padding: 0 48px 0 48px;
	border-radius: 20px;
	border: 1px solid rgba(77, 70, 53, 0.48);
	background: rgba(45, 52, 73, 0.34);
	color: #fff;
	font-size: 14px;
	backdrop-filter: blur(10px);
	transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field-shell ::v-deep .el-input__inner::placeholder {
	color: rgba(208, 197, 175, 0.38);
}

.field-shell ::v-deep .el-input__inner:focus {
	border-color: rgba(255, 198, 57, 0.5);
	box-shadow: 0 0 0 3px rgba(255, 198, 57, 0.08);
}

.field-shell ::v-deep .el-input__suffix {
	right: 12px;
}

.password-toggle {
	position: absolute;
	top: 50%;
	right: 14px;
	z-index: 3;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 30px;
	height: 30px;
	padding: 0;
	border: none;
	background: transparent;
	color: rgba(208, 197, 175, 0.62);
	transform: translateY(-50%);
	cursor: pointer;
}

.password-toggle:hover {
	color: #ffffff;
}

.login-tools {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding-top: 2px;
}

.role-tag {
	display: inline-flex;
	align-items: center;
	padding: 8px 12px;
	border-radius: 999px;
	background: rgba(255, 198, 57, 0.1);
	color: #f7be2c;
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.08em;
}

.forgot-link {
	padding: 0;
	border: none;
	background: transparent;
	color: #ffc639;
	font-size: 12px;
	font-weight: 700;
	cursor: pointer;
}

.forgot-link:hover {
	color: #ffdb7a;
}

.login-button {
	width: 100%;
	height: 58px;
	margin-top: 4px;
	border: none;
	border-radius: 999px;
	background: linear-gradient(135deg, #ffc639, #e1aa12) !important;
	color: #3f2e00 !important;
	font-size: 16px;
	font-weight: 800;
	letter-spacing: 0.02em;
	box-shadow: 0 0 20px -5px rgba(255, 198, 57, 0.4);
	transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.login-button:hover,
.login-button:focus {
	transform: scale(1.02);
	box-shadow: 0 0 40px 5px rgba(255, 198, 57, 0.3);
}

.login-button i {
	margin-left: 8px;
	font-size: 16px;
}

.support-block {
	margin-top: 26px;
	text-align: center;
}

.support-block p {
	margin: 0;
	color: rgba(208, 197, 175, 0.72);
	font-size: 12px;
	line-height: 1.8;
}

.login-footer {
	bottom: 0;
	justify-content: space-between;
	gap: 16px;
	color: rgba(255, 255, 255, 0.3);
	font-size: 10px;
	font-weight: 700;
	letter-spacing: 0.18em;
	text-transform: uppercase;
}

.footer-status {
	display: inline-flex;
	align-items: center;
	gap: 8px;
	color: rgba(255, 198, 57, 0.55);
}

.dialog-body {
	padding: 18px 10px 4px;
	text-align: center;
}

.dialog-icon {
	width: 64px;
	height: 64px;
	margin: 0 auto 18px;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 20px;
	background: rgba(255, 198, 57, 0.12);
	color: #ffc639;
	font-size: 28px;
}

.dialog-body h3 {
	margin: 0 0 12px;
	color: #fff;
	font-family: 'Epilogue', sans-serif;
	font-size: 24px;
}

.dialog-body p {
	margin: 0;
	color: rgba(208, 197, 175, 0.85);
	font-size: 14px;
	line-height: 1.8;
}

.dialog-button {
	min-width: 136px;
	height: 46px;
	margin-top: 24px;
	border: none;
	border-radius: 999px;
	background: linear-gradient(135deg, #ffc639, #e1aa12) !important;
	color: #3f2e00 !important;
	font-weight: 800;
}

.admin-login-page ::v-deep .password-tip-dialog {
	border-radius: 28px;
	overflow: hidden;
	background: rgba(11, 19, 38, 0.92);
	border: 1px solid rgba(255, 198, 57, 0.12);
	box-shadow: 0 20px 50px rgba(0, 0, 0, 0.45);
}

.admin-login-page ::v-deep .password-tip-dialog .el-dialog__header {
	padding: 0;
}

.admin-login-page ::v-deep .password-tip-dialog .el-dialog__body {
	padding: 0 28px 28px;
}

.admin-login-page ::v-deep .el-dialog__wrapper {
	background: rgba(3, 8, 18, 0.64);
	backdrop-filter: blur(8px);
}

@keyframes lightLeak {
	0%,
	100% {
		opacity: 0.3;
		transform: translate(-10%, -10%) scale(1);
	}
	50% {
		opacity: 0.6;
		transform: translate(10%, 10%) scale(1.1);
	}
}

@media (max-width: 768px) {
	.login-header,
	.login-footer {
		padding-left: 18px;
		padding-right: 18px;
	}

	.login-card {
		padding: 28px 22px;
		border-radius: 28px;
	}

	.card-top h1 {
		font-size: 30px;
	}

	.login-footer {
		flex-direction: column;
		align-items: center;
		justify-content: center;
		text-align: center;
		gap: 8px;
		letter-spacing: 0.12em;
	}
}
</style>
