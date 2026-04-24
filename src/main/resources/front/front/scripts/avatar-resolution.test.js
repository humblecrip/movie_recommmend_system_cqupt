const assert = require('assert')

const {
  parseSessionForm,
  getSessionAvatar,
  getFrontIdentityState,
  resolveFrontAvatar,
} = require('../src/utils/front-avatar')

function runTest(name, testFn) {
  try {
    testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  }
}

runTest('空头像时回退到兜底图', () => {
  const avatar = resolveFrontAvatar({
    sessionForm: {},
    cachedAvatar: '',
    baseUrl: 'http://localhost:8080/',
    fallbackAvatar: '/assets/avatar-fallback.png',
  })

  assert.strictEqual(avatar, '/assets/avatar-fallback.png')
})

runTest('http 头像地址保持原样', () => {
  const avatar = resolveFrontAvatar({
    sessionForm: {},
    cachedAvatar: 'https://cdn.example.com/avatar.png',
    baseUrl: 'http://localhost:8080/',
    fallbackAvatar: '/assets/avatar-fallback.png',
  })

  assert.strictEqual(avatar, 'https://cdn.example.com/avatar.png')
})

runTest('相对路径头像自动拼接 baseUrl', () => {
  const avatar = resolveFrontAvatar({
    sessionForm: {},
    cachedAvatar: 'upload/test.png',
    baseUrl: 'http://localhost:8080/',
    fallbackAvatar: '/assets/avatar-fallback.png',
  })

  assert.strictEqual(avatar, 'http://localhost:8080/upload/test.png')
})

runTest('session 头像优先于旧缓存头像', () => {
  const avatar = resolveFrontAvatar({
    sessionForm: {
      touxiang: 'upload/new-user.png',
    },
    cachedAvatar: 'upload/old-user.png',
    baseUrl: 'http://localhost:8080/',
    fallbackAvatar: '/assets/avatar-fallback.png',
  })

  assert.strictEqual(avatar, 'http://localhost:8080/upload/new-user.png')
})

runTest('无效 sessionForm 字符串会被安全兜底', () => {
  assert.deepStrictEqual(parseSessionForm('{bad json'), {})
  assert.strictEqual(getSessionAvatar(parseSessionForm('{bad json')), '')
})

runTest('未登录时返回文字占位而不是兜底头像', () => {
  const identity = getFrontIdentityState({
    hasToken: false,
    sessionForm: {},
    cachedAvatar: '',
    baseUrl: 'http://localhost:8080/',
    fallbackAvatar: '/assets/avatar-fallback.png',
    placeholderText: '去登录',
  })

  assert.deepStrictEqual(identity, {
    mode: 'placeholder',
    text: '去登录',
    src: '',
  })
})

runTest('已登录无头像时仍返回兜底头像', () => {
  const identity = getFrontIdentityState({
    hasToken: true,
    sessionForm: {},
    cachedAvatar: '',
    baseUrl: 'http://localhost:8080/',
    fallbackAvatar: '/assets/avatar-fallback.png',
    placeholderText: '去登录',
  })

  assert.deepStrictEqual(identity, {
    mode: 'avatar',
    text: '',
    src: '/assets/avatar-fallback.png',
  })
})
