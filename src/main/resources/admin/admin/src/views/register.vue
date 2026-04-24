<template>
	<div>
		<div class="register-container">
			<div class="register-swiper3">
				<div class="swiper-container mySwiper3">
					<div class="swiper-wrapper">
						<div class="swiper-slide" v-for="(item,index) in swiperList" :key="item.id">
							<div>
								<el-image :src="item.url" fit="cover"></el-image>
							</div>
						</div>
					</div>
					<!-- Add Pagination -->
					<div class="swiper-pagination"></div>
					<!-- Add Arrows -->
					<div class="swiper-button-next">
						<span class="icon iconfont icon-jiantou18"></span>
					</div>
					<div class="swiper-button-prev">
						<span class="icon iconfont icon-jiantou39"></span>
					</div>
				</div>
			</div>
			<el-form v-if="pageFlag=='register'" ref="ruleForm" class="rgs-form animate__animated animate__slideInDown" :model="ruleForm" :rules="rules">
				<div class="rgs-form2">
					<div class="title">基于协同过滤的电影推荐系统设计与实现</div>
					<el-form-item class="list-item" v-if="tableName=='yonghu'">
						<div class="lable" :class="changeRules('yonghuzhanghao')?'required':''">用户账号：</div>
						<el-input  v-model="ruleForm.yonghuzhanghao"  autocomplete="off" placeholder="用户账号"  type="text"  />
					</el-form-item>
					<el-form-item class="list-item" v-if="tableName=='yonghu'">
						<div class="lable" :class="changeRules('mima')?'required':''">密码：</div>
						<el-input  v-model="ruleForm.mima"  autocomplete="off" placeholder="密码"  type="password"  />
					</el-form-item>
					<el-form-item class="list-item" v-if="tableName=='yonghu'">
						<div class="lable" :class="changeRules('mima')?'required':''">确认密码：</div>
						<el-input  v-model="ruleForm.mima2" autocomplete="off" placeholder="确认密码" type="password" />
					</el-form-item>
					<el-form-item class="list-item" v-if="tableName=='yonghu'">
						<div class="lable" :class="changeRules('yonghuxingming')?'required':''">用户姓名：</div>
						<el-input  v-model="ruleForm.yonghuxingming"  autocomplete="off" placeholder="用户姓名"  type="text"  />
					</el-form-item>
					<el-form-item class="list-item" v-if="tableName=='yonghu'">
						<div class="lable" :class="changeRules('touxiang')?'required':''">头像：</div>
						<file-upload
							tip="点击上传头像"
							action="file/upload"
							:limit="3"
							:multiple="true"
							:fileUrls="ruleForm.touxiang?ruleForm.touxiang:''"
							@change="yonghutouxiangUploadChange"
						></file-upload>
					</el-form-item>
					<el-form-item class="list-item" v-if="tableName=='yonghu'">
						<div class="lable" :class="changeRules('xingbie')?'required':''">性别：</div>
						<el-select v-model="ruleForm.xingbie" placeholder="请选择性别" >
							<el-option
								v-for="(item,index) in yonghuxingbieOptions"
								v-bind:key="index"
								:label="item"
								:value="item">
							</el-option>
						</el-select>
					</el-form-item>
					<el-form-item class="list-item" v-if="tableName=='yonghu'">
						<div class="lable" :class="changeRules('lianxidianhua')?'required':''">联系电话：</div>
						<el-input  v-model="ruleForm.lianxidianhua"  autocomplete="off" placeholder="联系电话"  type="text"  />
					</el-form-item>
					<el-form-item class="list-item" v-if="tableName=='yonghu'">
						<div class="lable" :class="changeRules('shenfenzheng')?'required':''">身份证：</div>
						<el-input  v-model="ruleForm.shenfenzheng"  autocomplete="off" placeholder="身份证"  type="text"  />
					</el-form-item>
					<div class="register-btn">
						<div class="register-btn1">
							<button type="button" class="r-btn" @click="login()">注册</button>
						</div>
						<div class="register-btn2">
							<div class="r-login" @click="close()">已有账号，直接登录</div>
						</div>
					</div>
				</div>
			</el-form>
		</div>
	</div>
</template>

<script>
	import 'animate.css'
	import Swiper from "swiper";
export default {
	data() {
		return {
			ruleForm: {
			},
			forgetForm: {},
            pageFlag : '',
			tableName:"",
			rules: {},
            yonghuxingbieOptions: [],
			swiperList: [{"name":"01.jpg","uid":1728872523914,"url":"http://codegen.caihongy.cn/20241014/aa3886badeb24259b9a69cf9d67a4abd.jpg","status":"success"},{"name":"02.jpg","uid":1728872527592,"url":"http://codegen.caihongy.cn/20241014/ef2b0c1439154be681df13cec49e12d9.jpg","status":"success"},{"name":"03.jpg","uid":1728872531101,"url":"http://codegen.caihongy.cn/20241014/07ccdc2fb03047c88cbc32cfab168d1a.jpg","status":"success"}],
		};
	},
	mounted(){
		setTimeout(()=>{
			new Swiper(".mySwiper3", {"navigation":{"nextEl":".swiper-button-next","prevEl":".swiper-button-prev"},"pagination":{"el":".swiper-pagination","clickable":true},"autoplay":{"delay":3000,"disableOnInteraction":false},"effect":"fade"})
		}, 500)
		this.pageFlag = this.$route.query.pageFlag
		if(this.$route.query.pageFlag=='register'){
			
			let table = this.$storage.get("loginTable");
			this.tableName = table;
			if(this.tableName=='yonghu'){
				this.ruleForm = {
					yonghuzhanghao: '',
					mima: '',
					yonghuxingming: '',
					touxiang: '',
					xingbie: '',
					lianxidianhua: '',
					shenfenzheng: '',
				}
			}
			if ('yonghu' == this.tableName) {
				this.rules.yonghuzhanghao = [{ required: true, message: '请输入用户账号', trigger: 'blur' }]
			}
			if ('yonghu' == this.tableName) {
				this.rules.mima = [{ required: true, message: '请输入密码', trigger: 'blur' }]
			}
			if ('yonghu' == this.tableName) {
				this.rules.yonghuxingming = [{ required: true, message: '请输入用户姓名', trigger: 'blur' }]
			}
			this.yonghuxingbieOptions = "男,女".split(',')
		}
	},
	created() {
	},
	destroyed() {
		  	},
	methods: {
		changeRules(name){
			if(this.rules[name]){
				return true
			}
			return false
		},
		// 获取uuid
		getUUID () {
			return new Date().getTime();
		},
		close(){
			this.$router.push({ path: "/login" });
		},
        yonghutouxiangUploadChange(fileUrls) {
            this.ruleForm.touxiang = fileUrls;
        },

        // 多级联动参数


		// 注册
		login() {
			var url=this.tableName+"/register";
			if((!this.ruleForm.yonghuzhanghao) && `yonghu` == this.tableName){
				this.$message.error(`用户账号不能为空`);
				return
			}
			if((!this.ruleForm.mima) && `yonghu` == this.tableName){
				this.$message.error(`密码不能为空`);
				return
			}
			if((this.ruleForm.mima!=this.ruleForm.mima2) && `yonghu` == this.tableName){
				this.$message.error(`两次密码输入不一致`);
				return
			}
			if((!this.ruleForm.yonghuxingming) && `yonghu` == this.tableName){
				this.$message.error(`用户姓名不能为空`);
				return
			}
            if(this.ruleForm.touxiang!=null) {
                this.ruleForm.touxiang = this.ruleForm.touxiang.replace(new RegExp(this.$base.url,"g"),"");
            }
			if(`yonghu` == this.tableName && this.ruleForm.lianxidianhua &&(!this.$validate.isMobile(this.ruleForm.lianxidianhua))){
				this.$message.error(`联系电话应输入手机格式`);
				return
			}
			if(`yonghu` == this.tableName && this.ruleForm.shenfenzheng &&(!this.$validate.checkIdCard(this.ruleForm.shenfenzheng))){
				this.$message.error(`身份证应输入身份证格式`);
				return
			}
			this.$http({
				url: url,
				method: "post",
				data:this.ruleForm
			}).then(({ data }) => {
				if (data && data.code === 0) {
					this.$message({
						message: "注册成功",
						type: "success",
						duration: 1500,
						onClose: () => {
							this.$router.replace({ path: "/login" });
						}
					});
				} else {
					this.$message.error(data.msg);
				}
			});
		}
	}
};
</script>

<style lang="scss" scoped>
.register-container {
	position: relative;
	background-repeat: no-repeat;
	background-size: cover;
	background: url(http://codegen.caihongy.cn/20241012/2cf64ce479c04083a028aeeade009ddc.jpg);
	display: flex;
	width: 100%;
	min-height: 100vh;
	justify-content: center;
	align-items: center;
	background-position: center bottom;
	.register-swiper3 {
		margin: 0 auto;
		z-index: auto;
		top: 0;
		left: 0;
		width: 100vw;
		position: fixed;
		height: 100vh;
		.swiper-container {
			.swiper-slide {
				div {
					width: 100%;
					height: 100vh;
					.el-image {
						object-fit: cover;
						width: 100%;
						height: 100vh;
					}
				}
			}
			.swiper-pagination{
				left: 0;
				bottom: 10px;
				width: 100%;
				::v-deep span.swiper-pagination-bullet {
					border-radius: 100%;
					margin: 0 4px;
					background: #000;
					display: inline-block;
					width: 8px;
					opacity: .2;
					height: 8px;
				}
				::v-deep span.swiper-pagination-bullet:hover {
					background: #fff;
					opacity: 1;
				}
				::v-deep span.swiper-pagination-bullet.swiper-pagination-bullet-active {
					background: #fff;
					opacity: 1;
				}
			}
			.swiper-button-next {
				margin: -12px 0 0;
				top: 50%;
				width: 24px;
				height: 24px;
				.iconfont {
					color: #fff;
					width: 24px;
					font-size: 24px;
					height: 24px;
				}
			}
			.swiper-button-prev {
				margin: -12px 0 0;
				top: 50%;
				width: 24px;
				height: 24px;
				.iconfont {
					color: #fff;
					width: 24px;
					font-size: 24px;
					height: 24px;
				}
			}
			
			.swiper-button-prev:after {
				display:none;
			}
			.swiper-button-next:after {
				display:none;
			}
		}
	}
	.rgs-form {
		.rgs-form2 {
		width: 100%;
		}
		border-radius: 0px;
		padding: 20px 80px 20px 460px;
		margin: 20px 0;
		z-index: 1000;
		flex-direction: column;
		background: url(http://codegen.caihongy.cn/20241012/eaf0063a857e492b971e6c163c5883e4.png) no-repeat 50px 80px,url(http://codegen.caihongy.cn/20241012/13b7da1f594a4700a294d984c4f318a9.png) no-repeat center top / 100% 100%;
		display: flex;
		width: 880px;
		align-items: center;
		position: relative;
		flex-wrap: wrap;
		height: auto;
		.title {
			padding: 0 0px;
			margin: 0 0 20px -140px;
			color: #fff;
			background: noner;
			font-weight: 600;
			width: calc(100% + 140px);
			font-size: 22px;
			line-height: 40px;
			text-align: center;
		}
		.list-item {
			padding: 0 0 0 0px;
			margin: 0 0 26px;
			width: 100%;
			position: relative;
			height: auto;
			::v-deep .el-form-item__content {
				display: block;
			}
			.lable {
				padding: 0 20px 0 0;
				color: #fff;
				left: -140px;
				background: rgba(0, 0, 0, .5);
				width: 140px;
				font-size: 16px;
				line-height: 40px;
				position: absolute !important;
				text-align: right;
			}
			.el-input {
				width: 100%;
			}
			.el-input ::v-deep .el-input__inner {
				border: 0px solid #efeff7;
				border-radius: 0;
				padding: 0 10px;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 100%;
				font-size: 14px;
				height: 40px;
			}
			.el-input ::v-deep .el-input__inner:focus {
				border: 0px solid #efeff7;
				border-radius: 0;
				padding: 0 10px;
				outline: none;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 100%;
				font-size: 14px;
				height: 40px;
			}
			.el-input-number {
				width: 100%;
			}
			.el-input-number ::v-deep .el-input__inner {
				text-align: center;
				border: 0px solid #efeff7;
				border-radius: 0;
				padding: 0 10px;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 100%;
				font-size: 14px;
				height: 40px;
			}
			.el-input-number ::v-deep .el-input__inner:focus {
				border: 0px solid #efeff7;
				border-radius: 0;
				padding: 0 10px;
				outline: none;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 100%;
				font-size: 14px;
				height: 40px;
			}
			.el-input-number ::v-deep .el-input-number__decrease {
				display: none;
			}
			.el-input-number ::v-deep .el-input-number__increase {
				display: none;
			}
			.el-select {
				width: 100%;
			}
			.el-select ::v-deep .el-input__inner {
				border: 0px solid #efeff7;
				border-radius: 0;
				padding: 0 10px;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 100%;
				font-size: 16px;
				height: 40px;
			}
			.el-select ::v-deep .el-input__inner:focus {
				border: 0px solid #efeff7;
				border-radius: 0;
				padding: 0 10px;
				outline: none;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 100%;
				font-size: 16px;
				height: 40px;
			}
			.el-date-editor {
				width: 100%;
			}
			.el-date-editor ::v-deep .el-input__inner {
				border: 0px solid #efeff7;
				border-radius: 0;
				padding: 0 10px 0 30px;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 100%;
				font-size: 16px;
				height: 40px;
			}
			.el-date-editor ::v-deep .el-input__inner:focus {
				border: 0px solid #efeff7;
				border-radius: 0;
				padding: 0 10px 0 30px;
				outline: none;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 100%;
				font-size: 16px;
				height: 40px;
			}
			.el-date-editor.el-input {
				width: 100%;
			}
			::v-deep .el-upload--picture-card {
				background: transparent;
				border: 0;
				border-radius: 0;
				width: auto;
				height: auto;
				line-height: initial;
				vertical-align: middle;
			}
			::v-deep .upload .upload-img {
				border: 0px solid #efeff7;
				cursor: pointer;
				border-radius: 0;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 120px;
				font-size: 24px;
				line-height: 40px;
				text-align: center;
				height: 40px;
			}
			::v-deep .el-upload-list .el-upload-list__item {
				border: 0px solid #efeff7;
				cursor: pointer;
				border-radius: 0;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 120px;
				font-size: 24px;
				line-height: 40px;
				text-align: center;
				height: 40px;
			}
			::v-deep .el-upload .el-icon-plus {
				border: 0px solid #efeff7;
				cursor: pointer;
				border-radius: 0;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 120px;
				font-size: 24px;
				line-height: 40px;
				text-align: center;
				height: 40px;
			}
			::v-deep .el-upload__tip {
				color: #ccc;
				font-size: 15px;
			}
			::v-deep .el-input__inner::placeholder {
				color: #999;
				font-size: 16px;
			}
			.required {
				position: relative;
			}
			.required::after{
				color: red;
				left: 115px;
				position: absolute !important;
				content: "*";
				order: -1;
			}
			.editor {
				border: 0;
				background: #fff;
				width: 100%;
				min-height: 250px;
				height: auto;
			}
			.editor>.avatar-uploader {
				line-height: 0;
				height: 0;
			}
		}
		.list-item.email {
			input {
				border: 0px solid #efeff7;
				border-radius: 0px 0 0 0px;
				padding: 0 10px;
				margin: 0;
				color: #999;
				background: rgba(0, 0, 0, .5);
				flex: 1;
				width: 100%;
				font-size: 14px;
				height: 40px;
			}
			input:focus {
				border: 0px solid #efeff7;
				border-radius: 0px 0 0 0px;
				padding: 0 10px;
				outline: none;
				color: #999;
				background: rgba(0, 0, 0, .5);
				width: 100%;
				font-size: 14px;
				height: 40px;
			}
			input::placeholder {
				color: #999;
				font-size: 16px;
			}
			button {
				border: 0px solid #efeff7;
				cursor: pointer;
				border-radius: 0 0px 0px 0;
				padding: 0;
				margin: 0px 0 0 10px;
				color: #fff;
				background: #fed14e;
				width: 150px;
				font-size: 15px;
				height: 40px;
			}
			button:hover {
				opacity: 1;
			}
		}
		.register-btn {
			margin: 0 0 0 -140px;
			width: calc(100% + 140px);
		}
		.register-btn1 {
			width: 100%;
		}
		.register-btn2 {
			width: 100%;
		}
		.r-btn {
			border: 0px solid rgba(0, 0, 0, 1);
			cursor: pointer;
			padding: 0 10px;
			margin: 20px auto 5px;
			color: #fff;
			font-weight: 600;
			display: block;
			letter-spacing: 2px;
			font-size: 20px;
			border-radius: 0;
			background: #FF5F32;
			width: 100%;
			height: 44px;
		}
		.r-btn:hover {
			opacity: 1;
		}
		.r-login {
			cursor: pointer;
			padding: 0;
			color: #ddd;
			display: inline-block;
			width: 100%;
			font-size: 15px;
			line-height: 40px;
			text-align: center;
		}
		.r-login:hover {
		}
	}
}
	
	::-webkit-scrollbar {
	  display: none;
	}
</style>
