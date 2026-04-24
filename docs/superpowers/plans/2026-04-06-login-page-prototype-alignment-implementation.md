# Login Page Prototype Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将前台登录页重构为 `demo/user_login` 原型风格，同时保留现有单登录表单逻辑和角色/注册业务入口。

**Architecture:** 保留 `login.vue` 中现有脚本方法与状态字段，使用 TDD 先补结构测试，再重写登录页模板和样式。页面仍为单文件组件，避免为这次视觉改造引入额外复杂度。

**Tech Stack:** Vue 2、Element UI、SCSS、现有前台登录接口、Node 结构测试脚本

---

### Task 1: 锁定登录页目标结构

**Files:**
- Create: `src/main/resources/front/front/scripts/login-structure.test.js`
- Modify: `src/main/resources/front/front/src/pages/login/login.vue`

- [ ] **Step 1: Write the failing test**

编写结构测试，至少断言：
- 页面含影院氛围背景容器
- 页面含品牌栏 `Aether Cinema`
- 页面含玻璃登录卡片
- 页面保留账号、密码、登录按钮
- 页面保留角色选择逻辑入口
- 页面保留注册链接渲染

- [ ] **Step 2: Run test to verify it fails**

Run: `node src/main/resources/front/front/scripts/login-structure.test.js`
Expected: FAIL，提示目标结构尚不存在

- [ ] **Step 3: Write minimal implementation**

在 `login.vue` 中重写模板结构，建立：
- 顶部品牌栏
- 中央登录卡片
- 表单区
- 底部页脚

- [ ] **Step 4: Run test to verify it passes**

Run: `node src/main/resources/front/front/scripts/login-structure.test.js`
Expected: PASS

### Task 2: 迁移现有业务元素到原型布局

**Files:**
- Modify: `src/main/resources/front/front/src/pages/login/login.vue`
- Test: `src/main/resources/front/front/scripts/login-structure.test.js`

- [ ] **Step 1: Write the failing test**

补充断言，确保：
- 多角色时显示角色选择区
- 注册链接区仍由 `roles` 动态渲染
- 密码显隐入口仍存在
- 登录方法 `submitForm('loginForm')` 保留

- [ ] **Step 2: Run test to verify it fails**

Run: `node src/main/resources/front/front/scripts/login-structure.test.js`
Expected: FAIL，提示业务入口未完整映射

- [ ] **Step 3: Write minimal implementation**

将当前业务字段映射到新原型表单：
- `loginForm.username`
- `loginForm.password`
- `loginForm.tableName`
- `roles`
- `showPassword`

- [ ] **Step 4: Run test to verify it passes**

Run: `node src/main/resources/front/front/scripts/login-structure.test.js`
Expected: PASS

### Task 3: 完成视觉对齐与回归验证

**Files:**
- Modify: `src/main/resources/front/front/src/pages/login/login.vue`
- Test: `src/main/resources/front/front/scripts/login-structure.test.js`

- [ ] **Step 1: Polish styles**

完成：
- 深色背景图层
- 渐变遮罩
- 玻璃卡片
- 金色主按钮
- 输入框 focus 态
- 页脚与响应式布局

- [ ] **Step 2: Run focused tests**

Run:
- `node src/main/resources/front/front/scripts/login-structure.test.js`

Expected: PASS

- [ ] **Step 3: Run regression tests**

Run:
- `node src/main/resources/front/front/scripts/front-shell-structure.test.js`
- `node src/main/resources/front/front/scripts/storeup-panel-structure.test.js`

Expected: PASS

- [ ] **Step 4: Run build**

Run:
- `C:\Program Files\PowerShell\7\pwsh.exe -Command "$env:NODE_OPTIONS='--openssl-legacy-provider'; .\node_modules\.bin\vue-cli-service.cmd build"`

Expected: Build complete，允许保留项目既有 warning
