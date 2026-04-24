<template>
	<div class="main-content admin-account-list-page">
		<template v-if="showFlag">
			<section class="user-hero">
				<div class="hero-copy">
					<div class="hero-kicker">后台账号</div>
					<h1>Account Center</h1>
					<p>统一管理后台登录账号，保留现有新增、查看、修改、删除与分页能力。</p>
				</div>
				<el-button
					v-if="isAuth('users','新增')"
					class="hero-add-btn"
					type="button"
					@click="addOrUpdateHandler()"
				>
					<i class="el-icon-plus"></i>
					<span>新增账号</span>
				</el-button>
			</section>

			<section class="stats-grid">
				<div class="stat-card">
					<p>总账号数</p>
					<div class="stat-main">
						<strong>{{ totalPage || 0 }}</strong>
						<span>全部记录</span>
					</div>
				</div>
				<div class="stat-card">
					<p>当前页管理员</p>
					<div class="stat-main">
						<strong>{{ adminCount }}</strong>
						<span>本页统计</span>
					</div>
				</div>
				<div class="stat-card">
					<p>当前页普通账号</p>
					<div class="stat-main">
						<strong>{{ normalCount }}</strong>
						<span>本页统计</span>
					</div>
				</div>
				<div class="stat-card">
					<p>当前页角色数</p>
					<div class="stat-main">
						<strong>{{ roleKinds }}</strong>
						<span>去重统计</span>
					</div>
				</div>
			</section>

			<section class="table-shell">
				<div class="table-toolbar">
					<div class="toolbar-left">
						<div class="filter-box">
							<label>用户名</label>
							<el-input
								v-model="searchForm.username"
								placeholder="输入用户名"
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
							v-if="isAuth('users','删除')"
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
						v-if="isAuth('users','查看')"
						class="user-table"
						:data="dataList"
						:border="false"
						:stripe="false"
						@selection-change="selectionChangeHandler"
					>
						<el-table-column type="selection" align="center" width="56"></el-table-column>
						<el-table-column label="账号信息" min-width="260">
							<template slot-scope="scope">
								<div class="identity-cell">
									<div class="identity-avatar">
										{{ getAvatarFallback(scope.row) }}
									</div>
									<div class="identity-copy">
										<p>{{ scope.row.username || '未命名账号' }}</p>
										<span>#{{ scope.row.id || 'N/A' }}</span>
									</div>
								</div>
							</template>
						</el-table-column>
						<el-table-column label="角色权限" min-width="200">
							<template slot-scope="scope">
								<div class="tag-column">
									<span class="role-pill" :class="scope.row.role === '管理员' ? 'is-gold' : 'is-default'">
										{{ scope.row.role || '未设置角色' }}
									</span>
									<div class="tag-subline">后台管理身份</div>
								</div>
							</template>
						</el-table-column>
						<el-table-column label="操作" width="150" align="right">
							<template slot-scope="scope">
								<div class="action-group">
									<button
										v-if="isAuth('users','查看')"
										type="button"
										class="action-icon"
										title="查看"
										@click="addOrUpdateHandler(scope.row.id, 'info')"
									>
										<i class="el-icon-view"></i>
									</button>
									<button
										v-if="isAuth('users','修改')"
										type="button"
										class="action-icon"
										title="修改"
										@click="addOrUpdateHandler(scope.row.id)"
									>
										<i class="el-icon-edit"></i>
									</button>
									<button
										v-if="isAuth('users','删除')"
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
						<div class="empty-title">暂无后台账号</div>
						<p>当前筛选条件下没有匹配结果，可以调整条件后重新查询。</p>
					</div>
				</div>

				<div class="pagination-bar">
					<p>当前展示第 {{ pageIndex }} 页，共 {{ totalPage || 0 }} 条账号记录</p>
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
				username: ''
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
		adminCount() {
			return this.dataList.filter(function(item) {
				return item.role === '管理员'
			}).length
		},
		normalCount() {
			return this.dataList.filter(function(item) {
				return item.role !== '管理员'
			}).length
		},
		roleKinds() {
			var set = {}
			this.dataList.forEach(function(item) {
				if (item.role) {
					set[item.role] = true
				}
			})
			return Object.keys(set).length
		}
	},
	created() {
		this.getDataList()
	},
	methods: {
		contentStyleChange() {},
		getAvatarFallback(row) {
			if (row && row.username) {
				return row.username.substring(0, 1).toUpperCase()
			}
			return 'A'
		},
		search() {
			this.pageIndex = 1
			this.getDataList()
		},
		resetSearch() {
			this.searchForm = {
				username: ''
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
			if (this.searchForm.username !== '' && this.searchForm.username !== undefined) {
				params.username = '%' + this.searchForm.username + '%'
			}
			this.$http({
				url: 'users/page',
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
					url: 'users/delete',
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
.admin-account-list-page {
	min-height: 100%;
	padding: 18px 22px 28px;
	background:
		radial-gradient(circle at top left, rgba(255, 198, 57, 0.07), transparent 20%),
		linear-gradient(180deg, #0b1326 0%, #10192f 100%);
	color: #dae2fd;
}

.user-hero {
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
	min-width: 220px;
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

.tag-subline {
	font-size: 13px;
	font-weight: 600;
	line-height: 1.7;
	color: rgba(218, 226, 253, 0.66);
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

.admin-account-list-page .el-input,
.admin-account-list-page .el-select {
	width: 100%;
}

.admin-account-list-page ::v-deep .el-input__inner,
.admin-account-list-page ::v-deep .el-select .el-input__inner {
	height: 44px;
	line-height: 44px;
	padding: 0 16px;
	border: 1px solid rgba(148, 160, 191, 0.16);
	border-radius: 14px;
	background: rgba(45, 52, 73, 0.9);
	color: #dae2fd;
}

.admin-account-list-page ::v-deep .el-input__inner::placeholder {
	color: rgba(148, 160, 191, 0.64);
}

.admin-account-list-page ::v-deep .el-input__inner:focus,
.admin-account-list-page ::v-deep .el-select .el-input.is-focus .el-input__inner {
	border-color: rgba(255, 198, 57, 0.64);
}

.admin-account-list-page ::v-deep .el-table,
.admin-account-list-page ::v-deep .el-table__expanded-cell,
.admin-account-list-page ::v-deep .el-table tr,
.admin-account-list-page ::v-deep .el-table th,
.admin-account-list-page ::v-deep .el-table td {
	background: transparent;
}

.admin-account-list-page ::v-deep .el-table::before {
	background: rgba(255, 255, 255, 0.06);
}

.admin-account-list-page ::v-deep .el-table__header-wrapper th {
	padding: 16px 0;
	background: rgba(19, 27, 46, 0.72);
	border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-account-list-page ::v-deep .el-table__header-wrapper th .cell {
	font-size: 11px;
	font-weight: 800;
	letter-spacing: 0.14em;
	text-transform: uppercase;
	color: #ffc639;
}

.admin-account-list-page ::v-deep .el-table__body-wrapper td {
	padding: 18px 0;
	border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.admin-account-list-page ::v-deep .el-table__header-wrapper,
.admin-account-list-page ::v-deep .el-table__body-wrapper {
	scrollbar-width: thin;
	scrollbar-color: rgba(255, 198, 57, 0.55) rgba(255, 255, 255, 0.04);
}

.admin-account-list-page ::v-deep .el-table__body-wrapper::-webkit-scrollbar,
.admin-account-list-page ::v-deep .el-table__header-wrapper::-webkit-scrollbar {
	height: 10px;
	width: 10px;
}

.admin-account-list-page ::v-deep .el-table__body-wrapper::-webkit-scrollbar-track,
.admin-account-list-page ::v-deep .el-table__header-wrapper::-webkit-scrollbar-track {
	border-radius: 999px;
	background: rgba(255, 255, 255, 0.05);
}

.admin-account-list-page ::v-deep .el-table__body-wrapper::-webkit-scrollbar-thumb,
.admin-account-list-page ::v-deep .el-table__header-wrapper::-webkit-scrollbar-thumb {
	border-radius: 999px;
	background: linear-gradient(135deg, rgba(255, 198, 57, 0.92), rgba(225, 170, 18, 0.86));
	border: 2px solid rgba(19, 27, 46, 0.92);
}

.admin-account-list-page ::v-deep .el-table__body-wrapper .cell,
.admin-account-list-page ::v-deep .el-table__header-wrapper .cell {
	white-space: normal;
	word-break: break-word;
}

.admin-account-list-page ::v-deep .el-table__body-wrapper tr:hover td {
	background: rgba(34, 42, 61, 0.86);
}

.admin-account-list-page ::v-deep .el-checkbox__inner {
	border-color: rgba(148, 160, 191, 0.4);
	background: rgba(11, 19, 38, 0.82);
}

.admin-account-list-page ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner,
.admin-account-list-page ::v-deep .el-checkbox__input.is-indeterminate .el-checkbox__inner {
	background-color: #ffc639;
	border-color: #ffc639;
}

.admin-account-list-page ::v-deep .el-pagination {
	margin-left: auto;
}

.admin-account-list-page ::v-deep .el-pagination .btn-prev,
.admin-account-list-page ::v-deep .el-pagination .btn-next,
.admin-account-list-page ::v-deep .el-pagination .el-pager li {
	min-width: 38px;
	height: 38px;
	line-height: 38px;
	border-radius: 999px;
	border: 1px solid rgba(255, 255, 255, 0.08);
	background: rgba(19, 27, 46, 0.86);
	color: rgba(218, 226, 253, 0.72);
}

.admin-account-list-page ::v-deep .el-pagination .el-pager li.active,
.admin-account-list-page ::v-deep .el-pagination .btn-prev:hover,
.admin-account-list-page ::v-deep .el-pagination .btn-next:hover,
.admin-account-list-page ::v-deep .el-pagination .el-pager li:hover {
	background: #ffc639;
	color: #0b1326;
}

@media (max-width: 1400px) {
	.stats-grid {
		grid-template-columns: repeat(2, minmax(0, 1fr));
	}

	.user-hero,
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
	.admin-account-list-page {
		padding: 16px;
	}

	.stats-grid {
		grid-template-columns: minmax(0, 1fr);
	}

	.hero-copy h1 {
		font-size: 32px;
	}

	.filter-box {
		width: 100%;
	}
}
</style>
