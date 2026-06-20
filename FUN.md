# 街道办网格化便民服务管理系统

## 一、管理员功能

管理员拥有系统全权限操作，负责整体运维、数据管理、业务审核与统筹调度，可使用系统所有模块功能。

1. **数据总览模块**：查看事件总数、已办结事件、待处理事件、处理中事件、服务申请、居民、房屋等各类数据统计。Dashboard 页面集成 ECharts 柱状图（事件处置进度）和饼图（事件分类分布），事件办结率以 Hero 大数字展示，辅以 8 个指标卡片（网格数、居民数、事件数、服务事项数、待处理、处理中、已办结、通知公告数）形成完整治理看板。
2. **网格管理模块**：完成网格信息的查询（支持关键词搜索）、新增、编辑和删除。集成高德地图 JS API 2.0，在新增/编辑弹窗中通过 AMap.MouseTool 实现多边形围栏在线圈选（单击落点→双击完成），围栏坐标以 [[lng,lat],...] JSON 格式持久化存储至 fencePoints 字段。支持重新圈选和清除围栏。网格列表的 GET 接口对所有认证用户开放以支持事件上报时的下拉选择。
3. **居民信息管理模块**：对居民信息进行新增、编辑、删除和查询（支持按姓名和地址搜索）。管理居民姓名、性别、身份证号、联系电话、所属网格、居住地址、居民类型（常住人口/流动人口/独居老人/特殊人群）和备注信息。通过 gridId 和 houseId 外键关联网格和房屋。
4. **房屋信息管理模块**：完成房屋信息的查询、新增、编辑和删除。管理房屋编号、详细地址、楼栋、单元、门牌号、所属网格、房屋类型（商品房/经济适用房/公租房/自建房）、使用状态（自住/出租/空置）、户主姓名、居住人数以及经纬度坐标。房屋经纬度为地图可视化中的蓝色标记提供数据。
5. **便民服务模块**：发布、编辑、删除各类服务事项，包含事项名称、服务分类、办理部门、事项描述、所需材料、办理流程、办理时限和启用/停用状态。查看居民提交的服务申请记录，对申请进行审批流转：待受理→办理中→已办结/已驳回。状态通过下拉选择框切换，标签颜色自动同步（橙/蓝/绿/红）。
6. **事件上报与处置模块**：查看全部事件列表（支持按状态筛选），进行事件受理与状态流转（待处理→处理中→已办结），填写处理结果。可编辑和删除事件。事件上报支持选择所属网格（下拉加载已有网格）、选择分类、填写地址和经纬度、上传现场图片（JPG/PNG/WebP，≤5MB，UUID 重命名存储）。
7. **通知公告模块**：发布、编辑、删除社区公告，支持分类管理（社区通知/便民服务/政策宣传/社区活动）和发布状态控制（已发布/草稿）。公告列表按发布时间倒序排列。
8. **用户管理模块**：对系统用户进行查询、新增、编辑、删除。管理用户名、姓名、角色（管理员/普通用户）、联系电话、账号状态（启用/禁用）。系统内置 admin 账号不可删除。支持在"操作日志"标签页查看系统操作日志（按时间倒序，最多 100 条）。
9. **个人中心模块**：查看并编辑个人资料（姓名、电话），修改登录密码（需验证旧密码，新密码≥6位）。查看系统全局统计数据（事件总数、已办结数、待处理数、服务申请数）。
10. **地图可视化模块**：在高德地图上展示网格围栏（AMap.Polygon 绿色半透明多边形+名称标签）、事件位置（红色圆形标记）和房屋位置（蓝色圆形标记）三层叠加。点击任意地图元素查看详情。无围栏的网格回退显示点标记。顶部统计栏实时显示各类标记数量。

## 二、普通用户功能

普通用户主要面向辖区居民，侧重线上办事、问题上报、信息查看以及个人账号管理，仅开放居民使用类功能。

1. **便民服务模块**：浏览系统内公示的各项便民服务事项卡片（含分类、办理时限、所需材料），点击"在线申办"从已有事项下拉列表中选择，填写联系电话和申请详情后提交。申请提交后自动生成编号（SA-年份+序号），初始状态为"待受理"。仅可查看自己提交的申请记录及办理进度。
2. **事件上报与处置模块**：线上上报社区事件，填写事件标题、选择分类（城市管理/公共安全/环境卫生/矛盾纠纷/民生服务）、从已有网格下拉列表中选择所属网格、填写事件地址和详细描述、可上传现场图片（JPG/PNG/WebP）。仅可查看自己上报的事件记录及处理进度。
3. **通知公告模块**：浏览查看社区发布的公告列表（按时间倒序），查看公告详情。
4. **个人中心模块**：查看并编辑个人资料（姓名、电话），修改登录密码（需验证旧密码）。查看个人统计数据（我的事件数、处理中事件数、我的申请数、待处理申请数）。
5. **地图可视化模块**：普通用户无法访问地图可视化页面（该功能仅管理员可用），但事件上报时可查看地图定位信息。

## 三、技术栈

### 1. 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 17 (LTS) | Java record 简化模型定义 |
| 构建工具 | Maven 3.9+ | 依赖管理与构建 |
| 核心框架 | Spring Boot 3.5.0 | 自动配置、内嵌 Tomcat、端口 8080 |
| ORM | MyBatis-Plus 3.5.12 | 简化 SQLite 数据访问 |
| 数据库 | SQLite 3.49.1.0 | 嵌入式文件型数据库，HikariCP 连接池(maximum-pool-size=1) |
| 认证 | JJWT 0.12.6 + BCrypt | HMAC-SHA256 签发 JWT（24h 有效），BCrypt 不可逆密码加密 |
| 序列化 | Jackson (ObjectMapper) | JSON 序列化业务数据存储于 app_state 表 |
| 文件上传 | Spring Multipart | 图片上传至 uploads/ 目录，UUID 重命名 |

### 2. 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| 运行环境 | Node.js 20.19、npm 10+ | - |
| 构建工具 | Vite 7.x | HMR 热更新，开发端口 5173，代理 /api → localhost:8080 |
| 核心框架 | Vue 3.5 | Composition API + `<script setup>` 语法糖 |
| UI 组件库 | Element Plus 2.x | el-table / el-dialog / el-form / el-tag / ElMessage |
| 数据可视化 | ECharts 5.x | 柱状图（事件状态）、饼图（事件分类） |
| HTTP 客户端 | Axios 1.x | 请求拦截器自动附加 JWT Token，响应拦截器统一错误处理 |
| 路由 | Vue Router 4.x | 9 条路由 + beforeEach 导航守卫 + sessionStorage Token 校验 |
| 地图 | 高德地图 JS API 2.0 | MouseTool 多边形绘制 / Polygon 围栏 / Marker 标记 / Icon 自定义图标 |

> ⚠️ 注意：FUN.md 原始设计文档中提到了 Pinia 状态管理库，但在实际开发中发现本系统状态管理需求简单（仅 Token 存储于 sessionStorage + API 数据按页面独立加载），未引入额外状态管理库，减少了打包体积和依赖复杂度。

## 四、数据库

本系统采用 **SQLite** 文件型数据库，无需独立部署数据库服务，部署便捷、轻量化，数据库文件路径为 `backend/grid-service.db`。

### 1. 存储架构

实际实现采用 **JSON 序列化存储**方案，与原始设计文档中的 10 张关系表方案有所不同。业务数据以 Jackson JSON 字符串形式存储在 `app_state` 单表中：

| 字段 | 类型 | 说明 |
|------|------|------|
| state_key | TEXT (PK) | 数据类型标识（grids / residents / houses / serviceItems / events / applications / users / notices / operationLogs） |
| state_value | TEXT | Jackson 序列化的 List<T> JSON 字符串 |

系统启动时 DataStore.init() 从 app_state 表加载 JSON 反序列化为内存 ArrayList，所有业务操作直接操作内存列表，通过 persist() 方法将全量数据序列化写回数据库。

### 2. 数据模型（Java Record）

#### SystemUser 用户模型
对应原始设计的 user 表，实际字段：
`id, username, displayName, password(BCrypt密文), role(管理员/普通用户), phone, status(启用/禁用), createTime, updateTime`

#### Grid 网格模型
对应原始设计的 grid + grid_fence 表（合并），实际字段：
`id, code, name, community, description, staffName, staffPhone, residentCount, longitude, latitude, fencePoints`

fencePoints 以 `[[lng,lat],[lng,lat],...]` JSON 字符串存储围栏所有拐点坐标，替代原始设计中独立的 grid_fence 表。

#### Resident 居民模型
`id, name, gender, idCard, phone, gridName, gridId, address, houseId, residentType(常住人口/流动人口/独居老人/特殊人群), remark`

#### House 房屋模型
`id, houseCode, address, building, unit, roomNumber, gridName, gridId, houseType(商品房/经济适用房/公租房/自建房), usageStatus(自住/出租/空置), ownerName, residentCount, longitude, latitude`

#### ServiceItem 服务事项模型
`id, name, category, department, description, requiredMaterials, process, timeLimit, status(启用/停用)`

#### ServiceApplication 服务申请模型
`id, code, userId, serviceItemId, itemName, applicantName, applicantPhone, ownerUsername, content, attachment, status(待受理/办理中/已办结/已驳回), handleResult, createdAt, updatedAt`

#### Event 事件模型
`id, code, title, category(城市管理/公共安全/环境卫生/矛盾纠纷/民生服务), gridName, reporter, ownerUsername, description, imageUrl, address, longitude, latitude, status(待处理/处理中/已办结), handlerId, handleResult, createdAt, updatedAt`

#### Notice 通知公告模型
`id, title, type(社区通知/便民服务/政策宣传/社区活动), content, publisherId, status(已发布/草稿), publishedAt`

#### OperationLog 操作日志模型
`id, userId, username, operation, module, createTime`

### 3. 数据关联关系

由于采用 JSON 序列化存储（非关系型），数据关联在应用层通过字段匹配和 Stream 过滤实现：

1. ServiceApplication.ownerUsername → SystemUser.username：服务申请归属过滤（普通用户仅见本人申请）
2. Event.ownerUsername → SystemUser.username：事件归属过滤（普通用户仅见本人事件）
3. Resident.gridId → Grid.id / Resident.houseId → House.id：居民与网格、房屋的关联
4. House.gridId → Grid.id：房屋与网格的关联
5. Grid.fencePoints：围栏坐标直接存储于网格模型中，无需独立的 grid_fence 表
6. OperationLog.userId → SystemUser.id：日志归属关联

### 4. 与原始设计的差异说明

| 原始设计（10 表关系型） | 实际实现（JSON 序列化） | 原因 |
|------------------------|----------------------|------|
| 独立 grid_fence 表 | Grid.fencePoints JSON 字段 | 围栏坐标与网格强绑定，合并简化存取 |
| INTEGER 状态字段 | String 中文状态值 | 提高可读性和前后端一致性 |
| 独立 create_time/update_time | 内嵌于 Java record | Jackson 自动序列化时间字段 |
| 数据库外键约束 | 应用层 Stream 过滤 | 无外键约束的 SQLite 下保证灵活性 |
| ip 字段（操作日志） | 暂未实现 | 课程项目简化，通过 username 追溯即可 |

## 五、系统架构

### 前端页面（10 个 Vue SFC）

| 页面 | 路由 | 权限 |
|------|------|------|
| Login.vue | /login | 公开 |
| Dashboard.vue | / | 管理员 |
| GridList.vue | /grids | GET 全员 / 写操作管理员 |
| BaseInfo.vue | /base-info | 管理员 |
| ServiceList.vue | /services | 全员（管理员见全部，普通用户见本人） |
| EventList.vue | /events | 全员（管理员见全部，普通用户见本人） |
| NoticeList.vue | /notices | GET 全员 / 写操作管理员 |
| UserList.vue | /users | 管理员 |
| Profile.vue | /profile | 全员 |
| MapView.vue | /map | 管理员 |

### 后端 Controller（10 个）

`AuthController` / `GridController` / `BaseInfoController` / `ServiceItemController` / `ServiceApplicationController` / `EventController` / `NoticeController` / `DashboardController` / `SystemUserController` / `UploadController`

### 认证流程

```
用户登录 → BCrypt 密码验证 → JwtUtil.generate() 签发 Token
→ 前端存入 sessionStorage（标签页隔离）
→ 后续请求 Axios 拦截器自动附加 Authorization: Bearer <token>
→ AuthInterceptor 验证 Token → 注入 UserView 到 request
→ RoleInterceptor 校验角色权限（区分 GET/非GET 方法）
```

### 安全机制

- 密码：BCrypt 单向哈希加密，不可逆
- 认证：JWT HMAC-SHA256 签名，24h 过期
- 权限：双层拦截器（AuthInterceptor + RoleInterceptor），精确到 HTTP 方法级
- 存储：sessionStorage 标签页隔离，关闭标签页即清除 Token
- 账号保护：admin 账号不可删除
- 数据隔离：普通用户通过 ownerUsername 过滤仅见本人数据

### 与原始设计文档的偏差总结

原始 FUN.md 以"功能规划文档"角色描述系统愿景，实际实现在此基础上做了以下适配：

- 技术栈版本升级：Spring Boot 3.1→3.5、Vite 5→7
- 存储方案简化：10 张关系表→JSON 序列化单表（更适合课程项目快速迭代）
- 未引入 Pinia（状态管理需求简单，无需额外依赖）
- 围栏存储合并：grid_fence 独立表→Grid.fencePoints JSON 字段
- 角色名中文化：admin/user→管理员/普通用户
- 认证升级：Session→JWT Token
- 地图从"预留"到完整集成：围栏圈选+多层标记+总览页面
