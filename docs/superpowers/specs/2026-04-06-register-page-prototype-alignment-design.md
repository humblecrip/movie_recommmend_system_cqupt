# Register Page Prototype Alignment Design

**Date:** 2026-04-06

## Goal

将前台注册页重构为与 `D:\123123-main\demo\user_register\code.html` 的布局和视觉语言严格对齐，同时保留当前 `yonghu` 注册表单的现有字段、校验与上传逻辑。

## Confirmed Constraints

- 顶部导航整体删除，不保留视觉占位。
- 保留当前注册字段与校验，不新增后端能力。
- 保留头像上传能力，继续复用现有上传组件与接口。
- 保留“已有账号，直接登录”入口。
- 页脚可保留，但页脚链接仅做视觉占位，不新增真实跳转。
- 仅重构注册页，不顺带修改登录页与其他前台页面。

## Existing Behavior To Preserve

- 通过 `this.$route.query.role` 决定当前注册表所属表名。
- 当前主要服务于 `yonghu` 表注册。
- 继续使用 `file-upload` 组件上传头像，接口为 `file/upload`。
- 继续保留字段：
  - `yonghuzhanghao`
  - `mima`
  - `mima2`
  - `yonghuxingming`
  - `touxiang`
  - `xingbie`
  - `lianxidianhua`
  - `shenfenzheng`
- 继续保留校验：
  - 必填校验
  - 两次密码一致校验
  - 手机号校验
  - 身份证校验
- 注册成功后继续跳转 `/login`。

## Target UI Structure

### 1. Atmosphere Layer

- 整页深色影院背景
- 背景图 + 模糊 + 深色渐变遮罩
- 无顶部导航留白，页面主内容直接进入视觉中心

### 2. Center Register Card

- 居中玻璃拟态注册卡片
- 对齐原型中的主标题、副标题、卡片宽度与视觉气质
- 卡片内表单采用更干净的两列栅格结构

### 3. Avatar Upload Area

- 卡片顶部居中头像上传区
- 使用圆形上传容器对齐原型
- 内部仍复用当前 `file-upload` 组件，不自建上传逻辑

### 4. Form Mapping

- 用户账号：单独整行
- 密码 / 确认密码：两列并排
- 用户姓名：单独整行
- 性别 / 联系电话：两列并排
- 身份证：单独整行
- 头像：位于表单顶部居中区域

### 5. CTA and Secondary Action

- 主按钮为“注册”
- 次级入口为“已有账号，直接登录”
- 视觉靠近原型，但继续复用现有路由逻辑

### 6. Footer

- 底部轻量页脚保留，作为视觉收尾
- 页脚链接仅为占位文案

## Non-Goals

- 不新增顶部导航
- 不新增注册字段
- 不新增真实页脚链接
- 不修改 `file-upload` 组件实现
- 不引入新的角色注册模型

## Implementation Strategy

- 直接重构 `src/main/resources/front/front/src/pages/register/register.vue`
- 保留当前注册脚本和表单字段结构
- 主要重写模板和样式
- 通过结构测试锁定原型关键布局与业务字段并存

## Risks

- 现有注册页模板严重依赖老式表单结构，重写时需要避免误删 `file-upload` 和字段映射逻辑。
- `file-upload` 组件视觉默认样式与原型差异较大，需要通过外层容器和局部样式做包裹，而不是改组件本体。
- 当前注册页仅明显支持 `yonghu`，重构时不能误导成通用多表注册页。

## Verification

- 新增注册页结构测试，锁定：
  - 无顶部导航占位
  - 有影院背景层
  - 有玻璃注册卡片
  - 保留所有现有核心字段绑定
  - 保留头像上传组件入口
  - 保留注册按钮与登录入口
- 前端构建通过。
