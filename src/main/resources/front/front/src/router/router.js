import VueRouter from 'vue-router'
import { Message } from 'element-ui'
//引入组件
import Index from '../pages'
import Home from '../pages/home/home'
import Login from '../pages/login/login'
import Register from '../pages/register/register'
import Center from '../pages/center/center'
import Storeup from '../pages/storeup/list'
import AiRecommend from '../pages/ai-recommend/ai-recommend'
import payList from '../pages/pay'

import yonghuList from '../pages/yonghu/list'
import yonghuDetail from '../pages/yonghu/detail'
import yonghuAdd from '../pages/yonghu/add'
import dianyingleixingList from '../pages/dianyingleixing/list'
import dianyingleixingDetail from '../pages/dianyingleixing/detail'
import dianyingleixingAdd from '../pages/dianyingleixing/add'
import dianyingxinxiList from '../pages/dianyingxinxi/list'
import dianyingxinxiDetail from '../pages/dianyingxinxi/detail'
import dianyingxinxiAdd from '../pages/dianyingxinxi/add'
import configList from '../pages/config/list'
import configDetail from '../pages/config/detail'
import configAdd from '../pages/config/add'
import usersList from '../pages/users/list'
import usersDetail from '../pages/users/detail'
import usersAdd from '../pages/users/add'
import sensitivewordsList from '../pages/sensitivewords/list'
import sensitivewordsDetail from '../pages/sensitivewords/detail'
import sensitivewordsAdd from '../pages/sensitivewords/add'
import discussdianyingxinxiList from '../pages/discussdianyingxinxi/list'
import discussdianyingxinxiDetail from '../pages/discussdianyingxinxi/detail'
import discussdianyingxinxiAdd from '../pages/discussdianyingxinxi/add'

const {
	requiresFrontLogin,
} = require('../utils/front-access')
const {
	beginRouteLoading,
	finishRouteLoading,
	failRouteLoading,
} = require('../utils/route-loading')

const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
	return originalPush.call(this, location).catch(err => err)
}

const originalReplace = VueRouter.prototype.replace
VueRouter.prototype.replace = function replace(location) {
	return originalReplace.call(this, location).catch(err => err)
}

let pendingRouteLoadingId = 0

function shouldTrackRouteLoading(to, from) {
	if (!to || !from) {
		return false
	}
	const fromMatched = Array.isArray(from.matched) ? from.matched : []
	return fromMatched.length > 0 && to.fullPath !== from.fullPath
}

//配置路由
const router = new VueRouter({
	routes:[
		{
      path: '/',
      redirect: '/index/home'
    },
		{
			path: '/index',
			component: Index,
			children:[
				{
					path: 'home',
					component: Home
				},
				{
					path: 'center',
					component: Center,
				},
				{
					path: 'pay',
					component: payList,
				},
				{
					path: 'storeup',
					component: Storeup
				},
				{
					path: 'ai-recommend',
					component: AiRecommend
				},
				{
					path: 'yonghu',
					component: yonghuList
				},
				{
					path: 'yonghuDetail',
					component: yonghuDetail
				},
				{
					path: 'yonghuAdd',
					component: yonghuAdd
				},
				{
					path: 'dianyingleixing',
					component: dianyingleixingList
				},
				{
					path: 'dianyingleixingDetail',
					component: dianyingleixingDetail
				},
				{
					path: 'dianyingleixingAdd',
					component: dianyingleixingAdd
				},
				{
					path: 'dianyingxinxi',
					component: dianyingxinxiList
				},
				{
					path: 'dianyingxinxiDetail',
					component: dianyingxinxiDetail
				},
				{
					path: 'dianyingxinxiAdd',
					component: dianyingxinxiAdd
				},
				{
					path: 'config',
					component: configList
				},
				{
					path: 'configDetail',
					component: configDetail
				},
				{
					path: 'configAdd',
					component: configAdd
				},
				{
					path: 'users',
					component: usersList
				},
				{
					path: 'usersDetail',
					component: usersDetail
				},
				{
					path: 'usersAdd',
					component: usersAdd
				},
				{
					path: 'sensitivewords',
					component: sensitivewordsList
				},
				{
					path: 'sensitivewordsDetail',
					component: sensitivewordsDetail
				},
				{
					path: 'sensitivewordsAdd',
					component: sensitivewordsAdd
				},
				{
					path: 'discussdianyingxinxi',
					component: discussdianyingxinxiList
				},
				{
					path: 'discussdianyingxinxiDetail',
					component: discussdianyingxinxiDetail
				},
				{
					path: 'discussdianyingxinxiAdd',
					component: discussdianyingxinxiAdd
				},
			]
		},
		{
			path: '/login',
			component: Login
		},
		{
			path: '/register',
			component: Register
		},
	]
})

router.beforeEach((to, from, next) => {
	const hasFrontToken = !!localStorage.getItem('frontToken')
	const trackRouteLoading = shouldTrackRouteLoading(to, from)
	if (!hasFrontToken && requiresFrontLogin(to)) {
		if (trackRouteLoading) {
			pendingRouteLoadingId = beginRouteLoading({
				path: '/login',
				fullPath: '/login',
			})
		}
		Message.closeAll()
		Message({
			message: '请先登录后再查看个人信息或收藏内容',
			type: 'warning',
			duration: 1500,
		})
		next({
			path: '/login',
			query: {
				redirect: to.fullPath,
			},
		})
		return
	}

	if (trackRouteLoading) {
		pendingRouteLoadingId = beginRouteLoading(to)
	}

	next()
})

router.afterEach(() => {
	if (!pendingRouteLoadingId) {
		return
	}

	const navigationId = pendingRouteLoadingId
	pendingRouteLoadingId = 0
	finishRouteLoading(navigationId)
})

router.onError(() => {
	if (!pendingRouteLoadingId) {
		return
	}

	const navigationId = pendingRouteLoadingId
	pendingRouteLoadingId = 0
	failRouteLoading(navigationId)
})

export default router
