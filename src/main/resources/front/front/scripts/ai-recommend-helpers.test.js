const assert = require('assert')

const {
  getNextConversationIdAfterDelete,
  isNearMessagesBottom,
  normalizeIntentActions,
  mapAiMessage,
} = require('../src/pages/ai-recommend/ai-recommend-helpers')

function runTest(name, testFn) {
  try {
    testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  }
}

runTest('删除当前会话时优先切换到后一条会话', () => {
  const nextId = getNextConversationIdAfterDelete([
    { id: 11 },
    { id: 22 },
    { id: 33 },
  ], 22, 22)

  assert.strictEqual(nextId, 33)
})

runTest('删除最后一个当前会话时回退到前一条会话', () => {
  const nextId = getNextConversationIdAfterDelete([
    { id: 11 },
    { id: 22 },
    { id: 33 },
  ], 33, 33)

  assert.strictEqual(nextId, 22)
})

runTest('删除非当前会话时保持当前会话不变', () => {
  const nextId = getNextConversationIdAfterDelete([
    { id: 11 },
    { id: 22 },
    { id: 33 },
  ], 11, 22)

  assert.strictEqual(nextId, 22)
})

runTest('删除唯一会话时清空当前会话', () => {
  const nextId = getNextConversationIdAfterDelete([{ id: 11 }], 11, 11)

  assert.strictEqual(nextId, null)
})

runTest('消息列表接近底部时保持自动跟随', () => {
  const nearBottom = isNearMessagesBottom({
    scrollTop: 732,
    clientHeight: 600,
    scrollHeight: 1360,
  })

  assert.strictEqual(nearBottom, true)
})

runTest('消息列表明显离开底部时停止自动跟随', () => {
  const nearBottom = isNearMessagesBottom({
    scrollTop: 420,
    clientHeight: 600,
    scrollHeight: 1360,
  })

  assert.strictEqual(nearBottom, false)
})

runTest('结构化跳转动作会被标准化为前端可直接消费的数据', () => {
  const actions = normalizeIntentActions([
    {
      actionType: 'navigate',
      label: '去修改密码',
      targetRoute: '/index/center',
      query: {
        section: 'password',
        ignored: '',
      },
    },
    {
      actionType: 'noop',
      label: '忽略我',
      targetRoute: '',
    },
  ])

  assert.deepStrictEqual(actions, [
    {
      actionType: 'navigate',
      label: '去修改密码',
      targetRoute: '/index/center',
      query: {
        section: 'password',
      },
    },
  ])
})

runTest('助手消息映射会保留动作数据并清洗正文格式', () => {
  const mapped = mapAiMessage({
    id: 9,
    role: 'assistant',
    content: '**请点击下面按钮**',
    recommendationReason: '因为你提到了账号安全',
    movieId: null,
    intentActions: [
      {
        actionType: 'navigate',
        label: '去登录',
        targetRoute: '/login',
        query: null,
      },
    ],
  })

  assert.strictEqual(mapped.text, '请点击下面按钮')
  assert.strictEqual(mapped.reason, '因为你提到了账号安全')
  assert.deepStrictEqual(mapped.actions, [
    {
      actionType: 'navigate',
      label: '去登录',
      targetRoute: '/login',
      query: null,
    },
  ])
})

runTest('助手消息映射在没有推荐电影时不绑定卡片', () => {
  const mapped = mapAiMessage({
    id: 11,
    role: 'assistant',
    content: '可以，点击下面按钮进入个人中心。',
    movieId: null,
    recommendedMovies: [],
  })

  assert.strictEqual(mapped.movie, null)
  assert.deepStrictEqual(mapped.movies, [])
})

runTest('助手消息映射兼容单电影卡片', () => {
  const mapped = mapAiMessage({
    id: 12,
    role: 'assistant',
    content: '优先可以看《流浪地球2》。',
    movieId: 2,
  })

  assert.deepStrictEqual(mapped.movie, { id: 2 })
  assert.deepStrictEqual(mapped.movies, [{ id: 2 }])
})

runTest('助手消息映射支持多部推荐电影卡片', () => {
  const mapped = mapAiMessage({
    id: 13,
    role: 'assistant',
    content: '这次给你三部候选。',
    movieId: 2,
    recommendedMovies: [
      { id: 2, title: '隐入尘烟' },
      { id: 3, title: '扬名立万' },
      { id: 1, title: '流浪地球2' },
    ],
  })

  assert.deepStrictEqual(mapped.movie, { id: 2 })
  assert.deepStrictEqual(mapped.movies, [
    { id: 2, title: '隐入尘烟' },
    { id: 3, title: '扬名立万' },
    { id: 1, title: '流浪地球2' },
  ])
})
