const VueModule = require('vue')

const SHOW_DELAY_MS = 90
const MIN_VISIBLE_MS = 280
const DEFAULT_MESSAGE = '正在切换电影页面'

function resolveVueRuntime(vueModule) {
  if (vueModule && typeof vueModule.observable === 'function') {
    return vueModule
  }
  if (vueModule && vueModule.default && typeof vueModule.default.observable === 'function') {
    return vueModule.default
  }
  return vueModule && vueModule.default ? vueModule.default : vueModule
}

function createReactiveState(Vue, initialState) {
  if (Vue && typeof Vue.observable === 'function') {
    return Vue.observable(initialState)
  }

  if (Vue && Vue.util && typeof Vue.util.defineReactive === 'function') {
    const state = {}
    Object.keys(initialState).forEach(key => {
      Vue.util.defineReactive(state, key, initialState[key])
    })
    return state
  }

  return initialState
}

const Vue = resolveVueRuntime(VueModule)

const routeLoadingState = createReactiveState(Vue, {
  visible: false,
  active: false,
  phase: 'idle',
  message: DEFAULT_MESSAGE,
  routePath: '',
})

let activeNavigationId = 0
let navigationSeed = 0
let visibleSince = 0
let showTimer = null
let hideTimer = null

function clearShowTimer() {
  if (showTimer) {
    clearTimeout(showTimer)
    showTimer = null
  }
}

function clearHideTimer() {
  if (hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = null
  }
}

function resetRouteLoadingState() {
  clearShowTimer()
  clearHideTimer()
  visibleSince = 0
  routeLoadingState.visible = false
  routeLoadingState.active = false
  routeLoadingState.phase = 'idle'
  routeLoadingState.message = DEFAULT_MESSAGE
  routeLoadingState.routePath = ''
}

function resolveRoutePath(route) {
  if (!route) {
    return ''
  }
  return route.fullPath || route.path || ''
}

function beginRouteLoading(route) {
  const navigationId = ++navigationSeed
  activeNavigationId = navigationId
  clearShowTimer()
  clearHideTimer()
  routeLoadingState.active = true
  routeLoadingState.phase = 'pending'
  routeLoadingState.message = DEFAULT_MESSAGE
  routeLoadingState.routePath = resolveRoutePath(route)

  showTimer = setTimeout(() => {
    if (navigationId !== activeNavigationId || !routeLoadingState.active) {
      return
    }
    routeLoadingState.visible = true
    routeLoadingState.phase = 'visible'
    visibleSince = Date.now()
  }, SHOW_DELAY_MS)

  return navigationId
}

function settleRouteLoading(navigationId, phase) {
  if (navigationId && navigationId !== activeNavigationId) {
    return
  }

  clearShowTimer()
  routeLoadingState.active = false
  routeLoadingState.phase = phase || 'settling'

  if (!routeLoadingState.visible) {
    resetRouteLoadingState()
    return
  }

  const elapsed = visibleSince ? Date.now() - visibleSince : 0
  const waitMs = Math.max(MIN_VISIBLE_MS - elapsed, 0)

  clearHideTimer()
  hideTimer = setTimeout(() => {
    if (navigationId && navigationId !== activeNavigationId) {
      return
    }
    resetRouteLoadingState()
  }, waitMs)
}

function finishRouteLoading(navigationId) {
  settleRouteLoading(navigationId, 'settling')
}

function failRouteLoading(navigationId) {
  settleRouteLoading(navigationId, 'error')
}

function __unsafeResetRouteLoadingForTests() {
  activeNavigationId = 0
  navigationSeed = 0
  resetRouteLoadingState()
}

module.exports = {
  routeLoadingState,
  routeLoadingTiming: {
    SHOW_DELAY_MS,
    MIN_VISIBLE_MS,
  },
  beginRouteLoading,
  finishRouteLoading,
  failRouteLoading,
  __unsafeResetRouteLoadingForTests,
}
