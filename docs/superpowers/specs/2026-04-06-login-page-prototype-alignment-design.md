# Login Page Prototype Alignment Design

**Date:** 2026-04-06

## Goal

将前台登录页重构为与 `D:\123123-main\demo\user_login\code.html` 的布局和视觉语言严格对齐，同时保留当前单登录表单的现有业务能力。

## Confirmed Constraints

- 保留当前单登录表单流程，不拆成多入口登录。
- 保留账号、密码、登录、注册入口。
- 保留“仅当前台可登录角色大于 1 时显示角色选择”的业务逻辑。
- 尽量复用现有接口与缓存逻辑，不新增后端能力。
- 只重构登录页，不顺带改注册页与其他前台页面。

## Existing Behavior To Preserve

- 通过 `menu.list()` 动态装载前台可登录角色。
- 提交时继续使用 `<tableName>/login` 接口。
- 登录成功后继续写入：
  - `frontToken`
  - `UserTableName`
  - `username`
  - `frontSessionTable`
  - `frontRole`
  - `keyPath`
- 登录成功后继续执行头像同步缓存清理与事件派发。
- 登录成功后继续跳转到 `redirect` 或首页。

## Target UI Structure

### 1. Atmosphere Layer

- 整页深色影院背景
- 背景大图 + 模糊 + 深色渐变遮罩
- 保留沉浸式电影感，而不是当前生成式表单底图

### 2. Top Navigation

- 左上品牌文案 `Aether Cinema`
- 右上轻量帮助图标占位
- 纯视觉对齐，不增加实际帮助功能

### 3. Center Login Card

- 居中玻璃拟态卡片
- 标题、副标题对齐原型文案层级
- 表单项使用统一深色容器和金色 focus 态

### 4. Form Mapping

- 账号输入映射到当前 `loginForm.username`
- 密码输入映射到当前 `loginForm.password`
- 密码显隐功能保留，嵌入输入框右侧
- 角色选择仅在 `roles.length > 1` 时显示，并复用现有逻辑
- 登录按钮维持单主 CTA

### 5. Footer

- 底部版权和若干轻量链接文案
- 仅作为视觉结构，不引入新业务页面

## Non-Goals

- 不新增“记住我”真实逻辑
- 不新增“忘记密码”流程
- 不新增社交登录
- 不调整登录接口与权限规则
- 不修改注册页实现

## Implementation Strategy

- 直接重构 `src/main/resources/front/front/src/pages/login/login.vue`
- 保留脚本中的登录与缓存逻辑
- 主要重写模板和样式
- 必要时仅抽取少量常量，不做过度组件拆分

## Risks

- 当前 `login.vue` 模板和样式耦合较重，重写时要避免误删现有业务逻辑。
- 原型是纯静态 HTML，真实页面需要容纳角色下拉和多注册链接，必须以“不破坏原型主结构”为前提嵌入。
- 登录页重构后需要确认响应式布局仍然可用，尤其是窄屏下卡片宽度和页脚位置。

## Verification

- 新增登录页结构测试，锁定原型关键结构与业务入口共存。
- 构建通过。
- 手工确认：
  - 单角色时不显示角色下拉
  - 多角色时显示角色下拉
  - 登录按钮仍走原有接口
  - 注册入口仍可跳转
