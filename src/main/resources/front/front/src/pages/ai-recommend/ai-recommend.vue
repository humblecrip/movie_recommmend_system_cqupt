<template>
  <cinema-shell active-nav="discover" :show-search="false" @open-home="goHome" @open-movies="goMovies">
    <div class="recommend-page">
      <aside class="chat-sidebar">
        <div class="sidebar-header">
          <button class="new-chat-btn" type="button" @click="startNewChat">
            <span class="material-symbols-outlined">add</span>
            <span>新对话</span>
          </button>
        </div>
        <div class="sidebar-section">
          <h3 class="sidebar-title">对话历史</h3>
        </div>
        <nav class="chat-history">
          <div
            v-for="chat in conversations"
            :key="chat.id"
            class="chat-item"
            :class="{ active: currentConversationId === chat.id }"
          >
            <button
              type="button"
              class="chat-item-main"
              @click="selectChat(chat.id)"
            >
              <span class="chat-title">{{ chat.title || '新对话' }}</span>
            </button>
            <button
              type="button"
              class="chat-delete-btn"
              title="删除对话"
              aria-label="删除对话"
              @click.stop="deleteConversation(chat, $event)"
            >
              删除
            </button>
          </div>
        </nav>
      </aside>

      <main class="chat-main">
        <div class="ambient-bg">
          <div class="ambient ambient-one"></div>
          <div class="ambient ambient-two"></div>
        </div>

        <div v-if="!isLoggedIn" class="login-panel">
          <h2>登录后开启 AI 电影策展</h2>
          <p>系统会保存你的每轮对话、偏好和反馈，用于持续优化推荐排序。</p>
          <button class="wizard-btn primary" type="button" @click="$router.push('/login')">去登录</button>
        </div>

        <div v-else-if="showColdStartWizard" class="cold-start-wizard">
          <div class="wizard-header">
            <h2>先认识一下你的口味</h2>
            <p>完成冷启动偏好后，推荐会更贴近你的类型、年代、地区和观影心情。</p>
          </div>
          <div class="wizard-step">
            <h3 class="step-title">{{ coldStartQuestions[coldStartStep].question }}</h3>

            <div v-if="coldStartStep === 0" class="step-options multi-select">
              <button v-for="genre in availableGenres" :key="genre" type="button" class="option-btn" :class="{ selected: coldStartAnswers.genres.includes(genre) }" @click="toggleValue(coldStartAnswers.genres, genre)">{{ genre }}</button>
            </div>
            <div v-else-if="coldStartStep === 1" class="step-options multi-select">
              <button v-for="year in availableYears" :key="year" type="button" class="option-btn" :class="{ selected: coldStartAnswers.years.includes(year) }" @click="toggleValue(coldStartAnswers.years, year)">{{ year }}</button>
            </div>
            <div v-else-if="coldStartStep === 2" class="step-options multi-select">
              <button v-for="region in availableRegions" :key="region" type="button" class="option-btn" :class="{ selected: coldStartAnswers.regions.includes(region) }" @click="toggleValue(coldStartAnswers.regions, region)">{{ region }}</button>
            </div>
            <div v-else-if="coldStartStep === 3" class="step-options multi-select">
              <button v-for="mood in availableMoods" :key="mood" type="button" class="option-btn" :class="{ selected: coldStartAnswers.moods.includes(mood) }" @click="toggleValue(coldStartAnswers.moods, mood)">{{ mood }}</button>
            </div>
            <div v-else class="step-options">
              <button v-for="frequency in availableFrequencies" :key="frequency" type="button" class="option-btn" :class="{ selected: coldStartAnswers.frequency === frequency }" @click="coldStartAnswers.frequency = frequency">{{ frequency }}</button>
            </div>

            <div class="wizard-actions">
              <button v-if="coldStartStep > 0" class="wizard-btn secondary" type="button" @click="coldStartStep--">上一步</button>
              <button class="wizard-btn primary" type="button" :disabled="!canProceedColdStart" @click="nextColdStartStep">
                {{ coldStartStep === coldStartQuestions.length - 1 ? '完成并开始推荐' : '下一步' }}
              </button>
            </div>
            <div class="step-indicator">
              <span v-for="i in coldStartQuestions.length" :key="i" class="step-dot" :class="{ active: i - 1 <= coldStartStep, completed: i - 1 < coldStartStep }" />
            </div>
          </div>
        </div>

        <template v-else>
          <div class="chat-header">
            <div class="header-center">
              <h1 class="header-title">The Curator</h1>
              <p class="header-subtitle">会解释、会记忆反馈的 AI 电影推荐助手</p>
            </div>
          </div>

          <div class="chat-messages" ref="messagesContainer" @scroll="handleMessagesScroll">
            <div class="message-group ai-message">
              <div class="avatar ai-avatar"><img :src="aiAvatar" alt="AI"></div>
              <div class="message-body">
                <div class="message-sender">The Curator</div>
                <div class="message-bubble"><p>{{ greetingMessage }}</p></div>
              </div>
            </div>

            <template v-for="(msg, index) in messages">
              <div v-if="msg.role === 'user'" :key="'user-' + index" class="message-group user-message">
                <div class="avatar user-avatar">
                  <img v-if="userAvatar" :src="userAvatar" alt="User Avatar">
                  <span v-else class="material-symbols-outlined">person</span>
                </div>
                <div class="message-body">
                  <div class="message-sender">你</div>
                  <div class="message-bubble">{{ msg.content }}</div>
                </div>
              </div>

              <div v-else-if="msg.role === 'assistant'" :key="'ai-' + index" class="message-group ai-message">
                <div class="avatar ai-avatar"><img :src="aiAvatar" alt="AI"></div>
                <div class="message-body">
                  <div class="message-sender">The Curator</div>
                  <div class="message-bubble">
                    <p>{{ formatAssistantMessage(msg.text || msg.content) }}</p>
                    <div v-if="msg.actions && msg.actions.length" class="intent-actions">
                      <button
                        v-for="(action, actionIndex) in msg.actions"
                        :key="`${msg.id || index}-action-${actionIndex}`"
                        class="action-btn intent"
                        type="button"
                        @click.stop="handleIntentAction(action)"
                      >
                        {{ action.label }}
                      </button>
                    </div>
                    <div v-if="msg.reason" class="reason-box">推荐解释：{{ msg.reason }}</div>
                    <div v-if="msg.movies && msg.movies.length" class="movie-card-list">
                      <div
                        v-for="(movie, movieIndex) in msg.movies"
                        :key="`${msg.id || index}-movie-${movie.id || movieIndex}`"
                        class="movie-card"
                        @click="goMovieDetail(movie)"
                      >
                        <div class="movie-poster"><img :src="getMoviePoster(movie)" :alt="movieTitle(movie)" @error="handlePosterError($event, movie)"></div>
                        <div class="movie-info">
                          <div class="movie-tags">
                            <span class="movie-tag">{{ extractYear(movie) }}</span>
                            <span class="movie-tag">{{ movie.typeName || movie.dianyingleixing || '电影' }}</span>
                          </div>
                          <h3 class="movie-title">{{ movieTitle(movie) }}</h3>
                          <p class="movie-desc">{{ truncateText(movie.synopsis || movie.juqingjianjie, 120) }}</p>
                          <div class="movie-actions">
                            <button class="action-btn primary" type="button" @click.stop="goMovieDetail(movie)">查看详情</button>
                            <button class="action-btn secondary" type="button" @click.stop="addToMyList(movie)">想看</button>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div v-if="msg.movie && msg.id" class="feedback-section">
                      <span class="feedback-label">这条推荐怎么样？</span>
                      <div class="feedback-buttons">
                        <button v-for="item in feedbackOptions" :key="item.type" class="feedback-btn" :class="{ active: msg.feedback === item.type }" type="button" @click="submitFeedback(msg, item.type)" :title="item.label">
                          <span class="feedback-badge" aria-hidden="true">{{ item.shortLabel }}</span>
                          <span class="feedback-text">{{ item.label }}</span>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>

            <div v-if="isLoading" class="message-group ai-message">
              <div class="avatar ai-avatar"><img :src="aiAvatar" alt="AI"></div>
              <div class="message-body">
                <div class="message-sender">The Curator</div>
                <div class="message-bubble loading"><span class="loading-dot"></span><span class="loading-dot"></span><span class="loading-dot"></span></div>
              </div>
            </div>
            <div class="messages-spacer"></div>
          </div>

          <div class="quick-actions">
            <button v-for="(action, index) in quickActions" :key="index" type="button" class="quick-action-chip" @click="sendQuickAction(action.text)">{{ action.label }}</button>
          </div>

          <div class="input-area">
            <div class="input-container">
              <button class="input-btn mic-btn" type="button" title="Voice Input"><span class="material-symbols-outlined">mic</span></button>
              <input v-model="inputText" class="chat-input" type="text" placeholder="告诉我你今晚想看什么电影..." @keyup.enter="sendMessage">
              <button class="input-btn send-btn" type="button" :disabled="!inputText.trim() || isLoading" @click="sendMessage"><span class="material-symbols-outlined">send</span></button>
            </div>
            <p class="input-hint">每轮消息都会保存；你的反馈会影响下一次推荐排序。</p>
          </div>
        </template>
      </main>
    </div>
  </cinema-shell>
</template>

<script>
import CinemaShell from '../../components/CinemaShell'
import aiAgentAvatar from '../../assets/ai_agent.png'

const { parseSessionForm, resolveFrontAvatar } = require('../../utils/front-avatar')
const {
  getNextConversationIdAfterDelete,
  isNearMessagesBottom,
  normalizeAssistantText,
  mapAiMessage,
} = require('./ai-recommend-helpers')

export default {
  name: 'AiRecommend',
  components: { CinemaShell },
  data() {
    return {
      currentConversationId: null,
      inputText: '',
      isLoading: false,
      messages: [],
      conversations: [],
      greetingMessage: '告诉我你想看的类型、心情、年代或最近喜欢的电影，我会给出推荐理由，并根据你的反馈继续调整。',
      quickActions: [
        { label: '推荐一部悬疑片', text: '请推荐一部节奏紧凑的悬疑片' },
        { label: '今晚想轻松一点', text: '今晚想看轻松治愈的电影' },
        { label: '根据我的偏好推荐', text: '请根据我的偏好推荐三部电影' },
      ],
      feedbackOptions: [
        { type: 'like', label: '喜欢', shortLabel: '赞' },
        { type: 'dislike', label: '不喜欢', shortLabel: '踩' },
        { type: 'watched', label: '看过了', shortLabel: '看' },
        { type: 'want_watch', label: '想看', shortLabel: '藏' },
        { type: 'not_interested', label: '不感兴趣', shortLabel: '跳' },
      ],
      userAvatar: '',
      aiAvatar: aiAgentAvatar,
      fallbackPoster: require('@/assets/chapter.jpg'),
      liulangdiqiu2Poster: '',
      baseUrl: '',
      shouldAutoScrollMessages: true,
      autoScrollThreshold: 48,
      showColdStartWizard: false,
      coldStartStep: 0,
      coldStartAnswers: { genres: [], years: [], regions: [], moods: [], frequency: '' },
      coldStartQuestions: [
        { question: '你喜欢哪些电影类型？' },
        { question: '你偏好哪些年代的电影？' },
        { question: '你常看哪些地区的电影？' },
        { question: '你通常在什么心情下看电影？' },
        { question: '你的观影频率是？' },
      ],
      availableGenres: ['动作', '喜剧', '剧情', '悬疑', '爱情', '科幻', '惊悚', '动画', '纪录片', '冒险'],
      availableYears: ['2020 以后', '2010-2019', '2000-2009', '2000 以前', '不限年代'],
      availableRegions: ['中国大陆', '中国香港', '美国', '日本', '韩国', '欧洲', '不限地区'],
      availableMoods: ['放松', '热血', '浪漫', '烧脑', '治愈', '怀旧'],
      availableFrequencies: ['每周多次', '每周一次', '每月几次', '偶尔观看'],
    }
  },
  computed: {
    isLoggedIn() {
      return !!localStorage.getItem('frontToken')
    },
    canProceedColdStart() {
      const step = this.coldStartStep
      if (step === 0) return this.coldStartAnswers.genres.length > 0
      if (step === 1) return this.coldStartAnswers.years.length > 0
      if (step === 2) return this.coldStartAnswers.regions.length > 0
      if (step === 3) return this.coldStartAnswers.moods.length > 0
      return !!this.coldStartAnswers.frequency
    },
  },
  mounted() { this.scrollToBottom(true) },
  beforeDestroy() {
    window.removeEventListener('front-avatar-updated', this.handleAvatarUpdated)
  },
  methods: {
    syncUserAvatar() {
      if (!this.isLoggedIn) {
        this.userAvatar = ''
        return
      }
      this.userAvatar = resolveFrontAvatar({
        sessionForm: parseSessionForm(localStorage.getItem('sessionForm')),
        cachedAvatar: localStorage.getItem('frontHeadportrait') || '',
        baseUrl: this.baseUrl || this.$config.baseUrl,
        fallbackAvatar: '',
      })
    },
    handleAvatarUpdated(event) {
      const detail = event && event.detail ? event.detail : {}
      this.userAvatar = resolveFrontAvatar({
        sessionForm: parseSessionForm(detail.sessionForm || localStorage.getItem('sessionForm')),
        cachedAvatar: detail.cachedAvatar !== undefined ? detail.cachedAvatar : (localStorage.getItem('frontHeadportrait') || ''),
        baseUrl: this.baseUrl || this.$config.baseUrl,
        fallbackAvatar: '',
      })
    },
    getMessagesContainer() {
      return this.$refs.messagesContainer || null
    },
    isMessagesNearBottom(container) {
      return isNearMessagesBottom(container, this.autoScrollThreshold)
    },
    handleMessagesScroll() {
      const container = this.getMessagesContainer()
      if (!container) return
      this.shouldAutoScrollMessages = this.isMessagesNearBottom(container)
    },
    scrollToBottom(force = false) {
      this.$nextTick(() => {
        const container = this.getMessagesContainer()
        if (!container) return
        if (!force && !this.shouldAutoScrollMessages) return
        container.scrollTop = container.scrollHeight
        this.shouldAutoScrollMessages = true
      })
    },
    goHome() { this.$router.push('/index/home') },
    goMovies() { this.$router.push('/index/dianyingxinxi') },
    handleIntentAction(action) {
      if (!action || action.actionType !== 'navigate' || !action.targetRoute) return
      this.$router.push({
        path: action.targetRoute,
        query: action.query || undefined,
      })
    },
    goMovieDetail(movie) {
      if (!movie || !movie.id) return
      this.$router.push({ path: '/index/dianyingxinxiDetail', query: { id: movie.id } })
    },
    async checkColdStartStatus() {
      try {
        const res = await this.$http.get('ai-chat/cold-start/status')
        if (res.data && res.data.code === 0) {
          this.showColdStartWizard = !res.data.data.completed
        }
      } catch (e) {
        this.showColdStartWizard = false
      }
    },
    async loadConversations() {
      try {
        const res = await this.$http.get('ai-chat/conversation/list')
        if (res.data && res.data.code === 0) {
          this.conversations = res.data.data || []
          if (this.conversations.length > 0) {
            if (this.currentConversationId) {
              const current = this.conversations.find(item => item.id === this.currentConversationId)
              if (current) {
                this.selectChat(current.id)
                return
              }
            }
            this.selectChat(this.conversations[0].id)
          } else {
            this.currentConversationId = null
            this.messages = []
          }
        }
      } catch (e) {
        void e
      }
    },
    async selectChat(conversationId) {
      this.currentConversationId = conversationId
      try {
        const res = await this.$http.get('ai-chat/messages', { params: { conversationId } })
        if (res.data && res.data.code === 0) {
          this.messages = (res.data.data || []).map(m => this.mapMessage(m))
          this.shouldAutoScrollMessages = true
          this.scrollToBottom(true)
        }
      } catch (e) {
        this.messages = []
        this.shouldAutoScrollMessages = true
      }
    },
    mapMessage(m) {
      const msg = mapAiMessage(m)
      if (msg.movies && msg.movies.length) this.attachMovieDetails(msg)
      return msg
    },
    async attachMovieDetails(message) {
      const rawMovies = Array.isArray(message && message.movies) ? message.movies : []
      if (!rawMovies.length) return
      const detailedMovies = await Promise.all(rawMovies.map(movie => this.fetchMovieDetail(movie)))
      const normalizedMovies = detailedMovies.filter(movie => movie && movie.id)
      this.$set(message, 'movies', normalizedMovies)
      const primaryMovie = this.resolvePrimaryMovie(message.movie, normalizedMovies)
      this.$set(message, 'movie', primaryMovie)
    },
    async fetchMovieDetail(movie) {
      if (!movie || !movie.id) return null
      try {
        const res = await this.$http.get(`appmovie/front/detail/${movie.id}`)
        if (res.data && res.data.code === 0 && res.data.data) {
          return this.normalizeMovieCard(res.data.data)
        }
      } catch (e) {
        void e
      }
      return this.normalizeMovieCard(movie)
    },
    resolvePrimaryMovie(primaryMovie, movies) {
      const list = Array.isArray(movies) ? movies : []
      if (!list.length) return null
      if (primaryMovie && primaryMovie.id) {
        const matched = list.find(movie => Number(movie.id) === Number(primaryMovie.id))
        if (matched) return matched
      }
      return list[0]
    },
    async startNewChat() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      const res = await this.$http.post('ai-chat/conversation/create', { title: '新对话', isColdStart: 0 })
      if (res.data && res.data.code === 0) {
        const conversation = res.data.data
        this.conversations.unshift(conversation)
        this.currentConversationId = conversation.id
        this.messages = []
        this.shouldAutoScrollMessages = true
        this.scrollToBottom(true)
      }
    },
    async deleteConversation(chat, event) {
      if (event && typeof event.stopPropagation === 'function') {
        event.stopPropagation()
      }
      if (!chat || !chat.id) return
      try {
        const res = await this.$http.delete(`ai-chat/conversation/${chat.id}`)
        if (!(res.data && res.data.code === 0)) {
          this.$message.warning(res.data && res.data.msg ? res.data.msg : '删除失败')
          return
        }
        const nextConversationId = getNextConversationIdAfterDelete(
          this.conversations,
          chat.id,
          this.currentConversationId
        )
        this.conversations = this.conversations.filter(item => item.id !== chat.id)
        if (nextConversationId) {
          this.currentConversationId = nextConversationId
          await this.selectChat(nextConversationId)
        } else {
          this.currentConversationId = null
          this.messages = []
        }
        this.$message.success('对话已删除')
      } catch (e) {
        this.$message.error('删除失败')
      }
    },
    normalizeMovieCard(movie) {
      const normalized = Object.assign({}, movie || {})
      const poster = this.resolvePosterForMovie(normalized)
      normalized.posterUrl = poster
      normalized.coverUrl = poster
      normalized.imageUrl = poster
      normalized.poster = poster
      normalized.posterUrls = poster ? [poster] : []
      return normalized
    },
    async addToMyList(movie) {
      if (!movie || !movie.id) return
      try {
        const res = await this.$http.post('appmovie/actions/toggle', { movieId: movie.id, actionType: 'favorite' })
        if (res.data && res.data.code === 0) this.$message.success('已更新想看状态')
        else this.$message.warning(res.data && res.data.msg ? res.data.msg : '操作失败')
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    getPosterSource(movie) {
      if (!movie) return ''
      const candidates = [
        movie.posterUrl,
        movie.coverUrl,
        movie.imageUrl,
        movie.poster,
        movie.posterUrls,
        movie.haibao,
        movie.dianyingfengmian,
        movie.fengmian,
      ]
      for (let index = 0; index < candidates.length; index++) {
        const value = candidates[index]
        const list = Array.isArray(value) ? value : String(value || '').split(',')
        for (let itemIndex = 0; itemIndex < list.length; itemIndex++) {
          const poster = String(list[itemIndex] || '').trim()
          if (poster && poster !== 'null' && poster !== 'undefined') return poster
        }
      }
      return ''
    },
    resolveAssetUrl(url) {
      const asset = String(url || '').trim()
      if (!asset) return ''
      if (/^(https?:)?\/\//i.test(asset) || /^data:/i.test(asset)) return asset
      const base = String(this.baseUrl || '').replace(/\/+$/, '')
      const path = asset.replace(/^\/+/, '')
      return base ? `${base}/${path}` : path
    },
    resolvePosterForMovie(movie) {
      const explicitPoster = this.resolveAssetUrl(this.getPosterSource(movie))
      if (explicitPoster) return explicitPoster
      if (this.isLiulangdiqiu2(movie)) return this.liulangdiqiu2Poster || this.fallbackPoster
      return this.fallbackPoster
    },
    isLiulangdiqiu2(movie) {
      const title = this.movieTitle(movie).replace(/\s+/g, '').toLowerCase()
      return title.includes('流浪地球2')
    },
    getMoviePoster(movie) {
      if (!movie) return this.fallbackPoster
      return movie.posterUrl || this.resolvePosterForMovie(movie)
    },
    handlePosterError(event, movie) {
      const target = event && event.target
      if (!target) return
      const fallback = this.resolvePosterForMovie(movie)
      if (target.src !== fallback) {
        target.src = fallback
      }
    },
    movieTitle(movie) { return movie.title || movie.dianyingmingcheng || '未命名电影' },
    extractYear(movie) {
      const value = movie.releaseDate || movie.shangyingshijian
      if (!value) return '未知'
      const match = String(value).match(/\d{4}/)
      return match ? match[0] : '未知'
    },
    truncateText(text, maxLen) {
      if (!text) return ''
      const cleaned = text.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ')
      return cleaned.length <= maxLen ? cleaned : cleaned.slice(0, maxLen) + '...'
    },
    formatAssistantMessage(text) {
      return normalizeAssistantText(text)
    },
    sendQuickAction(text) { this.inputText = text; this.sendMessage() },
    async sendMessage() {
      const text = this.inputText.trim()
      if (!text || this.isLoading) return
      if (!this.currentConversationId) await this.startNewChat()
      if (!this.currentConversationId) return

      this.messages.push({ role: 'user', content: text })
      this.shouldAutoScrollMessages = true
      this.scrollToBottom(true)
      this.inputText = ''
      this.isLoading = true
      const assistantMessage = { id: null, role: 'assistant', text: '', movie: null, feedback: null }

      try {
        const token = localStorage.getItem('frontToken') || ''
        const url = `${this.baseUrl}ai-chat/stream?conversationId=${this.currentConversationId}&message=${encodeURIComponent(text)}&token=${encodeURIComponent(token)}`
        const eventSource = new EventSource(url)
        eventSource.onmessage = async (event) => {
          if (event.data === '[DONE]') {
            eventSource.close()
            this.isLoading = false
            await this.refreshCurrentConversation(assistantMessage)
            return
          }
          if (!assistantMessage.text) {
            this.messages.push(assistantMessage)
            this.scrollToBottom()
          }
          assistantMessage.text = normalizeAssistantText(
            assistantMessage.text + String(event.data || '').replace(/\\n/g, '\n')
          )
          this.scrollToBottom()
        }
        eventSource.onerror = async () => {
          eventSource.close()
          this.isLoading = false
          await this.refreshCurrentConversation(assistantMessage)
        }
      } catch (e) {
        this.isLoading = false
      }
    },
    async refreshCurrentConversation(streamMessage) {
      const previousLength = this.messages.length
      await this.selectChat(this.currentConversationId)
      if (this.messages.length === 0 && streamMessage && streamMessage.text) {
        this.messages.push(streamMessage)
        this.scrollToBottom()
      } else if (this.messages.length === previousLength) {
        const latest = this.messages[this.messages.length - 1]
        if (latest && latest.role === 'assistant' && latest.movie) return
      }
    },
    async submitFeedback(message, feedbackType) {
      if (!message.movie || !message.movie.id || !message.id) return
      try {
        const res = await this.$http.post('ai-chat/feedback', {
          conversationId: this.currentConversationId,
          messageId: message.id,
          movieId: message.movie.id,
          feedbackType,
          feedbackReason: '',
        })
        if (res.data && res.data.code === 0) {
          message.feedback = feedbackType
          this.$message.success('反馈已保存，后续推荐会调整排序')
        }
      } catch (e) {
        this.$message.error('反馈保存失败')
      }
    },
    toggleValue(list, value) {
      const idx = list.indexOf(value)
      if (idx === -1) list.push(value)
      else list.splice(idx, 1)
    },
    async nextColdStartStep() {
      if (this.coldStartStep < this.coldStartQuestions.length - 1) this.coldStartStep++
      else await this.completeColdStart()
    },
    async completeColdStart() {
      try {
        await this.$http.post('ai-chat/cold-start/complete', {
          favoriteGenres: JSON.stringify(this.coldStartAnswers.genres),
          favoriteYears: JSON.stringify(this.coldStartAnswers.years),
          favoriteRegions: JSON.stringify(this.coldStartAnswers.regions),
          moodPreferences: JSON.stringify(this.coldStartAnswers.moods),
          watchFrequency: this.coldStartAnswers.frequency,
        })
        this.showColdStartWizard = false
        this.$message.success('偏好已保存')
        await this.startNewChat()
      } catch (e) {
        this.$message.error('偏好保存失败')
      }
    },
  },
  watch: {
    isLoggedIn(value) {
      if (value) {
        this.syncUserAvatar()
        this.checkColdStartStatus()
        this.loadConversations()
      } else {
        this.userAvatar = ''
        this.currentConversationId = null
        this.messages = []
        this.conversations = []
        this.shouldAutoScrollMessages = true
      }
    },
  },
  created() {
    this.baseUrl = this.$config.baseUrl
    this.liulangdiqiu2Poster = this.resolveAssetUrl('upload/movie_liulangdiqiu2.jpg')
    this.syncUserAvatar()
    window.addEventListener('front-avatar-updated', this.handleAvatarUpdated)
    if (this.isLoggedIn) {
      this.checkColdStartStatus()
      this.loadConversations()
    }
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.recommend-page {
  --bg: #0b1326;
  --surface-low: #131b2e;
  --surface-high: #222a3d;
  --surface-card: #2d3449;
  --text-main: #dae2fd;
  --text-muted: rgba(218, 226, 253, 0.7);
  --text-soft: #d0c5af;
  --accent: #ffc639;
  --accent-deep: #e1aa12;
  --border: rgba(153, 144, 124, 0.22);
  display: flex;
  height: calc(100vh - 90px);
  background: var(--bg);
  color: var(--text-main);
  font-family: 'Manrope', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.chat-sidebar { width: 260px; flex-shrink: 0; display: flex; flex-direction: column; padding: 24px 16px; background: var(--bg); border-right: 1px solid var(--border); }
.sidebar-header { margin-bottom: 20px; }
.new-chat-btn { width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px; height: 44px; border: 1px solid rgba(255, 198, 57, 0.2); border-radius: 12px; background: rgba(255, 198, 57, 0.08); color: var(--accent); font-size: 14px; font-weight: 600; cursor: pointer; transition: background-color 0.2s, border-color 0.2s; }
.new-chat-btn:hover { background: rgba(255, 198, 57, 0.15); border-color: rgba(255, 198, 57, 0.35); }
.sidebar-section { margin-top: 16px; padding: 0 8px; }
.sidebar-title { font-size: 11px; font-weight: 800; letter-spacing: 0.18em; text-transform: uppercase; color: var(--text-soft); }
.chat-history { flex: 1; overflow-y: auto; margin-top: 12px; display: flex; flex-direction: column; gap: 4px; }
.chat-history::-webkit-scrollbar { display: none; }
.chat-item { display: flex; align-items: center; gap: 8px; padding: 4px; border-radius: 12px; background: transparent; color: var(--text-muted); transition: background-color 0.2s, color 0.2s, transform 0.2s; }
.chat-item-main { flex: 1; min-width: 0; display: flex; align-items: center; padding: 8px 12px; border: none; border-radius: 10px; background: transparent; color: inherit; font-size: 14px; text-align: left; cursor: pointer; }
.chat-item:hover, .chat-item.active { background: var(--surface-high); color: var(--text-main); }
.chat-item.active { transform: scale(1.02); }
.chat-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.chat-delete-btn { flex: 0 0 auto; height: 30px; padding: 0 10px; border: none; border-radius: 999px; background: rgba(255, 255, 255, 0.04); color: var(--text-muted); font-size: 12px; cursor: pointer; opacity: 0; transition: opacity 0.2s, background-color 0.2s, color 0.2s; }
.chat-item:hover .chat-delete-btn, .chat-item.active .chat-delete-btn { opacity: 1; }
.chat-delete-btn:hover { background: rgba(255, 96, 96, 0.16); color: #ffd0d0; }
.chat-main { flex: 1; display: flex; flex-direction: column; position: relative; overflow: hidden; }
.ambient-bg { position: absolute; inset: 0; pointer-events: none; z-index: 0; }
.ambient { position: absolute; border-radius: 50%; filter: blur(120px); }
.ambient-one { top: 25%; left: 25%; width: 500px; height: 500px; background: rgba(255, 198, 57, 0.05); }
.ambient-two { bottom: 25%; right: 25%; width: 600px; height: 600px; background: rgba(66, 76, 95, 0.1); }
.login-panel, .cold-start-wizard { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; position: relative; z-index: 1; text-align: center; }
.login-panel h2, .wizard-header h2 { font-size: 28px; font-weight: 800; color: var(--accent); margin-bottom: 12px; }
.login-panel p, .wizard-header p { color: var(--text-muted); font-size: 16px; margin-bottom: 24px; }
.wizard-header { margin-bottom: 40px; }
.wizard-step { background: var(--surface-low); padding: 32px; border-radius: 20px; max-width: 680px; width: 100%; box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3); }
.step-title { font-size: 20px; font-weight: 600; color: var(--text-main); margin-bottom: 24px; text-align: center; }
.step-options { display: flex; flex-wrap: wrap; gap: 12px; justify-content: center; }
.option-btn { padding: 12px 20px; border: 1px solid var(--border); border-radius: 12px; background: var(--surface-high); color: var(--text-muted); font-size: 14px; cursor: pointer; transition: all 0.2s; }
.option-btn:hover { border-color: rgba(255, 198, 57, 0.3); color: var(--text-main); }
.option-btn.selected { border-color: var(--accent); background: rgba(255, 198, 57, 0.15); color: var(--accent); }
.wizard-actions { display: flex; justify-content: center; gap: 16px; margin-top: 32px; }
.wizard-btn { min-width: 120px; height: 44px; border-radius: 12px; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.wizard-btn.primary { border: none; background: linear-gradient(135deg, var(--accent), var(--accent-deep)); color: #3f2e00; }
.wizard-btn.primary:disabled { opacity: 0.5; cursor: not-allowed; }
.wizard-btn.secondary { border: 1px solid var(--border); background: transparent; color: var(--text-muted); }
.step-indicator { display: flex; justify-content: center; gap: 8px; margin-top: 24px; }
.step-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--border); transition: all 0.3s; }
.step-dot.active, .step-dot.completed { background: var(--accent); }
.chat-header { height: 96px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; padding: 0 32px; position: relative; z-index: 1; background: rgba(11, 19, 38, 0.5); backdrop-filter: blur(12px); }
.header-center { text-align: center; }
.header-title { font-size: 28px; font-weight: 800; color: var(--accent); margin: 0 0 6px; }
.header-subtitle { margin: 0; color: var(--text-muted); }
.chat-messages { flex: 1; overflow-y: auto; padding: 24px 56px; position: relative; z-index: 1; scrollbar-width: none; -ms-overflow-style: none; }
.chat-messages::-webkit-scrollbar { display: none; }
.message-group { display: flex; gap: 16px; margin-bottom: 24px; }
.user-message { flex-direction: row-reverse; }
.avatar { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; overflow: hidden; }
.avatar img { width: 100%; height: 100%; object-fit: cover; }
.ai-avatar { background: var(--surface-high); }
.user-avatar { background: var(--surface-high); color: var(--text-main); }
.message-body { max-width: min(720px, 75%); }
.user-message .message-body { display: flex; flex-direction: column; align-items: flex-end; }
.message-sender { font-size: 12px; color: var(--text-soft); margin-bottom: 6px; }
.message-bubble { padding: 16px 18px; border-radius: 18px; background: var(--surface-low); color: var(--text-main); line-height: 1.7; white-space: pre-line; box-shadow: 0 12px 36px rgba(0, 0, 0, 0.16); }
.user-message .message-bubble { background: rgba(255, 198, 57, 0.16); color: #fff1c6; }
.reason-box { margin-top: 12px; padding: 10px 12px; border-left: 3px solid var(--accent); background: rgba(255, 198, 57, 0.08); color: var(--text-soft); border-radius: 8px; }
.intent-actions { margin-top: 14px; display: flex; flex-wrap: wrap; gap: 10px; }
.action-btn.intent { background: rgba(255, 198, 57, 0.12); color: var(--accent); border: 1px solid rgba(255, 198, 57, 0.32); }
.action-btn.intent:hover { background: rgba(255, 198, 57, 0.2); }
.movie-card-list { margin-top: 16px; display: grid; gap: 16px; }
.movie-card { display: flex; gap: 16px; padding: 14px; border: 1px solid var(--border); border-radius: 16px; background: var(--surface-card); cursor: pointer; }
.movie-poster { width: 96px; height: 136px; border-radius: 12px; overflow: hidden; flex-shrink: 0; background: #111827; }
.movie-poster img { width: 100%; height: 100%; object-fit: cover; }
.movie-info { flex: 1; min-width: 0; }
.movie-tags { display: flex; gap: 8px; margin-bottom: 8px; }
.movie-tag { padding: 3px 8px; border-radius: 999px; background: rgba(255, 198, 57, 0.12); color: var(--accent); font-size: 12px; }
.movie-title { margin: 0 0 8px; font-size: 18px; color: #fff; }
.movie-desc { margin: 0 0 12px; color: var(--text-muted); font-size: 13px; line-height: 1.6; }
.movie-actions { display: flex; flex-wrap: wrap; gap: 10px; }
.action-btn { min-height: 38px; border: none; border-radius: 10px; padding: 9px 14px; display: inline-flex; align-items: center; justify-content: center; gap: 6px; cursor: pointer; font-weight: 700; white-space: nowrap; line-height: 1.2; }
.action-btn.primary { background: var(--accent); color: #342600; }
.action-btn.secondary { background: rgba(255, 255, 255, 0.08); color: var(--text-main); }
.feedback-section { margin-top: 14px; display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.feedback-label { font-size: 12px; color: var(--text-soft); }
.feedback-buttons { display: flex; flex-wrap: wrap; gap: 8px; }
.feedback-btn { min-height: 34px; border-radius: 999px; border: 1px solid var(--border); background: transparent; color: var(--text-muted); display: inline-flex; align-items: center; justify-content: center; gap: 6px; flex: 0 0 auto; padding: 7px 10px; cursor: pointer; line-height: 1.1; white-space: nowrap; }
.feedback-badge { display: inline-flex; align-items: center; justify-content: center; min-width: 20px; height: 20px; padding: 0 4px; border-radius: 999px; background: rgba(255, 255, 255, 0.08); color: inherit; font-size: 11px; font-weight: 800; line-height: 1; }
.feedback-text { display: inline-block; font-size: 12px; font-weight: 700; line-height: 1; }
.feedback-btn:hover, .feedback-btn.active { border-color: var(--accent); color: var(--accent); background: rgba(255, 198, 57, 0.1); }
.loading { display: flex; gap: 6px; align-items: center; }
.loading-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--accent); animation: blink 1.2s infinite ease-in-out; }
.loading-dot:nth-child(2) { animation-delay: 0.15s; }
.loading-dot:nth-child(3) { animation-delay: 0.3s; }
@keyframes blink { 0%, 80%, 100% { opacity: 0.3; transform: translateY(0); } 40% { opacity: 1; transform: translateY(-4px); } }
.messages-spacer { height: 20px; }
.quick-actions { position: relative; z-index: 1; display: flex; gap: 12px; padding: 0 56px 16px; overflow-x: auto; }
.quick-action-chip { border: 1px solid var(--border); border-radius: 999px; padding: 10px 16px; background: rgba(255, 255, 255, 0.04); color: var(--text-muted); cursor: pointer; white-space: nowrap; }
.quick-action-chip:hover { color: var(--accent); border-color: rgba(255, 198, 57, 0.4); }
.input-area { position: relative; z-index: 1; padding: 0 56px 28px; }
.input-container { display: flex; align-items: center; gap: 12px; padding: 12px; border: 1px solid var(--border); border-radius: 18px; background: rgba(19, 27, 46, 0.92); }
.chat-input { flex: 1; border: none; outline: none; background: transparent; color: var(--text-main); font-size: 15px; }
.chat-input::placeholder { color: var(--text-muted); }
.input-btn { width: 40px; height: 40px; border-radius: 50%; border: none; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.mic-btn { background: transparent; color: var(--text-muted); }
.send-btn { background: var(--accent); color: #342600; }
.send-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.input-hint { color: var(--text-muted); font-size: 12px; text-align: center; margin-top: 10px; }
@media (max-width: 900px) {
  .recommend-page { height: auto; min-height: calc(100vh - 72px); }
  .chat-sidebar { width: 100%; padding: 16px 18px 0; border-right: none; border-bottom: 1px solid var(--border); }
  .sidebar-header { margin-bottom: 12px; }
  .chat-history { flex-direction: row; overflow-x: auto; overflow-y: hidden; padding-bottom: 12px; }
  .chat-item { flex: 0 0 auto; min-width: 170px; }
  .chat-delete-btn { opacity: 1; }
  .chat-messages { padding: 18px; }
  .message-body { max-width: 82%; }
  .quick-actions, .input-area { padding-left: 18px; padding-right: 18px; }
  .movie-card { flex-direction: column; }
  .movie-poster { width: 100%; height: 220px; }
  .feedback-section { align-items: flex-start; flex-direction: column; }
  .feedback-buttons { width: 100%; }
  .action-btn { flex: 1 1 120px; }
}
</style>
