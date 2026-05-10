const assert = require('assert')
const fs = require('fs')
const path = require('path')

const repoRoot = path.resolve(__dirname, '../../../../../..')

const checkedFiles = [
  'src/main/resources/front/front/src/pages/login/login.vue',
  'src/main/resources/front/front/src/pages/register/register.vue',
  'src/main/resources/front/front/src/utils/route-loading.js',
  'src/main/resources/front/front/src/components/CinemaShell.vue',
  'src/main/resources/front/front/src/components/RouteLoadingCurtain.vue',
  'src/main/resources/front/front/src/pages/center/center.vue',
  'src/main/resources/admin/admin/src/components/index/IndexAsideStatic.vue',
  'src/main/resources/admin/admin/src/views/login.vue',
  'src/main/resources/admin/admin/src/views/home.vue',
]

function label(...parts) {
  return parts.join('')
}

const forbiddenVisibleLabels = [
  label('以太', '影院'),
  label('Ae', 'ther'),
  label('Cine', 'Admin'),
  label('Digital', ' Curator'),
  label('影院', '业务'),
  label('影院', '场景'),
  label('导演', '剪辑版'),
  label('Security', ' Protocol'),
  label('登录', ' (Login)'),
  label('账号', ' (Username)'),
  label('密码', ' (Password)'),
  label('CIN', 'EMA'),
  label('A', 'I'),
  label('MOVIE', ' INTELLIGENCE'),
  label('Management', ' Portal'),
  label('Dash', 'board'),
  label('A', 'C'),
]

function readProjectFile(relativePath) {
  return fs.readFileSync(path.join(repoRoot, relativePath), 'utf8')
}

function extractVisibleTextRegion(relativePath, source) {
  if (!relativePath.endsWith('.vue')) {
    return source
  }
  const match = source.match(/<template>([\s\S]*?)<\/template>/)
  return match ? match[1] : source
}

function run() {
  const violations = []

  checkedFiles.forEach(relativePath => {
    const source = extractVisibleTextRegion(relativePath, readProjectFile(relativePath))
    forbiddenVisibleLabels.forEach(label => {
      if (source.includes(label)) {
        violations.push(`${relativePath}: ${label}`)
      }
    })
  })

  assert.deepStrictEqual(violations, [], `存在遗留用户可见品牌标签:\n${violations.join('\n')}`)
  console.log('brand-label-cleanup tests passed')
}

run()
