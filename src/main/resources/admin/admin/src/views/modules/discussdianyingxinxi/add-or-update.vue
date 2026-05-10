<template>
	<div class="addEdit-block comment-edit-page">
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
								<strong>{{ ruleForm.nickname || '匿名用户' }}</strong>
								<span>评论用户</span>
							</div>
							<div>
								<strong>{{ formatScore(ruleForm.score) }}</strong>
								<span>当前评分</span>
							</div>
						</div>
					</header>

					<section class="comment-panel">
						<div class="comment-summary">
							<div class="summary-avatar">{{ getNameFallback() }}</div>
							<div class="summary-copy">
								<div class="panel-kicker">Source Comment</div>
								<h3>查看原始评论并维护回复</h3>
								<p>保留现有富文本回复逻辑与提交接口，只对内容排版和交互容器进行重构。</p>
								<div class="score-row">
									<el-rate
										v-model="ruleForm.score"
										:max="5"
										:show-text="false"
										:colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
										void-color="#4f5b79"
										disabled
									></el-rate>
									<span>{{ formatScore(ruleForm.score) }}</span>
								</div>
							</div>
						</div>
						<div class="content-card ql-snow">
							<div class="card-label">评论内容</div>
							<div class="ql-editor" v-html="ruleForm.content || '暂无评论内容'"></div>
						</div>
					</section>

					<section class="detail-panel">
						<div class="section-head">
							<div>
								<div class="panel-kicker">Reply Editor</div>
								<h3>编辑评分与回复</h3>
							</div>
							<p>评分和回复通过兼容桥接写入 `app_movie_comment` / `app_movie_comment_vote`，不再直连旧表。</p>
						</div>

						<div class="field-grid single-column">
							<el-form-item class="field-card" prop="score">
								<div class="field-label">评分</div>
								<div class="field-control">
									<i class="field-icon el-icon-star-on"></i>
									<el-input-number
										v-if="type != 'info'"
										v-model="ruleForm.score"
										:min="0"
										:max="5"
										:step="1"
										:disabled="ro.score"
									></el-input-number>
									<div v-else class="field-display">{{ formatScore(ruleForm.score) }}</div>
								</div>
							</el-form-item>

							<el-form-item class="field-card" prop="reply">
								<div class="field-label">回复内容</div>
								<div v-if="type != 'info'" class="editor-shell">
									<editor
										v-model="ruleForm.reply"
										class="editor"
										myQuillEditor="reply"
										action="file/upload"
									></editor>
								</div>
								<div v-else-if="ruleForm.reply" class="reply-display ql-snow">
									<div class="ql-editor" v-html="ruleForm.reply"></div>
								</div>
								<div v-else class="field-display">暂无回复内容</div>
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
import {
	isNumber,
	isIntNumer
} from '@/utils/validate'

export default {
	data() {
		var validateNumber = (rule, value, callback) => {
			if (!value && value !== 0) {
				callback()
			} else if (!isNumber(value)) {
				callback(new Error('请输入数字'))
			} else {
				callback()
			}
		}
		var validateIntNumber = (rule, value, callback) => {
			if (!value && value !== 0) {
				callback()
			} else if (!isIntNumer(value)) {
				callback(new Error('请输入整数'))
			} else {
				callback()
			}
		}
		return {
			id: '',
			type: '',
			ro: {
				refid: false,
				userid: false,
				avatarurl: false,
				nickname: false,
				content: false,
				score: false,
				reply: false,
				thumbsupnum: false,
				crazilynum: false,
				istop: false,
				tuserids: false,
				cuserids: false
			},
			ruleForm: {
				refid: '',
				userid: '',
				avatarurl: '',
				nickname: '',
				content: '',
				score: '',
				reply: '',
				tuserids: '',
				cuserids: ''
			},
			rules: {
				refid: [
					{ required: true, message: '关联表id不能为空', trigger: 'blur' }
				],
				userid: [
					{ required: true, message: '用户id不能为空', trigger: 'blur' }
				],
				avatarurl: [],
				nickname: [],
				content: [
					{ required: true, message: '评论内容不能为空', trigger: 'blur' }
				],
				score: [
					{ validator: validateNumber, trigger: 'blur' }
				],
				reply: [],
				thumbsupnum: [
					{ validator: validateIntNumber, trigger: 'blur' }
				],
				crazilynum: [
					{ validator: validateIntNumber, trigger: 'blur' }
				],
				istop: [
					{ validator: validateIntNumber, trigger: 'blur' }
				],
				tuserids: [],
				cuserids: []
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
				return '评论详情'
			}
			return this.ruleForm.id ? 'Edit Comment Reply' : 'Create Comment Reply'
		},
		headerDescription() {
			if (this.type === 'info') {
				return '查看原评论内容、评分与后台回复。'
			}
			return '统一后台评论处理弹窗的视觉风格，同时继续复用原有评分与回复提交逻辑。'
		}
	},
	methods: {
		download(file) {
			window.open(`${file}`)
		},
		getNameFallback() {
			if (this.ruleForm.nickname) {
				return this.ruleForm.nickname.substring(0, 1)
			}
			return '评'
		},
		formatScore(value) {
			return Number(value || 0).toFixed(1)
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
					if (o == 'refid') {
						this.ruleForm.refid = obj[o]
						this.ro.refid = true
						continue
					}
					if (o == 'userid') {
						this.ruleForm.userid = obj[o]
						this.ro.userid = true
						continue
					}
					if (o == 'avatarurl') {
						this.ruleForm.avatarurl = obj[o]
						this.ro.avatarurl = true
						continue
					}
					if (o == 'nickname') {
						this.ruleForm.nickname = obj[o]
						this.ro.nickname = true
						continue
					}
					if (o == 'content') {
						this.ruleForm.content = obj[o]
						this.ro.content = true
						continue
					}
					if (o == 'score') {
						this.ruleForm.score = obj[o]
						this.ro.score = true
						continue
					}
					if (o == 'reply') {
						this.ruleForm.reply = obj[o]
						this.ro.reply = true
						continue
					}
					if (o == 'thumbsupnum') {
						this.ruleForm.thumbsupnum = obj[o]
						this.ro.thumbsupnum = true
						continue
					}
					if (o == 'crazilynum') {
						this.ruleForm.crazilynum = obj[o]
						this.ro.crazilynum = true
						continue
					}
					if (o == 'istop') {
						this.ruleForm.istop = obj[o]
						this.ro.istop = true
						continue
					}
					if (o == 'tuserids') {
						this.ruleForm.tuserids = obj[o]
						this.ro.tuserids = true
						continue
					}
					if (o == 'cuserids') {
						this.ruleForm.cuserids = obj[o]
						this.ro.cuserids = true
					}
				}
			}
		},
		info(id) {
			this.$http({
				url: `discussdianyingxinxi/info/${id}`,
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
			if (this.ruleForm.avatarurl != null) {
				this.ruleForm.avatarurl = this.ruleForm.avatarurl.replace(new RegExp(this.$base.url, 'g'), '')
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
						url: `discussdianyingxinxi/${!this.ruleForm.id ? 'save' : 'update'}`,
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
									this.parent.discussdianyingxinxiCrossAddOrUpdateFlag = false
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
			this.parent.discussdianyingxinxiCrossAddOrUpdateFlag = false
			this.parent.contentStyleChange()
		},
		avatarurlUploadChange(fileUrls) {
			this.ruleForm.avatarurl = fileUrls
		}
	}
}
</script>

<style lang="scss" scoped>
.comment-edit-page {
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
.comment-panel,
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

.comment-panel,
.detail-panel {
	padding: 28px;
	border-radius: 28px;
	background: rgba(45, 52, 73, 0.44);
	border: 1px solid rgba(255, 255, 255, 0.07);
	backdrop-filter: blur(22px);
}

.comment-panel {
	margin-bottom: 28px;
}

.comment-summary {
	display: flex;
	align-items: center;
	gap: 22px;
	margin-bottom: 24px;
}

.summary-avatar {
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
}

.summary-copy h3 {
	margin: 14px 0 10px;
	color: #f5f7ff;
	font-size: 28px;
	line-height: 1.2;
}

.summary-copy p {
	margin: 0 0 14px;
	max-width: 620px;
	color: rgba(218, 226, 253, 0.7);
	font-size: 14px;
	line-height: 1.8;
}

.score-row {
	display: flex;
	align-items: center;
	gap: 10px;
}

.score-row span {
	font-size: 12px;
	font-weight: 700;
	color: rgba(255, 198, 57, 0.88);
}

.content-card,
.reply-display {
	padding: 20px 22px;
	border-radius: 24px;
	background: rgba(8, 15, 29, 0.48);
	border: 1px solid rgba(255, 255, 255, 0.06);
}

.card-label,
.field-label {
	margin-bottom: 12px;
	color: rgba(208, 197, 175, 0.88);
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.12em;
	text-transform: uppercase;
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
}

.section-head p {
	margin: 0;
	max-width: 460px;
	color: rgba(218, 226, 253, 0.68);
	font-size: 13px;
	line-height: 1.8;
	text-align: right;
}

.field-grid.single-column {
	display: grid;
	grid-template-columns: minmax(0, 1fr);
	gap: 18px;
}

.add-update-preview ::v-deep .field-card {
	margin-bottom: 0;
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

.field-display {
	display: flex;
	align-items: center;
	min-height: 54px;
	padding: 0 18px 0 48px;
	border-radius: 18px;
	background: rgba(6, 14, 32, 0.82);
	border: 1px solid rgba(153, 144, 124, 0.28);
	color: #edf1ff;
	font-size: 14px;
}

.add-update-preview .el-input-number {
	width: 100%;
}

.add-update-preview .el-input-number ::v-deep .el-input__inner {
	height: 54px;
	border-radius: 18px;
	border: 1px solid rgba(153, 144, 124, 0.28);
	background: rgba(6, 14, 32, 0.82);
	padding: 0 18px 0 48px;
	color: #edf1ff;
	font-size: 14px;
	text-align: left;
}

.add-update-preview .el-input-number ::v-deep .el-input-number__increase,
.add-update-preview .el-input-number ::v-deep .el-input-number__decrease {
	background: rgba(255, 255, 255, 0.04);
	border-color: rgba(153, 144, 124, 0.18);
	color: rgba(218, 226, 253, 0.68);
}

.editor-shell {
	padding: 14px 16px;
	border-radius: 24px;
	background: rgba(6, 14, 32, 0.82);
	border: 1px solid rgba(153, 144, 124, 0.28);
}

.editor-shell ::v-deep .ql-toolbar.ql-snow,
.editor-shell ::v-deep .ql-container.ql-snow {
	border-color: rgba(153, 144, 124, 0.18);
	background: rgba(8, 15, 29, 0.42);
	color: #edf1ff;
}

.editor-shell ::v-deep .ql-editor {
	min-height: 160px;
	color: #edf1ff;
}

.add-update-preview ::v-deep .ql-editor {
	padding: 0;
	min-height: auto;
	line-height: 1.8;
	color: rgba(218, 226, 253, 0.78);
}

.add-update-preview ::v-deep .ql-editor p {
	margin: 0;
}

.add-update-preview ::v-deep .el-rate__icon {
	margin-right: 2px;
	font-size: 16px;
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

	.comment-summary {
		flex-direction: column;
		text-align: center;
	}
}

@media (max-width: 640px) {
	.hero-copy h2,
	.summary-copy h3,
	.section-head h3 {
		font-size: 24px;
	}

	.hero-meta {
		grid-template-columns: 1fr;
	}
}
</style>
