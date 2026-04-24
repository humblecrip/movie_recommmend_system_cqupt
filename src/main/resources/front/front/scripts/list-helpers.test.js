const assert = require('assert')
const path = require('path')

const helperPath = path.join(__dirname, '../src/pages/dianyingxinxi/list-helpers.js')
const {
  extractMovieYear,
  normalizeMovieScore,
  buildYearOptions,
  filterMovieList,
  paginateMovieList,
  resolveSortRequest,
} = require(helperPath)

const sampleList = [
  { id: 1, shangyingshijian: '2025-04-02', addtime: '2025-04-03 12:00:00', totalscore: 8.6, dianyingleixing: '动作' },
  { id: 2, shangyingshijian: '', addtime: '2024-03-01 09:00:00', totalscore: 7.2, dianyingleixing: '剧情' },
  { id: 3, shangyingshijian: 'invalid', addtime: '', totalscore: null, dianyingleixing: '动作' },
]

function run() {
  assert.strictEqual(extractMovieYear(sampleList[0]), '2025', '应优先从上映时间提取年份')
  assert.strictEqual(extractMovieYear(sampleList[1]), '2024', '上映时间缺失时应回退 addtime')
  assert.strictEqual(extractMovieYear(sampleList[2]), '未知年份', '无法提取年份时应返回未知年份')

  assert.strictEqual(normalizeMovieScore(8.6), 8.6, '数字评分应保持数值')
  assert.strictEqual(normalizeMovieScore('7.5'), 7.5, '字符串评分应转成数字')
  assert.strictEqual(normalizeMovieScore(null), 0, '空评分过滤时按 0 处理')

  assert.deepStrictEqual(
    buildYearOptions(sampleList),
    ['2025', '2024'],
    '年份选项应去重、排除未知年份并按倒序输出'
  )

  assert.deepStrictEqual(
    filterMovieList(sampleList, { genre: '动作', year: '2025', rating: '8.0+' }).map(item => item.id),
    [1],
    '应能叠加类型、年份和评分筛选'
  )

  assert.deepStrictEqual(
    filterMovieList(sampleList, { genre: 'All', year: 'Any', rating: '9.0+' }).map(item => item.id),
    [],
    '9.0+ 阈值应正确过滤掉不足 9 分的数据'
  )

  assert.deepStrictEqual(
    filterMovieList(sampleList, { rating: '6.0+' }).map(item => item.id),
    [1, 2, 3],
    '未知 rating 应视为不过滤'
  )

  assert.deepStrictEqual(
    filterMovieList(sampleList, { year: 2025 }).map(item => item.id),
    [1],
    'year 为数字时也应能匹配字符串年份'
  )

  assert.deepStrictEqual(
    paginateMovieList([{ id: 1 }, { id: 2 }, { id: 3 }], 2, 2),
    {
      total: 3,
      totalPage: 2,
      pageList: [{ id: 3 }],
    },
    '前端分页应返回总数、总页数和当前页数据'
  )

  assert.deepStrictEqual(
    paginateMovieList([{ id: 1 }, { id: 2 }, { id: 3 }], 'abc', 0),
    {
      total: 3,
      totalPage: 1,
      pageList: [{ id: 1 }, { id: 2 }, { id: 3 }],
    },
    '非法 page/pageSize 下应回退到稳定默认分页'
  )

  assert.deepStrictEqual(
    paginateMovieList([{ id: 1 }, { id: 2 }, { id: 3 }], 1, 0.5),
    {
      total: 3,
      totalPage: 1,
      pageList: [{ id: 1 }, { id: 2 }, { id: 3 }],
    },
    '小数 pageSize 应回退到默认分页，避免 Infinity 或空页'
  )

  assert.deepStrictEqual(
    resolveSortRequest('Popularity'),
    { sort: 'clicknum', order: 'desc' },
    'Popularity 应映射到点击量倒序'
  )

  assert.deepStrictEqual(
    resolveSortRequest('Newest First'),
    { sort: 'addtime', order: 'desc' },
    'Newest First 应映射到新增时间倒序'
  )

  assert.deepStrictEqual(
    resolveSortRequest('Highest Rated'),
    { sort: 'totalscore', order: 'desc' },
    'Highest Rated 应映射到评分倒序'
  )

  assert.deepStrictEqual(
    resolveSortRequest('A-Z'),
    { sort: 'dianyingmingcheng', order: 'asc' },
    'A-Z 应映射到电影名称正序'
  )

  assert.deepStrictEqual(
    resolveSortRequest('Unknown Option'),
    { sort: 'clicknum', order: 'desc' },
    '未知 sortBy 应 fallback 到 Popularity'
  )

  console.log('list-helpers tests passed')
}

run()
