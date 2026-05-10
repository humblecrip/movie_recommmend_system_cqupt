<template>
  <transition name="route-loading-curtain-fade">
    <div
      v-if="visible"
      class="route-loading-curtain"
      aria-live="polite"
      aria-busy="true"
    >
      <div class="curtain-panel curtain-panel-left"></div>
      <div class="curtain-panel curtain-panel-right"></div>
      <div class="curtain-glow"></div>

      <div class="curtain-center">
        <div class="curtain-brand">
          <span class="curtain-brand-main">电影推荐</span>
          <span class="curtain-brand-accent">系统</span>
        </div>
        <div class="curtain-title">{{ message }}</div>
        <div class="curtain-subtitle">下一幕正在入场，请稍候片刻</div>
        <div class="curtain-pulse-track" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'RouteLoadingCurtain',
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    message: {
      type: String,
      default: '正在切换电影页面',
    },
  },
}
</script>

<style scoped>
.route-loading-curtain {
  position: fixed;
  inset: 0;
  z-index: 9999;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at center, rgba(41, 211, 255, 0.14), transparent 30%),
    rgba(4, 10, 22, 0.92);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.curtain-panel {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 52%;
  background:
    linear-gradient(180deg, rgba(19, 31, 57, 0.98), rgba(8, 13, 28, 0.96)),
    repeating-linear-gradient(
      90deg,
      rgba(255, 255, 255, 0.04) 0,
      rgba(255, 255, 255, 0.04) 2px,
      transparent 2px,
      transparent 18px
    );
  box-shadow: inset 0 0 60px rgba(0, 0, 0, 0.38);
  animation: curtainSway 1.2s ease-in-out infinite alternate;
}

.curtain-panel-left {
  left: -4%;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  transform-origin: left center;
}

.curtain-panel-right {
  right: -4%;
  border-left: 1px solid rgba(255, 255, 255, 0.08);
  transform-origin: right center;
  animation-delay: 0.18s;
}

.curtain-glow {
  position: absolute;
  width: 36vw;
  height: 36vw;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(47, 200, 255, 0.28), transparent 66%);
  filter: blur(12px);
  opacity: 0.68;
  animation: glowPulse 1.4s ease-in-out infinite;
}

.curtain-center {
  position: relative;
  z-index: 2;
  width: min(420px, calc(100vw - 40px));
  padding: 40px 32px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(14, 23, 41, 0.9), rgba(8, 13, 25, 0.82));
  box-shadow:
    0 22px 60px rgba(0, 0, 0, 0.36),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
  text-align: center;
}

.curtain-brand {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 16px;
}

.curtain-brand-main {
  color: rgba(246, 247, 251, 0.96);
  font-size: 22px;
  font-weight: 300;
  letter-spacing: 0.26em;
}

.curtain-brand-accent {
  color: #2fc8ff;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-shadow: 0 0 18px rgba(47, 200, 255, 0.5);
}

.curtain-title {
  color: #f6f7fb;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.curtain-subtitle {
  margin-top: 10px;
  color: rgba(218, 226, 253, 0.72);
  font-size: 14px;
  line-height: 1.8;
  letter-spacing: 0.12em;
}

.curtain-pulse-track {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-top: 24px;
}

.curtain-pulse-track span {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(47, 200, 255, 0.85);
  box-shadow: 0 0 18px rgba(47, 200, 255, 0.5);
  animation: dotPulse 0.9s ease-in-out infinite;
}

.curtain-pulse-track span:nth-child(2) {
  animation-delay: 0.12s;
}

.curtain-pulse-track span:nth-child(3) {
  animation-delay: 0.24s;
}

.route-loading-curtain-fade-enter-active,
.route-loading-curtain-fade-leave-active {
  transition: opacity 0.22s ease;
}

.route-loading-curtain-fade-enter,
.route-loading-curtain-fade-leave-to {
  opacity: 0;
}

@keyframes curtainSway {
  from {
    transform: translateX(0) scaleX(1);
  }
  to {
    transform: translateX(0) scaleX(0.985);
  }
}

@keyframes glowPulse {
  0%,
  100% {
    transform: scale(0.94);
    opacity: 0.54;
  }
  50% {
    transform: scale(1.04);
    opacity: 0.8;
  }
}

@keyframes dotPulse {
  0%,
  100% {
    transform: translateY(0) scale(0.86);
    opacity: 0.56;
  }
  50% {
    transform: translateY(-5px) scale(1.12);
    opacity: 1;
  }
}

@media (max-width: 640px) {
  .curtain-center {
    padding: 34px 24px;
    border-radius: 22px;
  }

  .curtain-brand-main {
    font-size: 18px;
  }

  .curtain-brand-accent {
    font-size: 20px;
  }

  .curtain-title {
    font-size: 20px;
  }

  .curtain-subtitle {
    font-size: 13px;
    letter-spacing: 0.08em;
  }
}
</style>
