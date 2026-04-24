# Storeup Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `/index/storeup` 重构为与 `demo/user_favorite` 对齐的影院风收藏页，并保留现有搜索、分页、跳详情逻辑。

**Architecture:** 保持 `storeup/list.vue` 作为主页面文件，新增一个极小 helper 模块承载图片地址解析、排序配置与空态文案。交互继续走现有 `storeup/list`、`storeup/delete` 与详情跳转链路。

**Tech Stack:** Vue 2、Element UI、SCSS、Node 脚本级回归测试

---

### Task 1: 提取可测试的收藏页 helper

**Files:**
- Create: `src/main/resources/front/front/src/pages/storeup/storeup-helpers.js`
- Create: `src/main/resources/front/front/scripts/storeup-helpers.test.js`

- [ ] **Step 1: 写失败测试**
- [ ] **Step 2: 运行测试确认失败**
- [ ] **Step 3: 实现最小 helper**
- [ ] **Step 4: 运行测试确认通过**

### Task 2: 重构收藏页模板结构

**Files:**
- Modify: `src/main/resources/front/front/src/pages/storeup/list.vue`

- [ ] **Step 1: 接入侧栏、标题区、工具区和卡片网格结构**
- [ ] **Step 2: 保留并重接搜索、分页、详情跳转**
- [ ] **Step 3: 接入真实可用的取消收藏**
- [ ] **Step 4: 接入有限排序与空态**

### Task 3: 对齐视觉样式和动效

**Files:**
- Modify: `src/main/resources/front/front/src/pages/storeup/list.vue`

- [ ] **Step 1: 实现深色影院背景与固定侧栏**
- [ ] **Step 2: 实现卡片 hover、按钮、搜索框、分页样式**
- [ ] **Step 3: 补充响应式布局**

### Task 4: 回归验证

**Files:**
- Test: `src/main/resources/front/front/scripts/storeup-helpers.test.js`

- [ ] **Step 1: 运行 helper 测试**
- [ ] **Step 2: 运行 `npm run build`**
- [ ] **Step 3: 检查仅剩历史 warning**
