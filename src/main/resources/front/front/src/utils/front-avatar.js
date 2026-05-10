function normalizeAvatarValue(value) {
  if (value === undefined || value === null) {
    return ''
  }

  const avatar = String(value).split(',')[0].trim()
  if (!avatar || avatar === 'null' || avatar === 'undefined') {
    return ''
  }

  return avatar
}

function parseSessionForm(rawValue) {
  if (!rawValue) {
    return {}
  }

  if (typeof rawValue === 'object') {
    return rawValue
  }

  try {
    return JSON.parse(rawValue)
  } catch (error) {
    return {}
  }
}

function getSessionAvatar(sessionForm) {
  const form = parseSessionForm(sessionForm)
  return normalizeAvatarValue(form.touxiang) || normalizeAvatarValue(form.headportrait)
}

function resolveFrontAvatar(options) {
  const {
    sessionForm,
    cachedAvatar,
    baseUrl,
    fallbackAvatar,
  } = options || {}

  const avatar = getSessionAvatar(sessionForm) || normalizeAvatarValue(cachedAvatar)
  if (!avatar) {
    return fallbackAvatar || ''
  }

  if (/^https?:\/\//i.test(avatar)) {
    return avatar
  }

  if (/^data:/i.test(avatar)) {
    return avatar
  }

  const normalizedBaseUrl = String(baseUrl || '').replace(/\/+$/, '')
  const normalizedAvatar = avatar.replace(/^\/+/, '')

  if (!normalizedBaseUrl) {
    return avatar.startsWith('/') ? avatar : `/${normalizedAvatar}`
  }

  return `${normalizedBaseUrl}/${normalizedAvatar}`
}

function getFrontIdentityState(options) {
  const {
    hasToken,
    placeholderText,
  } = options || {}

  if (!hasToken) {
    return {
      mode: 'placeholder',
      text: placeholderText || '去登录',
      src: '',
    }
  }

  return {
    mode: 'avatar',
    text: '',
    src: resolveFrontAvatar(options),
  }
}

module.exports = {
  getFrontIdentityState,
  normalizeAvatarValue,
  parseSessionForm,
  getSessionAvatar,
  resolveFrontAvatar,
}
