import { defineStore } from 'pinia'

function buildEmptyRegisterDraft() {
  return {
    yonghuzhanghao: '',
    mima: '',
    mima2: '',
    yonghuxingming: '',
    touxiang: '',
    xingbie: '',
    lianxidianhua: '',
    shenfenzheng: '',
  }
}

export const useRegisterDraftStore = defineStore('registerDraft', {
  state: () => ({
    draft: buildEmptyRegisterDraft(),
  }),
  actions: {
    saveDraft(payload) {
      this.draft = Object.assign(buildEmptyRegisterDraft(), this.draft, payload || {})
    },
    getDraft() {
      return Object.assign(buildEmptyRegisterDraft(), this.draft)
    },
    clearDraft() {
      this.draft = buildEmptyRegisterDraft()
    },
  },
})
