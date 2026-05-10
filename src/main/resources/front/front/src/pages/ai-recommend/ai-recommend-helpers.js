function getNextConversationIdAfterDelete(conversations, deletedId, currentConversationId) {
  const list = Array.isArray(conversations) ? conversations : []
  const deletedIndex = list.findIndex(item => Number(item && item.id) === Number(deletedId))
  const remaining = list.filter(item => Number(item && item.id) !== Number(deletedId))

  if (Number(currentConversationId) !== Number(deletedId)) {
    return currentConversationId || null
  }

  if (!remaining.length) {
    return null
  }

  if (deletedIndex >= 0 && deletedIndex < remaining.length) {
    return remaining[deletedIndex].id
  }

  return remaining[remaining.length - 1].id
}

function isNearMessagesBottom(metrics, threshold = 48) {
  const safeThreshold = Number.isFinite(Number(threshold)) ? Math.max(Number(threshold), 0) : 48
  const scrollTop = Number(metrics && metrics.scrollTop)
  const clientHeight = Number(metrics && metrics.clientHeight)
  const scrollHeight = Number(metrics && metrics.scrollHeight)

  if (!Number.isFinite(scrollTop) || !Number.isFinite(clientHeight) || !Number.isFinite(scrollHeight)) {
    return true
  }

  return scrollHeight - (scrollTop + clientHeight) <= safeThreshold
}

function normalizeAssistantText(text) {
  const value = String(text || '')
  if (!value) return ''
  return value
    .replace(/\r\n/g, '\n')
    .replace(/\r/g, '\n')
    .replace(/^#{1,6}\s*/gm, '')
    .replace(/\*\*(.*?)\*\*/g, '$1')
    .replace(/__(.*?)__/g, '$1')
    .replace(/`([^`]+)`/g, '$1')
    .replace(/^[-*•]\s+/gm, '')
    .replace(/^\d+\.\s+/gm, '')
    .replace(/^\s*---+\s*$/gm, '')
    .replace(/^\s*___+\s*$/gm, '')
    .replace(/^\s*\*\*\*+\s*$/gm, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

function normalizeIntentActions(actions) {
  if (!Array.isArray(actions)) {
    return []
  }
  return actions
    .filter(action => action && action.actionType === 'navigate' && action.targetRoute)
    .map(action => {
      const query = action.query && typeof action.query === 'object' && !Array.isArray(action.query)
        ? Object.keys(action.query).reduce((result, key) => {
          const value = action.query[key]
          if (value !== undefined && value !== null && value !== '') {
            result[key] = String(value)
          }
          return result
        }, {})
        : null

      return {
        actionType: 'navigate',
        label: String(action.label || '立即前往'),
        targetRoute: String(action.targetRoute),
        query: query && Object.keys(query).length ? query : null,
      }
    })
}

function mapAiMessage(rawMessage) {
  const message = rawMessage || {}
  const recommendedMovies = Array.isArray(message.recommendedMovies)
    ? message.recommendedMovies
      .filter(movie => movie && movie.id)
      .slice(0, 3)
      .map(movie => Object.assign({}, movie))
    : []
  const movie = message.movieId ? { id: message.movieId } : null
  const movies = recommendedMovies.length
    ? recommendedMovies
    : (movie ? [Object.assign({}, movie)] : [])
  return {
    id: message.id || null,
    role: message.role || '',
    content: message.content || '',
    text: message.role === 'assistant' ? normalizeAssistantText(message.content) : null,
    reason: message.recommendationReason || null,
    movie,
    movies,
    actions: normalizeIntentActions(message.intentActions),
    feedback: null,
  }
}

module.exports = {
  getNextConversationIdAfterDelete,
  isNearMessagesBottom,
  normalizeAssistantText,
  normalizeIntentActions,
  mapAiMessage,
}
