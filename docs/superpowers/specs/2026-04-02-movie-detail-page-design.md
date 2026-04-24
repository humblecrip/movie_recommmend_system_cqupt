# 电影详情页重构设计

日期：2026-04-02

## 1. 背景

当前前台电影详情页位于 [detail.vue](D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\detail.vue)，功能较全，但布局和视觉仍是旧的表单式详情页，不符合新的电影详情原型图要求。

目标是将详情页整体重构为接近原型图的电影感页面，同时尽量复用现有前后端能力：

- 原型要求：首屏强调背景大图、左侧海报、右侧标题信息、主操作按钮、Overview、Cast & Crew、Similar Movies
- 用户约束：
  - 采用“首屏严格对齐原型 + 下方保留原有功能区”的方案
  - 演员展示逻辑暂不接后端
  - 如果当前后端已有评论、点赞、收藏、分享等功能，则保留；否则隐藏
  - `Watch Trailer` 复用为滚动/展开到电影详情正文
  - `Add to My List` 复用现有收藏逻辑
  - `Similar Movies` 使用“同类型电影，排除当前电影，再按热度/评分排序”

## 2. 现状与约束

### 2.1 当前前端结构

- 电影详情页路由： [router.js](D:\123123-main\src\main\resources\front\front\src\router\router.js)
- 当前详情页组件： [detail.vue](D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\detail.vue)
- 当前电影列表页组件： [list.vue](D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\list.vue)

### 2.2 当前后端可用字段

电影详情接口 [DianyingxinxiController.java](D:\123123-main\src\main\java\com\controller\DianyingxinxiController.java) 和实体 [DianyingxinxiEntity.java](D:\123123-main\src\main\java\com\entity\DianyingxinxiEntity.java) 当前已具备：

- `id`
- `dianyingmingcheng`
- `haibao`
- `dianyingleixing`
- `quyu`
- `shangyingshijian`
- `daoyan`
- `zhuyan`
- `juqingjianjie`
- `dianyingxiangqing`
- `thumbsupnum`
- `crazilynum`
- `clicknum`
- `discussnum`
- `totalscore`
- `storeupnum`

### 2.3 当前已存在的功能能力

前端详情页已具备以下可复用逻辑：

- 收藏 / 取消收藏
- 点赞 / 点踩
- 评论提交 / 评论列表 / 评论删除
- 分享到微博 / QQ 空间
- 详情 HTML 正文展示

### 2.4 当前缺失的数据

以下数据后端当前没有稳定字段支持，因此本次只做视觉占位或静态表现：

- 演员头像数据
- 演员姓名结构化数据
- 片长
- 分级
- 预告片 URL
- 专门的“相似电影关系”数据

## 3. 设计目标

### 3.1 主要目标

- 将电影详情页首屏重构为接近原型图的电影感布局
- 保持现有数据结构可用，不强依赖新增后端接口
- 保留已有评论、点赞、收藏、分享功能
- 让全页视觉统一，避免“上半部分是原型页、下半部分是旧后台详情页”的割裂感

### 3.2 非目标

- 本次不新增演员后端模型
- 本次不接真实预告片播放
- 本次不新增电影片长/分级字段
- 本次不做后台管理页同步改版

## 4. 页面结构设计

详情页分为 3 层：

### 4.1 顶部导航层

采用原型风格顶部导航：

- 左侧：品牌 Logo / 站点名称
- 中间：导航项
- 右侧：搜索图标、用户入口

说明：

- 不再以当前的面包屑 + 返回按钮作为主视觉入口
- `centerType` 场景仍保留返回能力，但作为次级入口弱化展示

### 4.2 首屏 Hero 层

首屏为本次重构核心，结构如下：

- 背景：当前电影海报第一张，做模糊铺底和深色遮罩
- 左侧：竖版海报卡
- 右侧：
  - 电影标题
  - 年份 / 时长 / 分级
  - 评分
  - 两个主按钮
  - Overview 简介
  - Cast & Crew 视觉区

规则：

- 年份由 `shangyingshijian` 提取
- 时长、分级本次使用静态文案占位
- Overview 优先使用 `juqingjianjie`
- Cast & Crew 只保留视觉布局，不接演员逻辑

### 4.3 Similar Movies 层

位于首屏下方，作为横向推荐区：

- 使用真实电影数据
- 规则：同 `dianyingleixing`，排除当前电影
- 排序优先级：
  1. `clicknum desc`
  2. `totalscore desc`
- 点击卡片进入对应详情页

### 4.4 扩展内容层

保留原有功能，但整体视觉升级为同一套电影风：

- 电影详情正文
- 收藏
- 点赞 / 点踩
- 评论区
- 分享区

不再保留当前首屏中的这些旧结构：

- 面包屑主视觉区
- 双层海报轮播
- 旧式 tabs 主外观
- 旧详情字段列表布局

## 5. 数据与交互映射

### 5.1 首屏字段映射

- 背景大图：`haibao` 第一张
- 左侧海报：`haibao` 第一张
- 标题：`dianyingmingcheng`
- 年份：`shangyingshijian`
- 评分：`totalscore`
- Overview：`juqingjianjie`

### 5.2 主按钮映射

`Watch Trailer`

- 不做真实视频
- 点击后滚动到“电影详情正文”区

`Add to My List`

- 复用现有收藏 / 取消收藏逻辑
- 已收藏与未收藏状态需要在按钮视觉上明确区分

### 5.3 Similar Movies 映射

通过现有电影列表接口查询：

- 条件：`dianyingleixing = 当前类型`
- 排除：`id != 当前电影`
- 排序：点击量优先，评分次之

如果结果不足，允许降级为：

- 先用同类型已有结果
- 若数量不足，则用热门电影补足，但优先展示同类型

### 5.4 扩展区映射

- 详情正文：`dianyingxiangqing`
- 点赞 / 点踩：沿用当前详情页逻辑
- 收藏：沿用当前详情页逻辑
- 评论：沿用当前详情页逻辑
- 分享：沿用当前详情页逻辑

## 6. 保留与隐藏规则

### 6.1 保留

- 收藏
- 点赞 / 点踩
- 评论
- 分享
- 详情 HTML 内容

### 6.2 保留但重排

- 所有现有交互模块下沉到首屏之后
- 统一换成深色电影风视觉

### 6.3 隐藏或弱化

- 首屏旧面包屑主视觉
- 旧式字段行列表
- 旧式轮播主结构
- 演员真实数据逻辑

## 7. 视觉规则

- 整体基调：深色、电影感、强对比、高层级信息展示
- 首屏信息层级：
  1. 标题
  2. 年份 / 时长 / 分级
  3. 评分
  4. 主按钮
  5. Overview
  6. Cast & Crew
- `Add to My List` 使用高辨识度描边/高亮样式映射收藏状态
- 下方扩展区必须与首屏使用同一视觉语言，不允许回退到白底后台感布局

## 8. 响应式要求

- 桌面端优先按原型布局实现
- 平板端允许 Hero 区改为上下布局
- 手机端需保证：
  - 海报与标题仍保留主次层级
  - 按钮可点击
  - Similar Movies 可横向或双列收缩
  - 评论区与详情正文不出现横向溢出

## 9. 测试与验收标准

### 9.1 视觉验收

- 首屏布局和视觉重心与原型接近
- 允许电影内容不同，不要求图片完全一致
- 间距、层级、按钮位置和主要结构需与原型同方向收敛

### 9.2 功能验收

- 进入详情页可正常拉取电影详情
- `Watch Trailer` 可滚动到详情正文区
- `Add to My List` 可正确收藏 / 取消收藏
- `Similar Movies` 可按同类型进入详情页
- 评论、点赞、收藏、分享在后端已有能力时继续可用

### 9.3 兼容验收

- 桌面端首屏不塌陷
- 移动端无明显布局破坏
- 不引入新的路由死链

## 10. 实施建议

建议直接在现有 [detail.vue](D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\detail.vue) 基础上重构，而不是新建一套并行详情页，原因如下：

- 可最大程度复用当前评论、收藏、点赞、分享逻辑
- 路由无需额外迁移
- 后端接口无需新增
- 风险更集中，回归路径更清晰

后续实现阶段建议：

1. 先完成详情页首屏 Hero 重构
2. 再接 Similar Movies 数据
3. 最后统一重排下方功能区视觉
