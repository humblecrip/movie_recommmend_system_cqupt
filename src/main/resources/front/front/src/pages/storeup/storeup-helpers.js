function normalizePicture(value) {
  if (value === undefined || value === null) {
    return ''
  }

  const picture = String(value).split(',')[0].trim()
  if (!picture || picture === 'null' || picture === 'undefined') {
    return ''
  }

  return picture
}

function resolveStoreupPicture(item, baseUrl, fallbackPicture) {
  const picture = normalizePicture(item && (item.posterUrl || item.picture))
  if (!picture) {
    return fallbackPicture || ''
  }

  if (/^https?:\/\//i.test(picture)) {
    return picture
  }

  return `${baseUrl || ''}${picture}`
}

function getStoreupSortOptions() {
  return [
    { key: 'latest', label: '最新收藏', sort: 'addtime', order: 'desc' },
    { key: 'oldest', label: '最早收藏', sort: 'addtime', order: 'asc' },
    { key: 'name', label: '名称排序', sort: 'name', order: 'asc' },
  ]
}

function getStoreupEmptyText(keyword) {
  const text = String(keyword || '').trim()
  if (!text) {
    return '暂无收藏内容'
  }
  return `没有找到与“${text}”相关的收藏`
}

module.exports = {
  resolveStoreupPicture,
  getStoreupSortOptions,
  getStoreupEmptyText,
}
