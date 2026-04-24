<template>
	<div class="admin-layout">
		<div class="layout-aside">
			<index-aside></index-aside>
		</div>

		<div class="layout-main">
			<div class="layout-header">
				<index-header></index-header>
			</div>

			<main class="layout-content" :class="{ 'layout-content-home': isHomeRoute }">
				<section v-if="!isHomeRoute" class="page-tools">
					<div class="page-tools-card">
						<bread-crumbs
							:title="title"
							class="page-breadcrumb"
							:style='{"padding":"0","margin":"0","width":"100%","background":"transparent","borderWidth":"0","fontSize":"14px"}'
						></bread-crumbs>
						<tags-view class="page-tags"></tags-view>
					</div>
				</section>

				<transition name="el-zoom-in-top">
					<router-view class="router-view" />
				</transition>
			</main>
		</div>
	</div>
</template>

<script>
import IndexAside from '@/components/index/IndexAsideStatic'
import IndexHeader from '@/components/index/IndexHeader'
import TagsView from '@/components/index/TagsView'

export default {
	components: {
		IndexAside,
		IndexHeader,
		TagsView
	},
	data() {
		return {
			title: ''
		}
	},
	computed: {
		isHomeRoute() {
			return this.$route.path === '/'
		}
	},
	watch: {
		$route: {
			handler(route) {
				this.title = route.name || ''
			},
			immediate: true
		}
	}
}
</script>

<style lang="scss" scoped>
	.admin-layout {
		background:
			radial-gradient(circle at top, rgba(255, 198, 57, 0.08), transparent 20%),
			linear-gradient(180deg, #0b1326 0%, #111b30 100%);
		display: flex;
		min-height: 100vh;
	}

	.layout-aside {
		position: fixed;
		top: 0;
		left: 0;
		z-index: 30;
		width: 232px;
		height: 100vh;
		box-shadow: 20px 0 42px rgba(0, 0, 0, 0.35);
	}

	.layout-main {
		position: relative;
		margin-left: 232px;
		width: calc(100% - 232px);
		min-height: 100vh;
	}

	.layout-header {
		position: fixed;
		top: 0;
		right: 0;
		left: 232px;
		z-index: 24;
	}

	.layout-content {
		box-sizing: border-box;
		padding: 92px 20px 24px;
		min-height: 100vh;
	}

	.layout-content-home {
		padding-top: 84px;
	}

	.page-tools {
		margin-bottom: 18px;
	}

	.page-tools-card {
		border: 1px solid rgba(255, 255, 255, 0.06);
		border-radius: 24px;
		padding: 14px 16px;
		background: rgba(255, 255, 255, 0.04);
		backdrop-filter: blur(20px);
	}

	.page-tags {
		margin-top: 10px;
	}

	::v-deep .page-breadcrumb .el-breadcrumb__inner,
	::v-deep .page-breadcrumb .el-breadcrumb__inner a,
	::v-deep .page-breadcrumb .el-breadcrumb__separator {
		color: #ffc639;
	}

	::v-deep .tags-view-container {
		border: none;
		background: transparent;
	}

	::v-deep .tags-view-wrapper .tags-view-item {
		border: 1px solid rgba(255, 255, 255, 0.08);
		border-radius: 999px;
		color: #ffc639;
		background: rgba(255, 255, 255, 0.04);
	}

	::v-deep .tags-view-wrapper .tags-view-item.active {
		border-color: rgba(255, 198, 57, 0.18);
		color: #ffc639;
		background: rgba(255, 198, 57, 0.08);
	}

	.router-view {
		width: 100%;
		transition: all 0.4s ease;
	}

	@media (max-width: 1400px) {
		.layout-content {
			padding-left: 16px;
			padding-right: 16px;
		}
	}
</style>
