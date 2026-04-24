<template>
	<router-view></router-view>
</template>

<script>
	export default {
		name: 'App',
		created() {
			
		}
	}
</script>

<style>
	* {
		box-sizing: border-box;
	}

	html,
	body {
		width: 100%;
		height: 100%;
		margin: 0;
		padding: 0;
	}

	[v-cloak] {
		display: none
	}

	.el-tabs__item {
		font-size: 18px;
	}

	.el-loading-spinner .el-loading-text {
		color: #4169E1;
	}

	.el-loading-spinner .path {
		stroke: #4169E1;
	}

	.el-breadcrumb {
		line-height: 60px;
		font-size: 18px;
	}

	.ql-editor {
		/*height: 500px;*/
	}

	.ql-snow .ql-tooltip[data-mode="link"]::before {
		content: "请输入链接地址:";
	}

	.ql-snow .ql-tooltip.ql-editing a.ql-action::after {
		border-right: 0px;
		content: "保存";
		padding-right: 0px;
	}

	.ql-snow .ql-tooltip[data-mode="video"]::before {
		content: "请输入视频地址:";
	}

	.ql-snow .ql-picker.ql-size .ql-picker-label::before,
	.ql-snow .ql-picker.ql-size .ql-picker-item::before {
		content: "14px";
	}

	.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="small"]::before,
	.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="small"]::before {
		content: "10px";
	}

	.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="large"]::before,
	.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="large"]::before {
		content: "18px";
	}

	.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="huge"]::before,
	.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="huge"]::before {
		content: "32px";
	}

	.ql-snow .ql-picker.ql-header .ql-picker-label::before,
	.ql-snow .ql-picker.ql-header .ql-picker-item::before {
		content: "文本";
	}

	.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="1"]::before,
	.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="1"]::before {
		content: "标题1";
	}

	.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="2"]::before,
	.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="2"]::before {
		content: "标题2";
	}

	.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="3"]::before,
	.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="3"]::before {
		content: "标题3";
	}

	.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="4"]::before,
	.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="4"]::before {
		content: "标题4";
	}

	.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="5"]::before,
	.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="5"]::before {
		content: "标题5";
	}

	.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="6"]::before,
	.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="6"]::before {
		content: "标题6";
	}

	.ql-snow .ql-picker.ql-font .ql-picker-label::before,
	.ql-snow .ql-picker.ql-font .ql-picker-item::before {
		content: "标准字体";
	}

	.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="serif"]::before,
	.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="serif"]::before {
		content: "衬线字体";
	}

	.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="monospace"]::before,
	.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="monospace"]::before {
		content: "等宽字体";
	}
	.breadcrumb-preview {
		border-radius: 0;
		padding: 0px 20px;
		margin: 0px auto;
		background: rgb(45, 50, 64);
		width: 100%;
		border-color: #b2b2b2;
		border-width: 0 0 0px;
		border-style: solid;
	}
	.breadcrumb-preview .el-breadcrumb {
		column-gap: 30px;
		padding: 5px 20px;
		margin: 0px auto;
		background: none;
		display: flex;
		width: 1200px;
		font-size: 16px;
		line-height: 30px;
		justify-content: center;
		
	}
	.breadcrumb-preview .el-breadcrumb .el-breadcrumb__separator {
		margin: 0 0px;
		color: #fff;
		font-weight: 600;
	}
	.breadcrumb-preview .el-breadcrumb .item1 .el-breadcrumb__inner a {
		border-radius: 0 0 15px 15px;
		padding: 0 10px;
		color: rgb(0, 78, 162);
		background: rgb(255, 255, 255);
		display: inline-block;
		border-color: rgb(0, 78, 162);
		border-width: 4px 0 0 0;
		line-height: 55px;
		border-style: solid;
		text-align: center;
		min-width: 130px;
	}
	.breadcrumb-preview .el-breadcrumb .item2 .el-breadcrumb__inner a {
		border-radius: 0 0 15px 15px;
		padding: 0 10px;
		color: #fff;
		background: rgb(0, 78, 162);
		display: inline-block;
		border-color: #fff;
		border-width: 4px 0 0;
		line-height: 55px;
		border-style: solid;
		text-align: center;
		min-width: 130px;
	}
	.breadcrumb-preview .el-breadcrumb .item3 .el-breadcrumb__inner a {
		border-radius: 0 0 15px 15px;
		padding: 0 10px;
		color: #3397da;
		background: #fff;
		display: inline-block;
		border-color: rgb(0, 78, 162);
		border-width: 4px 0 0;
		line-height: 55px;
		border-style: solid;
		text-align: center;
		min-width: 130px;
	}
	.back_box {
		border-radius: 0px;
		padding: 10px calc((100% - 1200px)/2);
		margin: 0 auto;
		background: #fff;
		display: flex;
		width: 100%;
		justify-content: flex-end;
	}
	.back_box .backBtn {
		border: 0;
		cursor: pointer;
		border-radius: 4px;
		padding: 0 20px;
		margin: 0;
		color: #fff;
		background: #0674fc;
		width: auto;
		font-size: 14px;
		line-height: 36px;
		height: 36px;
	}
	.back_box .backBtn .icon {
		margin: 0 4px 0 0;
		color: #fff;
		font-size: 14px;
	}
	.back_box .backBtn .text {
		color: #fff;
	}
	.back_box .backBtn:hover {
		opacity: 1;
	}
	.back_box .backBtn:hover .icon {
		color: #fff;
	}
	.back_box .backBtn:hover .text {
		color: #fff;
	}
	/* 分页样式 */
	#pagination {
		padding: 0 calc((100% - 1400px)/2);
		margin: 20px auto;
		overflow: hidden;
		color: #333;
		white-space: nowrap;
		font-weight: 500;
		width: 100%;
		clear: both;
		font-size: 16px;
		text-align: center;
		order: 50;
	}
	#pagination.el-pagination .el-pagination__total {
		margin: 0 10px 0 0;
		color: #666;
		font-weight: 400;
		display: inline-block;
		vertical-align: top;
		font-size: inherit;
		line-height: 28px;
		height: 28px;
	}
	
	#pagination.el-pagination .btn-prev {
		border: none;
		border-radius: 2px;
		padding: 0 10px;
		margin: 0 5px;
		color: #666;
		background: #f4f4f5;
		display: inline-block;
		vertical-align: top;
		font-size: 16px;
		line-height: 28px;
		min-width: 35px;
		height: 28px;
	}
	
	#pagination.el-pagination .btn-next {
		border: none;
		border-radius: 2px;
		padding: 0 10px;
		margin: 0 5px;
		color: #666;
		background: #f4f4f5;
		display: inline-block;
		vertical-align: top;
		font-size: 16px;
		line-height: 28px;
		min-width: 35px;
		height: 28px;
	}
	
	#pagination.el-pagination .btn-prev:disabled {
		border: none;
		cursor: not-allowed;
		border-radius: 2px;
		padding: 0 10px;
		margin: 0 5px;
		color: #C0C4CC;
		background: #f4f4f5;
		display: inline-block;
		vertical-align: top;
		font-size: 16px;
		line-height: 28px;
		height: 28px;
	}
	
	#pagination.el-pagination .btn-next:disabled {
		border: none;
		cursor: not-allowed;
		border-radius: 2px;
		padding: 0 10px;
		margin: 0 5px;
		color: #C0C4CC;
		background: #f4f4f5;
		display: inline-block;
		vertical-align: top;
		font-size: 16px;
		line-height: 28px;
		height: 28px;
	}
	
	#pagination.el-pagination .el-pager {
		padding: 0;
		margin: 0;
		display: inline-block;
		vertical-align: top;
	}
	
	#pagination.el-pagination .el-pager .number {
		cursor: pointer;
		padding: 0 4px;
		margin: 0 5px;
		color: #666;
		display: inline-block;
		vertical-align: top;
		font-size: 16px;
		line-height: 28px;
		border-radius: 2px;
		background: #f4f4f5;
		text-align: center;
		min-width: 30px;
		height: 28px;
	}
	
	#pagination.el-pagination .el-pager .number:hover {
		cursor: pointer;
		padding: 0 4px;
		margin: 0 5px;
		color: #fff;
		display: inline-block;
		vertical-align: top;
		font-size: 16px;
		line-height: 28px;
		border-radius: 2px;
		background: #0674fc;
		text-align: center;
		min-width: 30px;
		height: 28px;
	}
	
	#pagination.el-pagination .el-pager .number.active {
		cursor: default;
		padding: 0 4px;
		margin: 0 5px;
		color: #fff;
		display: inline-block;
		vertical-align: top;
		font-size: 16px;
		line-height: 28px;
		border-radius: 2px;
		background: #0674fc;
		text-align: center;
		min-width: 30px;
		height: 28px;
	}
	
	#pagination.el-pagination .el-pagination__sizes {
		display: inline-block;
		vertical-align: top;
		font-size: 13px;
		line-height: 28px;
		height: 28px;
	}
	
	#pagination.el-pagination .el-pagination__sizes .el-input {
		margin: 0 5px;
		width: 100px;
		position: relative;
	}
	
	#pagination.el-pagination .el-pagination__sizes .el-input .el-input__inner {
		border: 1px solid #DCDFE6;
		cursor: pointer;
		padding: 0 25px 0 8px;
		color: #606266;
		display: inline-block;
		font-size: 16px;
		line-height: 28px;
		border-radius: 3px;
		outline: 0;
		background: #FFF;
		width: 100%;
		text-align: center;
		height: 28px;
	}
	
	#pagination.el-pagination .el-pagination__sizes .el-input span.el-input__suffix {
		top: 0;
		position: absolute;
		right: 0;
		height: 100%;
	}
	
	#pagination.el-pagination .el-pagination__sizes .el-input .el-input__suffix .el-select__caret {
		cursor: pointer;
		color: #C0C4CC;
		width: 25px;
		font-size: 14px;
		line-height: 28px;
		text-align: center;
	}
	
	#pagination.el-pagination .el-pagination__jump {
		margin: 0 0 0 24px;
		color: #606266;
		display: inline-block;
		vertical-align: top;
		font-size: inherit;
		line-height: 28px;
		height: 28px;
	}
	
	#pagination.el-pagination .el-pagination__jump .el-input {
		border-radius: 3px;
		padding: 0 2px;
		margin: 0 2px;
		display: inline-block;
		width: 50px;
		font-size: inherit;
		line-height: 18px;
		position: relative;
		text-align: center;
		height: 28px;
	}
	
	#pagination.el-pagination .el-pagination__jump .el-input .el-input__inner {
		border: 1px solid #DCDFE6;
		cursor: pointer;
		padding: 0 3px;
		color: #606266;
		display: inline-block;
		font-size: 16px;
		line-height: 26px;
		border-radius: 3px;
		outline: 0;
		background: #FFF;
		width: 100%;
		text-align: center;
		height: 26px;
	}

	.front-theme-confirm.front-theme-confirm-logout {
		border: 1px solid rgba(153, 144, 124, 0.18);
		border-radius: 26px;
		overflow: hidden;
		background:
			radial-gradient(circle at top left, rgba(255, 198, 57, 0.12), transparent 28%),
			rgba(11, 19, 38, 0.96);
		box-shadow: 0 24px 60px rgba(0, 0, 0, 0.42);
	}

	.front-theme-confirm.front-theme-confirm-logout .el-message-box__header {
		padding: 24px 24px 0;
	}

	.front-theme-confirm.front-theme-confirm-logout .el-message-box__title {
		color: #f3f6ff;
		font-size: 24px;
		font-weight: 900;
		letter-spacing: -.03em;
	}

	.front-theme-confirm.front-theme-confirm-logout .el-message-box__headerbtn .el-message-box__close {
		color: rgba(243, 246, 255, 0.48);
	}

	.front-theme-confirm.front-theme-confirm-logout .el-message-box__content {
		padding: 16px 24px 10px;
	}

	.front-theme-confirm.front-theme-confirm-logout .el-message-box__message p {
		color: rgba(218, 226, 253, 0.78);
		font-size: 15px;
		font-weight: 600;
		line-height: 1.7;
	}

	.front-theme-confirm.front-theme-confirm-logout .el-message-box__btns {
		padding: 10px 24px 24px;
		display: flex;
		justify-content: flex-end;
		gap: 12px;
	}

	.front-theme-confirm.front-theme-confirm-logout .el-button {
		border-radius: 999px;
		min-width: 118px;
		height: 44px;
		font-size: 12px;
		font-weight: 900;
		letter-spacing: .16em;
		text-transform: uppercase;
	}

	.front-theme-confirm.front-theme-confirm-logout .el-button--default {
		border: 1px solid rgba(153, 144, 124, 0.42);
		color: #dae2fd;
		background: transparent;
	}

	.front-theme-confirm.front-theme-confirm-logout .el-button--primary {
		border: 0;
		color: #3f2e00;
		background: linear-gradient(135deg, #ffc639, #e1aa12);
		box-shadow: 0 10px 26px rgba(255, 198, 57, 0.28);
	}

	.front-theme-message.front-theme-message-login {
		border: 1px solid rgba(153, 144, 124, 0.18);
		border-radius: 18px;
		padding: 14px 18px;
		background:
			radial-gradient(circle at top left, rgba(255, 198, 57, 0.14), transparent 30%),
			rgba(11, 19, 38, 0.94);
		box-shadow: 0 18px 42px rgba(0, 0, 0, 0.34);
		backdrop-filter: blur(20px);
		-webkit-backdrop-filter: blur(20px);
	}

	.front-theme-message.front-theme-message-login .el-message__icon {
		margin-right: 10px;
		color: #ffc639;
		font-size: 18px;
	}

	.front-theme-message.front-theme-message-login .el-message__content {
		color: #f3f6ff;
		font-size: 14px;
		font-weight: 700;
		letter-spacing: .08em;
		text-transform: uppercase;
	}

	.front-theme-message.front-theme-message-logout {
		border: 1px solid rgba(153, 144, 124, 0.18);
		border-radius: 18px;
		padding: 14px 18px;
		background:
			radial-gradient(circle at top left, rgba(255, 198, 57, 0.14), transparent 30%),
			rgba(11, 19, 38, 0.94);
		box-shadow: 0 18px 42px rgba(0, 0, 0, 0.34);
		backdrop-filter: blur(20px);
		-webkit-backdrop-filter: blur(20px);
	}

	.front-theme-message.front-theme-message-logout .el-message__icon {
		margin-right: 10px;
		color: #ffc639;
		font-size: 18px;
	}

	.front-theme-message.front-theme-message-logout .el-message__content {
		color: #f3f6ff;
		font-size: 14px;
		font-weight: 700;
		letter-spacing: .08em;
		text-transform: uppercase;
	}
</style>
