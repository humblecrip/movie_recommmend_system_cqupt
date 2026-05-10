const assert = require('assert')
const fs = require('fs')
const path = require('path')

function runTest(name, testFn) {
  try {
    testFn()
    console.log(`PASS ${name}`)
  } catch (error) {
    console.error(`FAIL ${name}`)
    throw error
  }
}

const source = fs.readFileSync(
  path.join(__dirname, '../src/pages/ai-recommend/ai-recommend.vue'),
  'utf8'
)

runTest('AI 推荐页复用统一头像解析并监听头像同步事件', () => {
  assert.ok(source.includes('resolveFrontAvatar'))
  assert.ok(source.includes('front-avatar-updated'))
})

runTest('AI 推荐页历史列表包含独立删除入口且不会触发会话选中', () => {
  assert.ok(source.includes('@click.stop="deleteConversation(chat, $event)"'))
  assert.ok(source.includes('deleteConversation(chat, event)'))
})

runTest('AI 推荐页反馈按钮不再渲染 material token 文本', () => {
  assert.ok(!source.includes('thumb_up'))
  assert.ok(!source.includes('visibility'))
  assert.ok(source.includes('feedback-text'))
})

runTest('AI 推荐页消息区改为按用户滚动状态决定是否自动贴底', () => {
  assert.ok(source.includes('@scroll="handleMessagesScroll"'))
  assert.ok(!source.includes('updated() { this.scrollToBottom() }'))
  assert.ok(source.includes('shouldAutoScrollMessages'))
  assert.ok(source.includes('handleMessagesScroll()'))
})

runTest('AI 推荐页对话历史项不再包含 chat 图标占位', () => {
  assert.ok(!source.includes('class="material-symbols-outlined chat-icon"'))
  assert.ok(!source.includes('chat_bubble'))
})

runTest('AI 推荐页消息区隐藏可见滚动条但保留滚动能力', () => {
  assert.ok(source.includes('.chat-messages {'))
  assert.ok(source.includes('overflow-y: auto;'))
  assert.ok(source.includes('.chat-messages::-webkit-scrollbar { display: none; }'))
  assert.ok(source.includes('scrollbar-width: none;'))
})
