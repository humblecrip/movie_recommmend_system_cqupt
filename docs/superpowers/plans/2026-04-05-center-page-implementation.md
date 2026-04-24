# Center Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `/index/center` 重构为与 `demo/user_detail/code.html` 风格一致的影院感个人中心，同时保留现有编辑、上传、改密、收藏和跳转逻辑。

**Architecture:** 以 `center.vue` 为唯一主界面文件，保留原有数据与方法，新增一个极小的辅助模块承载可测试的导航与头像解析逻辑。模板改为侧栏导航 + 主舞台面板，样式集中在单文件内完成。

**Tech Stack:** Vue 2、Element UI、SCSS、现有 `file-upload` 组件、Node 脚本级回归测试

---

### Task 1: 提取可测试的页面辅助逻辑

**Files:**
- Create: `src/main/resources/front/front/src/pages/center/center-helpers.js`
- Create: `src/main/resources/front/front/scripts/center-helpers.test.js`

- [ ] **Step 1: 写失败测试**
- [ ] **Step 2: 运行测试确认失败**
- [ ] **Step 3: 实现最小 helper**
- [ ] **Step 4: 运行测试确认通过**

### Task 2: 重构个人中心模板结构

**Files:**
- Modify: `src/main/resources/front/front/src/pages/center/center.vue`

- [ ] **Step 1: 保留现有数据与方法，补充侧栏导航所需状态**
- [ ] **Step 2: 将旧 tabs 结构改为原型风格的侧栏 + 主内容布局**
- [ ] **Step 3: 把个人资料表单映射成原型对应的双列卡片**
- [ ] **Step 4: 把修改密码面板映射进同一主舞台**

### Task 3: 对齐视觉样式和动效

**Files:**
- Modify: `src/main/resources/front/front/src/pages/center/center.vue`

- [ ] **Step 1: 实现深色影院背景、玻璃面板、金色 CTA**
- [ ] **Step 2: 实现头像区、按钮、输入框、导航激活态**
- [ ] **Step 3: 补充进入动画、hover 微动效和响应式布局**

### Task 4: 回归验证

**Files:**
- Test: `src/main/resources/front/front/scripts/center-helpers.test.js`

- [ ] **Step 1: 运行 helper 测试**
- [ ] **Step 2: 运行 `npm run build`**
- [ ] **Step 3: 记录残留 warning，仅接受历史 warning**
