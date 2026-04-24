<template>
	<header class="admin-header">
		<div class="header-actions">
			<button class="icon-button" type="button" @click="goHome">
				<i class="el-icon-s-home"></i>
			</button>
			<button class="icon-button has-dot" type="button">
				<i class="el-icon-message"></i>
			</button>
			<button class="icon-button" type="button">
				<i class="el-icon-setting"></i>
			</button>
			<button
				v-if="$storage.get('role') !== '管理员'"
				class="front-button"
				type="button"
				@click="onIndexTap"
			>
				前台
			</button>

			<el-dropdown trigger="click" @command="handleCommand">
				<div class="profile-card">
					<img :src="avatarUrl" alt="avatar">
					<div class="profile-copy">
						<p>{{ $storage.get('adminName') || '未命名账号' }}</p>
						<span>{{ $storage.get('role') || '后台账号' }}</span>
					</div>
					<i class="el-icon-arrow-down"></i>
				</div>
				<el-dropdown-menu slot="dropdown" class="header-dropdown-menu">
					<el-dropdown-item command="home">首页</el-dropdown-item>
					<el-dropdown-item command="center">个人中心</el-dropdown-item>
					<el-dropdown-item command="updatePassword">修改密码</el-dropdown-item>
					<el-dropdown-item v-if="$storage.get('role') !== '管理员'" command="front">
						退出到前台
					</el-dropdown-item>
					<el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
				</el-dropdown-menu>
			</el-dropdown>
		</div>
	</header>
</template>

<script>
export default {
	computed: {
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
		handleCommand(command) {
			if (command === 'logout') {
				this.onLogout()
				return
			}
			if (command === 'front') {
				this.onIndexTap()
				return
			}
			if (command === 'home') {
				this.goHome()
				return
			}
			this.$router.push(`/${command}`)
		},
		goHome() {
			this.$router.push('/')
		},
		onLogout() {
			this.$storage.clear()
			this.$store.dispatch('tagsView/delAllViews')
			this.$router.replace({
				name: 'login'
			})
		},
		onIndexTap() {
			localStorage.setItem('frontToken', localStorage.getItem('Token'))
			localStorage.setItem('frontRole', localStorage.getItem('role'))
			localStorage.setItem('frontSessionTable', localStorage.getItem('sessionTable'))
			localStorage.setItem('frontHeadportrait', localStorage.getItem('headportrait'))
			localStorage.setItem('UserTableName', localStorage.getItem('sessionTable'))
			localStorage.setItem('frontUserid', localStorage.getItem('userid'))
			localStorage.setItem('username', localStorage.getItem('adminName'))
			window.location.href = `${this.$base.indexUrl}`
		}
	}
}
</script>

<style lang="scss" scoped>
	.admin-header {
		backdrop-filter: blur(24px);
		box-sizing: border-box;
		padding: 14px 22px;
		border-bottom: 1px solid rgba(255, 255, 255, 0.06);
		background:
			linear-gradient(180deg, rgba(11, 19, 38, 0.92), rgba(11, 19, 38, 0.84));
		display: flex;
		align-items: center;
		justify-content: flex-end;
		gap: 16px;
		width: 100%;
		min-height: 74px;
	}

	.header-actions {
		display: flex;
		align-items: center;
		gap: 10px;
	}

	.icon-button,
	.front-button {
		cursor: pointer;
		outline: none;
		border: none;
		transition: transform 0.2s ease, border-color 0.2s ease, color 0.2s ease, background 0.2s ease;
	}

	.icon-button {
		position: relative;
		border: 1px solid rgba(255, 255, 255, 0.08);
		border-radius: 14px;
		color: rgba(218, 226, 253, 0.74);
		background: rgba(255, 255, 255, 0.04);
		display: inline-flex;
		align-items: center;
		justify-content: center;
		width: 40px;
		height: 40px;

		i {
			font-size: 16px;
		}

		&:hover {
			transform: translateY(-1px);
			border-color: rgba(255, 198, 57, 0.35);
			color: #ffc639;
			background: rgba(255, 198, 57, 0.08);
		}

		&.has-dot::after {
			content: '';
			position: absolute;
			top: 9px;
			right: 10px;
			border-radius: 999px;
			background: #ffc639;
			width: 7px;
			height: 7px;
			box-shadow: 0 0 0 3px rgba(255, 198, 57, 0.14);
		}
	}

	.front-button {
		border: 1px solid rgba(255, 198, 57, 0.24);
		border-radius: 999px;
		padding: 0 15px;
		color: #ffc639;
		font-size: 12px;
		font-weight: 700;
		letter-spacing: 0.08em;
		background: rgba(255, 198, 57, 0.08);
		height: 38px;

		&:hover {
			transform: translateY(-1px);
			background: rgba(255, 198, 57, 0.14);
			box-shadow: 0 12px 24px rgba(255, 198, 57, 0.12);
		}
	}

	.profile-card {
		cursor: pointer;
		border: 1px solid rgba(255, 255, 255, 0.06);
		border-radius: 20px;
		padding: 6px 10px 6px 6px;
		background: rgba(34, 42, 61, 0.76);
		display: flex;
		align-items: center;
		gap: 10px;
		min-width: 190px;

		img {
			border: 2px solid rgba(255, 198, 57, 0.24);
			border-radius: 15px;
			object-fit: cover;
			width: 36px;
			height: 36px;
		}

		i {
			color: rgba(218, 226, 253, 0.52);
			font-size: 12px;
		}
	}

	.profile-copy {
		flex: 1;

		p,
		span {
			margin: 0;
			line-height: 1.3;
		}

		p {
			color: #f5f7ff;
			font-size: 12px;
			font-weight: 700;
		}

		span {
			color: rgba(218, 226, 253, 0.5);
			font-size: 10px;
			letter-spacing: 0.12em;
			text-transform: uppercase;
		}
	}

	::v-deep .header-dropdown-menu {
		min-width: 172px;
		border: 1px solid rgba(255, 255, 255, 0.08);
		border-radius: 18px;
		padding: 8px;
		background: linear-gradient(180deg, rgba(20, 29, 50, 0.98), rgba(12, 19, 36, 0.98));
		box-shadow: 0 24px 44px rgba(0, 0, 0, 0.28);

		.el-dropdown-menu__item {
			border-radius: 12px;
			padding: 0 12px;
			color: #dae2fd;
			font-size: 12px;
			font-weight: 700;
			line-height: 38px;

			&:not(.is-disabled):hover {
				background: rgba(255, 198, 57, 0.08);
				color: #ffc639;
			}
		}

		.el-dropdown-menu__item--divided {
			margin-top: 6px;
			border-top-color: rgba(255, 255, 255, 0.08);
		}
	}

	@media (max-width: 1280px) {
		.admin-header {
			padding: 14px 16px;
		}

		.profile-card {
			min-width: auto;
		}

		.profile-copy span {
			display: none;
		}
	}
</style>
