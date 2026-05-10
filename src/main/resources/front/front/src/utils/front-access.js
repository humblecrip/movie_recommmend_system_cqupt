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

function normalizeRequestPath(request) {
  const source = typeof request === 'string'
    ? request
    : (request && (request.url || request.path || request._url)) || ''

  const withoutQuery = String(source).split('#')[0].split('?')[0].replace(/\\/g, '/').trim()
  const path = withoutQuery.replace(/^https?:\/\/[^/]+/i, '').replace(/^\/+/, '')
  const lowerPath = path.toLowerCase()
  const knownPrefixes = [
    'appmovie/',
    'yonghu/',
    'users/',
    'storeup/',
    'file/',
  ]

  for (let i = 0; i < knownPrefixes.length; i += 1) {
    const index = lowerPath.indexOf(knownPrefixes[i])
    if (index >= 0) {
      return lowerPath.slice(index)
    }
  }

  return lowerPath
}

function isAccountApiPath(path) {
  if (!path) {
    return false
  }

  if (
    path === 'yonghu/session' ||
    path === 'yonghu/update' ||
    path.indexOf('yonghu/changepassword') === 0 ||
    path.indexOf('users/') === 0 ||
    path.indexOf('appmovie/actions/') === 0 ||
    path.indexOf('appmovie/favorites/') === 0 ||
    path === 'appmovie/comments/add' ||
    path.indexOf('appmovie/comments/delete') === 0 ||
    path === 'appmovie/comments/vote' ||
    path.indexOf('storeup/') === 0 ||
    path === 'file/upload'
  ) {
    return true
  }

  return false
}

function isPublicApiPath(path) {
  if (!path) {
    return false
  }

  if (
    path === 'appmovie/types' ||
    path === 'appmovie/front/list' ||
    path === 'appmovie/front/recommended' ||
    path.indexOf('appmovie/front/detail/') === 0 ||
    path === 'appmovie/comments/page'
  ) {
    return true
  }

  return false
}

function shouldRedirectAuthFailure(request, currentRoute) {
  const requestPath = normalizeRequestPath(request)

  if (isPublicApiPath(requestPath)) {
    return false
  }

  if (requiresFrontLogin(currentRoute)) {
    return true
  }

  return isAccountApiPath(requestPath)
}

module.exports = {
  normalizeRouteFlag,
  normalizeRequestPath,
  requiresFrontLogin,
  isPublicApiPath,
  shouldRedirectAuthFailure,
}
