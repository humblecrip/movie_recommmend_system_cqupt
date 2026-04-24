<template>
	<div class="addEdit-block user-edit-page">
		<div class="user-form-shell">
			<el-form
				ref="ruleForm"
				class="add-update-preview"
				:model="ruleForm"
				:rules="rules"
				label-width="0"
			>
				<div class="user-form-card">
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
								<strong>{{ ruleForm.dianyingleixing || '未填写类型' }}</strong>
								<span>当前类型</span>
							</div>
							<div>
								<strong>{{ type === 'info' ? '只读' : '可编辑' }}</strong>
								<span>编辑模式</span>
							</div>
						</div>
					</header>

					<section class="avatar-panel summary-panel">
						<div class="avatar-stage">
							<div class="avatar-ring">
								<div class="avatar-placeholder">
									<i class="el-icon-collection-tag"></i>
								</div>
							</div>
						</div>

						<div class="avatar-copy">
							<div class="panel-kicker">Genre Setup</div>
							<h3>维护电影类型标签</h3>
							<p>该字段用于后台分类维护与前台筛选展示。继续复用原有保存、更新、详情回填逻辑，不引入新接口。</p>
						</div>
					</section>

					<section class="detail-panel">
						<div class="section-head">
							<div>
								<div class="panel-kicker">Profile Details</div>
								<h3>编辑电影类型</h3>
							</div>
							<p>页面骨架、按钮区、卡片质感与用户管理表单保持一致，仅保留当前业务字段。</p>
						</div>

						<div class="field-grid single-grid">
							<el-form-item class="field-card" prop="dianyingleixing">
								<div class="field-label">电影类型</div>
								<div class="field-control">
									<i class="field-icon el-icon-collection-tag"></i>
									<el-input
										v-if="type != 'info'"
										v-model="ruleForm.dianyingleixing"
										placeholder="请输入电影类型"
										clearable
										:readonly="ro.dianyingleixing"
									></el-input>
									<div v-else class="field-display">{{ ruleForm.dianyingleixing || '未填写' }}</div>
								</div>
							</el-form-item>
						</div>
					</section>

					<el-form-item class="btn action-row">
						<el-button v-if="type != 'info'" class="btn4 secondary-btn" @click="back()">取消</el-button>
						<el-button v-if="type != 'info'" class="btn3 primary-btn" type="success" @click="onSubmit">确定</el-button>
						<el-button v-if="type == 'info'" class="btn5 primary-btn" type="success" @click="back()">返回</el-button>
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
			ro: {
				dianyingleixing: false,
			},
			ruleForm: {
				dianyingleixing: '',
			},
			rules: {
				dianyingleixing: [],
			},
		}
	},
	props: ['parent'],
	computed: {
		modeTag() {
			if (this.type === 'info') {
				return 'DETAIL MODE'
			}
			if (this.ruleForm.id) {
				return 'EDIT MODE'
			}
			return 'CREATE MODE'
		},
		headerTitle() {
			if (this.type === 'info') {
				return this.ruleForm.dianyingleixing || '电影类型详情'
			}
			return this.ruleForm.id ? 'Edit Genre Profile' : 'Create Genre Profile'
		},
		headerDescription() {
			if (this.type === 'info') {
				return '查看当前电影类型配置，提交与回填逻辑保持原系统一致。'
			}
			return '将电影类型表单直接对齐到用户管理表单的布局、质感与交互层次。'
		},
	},
	methods: {
		download(file) {
			window.open(`${file}`)
		},
		init(id, type) {
			if (id) {
				this.id = id
				this.type = type
			} else {
				this.id = ''
				this.type = type || ''
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
					if (o == 'dianyingleixing') {
						this.ruleForm.dianyingleixing = obj[o]
						this.ro.dianyingleixing = true
						continue
					}
				}
			}
			this.$http({
				url: `${this.$storage.get('sessionTable')}/session`,
				method: 'get'
			}).then(({ data }) => {
				if (!(data && data.code === 0)) {
					this.$message.error(data.msg)
				}
			})
		},
		info(id) {
			this.$http({
				url: `dianyingleixing/info/${id}`,
				method: 'get'
			}).then(({ data }) => {
				if (data && data.code === 0) {
					this.ruleForm = data.data
					let reg = new RegExp('../../../upload', 'g')
				} else {
					this.$message.error(data.msg)
				}
			})
		},
		async onSubmit() {
			var objcross = this.$storage.getObj('crossObj')
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
								}).then(({ data }) => {})
							}
						}
					}

					await this.$http({
						url: `dianyingleixing/${!this.ruleForm.id ? 'save' : 'update'}`,
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
									this.parent.dianyingleixingCrossAddOrUpdateFlag = false
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
			this.parent.dianyingleixingCrossAddOrUpdateFlag = false
			this.parent.contentStyleChange()
		},
	}
}
</script>

<style lang="scss" scoped>
.user-edit-page {
	padding: 0 0 30px;
}

.user-form-shell,
.add-update-preview {
	width: 100%;
}

.user-form-card {
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
.avatar-panel,
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

.avatar-panel {
	display: grid;
	grid-template-columns: 180px minmax(0, 1fr);
	gap: 34px;
	align-items: center;
	margin-bottom: 28px;
	padding: 28px;
	border-radius: 28px;
	background: rgba(45, 52, 73, 0.44);
	border: 1px solid rgba(255, 255, 255, 0.07);
	backdrop-filter: blur(22px);
}

.avatar-stage {
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
}

.avatar-ring {
	position: relative;
	width: 148px;
	height: 148px;
	padding: 6px;
	border-radius: 999px;
	background: linear-gradient(135deg, #ffcf55, #8ea7ff);
	box-shadow: 0 18px 36px rgba(11, 19, 38, 0.32);
}

.avatar-placeholder {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 100%;
	height: 100%;
	border-radius: 999px;
	background: linear-gradient(145deg, #0a1224, #18223a);
	color: rgba(255, 255, 255, 0.7);
	font-size: 44px;
}

.avatar-copy h3 {
	margin: 14px 0 10px;
	color: #f5f7ff;
	font-size: 28px;
	line-height: 1.2;
	letter-spacing: -0.03em;
}

.avatar-copy p {
	margin: 0;
	max-width: 620px;
	color: rgba(218, 226, 253, 0.7);
	font-size: 14px;
	line-height: 1.8;
}

.detail-panel {
	padding: 28px;
	border-radius: 28px;
	background: rgba(8, 15, 29, 0.4);
	border: 1px solid rgba(255, 255, 255, 0.06);
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

.single-grid {
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
	box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.02);
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
	box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.02);
	transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
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
	letter-spacing: 0.02em;
}

.secondary-btn {
	background: rgba(255, 255, 255, 0.06);
	color: rgba(218, 226, 253, 0.86);
}

.secondary-btn:hover,
.secondary-btn:focus {
	background: rgba(255, 255, 255, 0.12);
	color: #ffffff;
}

.primary-btn {
	background: linear-gradient(135deg, #ffc639, #e1aa12);
	color: #261a00;
	box-shadow: 0 16px 28px rgba(247, 190, 44, 0.18);
}

.primary-btn:hover,
.primary-btn:focus {
	background: linear-gradient(135deg, #ffd45d, #ecb51b);
	color: #261a00;
	transform: translateY(-1px);
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
	.user-form-card {
		padding: 22px;
		border-radius: 24px;
	}

	.avatar-panel {
		grid-template-columns: 1fr;
		justify-items: center;
		text-align: center;
	}

	.avatar-copy p {
		margin-left: auto;
		margin-right: auto;
	}

	.field-grid {
		grid-template-columns: 1fr;
	}
}

@media (max-width: 640px) {
	.hero-copy h2,
	.avatar-copy h3,
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
