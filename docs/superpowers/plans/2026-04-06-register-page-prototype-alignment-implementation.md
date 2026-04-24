# Register Page Prototype Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将前台注册页重构为 `demo/user_register` 原型风格，同时保留当前 `yonghu` 注册表单、头像上传和现有字段校验逻辑。

**Architecture:** 保留 `register.vue` 中现有字段定义、表单提交流程和上传逻辑，通过 TDD 先新增结构测试，再重写模板与样式。页面仍保持单文件组件，避免为本次视觉改造引入不必要的组件拆分。

**Tech Stack:** Vue 2、Element UI、SCSS、现有 `file-upload` 组件、Node 结构测试脚本

---

## File Structure

- Modify: `src/main/resources/front/front/src/pages/register/register.vue`
  - 负责注册页模板、样式、字段绑定、注册提交、上传回填
- Create: `src/main/resources/front/front/scripts/register-structure.test.js`
  - 锁定原型关键结构与现有业务字段入口
- Reference: `demo/user_register/code.html`
  - 提供目标布局与视觉层次

### Task 1: 锁定注册页原型结构

**Files:**
- Create: `src/main/resources/front/front/scripts/register-structure.test.js`
- Modify: `src/main/resources/front/front/src/pages/register/register.vue`

- [ ] **Step 1: Write the failing test**

编写结构测试，至少断言：
- 页面存在新的影院注册页根容器
- 页面存在背景氛围层
- 页面不存在顶部导航容器
- 页面存在玻璃注册卡片
- 页面存在原型主标题
- 页面保留注册按钮与登录入口

- [ ] **Step 2: Run test to verify it fails**

Run: `node src/main/resources/front/front/scripts/register-structure.test.js`
Expected: FAIL，提示目标结构尚不存在

- [ ] **Step 3: Write minimal implementation**

在 `register.vue` 中重写页面结构，建立：
- 背景层
- 中央注册卡片
- 标题区
- CTA 区
- 页脚区

- [ ] **Step 4: Run test to verify it passes**

Run: `node src/main/resources/front/front/scripts/register-structure.test.js`
Expected: PASS

### Task 2: 将现有注册字段映射到原型布局

**Files:**
- Modify: `src/main/resources/front/front/src/pages/register/register.vue`
- Test: `src/main/resources/front/front/scripts/register-structure.test.js`

- [ ] **Step 1: Write the failing test**

补充断言，确保：
- `registerForm.yonghuzhanghao`
- `registerForm.mima`
- `registerForm.mima2`
- `registerForm.yonghuxingming`
- `registerForm.xingbie`
- `registerForm.lianxidianhua`
- `registerForm.shenfenzheng`
- `file-upload`
- `submitForm('registerForm')`

这些入口在新页面结构中仍然存在。

- [ ] **Step 2: Run test to verify it fails**

Run: `node src/main/resources/front/front/scripts/register-structure.test.js`
Expected: FAIL，提示字段映射未完成

- [ ] **Step 3: Write minimal implementation**

将现有字段映射为原型两列栅格布局：
- 头像上传区置顶
- 账号整行
- 密码与确认密码并排
- 姓名整行
- 性别与电话并排
- 身份证整行

- [ ] **Step 4: Run test to verify it passes**

Run: `node src/main/resources/front/front/scripts/register-structure.test.js`
Expected: PASS

### Task 3: 完成样式对齐与回归验证

**Files:**
- Modify: `src/main/resources/front/front/src/pages/register/register.vue`
- Test: `src/main/resources/front/front/scripts/register-structure.test.js`

- [ ] **Step 1: Polish styles**

完成：
- 深色影院背景与遮罩
- 玻璃卡片视觉
- 原型式标题区
- 金色主按钮
- 上传区圆形容器
- 两列表单响应式布局
- 底部页脚

- [ ] **Step 2: Run focused tests**

Run:
- `node src/main/resources/front/front/scripts/register-structure.test.js`

Expected: PASS

- [ ] **Step 3: Run regression tests**

Run:
- `node src/main/resources/front/front/scripts/login-structure.test.js`
- `node src/main/resources/front/front/scripts/front-shell-structure.test.js`

Expected: PASS

- [ ] **Step 4: Run build**

Run:
- `C:\Program Files\PowerShell\7\pwsh.exe -Command "$env:NODE_OPTIONS='--openssl-legacy-provider'; .\node_modules\.bin\vue-cli-service.cmd build"`

Expected: Build complete，允许保留项目既有 warning
