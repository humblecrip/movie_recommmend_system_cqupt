<template>
	<div class="main-containers">
		<div class="body-containers" :class="{'body-containers--terminal': isHomePrototype}">
			<router-view id="scrollView"></router-view>
			
			<div class="bottom-preview" v-if="!isHomePrototype">
				<div class="footer"><div v-html="bottomContent"></div></div>
			</div>
		</div>
		
	</div>
</template>

<script>
const { getSessionAvatar } = require('../utils/front-avatar')
const {
  confirmFrontLogout,
  dispatchFrontAvatarChanged,
} = require('../utils/front-logout')

export default {
  data() {
    return {
      queryList: [
        {
          queryName: '电影名称',
        },
      ],
      queryIndex: 0,
      dianyingxinxidianyingmingcheng: '',
      activeIndex: '0',
      baseUrl: '',
      menuList: [],
      headportrait: localStorage.getItem('frontHeadportrait') ? localStorage.getItem('frontHeadportrait') : '',
      Token: localStorage.getItem('frontToken'),
      username: localStorage.getItem('username'),
      notAdmin: localStorage.getItem('frontSessionTable') != '"users"',
      iconArr: [
        'el-icon-star-off',
        'el-icon-goods',
        'el-icon-warning',
        'el-icon-question',
        'el-icon-info',
        'el-icon-help',
        'el-icon-picture-outline-round',
        'el-icon-camera-solid',
        'el-icon-video-camera-solid',
        'el-icon-video-camera',
        'el-icon-bell',
        'el-icon-s-cooperation',
        'el-icon-s-order',
        'el-icon-s-platform',
        'el-icon-s-operation',
        'el-icon-s-promotion',
        'el-icon-s-release',
        'el-icon-s-ticket',
        'el-icon-s-management',
        'el-icon-s-open',
        'el-icon-s-shop',
        'el-icon-s-marketing',
        'el-icon-s-flag',
        'el-icon-s-comment',
        'el-icon-s-finance',
        'el-icon-s-claim',
        'el-icon-s-opportunity',
        'el-icon-s-data',
        'el-icon-s-check',
      ],
      bottomContent: '',
      showType4: -1,
    }
  },
  async created() {
    this.baseUrl = this.$config.baseUrl
    this.menuList = this.$config.indexNav
    if (localStorage.getItem('frontToken')) {
      this.getSession()
    }
    const cateList = this.$config.cateList || []
    if (cateList.length) {
      for (let x in this.menuList) {
        for (let i in cateList) {
          if (this.menuList[x].name == cateList[i].name) {
            await this.$http.get(`option/${cateList[i].refTable}/${cateList[i].refColumn}`).then(rs => {
              this.$set(this.menuList[x], 'cateList', rs.data.data)
              this.$set(this.menuList[x], 'hasCate', true)
            })
          }
        }
      }
    }
  },
  mounted() {
    this.activeIndex = localStorage.getItem('keyPath') || '0'
  },
  computed: {
    isHomePrototype() {
      return this.$route.path === '/index/home'
    },
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
  },
  watch: {
    $route(newValue) {
      const url = window.location.href
      const arr = url.split('#')
      for (let x in this.menuList) {
        if (newValue.path === this.menuList[x].url) {
          this.activeIndex = x
        }
      }
      this.Token = localStorage.getItem('frontToken')
      if (arr[1] !== '/index/home') {
        const element = document.getElementById('scrollView')
        if (element) {
          window.scrollTo(0, element.offsetTop)
        }
      } else {
        window.scrollTo(0, 0)
      }
    },
    headportrait() {
      this.$forceUpdate()
    },
  },
  methods: {
    notifyAvatarChanged(sessionForm, cachedAvatar) {
      dispatchFrontAvatarChanged(sessionForm, cachedAvatar)
    },
    cateClick(url, fenlei) {
      this.$router.push(url + '?homeFenlei=' + fenlei)
    },
    preHttp(str) {
      return str && str.substr(0, 4) === 'http'
    },
    search(tablename) {
      if (this.queryIndex === 0 && this.dianyingxinxidianyingmingcheng) {
        this.$router.push({ path: '/index/' + tablename, query: { indexQueryCondition: this.dianyingxinxidianyingmingcheng } })
      }
    },
    async getSession() {
      await this.$http.get(`${localStorage.getItem('UserTableName')}/session`, { emulateJSON: true }).then(async res => {
        if (res.data.code === 0) {
          if (localStorage.getItem('UserTableName') === 'yonghu') {
            localStorage.setItem('username', res.data.data.yonghuzhanghao)
          }
          localStorage.setItem('sessionForm', JSON.stringify(res.data.data))
          localStorage.setItem('frontUserid', res.data.data.id)
          if (res.data.data.vip) {
            localStorage.setItem('vip', res.data.data.vip)
          }
          const sessionAvatar = getSessionAvatar(res.data.data)
          this.headportrait = sessionAvatar
          if (sessionAvatar) {
            localStorage.setItem('frontHeadportrait', sessionAvatar)
          } else {
            localStorage.removeItem('frontHeadportrait')
          }
          this.notifyAvatarChanged(res.data.data, sessionAvatar)
        }
      })
    },
    handleSelect(keyPath) {
      if (keyPath) {
        localStorage.setItem('keyPath', keyPath)
      }
    },
    toLogin() {
      this.$router.push('/login')
    },
    logout() {
      return confirmFrontLogout(this, {
        afterClear: () => {
          this.activeIndex = '0'
          localStorage.setItem('keyPath', this.activeIndex)
          this.Token = ''
          this.headportrait = ''
        },
      })
    },
    goBackend() {
      localStorage.setItem('Token', localStorage.getItem('frontToken'))
      localStorage.setItem('role', localStorage.getItem('frontRole'))
      localStorage.setItem('sessionTable', localStorage.getItem('frontSessionTable'))
      localStorage.setItem('headportrait', localStorage.getItem('frontHeadportrait'))
      localStorage.setItem('userid', Number(localStorage.getItem('frontUserid')))
      localStorage.setItem('adminName', localStorage.getItem('username'))
      localStorage.setItem('userForm', JSON.stringify(localStorage.getItem('sessionForm')))
      window.location.href = `${this.$config.baseUrl}admin/dist/index.html`
    },
    menuShowClick4(index) {
      this.showType4 = index
    },
    goMenu(path) {
      this.$router.push(path)
    },
    handleCommand(name) {
      if (name === 'register') {
        this.logout()
      } else if (name === 'user') {
        this.goMenu('/index/center')
      } else if (name === 'login') {
        this.toLogin()
      }
    },
  },
}
</script>


<style rel="stylesheet/scss" lang="scss" scoped>
	.top-el-dropdown-menu {
		border: 1px solid #EBEEF5;
		border-radius: 4px;
		padding: 10px 0;
		box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
		margin: 18px 0;
		color: #000;
		background: #fff;
		.user-item {
			border: 0;
			padding: 0 8px;
			margin: 0 0px;
			color: inherit;
			background: #fff;
			width: auto;
			font-size: inherit;
			line-height: 32px;
			height: 32px;
			.icon {
				color: inherit;
				font-size: inherit;
			}
		}
		.user-item:hover {
			color: #333;
			background: #475a8330;
		}
		.register-item {
			border: 0;
			padding: 0 8px;
			margin: 0 0px;
			color: inherit;
			background: inherit;
			width: auto;
			font-size: inherit;
			line-height: 32px;
			height: 32px;
			.icon {
				color: inherit;
				font-size: inherit;
			}
		}
		.register-item:hover {
			cursor: pointer;
			color: inherit;
			background: none;
		}
	}
	.main-containers {
		.body-containers {
			padding: 0px 0 0;
			margin: 0;
			background: #f6f6f6;
			min-height: 100vh;
			position: relative;
			.top-container {
				padding: 10px 20px 10px;
				z-index: 1002;
				color: #fff;
				display: flex;
				font-size: 16px;
				box-shadow: 0 0px 0px rgba(64, 158, 255, .3);
				top: 0;
				left: 0;
				background: rgb(45, 50, 64);
				width: 100%;
				justify-content: flex-end;
				align-items: center;
				position: inherit;
				height: 100px;
				.top_title {
					left: 50px;
					bottom: -60px;
					display: block;
					position: absolute;
					span {
						padding: 0;
						color: inherit;
						font-weight: 600;
						font-size: 20px;
						line-height: 44px;
						float: left;
					}
				}
				.top_tel {
					margin: 0 10px;
					color: inherit;
					font-size: 16px;
				}
				// -------- search --------
				.search {
					border-radius: 10px;
					margin: 0;
					top: 350px;
					left: 33%;
					background: rgba(255,255,255,.9);
					display: none;
					position: absolute;
					height: auto;
					.select {
						padding: 0;
						margin: 0;
						.el-select {
							padding: 0;
							margin: 0;
							width: 100%;
							::v-deep .el-input__inner {
								border: 0;
								border-radius: 0px;
								padding: 0 30px 0 10px;
								outline: none;
								margin: 0;
								color: #666;
								background: none;
								width: 180px;
								font-size: 14px;
								height: 44px;
							}
						}
					}
					.input {
						padding: 0;
						margin: 0;
						.el-input {
							width: 100%;
							::v-deep .el-input__inner {
								border: 0;
								border-radius: 0px;
								padding: 0 10px;
								outline: none;
								color: rgba(64, 158, 255, 1);
								background: none;
								width: 380px;
								font-size: 14px;
								height: 44px;
							}
						}
					}
					.btn {
						padding: 0;
						margin: 0;
						.search_btn {
							border: 0;
							cursor: pointer;
							border-radius: 10px;
							padding: 0 20px;
							margin: 0;
							outline: none;
							color: #fff;
							background: #fcbb78;
							width: auto;
							font-size: 16px;
							line-height: 44px;
							height: 44px;
							.icon {
								margin: 0 4px 0 0;
								color: rgba(255, 255, 255, 1);
								display: none;
								font-size: 16px;
							}
						}
						.search_btn:hover {
						}
					}
				}
				// -------- search --------
				.dropdown-box {
					color: inherit;
					display: flex;
					font-size: inherit;
					right: 20px;
					.el-dropdown-link {
						color: inherit;
						display: flex;
						font-size: inherit;
						align-items: center;
						.top_avatar2 {
							border-radius: 100%;
							margin: 0 10px;
							object-fit: cover;
							display: inline-block;
							width: 40px;
							height: 40px;
						}
						.top_label2 {
							color: inherit;
							font-size: inherit;
							line-height: 32px;
						}
						.top_nickname2 {
							color: inherit;
							font-size: inherit;
							line-height: 32px;
						}
						.icon {
							margin: 0 0 0 5px;
							color: #666;
							font-size: 14px;
						}
						.login-item {
							border: 0;
							padding: 0 8px;
							margin: 0 0px;
							color: inherit;
							background: inherit;
							width: auto;
							font-size: inherit;
							line-height: 32px;
							height: 32px;
							.icon {
								color: inherit;
								font-size: inherit;
							}
						}
						.login-item:hover {
							cursor: pointer;
							color: inherit;
							background: none;
						}
					}
				}
			}
			.menu-preview {
				.el-scrollbar {
					height: 100%;
			  
					& ::v-deep .scrollbar-wrapper-vertical {
						overflow-x: hidden;
					}
			  
					& ::v-deep .scrollbar-wrapper-horizontal {
						overflow-y: hidden;
			  
						.el-scrollbar__view {
							white-space: nowrap;
						}
					}
				}
				padding: 5px;
				margin: 0px auto 0;
				z-index: 1003;
				background: rgb(0, 78, 162);
				width: 100%;
				.menu-list {
					padding: 0 10px;
					margin: 0 auto;
					color: #fff;
					background: none;
					display: flex;
					width: 100%;
					line-height: 68px;
					justify-content: flex-end;
					position: relative;
					height: 68px;
					// 首页
					.menu-home {
						cursor: pointer;
						color: #fff;
						.title {
							cursor: pointer;
							padding: 0 20px;
							color: inherit;
							background: none;
							display: flex;
							.icon {
								padding: 0 10px;
								margin: 0;
								color: inherit;
								width: 14px;
								font-size: inherit;
								line-height: inherit;
								height: inherit;
							}
							.text {
								padding: 0 10px;
								color: inherit;
								font-size: inherit;
								line-height: inherit;
								height: inherit;
							}
						}
					}
					.menu-home:hover {
						.title {
							color: inherit;
							background: none;
						}
					}
					.menu-home.menu-active {
						.title {
							border-radius: 0 0 15px 15px;
							color: inherit;
							background: rgb(45, 50, 64);
						}
					}
					// 其他盒子
					.menu-item {
						color: inherit;
						background: none;
						.title {
							cursor: pointer;
							padding: 0 20px;
							color: inherit;
							background: none;
							display: flex;
							span {
								padding: 0 10px;
								margin: 0;
								color: inherit;
								width: 14px;
								font-size: inherit;
								line-height: inherit;
								height: inherit;
							}
							.text {
								padding: 0 10px;
								color: inherit;
								font-size: inherit;
								line-height: inherit;
								height: inherit;
							}
						}
						.menu-child-list {
							z-index: 11;
							flex-direction: column;
							background: rgba(255,255,255,.9);
							display: flex;
							width: 200px;
							justify-content: flex-start;
							position: absolute;
							flex-wrap: wrap;
							.child-item {
								cursor: pointer;
								padding: 0 20px;
								color: #333;
								width: 100% !important;
								font-size: 15px;
								line-height: 40px;
							}
							.child-item:hover {
								color: #0674fc;
								background: none;
							}
						}
					}
					.menu-item:hover {
						.title {
							color: inherit;
							background: none;
						}
					}
					.menu-item.menu-active {
						.title {
							border-radius: 0 0 15px 15px;
							color: inherit;
							background: rgb(45, 50, 64);
						}
					}
					// 个人中心
					.menu-user {
						cursor: pointer;
						color: inherit;
						display: none;
						.title {
							padding: 0 20px;
							color: inherit;
							display: flex;
							height: 42px;
							.icon {
								padding: 0 10px;
								margin: 0;
								color: inherit;
								width: 14px;
								font-size: 14px;
								line-height: inherit;
								height: inherit;
							}
							.text {
								padding: 0 10px;
								color: inherit;
								font-size: 14px;
								line-height: inherit;
								height: inherit;
							}
						}
					}
					.menu-user:hover {
						.title {
							color: inherit;
							background: none;
						}
					}
					.menu-user.menu-active {
						.title {
							border-radius: 0 0 15px 15px;
							color: inherit;
							background: rgb(45, 50, 64);
						}
					}
				}
			}
			.banner-preview {
				margin: 0 auto;
				width: 100%;
				position: relative;
				height: auto;
				.swiper-button-prev:after {
					display:none;
				}
				.swiper-button-next:after {
					display:none;
				}
				.swiper-slide {
					.swiper-item {
						width: 100%;
						height: auto;
						.el-image {
							object-fit: cover;
							width: 100%;
							height: 600px;
						}
					}
				}
				@keyframes wave1 {from { left: -236px } to { left: -1233px }}
				@keyframes wave2 {from { left: 0 } to { left: -1009px }}
				.swiper-pagination {
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
					margin: -12px calc((100% - 1200px)/2) 0 0;
					top: 50%;
					display: none;
					width: 24px;
					height: 24px;
					.icon {
						color: #fff;
						width: 24px;
						font-size: 24px;
						height: 24px;
					}
				}
				.swiper-button-prev {
					margin: -12px 0 0 calc((100% - 1200px)/2);
					top: 50%;
					display: none;
					width: 24px;
					height: 24px;
					.icon {
						color: #fff;
						width: 24px;
						font-size: 24px;
						height: 24px;
					}
				}
			}
			.bottom-preview {
				width: 100%;
				height: auto;
				.footer {
					padding: 20px calc((100% - 1200px)/2);
					margin: 0 auto;
					overflow: hidden;
					color: #fff;
					background: #000;
					width: 100%;
					min-height: 120px;
					text-align: center;
					height: auto;
				}
			}
		}
	}
</style>
