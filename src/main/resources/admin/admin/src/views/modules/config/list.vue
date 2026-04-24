<template>
	<div class="main-content admin-banner-list-page">
		<template v-if="showFlag">
			<section class="page-tools-card">
				<div class="page-tools-copy">
					<div class="page-tools-kicker">首页</div>
					<h1>轮播图</h1>
					<p>维护前台轮播图素材与查看入口，继续复用现有图片预览、查看、修改与分页逻辑。</p>
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
					<p>已配图片</p>
					<div class="stat-main">
						<strong>{{ imageCount }}</strong>
						<span>本页统计</span>
					</div>
				</div>
				<div class="stat-card">
					<p>已选条目</p>
					<div class="stat-main">
						<strong>{{ dataListSelections.length }}</strong>
						<span>当前选择</span>
					</div>
				</div>
			</section>

			<section class="table-shell">
				<div class="table-toolbar">
					<div class="toolbar-left">
						<div class="toolbar-copy">
							<div class="toolbar-kicker">Banner Config</div>
							<h3>轮播图配置列表</h3>
						</div>
					</div>
					<div class="toolbar-right">
						<div class="selection-chip">已选 {{ dataListSelections.length }} 项</div>
					</div>
				</div>

				<div class="table-wrap" v-loading="dataListLoading">
					<el-table
						v-if="isAuth('config','查看')"
						class="banner-table"
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
						<el-table-column prop="name" label="名称" min-width="220">
							<template slot-scope="scope">
								<div class="name-cell">
									<p>{{ scope.row.name || '未命名配置' }}</p>
									<span>轮播图配置项</span>
								</div>
							</template>
						</el-table-column>
						<el-table-column label="图片素材" min-width="260">
							<template slot-scope="scope">
								<div class="poster-cell">
									<div class="poster-preview" @click="previewRowImage(scope.row)">
										<img v-if="getImageUrl(scope.row)" :src="getImageUrl(scope.row)" alt="banner image">
										<div v-else class="poster-empty">无图片</div>
									</div>
								</div>
							</template>
						</el-table-column>
						<el-table-column label="操作" width="150" align="right">
							<template slot-scope="scope">
								<div class="action-group">
									<button
										v-if="isAuth('config','查看')"
										type="button"
										class="action-icon"
										title="查看"
										@click="addOrUpdateHandler(scope.row.id, 'info')"
									>
										<i class="el-icon-view"></i>
									</button>
									<button
										v-if="isAuth('config','修改')"
										type="button"
										class="action-icon"
										title="修改"
										@click="addOrUpdateHandler(scope.row.id)"
									>
										<i class="el-icon-edit"></i>
									</button>
								</div>
							</template>
						</el-table-column>
					</el-table>

					<div v-if="!dataListLoading && !dataList.length" class="empty-block">
						<div class="empty-title">暂无轮播图数据</div>
						<p>当前没有可展示的轮播图配置记录，请检查后端数据或稍后刷新页面。</p>
					</div>
				</div>

				<div class="pagination-bar">
					<p>当前展示第 {{ pageIndex }} 页，共 {{ totalPage || 0 }} 条轮播图记录</p>
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

		<el-dialog title="预览图" :visible.sync="previewVisible" width="50%">
			<img :src="previewImg" alt="" style="width: 100%;">
		</el-dialog>
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
				key: ''
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
			previewImg: '',
			previewVisible: false,
		}
	},
	computed: {
		tablename() {
			return this.$storage.get('sessionTable')
		},
		imageCount() {
			return this.dataList.filter(function(item) {
				return !!(item && item.value)
			}).length
		},
	},
	created() {
		this.init()
		this.getDataList()
		this.contentStyleChange()
	},
	methods: {
		getImageUrl(row) {
			if (!row || !row.value) {
				return ''
			}
			if (row.value.substring(0, 4) === 'http' && row.value.split(',w').length > 1) {
				return row.value
			}
			if (row.value.substring(0, 4) === 'http') {
				return row.value.split(',')[0]
			}
			return this.$base.url + row.value.split(',')[0]
		},
		previewRowImage(row) {
			var url = this.getImageUrl(row)
			if (!url) {
				return
			}
			this.previewImg = url
			this.previewVisible = true
		},
		imgPreView(url) {
			this.previewImg = url
			this.previewVisible = true
		},
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
		getDataList() {
			this.dataListLoading = true
			let params = {
				page: this.pageIndex,
				limit: this.pageSize,
				sort: 'id',
				order: 'desc',
				name: '%picture%'
			}
			this.$http({
				url: 'config/page',
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
		disscussListHandler(id, type) {
			this.$router.push({ path: '/discussconfig', query: { refid: id } })
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
					url: 'config/delete',
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
.admin-banner-list-page {
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

.toolbar-kicker {
	font-size: 11px;
	font-weight: 700;
	letter-spacing: 0.14em;
	text-transform: uppercase;
	color: rgba(208, 197, 175, 0.78);
}

.toolbar-copy h3 {
	margin: 8px 0 0;
	font-size: 28px;
	font-weight: 800;
	color: #f3f6ff;
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

.name-cell {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.name-cell p,
.name-cell span {
	margin: 0;
}

.name-cell p {
	font-size: 15px;
	font-weight: 800;
	color: #f3f6ff;
}

.name-cell span {
	font-size: 12px;
	font-weight: 600;
	color: rgba(218, 226, 253, 0.52);
}

.poster-cell {
	display: flex;
	align-items: center;
}

.poster-preview {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 128px;
	height: 80px;
	border-radius: 18px;
	overflow: hidden;
	border: 1px solid rgba(255, 198, 57, 0.14);
	background: rgba(45, 52, 73, 0.92);
	cursor: pointer;
}

.poster-preview img {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

.poster-empty {
	font-size: 12px;
	font-weight: 700;
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

.admin-banner-list-page ::v-deep .el-table,
.admin-banner-list-page ::v-deep .el-table__expanded-cell,
.admin-banner-list-page ::v-deep .el-table tr,
.admin-banner-list-page ::v-deep .el-table th,
.admin-banner-list-page ::v-deep .el-table td {
	background: transparent;
}

.admin-banner-list-page ::v-deep .el-table::before {
	background: rgba(255, 255, 255, 0.06);
}

.admin-banner-list-page ::v-deep .el-table__header-wrapper th {
	padding: 16px 0;
	background: rgba(19, 27, 46, 0.72);
	border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-banner-list-page ::v-deep .el-table__header-wrapper th .cell {
	font-size: 11px;
	font-weight: 800;
	letter-spacing: 0.14em;
	text-transform: uppercase;
	color: #ffc639;
}

.admin-banner-list-page ::v-deep .el-table__body-wrapper td {
	padding: 18px 0;
	border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.admin-banner-list-page ::v-deep .el-table__body-wrapper .cell,
.admin-banner-list-page ::v-deep .el-table__header-wrapper .cell {
	white-space: normal;
	word-break: break-word;
}

.admin-banner-list-page ::v-deep .el-table__body-wrapper tr:hover td {
	background: rgba(34, 42, 61, 0.86);
}

.admin-banner-list-page ::v-deep .el-checkbox__inner {
	border-color: rgba(148, 160, 191, 0.4);
	background: rgba(11, 19, 38, 0.82);
}

.admin-banner-list-page ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner,
.admin-banner-list-page ::v-deep .el-checkbox__input.is-indeterminate .el-checkbox__inner {
	background-color: #ffc639;
	border-color: #ffc639;
}

.admin-banner-list-page ::v-deep .el-pagination {
	margin-left: auto;
}

.admin-banner-list-page ::v-deep .el-pagination .btn-prev,
.admin-banner-list-page ::v-deep .el-pagination .btn-next,
.admin-banner-list-page ::v-deep .el-pagination .el-pager li {
	min-width: 38px;
	height: 38px;
	line-height: 38px;
	border-radius: 999px;
	border: 1px solid rgba(255, 255, 255, 0.08);
	background: rgba(19, 27, 46, 0.86);
	color: rgba(218, 226, 253, 0.72);
}

.admin-banner-list-page ::v-deep .el-pagination .el-pager li.active,
.admin-banner-list-page ::v-deep .el-pagination .btn-prev:hover,
.admin-banner-list-page ::v-deep .el-pagination .btn-next:hover,
.admin-banner-list-page ::v-deep .el-pagination .el-pager li:hover {
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
	.admin-banner-list-page {
		padding: 16px;
	}

	.stats-grid {
		grid-template-columns: minmax(0, 1fr);
	}

	.page-tools-copy h1 {
		font-size: 32px;
	}
}
</style>
