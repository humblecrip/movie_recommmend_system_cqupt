# 电影列表页原型严格对齐设计

## 1. 背景

当前前台电影信息列表页路由为 `/index/dianyingxinxi`，对应组件为：

- `D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\list.vue`

本页在上一轮工作中被改成了接近电影详情原型的结构，但这与当前真实需求不一致。  
本次需求要求将该列表页与：

- `D:\123123-main\demo\movie_list\code.html`
- `D:\123123-main\demo\movie_list\DESIGN.md`

进行严格对齐，重点对齐：

1. 页面布局
2. 动效表现
3. 筛选逻辑

同时要求：

- 尽可能复用后端已有 API
- 不为原型对齐额外新增后端功能
- `Quick View` 行为直接进入现有详情页

---

## 2. 目标

将电影列表页重构为以 `demo/movie_list/code.html` 为主结构来源的电影探索页，保留现有数据查询与详情跳转能力，并将主筛选收敛为原型中的：

- 类型
- 年份
- 评分
- 排序

页面最终应在视觉、层级和交互节奏上明显接近原型，而不是保留当前“详情页化”的列表布局。

---

## 3. 当前可复用能力

### 3.1 前端已有页面与路由

- 列表页：`/index/dianyingxinxi`
- 详情页：`/index/dianyingxinxiDetail?id=...`
- 当前列表组件：`D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\list.vue`
- 当前详情组件：`D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\detail.vue`
- 可复用 helper：`D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\detail-helpers.js`

### 3.2 后端已有 API

可直接复用的接口：

- `GET /dianyingxinxi/list`
- `GET /dianyingxinxi/page`
- `GET /dianyingxinxi/autoSort`
- `GET /dianyingleixing/list`
- `GET /dianyingxinxi/detail/{id}`

可复用的收藏接口：

- `GET /storeup/list`
- `POST /storeup/add`
- `POST /storeup/delete`
- `POST /dianyingxinxi/update`

### 3.3 当前可复用字段

电影表当前可稳定使用的字段：

- `id`
- `dianyingmingcheng`
- `haibao`
- `dianyingleixing`
- `quyu`
- `shangyingshijian`
- `daoyan`
- `zhuyan`
- `juqingjianjie`
- `totalscore`
- `storeupnum`
- `clicknum`
- `thumbsupnum`
- `addtime`

---

## 4. 用户确认过的关键约束

用户已明确确认以下设计边界：

1. 主筛选采用原型风格，只保留：
   - 类型
   - 年份
   - 评分
   - 排序
2. 动效要整体尽量对齐，包括：
   - 卡片 hover 动效
   - 筛选条交互动效
   - 顶部栏、分页和整体过渡反馈
3. 顶部导航按“能复用的就接上”的原则处理：
   - 可复用的搜索行为应接上
   - 无真实后端支撑的仅保留视觉与 hover
4. `Quick View` 不做弹层，直接进入现有详情页

---

## 5. 非目标

本次不做以下内容：

- 不新增后端接口
- 不新增数据库字段
- 不实现真正的 Watchlist / Notification / Profile 业务功能
- 不将导演/主演搜索继续保留为主筛选条的一部分
- 不引入新的动画库
- 不创建新的并行列表页路由

---

## 6. 原型对齐策略

### 6.1 页面结构以 `movie_list` 原型为主

页面整体改为 5 个主区块：

1. 顶部导航栏
2. 面包屑与标题区
3. 一行胶囊筛选条
4. 纯海报卡片网格
5. 底部分页

这意味着当前 `list.vue` 中偏向详情页的结构应整体移除，包括但不限于：

- 大型 Hero 首屏
- Overview 区块
- Similar Movies 区块
- 右侧热门信息侧栏
- 当前“业务区”分栏布局

### 6.2 视觉层级对齐

遵循 `DESIGN.md` 的核心规则：

- 背景为深蓝黑影院色系
- 主强调色为金色 `#ffc639`
- 通过色块层级和留白定义区块，而不是靠硬边框
- 顶部导航使用半透明玻璃效果
- 卡片 hover 时表现出浮起感和聚焦感

### 6.3 组件映射

原型结构与业务映射如下：

- 顶部搜索框：
  - 复用现有电影名称查询
- `Genre`
  - 映射到 `dianyingleixing`
- `Release`
  - 映射到 `shangyingshijian` 的年份，缺失时回退 `addtime`
- `Rating`
  - 映射到 `totalscore` 阈值筛选
- `Sort By`
  - 映射到现有 `sort/order`
- `Quick View`
  - 进入 `/index/dianyingxinxiDetail?id=...`

---

## 7. 筛选逻辑设计

## 7.0 接口调用契约

本次实现将明确分为两类请求：

### 基础列表请求

- 普通前台场景：`GET /dianyingxinxi/list`
- 个人中心场景：`GET /dianyingxinxi/page`

选择规则保持与当前页面一致：

- `centerType = false` -> `/list`
- `centerType = true` -> `/page`

### 请求参数约定

后端请求统一传递：

- `page`
- `limit`
- `sort`
- `order`
- `dianyingmingcheng`（仅顶部搜索时传，值固定为 `%关键字%`）
- `dianyingleixing`（仅类型筛选非 `All` 时传）

### 默认请求规模

为保证“年份/评分前端过滤 + 前端分页”结果一致，本次页面在触发查询时统一请求较大的基础结果集：

- `page = 1`
- `limit = 1000`

原因：

- 现有接口未提供年份/评分专用过滤参数
- 如果仍使用后端分页后再前端过滤，会导致总数错误、页码错误、某页过滤后为空

因此，本次分页口径写死为：

- 后端负责返回基础结果集
- 前端负责年份/评分过滤后的最终分页展示

该策略适用于本项目当前电影数据量较小的场景，是为满足“复用现有 API 且不新增接口”所做的明确取舍。

### 前端分页状态

前端页面状态中保留：

- `rawList`：后端返回的基础列表
- `filteredList`：应用年份/评分后的结果
- `page`
- `pageSize`

最终页面渲染与分页总数都基于 `filteredList`，不再直接依赖后端返回的 `total/pageSize/totalPage` 作为最终展示口径。

## 7.1 类型筛选

### 数据来源

- `GET /dianyingleixing/list`

### 查询方式

直接透传到现有列表接口参数：

- `dianyingleixing`

### 行为要求

- 默认选中 `All`
- 切换后回到第一页
- 可与其他筛选叠加

---

## 7.2 年份筛选

### 字段来源

优先：

- `shangyingshijian`

兜底：

- `addtime`

### 实现方式

年份下拉选项从 `rawList` 中抽取唯一年份值，按倒序展示。

筛选执行时：

- 固定在前端对 `rawList` 做年份过滤

### 年份解析规则

实现必须使用统一年份提取逻辑：

1. 优先读取 `shangyingshijian`
2. 若其为空、非法或无法提取年份，则回退 `addtime`
3. 若字符串长度至少 4 且前四位为数字，则取前四位
4. 否则记为 `未知年份`

适用示例：

- `2025-04-02` -> `2025`
- `2025-04-02 12:01:22` -> `2025`
- `2025/04/02` -> `2025`
- 空值 / 非法值 -> `未知年份`

年份下拉中：

- 不展示 `未知年份`
- 若用户未选择年份，则不过滤
- 若用户选择某年份，则只保留解析结果等于该年份的数据

### 取舍

这是为满足“尽可能复用已有 API”做出的折中方案。  
本次不新增后端年份筛选接口。

---

## 7.3 评分筛选

### 字段来源

- `totalscore`

### 选项设计

- `Any`
- `9.0+`
- `8.0+`
- `7.0+`

### 实现方式

评分筛选在前端做阈值过滤：

- `9.0+` -> `totalscore >= 9`
- `8.0+` -> `totalscore >= 8`
- `7.0+` -> `totalscore >= 7`

过滤对象明确为：

- 已经过后端基础查询
- 已应用类型与关键词条件
- 但尚未前端分页的 `rawList`

本次不要求后端支持最小评分参数。

---

## 7.4 排序筛选

排序完全复用现有接口参数：

- `sort`
- `order`

映射关系建议如下：

- `Popularity` -> `clicknum desc`
- `Newest First` -> `addtime desc`
- `Highest Rated` -> `totalscore desc`
- `A-Z` -> `dianyingmingcheng asc`

---

## 7.5 顶部搜索

顶部导航内保留一个全局搜索输入框。

行为：

- 仅复用电影名称检索
- 查询参数固定映射为 `dianyingmingcheng`
- 请求值固定为 `%keyword%`
- 输入后重新请求列表接口
- 不再保留导演/主演 3 个并列输入框

理由：

- 更贴近原型
- 避免筛选区噪音
- 仍然复用了已有查询能力

---

## 8. 列表卡片与动效设计

## 8.1 卡片布局

每张卡片改为原型的纯海报列表卡形式：

- 上方为 2:3 海报
- 下方为标题
- 再下方为年份与评分

不再展示：

- 导演
- 主演
- 地区
- 多个业务统计 badge
- 长段剧情简介

这些字段不是本次原型网格的主视觉重点，继续保留只会稀释原型布局。

## 8.2 卡片动效

卡片 hover 效果要求接近原型：

- 整卡轻微放大
- 整卡轻微上移
- 阴影增强
- 海报底部遮罩渐显
- `Quick View` 按钮由底部滑入

过渡时长控制在：

- `250ms ~ 300ms`

移动端不依赖 hover 展示核心信息。

## 8.3 Quick View 行为

虽然视觉文案保留 `Quick View`，但真实行为不做弹层。

点击后：

- 跳转到现有详情页 `/index/dianyingxinxiDetail?id=...`

这样既满足原型视觉，又不新增复杂预览逻辑。

---

## 9. 顶部栏与分页设计

## 9.1 顶部栏

按原型对齐：

- sticky 顶栏
- 半透明深色背景
- 强 blur 玻璃感
- 当前激活菜单为 `Discover`

功能处理：

- 顶部搜索框接真实查询
- 其他入口仅保留视觉和 hover 反馈

## 9.2 分页

分页改为原型风格：

- 当前页金色高亮
- 其他页为轻量 hover 反馈
- `Previous / Next` 风格贴近原型

分页在本次实现中分为两层：

- **UI 风格**：对齐原型分页外观
- **数据分页**：以 `filteredList` 的前端分页切片结果为准

因此本页最终展示时：

- 不再依赖后端返回的 `total/pageSize/totalPage` 作为分页展示依据
- 页面上的总数、页码、当前页数据均以前端过滤后的 `filteredList` 为准

---

## 10. 数据流设计

页面维护统一筛选状态：

- `keyword`
- `genre`
- `year`
- `rating`
- `sortBy`
- `page`

执行流程：

1. 页面初始化拉取分类与列表
2. 顶部搜索、类型、排序变化时重新请求后端基础列表（固定大 `limit`）
3. 年份与评分在前端对 `rawList` 做过滤，产出 `filteredList`
4. 页面分页基于 `filteredList` 做前端切片渲染
5. 点击卡片或 `Quick View` 进入详情页

这样可以保证：

- 年份筛选后的总数正确
- 评分筛选后的页码正确
- 年份选项不只来自某一页
- 不会出现“分页后过滤导致当前页为空但实际有数据”的问题

排序口径也明确如下：

- 排序切换必须重新请求后端基础列表
- 不在前端对 `rawList` 自行重排

原因：

- 现有接口已支持 `sort/order`
- 这样可以最大化复用后端现有能力
- 也避免前端排序与后端字段语义不一致

---

## 11. 文件范围

本次实现建议只涉及以下文件：

- 修改：`D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\list.vue`
- 复用：`D:\123123-main\src\main\resources\front\front\src\pages\dianyingxinxi\detail-helpers.js`
- 新增测试：`D:\123123-main\src\main\resources\front\front\scripts\list-structure.test.js`

必要时可补充一个列表辅助脚本，但默认不新增新 helper 文件。

---

## 12. 验证标准

### 12.1 布局验证

- 页面主结构与 `demo/movie_list/code.html` 明显一致
- 不再保留详情页式大 Hero / Overview / Similar Movies 结构
- 列表区改成纯海报网格

### 12.2 交互验证

- 顶部搜索可触发电影名称检索
- 类型筛选可用
- 年份筛选可用
- 评分筛选可用
- 排序切换可用
- `Quick View` 能进入详情页

### 12.3 动效验证

- 卡片 hover 有放大、上浮、遮罩和按钮滑入
- 顶部导航 hover 有颜色反馈
- 筛选项切换态明显
- 分页 hover 与当前态接近原型
- 移动端不依赖 hover 展示核心信息，点击卡片本体仍可进入详情页

### 12.4 构建验证

- 前端 `npm run build` 通过
- 不出现新的模板编译错误

### 12.5 状态与异常验证

- 加载中时页面有明确占位或 loading 状态
- 无结果时展示空状态而不是空白页
- 接口失败时有错误提示或兜底状态

---

## 13. 风险与处理

### 风险 1：年份和评分缺少后端专用筛选参数

处理：

- 前端做轻量过滤
- 不新增接口

### 风险 2：现有列表页已被改成错误方向结构

处理：

- 直接按原型重写页面主骨架
- 不在现有详情化布局上继续叠补丁

### 风险 3：功能过多会冲淡原型布局

处理：

- 坚持只保留用户确认的主筛选
- 其他不在原型核心路径上的展示字段直接下沉或删除

---

## 14. 最终决策

本次列表页重构采用以下策略：

1. 以 `demo/movie_list/code.html` 作为页面主结构来源
2. 主筛选收敛为 `类型 / 年份 / 评分 / 排序`
3. 顶部搜索仅复用电影名称检索
4. `Quick View` 直接进入现有详情页
5. 优先复用现有后端接口，不新增后端能力
6. 重点实现原型卡片网格、顶部栏、筛选条和分页的视觉与动效对齐
