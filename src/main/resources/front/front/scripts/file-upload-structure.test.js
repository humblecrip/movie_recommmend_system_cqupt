const assert = require('assert')
const fs = require('fs')
const path = require('path')

const fileUploadPath = path.join(__dirname, '../src/components/FileUpload.vue')
const source = fs.readFileSync(fileUploadPath, 'utf8')

function expectStructure(condition, message) {
  assert.ok(condition, `${message} [检查文件: ${fileUploadPath}]`)
}

function run() {
  expectStructure(source.includes('replaceable'), '上传组件应支持可替换上传模式')
  expectStructure(source.includes('renderFileList'), '上传组件应提供渲染用文件列表计算属性')
  expectStructure(source.includes(':file-list="renderFileList"'), '上传组件应使用渲染文件列表控制 el-upload')
  expectStructure(source.includes('this.replaceable ? [] : this.fileList'), '可替换上传时应忽略已有文件列表以允许再次点击上传')
  expectStructure(source.includes('clearUploadFilesIfNeeded'), '上传组件应提供可替换模式的内部文件清理方法')
  expectStructure(source.includes("this.$refs.upload.clearFiles()"), '可替换上传时应清空 el-upload 内部文件列表')
  expectStructure(source.includes('this.clearUploadFilesIfNeeded()'), '上传成功或外部值变化后应触发内部文件清理')

  console.log('file-upload-structure tests passed')
}

run()
