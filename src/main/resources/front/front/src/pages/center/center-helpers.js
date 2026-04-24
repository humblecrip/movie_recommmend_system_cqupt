function normalizeAvatar(value) {
  if (value === undefined || value === null) {
    return ''
  }

  const avatar = String(value).split(',')[0].trim()
  if (!avatar || avatar === 'null' || avatar === 'undefined') {
    return ''
  }

  return avatar
}

function resolveCenterAvatar(sessionForm, baseUrl, fallbackAvatar) {
  const avatar = normalizeAvatar(sessionForm && sessionForm.touxiang)
  if (!avatar) {
    return fallbackAvatar || ''
  }

  if (/^https?:\/\//i.test(avatar)) {
    return avatar
  }

  return `${baseUrl || ''}${avatar}`
}

function buildCenterNavItems(menuList) {
  const items = [
    { key: 'profile', label: '个人中心', type: 'panel', icon: 'person' },
    { key: 'password', label: '修改密码', type: 'panel', icon: 'lock' },
  ]

  ;(menuList || []).forEach(item => {
    if (!item || item.menu === '我的收藏管理' || !item.child || !item.child.length) {
      return
    }

    items.push({
      key: `backend-${item.child[0].tableName}`,
      label: item.child[0].menu,
      type: 'route',
      route: `/index/${item.child[0].tableName}?centerType=1`,
      icon: 'dashboard',
    })
  })

  items.push({ key: 'storeup', label: '我的收藏', type: 'panel', icon: 'favorite' })
  items.push({ key: 'logout', label: '退出登录', type: 'action', action: 'logout', icon: 'logout' })

  return items
}

module.exports = {
  resolveCenterAvatar,
  buildCenterNavItems,
}
