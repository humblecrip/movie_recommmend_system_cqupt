<template>
	<div class="dashboard-home">
		<section class="hero-panel">
			<div class="hero-copy">
				<p class="hero-kicker">Dashboard Overview</p>
				<h2>影院业务总览</h2>
				<span>读取 app_movie 与真实互动表聚合接口，集中展示后台关键指标。</span>
			</div>

			<div class="hero-pill">
				<i class="el-icon-date"></i>
				<span>最近 30 天</span>
			</div>
		</section>

		<section class="stats-grid">
			<article
				v-for="stat in stats"
				:key="stat.key"
				class="stat-card"
				:class="stat.key"
			>
				<div class="stat-head">
					<div class="stat-icon">
						<i :class="stat.icon"></i>
					</div>
					<span class="stat-tag">{{ stat.tag }}</span>
				</div>
				<strong>{{ formatNumber(stat.value) }}</strong>
				<p>{{ stat.label }}</p>
			</article>
		</section>

		<section v-if="canViewCharts" class="dashboard-grid">
			<article class="dashboard-card">
				<div class="card-head">
					<div>
						<h3>评分分布</h3>
						<span>影片评分值趋势</span>
					</div>
					<div class="legend-mark">
						<i></i>
						<em>评分值</em>
					</div>
				</div>
				<div id="dianyingxinxiChart1" class="chart-panel large-chart"></div>
			</article>

			<article class="dashboard-card">
				<div class="card-head">
					<div>
						<h3>收藏趋势</h3>
						<span>影片收藏热度对比</span>
					</div>
					<div class="card-select">收藏行为</div>
				</div>
				<div id="dianyingxinxiChart2" class="chart-panel large-chart"></div>
			</article>

			<article class="dashboard-card">
				<div class="card-head">
					<div>
						<h3>点击热度</h3>
						<span>Top Performing Titles</span>
					</div>
				</div>
				<div class="trend-list">
					<div v-for="item in clickRanking" :key="item.name" class="trend-item">
						<div class="trend-copy">
							<span>{{ item.name }}</span>
							<em>{{ formatNumber(item.value) }}</em>
						</div>
						<div class="trend-bar">
							<div class="trend-fill" :style="{ width: item.percent + '%' }"></div>
						</div>
					</div>
				</div>
			</article>

			<article class="dashboard-card">
				<div class="card-head">
					<div>
						<h3>口碑得分</h3>
						<span>点击 / 点赞 / 收藏叠加</span>
					</div>
				</div>
				<div class="stack-metrics">
					<div v-for="item in popularityStacks" :key="item.name" class="stack-item">
						<div class="stack-bar">
							<div class="stack-part clicks" :style="{ height: item.clickHeight + '%' }"></div>
							<div class="stack-part likes" :style="{ height: item.likeHeight + '%' }"></div>
							<div class="stack-part favorites" :style="{ height: item.favoriteHeight + '%' }"></div>
						</div>
						<p>{{ item.shortName }}</p>
					</div>
				</div>
			</article>

			<article class="engagement-card">
				<div class="engagement-copy">
					<p class="hero-kicker">User Engagement</p>
					<h3>用户参与度</h3>
					<span>通过真实收藏、点赞与点踩行为，快速观察影片库的互动分布。</span>

					<div class="engagement-legend">
						<div class="legend-row">
							<i class="gold"></i>
							<span>收藏量 ({{ formatNumber(totalCollections) }})</span>
						</div>
						<div class="legend-row">
							<i class="light"></i>
							<span>点赞量 ({{ formatNumber(totalLikes) }})</span>
						</div>
						<div class="legend-row">
							<i class="amber"></i>
							<span>点踩量 ({{ formatNumber(totalDislikes) }})</span>
						</div>
					</div>
				</div>

				<div class="engagement-chart">
					<div id="dianyingxinxiChart5" class="chart-panel donut-chart"></div>
					<div class="engagement-center">
						<strong>{{ formatNumber(totalUsers) }}</strong>
						<p>总用户数</p>
					</div>
				</div>
			</article>
		</section>

		<section v-else class="empty-card">
			<i class="el-icon-lock"></i>
			<p>当前账号没有首页统计权限。</p>
		</section>
	</div>
</template>

<script>
import router from '@/router/router-static'
import * as echarts from 'echarts'

export default {
	data() {
		return {
			dianyingxinxiCount: 0,
			totalUsers: 0,
			totalCollections: 0,
			totalClicks: 0,
			totalLikes: 0,
			totalDislikes: 0,
			scoreTrend: [],
			collectionTrend: [],
			clickRanking: [],
			popularityStacks: [],
			engagementSlices: [],
			charts: {
				line: null,
				bar: null,
				donut: null
			}
		}
	},
	computed: {
		canViewCharts() {
			return this.isAuth('dianyingxinxi', '首页统计')
		},
		stats() {
			return [
				{
					key: 'films',
					label: '影片总数',
					value: this.dianyingxinxiCount,
					tag: '影片资产',
					icon: 'el-icon-s-ticket'
				},
				{
					key: 'users',
					label: '活跃用户',
					value: this.totalUsers,
					tag: '账号规模',
					icon: 'el-icon-user'
				},
				{
					key: 'collections',
					label: '收藏总量',
					value: this.totalCollections,
					tag: '行为沉淀',
					icon: 'el-icon-star-on'
				},
				{
					key: 'clicks',
					label: '累计点击',
					value: this.totalClicks,
					tag: '流量表现',
					icon: 'el-icon-s-data'
				}
			]
		}
	},
	mounted() {
		this.init()
		this.loadDashboard()
		window.addEventListener('resize', this.handleResize)
	},
	beforeDestroy() {
		window.removeEventListener('resize', this.handleResize)
		this.disposeCharts()
	},
	methods: {
		init() {
			if (this.$storage.get('Token')) {
				this.$http({
					url: `${this.$storage.get('sessionTable')}/session`,
					method: 'get'
				}).then(({ data }) => {
					if (data && data.code !== 0) {
						router.push({ name: 'login' })
					}
				})
			} else {
				router.push({ name: 'login' })
			}
		},
		async loadDashboard() {
			try {
				const [overview, userPage] = await Promise.all([
					this.request('appmovie/admin/dashboard/overview'),
					this.request('yonghu/page', { page: 1, limit: 1, sort: 'id', order: 'desc' })
				])
				const dashboard = overview || {}

				this.dianyingxinxiCount = this.pickNumber(dashboard, ['movieTotal'])
				this.totalUsers = this.pickNumber(dashboard, ['totalUsers', 'userCount'], Number((userPage && userPage.total) || 0))
				this.totalCollections = this.pickNumber(dashboard, ['totalFavoriteCount'])

				this.scoreTrend = this.normalizeMetricList(this.pickList(dashboard, ['releaseYearDistribution', 'typeDistribution']), 7, ['value'])
				this.collectionTrend = this.normalizeMetricList(this.pickList(dashboard, ['topFavoritedMovies']), 7, ['favoriteCount'])
				const clickList = this.normalizeMetricList(this.pickList(dashboard, ['topClickedMovies']), 5, ['clickCount'])
				const likesList = this.normalizeMetricList(this.pickList(dashboard, ['topLikedMovies']), 5, ['likeCount'])
				const favoritesList = this.normalizeMetricList(this.pickList(dashboard, ['topFavoritedMovies']), 5, ['favoriteCount'])

				this.totalClicks = this.pickNumber(dashboard, ['totalClickCount'], this.sumMetricValue(clickList))
				this.totalLikes = this.pickNumber(dashboard, ['totalLikeCount'], this.sumMetricValue(likesList))
				this.totalDislikes = this.pickNumber(dashboard, ['totalDislikeCount'])
				this.clickRanking = this.buildRanking(clickList)
				this.popularityStacks = this.buildStacks(clickList, likesList, favoritesList)
				this.engagementSlices = [
					{ name: '收藏量', value: this.totalCollections },
					{ name: '点赞量', value: this.totalLikes },
					{ name: '点踩量', value: this.totalDislikes }
				]

				this.$nextTick(() => {
					if (this.canViewCharts) {
						this.renderCharts()
					}
				})
			} catch (error) {
				this.$message({
					message: error.message || '后台首页统计加载失败',
					type: 'warning',
					duration: 1800
				})
			}
		},
		request(url, params = {}) {
			return this.$http({
				url,
				method: 'get',
				params
			}).then(({ data }) => {
				if (data && data.code === 0) {
					return data.data
				}
				throw new Error((data && data.msg) || '请求失败')
			})
		},
		pickNumber(source, keys, fallback = 0) {
			for (let index = 0; index < keys.length; index++) {
				const value = source && source[keys[index]]
				if (value !== undefined && value !== null && value !== '') {
					const numberValue = Number(value)
					return Number.isFinite(numberValue) ? numberValue : fallback
				}
			}
			return fallback
		},
		pickList(source, keys) {
			for (let index = 0; index < keys.length; index++) {
				const value = source && source[keys[index]]
				if (Array.isArray(value)) {
					return value
				}
			}
			return []
		},
		normalizeMetricList(list, limit, valueKeys = ['total', 'value']) {
			return (list || []).slice(0, limit).map(item => ({
				name: item.title || item.movieTitle || item.label || item.name || item.dianyingmingcheng || '未命名电影',
				value: this.pickNumber(item, valueKeys)
			}))
		},
		sumMetricValue(list) {
			return list.reduce((total, item) => total + Number(item.value || 0), 0)
		},
		buildRanking(list) {
			const max = Math.max(...list.map(item => item.value), 1)
			return list.slice(0, 3).map(item => ({
				name: item.name,
				value: item.value,
				percent: Math.max(18, (item.value / max) * 100)
			}))
		},
		buildStacks(clicks, likes, favorites) {
			const lookup = {}
			clicks.slice(0, 3).forEach(item => {
				lookup[item.name] = {
					name: item.name,
					shortName: item.name.length > 6 ? `${item.name.slice(0, 6)}...` : item.name,
					clicks: item.value,
					likes: 0,
					favorites: 0
				}
			})
			likes.forEach(item => {
				if (lookup[item.name]) {
					lookup[item.name].likes = item.value
				}
			})
			favorites.forEach(item => {
				if (lookup[item.name]) {
					lookup[item.name].favorites = item.value
				}
			})
			return Object.values(lookup).map(item => {
				const total = Math.max(item.clicks + item.likes + item.favorites, 1)
				return {
					name: item.name,
					shortName: item.shortName,
					clickHeight: (item.clicks / total) * 100,
					likeHeight: (item.likes / total) * 100,
					favoriteHeight: (item.favorites / total) * 100
				}
			})
		},
		formatNumber(value) {
			return Number(value || 0).toLocaleString()
		},
		renderCharts() {
			this.disposeCharts()
			this.renderScoreChart()
			this.renderCollectionChart()
			this.renderDonutChart()
		},
		renderScoreChart() {
			const dom = document.getElementById('dianyingxinxiChart1')
			if (!dom) {
				return
			}
			this.charts.line = echarts.init(dom)
			this.charts.line.setOption({
				backgroundColor: 'transparent',
				grid: {
					top: 32,
					right: 18,
					bottom: 28,
					left: 42
				},
				tooltip: {
					trigger: 'axis',
					backgroundColor: '#151d31',
					borderColor: 'rgba(255,198,57,0.18)',
					textStyle: {
						color: '#f5f7ff'
					}
				},
				xAxis: {
					type: 'category',
					data: this.scoreTrend.map(item => item.name),
					axisLine: { lineStyle: { color: 'rgba(218,226,253,0.18)' } },
					axisLabel: { color: 'rgba(218,226,253,0.56)' }
				},
				yAxis: {
					type: 'value',
					axisLine: { show: false },
					axisTick: { show: false },
					splitLine: { lineStyle: { color: 'rgba(218,226,253,0.08)' } },
					axisLabel: { color: 'rgba(218,226,253,0.56)' }
				},
				series: [{
					data: this.scoreTrend.map(item => item.value),
					type: 'line',
					smooth: true,
					symbolSize: 8,
					lineStyle: {
						width: 3,
						color: '#ffc639'
					},
					itemStyle: {
						color: '#ffc639'
					},
					areaStyle: {
						color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
							{ offset: 0, color: 'rgba(255,198,57,0.30)' },
							{ offset: 1, color: 'rgba(255,198,57,0.02)' }
						])
					}
				}]
			})
		},
		renderCollectionChart() {
			const dom = document.getElementById('dianyingxinxiChart2')
			if (!dom) {
				return
			}
			this.charts.bar = echarts.init(dom)
			this.charts.bar.setOption({
				backgroundColor: 'transparent',
				grid: {
					top: 26,
					right: 8,
					bottom: 28,
					left: 42
				},
				tooltip: {
					trigger: 'axis',
					backgroundColor: '#151d31',
					borderColor: 'rgba(255,198,57,0.18)',
					textStyle: {
						color: '#f5f7ff'
					}
				},
				xAxis: {
					type: 'category',
					data: this.collectionTrend.map(item => item.name),
					axisLine: { lineStyle: { color: 'rgba(218,226,253,0.18)' } },
					axisLabel: { color: 'rgba(218,226,253,0.56)' }
				},
				yAxis: {
					type: 'value',
					axisLine: { show: false },
					axisTick: { show: false },
					splitLine: { lineStyle: { color: 'rgba(218,226,253,0.08)' } },
					axisLabel: { color: 'rgba(218,226,253,0.56)' }
				},
				series: [{
					type: 'bar',
					data: this.collectionTrend.map(item => item.value),
					barWidth: 26,
					itemStyle: {
						borderRadius: [16, 16, 0, 0],
						color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
							{ offset: 0, color: '#ffc639' },
							{ offset: 1, color: 'rgba(255,198,57,0.24)' }
						])
					}
				}]
			})
		},
		renderDonutChart() {
			const dom = document.getElementById('dianyingxinxiChart5')
			if (!dom) {
				return
			}
			this.charts.donut = echarts.init(dom)
			this.charts.donut.setOption({
				backgroundColor: 'transparent',
				tooltip: {
					trigger: 'item',
					backgroundColor: '#151d31',
					borderColor: 'rgba(255,198,57,0.18)',
					textStyle: {
						color: '#f5f7ff'
					}
				},
				legend: {
					show: false
				},
				color: ['#ffc639', 'rgba(218,226,253,0.46)', '#e1aa12'],
				series: [{
					type: 'pie',
					radius: ['58%', '78%'],
					center: ['50%', '50%'],
					avoidLabelOverlap: false,
					label: { show: false },
					labelLine: { show: false },
					data: this.engagementSlices
				}]
			})
		},
		handleResize() {
			Object.keys(this.charts).forEach(key => {
				if (this.charts[key]) {
					this.charts[key].resize()
				}
			})
		},
		disposeCharts() {
			Object.keys(this.charts).forEach(key => {
				if (this.charts[key]) {
					this.charts[key].dispose()
					this.charts[key] = null
				}
			})
		}
	}
}
</script>

<style lang="scss" scoped>
	.dashboard-home {
		color: #dae2fd;
		padding: 0 2px 14px;
	}

	.hero-panel,
	.dashboard-card,
	.engagement-card,
	.empty-card,
	.stat-card {
		border: 1px solid rgba(255, 255, 255, 0.06);
		backdrop-filter: blur(20px);
		background: rgba(20, 28, 47, 0.82);
	}

	.hero-panel {
		border-radius: 28px;
		padding: 22px 24px;
		margin-bottom: 18px;
		background:
			radial-gradient(circle at right top, rgba(255, 198, 57, 0.18), transparent 32%),
			linear-gradient(135deg, rgba(34, 42, 61, 0.95), rgba(16, 24, 42, 0.92));
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
		gap: 14px;
	}

	.hero-kicker {
		margin: 0 0 6px;
		color: #ffc639;
		font-size: 11px;
		font-weight: 800;
		letter-spacing: 0.2em;
		text-transform: uppercase;
	}

	.hero-copy {
		h2,
		span {
			margin: 0;
		}

		h2 {
			color: #f7f8fd;
			font-size: 30px;
			line-height: 1.1;
			margin-bottom: 8px;
		}

		span {
			color: rgba(218, 226, 253, 0.58);
			font-size: 13px;
			line-height: 1.6;
			max-width: 560px;
			display: block;
		}
	}

	.hero-pill {
		border-radius: 999px;
		padding: 0 15px;
		color: rgba(218, 226, 253, 0.78);
		font-size: 11px;
		font-weight: 700;
		letter-spacing: 0.12em;
		text-transform: uppercase;
		background: rgba(255, 255, 255, 0.05);
		display: inline-flex;
		align-items: center;
		gap: 8px;
		height: 38px;
	}

	.stats-grid {
		display: grid;
		grid-template-columns: repeat(4, minmax(0, 1fr));
		gap: 14px;
		margin-bottom: 18px;
	}

	.stat-card {
		border-radius: 24px;
		padding: 18px;
		position: relative;
		overflow: hidden;
		transition: transform 0.25s ease, box-shadow 0.25s ease;

		&::after {
			content: '';
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			height: 2px;
			background: linear-gradient(90deg, transparent, rgba(255, 198, 57, 0.55), transparent);
		}

		&:hover {
			transform: translateY(-2px);
			box-shadow: 0 24px 50px rgba(0, 0, 0, 0.22);
		}

		strong {
			color: #f5f7ff;
			font-size: 30px;
			line-height: 1.1;
			margin-bottom: 6px;
			display: block;
		}

		p {
			margin: 0;
			color: rgba(218, 226, 253, 0.42);
			font-size: 11px;
			font-weight: 800;
			letter-spacing: 0.16em;
			text-transform: uppercase;
		}
	}

	.stat-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 18px;
	}

	.stat-icon {
		border-radius: 14px;
		color: #ffc639;
		background: rgba(255, 198, 57, 0.1);
		display: inline-flex;
		align-items: center;
		justify-content: center;
		width: 42px;
		height: 42px;

		i {
			font-size: 16px;
		}
	}

	.stat-tag {
		color: #91f2c1;
		font-size: 11px;
		font-weight: 700;
	}

	.dashboard-grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 16px;
	}

	.dashboard-card {
		border-radius: 28px;
		padding: 20px;
		min-height: 320px;
	}

	.card-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 10px;
		margin-bottom: 16px;

		h3,
		span,
		em {
			margin: 0;
		}

		h3 {
			color: #f5f7ff;
			font-size: 19px;
			margin-bottom: 4px;
		}

		span {
			color: rgba(218, 226, 253, 0.48);
			font-size: 12px;
		}
	}

	.legend-mark,
	.card-select {
		border-radius: 999px;
		padding: 0 12px;
		color: rgba(218, 226, 253, 0.72);
		font-size: 10px;
		font-weight: 700;
		letter-spacing: 0.12em;
		text-transform: uppercase;
		background: rgba(255, 255, 255, 0.05);
		display: inline-flex;
		align-items: center;
		gap: 6px;
		height: 30px;
	}

	.legend-mark i {
		border-radius: 999px;
		background: #ffc639;
		width: 8px;
		height: 8px;
	}

	.chart-panel {
		width: 100%;
	}

	.large-chart {
		height: 246px;
	}

	.trend-list {
		padding-top: 8px;
		display: grid;
		gap: 18px;
	}

	.trend-item {
		display: grid;
		gap: 8px;
	}

	.trend-copy {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 10px;

		span,
		em {
			font-style: normal;
			font-size: 12px;
			font-weight: 700;
			text-transform: uppercase;
			letter-spacing: 0.08em;
		}

		span {
			color: #f5f7ff;
		}

		em {
			color: #ffc639;
		}
	}

	.trend-bar {
		border-radius: 999px;
		background: rgba(255, 255, 255, 0.08);
		overflow: hidden;
		width: 100%;
		height: 8px;
	}

	.trend-fill {
		border-radius: inherit;
		background: linear-gradient(90deg, rgba(255, 198, 57, 0.35), #ffc639);
		height: 100%;
	}

	.stack-metrics {
		padding-top: 8px;
		display: flex;
		align-items: flex-end;
		justify-content: center;
		gap: 24px;
		height: 210px;
	}

	.stack-item {
		text-align: center;

		p {
			margin: 10px 0 0;
			color: rgba(218, 226, 253, 0.48);
			font-size: 10px;
			font-weight: 700;
			letter-spacing: 0.08em;
			text-transform: uppercase;
		}
	}

	.stack-bar {
		border-radius: 16px 16px 8px 8px;
		background: rgba(255, 255, 255, 0.04);
		display: flex;
		flex-direction: column-reverse;
		justify-content: flex-start;
		overflow: hidden;
		width: 56px;
		height: 176px;
	}

	.stack-part {
		width: 100%;
	}

	.stack-part.clicks {
		background: rgba(255, 198, 57, 0.92);
	}

	.stack-part.likes {
		background: rgba(255, 198, 57, 0.56);
	}

	.stack-part.favorites {
		background: rgba(218, 226, 253, 0.34);
	}

	.engagement-card {
		border-radius: 30px;
		padding: 24px;
		background:
			radial-gradient(circle at left center, rgba(255, 198, 57, 0.12), transparent 28%),
			linear-gradient(135deg, rgba(34, 42, 61, 0.82), rgba(17, 27, 48, 0.88));
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 18px;
		grid-column: 1 / -1;
	}

	.engagement-copy {
		max-width: 380px;

		h3,
		span {
			margin: 0;
		}

		h3 {
			color: #f7f8fd;
			font-size: 28px;
			margin-bottom: 8px;
		}

		span {
			color: rgba(218, 226, 253, 0.56);
			font-size: 13px;
			line-height: 1.65;
			display: block;
		}
	}

	.engagement-legend {
		display: grid;
		gap: 12px;
		margin-top: 20px;
	}

	.legend-row {
		display: flex;
		align-items: center;
		gap: 10px;
		color: rgba(218, 226, 253, 0.8);
		font-size: 11px;
		font-weight: 700;
		letter-spacing: 0.08em;
		text-transform: uppercase;
	}

	.legend-row i {
		border-radius: 999px;
		width: 10px;
		height: 10px;
	}

	.legend-row .gold {
		background: #ffc639;
	}

	.legend-row .light {
		background: rgba(218, 226, 253, 0.46);
	}

	.legend-row .amber {
		background: #e1aa12;
	}

	.engagement-chart {
		position: relative;
		display: flex;
		align-items: center;
		justify-content: center;
		width: 240px;
		height: 240px;
	}

	.donut-chart {
		width: 240px;
		height: 240px;
	}

	.engagement-center {
		position: absolute;
		text-align: center;

		strong,
		p {
			margin: 0;
		}

		strong {
			color: #f7f8fd;
			font-size: 30px;
			display: block;
		}

		p {
			color: rgba(218, 226, 253, 0.46);
			font-size: 11px;
			font-weight: 700;
			letter-spacing: 0.12em;
			text-transform: uppercase;
		}
	}

	.empty-card {
		border-radius: 24px;
		padding: 36px 20px;
		text-align: center;

		i {
			color: #ffc639;
			font-size: 28px;
			margin-bottom: 10px;
			display: inline-block;
		}

		p {
			margin: 0;
			color: rgba(218, 226, 253, 0.72);
			font-size: 13px;
		}
	}

	@media (max-width: 1280px) {
		.stats-grid,
		.dashboard-grid {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}

		.engagement-card {
			flex-direction: column;
			align-items: flex-start;
		}
	}
</style>
