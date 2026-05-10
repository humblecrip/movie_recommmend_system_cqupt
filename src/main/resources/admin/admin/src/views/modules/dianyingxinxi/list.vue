<template>
	<div class="main-content admin-film-library">
		<template v-if="showFlag">
			<section class="library-hero">
				<div class="hero-copy">
					<div class="hero-eyebrow">后台片库</div>
					<h1>Film Library</h1>
					<p>当前管理 {{ totalPage || 0 }} 部电影，保留现有新增、编辑、删除、评论和分页能力。</p>
				</div>
				<div class="hero-toolbar">
					<el-select v-model="searchForm.dianyingleixing" placeholder="全部类型" clearable>
						<el-option v-for="(item,index) in dianyingleixingOptions" :key="index" :label="item" :value="item"></el-option>
					</el-select>
					<el-select v-model="searchForm.quyu" placeholder="全部区域" clearable>
						<el-option v-for="(item,index) in quyuOptions" :key="index" :label="item" :value="item"></el-option>
					</el-select>
					<el-button class="hero-query-btn" @click="search()">立即筛选</el-button>
				</div>
			</section>

			<section class="filter-dock">
				<div class="filter-main-grid">
					<div class="filter-box">
						<label>电影名称</label>
						<el-input
							v-model="searchForm.dianyingmingcheng"
							placeholder="输入电影名称"
							clearable
							@keydown.enter.native="search()"
						></el-input>
					</div>
					<div class="filter-box">
						<label>导演</label>
						<el-input
							v-model="searchForm.daoyan"
							placeholder="输入导演名称"
							clearable
							@keydown.enter.native="search()"
						></el-input>
					</div>
					<div class="filter-box">
						<label>主演</label>
						<el-input
							v-model="searchForm.zhuyan"
							placeholder="输入主演名称"
							clearable
							@keydown.enter.native="search()"
						></el-input>
					</div>
				</div>

				<div class="filter-actions">
					<div class="selection-hint">已选 {{ dataListSelections.length }} 项</div>
					<el-button class="ghost-btn" @click="resetSearch()">重置</el-button>
					<el-button class="accent-btn" @click="search()">查询</el-button>
					<el-button
						v-if="isAuth('dianyingxinxi','新增')"
						class="accent-btn accent-btn-secondary"
						@click="addOrUpdateHandler()"
					>
						新增电影
					</el-button>
					<el-button
						v-if="isAuth('dianyingxinxi','删除')"
						class="danger-btn"
						:disabled="dataListSelections.length ? false : true"
						@click="deleteHandler()"
					>
						批量删除
					</el-button>
				</div>
			</section>

			<section class="film-list-section">
				<div class="section-head">
					<div>
						<div class="section-kicker">电影列表</div>
						<h2>Movie Collection</h2>
					</div>
					<div class="section-stat">第 {{ pageIndex }} 页 · 共 {{ totalPage || 0 }} 条记录</div>
				</div>

				<div v-loading="dataListLoading" class="film-list-wrap">
					<div v-if="isAuth('dianyingxinxi','查看') && dataList.length" class="film-card-list">
						<article v-for="item in dataList" :key="item.id" class="film-card">
							<div class="film-card-inner">
								<div class="film-poster-shell" @click="imgPreView(getMoviePoster(item))">
									<img v-if="getMoviePoster(item)" :src="getMoviePoster(item)" alt="movie poster">
									<div v-else class="poster-empty">暂无海报</div>
									<div class="poster-overlay"></div>
								</div>

								<div class="film-body">
									<div class="film-top-row">
										<div class="film-heading">
											<div class="film-badges">
												<el-checkbox
													class="film-selector"
													:value="isSelected(item.id)"
													@change="toggleCardSelection(item, $event)"
												></el-checkbox>
												<span class="status-badge" :class="getStatusClass(item)">
													{{ getStatusLabel(item) }}
												</span>
												<span class="minor-meta">{{ item.shangyingshijian || '未设置上映时间' }}</span>
											</div>
											<h3>{{ item.dianyingmingcheng || '未命名电影' }}</h3>
											<p>{{ getMovieSummary(item) }}</p>
										</div>

										<div class="film-actions-group">
											<button
												v-if="isAuth('dianyingxinxi','查看')"
												type="button"
												class="icon-action"
												title="查看"
												@click="addOrUpdateHandler(item.id, 'info')"
											>
												<i class="el-icon-view"></i>
											</button>
											<button
												v-if="isAuth('dianyingxinxi','修改')"
												type="button"
												class="icon-action"
												title="修改"
												@click="addOrUpdateHandler(item.id)"
											>
												<i class="el-icon-edit"></i>
											</button>
											<button
												v-if="isAuth('dianyingxinxi','查看评论')"
												type="button"
												class="icon-action icon-action-comment"
												title="查看评论"
												@click="disscussListHandler(item.id)"
											>
												<span>评</span>
											</button>
											<button
												v-if="isAuth('dianyingxinxi','删除')"
												type="button"
												class="icon-action icon-action-danger"
												title="删除"
												@click="deleteHandler(item.id)"
											>
												<i class="el-icon-delete"></i>
											</button>
										</div>
									</div>

									<div class="film-facts">
										<div class="fact-item">
											<span>导演</span>
											<strong>{{ item.daoyan || '待补充' }}</strong>
										</div>
										<div class="fact-item">
											<span>主演</span>
											<strong>{{ item.zhuyan || '待补充' }}</strong>
										</div>
										<div class="fact-item">
											<span>区域</span>
											<strong>{{ item.quyu || '待补充' }}</strong>
										</div>
										<div class="fact-item">
											<span>上映日期</span>
											<strong>{{ item.shangyingshijian || '待补充' }}</strong>
										</div>
									</div>

									<div class="film-metrics">
										<div class="metric-item metric-score">
											<i class="el-icon-star-on"></i>
											<strong>{{ formatScore(item.totalscore) }}</strong>
											<span>评分</span>
										</div>
										<div class="metric-item">
											<strong>{{ formatNumber(item.clicknum) }}</strong>
											<span>点击</span>
										</div>
										<div class="metric-item">
											<strong>{{ formatNumber(item.discussnum) }}</strong>
											<span>评论</span>
										</div>
										<div class="metric-item">
											<strong>{{ formatNumber(item.storeupnum) }}</strong>
											<span>收藏</span>
										</div>
									</div>
								</div>
							</div>
						</article>
					</div>

					<div v-else-if="!dataListLoading" class="empty-block">
						<div class="empty-title">暂无电影数据</div>
						<p>当前筛选条件下没有匹配结果，可以调整筛选项后重新查询。</p>
					</div>
				</div>

				<el-pagination
					@size-change="sizeChangeHandle"
					@current-change="currentChangeHandle"
					:current-page="pageIndex"
					background
					:page-sizes="[10, 50, 100, 200]"
					:page-size="pageSize"
					:layout="layouts.join()"
					:total="totalPage"
					prev-text="<"
					next-text=">"
					:hide-on-single-page="false"
				></el-pagination>
			</section>
		</template>

		<add-or-update v-if="addOrUpdateFlag" :parent="this" ref="addOrUpdate"></add-or-update>

		<el-dialog title="海报预览" :visible.sync="previewVisible" width="50%">
			<img :src="previewImg" alt="" style="width: 100%;">
		</el-dialog>
	</div>
</template>

<script>
import AddOrUpdate from './add-or-update'

export default {
	components: {
		AddOrUpdate
	},
	data() {
		return {
			searchForm: {
				key: '',
				dianyingmingcheng: '',
				dianyingleixing: '',
				quyu: '',
				daoyan: '',
				zhuyan: ''
			},
			dataList: [],
			pageIndex: 1,
			pageSize: 10,
			totalPage: 0,
			dataListLoading: false,
			dataListSelections: [],
			showFlag: true,
			addOrUpdateFlag: false,
			layouts: ['total', 'prev', 'pager', 'next', 'sizes'],
			previewImg: '',
			previewVisible: false,
			dianyingleixingOptions: [],
			quyuOptions: '大陆,美国,韩国,日本,中国香港,中国台湾,泰国,印度,法国,英国,俄罗斯,意大利,西班牙,德国,波兰,澳大利亚,伊朗,其他'.split(',')
		}
	},
	created() {
		this.init()
		this.getDataList()
	},
	methods: {
		init() {
			this.loadFilterOptions()
		},
		loadFilterOptions() {
			this.$http({
				url: 'appmovie/types',
				method: 'get'
			}).then(({ data }) => {
				if (data && data.code === 0) {
					this.dianyingleixingOptions = (data.data || []).map(function(item) {
						return item.typeName
					})
				}
			})
		},
		imgPreView(url) {
			this.previewImg = url
			this.previewVisible = true
		},
		formatNumber(value) {
			return Number(value || 0)
		},
		formatScore(value) {
			var score = Number(value || 0)
			return score ? score.toFixed(1) : '0.0'
		},
		getMoviePoster(row) {
			if (row && row.haibao) {
				if (row.haibao.substring(0, 4) === 'http' && row.haibao.split(',w').length > 1) {
					return row.haibao
				}
				if (row.haibao.substring(0, 4) === 'http') {
					return row.haibao.split(',')[0]
				}
				return this.$base.url + row.haibao.split(',')[0]
			}
			return ''
		},
		getMovieSummary(row) {
			if (row && row.juqingjianjie) {
				return row.juqingjianjie
			}
			if (row && row.dianyingxiangqing) {
				return row.dianyingxiangqing.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim() || '暂无剧情简介'
			}
			return '暂无剧情简介'
		},
		getStatusLabel(row) {
			if (row && row.shangyingshijian) {
				return '已发布'
			}
			return '待完善'
		},
		getStatusClass(row) {
			return row && row.shangyingshijian ? 'is-live' : 'is-draft'
		},
		isSelected(id) {
			return this.dataListSelections.some(function(item) {
				return Number(item.id) === Number(id)
			})
		},
		toggleCardSelection(row, checked) {
			var current = this.dataListSelections.slice()
			var exists = current.findIndex(function(item) {
				return Number(item.id) === Number(row.id)
			})
			if (checked && exists === -1) {
				current.push(row)
			}
			if (!checked && exists !== -1) {
				current.splice(exists, 1)
			}
			this.dataListSelections = current
		},
		search() {
			this.pageIndex = 1
			this.getDataList()
		},
		resetSearch() {
			this.searchForm = {
				key: '',
				dianyingmingcheng: '',
				dianyingleixing: '',
				quyu: '',
				daoyan: '',
				zhuyan: ''
			}
			this.dataListSelections = []
			this.search()
		},
		getDataList() {
			this.dataListLoading = true
			var params = {
				page: this.pageIndex,
				limit: this.pageSize,
				sort: 'id',
				order: 'desc'
			}
			if (this.searchForm.dianyingmingcheng !== '' && this.searchForm.dianyingmingcheng !== undefined) {
				params.dianyingmingcheng = '%' + this.searchForm.dianyingmingcheng + '%'
			}
			if (this.searchForm.dianyingleixing !== '' && this.searchForm.dianyingleixing !== undefined) {
				params.dianyingleixing = this.searchForm.dianyingleixing
			}
			if (this.searchForm.quyu !== '' && this.searchForm.quyu !== undefined) {
				params.quyu = this.searchForm.quyu
			}
			if (this.searchForm.daoyan !== '' && this.searchForm.daoyan !== undefined) {
				params.daoyan = '%' + this.searchForm.daoyan + '%'
			}
			if (this.searchForm.zhuyan !== '' && this.searchForm.zhuyan !== undefined) {
				params.zhuyan = '%' + this.searchForm.zhuyan + '%'
			}
			this.$http({
				url: 'dianyingxinxi/page',
				method: 'get',
				params: params
			}).then(({ data }) => {
				if (data && data.code === 0) {
					this.dataList = data.data.list
					this.totalPage = data.data.total
				} else {
					this.dataList = []
					this.totalPage = 0
				}
				this.dataListSelections = []
				this.dataListLoading = false
			})
		},
		sizeChangeHandle(val) {
			this.pageSize = val
			this.pageIndex = 1
			this.getDataList()
		},
		currentChangeHandle(val) {
			this.pageIndex = val
			this.getDataList()
		},
		selectionChangeHandler(val) {
			this.dataListSelections = val
		},
		addOrUpdateHandler(id, type) {
			this.showFlag = false
			this.addOrUpdateFlag = true
			if (type !== 'info' && type !== 'msg') {
				type = 'else'
			}
			this.$nextTick(() => {
				this.$refs.addOrUpdate.init(id, type)
			})
		},
		disscussListHandler(id) {
			this.$router.push({ path: '/discussdianyingxinxi', query: { refid: id } })
		},
		async deleteHandler(id) {
			var ids = id ? [Number(id)] : this.dataListSelections.map(function(item) {
				return Number(item.id)
			})
			await this.$confirm('确定进行[' + (id ? '删除' : '批量删除') + ']操作?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(async () => {
				await this.$http({
					url: 'dianyingxinxi/delete',
					method: 'post',
					data: ids
				}).then(async ({ data }) => {
					if (data && data.code === 0) {
						this.$message({
							message: '操作成功',
							type: 'success',
							duration: 1500,
							onClose: () => {
								this.search()
							}
						})
					} else {
						this.$message.error(data.msg)
					}
				})
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.admin-film-library {
	min-height: 100%;
	padding: 18px 22px 28px;
	background:
		radial-gradient(circle at top left, rgba(255, 198, 57, 0.08), transparent 22%),
		radial-gradient(circle at top right, rgba(63, 94, 251, 0.08), transparent 20%),
		linear-gradient(180deg, #0b1326 0%, #101a31 100%);
	color: #dae2fd;
}

.library-hero {
	display: flex;
	align-items: flex-end;
	justify-content: space-between;
	gap: 20px;
	padding: 10px 4px 0;
	margin-bottom: 18px;
}

.hero-copy {
	max-width: 620px;
}

.hero-eyebrow,
.section-kicker {
	font-size: 11px;
	font-weight: 700;
	letter-spacing: 0.28em;
	text-transform: uppercase;
	color: rgba(255, 198, 57, 0.78);
}

.hero-copy h1,
.section-head h2 {
	margin: 10px 0 8px;
	font-family: 'Epilogue', 'Avenir Next', sans-serif;
	font-size: 44px;
	font-weight: 900;
	line-height: 1;
	color: #f2f5ff;
}

.hero-copy p {
	margin: 0;
	font-size: 15px;
	font-weight: 600;
	color: rgba(208, 197, 175, 0.82);
}

.hero-toolbar {
	display: flex;
	align-items: center;
	gap: 12px;
	padding: 10px;
	border-radius: 20px;
	background: rgba(23, 31, 51, 0.92);
	box-shadow: 0 18px 40px rgba(4, 9, 20, 0.28);
	backdrop-filter: blur(24px);
}

.filter-dock {
	padding: 18px;
	margin-bottom: 22px;
	border-radius: 28px;
	background: rgba(23, 31, 51, 0.9);
	box-shadow: 0 20px 44px rgba(5, 10, 23, 0.24);
	backdrop-filter: blur(24px);
	animation: fade-up 0.45s ease;
}

.filter-main-grid {
	display: grid;
	grid-template-columns: repeat(3, minmax(0, 1fr));
	gap: 14px;
}

.filter-box {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.filter-box label {
	font-size: 11px;
	font-weight: 700;
	letter-spacing: 0.16em;
	text-transform: uppercase;
	color: rgba(208, 197, 175, 0.78);
}

.filter-actions {
	display: flex;
	align-items: center;
	gap: 10px;
	flex-wrap: wrap;
	margin-top: 14px;
}

.selection-hint {
	margin-right: auto;
	padding: 10px 14px;
	border-radius: 999px;
	background: rgba(45, 52, 73, 0.92);
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.08em;
	color: rgba(218, 226, 253, 0.78);
	text-transform: uppercase;
}

.hero-query-btn,
.accent-btn,
.ghost-btn,
.danger-btn {
	height: 42px;
	padding: 0 18px;
	border: none;
	border-radius: 999px;
	font-size: 12px;
	font-weight: 800;
	letter-spacing: 0.08em;
	text-transform: uppercase;
	transition: transform 0.25s ease, box-shadow 0.25s ease, background 0.25s ease;
}

.hero-query-btn,
.accent-btn {
	background: linear-gradient(135deg, #ffc639 0%, #e1aa12 100%);
	color: #3f2e00;
	box-shadow: 0 14px 28px rgba(225, 170, 18, 0.18);
}

.accent-btn-secondary {
	background: linear-gradient(135deg, #d8e3fb 0%, #bcc7de 100%);
	color: #111c2d;
	box-shadow: 0 14px 28px rgba(188, 199, 222, 0.18);
}

.ghost-btn {
	background: rgba(45, 52, 73, 0.92);
	color: #dae2fd;
}

.danger-btn {
	background: linear-gradient(135deg, #ff7b72 0%, #ef4444 100%);
	color: #fff4f2;
	box-shadow: 0 14px 28px rgba(239, 68, 68, 0.16);
}

.hero-query-btn:hover,
.accent-btn:hover,
.ghost-btn:hover,
.danger-btn:hover {
	transform: translateY(-1px);
}

.film-list-section {
	animation: fade-up 0.55s ease;
}

.section-head {
	display: flex;
	align-items: flex-end;
	justify-content: space-between;
	gap: 18px;
	margin-bottom: 18px;
	padding: 0 4px;
}

.section-head h2 {
	font-size: 36px;
}

.section-stat {
	padding: 12px 16px;
	border-radius: 999px;
	background: rgba(19, 27, 46, 0.92);
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.08em;
	color: rgba(208, 197, 175, 0.82);
	text-transform: uppercase;
}

.film-list-wrap {
	min-height: 220px;
}

.film-card-list {
	display: flex;
	flex-direction: column;
	gap: 18px;
}

.film-card {
	border-radius: 28px;
	background: rgba(34, 42, 61, 0.9);
	box-shadow: 0 20px 44px rgba(4, 9, 20, 0.2);
	overflow: hidden;
	transition: transform 0.3s ease, background 0.3s ease;
}

.film-card:hover {
	transform: translateY(-2px);
	background: rgba(40, 49, 71, 0.96);
}

.film-card-inner {
	display: flex;
	gap: 28px;
	padding: 24px;
}

.film-poster-shell {
	position: relative;
	flex: 0 0 156px;
	width: 156px;
	height: 228px;
	border-radius: 24px;
	overflow: hidden;
	background: rgba(11, 19, 38, 0.92);
	cursor: pointer;
	box-shadow: 0 18px 40px rgba(0, 0, 0, 0.32);
}

.film-poster-shell img {
	width: 100%;
	height: 100%;
	object-fit: cover;
	transition: transform 0.5s ease;
}

.film-card:hover .film-poster-shell img {
	transform: scale(1.06);
}

.poster-overlay {
	position: absolute;
	inset: 0;
	background: linear-gradient(180deg, rgba(11, 19, 38, 0.04) 0%, rgba(2, 6, 23, 0.52) 100%);
}

.poster-empty {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 100%;
	height: 100%;
	padding: 18px;
	font-size: 14px;
	font-weight: 700;
	color: rgba(218, 226, 253, 0.64);
	text-align: center;
}

.film-body {
	display: flex;
	flex: 1;
	flex-direction: column;
	justify-content: space-between;
	min-width: 0;
}

.film-top-row {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 18px;
}

.film-heading {
	min-width: 0;
}

.film-badges {
	display: flex;
	align-items: center;
	gap: 10px;
	margin-bottom: 12px;
}

.film-selector {
	margin-right: 2px;
}

.status-badge {
	display: inline-flex;
	align-items: center;
	padding: 4px 10px;
	border-radius: 999px;
	font-size: 10px;
	font-weight: 800;
	letter-spacing: 0.12em;
	text-transform: uppercase;
}

.status-badge.is-live {
	background: rgba(255, 198, 57, 0.12);
	color: #ffc639;
}

.status-badge.is-draft {
	background: rgba(188, 199, 222, 0.12);
	color: #bcc7de;
}

.minor-meta {
	font-size: 12px;
	font-weight: 700;
	color: rgba(208, 197, 175, 0.66);
}

.film-heading h3 {
	margin: 0 0 10px;
	font-family: 'Epilogue', 'Avenir Next', sans-serif;
	font-size: 34px;
	font-weight: 800;
	line-height: 1.1;
	color: #f2f5ff;
	transition: color 0.25s ease;
}

.film-card:hover .film-heading h3 {
	color: #ffc639;
}

.film-heading p {
	margin: 0;
	max-width: 780px;
	font-size: 14px;
	line-height: 1.75;
	color: rgba(218, 226, 253, 0.72);
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.film-actions-group {
	display: flex;
	gap: 10px;
}

.icon-action {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 42px;
	height: 42px;
	border: none;
	border-radius: 999px;
	background: rgba(45, 52, 73, 0.94);
	color: #dae2fd;
	font-size: 16px;
	cursor: pointer;
	transition: transform 0.2s ease, background 0.25s ease, color 0.25s ease;
}

.icon-action span {
	font-size: 13px;
	font-weight: 800;
}

.icon-action:hover {
	transform: translateY(-1px);
	background: #ffc639;
	color: #3f2e00;
}

.icon-action-comment:hover {
	background: #d8e3fb;
	color: #111c2d;
}

.icon-action-danger:hover {
	background: #ef4444;
	color: #fff4f2;
}

.film-facts {
	display: grid;
	grid-template-columns: repeat(4, minmax(0, 1fr));
	gap: 16px;
	padding: 20px 0 18px;
}

.fact-item {
	display: flex;
	flex-direction: column;
	gap: 6px;
}

.fact-item span {
	font-size: 10px;
	font-weight: 700;
	letter-spacing: 0.12em;
	text-transform: uppercase;
	color: rgba(148, 160, 191, 0.72);
}

.fact-item strong {
	font-size: 15px;
	font-weight: 700;
	line-height: 1.5;
	color: #f2f5ff;
	word-break: break-word;
}

.film-metrics {
	display: flex;
	align-items: center;
	gap: 26px;
	padding-top: 18px;
}

.metric-item {
	display: inline-flex;
	align-items: center;
	gap: 8px;
	color: rgba(218, 226, 253, 0.88);
}

.metric-item i {
	font-size: 14px;
	color: rgba(208, 197, 175, 0.76);
}

.metric-score i {
	color: #ffc639;
}

.metric-item strong {
	font-size: 16px;
	font-weight: 800;
}

.metric-item span {
	font-size: 10px;
	font-weight: 700;
	letter-spacing: 0.12em;
	text-transform: uppercase;
	color: rgba(148, 160, 191, 0.72);
}

.empty-block {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 88px 20px;
	border-radius: 28px;
	background: rgba(23, 31, 51, 0.88);
	text-align: center;
}

.empty-title {
	margin-bottom: 10px;
	font-family: 'Epilogue', 'Avenir Next', sans-serif;
	font-size: 28px;
	font-weight: 800;
	color: #f2f5ff;
}

.empty-block p {
	margin: 0;
	max-width: 420px;
	line-height: 1.8;
	color: rgba(218, 226, 253, 0.68);
}

.admin-film-library .el-input,
.admin-film-library .el-select {
	width: 100%;
}

.admin-film-library ::v-deep .el-input__inner,
.admin-film-library ::v-deep .el-select .el-input__inner {
	height: 44px;
	line-height: 44px;
	padding: 0 16px;
	border: 1px solid rgba(148, 160, 191, 0.16);
	border-radius: 14px;
	background: rgba(45, 52, 73, 0.9);
	color: #dae2fd;
}

.admin-film-library ::v-deep .el-input__inner::placeholder {
	color: rgba(148, 160, 191, 0.66);
}

.admin-film-library ::v-deep .el-select .el-input.is-focus .el-input__inner,
.admin-film-library ::v-deep .el-input__inner:focus {
	border-color: rgba(255, 198, 57, 0.64);
}

.admin-film-library ::v-deep .el-checkbox__inner {
	border-color: rgba(148, 160, 191, 0.4);
	background: rgba(11, 19, 38, 0.82);
}

.admin-film-library ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner,
.admin-film-library ::v-deep .el-checkbox__input.is-indeterminate .el-checkbox__inner {
	background-color: #ffc639;
	border-color: #ffc639;
}

.admin-film-library .el-pagination {
	margin-top: 26px;
	text-align: center;
}

.admin-film-library .el-pagination ::v-deep .btn-prev,
.admin-film-library .el-pagination ::v-deep .btn-next,
.admin-film-library .el-pagination ::v-deep .el-pager li {
	min-width: 40px;
	height: 40px;
	line-height: 40px;
	border-radius: 999px;
	border: none;
	background: rgba(19, 27, 46, 0.92);
	color: rgba(218, 226, 253, 0.72);
}

.admin-film-library .el-pagination ::v-deep .el-pager li.active,
.admin-film-library .el-pagination ::v-deep .btn-prev:hover,
.admin-film-library .el-pagination ::v-deep .btn-next:hover,
.admin-film-library .el-pagination ::v-deep .el-pager li:hover {
	background: #ffc639;
	color: #3f2e00;
}

@keyframes fade-up {
	from {
		opacity: 0;
		transform: translateY(10px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

@media (max-width: 1360px) {
	.library-hero,
	.section-head,
	.film-top-row {
		flex-direction: column;
		align-items: flex-start;
	}

	.hero-toolbar {
		width: 100%;
		flex-wrap: wrap;
	}

	.film-actions-group {
		width: 100%;
	}
}

@media (max-width: 1100px) {
	.filter-main-grid,
	.film-facts {
		grid-template-columns: repeat(2, minmax(0, 1fr));
	}

	.film-card-inner {
		flex-direction: column;
	}

	.film-poster-shell {
		width: 100%;
		max-width: 220px;
	}

	.film-metrics {
		flex-wrap: wrap;
	}
}

@media (max-width: 768px) {
	.admin-film-library {
		padding: 16px;
	}

	.hero-copy h1,
	.section-head h2,
	.film-heading h3 {
		font-size: 28px;
	}

	.filter-main-grid,
	.film-facts {
		grid-template-columns: minmax(0, 1fr);
	}

	.filter-actions {
		align-items: stretch;
	}

	.selection-hint {
		width: 100%;
		margin-right: 0;
	}

	.hero-toolbar {
		flex-direction: column;
		align-items: stretch;
	}
}
</style>
