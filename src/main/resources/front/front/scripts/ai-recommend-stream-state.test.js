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

runTest('发送消息时不再预先插入空助手气泡', () => {
  const sendMessageStart = source.indexOf('async sendMessage() {')
  const onMessageStart = source.indexOf('eventSource.onmessage = async (event) => {')
  const pushIndex = source.indexOf('this.messages.push(assistantMessage)')

  assert.ok(sendMessageStart >= 0)
  assert.ok(onMessageStart > sendMessageStart)
  assert.ok(pushIndex > onMessageStart)
})

runTest('首个 SSE chunk 到来后才创建真实助手消息', () => {
  assert.ok(source.includes('if (!assistantMessage.text) {'))
  assert.ok(source.includes("this.messages.push(assistantMessage)"))
})
