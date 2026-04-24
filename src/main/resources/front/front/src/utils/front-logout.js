const Vue = require('vue')

function buildFrontLogoutConfirmOptions() {
  return {
    message: '确认退出当前登录状态吗？',
    title: '退出确认',
    options: {
      confirmButtonText: '退出登录',
      cancelButtonText: '暂不退出',
      type: 'warning',
      customClass: 'front-theme-confirm front-theme-confirm-logout',
      distinguishCancelAndClose: true,
      closeOnClickModal: false,
    },
  }
}

function dispatchFrontAvatarChanged(sessionForm, cachedAvatar) {
  if (typeof window === 'undefined' || !window.dispatchEvent) {
    return
  }

  window.dispatchEvent(new CustomEvent('front-avatar-updated', {
    detail: {
      sessionForm: sessionForm || {},
      cachedAvatar: cachedAvatar || '',
    },
  }))
}

function resolveFrontLogoutRedirectPath(extraOptions) {
  const options = extraOptions || {}
  return options.redirectPath || '/index/home'
}

function buildFrontLogoutHashTarget(targetPath) {
  return `#${targetPath || '/index/home'}`
}

function clearFrontHttpToken(vm) {
  if (vm && vm.$http && vm.$http.headers && vm.$http.headers.common) {
    vm.$http.headers.common.Token = ''
  }

  if (Vue.http && Vue.http.headers && Vue.http.headers.common) {
    Vue.http.headers.common.Token = ''
  }
}

function executeFrontLogout(vm, extraOptions) {
  const options = extraOptions || {}

  localStorage.clear()
  clearFrontHttpToken(vm)
  dispatchFrontAvatarChanged({}, '')

  if (typeof options.afterClear === 'function') {
    options.afterClear()
  }

  const targetPath = resolveFrontLogoutRedirectPath(options)
  const targetHash = buildFrontLogoutHashTarget(targetPath)

  if (typeof window !== 'undefined' && window.location) {
    window.location.hash = targetHash
  } else if (vm && vm.$router) {
    vm.$router.replace(targetPath).catch(() => {
      vm.$router.push(targetPath).catch(() => {})
    })
  }

  if (vm && typeof vm.$forceUpdate === 'function') {
    vm.$forceUpdate()
  }

  if (vm && typeof vm.$message === 'function') {
    vm.$message({
      message: '登出成功',
      customClass: 'front-theme-message front-theme-message-logout',
      iconClass: 'el-icon-switch-button',
      duration: 1500,
      offset: 28,
    })
  }
}

function confirmFrontLogout(vm, extraOptions) {
  const confirmConfig = buildFrontLogoutConfirmOptions()
  return vm.$confirm(
    confirmConfig.message,
    confirmConfig.title,
    confirmConfig.options,
  ).then(() => {
    executeFrontLogout(vm, extraOptions)
    return true
  }).catch(action => {
    if (action !== 'cancel' && action !== 'close') {
      throw action
    }
    return false
  })
}

module.exports = {
  buildFrontLogoutConfirmOptions,
  clearFrontHttpToken,
  confirmFrontLogout,
  dispatchFrontAvatarChanged,
  executeFrontLogout,
  buildFrontLogoutHashTarget,
  resolveFrontLogoutRedirectPath,
}
