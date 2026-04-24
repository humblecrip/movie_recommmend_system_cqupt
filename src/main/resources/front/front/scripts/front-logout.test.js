const assert = require('assert')

const {
  buildFrontLogoutConfirmOptions,
  buildFrontLogoutHashTarget,
  executeFrontLogout,
  resolveFrontLogoutRedirectPath,
} = require('../src/utils/front-logout')

function runTest(name, testFn) {
  try {
    testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  }
}

runTest('退出确认弹窗使用统一文案和样式', () => {
  const result = buildFrontLogoutConfirmOptions()

  assert.deepStrictEqual(result, {
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
  })
})

runTest('退出后默认跳转到首页', () => {
  assert.strictEqual(resolveFrontLogoutRedirectPath(), '/index/home')
  assert.strictEqual(resolveFrontLogoutRedirectPath({ redirectPath: '/index/dianyingxinxi' }), '/index/dianyingxinxi')
})

runTest('退出后生成 hash 首页跳转目标', () => {
  assert.strictEqual(buildFrontLogoutHashTarget(), '#/index/home')
  assert.strictEqual(buildFrontLogoutHashTarget('/index/storeup'), '#/index/storeup')
})

runTest('缺少 Vue.http 时退出逻辑也不会报错且会跳转首页', () => {
  const originalWindow = global.window
  const originalLocalStorage = global.localStorage
  const originalCustomEvent = global.CustomEvent

  const calls = {
    cleared: false,
    forced: false,
    messaged: false,
    messageOptions: null,
  }

  global.localStorage = {
    clear() {
      calls.cleared = true
    },
  }

  global.window = {
    location: {
      hash: '',
    },
    dispatchEvent() {},
  }

  global.CustomEvent = function CustomEvent(type, payload) {
    return { type, payload }
  }

  try {
    executeFrontLogout({
      $http: {
        headers: {
          common: {
            Token: 'abc',
          },
        },
      },
      $forceUpdate() {
        calls.forced = true
      },
      $message(options) {
        calls.messaged = true
        calls.messageOptions = options
      },
    })

    assert.strictEqual(calls.cleared, true)
    assert.strictEqual(calls.forced, true)
    assert.strictEqual(calls.messaged, true)
    assert.strictEqual(global.window.location.hash, '#/index/home')
    assert.deepStrictEqual(calls.messageOptions, {
      message: '登出成功',
      customClass: 'front-theme-message front-theme-message-logout',
      iconClass: 'el-icon-switch-button',
      duration: 1500,
      offset: 28,
    })
  } finally {
    global.window = originalWindow
    global.localStorage = originalLocalStorage
    global.CustomEvent = originalCustomEvent
  }
})
