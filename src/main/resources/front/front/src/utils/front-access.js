function normalizeRouteFlag(value) {
  if (value === undefined || value === null) {
    return false
  }

  return String(value) !== '0' && String(value).trim() !== ''
}

function requiresFrontLogin(route) {
  const currentRoute = route || {}
  const path = currentRoute.path || ''
  const query = currentRoute.query || {}

  if (path === '/index/center' || path === '/index/storeup' || path === '/index/yonghuDetail') {
    return true
  }

  if (normalizeRouteFlag(query.centerType) || normalizeRouteFlag(query.storeupType)) {
    return true
  }

  return false
}

module.exports = {
  normalizeRouteFlag,
  requiresFrontLogin,
}
