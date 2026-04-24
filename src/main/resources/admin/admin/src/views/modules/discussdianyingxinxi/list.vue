<template>
	<div class="main-content admin-comment-list-page">
		<template v-if="showFlag">
			<section class="comment-hero">
				<div class="hero-copy">
					<div class="hero-kicker">评论管理</div>
					<h1>Comment Console</h1>
					<p>管理电影评论、回复内容与置顶状态，保留现有查询、返回、回复、删除和分页能力。</p>
				</div>
				<div class="hero-actions">
					<el-button class="hero-secondary-btn" type="button" @click="backClick()">
						<i class="el-icon-back"></i>
						<span>返回上一页</span>
					</el-button>
					<el-button
						v-if="isAuth('discussdianyingxinxi','新增')"
						class="hero-add-btn"
						type="button"
						@click="addOrUpdateHandler()"
					>
						<i class="el-icon-plus"></i>
						<span>新增评论</span>
					</el-button>
				</div>
			</section>

			<section class="stats-grid">
				<div class="stat-card">
					<p>总评论数</p>
					<div class="stat-main">
						<strong>{{ totalPage || 0 }}</strong>
						<span>全部记录</span>
					</div>
				</div>
				<div class="stat-card">
					<p>当前页置顶</p>
					<div class="stat-main">
						<strong>{{ topCount }}</strong>
						<span>本页统计</span>
					</div>
				</div>
				<div class="stat-card">
					<p>当前页已回复</p>
					<div class="stat-main">
						<strong>{{ repliedCount }}</strong>
						<span>本页统计</span>
					</div>
				</div>
				<div class="stat-card">
					<p>当前页均分</p>
					<div class="stat-main">
						<strong>{{ averageScore }}</strong>
						<span>五星制</span>
					</div>
				</div>
			</section>

			<section class="table-shell">
				<div class="table-toolbar">
					<div class="toolbar-left">
						<div class="filter-box">
							<label>用户名</label>
							<el-input
								v-model="searchForm.nickname"
								placeholder="输入评论用户"
								clearable
								@keydown.enter.native="search()"
							></el-input>
						</div>
						<div class="filter-box filter-box-wide">
							<label>评论内容</label>
							<el-input
								v-model="searchForm.content"
								placeholder="输入评论关键词"
								clearable
								@keydown.enter.native="search()"
							></el-input>
						</div>
						<el-button class="toolbar-btn primary-btn" @click="search()">查询</el-button>
						<el-button class="toolbar-btn ghost-btn" @click="resetSearch()">重置</el-button>
					</div>
					<div class="toolbar-right">
						<div class="selection-chip">已选 {{ dataListSelections.length }} 项</div>
						<el-button
							v-if="isAuth('discussdianyingxinxi','删除')"
							class="toolbar-btn danger-btn"
							:disabled="!dataListSelections.length"
							@click="deleteHandler()"
						>
							批量删除
						</el-button>
					</div>
				</div>

				<div class="table-wrap" v-loading="dataListLoading">
					<el-table
						class="comment-table"
						:data="dataList"
						:border="false"
						:stripe="false"
						@selection-change="selectionChangeHandler"
					>
						<el-table-column type="selection" align="center" width="56"></el-table-column>
						<el-table-column label="评论用户" min-width="220">
							<template slot-scope="scope">
								<div class="identity-cell">
									<div class="identity-avatar">
										{{ getNameFallback(scope.row) }}
									</div>
									<div class="identity-copy">
										<p>{{ scope.row.nickname || '匿名用户' }}</p>
										<span>#{{ scope.row.id || 'N/A' }}</span>
									</div>
								</div>
							</template>
						</el-table-column>
						<el-table-column label="评论内容" min-width="320">
							<template slot-scope="scope">
								<div class="comment-block">
									<div class="rich-text ql-snow">
										<div class="ql-editor" v-html="scope.row.content || '暂无评论内容'"></div>
									</div>
									<div class="score-row">
										<el-rate
											v-model="scope.row.score"
											:max="5"
											:allow-half="false"
											:show-text="false"
											:colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
											void-color="#4f5b79"
											disabled
										></el-rate>
										<span>{{ formatScore(scope.row.score) }}</span>
									</div>
								</div>
							</template>
						</el-table-column>
						<el-table-column label="回复状态" min-width="260">
							<template slot-scope="scope">
								<div class="tag-column">
									<span class="role-pill" :class="scope.row.reply ? 'is-gold' : 'is-default'">
										{{ scope.row.reply ? '已回复' : '待回复' }}
									</span>
									<div class="reply-content ql-snow" v-if="scope.row.reply">
										<div class="ql-editor" v-html="scope.row.reply"></div>
									</div>
									<div v-else class="tag-subline">当前评论暂无后台回复</div>
								</div>
							</template>
						</el-table-column>
						<el-table-column label="置顶状态" min-width="180">
							<template slot-scope="scope">
								<div class="switch-column">
									<span class="status-text" :class="scope.row.istop === 1 ? 'is-active' : ''">
										{{ scope.row.istop === 1 ? '已置顶' : '未置顶' }}
									</span>
									<el-switch
										v-model="scope.row.istop"
										:active-value="1"
										:inactive-value="0"
										active-color="#ffc639"
										inactive-color="#4f5b79"
										@change="(e) => discussistopChange(e, scope.row)"
									></el-switch>
								</div>
							</template>
						</el-table-column>
						<el-table-column label="操作" width="220" align="right">
							<template slot-scope="scope">
								<div class="action-group">
									<button
										v-if="isAuth('discussdianyingxinxi','修改')"
										type="button"
										class="action-icon"
										title="修改"
										@click="addOrUpdateHandler(scope.row.id)"
									>
										<i class="el-icon-edit"></i>
									</button>
									<button
										v-if="isAuth('discussdianyingxinxi','查看评论')"
										type="button"
										class="action-icon action-icon-text"
										title="查看评论"
										@click="disscussListHandler(scope.row.id)"
									>
										<span>子</span>
									</button>
									<button
										type="button"
										class="action-icon action-icon-text"
										title="回复"
										@click="addOrUpdateHandler(scope.row.id)"
									>
										<span>回</span>
									</button>
									<button
										v-if="isAuth('discussdianyingxinxi','删除')"
										type="button"
										class="action-icon action-icon-danger"
										title="删除"
										@click="deleteHandler(scope.row.id, scope.row.refid)"
									>
										<i class="el-icon-delete"></i>
									</button>
								</div>
							</template>
						</el-table-column>
					</el-table>

					<div v-if="!dataListLoading && !dataList.length" class="empty-block">
						<div class="empty-title">暂无评论记录</div>
						<p>当前筛选条件下没有匹配结果，可以调整条件后重新查询。</p>
					</div>
				</div>

				<div class="pagination-bar">
					<p>当前展示第 {{ pageIndex }} 页，共 {{ totalPage || 0 }} 条评论记录</p>
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
				</div>
			</section>
		</template>

		<add-or-update v-if="addOrUpdateFlag" :parent="this" ref="addOrUpdate"></add-or-update>
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
				nickname: '',
				content: ''
			},
			dataList: [],
			pageIndex: 1,
			pageSize: 10,
			totalPage: 0,
			dataListLoading: false,
			dataListSelections: [],
			showFlag: true,
			addOrUpdateFlag: false,
			layouts: ['prev', 'pager', 'next', 'sizes']
		}
	},
	computed: {
		topCount() {
			return this.dataList.filter(function(item) {
				return Number(item.istop) === 1
			}).length
		},
		repliedCount() {
			return this.dataList.filter(function(item) {
				return !!(item.reply && String(item.reply).replace(/<[^>]+>/g, '').trim())
			}).length
		},
		averageScore() {
			if (!this.dataList.length) {
				return '0.0'
			}
			var total = this.dataList.reduce(function(sum, item) {
				return sum + Number(item.score || 0)
			}, 0)
			return (total / this.dataList.length).toFixed(1)
		}
	},
	created() {
		this.init()
		this.getDataList()
	},
	methods: {
		contentStyleChange() {},
		init() {},
		getNameFallback(row) {
			if (row && row.nickname) {
				return row.nickname.substring(0, 1)
			}
			return '评'
		},
		formatScore(value) {
			return Number(value || 0).toFixed(1)
		},
		search() {
			this.pageIndex = 1
			this.getDataList()
		},
		resetSearch() {
			this.searchForm = {
				nickname: '',
				content: ''
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
				order: 'desc',
				refid: this.$route.query.refid
			}
			if (this.searchForm.nickname !== '' && this.searchForm.nickname !== undefined) {
				params.nickname = '%' + this.searchForm.nickname + '%'
			}
			if (this.searchForm.content !== '' && this.searchForm.content !== undefined) {
				params.content = '%' + this.searchForm.content + '%'
			}
			this.$http({
				url: 'discussdianyingxinxi/page',
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
			this.crossAddOrUpdateFlag = false
			if (type !== 'info' && type !== 'msg') {
				type = 'else'
			}
			this.$nextTick(() => {
				this.$refs.addOrUpdate.init(id, type)
			})
		},
		disscussListHandler(id) {
			this.$router.push({ path: '/discussdiscussdianyingxinxi', query: { refid: id } })
		},
		backClick() {
			history.back()
		},
		discussistopChange(e, row) {
			this.$http({
				url: 'discussdianyingxinxi/update',
				method: 'post',
				data: row
			}).then(() => {})
		},
		async deleteHandler(id, refid) {
			var ids = id ? [Number(id)] : this.dataListSelections.map(function(item) {
				return Number(item.id)
			})
			await this.$confirm('确定进行[' + (id ? '删除' : '批量删除') + ']操作?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(async () => {
				await this.$http({
					url: 'discussdianyingxinxi/delete',
					method: 'post',
					data: ids
				}).then(async ({ data }) => {
					if (data && data.code === 0) {
						if (refid) {
							this.$http({
								url: 'dianyingxinxi/info/' + refid,
								method: 'get'
							}).then(res => {
								if (res.data && res.data.code === 0) {
									res.data.data.discussnum = res.data.data.discussnum - Number(ids.length)
									this.$http({
										url: 'dianyingxinxi/update',
										method: 'post',
										data: res.data.data
									}).then(res1 => {
										if (res1.data && res1.data.code === 0) {
											this.$message({
												message: '操作成功',
												type: 'success',
												duration: 1500,
												onClose: () => {
													this.search()
												}
											})
										}
									})
								}
							})
						} else {
							this.$message({
								message: '操作成功',
								type: 'success',
								duration: 1500,
								onClose: () => {
									this.search()
								}
							})
						}
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
.admin-comment-list-page {
	min-height: 100%;
	padding: 18px 22px 28px;
	background:
		radial-gradient(circle at top left, rgba(255, 198, 57, 0.07), transparent 20%),
		linear-gradient(180deg, #0b1326 0%, #10192f 100%);
	color: #dae2fd;
}

.comment-hero {
	display: flex;
	align-items: flex-end;
	justify-content: space-between;
	gap: 20px;
	margin-bottom: 24px;
}

.hero-kicker {
	font-size: 11px;
	font-weight: 700;
	letter-spacing: 0.24em;
	text-transform: uppercase;
	color: rgba(255, 198, 57, 0.78);
}

.hero-copy h1 {
	margin: 10px 0 8px;
	font-family: 'Epilogue', 'Avenir Next', sans-serif;
	font-size: 46px;
	font-weight: 800;
	line-height: 1;
	color: #f3f6ff;
}

.hero-copy p {
	margin: 0;
	max-width: 720px;
	font-size: 15px;
	line-height: 1.8;
	color: rgba(218, 226, 253, 0.64);
}

.hero-actions {
	display: flex;
	align-items: center;
	gap: 12px;
	flex-wrap: wrap;
}

.hero-add-btn,
.hero-secondary-btn {
	display: inline-flex;
	align-items: center;
	gap: 10px;
	height: 54px;
	padding: 0 24px;
	border: none;
	border-radius: 999px;
	font-size: 14px;
	font-weight: 800;
}

.hero-add-btn {
	background: linear-gradient(135deg, #ffc639 0%, #e1aa12 100%);
	color: #0b1326;
	box-shadow: 0 15px 30px rgba(255, 198, 57, 0.26);
}

.hero-secondary-btn {
	background: rgba(45, 52, 73, 0.92);
	color: #dae2fd;
}

.stats-grid {
	display: grid;
	grid-template-columns: repeat(4, minmax(0, 1fr));
	gap: 16px;
	margin-bottom: 22px;
}

.stat-card {
	padding: 22px 24px;
	border-radius: 28px;
	background: rgba(19, 27, 46, 0.92);
	box-shadow: 0 18px 40px rgba(2, 6, 23, 0.2);
}

.stat-card p {
	margin: 0 0 12px;
	font-size: 11px;
	font-weight: 800;
	letter-spacing: 0.16em;
	text-transform: uppercase;
	color: rgba(255, 198, 57, 0.72);
}

.stat-main {
	display: flex;
	align-items: baseline;
	gap: 10px;
}

.stat-main strong {
	font-family: 'Epilogue', 'Avenir Next', sans-serif;
	font-size: 34px;
	font-weight: 800;
	color: #f3f6ff;
}

.stat-main span {
	font-size: 12px;
	font-weight: 700;
	color: rgba(218, 226, 253, 0.46);
}

.table-shell {
	border-radius: 32px;
	overflow: hidden;
	background: rgba(19, 27, 46, 0.94);
	box-shadow: 0 24px 54px rgba(0, 0, 0, 0.28);
}

.table-toolbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 18px;
	padding: 24px;
	background: rgba(23, 31, 51, 0.9);
}

.toolbar-left,
.toolbar-right {
	display: flex;
	align-items: flex-end;
	gap: 12px;
	flex-wrap: wrap;
}

.filter-box {
	display: flex;
	flex-direction: column;
	gap: 8px;
	min-width: 200px;
}

.filter-box-wide {
	min-width: 280px;
}

.filter-box label {
	font-size: 11px;
	font-weight: 700;
	letter-spacing: 0.14em;
	text-transform: uppercase;
	color: rgba(208, 197, 175, 0.78);
}

.selection-chip {
	display: inline-flex;
	align-items: center;
	height: 42px;
	padding: 0 16px;
	border-radius: 999px;
	background: rgba(45, 52, 73, 0.92);
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.08em;
	text-transform: uppercase;
	color: rgba(218, 226, 253, 0.78);
}

.toolbar-btn {
	height: 42px;
	padding: 0 18px;
	border: none;
	border-radius: 999px;
	font-size: 12px;
	font-weight: 800;
	letter-spacing: 0.08em;
	text-transform: uppercase;
}

.primary-btn {
	background: linear-gradient(135deg, #ffc639 0%, #e1aa12 100%);
	color: #3f2e00;
}

.ghost-btn {
	background: rgba(45, 52, 73, 0.92);
	color: #dae2fd;
}

.danger-btn {
	background: linear-gradient(135deg, #ff7b72 0%, #ef4444 100%);
	color: #fff4f2;
}

.table-wrap {
	min-height: 320px;
	overflow: hidden;
}

.identity-cell {
	display: flex;
	align-items: center;
	gap: 14px;
}

.identity-avatar {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 52px;
	height: 52px;
	border-radius: 999px;
	border: 1px solid rgba(255, 198, 57, 0.26);
	background: rgba(45, 52, 73, 0.92);
	font-family: 'Epilogue', 'Avenir Next', sans-serif;
	font-size: 20px;
	font-weight: 800;
	color: #ffc639;
}

.identity-copy p,
.identity-copy span {
	margin: 0;
}

.identity-copy p {
	font-size: 15px;
	font-weight: 800;
	color: #f3f6ff;
}

.identity-copy span {
	font-size: 12px;
	font-weight: 600;
	color: rgba(218, 226, 253, 0.52);
}

.comment-block {
	display: flex;
	flex-direction: column;
	gap: 12px;
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

.tag-column {
	display: flex;
	flex-direction: column;
	align-items: flex-start;
	gap: 10px;
}

.role-pill {
	display: inline-flex;
	align-items: center;
	height: 28px;
	padding: 0 12px;
	border-radius: 999px;
	border: 1px solid rgba(218, 226, 253, 0.12);
	background: rgba(45, 52, 73, 0.86);
	font-size: 10px;
	font-weight: 800;
	letter-spacing: 0.14em;
	text-transform: uppercase;
}

.role-pill.is-gold {
	border-color: rgba(255, 198, 57, 0.2);
	background: rgba(255, 198, 57, 0.1);
	color: #ffc639;
}

.role-pill.is-default {
	color: rgba(218, 226, 253, 0.74);
}

.tag-subline,
.reply-content {
	font-size: 13px;
	font-weight: 600;
	line-height: 1.7;
	color: rgba(218, 226, 253, 0.66);
}

.switch-column {
	display: flex;
	flex-direction: column;
	align-items: flex-start;
	gap: 12px;
}

.status-text {
	font-size: 12px;
	font-weight: 700;
	color: rgba(218, 226, 253, 0.62);
}

.status-text.is-active {
	color: #ffc639;
}

.action-group {
	display: flex;
	justify-content: flex-end;
	gap: 10px;
	flex-wrap: wrap;
}

.action-icon {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 38px;
	height: 38px;
	border: none;
	border-radius: 12px;
	background: transparent;
	color: rgba(218, 226, 253, 0.42);
	font-size: 18px;
	cursor: pointer;
	transition: background 0.2s ease, color 0.2s ease;
}

.action-icon-text span {
	font-size: 13px;
	font-weight: 800;
}

.action-icon:hover {
	background: rgba(255, 198, 57, 0.1);
	color: #ffc639;
}

.action-icon-danger:hover {
	background: rgba(239, 68, 68, 0.1);
	color: #ff8c84;
}

.pagination-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 18px;
	padding: 22px 24px;
	background: rgba(23, 31, 51, 0.95);
}

.pagination-bar p {
	margin: 0;
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.08em;
	text-transform: uppercase;
	color: rgba(218, 226, 253, 0.42);
}

.empty-block {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 96px 20px;
	text-align: center;
}

.empty-title {
	margin-bottom: 10px;
	font-family: 'Epilogue', 'Avenir Next', sans-serif;
	font-size: 28px;
	font-weight: 800;
	color: #f3f6ff;
}

.empty-block p {
	margin: 0;
	max-width: 420px;
	line-height: 1.8;
	color: rgba(218, 226, 253, 0.64);
}

.rich-text,
.reply-content {
	width: 100%;
}

.admin-comment-list-page .el-input,
.admin-comment-list-page .el-select {
	width: 100%;
}

.admin-comment-list-page ::v-deep .el-input__inner,
.admin-comment-list-page ::v-deep .el-select .el-input__inner {
	height: 44px;
	line-height: 44px;
	padding: 0 16px;
	border: 1px solid rgba(148, 160, 191, 0.16);
	border-radius: 14px;
	background: rgba(45, 52, 73, 0.9);
	color: #dae2fd;
}

.admin-comment-list-page ::v-deep .el-input__inner::placeholder {
	color: rgba(148, 160, 191, 0.64);
}

.admin-comment-list-page ::v-deep .el-input__inner:focus,
.admin-comment-list-page ::v-deep .el-select .el-input.is-focus .el-input__inner {
	border-color: rgba(255, 198, 57, 0.64);
}

.admin-comment-list-page ::v-deep .el-table,
.admin-comment-list-page ::v-deep .el-table__expanded-cell,
.admin-comment-list-page ::v-deep .el-table tr,
.admin-comment-list-page ::v-deep .el-table th,
.admin-comment-list-page ::v-deep .el-table td {
	background: transparent;
}

.admin-comment-list-page ::v-deep .el-table::before {
	background: rgba(255, 255, 255, 0.06);
}

.admin-comment-list-page ::v-deep .el-table__header-wrapper th {
	padding: 16px 0;
	background: rgba(19, 27, 46, 0.72);
	border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-comment-list-page ::v-deep .el-table__header-wrapper th .cell {
	font-size: 11px;
	font-weight: 800;
	letter-spacing: 0.14em;
	text-transform: uppercase;
	color: #ffc639;
}

.admin-comment-list-page ::v-deep .el-table__body-wrapper td {
	padding: 18px 0;
	border-bottom: 1px solid rgba(255, 255, 255, 0.05);
	vertical-align: top;
}

.admin-comment-list-page ::v-deep .el-table__header-wrapper,
.admin-comment-list-page ::v-deep .el-table__body-wrapper {
	scrollbar-width: thin;
	scrollbar-color: rgba(255, 198, 57, 0.55) rgba(255, 255, 255, 0.04);
}

.admin-comment-list-page ::v-deep .el-table__body-wrapper::-webkit-scrollbar,
.admin-comment-list-page ::v-deep .el-table__header-wrapper::-webkit-scrollbar {
	height: 10px;
	width: 10px;
}

.admin-comment-list-page ::v-deep .el-table__body-wrapper::-webkit-scrollbar-track,
.admin-comment-list-page ::v-deep .el-table__header-wrapper::-webkit-scrollbar-track {
	border-radius: 999px;
	background: rgba(255, 255, 255, 0.05);
}

.admin-comment-list-page ::v-deep .el-table__body-wrapper::-webkit-scrollbar-thumb,
.admin-comment-list-page ::v-deep .el-table__header-wrapper::-webkit-scrollbar-thumb {
	border-radius: 999px;
	background: linear-gradient(135deg, rgba(255, 198, 57, 0.92), rgba(225, 170, 18, 0.86));
	border: 2px solid rgba(19, 27, 46, 0.92);
}

.admin-comment-list-page ::v-deep .el-table__body-wrapper .cell,
.admin-comment-list-page ::v-deep .el-table__header-wrapper .cell {
	white-space: normal;
	word-break: break-word;
}

.admin-comment-list-page ::v-deep .el-table__body-wrapper tr:hover td {
	background: rgba(34, 42, 61, 0.86);
}

.admin-comment-list-page ::v-deep .el-checkbox__inner {
	border-color: rgba(148, 160, 191, 0.4);
	background: rgba(11, 19, 38, 0.82);
}

.admin-comment-list-page ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner,
.admin-comment-list-page ::v-deep .el-checkbox__input.is-indeterminate .el-checkbox__inner {
	background-color: #ffc639;
	border-color: #ffc639;
}

.admin-comment-list-page ::v-deep .el-rate__icon {
	margin-right: 2px;
	font-size: 16px;
}

.admin-comment-list-page ::v-deep .el-switch__core {
	border: none;
	background: #4f5b79 !important;
}

.admin-comment-list-page ::v-deep .el-switch.is-checked .el-switch__core {
	background: #ffc639 !important;
}

.admin-comment-list-page ::v-deep .el-pagination {
	margin-left: auto;
}

.admin-comment-list-page ::v-deep .el-pagination .btn-prev,
.admin-comment-list-page ::v-deep .el-pagination .btn-next,
.admin-comment-list-page ::v-deep .el-pagination .el-pager li {
	min-width: 38px;
	height: 38px;
	line-height: 38px;
	border-radius: 999px;
	border: 1px solid rgba(255, 255, 255, 0.08);
	background: rgba(19, 27, 46, 0.86);
	color: rgba(218, 226, 253, 0.72);
}

.admin-comment-list-page ::v-deep .el-pagination .el-pager li.active,
.admin-comment-list-page ::v-deep .el-pagination .btn-prev:hover,
.admin-comment-list-page ::v-deep .el-pagination .btn-next:hover,
.admin-comment-list-page ::v-deep .el-pagination .el-pager li:hover {
	background: #ffc639;
	color: #0b1326;
}

.admin-comment-list-page ::v-deep .ql-editor {
	padding: 0;
	min-height: auto;
	line-height: 1.75;
	color: rgba(218, 226, 253, 0.76);
}

.admin-comment-list-page ::v-deep .ql-editor p {
	margin: 0;
}

@media (max-width: 1400px) {
	.stats-grid {
		grid-template-columns: repeat(2, minmax(0, 1fr));
	}

	.comment-hero,
	.table-toolbar,
	.pagination-bar {
		flex-direction: column;
		align-items: flex-start;
	}

	.toolbar-left,
	.toolbar-right {
		width: 100%;
	}
}

@media (max-width: 900px) {
	.admin-comment-list-page {
		padding: 16px;
	}

	.stats-grid {
		grid-template-columns: minmax(0, 1fr);
	}

	.hero-copy h1 {
		font-size: 32px;
	}

	.filter-box,
	.filter-box-wide {
		width: 100%;
		min-width: 0;
	}
}
</style>
