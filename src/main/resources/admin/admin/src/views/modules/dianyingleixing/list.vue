<template>
	<div class="main-content admin-genre-list-page">
		<template v-if="showFlag">
			<section class="page-tools-card">
				<div class="page-tools-copy">
					<div class="page-tools-kicker">首页</div>
					<h1>电影类型</h1>
					<p>维护电影分类标签、支持类型检索，并继续复用现有新增、编辑、删除与分页逻辑。</p>
				</div>
				<div class="page-tools-actions">
					<el-button
						v-if="isAuth('dianyingleixing','新增')"
						class="hero-add-btn"
						type="button"
						@click="addOrUpdateHandler()"
					>
						<i class="el-icon-plus"></i>
						<span>新增类型</span>
					</el-button>
				</div>
			</section>

			<section class="stats-grid">
				<div class="stat-card">
					<p>总记录</p>
					<div class="stat-main">
						<strong>{{ totalPage || 0 }}</strong>
						<span>全部数据</span>
					</div>
				</div>
				<div class="stat-card">
					<p>当前页记录</p>
					<div class="stat-main">
						<strong>{{ dataList.length }}</strong>
						<span>本页展示</span>
					</div>
				</div>
				<div class="stat-card">
					<p>筛选关键词</p>
					<div class="stat-main">
						<strong>{{ searchForm.dianyingleixing || '全部' }}</strong>
						<span>当前条件</span>
					</div>
				</div>
				<div class="stat-card">
					<p>已选条目</p>
					<div class="stat-main">
						<strong>{{ dataListSelections.length }}</strong>
						<span>批量操作</span>
					</div>
				</div>
			</section>

			<section class="table-shell">
				<div class="table-toolbar">
					<div class="toolbar-left">
						<div class="filter-box">
							<label>电影类型</label>
							<el-input
								v-model="searchForm.dianyingleixing"
								placeholder="输入电影类型"
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
							v-if="isAuth('dianyingleixing','删除')"
							class="toolbar-btn danger-btn"
							:disabled="dataListSelections.length ? false : true"
							@click="deleteHandler()"
						>
							批量删除
						</el-button>
					</div>
				</div>

				<div class="table-wrap" v-loading="dataListLoading">
					<el-table
						v-if="isAuth('dianyingleixing','查看')"
						class="genre-table"
						:data="dataList"
						:border="false"
						:stripe="false"
						@selection-change="selectionChangeHandler"
					>
						<el-table-column type="selection" align="center" width="56"></el-table-column>
						<el-table-column label="序号" width="88">
							<template slot-scope="scope">
								<div class="mono-cell">{{ scope.$index + 1 }}</div>
							</template>
						</el-table-column>
						<el-table-column prop="dianyingleixing" label="电影类型" min-width="420">
							<template slot-scope="scope">
								<div class="genre-cell">
									<p>{{ scope.row.dianyingleixing || '未填写类型' }}</p>
									<span>分类标签</span>
								</div>
							</template>
						</el-table-column>
						<el-table-column label="操作" width="150" align="right">
							<template slot-scope="scope">
								<div class="action-group">
									<button
										v-if="isAuth('dianyingleixing','修改')"
										type="button"
										class="action-icon"
										title="修改"
										@click="addOrUpdateHandler(scope.row.id)"
									>
										<i class="el-icon-edit"></i>
									</button>
									<button
										v-if="isAuth('dianyingleixing','删除')"
										type="button"
										class="action-icon action-icon-danger"
										title="删除"
										@click="deleteHandler(scope.row.id)"
									>
										<i class="el-icon-delete"></i>
									</button>
								</div>
							</template>
						</el-table-column>
					</el-table>

					<div v-if="!dataListLoading && !dataList.length" class="empty-block">
						<div class="empty-title">暂无电影类型数据</div>
						<p>可以通过右上角按钮新增电影类型，或调整筛选条件重新查询。</p>
					</div>
				</div>

				<div class="pagination-bar">
					<p>当前展示第 {{ pageIndex }} 页，共 {{ totalPage || 0 }} 条电影类型记录</p>
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
		AddOrUpdate,
	},
	data() {
		return {
			indexQueryCondition: '',
			searchForm: {
				dianyingleixing: ''
			},
			form: {},
			dataList: [],
			pageIndex: 1,
			pageSize: 10,
			totalPage: 0,
			dataListLoading: false,
			dataListSelections: [],
			showFlag: true,
			addOrUpdateFlag: false,
			layouts: ['prev', 'pager', 'next', 'sizes'],
		}
	},
	computed: {
		tablename() {
			return this.$storage.get('sessionTable')
		},
	},
	created() {
		this.init()
		this.getDataList()
		this.contentStyleChange()
	},
	methods: {
		contentStyleChange() {
			this.contentPageStyleChange()
		},
		contentPageStyleChange() {
		},
		init() {
		},
		search() {
			this.pageIndex = 1
			this.getDataList()
		},
		resetSearch() {
			this.searchForm = {
				dianyingleixing: ''
			}
			this.dataListSelections = []
			this.search()
		},
		getDataList() {
			this.dataListLoading = true
			let params = {
				page: this.pageIndex,
				limit: this.pageSize,
				sort: 'id',
				order: 'desc',
			}
			if (this.searchForm.dianyingleixing != '' && this.searchForm.dianyingleixing != undefined) {
				params.dianyingleixing = '%' + this.searchForm.dianyingleixing + '%'
			}
			this.$http({
				url: 'dianyingleixing/page',
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
			if (type != 'info' && type != 'msg') {
				type = 'else'
			}
			this.$nextTick(() => {
				this.$refs.addOrUpdate.init(id, type)
			})
		},
		async deleteHandler(id) {
			var ids = id ? [Number(id)] : this.dataListSelections.map(item => {
				return Number(item.id)
			})
			await this.$confirm(`确定进行[${id ? '删除' : '批量删除'}]操作?`, '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(async () => {
				await this.$http({
					url: 'dianyingleixing/delete',
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
		},
	}
}
</script>

<style lang="scss" scoped>
.admin-genre-list-page {
	min-height: 100%;
	padding: 18px 22px 28px;
	background:
		radial-gradient(circle at top left, rgba(255, 198, 57, 0.07), transparent 20%),
		linear-gradient(180deg, #0b1326 0%, #10192f 100%);
	color: #dae2fd;
}

.page-tools-card {
	display: flex;
	align-items: flex-end;
	justify-content: space-between;
	gap: 20px;
	margin-bottom: 24px;
	padding: 24px 28px;
	border-radius: 28px;
	background: rgba(19, 27, 46, 0.92);
	box-shadow: 0 18px 40px rgba(2, 6, 23, 0.2);
}

.page-tools-kicker {
	font-size: 11px;
	font-weight: 700;
	letter-spacing: 0.24em;
	text-transform: uppercase;
	color: rgba(255, 198, 57, 0.78);
}

.page-tools-copy h1 {
	margin: 10px 0 8px;
	font-family: 'Epilogue', 'Avenir Next', sans-serif;
	font-size: 42px;
	font-weight: 800;
	line-height: 1;
	color: #f3f6ff;
}

.page-tools-copy p {
	margin: 0;
	max-width: 720px;
	font-size: 15px;
	line-height: 1.8;
	color: rgba(218, 226, 253, 0.64);
}

.hero-add-btn {
	display: inline-flex;
	align-items: center;
	gap: 10px;
	height: 54px;
	padding: 0 24px;
	border: none;
	border-radius: 999px;
	background: linear-gradient(135deg, #ffc639 0%, #e1aa12 100%);
	color: #0b1326;
	font-size: 14px;
	font-weight: 800;
	box-shadow: 0 15px 30px rgba(255, 198, 57, 0.26);
}

.hero-add-btn:hover {
	transform: translateY(-1px);
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

.mono-cell {
	font-family: 'Consolas', 'Courier New', monospace;
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.08em;
	color: rgba(218, 226, 253, 0.68);
}

.genre-cell {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.genre-cell p,
.genre-cell span {
	margin: 0;
}

.genre-cell p {
	font-size: 15px;
	font-weight: 800;
	color: #f3f6ff;
}

.genre-cell span {
	font-size: 12px;
	font-weight: 600;
	color: rgba(218, 226, 253, 0.52);
}

.action-group {
	display: flex;
	justify-content: flex-end;
	gap: 10px;
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

.admin-genre-list-page ::v-deep .el-input {
	width: 100%;
}

.admin-genre-list-page ::v-deep .el-input__inner {
	height: 44px;
	line-height: 44px;
	padding: 0 16px;
	border: 1px solid rgba(148, 160, 191, 0.16);
	border-radius: 14px;
	background: rgba(45, 52, 73, 0.9);
	color: #dae2fd;
}

.admin-genre-list-page ::v-deep .el-input__inner::placeholder {
	color: rgba(148, 160, 191, 0.64);
}

.admin-genre-list-page ::v-deep .el-input__inner:focus {
	border-color: rgba(255, 198, 57, 0.64);
}

.admin-genre-list-page ::v-deep .el-table,
.admin-genre-list-page ::v-deep .el-table__expanded-cell,
.admin-genre-list-page ::v-deep .el-table tr,
.admin-genre-list-page ::v-deep .el-table th,
.admin-genre-list-page ::v-deep .el-table td {
	background: transparent;
}

.admin-genre-list-page ::v-deep .el-table::before {
	background: rgba(255, 255, 255, 0.06);
}

.admin-genre-list-page ::v-deep .el-table__header-wrapper th {
	padding: 16px 0;
	background: rgba(19, 27, 46, 0.72);
	border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-genre-list-page ::v-deep .el-table__header-wrapper th .cell {
	font-size: 11px;
	font-weight: 800;
	letter-spacing: 0.14em;
	text-transform: uppercase;
	color: #ffc639;
}

.admin-genre-list-page ::v-deep .el-table__body-wrapper td {
	padding: 18px 0;
	border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.admin-genre-list-page ::v-deep .el-table__body-wrapper .cell,
.admin-genre-list-page ::v-deep .el-table__header-wrapper .cell {
	white-space: normal;
	word-break: break-word;
}

.admin-genre-list-page ::v-deep .el-table__body-wrapper tr:hover td {
	background: rgba(34, 42, 61, 0.86);
}

.admin-genre-list-page ::v-deep .el-checkbox__inner {
	border-color: rgba(148, 160, 191, 0.4);
	background: rgba(11, 19, 38, 0.82);
}

.admin-genre-list-page ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner,
.admin-genre-list-page ::v-deep .el-checkbox__input.is-indeterminate .el-checkbox__inner {
	background-color: #ffc639;
	border-color: #ffc639;
}

.admin-genre-list-page ::v-deep .el-pagination {
	margin-left: auto;
}

.admin-genre-list-page ::v-deep .el-pagination .btn-prev,
.admin-genre-list-page ::v-deep .el-pagination .btn-next,
.admin-genre-list-page ::v-deep .el-pagination .el-pager li {
	min-width: 38px;
	height: 38px;
	line-height: 38px;
	border-radius: 999px;
	border: 1px solid rgba(255, 255, 255, 0.08);
	background: rgba(19, 27, 46, 0.86);
	color: rgba(218, 226, 253, 0.72);
}

.admin-genre-list-page ::v-deep .el-pagination .el-pager li.active,
.admin-genre-list-page ::v-deep .el-pagination .btn-prev:hover,
.admin-genre-list-page ::v-deep .el-pagination .btn-next:hover,
.admin-genre-list-page ::v-deep .el-pagination .el-pager li:hover {
	background: #ffc639;
	color: #0b1326;
}

@media (max-width: 1400px) {
	.stats-grid {
		grid-template-columns: repeat(2, minmax(0, 1fr));
	}

	.page-tools-card,
	.table-toolbar,
	.pagination-bar {
		flex-direction: column;
		align-items: flex-start;
	}
}

@media (max-width: 900px) {
	.admin-genre-list-page {
		padding: 16px;
	}

	.stats-grid {
		grid-template-columns: minmax(0, 1fr);
	}

	.page-tools-copy h1 {
		font-size: 32px;
	}

	.filter-box {
		width: 100%;
	}
}
</style>
