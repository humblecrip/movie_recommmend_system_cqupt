<template>
  <div class="prototype-detail-page">
    <div class="detail-shell">
      <div class="back_box prototype-back-box">
        <el-button class="backBtn" size="mini" @click="backClick">
          <span class="icon iconfont icon-jiantou33"></span>
          <span class="text">返回</span>
        </el-button>
      </div>

      <main class="detail-main">
        <section class="hero-section" :style="heroBackdropStyle">
          <div class="hero-background-mask"></div>
          <div class="hero-content">
            <div class="hero-inner">
              <div class="hero-poster-wrap">
                <img :src="primaryPoster || fallbackPoster" :alt="detail.dianyingmingcheng || '电影海报'">
              </div>
              <div class="hero-copy">
                <div class="hero-meta-row">
                  <span class="meta-chip meta-chip-primary">{{ heroMeta.year }}</span>
                  <span class="meta-chip">{{ heroMeta.runtime }}</span>
                  <span class="meta-chip">{{ heroMeta.maturityRating }}</span>
                  <div class="meta-rating">
                    <i class="el-icon-star-on"></i>
                    <span>{{ heroMeta.scoreText }}/5</span>
                  </div>
                </div>

                <h1 class="hero-title">{{ detail.dianyingmingcheng || '电影详情' }}</h1>

                <div class="hero-actions">
                  <el-button class="primary-btn" type="primary" @click="scrollToDetailSection">
                    <i class="el-icon-video-play"></i>
                    查看详情
                  </el-button>
                  <el-button class="ghost-btn" @click="handleStoreupToggle">
                    <i class="el-icon-plus"></i>
                    {{ isStoreup ? '取消收藏' : '加入收藏' }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="overview-section">
          <div class="overview-grid">
            <div class="overview-copy">
              <h2>影片概览</h2>
              <p>{{ heroMeta.overview }}</p>
              <div class="genre-chips" v-if="genreList.length">
                <span class="genre-chip" v-for="item in genreList" :key="item">{{ item }}</span>
              </div>
            </div>

            <div class="overview-cards">
              <div class="info-card">
                <span class="info-label">导演</span>
                <span class="info-value">{{ detail.daoyan || '待补充' }}</span>
              </div>
              <div class="info-card">
                <span class="info-label">区域</span>
                <span class="info-value">{{ detail.quyu || '待补充' }}</span>
              </div>
              <div class="info-card">
                <span class="info-label">上映时间</span>
                <span class="info-value">{{ heroMeta.releaseDateText }}</span>
              </div>
              <div class="info-card">
                <span class="info-label">收藏数</span>
                <span class="info-value">{{ detail.storeupnum || 0 }}</span>
              </div>
            </div>
          </div>
        </section>

        <section class="similar-section">
          <div class="section-head">
            <h2>相似影片</h2>
          </div>

          <div class="similar-grid" v-if="similarMovies.length">
            <div v-for="item in similarMovies" :key="item.id" class="similar-item" @click="openSimilarMovie(item)">
              <div class="similar-cover">
                <img :src="getMovieCover(item)" :alt="item.dianyingmingcheng">
              </div>
              <div class="similar-info">
                <h4>{{ item.dianyingmingcheng }}</h4>
                <div class="similar-meta">
                  <span>{{ getMovieYearText(item.shangyingshijian) }}</span>
                  <span>{{ formatScore(item.totalscore) }}/5</span>
                </div>
              </div>
            </div>
          </div>
          <div class="empty-state" v-else>暂无可展示的相似电影。</div>
        </section>

        <section class="engagement-section">
          <div class="engagement-head">
            <h2>更多信息</h2>
          </div>

          <div class="zancai prototype-zancai">
            <div v-if="!isThumbsupnum && !isCrazilynum" class="zan" @click="thumbsupOrCrazily(21)">
              <i class="icon iconfont icon-zan10"></i>
              <span class="text">赞一下({{ detail.thumbsupnum || 0 }})</span>
            </div>
            <div v-if="!isThumbsupnum && !isCrazilynum" class="cai" @click="thumbsupOrCrazily(22)">
              <i class="icon iconfont icon-cai15"></i>
              <span class="text">踩一下({{ detail.crazilynum || 0 }})</span>
            </div>
            <div v-if="isThumbsupnum" class="zanActive" @click="cancelThumbsupOrCrazily(21)">
              <i class="icon iconfont icon-zan10"></i>
              <span class="text">已赞({{ detail.thumbsupnum || 0 }})</span>
            </div>
            <div v-if="isCrazilynum" class="caiActive" @click="cancelThumbsupOrCrazily(22)">
              <i class="icon iconfont icon-cai15"></i>
              <span class="text">已踩({{ detail.crazilynum || 0 }})</span>
            </div>
          </div>

          <div ref="detailSection" class="detail-anchor"></div>
          <el-tabs class="detail-tabs prototype-tabs" v-model="activeName" type="border-card" v-if="tabsNum > 0">
            <el-tab-pane label="电影详情" name="first">
              <div class="ql-snow ql-editor detail-html" v-html="detail.dianyingxiangqing"></div>
            </el-tab-pane>
            <el-tab-pane label="评论" name="second">
              <div class="comment-scene">
                <section class="comment-composer-card">
                  <div class="comment-composer-head">
                    <div class="comment-composer-copy">
                      <span class="comment-kicker">观众短评</span>
                      <h3>写下你的观影感受</h3>
                      <p>支持图文短评与评分展示，提交后会直接出现在当前影片评论区。现有评论规则保持不变，每位用户仅可评论一次。</p>
                    </div>
                    <div class="comment-rule-chip">每人限评一次</div>
                  </div>

                  <el-form class="add commentForm" :model="form" :rules="rules" ref="form">
                    <el-form-item class="item comment-editor-item" label="评论" prop="content">
                      <div class="comment-editor-shell">
                        <editor
                          myQuillEditor="content"
                          v-model="form.content"
                          class="editor"
                          action="file/upload"
                          toolbar-preset="comment"
                          placeholder="分享你对这部电影的看法..."
                          :editor-height="220"
                        ></editor>
                      </div>
                    </el-form-item>

                    <div class="comment-form-footer">
                      <el-form-item class="item comment-score-item" label="评分" prop="score">
                        <div class="comment-score-wrap">
                          <div class="comment-score-copy">
                            <span class="comment-score-label">你的评分</span>
                            <p>给这部电影留下一个整体印象。</p>
                          </div>
                          <el-rate
                            v-model="form.score"
                            :max="Number(5)"
                            :allow-half="false"
                            :low-threshold="Number(2)"
                            :high-threshold="Number(4)"
                            :show-text="false"
                            :texts="['极差', '失望', '一般', '满意', '惊喜']"
                            text-color="#1F2D3D"
                            :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
                            void-color="#C6D1DE"
                            disabled-void-color="#EFF2F7"
                            :icon-classes="['el-icon-star-on', 'el-icon-star-on', 'el-icon-star-on']"
                            void-icon-class="el-icon-star-off"
                            disabled-void-icon-class="el-icon-star-on"
                            :show-score="false"
                          ></el-rate>
                        </div>
                      </el-form-item>

                      <el-form-item class="commentBtn">
                        <el-button class="submitBtn" type="primary" @click="submitForm('form')">立即提交</el-button>
                        <el-button class="resetBtn" @click="resetForm('form')">重置</el-button>
                      </el-form-item>
                    </div>
                  </el-form>
                </section>

                <section class="comment-stream">
                  <div class="comment-stream-head">
                    <div class="comment-stream-copy">
                      <span class="comment-kicker">观众评论</span>
                      <h3>观众评论</h3>
                    </div>
                    <div class="comment-stream-total">{{ total }} 条评论</div>
                  </div>

                  <div v-if="infoList.length" class="comment-list">
                    <article class="comment-card" v-for="item in infoList" :key="item.id" @mouseenter="discussEnter(item.id)" @mouseleave="discussLeave">
                      <div class="comment-card-head">
                        <div class="user">
                          <div class="comment-avatar-shell">
                            <el-image v-if="item.avatarurl" :size="50" :src="baseUrl + item.avatarurl"></el-image>
                            <el-image v-else :size="50" :src="require('@/assets/touxiang.png')"></el-image>
                          </div>
                          <div class="comment-user-meta">
                            <div class="comment-name-row">
                              <div class="name">{{ item.nickname || '匿名用户' }}</div>
                              <span class="comment-pin" v-if="item.istop">置顶</span>
                            </div>
                            <div class="comment-time">{{ item.addtime }}</div>
                          </div>
                        </div>

                        <div class="comment-card-actions">
                          <el-button class="delBtn" v-if="showIndex == item.id && userid == item.userid" @click="discussDel(item.id)">删除</el-button>
                        </div>
                      </div>

                      <div class="comment-content-box comment-body">
                        <div class="ql-snow ql-editor" v-html="item.content"></div>
                      </div>

                      <div class="comment-footer">
                        <el-rate
                          class="comment-rate"
                          v-model="item.score"
                          disabled
                          :max="Number(5)"
                          :allow-half="false"
                          :low-threshold="Number(2)"
                          :high-threshold="Number(4)"
                          :show-text="false"
                          :texts="['极差', '失望', '一般', '满意', '惊喜']"
                          text-color="#1F2D3D"
                          :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
                          void-color="#C6D1DE"
                          disabled-void-color="#EFF2F7"
                          :icon-classes="['el-icon-star-on', 'el-icon-star-on', 'el-icon-star-on']"
                          void-icon-class="el-icon-star-off"
                          disabled-void-icon-class="el-icon-star-on"
                          :show-score="false"
                        ></el-rate>

                        <div class="zancai-box">
                          <div v-if="!comcaiChange(item)" class="zan-item" :class="comzanChange(item) ? 'active' : ''" @click="comzanClick(item)">
                            <span class="icon iconfont" :class="comzanChange(item) ? 'icon-zan11' : 'icon-zan07'"></span>
                            <span class="label">{{ comzanChange(item) ? '已赞' : '赞' }}</span>
                            <span class="num">({{ item.thumbsupnum }})</span>
                          </div>
                          <div v-if="!comzanChange(item)" class="cai-item" :class="comcaiChange(item) ? 'active' : ''" @click="comcaiClick(item)">
                            <span class="icon iconfont" :class="comcaiChange(item) ? 'icon-cai16' : 'icon-cai01'"></span>
                            <span class="label">{{ comcaiChange(item) ? '已踩' : '踩' }}</span>
                            <span class="num">({{ item.crazilynum }})</span>
                          </div>
                        </div>
                      </div>

                      <div class="comment-content-box reply-box" v-if="item.reply">
                        <span class="reply-label">回复</span>
                        <span class="ql-snow ql-editor" v-html="item.reply"></span>
                      </div>
                    </article>
                  </div>
                  <div v-else class="comment-empty-state">还没有观众评论，来写下第一条观后感。</div>

                  <el-pagination
                    background
                    id="pagination"
                    class="pagination"
                    :page-size="pageSize"
                    prev-text="上一页"
                    next-text="下一页"
                    :hide-on-single-page="false"
                    :layout="['total','prev','pager','next','sizes','jumper'].join()"
                    :total="total"
                    @current-change="curChange"
                    @prev-click="prevClick"
                    @next-click="nextClick"
                    @size-change="sizeChange"
                  ></el-pagination>
                </section>
              </div>
            </el-tab-pane>
          </el-tabs>
        </section>
      </main>
    </div>

    <div class="share_view">
      <div class="share share-weibo" @click="shareToMicroblog">
        <img src="@/assets/weibo.png" alt="微博分享">
      </div>
      <div class="share share-qq" @click="shareToQQRom">
        <img src="@/assets/qq.png" alt="QQ空间分享">
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import {
  extractAppMovieDetail,
  extractAppMovieList,
  getPosterList,
  getPrimaryPoster,
  formatHeroMeta,
  buildSimilarMovies,
  normalizeAppMovieList,
  normalizeAppMovieRecord,
  resolvePosterUrl,
} from './detail-helpers'
import { useCommentDraftStore } from '@/stores/comment-draft'

export default {
  data() {
    return {
      tablename: 'dianyingxinxi',
      baseUrl: '',
      fallbackPoster: require('@/assets/chapter.jpg'),
      userid: Number(localStorage.getItem('frontUserid')),
      id: 0,
      detail: {},
      tabsNum: 2,
      activeName: 'first',
      posterList: [],
      primaryPoster: '',
      similarMovies: [],
      heroMeta: {
        year: '未知年份',
        runtime: '2小时16分钟',
        maturityRating: '建议13岁以上观看',
        overview: '暂无剧情简介',
        scoreText: '暂无',
        releaseDateText: '上映日期待定',
      },
      form: {
        content: '',
        userid: Number(localStorage.getItem('frontUserid')),
        nickname: localStorage.getItem('username'),
        avatarurl: '',
        score: 0,
      },
      showIndex: -1,
      infoList: [],
      rules: {
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
        score: [{ required: true, message: '请选择评分', trigger: 'blur' }],
      },
      total: 1,
      pageSize: 10,
      totalPage: 1,
      storeupParams: {
        name: '',
        picture: '',
        refid: 0,
        tablename: 'dianyingxinxi',
        userid: Number(localStorage.getItem('frontUserid')),
      },
      isStoreup: false,
      storeupInfo: {},
      isCrazilynum: false,
      isThumbsupnum: false,
      thumbsupOrCrazilyInfo: {},
      centerType: false,
      storeupType: false,
      title: '',
      shareUrl: location.href,
      sensitiveWordsArr: [],
    }
  },
  computed: {
    heroBackdropStyle() {
      const backdrop = this.primaryPoster || this.fallbackPoster
      return {
        backgroundImage: `url(${backdrop})`,
      }
    },
    genreList() {
      const raw = String(this.detail.dianyingleixing || '')
      return raw
        .split(/[、,，/]+/)
        .map(item => item.trim())
        .filter(Boolean)
    },
  },
  created() {
    this.syncRouteState()
    this.init()
  },
  watch: {
    $route(to, from) {
      this.syncRouteState()
      if (!from || to.query.id !== from.query.id) {
        this.init()
        return
      }
      this.restoreActiveTabSection('auto')
      this.restoreCommentDraft()
    },
    'form.content'() {
      this.persistCommentDraft()
    },
    'form.score'() {
      this.persistCommentDraft()
    },
  },
  methods: {
    syncRouteState() {
      this.centerType = !!(this.$route.query.centerType && this.$route.query.centerType != 0)
      this.storeupType = !!(this.$route.query.storeupType && this.$route.query.storeupType != 0)
      this.activeName = this.$route.query.tab === 'second' ? 'second' : 'first'
      this.shareUrl = location.href
    },
    getDetailRouteLocation(tabName) {
      const query = Object.assign({}, this.$route.query || {})
      const currentId = this.detail.id || this.id || query.id
      if (currentId) {
        query.id = currentId
      }
      if (tabName) {
        query.tab = tabName
      } else {
        delete query.tab
      }
      return {
        path: '/index/dianyingxinxiDetail',
        query,
      }
    },
    getDetailRedirectPath(tabName) {
      return this.$router.resolve(this.getDetailRouteLocation(tabName)).route.fullPath
    },
    getCommentDraftStore() {
      return useCommentDraftStore()
    },
    getCommentDraftMovieId() {
      return (this.detail && this.detail.id) || this.id || ((this.$route.query || {}).id)
    },
    resetCommentFormFields() {
      this.form.content = ''
      this.form.score = 0
      this.form.userid = Number(localStorage.getItem('frontUserid'))
      this.form.nickname = localStorage.getItem('username')
      this.form.avatarurl = ''
    },
    persistCommentDraft(force = false) {
      const movieId = this.getCommentDraftMovieId()
      if (!movieId) {
        return
      }
      const content = this.form.content ? String(this.form.content) : ''
      const score = Number(this.form.score || 0)
      if (!force && !content && !score) {
        this.clearCommentDraft()
        return
      }
      this.getCommentDraftStore().saveDraft(movieId, {
        content,
        score,
      })
    },
    restoreCommentDraft() {
      const draft = this.getCommentDraftStore().getDraft(this.getCommentDraftMovieId())
      this.form.content = draft.content || ''
      this.form.score = Number(draft.score || 0)
    },
    clearCommentDraft() {
      this.getCommentDraftStore().clearDraft(this.getCommentDraftMovieId())
    },
    redirectToFrontLogin(tabName, message) {
      if (message) {
        this.$message({ type: 'warning', message, duration: 1500 })
      }
      this.$router.push({
        path: '/login',
        query: {
          redirect: this.getDetailRedirectPath(tabName),
        },
      })
    },
    restoreActiveTabSection(behavior = 'smooth') {
      if (this.activeName !== 'second') {
        return
      }
      this.$nextTick(() => {
        const target = this.$refs.detailSection
        if (target && target.scrollIntoView) {
          target.scrollIntoView({ behavior, block: 'start' })
        }
      })
    },
    resolveMoviePoster(poster) {
      return resolvePosterUrl(poster, this.baseUrl) || this.fallbackPoster
    },
    getMovieCover(item) {
      const list = getPosterList(item || {})
      const poster = resolvePosterUrl(list[0], this.baseUrl)
      return poster || this.fallbackPoster
    },
    formatScore(score) {
      if (score === undefined || score === null || score === '') {
        return '暂无'
      }
      const value = Number(score)
      return Number.isNaN(value) ? '暂无' : value.toFixed(1)
    },
    getMovieYearText(dateText) {
      if (!dateText) {
        return '未知年份'
      }
      return String(dateText).slice(0, 4)
    },
    async loadSimilarMovies() {
      if (!this.detail || !this.detail.id) {
        this.similarMovies = []
        return
      }
      try {
        const res = await this.$http.get('appmovie/front/list', {
          params: {
            page: 1,
            limit: 60,
            sort: 'clickCount',
            order: 'desc',
          },
        })
        if (res.data && res.data.code !== undefined && res.data.code != 0) {
          throw new Error(res.data.msg || '相似电影加载失败')
        }
        const list = normalizeAppMovieList(extractAppMovieList((res.data && res.data.data) || res.data || {}))
        this.similarMovies = buildSimilarMovies(this.detail, list, 5)
      } catch (error) {
        this.similarMovies = []
      }
    },
    async init() {
      this.id = this.$route.query.id
      this.baseUrl = this.$config.baseUrl
      this.resetCommentFormFields()
      try {
        const res = await this.$http.get('appmovie/front/detail/' + this.id, {})
        if (res.data && res.data.code !== undefined && res.data.code != 0) {
          throw new Error(res.data.msg || '电影详情加载失败')
        }
        const responseData = (res.data && res.data.data) || res.data || {}
        this.detail = normalizeAppMovieRecord(extractAppMovieDetail(responseData))
        this.title = this.detail.dianyingmingcheng
        this.posterList = getPosterList(this.detail)
        this.primaryPoster = getPrimaryPoster(this.detail, this.baseUrl) || this.fallbackPoster
        this.heroMeta = formatHeroMeta(this.detail)
        const embeddedSimilarMovies = normalizeAppMovieList(extractAppMovieList(responseData))
        this.similarMovies = embeddedSimilarMovies.length ? buildSimilarMovies(this.detail, embeddedSimilarMovies, 5) : []
        this.getSensitiveWords()
        this.getDiscussList(1)
        if (!this.similarMovies.length) {
          await this.loadSimilarMovies()
        }
        if (localStorage.getItem('frontToken')) {
          this.getStoreupStatus()
          this.getThumbsupOrCrazilyStatus()
        }
        this.restoreCommentDraft()
        this.restoreActiveTabSection('auto')
      } catch (error) {
        this.detail = {}
        this.title = ''
        this.posterList = []
        this.primaryPoster = this.fallbackPoster
        this.similarMovies = []
        this.heroMeta = formatHeroMeta(this.detail)
      }
    },
    scrollToDetailSection() {
      this.activeName = 'first'
      this.$nextTick(() => {
        const target = this.$refs.detailSection
        if (target && target.scrollIntoView) {
          target.scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
      })
    },
    handleStoreupToggle() {
      if (!localStorage.getItem('frontToken')) {
        this.$message({ type: 'warning', message: '请先登录后再收藏', duration: 1500 })
        return
      }
      this.storeup(this.isStoreup ? -1 : 1)
    },
    openSimilarMovie(item) {
      if (!item || !item.id) {
        return
      }
      const query = { id: item.id }
      if (this.centerType) {
        query.centerType = 1
      }
      if (this.storeupType) {
        query.storeupType = 1
      }
      this.$router.push({ path: '/index/dianyingxinxiDetail', query })
    },
    shareToMicroblog() {
      window.open('https://service.weibo.com/share/share.php?url=' + encodeURIComponent(this.shareUrl) + '&title=' + this.title)
    },
    shareToQQRom() {
      window.open('https://sns.qzone.qq.com/cgi-bin/qzshare/cgi_qzshare_onekey?url=' + encodeURIComponent(this.shareUrl) + '&title=' + this.title + '&summary=' + this.title)
    },
    getSensitiveWords() {
      this.$http.get('keywords/list').then(rs => {
        this.sensitiveWordsArr = rs.data && rs.data.code === 0 && Array.isArray(rs.data.data) ? rs.data.data : []
      })
    },
    maskSensitiveWords(content) {
      let maskedContent = content || ''
      for (let i = 0; i < this.sensitiveWordsArr.length; i++) {
        const keyword = this.sensitiveWordsArr[i]
        if (!keyword || maskedContent.indexOf(keyword) === -1) {
          continue
        }
        const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
        maskedContent = maskedContent.replace(new RegExp(escapedKeyword, 'g'), '**')
      }
      return maskedContent
    },
    storeup(type) {
      const actionType = type == 1 ? 'favorite' : 'favorite'
      this.$http.post('appmovie/actions/toggle', {
        movieId: this.detail.id || this.id,
        actionType,
      }).then(res => {
        if (res.data && res.data.code == 0) {
          this.applyActionStatus(res.data.data || {})
          this.$message({ type: 'success', message: type == 1 ? '收藏成功!' : '取消成功!', duration: 1500 })
        }
      })
    },
    applyActionStatus(status) {
      this.isStoreup = !!status.favorite
      this.isThumbsupnum = !!status.liked
      this.isCrazilynum = !!status.disliked
      this.detail.storeupnum = Number(status.favoriteCount || 0)
      this.detail.thumbsupnum = Number(status.likeCount || 0)
      this.detail.crazilynum = Number(status.dislikeCount || 0)
    },
    getStoreupStatus() {
      if (localStorage.getItem('frontToken')) {
        this.$http.get('appmovie/actions/status', { params: { movieId: this.detail.id || this.id } }).then(res => {
          if (res.data && res.data.code == 0) {
            this.applyActionStatus(res.data.data || {})
          }
        })
      }
    },
    thumbsupOrCrazily(type) {
      const actionType = type == 21 ? 'like' : 'dislike'
      this.$http.post('appmovie/actions/toggle', {
        movieId: this.detail.id || this.id,
        actionType,
      }).then(res => {
        if (res.data && res.data.code == 0) {
          this.applyActionStatus(res.data.data || {})
          this.$message({ type: 'success', message: '操作成功!', duration: 1500 })
        }
      })
    },
    cancelThumbsupOrCrazily(type) {
      this.thumbsupOrCrazily(type)
    },
    getThumbsupOrCrazilyStatus() {
      this.getStoreupStatus()
    },
    curChange(page) {
      this.getDiscussList(page)
    },
    prevClick(page) {
      this.getDiscussList(page)
    },
    nextClick(page) {
      this.getDiscussList(page)
    },
    sizeChange(size) {
      this.pageSize = size
      this.getDiscussList(1)
    },
    hasHistoryBack() {
      return window.history && window.history.length > 1
    },
    getDefaultBackRoute() {
      return {
        path: '/index/home',
        query: {
          view: 'movies',
        },
      }
    },
    backClick() {
      if (this.storeupType) {
        history.back()
        return
      }
      if (this.centerType) {
        this.$router.push({ path: '/index/dianyingxinxi', query: { centerType: 1 } })
        return
      }
      if (this.hasHistoryBack()) {
        history.back()
        return
      }
      this.$router.push(this.getDefaultBackRoute())
    },
    download(file) {
      if (!file) {
        this.$message({ type: 'error', message: '文件不存在', duration: 1500 })
        return
      }
      const arr = file.replace(new RegExp('upload/', 'g'), '')
      axios.get(this.baseUrl + '/file/download?fileName=' + arr, {
        headers: { token: localStorage.getItem('frontToken') },
        responseType: 'blob',
      }).then(({ data }) => {
        const binaryData = [data]
        const objectUrl = window.URL.createObjectURL(new Blob(binaryData, { type: 'application/pdf;chartset=UTF-8' }))
        const a = document.createElement('a')
        a.href = objectUrl
        a.download = arr
        a.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }))
        window.URL.revokeObjectURL(data)
      })
    },
    getDiscussList(page) {
      this.$http.get('appmovie/comments/page', { params: { page, limit: this.pageSize, movieId: this.detail.id || this.id, legacyUserId: this.userid, sort: 'istop', order: 'desc' } }).then(res => {
        if (res.data.code == 0) {
          this.infoList = (res.data.data.list || []).map(this.normalizeCommentItem)
          this.total = res.data.data.total
          this.pageSize = Number(res.data.data.pageSize)
          this.totalPage = res.data.data.totalPage
        }
      })
    },
    normalizeCommentItem(item) {
      return Object.assign({}, item || {}, {
        userid: item.legacyUserId,
        nickname: item.authorName || '匿名用户',
        avatarurl: item.authorAvatar || '',
        content: item.contentHtml || '',
        score: Number(item.rating || 0),
        reply: item.replyHtml || '',
        thumbsupnum: Number(item.likeCount || 0),
        crazilynum: Number(item.dislikeCount || 0),
        istop: item.pinned ? 1 : 0,
        addtime: item.createdAt || '',
        tuserids: item.currentUserVote === 'like' ? String(this.userid || '') : '',
        cuserids: item.currentUserVote === 'dislike' ? String(this.userid || '') : '',
      })
    },
    comzanChange(row) {
      if (row.tuserids) {
        const arr = row.tuserids.split(',')
        for (let x in arr) {
          if (arr[x] == this.userid) {
            return true
          }
        }
      }
      return false
    },
    comzanClick(row) {
      if (!this.userid) {
        return false
      }
      if (!this.comzanChange(row)) {
        row.thumbsupnum++
        row.tuserids = row.tuserids ? row.tuserids + ',' + this.userid : String(this.userid)
        this.toggleCommentVote(row, 'like', '点赞成功')
      } else {
        this.toggleCommentVote(row, 'like', '取消成功')
      }
    },
    comcaiChange(row) {
      if (row.cuserids) {
        const arr = row.cuserids.split(',')
        for (let x in arr) {
          if (arr[x] == this.userid) {
            return true
          }
        }
      }
      return false
    },
    comcaiClick(row) {
      if (!this.userid) {
        return false
      }
      if (!this.comcaiChange(row)) {
        row.crazilynum++
        row.cuserids = row.cuserids ? row.cuserids + ',' + this.userid : String(this.userid)
        this.toggleCommentVote(row, 'dislike', '点踩成功')
      } else {
        this.toggleCommentVote(row, 'dislike', '取消成功')
      }
    },
    toggleCommentVote(row, voteType, message) {
      this.$http.post('appmovie/comments/vote', {
        commentId: row.id,
        voteType,
      }).then(res => {
        if (res.data && res.data.code == 0 && res.data.data) {
          Object.assign(row, this.normalizeCommentItem(res.data.data))
          this.$message.success(message)
        }
      })
    },
    discussEnter(index) {
      this.showIndex = index
    },
    discussLeave() {
      this.showIndex = -1
    },
    discussDel(id) {
      this.$confirm('是否删除此评论？').then(() => {
        this.$http.post('appmovie/comments/delete', { commentId: id }).then(res => {
          if (res.data && res.data.code == 0) {
            this.addDiscussNum(1)
            this.$message({
              type: 'success',
              message: '删除成功!',
              duration: 1500,
              onClose: () => {
                this.getDiscussList(1)
              },
            })
          }
        })
      }).catch(() => {})
    },
    submitForm(formName) {
      if (!localStorage.getItem('frontToken')) {
        this.persistCommentDraft(true)
        this.redirectToFrontLogin('second', '请先登录后再发表评论')
        return false
      }
      this.form.content = this.maskSensitiveWords(this.form.content)
      this.$refs[formName].validate(valid => {
        if (valid) {
          const payload = {
            movieId: this.detail.id || this.id,
            contentHtml: this.form.content,
            rating: Number(this.form.score || 0),
            authorAvatar: localStorage.getItem('frontHeadportrait') ? localStorage.getItem('frontHeadportrait') : '',
          }
          this.$http.post('appmovie/comments/add', payload).then(rs2 => {
            if (rs2.data.code == 0 && rs2.data.data) {
              this.addDiscussNum(2)
              this.clearCommentDraft()
              this.form.content = ''
              this.form.score = 0
              this.getDiscussList(1)
              this.$message({ type: 'success', message: '评论成功!', duration: 1500 })
            } else {
              this.$message({ type: 'error', message: '每个用户只能评论一次!', duration: 1500 })
            }
          })
        } else {
          return false
        }
      })
    },
    resetForm(formName) {
      this.$refs[formName].resetFields()
      this.clearCommentDraft()
    },
    addDiscussNum(type) {
      if (type == 2) {
        this.detail.discussnum = Number(this.detail.discussnum || 0) + 1
      } else if (type == 1) {
        this.detail.discussnum = Math.max(Number(this.detail.discussnum || 0) - 1, 0)
      }
      this.$http.get('appmovie/front/detail/' + (this.detail.id || this.id), {}).then(rs => {
        if (rs.data && rs.data.code == 0 && rs.data.data) {
          const latest = normalizeAppMovieRecord(extractAppMovieDetail(rs.data.data))
          this.detail.discussnum = latest.discussnum
          this.detail.totalscore = latest.totalscore
          this.heroMeta = formatHeroMeta(this.detail)
        }
      })
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.prototype-detail-page {
  min-height: 100vh;
  background: #0b1326;
  color: #dae2fd;
}

.detail-shell {
  width: 100%;
}

.back_box.prototype-back-box {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 60;
  width: auto;
  min-width: 0;
  padding: 0;
  margin: 0;
  display: inline-flex;
  justify-content: flex-start;
  align-items: center;
  background: transparent;
  border-radius: 999px;
  box-shadow: none;
}

.prototype-back-box .backBtn.el-button {
  min-width: 0;
  height: 40px;
  padding: 0 16px;
  margin: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid rgba(232, 198, 120, 0.28);
  border-radius: 999px;
  background: rgba(11, 19, 38, 0.72);
  color: #e9efff;
  box-shadow: 0 14px 36px rgba(0, 0, 0, 0.34), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(14px);
  transition: background 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.prototype-back-box .backBtn.el-button .icon,
.prototype-back-box .backBtn.el-button .text {
  color: #e9efff;
}

.prototype-back-box .backBtn.el-button .icon {
  margin: 0;
  font-size: 14px;
}

.prototype-back-box .backBtn.el-button .text {
  line-height: 1;
}

.prototype-back-box .backBtn.el-button:hover,
.prototype-back-box .backBtn.el-button:focus {
  border-color: rgba(232, 198, 120, 0.48);
  background: rgba(24, 35, 62, 0.82);
  color: #ffffff;
  box-shadow: 0 18px 42px rgba(0, 0, 0, 0.4), 0 0 0 1px rgba(232, 198, 120, 0.08);
  transform: translateY(-1px);
}

.prototype-back-box .backBtn.el-button:hover .icon,
.prototype-back-box .backBtn.el-button:hover .text,
.prototype-back-box .backBtn.el-button:focus .icon,
.prototype-back-box .backBtn.el-button:focus .text {
  color: #ffffff;
}

.detail-main {
  min-height: 100vh;
  padding-bottom: 96px;
}

.hero-section {
  position: relative;
  width: 100%;
  min-height: 870px;
  overflow: hidden;
  background-position: center center;
  background-size: cover;
}

.hero-background-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, #0b1326 0%, rgba(11, 19, 38, 0.4) 45%, transparent 100%),
    linear-gradient(to right, rgba(11, 19, 38, 0.8) 0%, transparent 60%);
}

.hero-content {
  position: relative;
  z-index: 1;
  min-height: 870px;
  max-width: 1536px;
  margin: 0 auto;
  padding: 0 32px 96px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.hero-inner {
  display: flex;
  align-items: flex-end;
  gap: 32px;
}

.hero-poster-wrap {
  width: 288px;
  aspect-ratio: 2 / 3;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.6);
  margin-bottom: -48px;
}

.hero-poster-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-copy {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-meta-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
}

.meta-chip {
  background: #222a3d;
  color: #d0c5af;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}

.meta-chip-primary {
  background: rgba(255, 198, 57, 0.1);
  color: #ffc639;
}

.meta-rating {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #ffc639;
  font-size: 14px;
  font-weight: 700;
}

.hero-title {
  margin: 0;
  font-size: 72px;
  line-height: 0.95;
  font-weight: 900;
  letter-spacing: -0.04em;
  color: #dae2fd;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding-top: 8px;
}

.primary-btn,
.ghost-btn,
.submitBtn,
.resetBtn,
.delBtn {
  border-radius: 999px;
  height: 52px;
  padding: 0 28px;
  font-weight: 700;
}

.primary-btn {
  border: none;
  background: linear-gradient(135deg, #ffc639, #e1aa12);
  color: #3f2e00;
}

.ghost-btn {
  border: 1px solid rgba(153, 144, 124, 0.35);
  background: transparent;
  color: #dae2fd;
}

.overview-section,
.similar-section,
.engagement-section {
  max-width: 1536px;
  margin: 64px auto 0;
  padding: 0 32px;
}

.overview-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.8fr) minmax(320px, 1fr);
  gap: 32px;
}

.overview-copy h2,
.section-head h2,
.engagement-head h2 {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #dae2fd;
}

.overview-copy p {
  margin: 24px 0 0;
  max-width: 820px;
  font-size: 18px;
  line-height: 1.8;
  color: rgba(218, 226, 253, 0.8);
}

.genre-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 24px;
}

.genre-chip {
  background: #222a3d;
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  color: #dae2fd;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.info-card,
.similar-item,
.comment-item,
.prototype-tabs {
  background: #131b2e;
}

.info-card {
  padding: 24px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 124px;
}

.info-label {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: #d0c5af;
}

.info-value {
  margin-top: 8px;
  color: #dae2fd;
  font-weight: 700;
  line-height: 1.6;
}

.section-head,
.engagement-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
}

.similar-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 24px;
}

.similar-item {
  cursor: pointer;
  border-radius: 18px;
  overflow: hidden;
  transition: transform 0.3s ease;
}

.similar-item:hover {
  transform: translateY(-4px);
}

.similar-cover {
  aspect-ratio: 2 / 3;
  overflow: hidden;
}

.similar-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.similar-info {
  padding: 12px 4px 0;
}

.similar-info h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #dae2fd;
}

.similar-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 8px;
  color: #99907c;
  font-size: 10px;
  font-weight: 600;
}

.empty-state {
  color: rgba(218, 226, 253, 0.72);
}

.prototype-zancai {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.zan,
.cai,
.zanActive,
.caiActive {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 44px;
  padding: 0 18px;
  border-radius: 999px;
  background: #171f33;
}

.zan,
.zanActive {
  color: #dae2fd;
}

.cai,
.caiActive {
  color: #bcc7de;
}

.detail-anchor {
  height: 1px;
}

.prototype-tabs {
  border: none;
  border-radius: 28px;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(16, 24, 40, 0.96), rgba(11, 19, 32, 0.98));
  box-shadow: 0 24px 54px rgba(0, 0, 0, 0.36), inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

.prototype-tabs ::v-deep .el-tabs__header {
  margin: 0;
  border: none;
  background: rgba(22, 31, 51, 0.88);
}

.prototype-tabs ::v-deep .el-tabs__item {
  color: rgba(218, 226, 253, 0.72);
  height: 58px;
  line-height: 58px;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 700;
}

.prototype-tabs ::v-deep .el-tabs__item.is-active {
  color: #ffc639;
  background: linear-gradient(180deg, rgba(255, 198, 57, 0.14), rgba(255, 198, 57, 0.04));
}

.prototype-tabs ::v-deep .el-tabs__content {
  padding: 28px;
}

.detail-html,
.comment-content-box ::v-deep .ql-editor {
  color: rgba(218, 226, 253, 0.88);
}

.comment-scene {
  display: flex;
  flex-direction: column;
  gap: 26px;
}

.comment-composer-card,
.comment-stream,
.comment-card,
.comment-empty-state {
  border: 1px solid rgba(255, 255, 255, 0.05);
  background:
    radial-gradient(circle at top right, rgba(255, 198, 57, 0.08), transparent 26%),
    linear-gradient(180deg, rgba(23, 31, 51, 0.96), rgba(14, 22, 39, 0.98));
  box-shadow: 0 22px 48px rgba(0, 0, 0, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

.comment-composer-card,
.comment-stream {
  border-radius: 24px;
  padding: 24px;
}

.comment-composer-head,
.comment-stream-head,
.comment-card-head,
.comment-footer,
.comment-form-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.comment-composer-head {
  margin-bottom: 20px;
}

.comment-composer-copy,
.comment-stream-copy {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-kicker {
  color: #d0c5af;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.comment-composer-copy h3,
.comment-stream-copy h3 {
  margin: 0;
  color: #f6f8ff;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.comment-composer-copy p {
  margin: 0;
  max-width: 760px;
  color: rgba(218, 226, 253, 0.72);
  font-size: 14px;
  line-height: 1.8;
}

.comment-rule-chip,
.comment-stream-total,
.comment-pin {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 38px;
  padding: 0 16px;
  border-radius: 999px;
  background: rgba(255, 198, 57, 0.12);
  color: #ffc639;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.commentForm {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.commentForm ::v-deep .el-form-item {
  margin-bottom: 0;
}

.commentForm ::v-deep .el-form-item__label {
  color: #d0c5af;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.comment-editor-shell {
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid rgba(255, 198, 57, 0.14);
  background: linear-gradient(180deg, rgba(11, 19, 32, 0.9), rgba(18, 26, 42, 0.98));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

.comment-editor-shell ::v-deep .ql-toolbar.ql-snow {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 10px;
  padding: 14px 16px 12px;
  border: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.02);
}

.comment-editor-shell ::v-deep .ql-formats {
  margin-right: 0 !important;
}

.comment-editor-shell ::v-deep .ql-snow .ql-stroke {
  stroke: rgba(218, 226, 253, 0.72);
}

.comment-editor-shell ::v-deep .ql-snow .ql-fill {
  fill: rgba(218, 226, 253, 0.72);
}

.comment-editor-shell ::v-deep .ql-snow .ql-picker {
  color: rgba(218, 226, 253, 0.72);
}

.comment-editor-shell ::v-deep .ql-toolbar button:hover .ql-stroke,
.comment-editor-shell ::v-deep .ql-toolbar button:hover .ql-fill,
.comment-editor-shell ::v-deep .ql-toolbar button.ql-active .ql-stroke,
.comment-editor-shell ::v-deep .ql-toolbar button.ql-active .ql-fill,
.comment-editor-shell ::v-deep .ql-toolbar .ql-picker-label:hover,
.comment-editor-shell ::v-deep .ql-toolbar .ql-picker-label.ql-active {
  stroke: #ffc639;
  fill: #ffc639;
  color: #ffc639;
}

.comment-editor-shell ::v-deep .ql-container.ql-snow {
  border: none;
  background: transparent;
}

.comment-editor-shell ::v-deep .ql-editor {
  padding: 18px 18px 20px;
  color: rgba(246, 248, 255, 0.92);
  font-size: 15px;
  line-height: 1.8;
}

.comment-editor-shell ::v-deep .ql-editor.ql-blank::before {
  color: rgba(218, 226, 253, 0.38);
  font-style: normal;
}

.comment-score-item {
  flex: 1;
}

.comment-score-wrap {
  display: flex;
  align-items: center;
  gap: 18px;
  min-height: 64px;
  border-radius: 18px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.comment-score-copy {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.comment-score-label {
  color: #dae2fd;
  font-size: 15px;
  font-weight: 700;
}

.comment-score-copy p {
  margin: 0;
  color: rgba(218, 226, 253, 0.58);
  font-size: 13px;
}

.comment-score-wrap ::v-deep .el-rate {
  margin-left: auto;
}

.commentBtn {
  margin-bottom: 0;
}

.submitBtn,
.resetBtn,
.delBtn {
  min-width: 108px;
  height: 44px;
  border: none;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.08em;
  transition: transform 0.22s ease, box-shadow 0.22s ease, background-color 0.22s ease, color 0.22s ease;
}

.submitBtn {
  background: linear-gradient(135deg, #ffc639, #e1aa12);
  color: #3f2e00;
  box-shadow: 0 16px 28px rgba(225, 170, 18, 0.28);
}

.submitBtn:hover,
.resetBtn:hover,
.delBtn:hover {
  transform: translateY(-2px);
}

.resetBtn,
.delBtn {
  background: rgba(52, 62, 87, 0.92);
  color: #dae2fd;
}

.comment-list {
  display: grid;
  gap: 18px;
  margin-top: 20px;
}

.comment-card {
  border-radius: 22px;
  padding: 20px;
}

.user {
  display: flex;
  align-items: center;
  gap: 14px;
}

.comment-avatar-shell {
  flex: 0 0 auto;
  width: 56px;
  height: 56px;
  padding: 2px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 198, 57, 0.42), rgba(76, 118, 207, 0.3));
}

.user .el-image {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  overflow: hidden;
  display: block;
}

.comment-user-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.comment-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user .name {
  color: #f6f8ff;
  font-size: 16px;
  font-weight: 700;
}

.comment-time {
  color: rgba(218, 226, 253, 0.52);
  font-size: 12px;
  letter-spacing: 0.04em;
}

.comment-card-actions {
  display: flex;
  align-items: center;
  margin-left: auto;
}

.comment-content-box {
  margin-top: 18px;
}

.comment-body {
  padding: 2px 2px 0;
}

.comment-body ::v-deep .ql-editor {
  padding: 0;
  line-height: 1.9;
}

.comment-footer {
  margin-top: 18px;
  align-items: flex-end;
}

.comment-rate {
  flex: 0 0 auto;
}

.zancai-box {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  margin-left: auto;
}

.zan-item,
.cai-item {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 40px;
  padding: 0 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.03);
  color: rgba(218, 226, 253, 0.74);
  transition: background-color 0.22s ease, color 0.22s ease, transform 0.22s ease;
}

.zan-item:hover,
.cai-item:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.06);
}

.zan-item.active,
.cai-item.active {
  background: rgba(255, 198, 57, 0.12);
  color: #ffc639;
}

.reply-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
  border-radius: 18px;
  padding: 16px 18px;
  background: rgba(255, 255, 255, 0.03);
  color: rgba(218, 226, 253, 0.76);
}

.reply-label {
  color: #d0c5af;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.reply-box ::v-deep .ql-editor {
  padding: 0;
}

.comment-empty-state {
  margin-top: 20px;
  border-radius: 22px;
  padding: 40px 24px;
  text-align: center;
  color: rgba(218, 226, 253, 0.68);
  font-size: 15px;
}

.pagination {
  margin-top: 26px;
  text-align: center;
}

.pagination ::v-deep .btn-prev,
.pagination ::v-deep .btn-next,
.pagination ::v-deep .el-pager li,
.pagination ::v-deep .el-pagination__jump,
.pagination ::v-deep .el-pagination__total,
.pagination ::v-deep .el-pagination__sizes .el-input__inner,
.pagination ::v-deep .el-pagination__editor.el-input .el-input__inner {
  background: rgba(34, 42, 61, 0.92);
  color: #dae2fd;
  border: none;
}

.pagination ::v-deep .btn-prev,
.pagination ::v-deep .btn-next,
.pagination ::v-deep .el-pager li {
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  border-radius: 12px;
}

.pagination ::v-deep .el-pager li.active {
  background: linear-gradient(135deg, #ffc639, #e1aa12);
  color: #3f2e00;
}

.share_view {
  position: fixed;
  right: 16px;
  bottom: 20%;
  z-index: 20;
  overflow: hidden;
  border-radius: 12px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.4);
}

.share {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #171f33;
}

.share img {
  width: 24px;
  height: 24px;
}

@media (max-width: 1280px) {
  .similar-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .hero-inner,
  .overview-grid {
    grid-template-columns: 1fr;
    display: grid;
  }

  .hero-poster-wrap {
    width: 240px;
    margin-bottom: 0;
  }

  .hero-title {
    font-size: 56px;
  }

  .similar-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .comment-composer-head,
  .comment-stream-head,
  .comment-card-head,
  .comment-footer,
  .comment-form-footer,
  .comment-score-wrap {
    align-items: flex-start;
    flex-direction: column;
  }

  .comment-card-actions,
  .zancai-box,
  .comment-score-wrap ::v-deep .el-rate {
    margin-left: 0;
  }
}

@media (max-width: 768px) {
  .back_box.prototype-back-box {
    top: 12px;
    left: 12px;
  }

  .prototype-back-box .backBtn.el-button {
    height: 36px;
    padding: 0 12px;
  }

  .hero-section {
    min-height: 680px;
  }

  .hero-content,
  .overview-section,
  .similar-section,
  .engagement-section {
    padding-left: 20px;
    padding-right: 20px;
  }

  .hero-inner {
    gap: 20px;
  }

  .hero-poster-wrap {
    display: none;
  }

  .hero-title {
    font-size: 42px;
  }

  .overview-cards,
  .similar-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .comment-composer-card,
  .comment-stream,
  .comment-card {
    padding: 18px;
  }

  .comment-composer-copy h3,
  .comment-stream-copy h3 {
    font-size: 24px;
  }

  .comment-name-row,
  .user {
    align-items: flex-start;
    flex-direction: column;
  }

  .share_view {
    display: none;
  }
}
</style>
