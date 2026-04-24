<template>
	<div class="addEdit-block account-edit-page">
		<div class="form-shell">
			<el-form
				ref="ruleForm"
				class="add-update-preview"
				:model="ruleForm"
				:rules="rules"
				label-width="0"
			>
				<div class="form-card">
					<div class="ambient-orb orb-left"></div>
					<div class="ambient-orb orb-right"></div>

					<header class="hero-panel">
						<div class="hero-copy">
							<div class="mode-tag">{{ modeTag }}</div>
							<h2>{{ headerTitle }}</h2>
							<p>{{ headerDescription }}</p>
						</div>
						<div class="hero-meta">
							<div>
								<strong>{{ ruleForm.username || '未填写用户名' }}</strong>
								<span>账号标识</span>
							</div>
							<div>
								<strong>{{ ruleForm.role || '后台账号' }}</strong>
								<span>账号类型</span>
							</div>
						</div>
					</header>

					<section class="summary-panel">
						<div class="summary-badge">
							<div class="badge-circle">{{ getNameFallback() }}</div>
							<div class="badge-copy">
								<div class="panel-kicker">Account Summary</div>
								<h3>维护后台登录账号</h3>
								<p>保留现有保存与回填逻辑，仅重构表单布局与视觉层次。</p>
							</div>
						</div>
					</section>

					<section class="detail-panel">
						<div class="section-head">
							<div>
								<div class="panel-kicker">Credentials</div>
								<h3>编辑账号信息</h3>
							</div>
							<p>继续复用原有 `users/save` 与 `users/update` 接口，不新增后端字段。</p>
						</div>

						<div class="field-grid single-column">
							<el-form-item class="field-card" prop="username">
								<div class="field-label">用户名</div>
								<div class="field-control">
									<i class="field-icon el-icon-user"></i>
									<el-input
										v-if="type != 'info'"
										v-model="ruleForm.username"
										placeholder="请输入用户名"
										clearable
										:readonly="ro.username"
									></el-input>
									<div v-else class="field-display">{{ ruleForm.username || '未填写' }}</div>
								</div>
							</el-form-item>

							<el-form-item class="field-card" prop="password">
								<div class="field-label">密码</div>
								<div class="field-control password-control">
									<i class="field-icon el-icon-lock"></i>
									<el-input
										v-if="type != 'info'"
										v-model="ruleForm.password"
										:type="passwordVisible ? 'text' : 'password'"
										placeholder="请输入密码"
										clearable
										:readonly="ro.password"
									></el-input>
									<div v-else class="field-display">{{ ruleForm.password || '未填写' }}</div>
									<button
										v-if="type != 'info'"
										type="button"
										class="visibility-trigger"
										@click="passwordVisible = !passwordVisible"
									>
										<i class="el-icon-view"></i>
									</button>
								</div>
							</el-form-item>
						</div>
					</section>

					<el-form-item class="btn action-row">
						<el-button v-if="type != 'info'" class="secondary-btn" @click="back()">取消</el-button>
						<el-button v-if="type != 'info'" class="primary-btn" type="success" @click="onSubmit">确定</el-button>
						<el-button v-if="type == 'info'" class="primary-btn" type="success" @click="back()">返回</el-button>
					</el-form-item>
				</div>
			</el-form>
		</div>
	</div>
</template>

<script>
export default {
	data() {
		return {
			id: '',
			type: '',
			passwordVisible: false,
			ro: {
				username: false,
				password: false,
				role: false
			},
			ruleForm: {
				username: '',
				password: '',
				role: ''
			},
			rules: {
				username: [
					{ required: true, message: '用户名不能为空', trigger: 'blur' }
				],
				password: [
					{ required: true, message: '密码不能为空', trigger: 'blur' }
				],
				role: []
			}
		}
	},
	props: ['parent'],
	computed: {
		modeTag() {
			if (this.type === 'info') {
				return 'DETAIL MODE'
			}
			return this.ruleForm.id ? 'EDIT MODE' : 'CREATE MODE'
		},
		headerTitle() {
			if (this.type === 'info') {
				return '后台账号详情'
			}
			return this.ruleForm.id ? 'Edit Admin Account' : 'Create Admin Account'
		},
		headerDescription() {
			if (this.type === 'info') {
				return '查看当前后台账号的用户名与密码信息，保留原有返回逻辑。'
			}
			return '统一为深色玻璃卡片布局，同时继续复用已有账号新增与编辑接口。'
		}
	},
	methods: {
		download(file) {
			window.open(`${file}`)
		},
		getNameFallback() {
			if (this.ruleForm.username) {
				return this.ruleForm.username.substring(0, 1).toUpperCase()
			}
			return 'A'
		},
		init(id, type) {
			this.passwordVisible = false
			if (id) {
				this.id = id
				this.type = type
			} else {
				this.id = ''
				this.type = type || ''
				this.ruleForm = {
					username: '',
					password: '',
					role: ''
				}
			}
			if (this.type == 'info' || this.type == 'else' || this.type == 'msg') {
				this.info(id)
			} else if (this.type == 'logistics') {
				for (let x in this.ro) {
					this.ro[x] = true
				}
				this.logistics = false
				this.info(id)
			} else if (this.type == 'cross') {
				var obj = this.$storage.getObj('crossObj')
				for (var o in obj) {
					if (o == 'username') {
						this.ruleForm.username = obj[o]
						this.ro.username = true
						continue
					}
					if (o == 'password') {
						this.ruleForm.password = obj[o]
						this.ro.password = true
						continue
					}
					if (o == 'role') {
						this.ruleForm.role = obj[o]
						this.ro.role = true
					}
				}
			}
		},
		info(id) {
			this.$http({
				url: `users/info/${id}`,
				method: 'get'
			}).then(({ data }) => {
				if (data && data.code === 0) {
					this.ruleForm = data.data
				} else {
					this.$message.error(data.msg)
				}
			})
		},
		async onSubmit() {
			if (!this.ruleForm.id) {
				delete this.ruleForm.userid
			}
			await this.$refs['ruleForm'].validate(async valid => {
				if (valid) {
					if (this.type == 'cross') {
						var statusColumnName = this.$storage.get('statusColumnName')
						var statusColumnValue = this.$storage.get('statusColumnValue')
						if (statusColumnName != '') {
							var obj = this.$storage.getObj('crossObj')
							if (statusColumnName && !statusColumnName.startsWith('[')) {
								for (var o in obj) {
									if (o == statusColumnName) {
										obj[o] = statusColumnValue
									}
								}
								var table = this.$storage.get('crossTable')
								await this.$http({
									url: `${table}/update`,
									method: 'post',
									data: obj
								}).then(() => {})
							}
						}
					}

					await this.$http({
						url: `users/${!this.ruleForm.id ? 'save' : 'update'}`,
						method: 'post',
						data: this.ruleForm
					}).then(async ({ data }) => {
						if (data && data.code === 0) {
							this.$message({
								message: '操作成功',
								type: 'success',
								duration: 1500,
								onClose: () => {
									this.parent.showFlag = true
									this.parent.addOrUpdateFlag = false
									this.parent.usersCrossAddOrUpdateFlag = false
									this.parent.search()
									this.parent.contentStyleChange()
								}
							})
						} else {
							this.$message.error(data.msg)
						}
					})
				}
			})
		},
		getUUID() {
			return new Date().getTime()
		},
		back() {
			this.parent.showFlag = true
			this.parent.addOrUpdateFlag = false
			this.parent.usersCrossAddOrUpdateFlag = false
			this.parent.contentStyleChange()
		}
	}
}
</script>

<style lang="scss" scoped>
.account-edit-page {
	padding: 0 0 30px;
}

.form-shell,
.add-update-preview {
	width: 100%;
}

.form-card {
	position: relative;
	overflow: hidden;
	padding: 34px;
	border-radius: 30px;
	background:
		radial-gradient(circle at top right, rgba(247, 190, 44, 0.18), transparent 34%),
		radial-gradient(circle at left bottom, rgba(87, 120, 255, 0.14), transparent 30%),
		linear-gradient(145deg, #11192d, #172239 54%, #0d1629);
	border: 1px solid rgba(255, 255, 255, 0.08);
	box-shadow: 0 26px 60px rgba(8, 12, 24, 0.28);
}

.ambient-orb {
	position: absolute;
	border-radius: 999px;
	filter: blur(18px);
	pointer-events: none;
}

.orb-left {
	left: -74px;
	top: 180px;
	width: 180px;
	height: 180px;
	background: rgba(92, 124, 250, 0.16);
}

.orb-right {
	right: -66px;
	top: -40px;
	width: 210px;
	height: 210px;
	background: rgba(247, 190, 44, 0.14);
}

.hero-panel,
.summary-panel,
.detail-panel,
.action-row {
	position: relative;
	z-index: 1;
}

.hero-panel {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 24px;
	margin-bottom: 28px;
}

.mode-tag,
.panel-kicker {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	padding: 6px 12px;
	border-radius: 999px;
	background: rgba(247, 190, 44, 0.12);
	color: #ffd76b;
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.16em;
	text-transform: uppercase;
}

.hero-copy h2 {
	margin: 16px 0 10px;
	color: #f5f7ff;
	font-size: 34px;
	line-height: 1.1;
	font-weight: 800;
	letter-spacing: -0.03em;
}

.hero-copy p {
	margin: 0;
	max-width: 720px;
	color: rgba(218, 226, 253, 0.72);
	font-size: 14px;
	line-height: 1.8;
}

.hero-meta {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 12px;
	min-width: 260px;
}

.hero-meta div {
	padding: 16px 18px;
	border-radius: 20px;
	background: rgba(8, 15, 29, 0.42);
	border: 1px solid rgba(255, 255, 255, 0.06);
}

.hero-meta strong {
	display: block;
	color: #ffffff;
	font-size: 16px;
	line-height: 1.6;
}

.hero-meta span {
	display: block;
	margin-top: 6px;
	color: rgba(208, 197, 175, 0.72);
	font-size: 12px;
	letter-spacing: 0.08em;
	text-transform: uppercase;
}

.summary-panel,
.detail-panel {
	padding: 28px;
	border-radius: 28px;
	background: rgba(45, 52, 73, 0.44);
	border: 1px solid rgba(255, 255, 255, 0.07);
	backdrop-filter: blur(22px);
}

.summary-panel {
	margin-bottom: 28px;
}

.summary-badge {
	display: flex;
	align-items: center;
	gap: 22px;
}

.badge-circle {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 108px;
	height: 108px;
	border-radius: 999px;
	background: linear-gradient(135deg, #ffcf55, #8ea7ff);
	color: #261a00;
	font-size: 40px;
	font-weight: 800;
	box-shadow: 0 18px 36px rgba(11, 19, 38, 0.32);
}

.badge-copy h3 {
	margin: 14px 0 10px;
	color: #f5f7ff;
	font-size: 28px;
	line-height: 1.2;
}

.badge-copy p {
	margin: 0;
	max-width: 620px;
	color: rgba(218, 226, 253, 0.7);
	font-size: 14px;
	line-height: 1.8;
}

.detail-panel {
	background: rgba(8, 15, 29, 0.4);
}

.section-head {
	display: flex;
	align-items: flex-end;
	justify-content: space-between;
	gap: 18px;
	margin-bottom: 26px;
}

.section-head h3 {
	margin: 14px 0 0;
	color: #f7f8fc;
	font-size: 28px;
	line-height: 1.2;
	letter-spacing: -0.03em;
}

.section-head p {
	margin: 0;
	max-width: 460px;
	color: rgba(218, 226, 253, 0.68);
	font-size: 13px;
	line-height: 1.8;
	text-align: right;
}

.field-grid {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 18px 20px;
}

.single-column {
	grid-template-columns: minmax(0, 1fr);
}

.add-update-preview ::v-deep .field-card {
	margin-bottom: 0;
}

.field-label {
	margin-bottom: 10px;
	padding-left: 4px;
	color: rgba(208, 197, 175, 0.88);
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.12em;
	text-transform: uppercase;
}

.field-control {
	position: relative;
}

.field-icon {
	position: absolute;
	left: 16px;
	top: 50%;
	z-index: 2;
	transform: translateY(-50%);
	color: rgba(247, 190, 44, 0.72);
	font-size: 18px;
}

.field-display,
.add-update-preview .el-input ::v-deep .el-input__inner {
	height: 54px;
	border-radius: 18px;
}

.field-display {
	display: flex;
	align-items: center;
	padding: 0 18px 0 48px;
	background: rgba(6, 14, 32, 0.82);
	border: 1px solid rgba(153, 144, 124, 0.28);
	color: #edf1ff;
	font-size: 14px;
}

.add-update-preview .el-input {
	width: 100%;
}

.add-update-preview .el-input ::v-deep .el-input__inner {
	border: 1px solid rgba(153, 144, 124, 0.28);
	background: rgba(6, 14, 32, 0.82);
	padding: 0 48px 0 48px;
	color: #edf1ff;
	font-size: 14px;
	transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.add-update-preview .el-input ::v-deep .el-input__inner::placeholder {
	color: rgba(218, 226, 253, 0.34);
}

.add-update-preview .el-input ::v-deep .el-input__inner:focus {
	border-color: rgba(247, 190, 44, 0.72);
	box-shadow: 0 0 0 4px rgba(247, 190, 44, 0.08);
}

.add-update-preview .el-input ::v-deep .el-input__inner[readonly='readonly'] {
	background: rgba(6, 14, 32, 0.72);
	border-color: rgba(153, 144, 124, 0.18);
	color: rgba(237, 241, 255, 0.72);
}

.visibility-trigger {
	position: absolute;
	right: 14px;
	top: 50%;
	z-index: 3;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 28px;
	height: 28px;
	padding: 0;
	border: none;
	background: transparent;
	color: rgba(218, 226, 253, 0.54);
	transform: translateY(-50%);
	cursor: pointer;
}

.visibility-trigger:hover {
	color: #ffcf55;
}

.add-update-preview ::v-deep .el-form-item__error {
	padding-top: 8px;
	color: #ffb4ab;
}

.add-update-preview ::v-deep .btn.action-row {
	display: flex;
	justify-content: flex-end;
	margin: 26px 0 0;
	padding-top: 26px;
	border-top: 1px solid rgba(153, 144, 124, 0.16);
}

.secondary-btn,
.primary-btn {
	min-width: 126px;
	height: 48px;
	padding: 0 28px;
	border: none;
	border-radius: 999px;
	font-size: 14px;
	font-weight: 700;
}

.secondary-btn {
	background: rgba(255, 255, 255, 0.06);
	color: rgba(218, 226, 253, 0.86);
}

.primary-btn {
	background: linear-gradient(135deg, #ffc639, #e1aa12);
	color: #261a00;
	box-shadow: 0 16px 28px rgba(247, 190, 44, 0.18);
}

@media (max-width: 1200px) {
	.hero-panel,
	.section-head {
		flex-direction: column;
		align-items: flex-start;
	}

	.section-head p {
		text-align: left;
	}

	.hero-meta {
		width: 100%;
	}
}

@media (max-width: 960px) {
	.form-card {
		padding: 22px;
		border-radius: 24px;
	}

	.summary-badge {
		flex-direction: column;
		text-align: center;
	}
}

@media (max-width: 640px) {
	.hero-copy h2,
	.badge-copy h3,
	.section-head h3 {
		font-size: 24px;
	}

	.hero-meta {
		grid-template-columns: 1fr;
	}

	.secondary-btn,
	.primary-btn {
		min-width: 110px;
		padding: 0 20px;
	}
}
</style>
