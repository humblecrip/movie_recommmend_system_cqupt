<template>
	<div class="addEdit-block movie-form-page">
		<div class="form-shell">
			<div class="form-header-card">
				<div>
					<div class="form-mode">{{ modeLabel }}</div>
					<h2>{{ headerTitle }}</h2>
					<p>{{ headerDescription }}</p>
				</div>
				<div class="form-overview-tags">
					<span>{{ ruleForm.dianyingleixing || '电影类型待补充' }}</span>
					<span>{{ ruleForm.quyu || '区域待补充' }}</span>
					<span>{{ ruleForm.shangyingshijian || '上映时间待补充' }}</span>
				</div>
			</div>

			<el-form
				class="add-update-preview"
				ref="ruleForm"
				:model="ruleForm"
				:rules="rules"
				label-width="140px"
			>
				<div class="form-content-grid">
					<section class="form-main-card">
						<div class="section-title-row">
							<h3>基础资料</h3>
							<p>维护电影名称、类型、区域、上映信息与主创阵容。</p>
						</div>
						<div class="form-grid-two">
							<el-form-item class="input" v-if="type!='info'" label="电影名称" prop="dianyingmingcheng">
								<el-input v-model="ruleForm.dianyingmingcheng" placeholder="电影名称" clearable :readonly="ro.dianyingmingcheng"></el-input>
							</el-form-item>
							<el-form-item v-else class="input" label="电影名称" prop="dianyingmingcheng">
								<el-input v-model="ruleForm.dianyingmingcheng" placeholder="电影名称" readonly></el-input>
							</el-form-item>

							<el-form-item class="select" v-if="type!='info'" label="电影类型" prop="dianyingleixing">
								<el-select :disabled="ro.dianyingleixing" v-model="ruleForm.dianyingleixing" placeholder="请选择电影类型">
									<el-option v-for="(item,index) in dianyingleixingOptions" :key="index" :label="item" :value="item"></el-option>
								</el-select>
							</el-form-item>
							<el-form-item v-else class="input" label="电影类型" prop="dianyingleixing">
								<el-input v-model="ruleForm.dianyingleixing" placeholder="电影类型" readonly></el-input>
							</el-form-item>

							<el-form-item class="select" v-if="type!='info'" label="区域" prop="quyu">
								<el-select :disabled="ro.quyu" v-model="ruleForm.quyu" placeholder="请选择区域">
									<el-option v-for="(item,index) in quyuOptions" :key="index" :label="item" :value="item"></el-option>
								</el-select>
							</el-form-item>
							<el-form-item v-else class="input" label="区域" prop="quyu">
								<el-input v-model="ruleForm.quyu" placeholder="区域" readonly></el-input>
							</el-form-item>

							<el-form-item class="date" v-if="type!='info'" label="上映时间" prop="shangyingshijian">
								<el-date-picker
									format="yyyy 年 MM 月 dd 日"
									value-format="yyyy-MM-dd"
									v-model="ruleForm.shangyingshijian"
									type="date"
									:readonly="ro.shangyingshijian"
									placeholder="上映时间"
								></el-date-picker>
							</el-form-item>
							<el-form-item class="input" v-else-if="ruleForm.shangyingshijian" label="上映时间" prop="shangyingshijian">
								<el-input v-model="ruleForm.shangyingshijian" placeholder="上映时间" readonly></el-input>
							</el-form-item>

							<el-form-item class="input" v-if="type!='info'" label="导演" prop="daoyan">
								<el-input v-model="ruleForm.daoyan" placeholder="导演" clearable :readonly="ro.daoyan"></el-input>
							</el-form-item>
							<el-form-item v-else class="input" label="导演" prop="daoyan">
								<el-input v-model="ruleForm.daoyan" placeholder="导演" readonly></el-input>
							</el-form-item>

							<el-form-item class="input" v-if="type!='info'" label="主演" prop="zhuyan">
								<el-input v-model="ruleForm.zhuyan" placeholder="主演" clearable :readonly="ro.zhuyan"></el-input>
							</el-form-item>
							<el-form-item v-else class="input" label="主演" prop="zhuyan">
								<el-input v-model="ruleForm.zhuyan" placeholder="主演" readonly></el-input>
							</el-form-item>
						</div>

						<div class="section-title-row narrative-title">
							<h3>内容描述</h3>
							<p>展示剧情摘要与电影详情富文本内容。</p>
						</div>

						<el-form-item class="textarea section-block" v-if="type!='info'" label="剧情简介" prop="juqingjianjie">
							<el-input type="textarea" :rows="7" placeholder="剧情简介" v-model="ruleForm.juqingjianjie"></el-input>
						</el-form-item>
						<el-form-item v-else-if="ruleForm.juqingjianjie" class="section-block" label="剧情简介" prop="juqingjianjie">
							<span class="text intro-text">{{ruleForm.juqingjianjie}}</span>
						</el-form-item>

						<el-form-item v-if="type!='info'" class="section-block editor-block" label="电影详情" prop="dianyingxiangqing">
							<editor v-model="ruleForm.dianyingxiangqing" class="editor" myQuillEditor="dianyingxiangqing" action="file/upload"></editor>
						</el-form-item>
						<el-form-item v-else-if="ruleForm.dianyingxiangqing" class="section-block editor-block" label="电影详情" prop="dianyingxiangqing">
							<span class="text ql-snow ql-editor detail-html" v-html="ruleForm.dianyingxiangqing"></span>
						</el-form-item>
					</section>

					<aside class="form-side-card">
						<div class="section-title-row side-title">
							<h3>海报信息</h3>
							<p>上传或预览当前电影海报素材。</p>
						</div>

						<div class="poster-preview-box">
							<img v-if="posterPreview" :src="posterPreview" alt="poster preview" class="poster-preview-image">
							<div v-else class="poster-empty">暂无海报</div>
						</div>

						<el-form-item class="upload poster-upload" v-if="type!='info' && !ro.haibao" label="海报" prop="haibao">
							<file-upload
								tip="点击上传海报"
								action="file/upload"
								:limit="3"
								:multiple="true"
								:fileUrls="ruleForm.haibao?ruleForm.haibao:''"
								@change="haibaoUploadChange"
							></file-upload>
						</el-form-item>
						<el-form-item class="upload poster-gallery" v-else-if="ruleForm.haibao" label="海报" prop="haibao">
							<div class="poster-gallery-list">
								<img v-if="ruleForm.haibao.substring(0,4)=='http'&&ruleForm.haibao.split(',w').length>1" class="upload-img" :src="ruleForm.haibao" width="100" height="100">
								<img v-else-if="ruleForm.haibao.substring(0,4)=='http'" class="upload-img" :src="ruleForm.haibao.split(',')[0]" width="100" height="100">
								<img v-else class="upload-img" v-for="(item,index) in ruleForm.haibao.split(',')" :key="index" :src="$base.url+item" width="100" height="100">
							</div>
						</el-form-item>

						<div class="side-meta-grid">
							<div>
								<strong>{{ ruleForm.dianyingmingcheng || '未命名电影' }}</strong>
								<span>当前影片</span>
							</div>
							<div>
								<strong>{{ ruleForm.daoyan || '待补充' }}</strong>
								<span>导演</span>
							</div>
							<div>
								<strong>{{ ruleForm.zhuyan || '待补充' }}</strong>
								<span>主演</span>
							</div>
							<div>
								<strong>{{ ruleForm.quyu || '待补充' }}</strong>
								<span>区域</span>
							</div>
						</div>
					</aside>
				</div>

				<el-form-item class="btn action-row">
					<el-button class="btn3" v-if="type!='info'" type="success" @click="onSubmit">确定</el-button>
					<el-button class="btn4" v-if="type!='info'" type="success" @click="back()">取消</el-button>
					<el-button class="btn5" v-if="type=='info'" type="success" @click="back()">返回</el-button>
				</el-form-item>
			</el-form>
		</div>
	</div>
</template>

<script>
import {
	isNumber,
	isIntNumer,
} from '@/utils/validate'

export default {
	data() {
		var validateNumber = (rule, value, callback) => {
			if (!value) {
				callback()
			} else if (!isNumber(value)) {
				callback(new Error('请输入数字'))
			} else {
				callback()
			}
		}
		var validateIntNumber = (rule, value, callback) => {
			if (!value) {
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
				dianyingmingcheng: false,
				haibao: false,
				dianyingleixing: false,
				quyu: false,
				shangyingshijian: false,
				daoyan: false,
				zhuyan: false,
				juqingjianjie: false,
				dianyingxiangqing: false,
				thumbsupnum: false,
				crazilynum: false,
				clicktime: false,
				clicknum: false,
				discussnum: false,
				totalscore: false,
				storeupnum: false,
			},
			ruleForm: {
				dianyingmingcheng: '',
				haibao: '',
				dianyingleixing: '',
				quyu: '',
				shangyingshijian: '',
				daoyan: '',
				zhuyan: '',
				juqingjianjie: '',
				dianyingxiangqing: '',
				clicktime: '',
			},
			dianyingleixingOptions: [],
			quyuOptions: [],
			rules: {
				dianyingmingcheng: [],
				haibao: [],
				dianyingleixing: [],
				quyu: [],
				shangyingshijian: [],
				daoyan: [],
				zhuyan: [],
				juqingjianjie: [],
				dianyingxiangqing: [],
				thumbsupnum: [{ validator: validateIntNumber, trigger: 'blur' }],
				crazilynum: [{ validator: validateIntNumber, trigger: 'blur' }],
				clicktime: [],
				clicknum: [{ validator: validateIntNumber, trigger: 'blur' }],
				discussnum: [{ validator: validateIntNumber, trigger: 'blur' }],
				totalscore: [{ validator: validateNumber, trigger: 'blur' }],
				storeupnum: [{ validator: validateIntNumber, trigger: 'blur' }],
			},
		}
	},
	props: ['parent'],
	computed: {
		modeLabel() {
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
				return this.ruleForm.dianyingmingcheng ? this.ruleForm.dianyingmingcheng : '电影详情'
			}
			return this.ruleForm.id ? '编辑电影资料' : '新增电影资料'
		},
		headerDescription() {
			if (this.type === 'info') {
				return '查看当前电影的基础资料、海报信息与详情内容。'
			}
			return '保持原有提交逻辑不变，仅优化桌面管理场景下的录入与预览体验。'
		},
		posterPreview() {
			if (!this.ruleForm.haibao) {
				return ''
			}
			if (this.ruleForm.haibao.substring(0, 4) === 'http' && this.ruleForm.haibao.split(',w').length > 1) {
				return this.ruleForm.haibao
			}
			if (this.ruleForm.haibao.substring(0, 4) === 'http') {
				return this.ruleForm.haibao.split(',')[0]
			}
			return this.$base.url + this.ruleForm.haibao.split(',')[0]
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
					if (o == 'dianyingmingcheng') {
						this.ruleForm.dianyingmingcheng = obj[o]
						this.ro.dianyingmingcheng = true
						continue
					}
					if (o == 'haibao') {
						this.ruleForm.haibao = obj[o]
						this.ro.haibao = true
						continue
					}
					if (o == 'dianyingleixing') {
						this.ruleForm.dianyingleixing = obj[o]
						this.ro.dianyingleixing = true
						continue
					}
					if (o == 'quyu') {
						this.ruleForm.quyu = obj[o]
						this.ro.quyu = true
						continue
					}
					if (o == 'shangyingshijian') {
						this.ruleForm.shangyingshijian = obj[o]
						this.ro.shangyingshijian = true
						continue
					}
					if (o == 'daoyan') {
						this.ruleForm.daoyan = obj[o]
						this.ro.daoyan = true
						continue
					}
					if (o == 'zhuyan') {
						this.ruleForm.zhuyan = obj[o]
						this.ro.zhuyan = true
						continue
					}
					if (o == 'juqingjianjie') {
						this.ruleForm.juqingjianjie = obj[o]
						this.ro.juqingjianjie = true
						continue
					}
					if (o == 'dianyingxiangqing') {
						this.ruleForm.dianyingxiangqing = obj[o]
						this.ro.dianyingxiangqing = true
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
					if (o == 'clicktime') {
						this.ruleForm.clicktime = obj[o]
						this.ro.clicktime = true
						continue
					}
					if (o == 'clicknum') {
						this.ruleForm.clicknum = obj[o]
						this.ro.clicknum = true
						continue
					}
					if (o == 'discussnum') {
						this.ruleForm.discussnum = obj[o]
						this.ro.discussnum = true
						continue
					}
					if (o == 'totalscore') {
						this.ruleForm.totalscore = obj[o]
						this.ro.totalscore = true
						continue
					}
					if (o == 'storeupnum') {
						this.ruleForm.storeupnum = obj[o]
						this.ro.storeupnum = true
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
			this.$http({
				url: 'appmovie/types',
				method: 'get'
			}).then(({ data }) => {
				if (data && data.code === 0) {
					this.dianyingleixingOptions = (data.data || []).map(item => item.typeName)
				} else {
					this.$message.error(data.msg)
				}
			})
			this.quyuOptions = '大陆,美国,韩国,日本,中国香港,中国台湾,泰国,印度,法国,英国,俄罗斯,意大利,西班牙,德国,波兰,澳大利亚,伊朗,其他'.split(',')
		},
		info(id) {
			this.$http({
				url: `dianyingxinxi/info/${id}`,
				method: 'get'
			}).then(({ data }) => {
				if (data && data.code === 0) {
					this.ruleForm = data.data
					let reg = new RegExp('../../../upload', 'g')
					this.ruleForm.dianyingxiangqing = this.ruleForm.dianyingxiangqing.replace(reg, '../../../springbootdo4wek3z/upload')
				} else {
					this.$message.error(data.msg)
				}
			})
		},
		async onSubmit() {
			if (this.ruleForm.haibao != null) {
				this.ruleForm.haibao = this.ruleForm.haibao.replace(new RegExp(this.$base.url, 'g'), '')
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
						url: `dianyingxinxi/${!this.ruleForm.id ? 'save' : 'update'}`,
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
									this.parent.dianyingxinxiCrossAddOrUpdateFlag = false
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
			this.parent.dianyingxinxiCrossAddOrUpdateFlag = false
			this.parent.contentStyleChange()
		},
		haibaoUploadChange(fileUrls) {
			this.ruleForm.haibao = fileUrls
		},
	},
}
</script>

<style lang="scss" scoped>
.movie-form-page {
	padding: 0 0 30px;
}

.form-shell {
	position: relative;
	display: flex;
	flex-direction: column;
	gap: 20px;
}

.form-shell::before,
.form-shell::after {
	content: '';
	position: absolute;
	border-radius: 999px;
	filter: blur(22px);
	pointer-events: none;
}

.form-shell::before {
	top: 48px;
	left: -52px;
	width: 180px;
	height: 180px;
	background: rgba(92, 124, 250, 0.12);
}

.form-shell::after {
	top: -30px;
	right: -34px;
	width: 210px;
	height: 210px;
	background: rgba(247, 190, 44, 0.12);
}

.form-header-card,
.add-update-preview {
	position: relative;
	z-index: 1;
	border: 1px solid rgba(255, 255, 255, 0.08);
	border-radius: 28px;
	background:
		radial-gradient(circle at top right, rgba(247, 190, 44, 0.16), transparent 34%),
		radial-gradient(circle at left bottom, rgba(87, 120, 255, 0.12), transparent 30%),
		linear-gradient(145deg, rgba(17, 25, 45, 0.96), rgba(23, 34, 57, 0.96) 54%, rgba(13, 22, 41, 0.98));
	box-shadow: 0 26px 60px rgba(8, 12, 24, 0.24);
	backdrop-filter: blur(26px);
}

.form-header-card {
	display: flex;
	justify-content: space-between;
	gap: 20px;
	align-items: flex-start;
	padding: 28px 30px;
}

.form-mode {
	margin-bottom: 10px;
	color: #ffd76b;
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.16em;
	text-transform: uppercase;
}

.form-header-card h2 {
	margin: 0 0 10px;
	color: #f5f7ff;
	font-size: 30px;
}

.form-header-card p {
	margin: 0;
	max-width: 760px;
	color: rgba(218, 226, 253, 0.72);
	font-size: 14px;
	line-height: 1.8;
}

.form-overview-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 10px;
	justify-content: flex-end;
}

.form-overview-tags span {
	padding: 8px 12px;
	border-radius: 999px;
	background: rgba(247, 190, 44, 0.1);
	color: #ffd76b;
	font-size: 12px;
	font-weight: 600;
	border: 1px solid rgba(247, 190, 44, 0.22);
}

.add-update-preview {
	padding: 28px;
}

.form-content-grid {
	display: grid;
	grid-template-columns: minmax(0, 1fr) 320px;
	gap: 22px;
}

.form-main-card,
.form-side-card {
	border: 1px solid rgba(255, 255, 255, 0.06);
	border-radius: 24px;
	background: rgba(8, 15, 29, 0.42);
}

.form-main-card {
	padding: 22px;
}

.form-side-card {
	padding: 20px;
	height: fit-content;
	position: sticky;
	top: 0;
}

.section-title-row {
	margin-bottom: 18px;
}

.section-title-row h3 {
	margin: 0 0 8px;
	color: #f5f7ff;
	font-size: 20px;
}

.section-title-row p {
	margin: 0;
	color: rgba(218, 226, 253, 0.68);
	font-size: 13px;
}

.narrative-title {
	margin-top: 10px;
}

.form-grid-two {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 0 16px;
}

.section-block {
	width: 100%;
}

.poster-preview-box {
	overflow: hidden;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 24px;
	background: linear-gradient(145deg, rgba(6, 14, 32, 0.92), rgba(24, 34, 58, 0.8));
	border: 1px solid rgba(153, 144, 124, 0.18);
	height: 360px;
	margin-bottom: 16px;
}

.poster-preview-image {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

.poster-empty {
	color: rgba(218, 226, 253, 0.62);
	font-size: 14px;
}

.poster-gallery-list {
	display: flex;
	flex-wrap: wrap;
	gap: 10px;
}

.side-meta-grid {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 12px;
	margin-top: 12px;
}

.side-meta-grid div {
	padding: 14px;
	border-radius: 18px;
	background: rgba(6, 14, 32, 0.72);
	border: 1px solid rgba(153, 144, 124, 0.14);
}

.side-meta-grid strong {
	display: block;
	color: #f5f7ff;
	font-size: 16px;
	line-height: 1.6;
}

.side-meta-grid span {
	display: block;
	margin-top: 4px;
	color: rgba(218, 226, 253, 0.54);
	font-size: 12px;
}

.add-update-preview ::v-deep .el-form-item {
	margin-bottom: 22px;
}

.add-update-preview ::v-deep .el-form-item__label {
	color: rgba(208, 197, 175, 0.88);
	font-weight: 700;
	font-size: 12px;
	letter-spacing: 0.12em;
	text-transform: uppercase;
}

.add-update-preview ::v-deep .el-form-item__content {
	line-height: normal;
}

.add-update-preview .el-input,
.add-update-preview .el-select,
.add-update-preview .el-date-editor,
.add-update-preview .el-input-number {
	width: 100%;
}

.add-update-preview .el-input ::v-deep .el-input__inner,
.add-update-preview .el-select ::v-deep .el-input__inner,
.add-update-preview .el-date-editor ::v-deep .el-input__inner,
.add-update-preview .el-input-number ::v-deep .el-input__inner {
	border: 1px solid rgba(153, 144, 124, 0.28);
	border-radius: 18px;
	height: 54px;
	line-height: 54px;
	padding: 0 14px;
	background: rgba(6, 14, 32, 0.82);
	color: #edf1ff;
	font-size: 14px;
	box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.02);
}

.add-update-preview .el-input ::v-deep .el-input__inner[readonly='readonly'],
.add-update-preview .el-date-editor ::v-deep .el-input__inner[readonly='readonly'],
.add-update-preview .el-select ::v-deep .is-disabled .el-input__inner,
.add-update-preview .el-input-number ::v-deep .is-disabled .el-input__inner {
	background: rgba(6, 14, 32, 0.72);
	border-color: rgba(153, 144, 124, 0.18);
	color: rgba(237, 241, 255, 0.72);
}

.add-update-preview .el-input ::v-deep .el-input__inner::placeholder,
.add-update-preview .el-select ::v-deep .el-input__inner::placeholder,
.add-update-preview .el-date-editor ::v-deep .el-input__inner::placeholder {
	color: rgba(218, 226, 253, 0.34);
}

.add-update-preview .el-input ::v-deep .el-input__inner:focus,
.add-update-preview .el-select ::v-deep .el-input__inner:focus,
.add-update-preview .el-date-editor ::v-deep .el-input__inner:focus,
.add-update-preview .el-input-number ::v-deep .el-input__inner:focus {
	border-color: rgba(247, 190, 44, 0.72);
	box-shadow: 0 0 0 4px rgba(247, 190, 44, 0.08);
}

.add-update-preview .el-textarea ::v-deep .el-textarea__inner {
	border: 1px solid rgba(153, 144, 124, 0.28);
	border-radius: 18px;
	padding: 14px 16px;
	background: rgba(6, 14, 32, 0.82);
	color: #edf1ff;
	min-height: 160px;
	box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.02);
}

.add-update-preview .el-textarea ::v-deep .el-textarea__inner[readonly='readonly'] {
	background: rgba(6, 14, 32, 0.72);
	border-color: rgba(153, 144, 124, 0.18);
	color: rgba(237, 241, 255, 0.72);
}

.add-update-preview .el-textarea ::v-deep .el-textarea__inner::placeholder {
	color: rgba(218, 226, 253, 0.34);
}

.add-update-preview .el-select ::v-deep .el-input .el-select__caret,
.add-update-preview .el-date-editor ::v-deep .el-input__icon {
	color: rgba(218, 226, 253, 0.56);
}

.poster-upload ::v-deep .el-upload--picture-card,
.poster-upload ::v-deep .el-upload-list__item,
.poster-upload ::v-deep .el-upload .el-icon-plus,
.poster-gallery ::v-deep .upload-img {
	border-radius: 16px;
}

.poster-upload ::v-deep .el-upload .el-icon-plus {
	border: 1px solid rgba(247, 190, 44, 0.28);
	background: rgba(247, 190, 44, 0.12);
	color: #ffd76b;
	width: 92px;
	height: 92px;
	line-height: 92px;
}

.poster-gallery ::v-deep .upload-img,
.poster-upload ::v-deep .el-upload-list__item {
	width: 92px;
	height: 92px;
	object-fit: cover;
	border: 1px solid rgba(153, 144, 124, 0.22);
}

.intro-text,
.detail-html {
	display: block;
	padding: 16px;
	border-radius: 18px;
	background: rgba(6, 14, 32, 0.72);
	color: #edf1ff;
	line-height: 1.9;
	border: 1px solid rgba(153, 144, 124, 0.14);
}

.editor-block ::v-deep .ql-toolbar,
.editor-block ::v-deep .ql-container {
	border-color: rgba(153, 144, 124, 0.22);
	background: rgba(6, 14, 32, 0.76);
	color: #edf1ff;
}

.editor-block ::v-deep .ql-container {
	border-radius: 0 0 18px 18px;
	min-height: 240px;
}

.editor-block ::v-deep .ql-toolbar {
	border-radius: 18px 18px 0 0;
}

.action-row {
	margin: 24px 0 0;
	padding-top: 26px;
	border-top: 1px solid rgba(153, 144, 124, 0.16);
}

.action-row .btn3,
.action-row .btn4,
.action-row .btn5 {
	border: none;
	border-radius: 999px;
	min-width: 126px;
	height: 48px;
	font-size: 14px;
	font-weight: 700;
	letter-spacing: 0.02em;
}

.action-row .btn3 {
	background: linear-gradient(135deg, #ffc639, #e1aa12);
	color: #261a00;
	box-shadow: 0 16px 28px rgba(247, 190, 44, 0.18);
}

.action-row .btn4 {
	background: rgba(255, 255, 255, 0.06);
	color: rgba(218, 226, 253, 0.86);
}

.action-row .btn5 {
	background: linear-gradient(135deg, #ffc639, #e1aa12);
	color: #261a00;
	box-shadow: 0 16px 28px rgba(247, 190, 44, 0.18);
}

.action-row .btn3:hover,
.action-row .btn5:hover {
	transform: translateY(-1px);
}

.action-row .btn4:hover {
	background: rgba(255, 255, 255, 0.12);
	color: #ffffff;
}

@media (max-width: 1280px) {
	.form-content-grid {
		grid-template-columns: 1fr;
	}

	.form-side-card {
		position: static;
	}
}

@media (max-width: 900px) {
	.form-grid-two,
	.side-meta-grid {
		grid-template-columns: 1fr;
	}

	.form-header-card {
		flex-direction: column;
	}

	.form-overview-tags {
		justify-content: flex-start;
	}
}
</style>
