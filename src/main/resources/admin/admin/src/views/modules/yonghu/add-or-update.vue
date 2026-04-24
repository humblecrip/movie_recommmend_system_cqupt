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
								<strong>{{ ruleForm.yonghuzhanghao || '未填写账号' }}</strong>
								<span>账号标识</span>
							</div>
							<div>
								<strong>{{ ruleForm.xingbie || '未填写性别' }}</strong>
								<span>基础信息</span>
							</div>
						</div>
					</header>

					<section class="avatar-panel">
						<div class="avatar-stage">
							<div class="avatar-ring">
								<img v-if="avatarPreview" :src="avatarPreview" alt="avatar preview" class="avatar-preview-image">
								<div v-else class="avatar-placeholder">
									<i class="el-icon-user-solid"></i>
								</div>
							</div>
							<div v-if="type !== 'info' && !ro.touxiang" class="avatar-upload-badge">
								<i class="el-icon-camera-solid"></i>
							</div>
						</div>

						<div class="avatar-copy">
							<div class="panel-kicker">头像 Avatar</div>
							<h3>维护用户头像与基础识别信息</h3>
							<p>支持继续复用当前上传接口，建议上传 1:1 的清晰头像图片，便于后台识别与列表展示。</p>

							<el-form-item class="upload avatar-upload-field" prop="touxiang">
								<file-upload
									v-if="type != 'info' && !ro.touxiang"
									tip="点击或拖拽上传头像"
									action="file/upload"
									:limit="3"
									:multiple="true"
									:fileUrls="ruleForm.touxiang ? ruleForm.touxiang : ''"
									@change="touxiangUploadChange"
								></file-upload>

								<div v-else-if="ruleForm.touxiang" class="avatar-gallery">
									<img
										v-if="ruleForm.touxiang.substring(0, 4) == 'http' && ruleForm.touxiang.split(',w').length > 1"
										class="upload-img"
										:src="ruleForm.touxiang"
										width="104"
										height="104"
									>
									<img
										v-else-if="ruleForm.touxiang.substring(0, 4) == 'http'"
										class="upload-img"
										:src="ruleForm.touxiang.split(',')[0]"
										width="104"
										height="104"
									>
									<img
										v-else
										v-for="(item, index) in ruleForm.touxiang.split(',')"
										:key="index"
										class="upload-img"
										:src="$base.url + item"
										width="104"
										height="104"
									>
								</div>
								<div v-else class="avatar-readonly-empty">当前未上传头像</div>
							</el-form-item>
						</div>
					</section>

					<section class="detail-panel">
						<div class="section-head">
							<div>
								<div class="panel-kicker">Profile Details</div>
								<h3>编辑用户资料</h3>
							</div>
							<p>保留现有表单校验、详情回填与提交逻辑，仅重构录入区域的布局与视觉层次。</p>
						</div>

						<div class="field-grid">
							<el-form-item class="field-card" prop="yonghuzhanghao">
								<div class="field-label">用户账号</div>
								<div class="field-control">
									<i class="field-icon el-icon-user"></i>
									<el-input
										v-if="type != 'info'"
										v-model="ruleForm.yonghuzhanghao"
										placeholder="请输入用户账号"
										clearable
										:readonly="ro.yonghuzhanghao"
									></el-input>
									<div v-else class="field-display">{{ ruleForm.yonghuzhanghao || '未填写' }}</div>
								</div>
							</el-form-item>

							<el-form-item class="field-card" prop="mima">
								<div class="field-label">密码</div>
								<div class="field-control password-control">
									<i class="field-icon el-icon-lock"></i>
									<el-input
										v-if="type != 'info'"
										v-model="ruleForm.mima"
										:type="passwordVisible ? 'text' : 'password'"
										placeholder="请输入密码"
										clearable
										:readonly="ro.mima"
									></el-input>
									<div v-else class="field-display">{{ ruleForm.mima || '未填写' }}</div>
									<button
										v-if="type != 'info'"
										type="button"
										class="visibility-trigger"
										@click="passwordVisible = !passwordVisible"
									>
										<i :class="passwordVisible ? 'el-icon-view' : 'el-icon-view'"></i>
									</button>
								</div>
							</el-form-item>

							<el-form-item class="field-card" prop="yonghuxingming">
								<div class="field-label">用户姓名</div>
								<div class="field-control">
									<i class="field-icon el-icon-postcard"></i>
									<el-input
										v-if="type != 'info'"
										v-model="ruleForm.yonghuxingming"
										placeholder="请输入用户姓名"
										clearable
										:readonly="ro.yonghuxingming"
									></el-input>
									<div v-else class="field-display">{{ ruleForm.yonghuxingming || '未填写' }}</div>
								</div>
							</el-form-item>

							<el-form-item class="field-card" prop="xingbie">
								<div class="field-label">性别</div>
								<div class="field-control">
									<i class="field-icon el-icon-s-custom"></i>
									<el-select
										v-if="type != 'info'"
										v-model="ruleForm.xingbie"
										:disabled="ro.xingbie"
										placeholder="请选择性别"
									>
										<el-option
											v-for="(item, index) in xingbieOptions"
											:key="index"
											:label="item"
											:value="item"
										></el-option>
									</el-select>
									<div v-else class="field-display">{{ ruleForm.xingbie || '未填写' }}</div>
								</div>
							</el-form-item>

							<el-form-item class="field-card" prop="lianxidianhua">
								<div class="field-label">联系电话</div>
								<div class="field-control">
									<i class="field-icon el-icon-phone-outline"></i>
									<el-input
										v-if="type != 'info'"
										v-model="ruleForm.lianxidianhua"
										placeholder="请输入联系电话"
										clearable
										:readonly="ro.lianxidianhua"
									></el-input>
									<div v-else class="field-display">{{ ruleForm.lianxidianhua || '未填写' }}</div>
								</div>
							</el-form-item>

							<el-form-item class="field-card" prop="shenfenzheng">
								<div class="field-label">身份证</div>
								<div class="field-control">
									<i class="field-icon el-icon-postcard"></i>
									<el-input
										v-if="type != 'info'"
										v-model="ruleForm.shenfenzheng"
										placeholder="请输入身份证号"
										clearable
										:readonly="ro.shenfenzheng"
									></el-input>
									<div v-else class="field-display">{{ ruleForm.shenfenzheng || '未填写' }}</div>
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
import {
	isMobile,
	checkIdCard,
} from '@/utils/validate'

export default {
	data() {
		var validateIdCard = (rule, value, callback) => {
			if (!value) {
				callback()
			} else if (!checkIdCard(value)) {
				callback(new Error('请输入正确的身份证号码'))
			} else {
				callback()
			}
		}
		var validateMobile = (rule, value, callback) => {
			if (!value) {
				callback()
			} else if (!isMobile(value)) {
				callback(new Error('请输入正确的手机号码'))
			} else {
				callback()
			}
		}
		return {
			id: '',
			type: '',
			passwordVisible: false,
			ro: {
				yonghuzhanghao: false,
				mima: false,
				yonghuxingming: false,
				touxiang: false,
				xingbie: false,
				lianxidianhua: false,
				shenfenzheng: false,
			},
			ruleForm: {
				yonghuzhanghao: '',
				mima: '',
				yonghuxingming: '',
				touxiang: '',
				xingbie: '',
				lianxidianhua: '',
				shenfenzheng: '',
			},
			xingbieOptions: [],
			rules: {
				yonghuzhanghao: [
					{ required: true, message: '用户账号不能为空', trigger: 'blur' },
				],
				mima: [
					{ required: true, message: '密码不能为空', trigger: 'blur' },
				],
				yonghuxingming: [
					{ required: true, message: '用户姓名不能为空', trigger: 'blur' },
				],
				touxiang: [],
				xingbie: [],
				lianxidianhua: [
					{ validator: validateMobile, trigger: 'blur' },
				],
				shenfenzheng: [
					{ validator: validateIdCard, trigger: 'blur' },
				],
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
				return '用户资料详情'
			}
			return this.ruleForm.id ? 'Edit User Profile' : 'Create User Profile'
		},
		headerDescription() {
			if (this.type === 'info') {
				return '查看当前用户的账号信息、头像资料与身份字段，提交逻辑保持原系统一致。'
			}
			return '将原型中的深色玻璃卡片布局对齐到当前管理后台，同时继续复用现有用户保存与更新接口。'
		},
		avatarPreview() {
			if (!this.ruleForm.touxiang) {
				return ''
			}
			if (this.ruleForm.touxiang.substring(0, 4) === 'http' && this.ruleForm.touxiang.split(',w').length > 1) {
				return this.ruleForm.touxiang
			}
			if (this.ruleForm.touxiang.substring(0, 4) === 'http') {
				return this.ruleForm.touxiang.split(',')[0]
			}
			return this.$base.url + this.ruleForm.touxiang.split(',')[0]
		},
	},
	created() {
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
			this.passwordVisible = false
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
					if (o == 'yonghuzhanghao') {
						this.ruleForm.yonghuzhanghao = obj[o]
						this.ro.yonghuzhanghao = true
						continue
					}
					if (o == 'mima') {
						this.ruleForm.mima = obj[o]
						this.ro.mima = true
						continue
					}
					if (o == 'yonghuxingming') {
						this.ruleForm.yonghuxingming = obj[o]
						this.ro.yonghuxingming = true
						continue
					}
					if (o == 'touxiang') {
						this.ruleForm.touxiang = obj[o]
						this.ro.touxiang = true
						continue
					}
					if (o == 'xingbie') {
						this.ruleForm.xingbie = obj[o]
						this.ro.xingbie = true
						continue
					}
					if (o == 'lianxidianhua') {
						this.ruleForm.lianxidianhua = obj[o]
						this.ro.lianxidianhua = true
						continue
					}
					if (o == 'shenfenzheng') {
						this.ruleForm.shenfenzheng = obj[o]
						this.ro.shenfenzheng = true
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
			this.xingbieOptions = '男,女'.split(',')
		},
		info(id) {
			this.$http({
				url: `yonghu/info/${id}`,
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
			if (this.ruleForm.touxiang != null) {
				this.ruleForm.touxiang = this.ruleForm.touxiang.replace(new RegExp(this.$base.url, 'g'), '')
			}
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
						url: `yonghu/${!this.ruleForm.id ? 'save' : 'update'}`,
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
									this.parent.yonghuCrossAddOrUpdateFlag = false
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
			this.parent.yonghuCrossAddOrUpdateFlag = false
			this.parent.contentStyleChange()
		},
		touxiangUploadChange(fileUrls) {
			this.ruleForm.touxiang = fileUrls
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

.avatar-preview-image,
.avatar-placeholder {
	width: 100%;
	height: 100%;
	border-radius: 999px;
}

.avatar-preview-image {
	display: block;
	object-fit: cover;
	background: #0b1326;
}

.avatar-placeholder {
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(145deg, #0a1224, #18223a);
	color: rgba(255, 255, 255, 0.7);
	font-size: 44px;
}

.avatar-upload-badge {
	position: absolute;
	right: 12px;
	bottom: 8px;
	display: flex;
	align-items: center;
	justify-content: center;
	width: 40px;
	height: 40px;
	border-radius: 999px;
	background: linear-gradient(135deg, #ffcb46, #e9a900);
	color: #261a00;
	font-size: 18px;
	box-shadow: 0 10px 20px rgba(247, 190, 44, 0.28);
}

.avatar-copy h3 {
	margin: 14px 0 10px;
	color: #f5f7ff;
	font-size: 28px;
	line-height: 1.2;
	letter-spacing: -0.03em;
}

.avatar-copy p {
	margin: 0 0 18px;
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
.add-update-preview .el-input ::v-deep .el-input__inner,
.add-update-preview .el-select ::v-deep .el-input__inner {
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

.add-update-preview .el-input,
.add-update-preview .el-select {
	width: 100%;
}

.add-update-preview .el-input ::v-deep .el-input__inner,
.add-update-preview .el-select ::v-deep .el-input__inner {
	border: 1px solid rgba(153, 144, 124, 0.28);
	background: rgba(6, 14, 32, 0.82);
	padding: 0 48px 0 48px;
	color: #edf1ff;
	font-size: 14px;
	box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.02);
	transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.add-update-preview .el-input ::v-deep .el-input__inner::placeholder,
.add-update-preview .el-select ::v-deep .el-input__inner::placeholder {
	color: rgba(218, 226, 253, 0.34);
}

.add-update-preview .el-input ::v-deep .el-input__inner:focus,
.add-update-preview .el-select ::v-deep .el-input__inner:focus {
	border-color: rgba(247, 190, 44, 0.72);
	box-shadow: 0 0 0 4px rgba(247, 190, 44, 0.08);
}

.add-update-preview .el-input ::v-deep .el-input__inner[readonly='readonly'],
.add-update-preview .el-select ::v-deep .is-disabled .el-input__inner {
	background: rgba(6, 14, 32, 0.72);
	border-color: rgba(153, 144, 124, 0.18);
	color: rgba(237, 241, 255, 0.72);
}

.add-update-preview .el-input ::v-deep .el-input__suffix,
.add-update-preview .el-select ::v-deep .el-input__suffix {
	right: 12px;
}

.add-update-preview .el-select ::v-deep .el-input .el-select__caret {
	color: rgba(218, 226, 253, 0.56);
	font-size: 16px;
}

.password-control .el-input {
	position: relative;
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

.add-update-preview ::v-deep .avatar-upload-field {
	margin-bottom: 0;
}

.avatar-gallery {
	display: flex;
	flex-wrap: wrap;
	gap: 12px;
}

.avatar-gallery .upload-img {
	display: block;
	object-fit: cover;
	border-radius: 18px;
	border: 1px solid rgba(153, 144, 124, 0.22);
}

.avatar-readonly-empty {
	display: inline-flex;
	align-items: center;
	padding: 10px 16px;
	border-radius: 999px;
	background: rgba(6, 14, 32, 0.75);
	border: 1px solid rgba(153, 144, 124, 0.18);
	color: rgba(218, 226, 253, 0.62);
	font-size: 13px;
}

.avatar-upload-field ::v-deep .el-upload {
	width: 100%;
}

.avatar-upload-field ::v-deep .el-upload--picture-card {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 180px;
	height: 54px;
	border-radius: 999px;
	border: 1px solid rgba(247, 190, 44, 0.28);
	background: rgba(247, 190, 44, 0.12);
	color: #ffd76b;
	overflow: hidden;
}

.avatar-upload-field ::v-deep .el-upload--picture-card .el-icon-plus {
	font-size: 18px;
	line-height: 1;
}

.avatar-upload-field ::v-deep .el-upload-list--picture-card {
	display: flex;
	flex-wrap: wrap;
	gap: 12px;
	margin-top: 14px;
}

.avatar-upload-field ::v-deep .el-upload-list--picture-card .el-upload-list__item {
	width: 104px;
	height: 104px;
	margin: 0;
	border-radius: 18px;
	border: 1px solid rgba(153, 144, 124, 0.22);
}

.avatar-upload-field ::v-deep .el-upload__tip {
	margin-top: 14px;
	color: rgba(218, 226, 253, 0.58);
	font-size: 12px;
	line-height: 1.6;
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

	.avatar-upload-field ::v-deep .el-upload--picture-card {
		width: 100%;
	}

	.secondary-btn,
	.primary-btn {
		min-width: 110px;
		padding: 0 20px;
	}
}
</style>
