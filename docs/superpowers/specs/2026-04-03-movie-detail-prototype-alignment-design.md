# 电影详情页原型严格对齐重设计

日期：2026-04-03

## 1. 目标

将前台电影详情页重构为与 `D:\123123-main\demo\movie_detail\code.html` 的页面结构和视觉层级严格一致的版本，同时尽可能复用当前项目已有后端 API、字段和前端业务逻辑。

本次重构不是“做一个风格接近的电影详情页”，而是以原型 HTML 为主结构来源，对现有 `detail.vue` 进行重新对齐。

## 2. 输入依据

### 2.1 设计原型来源

- 原型 HTML：`D:\123123-main\demo\movie_detail\code.html`
- 设计说明：`D:\123123-main\demo\movie_detail\DESIGN.md`
- 参考截图：`D:\123123-main\demo\movie_detail\screen.png`

### 2.2 当前前后端基础

- 当前详情页组件：`D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\detail.vue`
- 当前 helper：`D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\detail-helpers.js`
- 后端控制器：`D:\123123-main\src\main\java\com\controller\DianyingxinxiController.java`
- 实体字段：`D:\123123-main\src\main\java\com\entity\DianyingxinxiEntity.java`

### 2.3 可复用后端字段

当前详情页可稳定复用的主要字段：

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

### 2.4 可复用前端逻辑

本次必须保留并尽量直接复用的逻辑：

- 收藏 / 取消收藏
- 点赞 / 点踩
- 评论提交 / 评论列表 / 评论删除
- 分享到微博 / QQ 空间
- 详情 HTML 正文展示
- 相似电影 helper 逻辑

## 3. 范围

### 3.1 本次必须完成

1. 详情页主结构严格向原型 HTML 对齐
2. 删除当前实现中不属于原型的新增组件
3. 保留已有业务逻辑并映射到原型结构中
4. 暂时删除演员详情区块
5. Similar Movies 使用真实电影数据
6. Hero 和 Overview 的内容尽量用真实字段填充

### 3.2 本次明确删除

从当前 `detail.vue` 中删除以下偏离原型的新增内容：

- 自定义顶部标题栏 / 顶部导航条
- 海报缩略图轮播 / 次级海报条
- 额外统计卡片
- Cast & Crew 整块
- 不属于原型结构的 Hero 附加模块

### 3.3 本次不做

- 不新增后端接口
- 不新增演员结构化数据
- 不接真实预告片播放
- 不新增片长字段
- 不新增电影分级字段
- 不改后台页面

## 4. 页面结构设计

页面按原型严格收敛为四段。

### 4.1 Hero 区

严格对齐 `code.html` 中 Hero 区的主结构：

- 背景大图
- 左侧单张海报
- 元信息标签行
- 标题
- 两个 CTA 按钮

实现要求：

- 保留背景图与主海报的重叠层次
- 保留原型中的深色渐变覆盖方式
- 不再出现当前实现中的自定义顶部导航和缩略图条
- Hero 中不放演员详情卡片

### 4.2 Overview + 信息卡区

严格对齐原型中 `Overview` 左列与右侧 4 张小卡片布局：

左侧：

- Overview 标题
- 剧情简介正文
- 类型标签

右侧：

- 四张资料信息卡

### 4.3 Similar Movies 区

严格对齐原型中的电影卡片网格结构：

- 海报
- 标题
- 年份
- 评分

不保留当前实现中偏离原型的自定义推荐信息样式。

### 4.4 业务区

原型没有现成的评论 / 点赞 / 分享业务区，因此本次将其保留在 Similar Movies 之后，作为扩展内容区：

- 电影详情正文
- 评论提交与评论列表
- 点赞 / 点踩
- 分享

这部分不要求逐像素照搬原型，但视觉语言必须与原型统一：

- 深色背景
- 无强分割线
- 主要通过背景层级和留白区分区块

## 5. 数据映射设计

### 5.1 Hero 区映射

- 背景图：`haibao` 第一张
- 左侧海报：`haibao` 第一张
- 标题：`dianyingmingcheng`
- 年份：由 `shangyingshijian` 提取年份
- 时长：静态占位
- 分级：静态占位
- 评分：`totalscore`

### 5.2 Overview 区映射

- 简介正文：`juqingjianjie`
- 类型标签：`dianyingleixing`
  - 若是单值则显示单标签
  - 若字符串中包含可分隔符，可拆成多个标签

### 5.3 右侧 4 张信息卡映射

用户已确认采用以下字段组合：

1. 导演：`daoyan`
2. 区域：`quyu`
3. 上映时间：`shangyingshijian`
4. 收藏数：`storeupnum`

选择该组合的原因：

- 全部为当前后端稳定字段
- 信息角色接近原型中的基础资料卡
- 不引入伪字段与额外后端依赖

### 5.4 Similar Movies 映射

继续复用当前 helper 推荐逻辑：

- 优先同类型 `dianyingleixing`
- 排除当前电影
- 按点击量 / 评分排序
- 点击卡片进入对应详情页

展示字段：

- 海报：`haibao` 第一张
- 标题：`dianyingmingcheng`
- 年份：`shangyingshijian`
- 评分：`totalscore`

### 5.5 CTA 映射

`Watch Trailer`

- 不接真实视频
- 点击后滚动到详情正文区

`Add to My List`

- 复用现有收藏 / 取消收藏逻辑
- 状态文案需要和当前收藏状态联动

## 6. 逻辑与交互约束

### 6.1 必须复用当前已有逻辑

以下逻辑优先复用，不重写业务语义：

- `storeup` 收藏逻辑
- 点赞 / 点踩逻辑
- 评论提交与评论列表逻辑
- 分享逻辑
- `buildSimilarMovies` 推荐逻辑

### 6.2 路由与进入方式

- 继续使用当前详情页路由 `/index/dianyingxinxiDetail?id=...`
- `centerType` / `storeupType` 兼容保留
- 从 Similar Movies 点击跳转后，应重新加载当前页数据

### 6.3 删除演员详情

本次明确删除原型中的 `Cast & Crew` 视觉区，不做占位，不做简化版，不做结构化映射。

## 7. 视觉规则

以 `DESIGN.md` 的设计系统为准，重点执行以下规则：

- 背景采用深色电影感底色
- 主强调色使用金色系 CTA
- 不使用明显 1px 分割线组织页面
- 通过背景层级、留白和卡片层级形成区块边界
- 圆角、模糊、叠层关系尽量向原型收敛
- 不再保留当前实现中偏离原型的自定义布局部件

## 8. 验收标准

### 8.1 结构验收

- 页面主结构与 `code.html` 一致
- Hero / Overview / Similar Movies 的顺序一致
- 当前多余的顶部栏、缩略图条、演员区已删除

### 8.2 业务验收

- 详情页可正常打开
- `Watch Trailer` 可滚动到正文
- `Add to My List` 可正常收藏 / 取消收藏
- Similar Movies 点击后可进入目标详情页
- 评论 / 点赞 / 分享逻辑继续可用

### 8.3 数据验收

- Hero 背景和海报使用真实海报字段
- Overview 使用真实简介字段
- 右侧 4 张信息卡使用确认过的真实字段映射
- Similar Movies 使用真实电影数据，不用静态占位卡片

## 9. 风险与限制

以下内容在本轮仍为限制项，需要在交付说明中明确：

- 时长为静态占位，不是后端真实字段
- 分级为静态占位，不是后端真实字段
- 不提供真实预告片播放
- 不展示演员详情区
- 原型中的部分纯展示文案必须被真实字段替换，因此文案内容不保证与 demo 完全相同，但结构必须一致

## 10. 实施原则

- 以 `code.html` 为结构主来源，而不是以当前 `detail.vue` 为主来源微调
- 仅复用当前可复用的业务逻辑和数据获取方式
- 不做额外功能扩展
- 不保留任何与原型结构冲突的新增组件
- 若原型结构与现有业务区冲突，则优先保证原型主展示区严格一致，业务区下沉到后续扩展区

## 11. 结论

本次详情页重构采用：

- **原型结构严格对齐**
- **现有后端与业务逻辑最大化复用**
- **演员详情区删除**
- **右侧信息卡采用：导演 / 区域 / 上映时间 / 收藏数**

该方案范围明确，可直接进入实现计划阶段。