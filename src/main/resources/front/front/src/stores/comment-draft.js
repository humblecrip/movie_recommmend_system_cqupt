import { defineStore } from 'pinia'

function normalizeDraftKey(movieId) {
  if (movieId === undefined || movieId === null || movieId === '') {
    return ''
  }
  return String(movieId)
}

export const useCommentDraftStore = defineStore('commentDraft', {
  state: () => ({
    drafts: {},
  }),
  actions: {
    saveDraft(movieId, draft) {
      const key = normalizeDraftKey(movieId)
      if (!key) {
        return
      }
      this.drafts = Object.assign({}, this.drafts, {
        [key]: {
          content: draft && draft.content ? String(draft.content) : '',
          score: Number((draft && draft.score) || 0),
          updatedAt: Date.now(),
        },
      })
    },
    getDraft(movieId) {
      const key = normalizeDraftKey(movieId)
      if (!key) {
        return {
          content: '',
          score: 0,
        }
      }
      return this.drafts[key] || {
        content: '',
        score: 0,
      }
    },
    clearDraft(movieId) {
      const key = normalizeDraftKey(movieId)
      if (!key || !this.drafts[key]) {
        return
      }
      const nextDrafts = Object.assign({}, this.drafts)
      delete nextDrafts[key]
      this.drafts = nextDrafts
    },
  },
})
