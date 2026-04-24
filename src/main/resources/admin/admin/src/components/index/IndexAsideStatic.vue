<template>
	<aside class="admin-sidebar">
		<div class="brand-panel" @click="menuHandler('')">
			<div class="brand-mark">AC</div>
			<div class="brand-copy">
				<h1>{{ $project.projectName }}</h1>
				<p>Management Portal</p>
			</div>
		</div>

		<div class="sidebar-user">
			<img :src="avatarUrl" alt="avatar">
			<div class="sidebar-user-copy">
				<strong>{{ $storage.get('adminName') || '未命名账号' }}</strong>
				<span>{{ $storage.get('role') || '后台账号' }}</span>
			</div>
		</div>

		<div class="sidebar-scroll">
			<div class="nav-section">
				<p class="section-title">概览</p>
				<button
					class="nav-link"
					:class="{ active: isActive('/') }"
					type="button"
					@click="menuHandler('')"
				>
					<i class="el-icon-s-home"></i>
					<span>Dashboard</span>
				</button>
			</div>

			<div v-for="menu in backMenus" :key="menu.menu" class="nav-section">
				<p class="section-title">{{ menu.menu }}</p>
				<button
					v-for="child in menu.child"
					:key="child.tableName"
					class="nav-link"
					:class="{ active: isActive('/' + child.tableName) }"
					type="button"
					@click="menuHandler(child.tableName)"
				>
					<i :class="resolveIcon(menu.menu, child.menu)"></i>
					<span>{{ child.menu }}</span>
				</button>
			</div>
		</div>

		<div class="sidebar-footer">
			<button class="primary-action" type="button" @click="menuHandler('dianyingxinxi')">
				进入影片库
			</button>
		</div>
	</aside>
</template>

<script>
import menu from '@/utils/menu'

export default {
	computed: {
		backMenus() {
			const menus = menu.list()
			const role = this.$storage.get('role')
			const roleMenu = menus.find(item => item.roleName === role)
			if (!roleMenu || !roleMenu.backMenu) {
				return []
			}
			return roleMenu.backMenu.filter(item => item.child && item.child.length)
		},
		avatarUrl() {
			const avatar = this.$storage.get('headportrait')
			if (!avatar) {
				return require('@/assets/img/avator.png')
			}
			if (avatar.substring(0, 4) === 'http') {
				return avatar.split(',')[0]
			}
			return `${this.$base.url}${avatar.split(',')[0]}`
		}
	},
	methods: {
		menuHandler(name) {
			this.$router.push(`/${name}`)
		},
		isActive(path) {
			return this.$route.path === path
		},
		resolveIcon(groupName, menuName) {
			const matchText = `${groupName}${menuName}`
			if (matchText.indexOf('用户') !== -1) {
				return 'el-icon-user'
			}
			if (matchText.indexOf('电影类型') !== -1) {
				return 'el-icon-menu'
			}
			if (matchText.indexOf('电影') !== -1) {
				return 'el-icon-s-ticket'
			}
			if (matchText.indexOf('敏感词') !== -1) {
				return 'el-icon-warning'
			}
			if (matchText.indexOf('轮播图') !== -1) {
				return 'el-icon-picture'
			}
			if (matchText.indexOf('评论') !== -1) {
				return 'el-icon-edit-outline'
			}
			return 'el-icon-menu'
		}
	}
}
</script>

<style lang="scss" scoped>
	.admin-sidebar {
		box-sizing: border-box;
		padding: 20px 16px 16px;
		background:
			radial-gradient(circle at top, rgba(255, 198, 57, 0.14), transparent 28%),
			linear-gradient(180deg, #0b1326 0%, #0f1730 100%);
		display: flex;
		flex-direction: column;
		width: 232px;
		height: 100vh;
	}

	.brand-panel {
		cursor: pointer;
		margin-bottom: 18px;
		display: flex;
		align-items: center;
		gap: 12px;
	}

	.brand-mark {
		box-shadow: 0 14px 28px rgba(255, 198, 57, 0.18);
		border-radius: 18px;
		color: #3f2e00;
		font-size: 16px;
		font-weight: 900;
		letter-spacing: 0.08em;
		background: linear-gradient(135deg, #ffc639, #e1aa12);
		display: inline-flex;
		align-items: center;
		justify-content: center;
		width: 48px;
		height: 48px;
	}

	.brand-copy {
		h1,
		p {
			margin: 0;
		}

		h1 {
			color: #ffc639;
			font-size: 16px;
			font-weight: 800;
			line-height: 1.2;
		}

		p {
			color: rgba(218, 226, 253, 0.42);
			font-size: 10px;
			font-weight: 700;
			letter-spacing: 0.18em;
			text-transform: uppercase;
		}
	}

	.sidebar-user {
		border: 1px solid rgba(255, 255, 255, 0.06);
		border-radius: 20px;
		padding: 12px;
		background: rgba(255, 255, 255, 0.04);
		display: flex;
		align-items: center;
		gap: 10px;
		margin-bottom: 16px;

		img {
			border: 2px solid rgba(255, 198, 57, 0.18);
			border-radius: 14px;
			object-fit: cover;
			width: 42px;
			height: 42px;
		}
	}

	.sidebar-user-copy {
		strong,
		span {
			display: block;
			line-height: 1.3;
		}

		strong {
			color: #f6f8ff;
			font-size: 13px;
		}

		span {
			color: rgba(218, 226, 253, 0.5);
			font-size: 10px;
			letter-spacing: 0.12em;
			text-transform: uppercase;
		}
	}

	.sidebar-scroll {
		padding-right: 4px;
		overflow-y: auto;
		flex: 1;
	}

	.sidebar-scroll::-webkit-scrollbar {
		width: 6px;
	}

	.sidebar-scroll::-webkit-scrollbar-thumb {
		border-radius: 999px;
		background: rgba(255, 255, 255, 0.12);
	}

	.nav-section + .nav-section {
		margin-top: 14px;
	}

	.section-title {
		margin: 0 0 8px;
		padding-left: 6px;
		color: rgba(218, 226, 253, 0.34);
		font-size: 10px;
		font-weight: 700;
		letter-spacing: 0.18em;
		text-transform: uppercase;
	}

	.nav-link {
		cursor: pointer;
		outline: none;
		border: 1px solid transparent;
		border-radius: 15px;
		padding: 11px 12px;
		color: rgba(218, 226, 253, 0.76);
		font-size: 13px;
		font-weight: 600;
		background: transparent;
		display: flex;
		align-items: center;
		gap: 10px;
		width: 100%;
		transition: transform 0.2s ease, border-color 0.2s ease, color 0.2s ease, background 0.2s ease;

		& + .nav-link {
			margin-top: 4px;
		}

		i {
			font-size: 14px;
			width: 16px;
		}

		&:hover,
		&.active {
			transform: translateX(2px);
			border-color: rgba(255, 198, 57, 0.18);
			color: #ffc639;
			background: rgba(255, 198, 57, 0.08);
		}
	}

	.sidebar-footer {
		padding-top: 14px;
		margin-top: 14px;
		border-top: 1px solid rgba(255, 255, 255, 0.06);
		display: grid;
	}

	.primary-action {
		cursor: pointer;
		outline: none;
		border-radius: 999px;
		padding: 0 15px;
		font-size: 12px;
		font-weight: 700;
		letter-spacing: 0.08em;
		height: 38px;
		transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, color 0.2s ease;
	}

	.primary-action {
		border: 1px solid rgba(255, 198, 57, 0.24);
		color: #ffc639;
		background: rgba(255, 198, 57, 0.08);

		&:hover {
			transform: translateY(-1px);
			background: rgba(255, 198, 57, 0.14);
			box-shadow: 0 16px 30px rgba(255, 198, 57, 0.12);
		}
	}
</style>
