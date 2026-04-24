const assert = require('assert')
const fs = require('fs')
const path = require('path')

const detailPath = path.join(__dirname, '../src/pages/dianyingxinxi/detail.vue')
const editorPath = path.join(__dirname, '../src/components/Editor.vue')
const detailSource = fs.readFileSync(detailPath, 'utf8')
const editorSource = fs.readFileSync(editorPath, 'utf8')

function expectStructure(condition, message, filePath) {
  assert.ok(condition, `${message} [检查文件: ${filePath}]`)
}

function run() {
  expectStructure(detailSource.includes('comment-composer-card'), '电影详情评论区应使用新的评论输入卡片结构', detailPath)
  expectStructure(detailSource.includes('comment-card'), '电影详情评论列表应使用新的评论卡片结构', detailPath)
  expectStructure(detailSource.includes('toolbar-preset="comment"'), '电影详情评论编辑器应使用评论场景工具栏预设', detailPath)
  expectStructure(detailSource.includes('comment-empty-state'), '电影详情评论区应提供空态样式节点', detailPath)

  expectStructure(editorSource.includes('COMMENT_TOOLBAR_OPTIONS'), '编辑器组件应提供评论场景工具栏预设', editorPath)
  expectStructure(editorSource.includes('toolbarPreset'), '编辑器组件应支持工具栏预设配置', editorPath)
  expectStructure(editorSource.includes('placeholder'), '编辑器组件应支持自定义占位文案', editorPath)

  console.log('dianying-detail-comment-structure tests passed')
}

run()
