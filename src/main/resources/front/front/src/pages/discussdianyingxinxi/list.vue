<template>
	<div>
		<div class="breadcrumb-preview">
			<el-breadcrumb :separator="''">
				<el-breadcrumb-item class="item1" to="/"><a>首页</a></el-breadcrumb-item>
				<el-breadcrumb-item class="item2" v-for="(item, index) in breadcrumbItem" :key="index"><a>{{item.name}}</a></el-breadcrumb-item>
			</el-breadcrumb>
		</div>
		<div v-if="centerType" class="back_box">
			<el-button class="backBtn" size="mini" @click="backClick">
				<span class="icon iconfont icon-jiantou33"></span>
				<span class="text">返回</span>
			</el-button>
		</div>
		<div class="list-preview">
			<el-form :inline="true" :model="formSearch" class="list-form-pv">
				<el-form-item class="list-item">
					<div class="lable">用户名：</div>
					<el-input v-model="formSearch.nickname" placeholder="用户名" @keydown.enter.native="getList(1, curFenlei)" clearable></el-input>
				</el-form-item>
				<el-form-item class="list-item">
					<div class="lable">评论内容：</div>
					<el-input v-model="formSearch.content" placeholder="评论内容" @keydown.enter.native="getList(1, curFenlei)" clearable></el-input>
				</el-form-item>
				<el-button class="list-search-btn" v-if=" true " type="primary" @click="getList(1, curFenlei)">
					<i class="el-icon-search"></i>
					查询
				</el-button>
			</el-form>
			<div class="select2">
				<div class="select2-list" v-for="(item,index) in selectOptionsList" :key="index">
					<div class="label">{{item.name}}：</div>
					<div class="item-body">
						<div class="item" @click="selectClick2(item,-1)" :class="item.check ==-1 ? 'active' : ''">全部</div>
						<div class="item" @click="selectClick2(item,index1)" :class="item.check == index1 ? 'active' : ''" v-for="item1,index1 in item.list" :key="index1">{{item1}}</div>
					</div>
				</div>
			</div>
			<div class="list">
				<!-- 样式三 -->
				<div class="list3 index-pv1">
					<div v-for="(item, index) in dataList" :key="index" @click.stop="toDetail(item)" class="list-item animation-box">
						<div class="img">
						</div>
						<div class="item-info">
							<div>
								<div class="time_item">
									<span class="icon iconfont icon-shijian21"></span>
									<span class="label">发布时间：</span>
									<span class="text">{{item.addtime.split(' ')[0]}}</span>
								</div>
							</div>
							<div class="desc ql-snow ql-editor" v-html="item.content"></div>
						</div>
					</div>
				</div>
			</div>

	
			<el-pagination
				background
				id="pagination"
				class="pagination"
				:pager-count="7"
				:page-size="pageSize"
				prev-text="上一页"
				next-text="下一页"
				:hide-on-single-page="false"
				:layout='["total","prev","pager","next","sizes","jumper"].join()'
				:total="total"
				:page-sizes="pageSizes"
				@current-change="curChange"
				@size-change="sizeChange"
				@prev-click="prevClick"
				@next-click="nextClick"
				></el-pagination>
		</div>
		<el-dialog title="预览图" :visible.sync="previewVisible" width="50%">
			<img :src="previewImg" alt="" style="width: 100%;">
		</el-dialog>
	</div>
</template>
<script>
	export default {
		//数据集合
		data() {
			return {
				selectIndex2: 0,
				selectOptionsList: [],
				layouts: '',
				swiperIndex: -1,
				baseUrl: '',
				breadcrumbItem: [
					{
						name: '电影信息评论表'
					}
				],
				formSearch: {
					nickname: '',
					content: '',
				},
				fenlei: [],
				feileiColumn: '',
				dataList: [],
				total: 1,
				pageSize: 12,
				pageSizes: [],
				totalPage: 1,
				curFenlei: '全部',
				isPlain: false,
				indexQueryCondition: '',
				timeRange: [],
				centerType:false,
				previewImg: '',
				previewVisible: false,
				sortType: 'id',
				sortOrder: 'desc',
			}
		},
		async created() {
			if(this.$route.query.centerType&&this.$route.query.centerType!=0){
				this.centerType = true
			}
			this.baseUrl = this.$config.baseUrl;
			await this.getFenlei();
			let fenlei = '全部'
			if(this.$route.query.homeFenlei){
				fenlei = this.$route.query.homeFenlei
			}
			this.getList(1, fenlei);
		},
		watch:{
			$route(newValue){
				this.getList(1, newValue.query.homeFenlei);
			}
		},
		//方法集合
		methods: {
			selectClick2(row,index) {
				row.check = index
				if(index == -1){
					this.formSearch[row.tableName] = ''
				}else {
					this.formSearch[row.tableName] = row.list[index]
				}
				this.getList()
			},
			async getFenlei() {
			},
			getList(page) {
				let params = {
					page,
					limit: this.pageSize,
				};
				let searchWhere = {};
				if (this.formSearch.nickname != '') searchWhere.nickname = '%' + this.formSearch.nickname + '%';
				if (this.formSearch.content != '') searchWhere.content = '%' + this.formSearch.content + '%';
				if (this.sortType) searchWhere.sort = this.sortType
				if (this.sortOrder) searchWhere.order = this.sortOrder
				this.$http.get(`discussdianyingxinxi/${this.centerType?'page':'list'}`, {params: Object.assign(params, searchWhere)}).then(res => {
					if (res.data.code == 0) {
						this.dataList = res.data.data.list;
						this.total = Number(res.data.data.total);
						this.pageSize = Number(res.data.data.pageSize);
						this.totalPage = res.data.data.totalPage;
						if(this.pageSizes.length==0){
							this.pageSizes = [this.pageSize, this.pageSize*2, this.pageSize*3, this.pageSize*5];
						}
					}
				});
			},
			sortClick(type){
				if(this.sortType==type){
					if(this.sortOrder == 'desc'){
						this.sortOrder = 'asc'
					}else{
						this.sortOrder = 'desc'
					}
				}else{
					this.sortType = type
					this.sortOrder = 'desc'
				}
				this.getList(1, '全部')
			},
			curChange(page) {
				this.getList(page);
			},
			prevClick(page) {
				this.getList(page);
			},
			sizeChange(size){
				this.pageSize = size
				this.getList(1);
			},
			nextClick(page) {
				this.getList(page);
			},
			imgPreView(url){
				this.previewImg = url
				this.previewVisible = true
			},
			toDetail(item) {
				let params = {
					id: item.id
				}
				if(this.centerType){
					params.centerType = 1
				}
				this.$router.push({path: '/index/discussdianyingxinxiDetail', query: params});
			},
			btnAuth(tableName,key){
				if(this.centerType){
					return this.isBackAuth(tableName,key)
				}else{
					return this.isAuth(tableName,key)
				}
			},
			backClick() {
				this.$router.push({path: '/index/center'});
			},
		}
	}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
	.list-preview {
		padding: 0 calc((100% - 1300px)/2);
		margin: 0px auto;
		color: #333;
		background: #fff;
		display: flex;
		width: 100%;
		font-size: 16px;
		justify-content: flex-start;
		align-items: flex-start;
		position: relative;
		flex-wrap: wrap;
		.list-form-pv {
			padding: 10px;
			margin: 20px 0;
			color: inherit;
			background: none;
			display: flex;
			width: 100%;
			font-size: inherit;
			flex-wrap: wrap;
			height: auto;
			order: 2;
			.list-item {
				padding: 0;
				margin: 0 0px 10px 0;
				display: flex;
				font-size: inherit;
				align-items: center;
				flex-wrap: wrap;
				::v-deep.el-form-item__content {
					display: flex;
				}
				.lable {
					padding: 0 10px;
					color: #333;
					white-space: nowrap;
					display: inline-block;
					width: auto;
					font-size: 16px;
					line-height: 40px;
				}
				.el-input {
					width: auto;
				}
				.datetimerange {
					border: 1px solid #243559;
					border-radius: 0px;
					padding: 3px 3px;
					background: #fff;
					width: auto;
					justify-content: center;
				}
				.el-input ::v-deep .el-input__inner {
					border: 1px solid #243559;
					border-radius: 0px;
					padding: 0 10px;
					margin: 0 5px 0 0;
					color: #333;
					width: auto;
					font-size: 16px;
					line-height: 40px;
					height: 40px;
				}
				.el-select {
					width: 100%;
				}
				.el-select ::v-deep .el-input__inner {
				}
				.el-date-editor {
					width: auto;
				}
				.el-date-editor ::v-deep .el-input__inner {
					border: 1px solid #243559;
					border-radius: 0px;
					padding: 0 0px 0 30px;
					margin: 0;
					color: #333;
					width: auto;
					font-size: 16px;
					line-height: 40px;
					height: 40px;
				}
			}
			.list-search-btn {
				cursor: pointer;
				border: 0;
				border-radius: 0px;
				padding: 0px 15px;
				margin: 0 10px 0 10px;
				color: #fff;
				background: rgb(0, 78, 162);
				width: auto;
				font-size: inherit;
				line-height: 40px;
				height: 40px;
				i {
					margin: 0 10px 0 0;
					color: #fff;
					font-size: inherit;
				}
			}
			.list-add-btn {
				cursor: pointer;
				border: 0;
				border-radius: 0px;
				padding: 0px 15px;
				margin: 0 10px 0 0;
				color: #fff;
				background: rgb(45, 50, 64);
				width: auto;
				font-size: inherit;
				line-height: 40px;
				height: 40px;
				i {
					margin: 0 10px 0 0;
					color: #fff;
					font-size: inherit;
				}
			}
		}
		.select2 {
			border: 0px solid #243559;
			padding: 0;
			margin: 0 auto;
			background: none;
			width: 100%;
			font-size: 15px;
			height: auto;
			order: 3;
			.select2-list {
				border: 1px solid #243559;
				border-radius: 6px;
				padding: 0 0px;
				margin: 0 0 10px;
				background: #f6f6f6;
				width: 100%;
				min-height: 70px;
				height: auto;
				.label {
					padding: 0 5px;
					margin: 0 20px 0 0;
					color: #fff;
					background: rgb(45, 50, 64);
					font-weight: 500;
					display: inline-block;
					font-size: inherit;
					line-height: 70px;
					text-align: right;
					min-width: 100px;
				}
				.item-body {
					display: inline-block;
					width: auto;
					flex-wrap: wrap;
					height: auto;
					.item {
						border-radius: 4px;
						padding: 0 5px;
						color: inherit;
						background: none;
						display: inline-block;
						font-size: inherit;
						line-height: 40px;
						text-align: center;
						min-width: 80px;
					}
					.item:hover {
						cursor: pointer;
						color: #fff;
						background: #3397da;
					}
					.item.active {
						cursor: pointer;
						color: #fff;
						background: rgb(0, 78, 162);
						display: inline-block;
						min-width: 80px;
						text-align: center;
					}
				}
			}
		}
		.sort_view {
			border-radius: 4px;
			padding: 10px 20px 10px;
			margin: 20px auto 0;
			color: rgb(89, 89, 89);
			background: none;
			width: 100%;
			font-size: inherit;
			border-color: rgb(45, 50, 64);
			border-width: 1px;
			border-style: solid;
			order: 3;
			.click-sort-btn {
				border: 0;
				border-radius: 8px;
				padding: 0 5px;
				margin: 0 5px;
				color: inherit;
				background: none;
				font-size: inherit;
				.icon {
					margin: 0 2px 0 0;
					color: inherit;
					font-size: inherit;
					line-height: 40px;
				}
				.text {
					color: inherit;
					font-size: inherit;
					line-height: 40px;
				}
			}
		}
		.list {
			margin: 20px 0 0;
			overflow: hidden;
			background: none;
			width: calc(100% - 0px);
			clear: both;
			font-size: 15px;
			order: 8;
			.index-pv1 .animation-box {
				transform: rotate(0deg) scale(1) skew(0deg, 0deg) translate3d(0px, 0px, 0px);
				z-index: initial;
			}
				
			.index-pv1 .animation-box:hover {
				transform: rotate(0) scale(1) skew(0deg, 0deg) translate3d(0px, 0px, 0px);
				-webkit-perspective: 1000px;
				perspective: 1000px;
				transition: 0.3s;
				z-index: 1;
			}
				
			.index-pv1 .animation-box img {
				transform: rotate(0deg) scale(1) skew(0deg, 0deg) translate3d(0px, 0px, 0px);
			}
			
			.index-pv1 .animation-box img:hover {
				transform: rotate(0) scale(1) skew(0deg, 0deg) translate3d(0px, 0px, 0px);
				-webkit-perspective: 1000px;
				perspective: 1000px;
				transition: 0.3s;
			}
			.list3 {
				padding: 0;
				background: #fff;
				display: flex;
				gap: 20px;
				width: 100%;
				flex-wrap: wrap;
				height: auto;
				.list-item {
					border: 1px solid rgb(45, 50, 64);
					cursor: pointer;
					padding: 10px;
					margin: 0;
					overflow: hidden;
					align-content: center;
					background: none;
					display: flex;
					width: 420px;
					align-items: center;
					position: relative;
					height: auto;
					.img {
						border: 1px solid rgb(95, 136, 176);
						border-radius: 5px;
						padding: 6px;
						margin: 0 10px 0 0;
						overflow: hidden;
						width: 200px;
						height: 200px;
						.image {
							border-radius: 10px;
							object-fit: cover;
							display: block;
							width: 100%;
							height: 100%;
						}
					}
					.item-info {
						padding: 0 0px;
						flex: 1;
						display: flex;
						position: relative;
						height: auto;
						.name {
							padding: 0 10px;
							color: #333;
							font-size: 14px;
							border-color: rgb(95, 136, 176);
							border-width: 0 0 1px 0;
							line-height: 36px;
							border-style: solid;
						}
						.price {
							padding: 0 10px;
							color: #f00;
							font-size: 16px;
							border-color: rgb(95, 136, 176);
							border-width: 0 0 1px 0;
							line-height: 24px;
							border-style: solid;
						}
						.time_item {
							padding: 0 10px;
							color: rgb(95, 136, 176);
							border-color: rgb(95, 136, 176);
							border-width: 0 0 1px 0;
							border-style: solid;
							.icon {
								margin: 0 2px 0 0;
								color: inherit;
								font-size: 12px;
								line-height: 28px;
							}
							.label {
								color: inherit;
								font-size: 12px;
								line-height: 1.5;
							}
							.text {
								color: inherit;
								font-size: 12px;
								line-height: 1.5;
							}
						}
						.publisher_item {
							padding: 0 10px;
							color: rgb(95, 136, 176);
							border-color: rgb(95, 136, 176);
							border-width: 0 0 1px 0;
							border-style: solid;
							.icon {
								margin: 0 2px 0 0;
								font-size: 12px;
								line-height: 28px;
							}
							.label {
								font-size: 12px;
								line-height: 1.5;
							}
							.text {
								font-size: 12px;
								line-height: 28px;
							}
						}
						.like_item {
							padding: 0 10px;
							color: rgb(95, 136, 176);
							border-color: rgb(95, 136, 176);
							border-width: 0 0 1px 0;
							border-style: solid;
							.icon {
								margin: 0 2px 0 0;
								font-size: 12px;
								line-height: 28px;
							}
							.label {
								font-size: 12px;
								line-height: 1.5;
							}
							.text {
								font-size: 12px;
								line-height: 28px;
							}
						}
						.collect_item {
							padding: 0 10px;
							color: rgb(95, 136, 176);
							border-color: rgb(95, 136, 176);
							border-width: 0 0 1px 0;
							border-style: solid;
							.icon {
								margin: 0 2px 0 0;
								font-size: 12px;
								line-height: 28px;
							}
							.label {
								font-size: 12px;
								line-height: 1.5;
							}
							.text {
								font-size: 12px;
								line-height: 28px;
							}
						}
						.view_item {
							padding: 0 10px;
							color: rgb(95, 136, 176);
							.icon {
								margin: 0 2px 0 0;
								font-size: 12px;
								line-height: 28px;
							}
							.label {
								font-size: 12px;
								line-height: 1.5;
							}
							.text {
								font-size: 12px;
								line-height: 28px;
							}
						}
					}
					.desc {
						color: #666;
						top: 150px;
						left: -195px;
						flex: 1;
						display: none;
						width: 185px;
						font-size: 12px;
						line-height: 1.5;
						position: absolute;
					}
				}
				.list-item:hover {
					.item-info {
						.name {
						}
						.price {
						}
						.time_item {
							.icon {
							}
							.label {
							}
							.text {
							}
						}
						.publisher_item {
							.icon {
							}
							.label {
							}
							.text {
							}
						}
						.like_item {
							.icon {
							}
							.label {
							}
							.text {
							}
						}
						.collect_item {
							.icon {
							}
							.label {
							}
							.text {
							}
						}
						.view_item {
							.icon {
							}
							.label {
							}
							.text {
							}
						}
					}
					.desc {
					}
				}
			}
		}
	}
</style>
