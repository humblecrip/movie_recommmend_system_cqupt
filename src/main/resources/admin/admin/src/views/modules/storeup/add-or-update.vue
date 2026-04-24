<template>
	<div class="addEdit-block favorite-edit-page">
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
								<strong>{{ ruleForm.name || '未填写名称' }}</strong>
								<span>收藏名称</span>
							</div>
							<div>
								<strong>{{ ruleForm.inteltype || '未分类' }}</strong>
								<span>推荐标签</span>
							</div>
						</div>
					</header>

					<section class="media-panel">
						<div class="media-stage">
							<div class="media-frame">
								<img v-if="picturePreview" :src="picturePreview" alt="picture preview" class="media-preview-image">
								<div v-else class="media-placeholder">
									<i class="el-icon-picture-outline"></i>
								</div>
							</div>
							<div v-if="type !== 'info' && !ro.picture" class="media-upload-badge">
								<i class="el-icon-camera-solid"></i>
							</div>
						</div>

						<div class="media-copy">
							<div class="panel-kicker">Cover Assets</div>
							<h3>维护收藏封面与展示素材</h3>
							<p>继续复用原有上传接口，不新增任何文件能力，仅对上传区域视觉进行统一。</p>

							<el-form-item class="upload media-upload-field" prop="picture">
								<file-upload
									v-if="type != 'info' && !ro.picture"
									tip="点击或拖拽上传图片"
									action="file/upload"
									:limit="3"
									:multiple="true"
									:fileUrls="ruleForm.picture ? ruleForm.picture : ''"
									@change="pictureUploadChange"
								></file-upload>

								<div v-else-if="ruleForm.picture" class="media-gallery">
									<img
										v-if="ruleForm.picture.substring(0, 4) == 'http' && ruleForm.picture.split(',w').length > 1"
										class="upload-img"
										:src="ruleForm.picture"
										width="104"
										height="104"
									>
									<img
										v-else-if="ruleForm.picture.substring(0, 4) == 'http'"
										class="upload-img"
										:src="ruleForm.picture.split(',')[0]"
										width="104"
										height="104"
									>
									<img
										v-else
										v-for="(item, index) in ruleForm.picture.split(',')"
										:key="index"
										class="upload-img"
										:src="$base.url + item"
										width="104"
										height="104"
									>
								</div>
								<div v-else class="media-readonly-empty">当前未上传图片</div>
							</el-form-item>
						</div>
					</section>

					<section class="detail-panel">
						<div class="section-head">
							<div>
								<div class="panel-kicker">Favorite Details</div>
								<h3>编辑收藏信息</h3>
							</div>
							<p>保留原有字段结构与提交校验，仅重构输入区的布局效果。</p>
						</div>

						<div class="field-grid">
							<el-form-item class="field-card" prop="name">
								<div class="field-label">名称</div>
								<div class="field-control">
									<i class="field-icon el-icon-collection-tag"></i>
									<el-input
										v-if="type != 'info'"
										v-model="ruleForm.name"
										placeholder="请输入名称"
										clearable
										:readonly="ro.name"
									></el-input>
									<div v-else class="field-display">{{ ruleForm.name || '未填写' }}</div>
								</div>
							</el-form-item>

							<el-form-item class="field-card" prop="inteltype">
								<div class="field-label">推荐类型</div>
								<div class="field-control">
									<i class="field-icon el-icon-s-opportunity"></i>
									<el-input
										v-if="type != 'info'"
										v-model="ruleForm.inteltype"
										placeholder="请输入推荐类型"
										clearable
										:readonly="ro.inteltype"
									></el-input>
									<div v-else class="field-display">{{ ruleForm.inteltype || '未填写' }}</div>
								</div>
							</el-form-item>

							<el-form-item class="field-card field-card-full" prop="remark">
								<div class="field-label">备注</div>
								<div class="field-control textarea-control">
									<i class="field-icon el-icon-document"></i>
									<el-input
										v-if="type != 'info'"
										v-model="ruleForm.remark"
										type="textarea"
										:rows="4"
										placeholder="请输入备注信息"
										:readonly="ro.remark"
									></el-input>
									<div v-else class="field-display textarea-display">{{ ruleForm.remark || '未填写' }}</div>
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
			ro: {
				userid: false,
				refid: false,
				tablename: false,
				name: false,
				picture: false,
				type: false,
				inteltype: false,
				remark: false
			},
			ruleForm: {
				userid: '',
				refid: '',
				tablename: '',
				name: '',
				picture: '',
				inteltype: '',
				remark: ''
			},
			rules: {
				userid: [
					{ required: true, message: '用户id不能为空', trigger: 'blur' }
				],
				refid: [],
				tablename: [],
				name: [
					{ required: true, message: '名称不能为空', trigger: 'blur' }
				],
				picture: [],
				type: [],
				inteltype: [],
				remark: []
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
				return '收藏详情'
			}
			return this.ruleForm.id ? 'Edit Favorite Record' : 'Create Favorite Record'
		},
		headerDescription() {
			if (this.type === 'info') {
				return '查看当前收藏条目的名称、图片与备注信息。'
			}
			return '对齐后台深色卡片视觉，继续使用当前收藏保存与更新接口。'
		},
		picturePreview() {
			if (!this.ruleForm.picture) {
				return ''
			}
			if (this.ruleForm.picture.substring(0, 4) === 'http' && this.ruleForm.picture.split(',w').length > 1) {
				return this.ruleForm.picture
			}
			if (this.ruleForm.picture.substring(0, 4) === 'http') {
				return this.ruleForm.picture.split(',')[0]
			}
			return this.$base.url + this.ruleForm.picture.split(',')[0]
		}
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
				this.ruleForm = {
					userid: '',
					refid: '',
					tablename: '',
					name: '',
					picture: '',
					inteltype: '',
					remark: ''
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
					if (o == 'userid') {
						this.ruleForm.userid = obj[o]
						this.ro.userid = true
						continue
					}
					if (o == 'refid') {
						this.ruleForm.refid = obj[o]
						this.ro.refid = true
						continue
					}
					if (o == 'tablename') {
						this.ruleForm.tablename = obj[o]
						this.ro.tablename = true
						continue
					}
					if (o == 'name') {
						this.ruleForm.name = obj[o]
						this.ro.name = true
						continue
					}
					if (o == 'picture') {
						this.ruleForm.picture = obj[o]
						this.ro.picture = true
						continue
					}
					if (o == 'type') {
						this.ruleForm.type = obj[o]
						this.ro.type = true
						continue
					}
					if (o == 'inteltype') {
						this.ruleForm.inteltype = obj[o]
						this.ro.inteltype = true
						continue
					}
					if (o == 'remark') {
						this.ruleForm.remark = obj[o]
						this.ro.remark = true
					}
				}
			}
		},
		info(id) {
			this.$http({
				url: `storeup/info/${id}`,
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
			if (this.ruleForm.picture != null) {
				this.ruleForm.picture = this.ruleForm.picture.replace(new RegExp(this.$base.url, 'g'), '')
			}
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
						url: `storeup/${!this.ruleForm.id ? 'save' : 'update'}`,
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
									this.parent.storeupCrossAddOrUpdateFlag = false
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
			this.parent.storeupCrossAddOrUpdateFlag = false
			this.parent.contentStyleChange()
		},
		pictureUploadChange(fileUrls) {
			this.ruleForm.picture = fileUrls
		}
	}
}
</script>

<style lang="scss" scoped>
.favorite-edit-page {
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
.media-panel,
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

.media-panel {
	display: grid;
	grid-template-columns: 220px minmax(0, 1fr);
	gap: 34px;
	align-items: center;
	margin-bottom: 28px;
	padding: 28px;
	border-radius: 28px;
	background: rgba(45, 52, 73, 0.44);
	border: 1px solid rgba(255, 255, 255, 0.07);
	backdrop-filter: blur(22px);
}

.media-stage {
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
}

.media-frame {
	width: 168px;
	height: 220px;
	padding: 6px;
	border-radius: 28px;
	background: linear-gradient(135deg, #ffcf55, #8ea7ff);
	box-shadow: 0 18px 36px rgba(11, 19, 38, 0.32);
}

.media-preview-image,
.media-placeholder {
	width: 100%;
	height: 100%;
	border-radius: 22px;
}

.media-preview-image {
	display: block;
	object-fit: cover;
	background: #0b1326;
}

.media-placeholder {
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(145deg, #0a1224, #18223a);
	color: rgba(255, 255, 255, 0.7);
	font-size: 44px;
}

.media-upload-badge {
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
}

.media-copy h3 {
	margin: 14px 0 10px;
	color: #f5f7ff;
	font-size: 28px;
	line-height: 1.2;
}

.media-copy p {
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

.field-card-full {
	grid-column: 1 / -1;
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
	top: 18px;
	z-index: 2;
	color: rgba(247, 190, 44, 0.72);
	font-size: 18px;
}

.field-display,
.add-update-preview .el-input ::v-deep .el-input__inner,
.add-update-preview .el-textarea ::v-deep .el-textarea__inner {
	border-radius: 18px;
}

.field-display {
	display: flex;
	align-items: center;
	min-height: 54px;
	padding: 0 18px 0 48px;
	background: rgba(6, 14, 32, 0.82);
	border: 1px solid rgba(153, 144, 124, 0.28);
	color: #edf1ff;
	font-size: 14px;
}

.textarea-display {
	align-items: flex-start;
	padding-top: 16px;
	padding-bottom: 16px;
	line-height: 1.8;
}

.add-update-preview .el-input,
.add-update-preview .el-textarea {
	width: 100%;
}

.add-update-preview .el-input ::v-deep .el-input__inner {
	height: 54px;
	border: 1px solid rgba(153, 144, 124, 0.28);
	background: rgba(6, 14, 32, 0.82);
	padding: 0 18px 0 48px;
	color: #edf1ff;
	font-size: 14px;
}

.add-update-preview .el-textarea ::v-deep .el-textarea__inner {
	min-height: 132px;
	border: 1px solid rgba(153, 144, 124, 0.28);
	background: rgba(6, 14, 32, 0.82);
	padding: 16px 18px 16px 48px;
	color: #edf1ff;
	font-size: 14px;
	line-height: 1.8;
}

.add-update-preview .el-input ::v-deep .el-input__inner::placeholder,
.add-update-preview .el-textarea ::v-deep .el-textarea__inner::placeholder {
	color: rgba(218, 226, 253, 0.34);
}

.media-gallery {
	display: flex;
	flex-wrap: wrap;
	gap: 12px;
}

.media-gallery .upload-img {
	display: block;
	object-fit: cover;
	border-radius: 18px;
	border: 1px solid rgba(153, 144, 124, 0.22);
}

.media-readonly-empty {
	display: inline-flex;
	align-items: center;
	padding: 10px 16px;
	border-radius: 999px;
	background: rgba(6, 14, 32, 0.75);
	border: 1px solid rgba(153, 144, 124, 0.18);
	color: rgba(218, 226, 253, 0.62);
	font-size: 13px;
}

.media-upload-field ::v-deep .el-upload {
	width: 100%;
}

.media-upload-field ::v-deep .el-upload--picture-card {
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

.media-upload-field ::v-deep .el-upload-list--picture-card {
	display: flex;
	flex-wrap: wrap;
	gap: 12px;
	margin-top: 14px;
}

.media-upload-field ::v-deep .el-upload-list--picture-card .el-upload-list__item {
	width: 104px;
	height: 104px;
	margin: 0;
	border-radius: 18px;
	border: 1px solid rgba(153, 144, 124, 0.22);
}

.media-upload-field ::v-deep .el-upload__tip {
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

	.media-panel {
		grid-template-columns: 1fr;
		justify-items: center;
		text-align: center;
	}

	.field-grid {
		grid-template-columns: 1fr;
	}

	.field-card-full {
		grid-column: auto;
	}
}

@media (max-width: 640px) {
	.hero-copy h2,
	.media-copy h3,
	.section-head h3 {
		font-size: 24px;
	}

	.hero-meta {
		grid-template-columns: 1fr;
	}
}
</style>
