# 智能在线考试系统 — 项目总文档

> 本文档是整个项目的**权威总览**：定位、架构、前后端设计、业务流程、启动配置、约定与边界。
> 细节深挖请沿文末「文档索引」跳转到对应专项文档。

---

## 一、项目概述

### 1.1 项目定位

一套**前后端分离**的智能在线考试系统，面向**行测（行政职业能力测验）**等标准化机考场景。

- 试卷**仅支持单项选择题**（数据模型预留了题型字段，业务上 AI 生成、Excel 导入、组卷均只产出单选题，与行测全单选的实际情况一致）；
- 「智能」体现在两处：**AI 生成题目**（调用 DeepSeek 大模型按知识点出题）与**智能组卷**（按分类/难度/数量规则随机抽题成卷）；
- 判分为**标准答案自动匹配**，零延迟、零成本，无需人工阅卷。

### 1.2 核心能力

| 域 | 能力 |
| :--- | :--- |
| 内容管理 | 轮播图（图片上传）、公告（富文本、发布/草稿）、题目分类（树形）、题库（条件检索、Excel 批量导入、AI 生成） |
| 组卷发布 | 手动选题组卷、规则式智能抽题组卷、草稿→发布→结束状态流转 |
| 在线考试 | 开始考试（下发无答案题目）、前端倒计时、答题卡、交卷自动判分 |
| 成绩体系 | 我的成绩（逐题明细 + 解析）、成绩排行榜、管理员查看全部考生记录 |
| 运营支撑 | 后台数据概览（总量统计 + 难度/分类分布图表） |

### 1.3 角色与端

| 角色 | role | 端 | 能做什么 |
| :--- | :--- | :--- | :--- |
| 管理员 | 0 | 管理后台 `/admin/*` | 全部内容管理、组卷发布、查看/删除所有考试记录、数据概览 |
| 学生 | 1 | 学生前台 | 浏览首页/公告/题库、参加考试、查看本人成绩、查看排行榜 |

---

## 二、系统架构

### 2.1 总体架构

```
┌──────────────────────────  浏览器  ──────────────────────────┐
│  Vue 3 SPA（frontend-exam，Vite 构建）                        │
│  ├─ StudentLayout：首页/公告/题库/考试/答题/成绩/排行          │
│  ├─ AdminLayout：概览/轮播/公告/分类/题目/导入/试卷/记录        │
│  ├─ Pinia：user(token持久化) / exam(考试态) / app              │
│  └─ Axios：请求带 Bearer token，响应统一解包 Result            │
└───────────────┬──────────────────────────────────────────────┘
                │ HTTP /api/v1/**（开发期经 Vite 代理转发，无跨域）
┌───────────────▼──────────────────────────────────────────────┐
│  Spring Boot 4.1.1（backend-exam，端口 8080）                  │
│  ├─ JwtInterceptor：token 校验 → 注入 userId/role              │
│  │   └─ @RequireAdmin 注解：管理端接口校验 role=0              │
│  ├─ Controller × 9：User/Banner/Notice/Category/Question/     │
│  │                  Exam/Record/File/Stats                     │
│  ├─ Service：组卷抽题 / 自动判分 / Excel(EasyExcel) /          │
│  │           AI 出题(RestClient → DeepSeek)                    │
│  ├─ MyBatis（纯注解 + 动态 SQL）                               │
│  └─ 静态映射 /upload/** → 本地磁盘（轮播图图片）                │
└───────────────┬──────────────────────────┬───────────────────┘
                │ JDBC                     │ HTTPS
        ┌───────▼───────┐          ┌───────▼────────┐
        │ MySQL 8.0     │          │ DeepSeek API   │
        │ exam_system   │          │ (OpenAI 兼容)   │
        │ 7 张表        │          │ 仅 AI 出题使用  │
        └───────────────┘          └────────────────┘
```

### 2.2 请求链路（约定）

1. 所有接口统一前缀 `/api/v1`，统一返回 `Result<T>`：`{code, msg, data}`；分页统一 `PageResult<T>`：`{total, list}`；
2. 除 6 个公开接口（登录、前台轮播/公告/热门题/模板下载）外均需 `Authorization: Bearer <token>`；
3. 鉴权失败返回 **HTTP 401/403**（拦截器直接写状态码），业务失败返回 **HTTP 200 + body code≠200**（全局异常处理器）——前端拦截器对两种形态都做了处理；
4. 错误码：200 成功 / 401 未登录或 token 失效 / 403 无权限 / 404 不存在 / 500 服务器错误。

---

## 三、技术选型

### 3.1 后端（backend-exam）

| 分类 | 技术 | 选型说明 |
| :--- | :--- | :--- |
| 语言/框架 | Java 21、Spring Boot 4.1.1、Spring MVC | LTS 版本，虚拟线程时代的主流企业级组合 |
| 持久层 | MyBatis 4.1.0（纯注解 + 动态 SQL） | 轻量，SQL 可控，无 XML 负担 |
| 数据库 | MySQL 8.0 | 通用关系型；JSON 数据以 TEXT 列存储（见 4.4） |
| 鉴权 | JWT（jjwt 0.12.x）+ BCrypt | 无状态鉴权，密码不可逆加密 |
| Excel | EasyExcel 3.3.4 | 题目模板生成 / 预览 / 批量导入 |
| AI | DeepSeek（RestClient 直调 OpenAI 兼容协议） | 无 SDK 依赖，`response_format=json_object` 保证可解析 |
| 接口文档 | Knife4j 4.5.0（springdoc-openapi） | `/doc.html` 在线调试 |
| 构建 | Maven（自带 mvnw 包装器） | 免装 Maven，仅需 JAVA_HOME |

### 3.2 前端（frontend-exam）

| 分类 | 技术 | 选型说明 |
| :--- | :--- | :--- |
| 框架 | Vue 3.5（Composition API + `<script setup>`） | 现行主流 |
| UI | Element Plus 2.x（全量引入 + 全局图标注册） | 中后台生态最成熟 |
| 路由 | Vue Router 4（history 模式 + 全局角色守卫） | 双端路由隔离 |
| 状态 | Pinia 3（user/app/exam 三 store） | 官方推荐，组合式写法 |
| HTTP | Axios（统一实例 + 双拦截器） | 自动带 token、统一错误提示、自动解包 data |
| 构建 | Vite 7 | 秒级冷启动；dev 代理 `/api`、`/upload` 免跨域 |

---

## 四、后端设计详述

### 4.1 分层结构

```
src/main/java/com/example/demo/
├── DemoApplication.java      # 启动类（@MapperScan）
├── common/                   # Result / PageResult / BizException / 全局异常处理 / @RequireAdmin
├── config/                   # JwtInterceptor、WebConfig(CORS+静态映射)、Knife4j、BeanConfig、DataInitializer
├── controller/               # 9 个控制器（薄层，仅参数绑定与转发）
├── service/                  # 业务核心（组卷、判分、Excel、AI）
├── mapper/                   # MyBatis 纯注解接口
├── entity/ dto/ vo/          # 表实体 / 入参 / 出参（三层对象分离）
└── util/                     # JwtUtil、JsonUtil、ExcelUtil
src/main/resources/
├── application.yml           # 端口/数据源/JWT/上传目录/LLM/文档开关
└── db/schema.sql             # 建表脚本（幂等，启动自动执行）
```

### 4.2 鉴权与授权

- **登录**：校验 BCrypt 密码 → 签发 JWT（subject=userId，claims 含 username/role，有效期 24h）；
- **JwtInterceptor**：拦截 `/api/**`（白名单放行公开接口与 OPTIONS），解析 token 后把 `userId/username/role` 注入 request attribute，Controller 用 `@RequestAttribute` 取用；
- **@RequireAdmin**：方法/类级注解，拦截器发现标注且 `role≠0` 时直接返回 403；
- **数据归属**：考生端记录详情/删除在 Service 层校验 `userId` 本人；管理员走独立的 `/record/admin/*` 接口，无归属校验。

### 4.3 核心算法

**智能组卷（规则抽题，不调大模型）**
1. 入参 `{name, duration, rules:[{categoryId, count, difficulty?}]}`；
2. 每条规则按「分类 + 难度 + 数量」随机抽题；
3. 指定难度抽不足时**自动放宽难度补齐**，仍不足按实际数量成卷；
4. 题目 ID 存为 JSON 数组，试卷落库为**草稿**状态，发布后才可开考。

**自动判分（交卷即出分）**
1. 入参 `{recordId, answers:{"题目ID":"答案字母"}}`；
2. 每题分值 = 总分 ÷ 题目数；逐题与标准答案比对（字母排序后集合相等即正确——该机制同时兼容单选与多选答案形态）；
3. 汇总 `score/correctCount/wrongCount`，记录置为「已交卷」，**不可重复提交**。

**AI 生成题目（DeepSeek）**
1. 入参 `{knowledgePoint, count=5, difficulty=1, categoryId?}`；
2. System Prompt 约束「专业出题专家 + 严格返回 JSON」，User Prompt 注入知识点/数量/难度，`response_format=json_object` + 代码块围栏剥离双保险；
3. 解析为 `List<QuestionVO>` 返回，**不落库**——前端展示供勾选，确认后逐条调 `POST /question` 保存；
4. 未配置 `llm.api-key` 时快速失败并给出明确提示，不影响其他功能。

**Excel 批量导入（EasyExcel）**
- 模板表头固定：`题干 | 选项A | 选项B | 选项C | 选项D | 答案 | 解析 | 难度 | 分类`；
- 选项只填文字，字母前缀由后端拼接；答案填单个字母（单选）；分类填名称，**不存在自动创建**；
- `preview` 接口解析回显不落库，`import` 接口批量入库并返回成功条数。

### 4.4 数据库设计（7 张表）

| 表 | 说明 | 关键字段 |
| :--- | :--- | :--- |
| `sys_user` | 用户 | username(唯一)、password(BCrypt)、role(0管理/1学生)、status |
| `banner` | 轮播图 | title、image_url、link_url、sort_order(越大越前)、status |
| `notice` | 公告 | title、content、type(1通知/2警告/3活动)、status(0草稿/1发布) |
| `category` | 分类 | name、parent_id(0=顶级，支持树形)、sort_order |
| `question` | 题目 | category_id、stem、options(JSON数组)、answer、analysis、difficulty(1-3)、view_count、type |
| `exam` | 试卷 | name、total_score、duration(分钟)、question_ids(JSON数组)、status(0草稿/1发布/2结束) |
| `exam_record` | 考试记录 | user_id、exam_id、answers(JSON对象)、score、correct/wrong_count、status(0进行中/1已交卷)、start/submit_time |

> **JSON 列约定**：`options`/`answers`/`question_ids` 以 JSON 字符串存于 TEXT 列，由应用层（Jackson）序列化/解析——规避 MySQL JSON 类型的空串与校验问题。**接口出参时 options 解析为数组，入参（新增/编辑题目）时需传 JSON 字符串**，这是前后端对接最易错的点。

### 4.5 初始化与文件

- **零手工建库**：数据源 URL 带 `createDatabaseIfNotExist=true`；启动即执行 `schema.sql`（`CREATE TABLE IF NOT EXISTS`，幂等）；`DataInitializer`（CommandLineRunner）在空库时灌入种子数据：**2 个账号 + 4 个行测分类（言语理解/数量关系/判断推理/资料分析）+ 12 道样例题**；
- **文件上传**：`POST /file/upload` 存至本地磁盘（`file.upload-dir`），文件名 MD5 化防冲突，返回相对路径 `/upload/xxx.png`；`WebConfig` 将 `/upload/**` 映射到该磁盘目录，浏览器可直接访问。

---

## 五、前端设计详述

### 5.1 工程结构

```
frontend-exam/src/
├── api/            # 9 个接口模块 + request.js（axios 实例与拦截器）
├── components/     # UploadImage（拖拽上传）/ RichText（轻量富文本）/ AnswerCard（答题卡）
├── layout/         # StudentLayout（顶部导航）/ AdminLayout（侧边栏+面包屑，可折叠）
├── router/         # 路由表 + 全局角色守卫
├── store/          # Pinia：user / app / exam
├── utils/          # constants（数据字典枚举）/ format（时间、倒计时、截断）
└── views/
    ├── login/      # 登录页
    ├── student/    # 8 页：首页/公告列表/公告详情/题库/考试列表/答题/成绩/排行
    └── admin/      # 8 页：概览/轮播/公告/分类/题目/导入/试卷/记录
```

### 5.2 路由与权限守卫

| 路由 | 页面 | 权限 |
| :--- | :--- | :--- |
| `/login` | 登录 | 公开（已登录访问则按 role 跳首页） |
| `/home` `/notice` `/notice/:id` `/question` `/exam` `/exam/:id/do` `/record` `/rank` | 学生端 8 页 | 登录（role=1） |
| `/admin/dashboard` `/admin/banner` `/admin/notice` `/admin/category` `/admin/question` `/admin/question/import` `/admin/exam` `/admin/record` | 管理端 8 页 | 管理员（role=0） |

守卫策略（`router.beforeEach`）：未登录 → `/login`；学生访问 `/admin/*` → 提示无权限并跳 `/home`；管理员访问学生端页面 → 跳 `/admin/dashboard`（双端互不串门）；登录后按 `role` 决定落点。

### 5.3 状态管理（Pinia）

| Store | 持久化 | 职责 |
| :--- | :--- | :--- |
| `user` | localStorage | token、userInfo(role)、isAdmin；login/logout/fetchInfo |
| `exam` | 内存（非持久化） | 当前考试：recordId、题目、answerMap、timeLeft 倒计时；start/startTimer/submit/reset；**时间归零自动交卷** |
| `app` | 内存 | 后台侧边栏折叠状态 |

### 5.4 HTTP 层设计

- 请求拦截：自动附加 `Authorization: Bearer <token>`；
- 响应拦截（成功分支）：`code===200` 直接返回解包后的 `data`（页面代码零样板）；blob 响应透传（模板下载）；业务失败 ElMessage 提示并 reject；
- 响应拦截（错误分支）：HTTP 401 → 清 token 整页跳登录；403 → 提示无权限；404/500/网络异常 → 分类提示；
- 特例：AI 生成接口单独设 **180s 超时**（DeepSeek 同步调用耗时长），其余默认 30s。

### 5.5 关键页面交互

**考试答题页 `/exam/:id/do`（核心）**
- 进入即调 `POST /exam/{id}/start`，下发无答案题目 + recordId，倒计时启动；
- 布局：顶部粘性栏（试卷名 + 已答进度 + 红色大号倒计时，≤5 分钟闪烁告警）｜中部全题滚动列表（便于检查）｜右侧粘性答题卡（网格题号、已答/未答/当前题三色区分、点击平滑滚动跳题）；
- 交卷：二次确认（未答 N 题提醒）→ 组装 `{recordId, answers}` → 跳转「我的成绩」并**自动弹出本次成绩详情**（逐题对错 + 正确答案 + 解析）；
- 兜底：倒计时归零自动交卷；`beforeunload` 离开提醒；手动交卷与自动交卷有竞态锁防重复提交；误触返回后短时间内重进可恢复内存中的考试态。

**题目管理页 `/admin/question`**
- 筛选：分类下拉树 + 难度 + 题型 + 题干关键词（POST 复合分页）；
- 录入：动态增删选项（2-8 个）、单选答案、分类树选择、难度、解析；提交时前端自动把选项数组序列化为 JSON 字符串；
- AI 生成：对话框输入知识点/数量/难度/分类 → 长 loading → 结果表格勾选 → 批量保存入库。

**试卷管理页 `/admin/exam`**
- 手动组卷：弹窗内嵌可筛选题库（分类/难度/关键词 + 分页），勾选题目实时计数，保存名称/总分/时长成卷；
- 智能组卷：规则式表单（多条「分类+数量+难度」规则增删），提交后返回新试卷 ID；
- 状态操作：草稿→发布、发布→停止，二次确认；详情弹窗展示全部题目**含答案与解析**（仅管理员接口）。

**数据概览页 `/admin/dashboard`**
- 7 个统计卡片（题目/试卷/已发布/用户/学生/记录/分类总数）；
- 难度分布、分类题目数两个条形图（纯 CSS 实现，单色 `#337ecc` 经对比度校验 ≥3:1，直接标注数值，无外部图表库依赖）。

### 5.6 公共组件

| 组件 | 用途 |
| :--- | :--- |
| `UploadImage` | 拖拽/点击上传（前置校验类型与 10MB 限制），v-model 绑定图片相对路径，带预览与移除 |
| `RichText` | 轻量 contenteditable 富文本（加粗/斜体/下划线/清除格式），公告内容以 HTML 存储 |
| `AnswerCard` | 答题卡网格：已答/未答/当前题视觉区分，点击 emit 跳题 |

---

## 六、接口总览

> 完整字段级说明见 [`backend-exam/Request.md`](backend-exam/Request.md)，在线调试 `http://localhost:8080/doc.html`。

| 分组 | 接口数 | 代表接口 |
| :--- | :--- | :--- |
| 用户 `/user` | 3 | login / info / logout |
| 轮播图 `/banner` | 7 | enabled(公开)、list、CRUD、status 切换 |
| 公告 `/notice` | 8 | enabled/latest(公开)、list、CRUD、status 切换 |
| 分类 `/category` | 5 | list(含题目数)、tree、CRUD |
| 题目 `/question` | 9 | page(复合分页)、hot(公开)、CRUD、excel preview/import、template(公开)、**ai/generate** |
| 试卷考试 `/exam` | 9 | list、detail(含答案)、create、**ai/compose**、status、**{id}/start**、**{id}/submit** |
| 记录排行 `/record` | 7 | list、detail、ranking、**admin/list·detail·delete** |
| 文件 `/file` | 1 | upload |
| 统计 `/stats` | 1 | dashboard |

公开接口（无需 token）共 6 个：`/user/login`、`/banner/enabled`、`/notice/enabled`、`/notice/latest`、`/question/hot`、`/question/template`。

---

## 七、核心业务流程

### 7.1 登录与鉴权

```
登录页 → POST /user/login → 存 token+userInfo(localStorage/Pinia)
      → role=0 进 /admin/dashboard ｜ role=1 进 /home
后续请求自动带 Bearer token → 401 时清 token 整页跳回登录
```

### 7.2 管理员建卷发布

```
备题（三选一或混用）：
  手动录入 POST /question ｜ Excel 导入 template→preview→import ｜ AI 生成 ai/generate→勾选→POST /question
组卷（二选一）：
  手动：question/page 勾选 → POST /exam {name,totalScore,duration,questionIds}
  智能：POST /exam/ai/compose {name,duration,rules[]} → 返回新试卷 ID
发布：PATCH /exam/{id}/status?status=1   （草稿→发布，学生端可见可考）
```

### 7.3 学生考试闭环

```
GET /exam/list（仅展示 status=1）
→ POST /exam/{id}/start（拿 recordId + 无答案题目，前端倒计时 duration 分钟）
→ 作答（answerMap 内存态；时间归零自动交卷）
→ POST /exam/{id}/submit {recordId, answers}（后端即时判分）
→ 跳转 /record?detail=recordId → GET /record/{id}（得分 + 逐题对错 + 解析）
→ 排行榜 GET /record/ranking
```

### 7.4 管理员督考

```
GET /stats/dashboard（总量 + 分布概览）
GET /record/admin/list?userId=&examId=（按学生/试卷筛全部记录）
GET /record/admin/{id}（任意记录逐题明细）｜ DELETE /record/admin/{id}
```

---

## 八、快速启动与配置

### 8.1 环境要求

| 依赖 | 版本 | 说明 |
| :--- | :--- | :--- |
| JDK | 21 | 需正确设置 `JAVA_HOME` |
| MySQL | 8.0 | 服务需先启动；库表自动创建，无需手工建库 |
| Node.js | ≥ 20（实测 24） | 前端构建 |
| DeepSeek API Key | 可选 | 仅「AI 生成题目」需要 |

### 8.2 启动步骤

```bash
# 1) 后端（先）
cd backend-exam
#    首次请设置环境变量（真实密码/密钥不要写进 application.yml 提交）：
#    MYSQL_PASSWORD ← 你的 MySQL 密码（必需）
#    LLM_API_KEY    ← DeepSeek Key（可选，AI 出题用）
mvnw.cmd spring-boot:run       # Linux/macOS: ./mvnw spring-boot:run
#    → http://localhost:8080 ，文档 /doc.html

# 2) 前端（后）
cd frontend-exam
npm install                    # 首次
npm run dev
#    → 终端输出的地址（默认 http://localhost:5173）
```

登录：管理员 `admin/admin123`，学生 `student/123456`。

### 8.3 关键配置项（backend application.yml）

| 配置 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `server.port` | 8080 | 与前端代理 target 保持一致 |
| `spring.datasource.password` | `${MYSQL_PASSWORD:root}` | **真实密码用环境变量 `MYSQL_PASSWORD` 注入**；URL 带 `createDatabaseIfNotExist=true` 自动建库 |
| `spring.sql.init` | always + db/schema.sql | 幂等建表 |
| `jwt.secret` / `jwt.expiration` | `${JWT_SECRET:开发占位值}` / 24h | **生产必须通过 `JWT_SECRET` 环境变量替换** |
| `file.upload-dir` | `${FILE_UPLOAD_DIR:./upload}` | 图片落盘目录（相对后端运行目录，即 backend-exam/upload） |
| `llm.api-key` | `${LLM_API_KEY:占位符}` | **通过 `LLM_API_KEY` 环境变量注入真实 Key 才可用 AI 出题** |
| `knife4j.enable` | true | 生产可关闭 |

### 8.4 前端开发代理（vite.config.js）

`/api` 与 `/upload` → `http://localhost:8080`：开发期请求走相对路径，**无跨域问题**；上传图片的相对路径（`/upload/xxx.png`）直接可显示，无需拼接后端域名。生产部署时可由 Nginx 承担同样的反代职责。

### 8.5 生产构建

```bash
cd frontend-exam && npm run build   # 产物 dist/，静态托管 + /api、/upload 反代到后端即可
```

> 注：前端使用 history 路由，静态服务器需配置 fallback 到 `index.html`。

---

## 九、项目约定（前后端共同遵守）

| 约定 | 内容 |
| :--- | :--- |
| 题型范围 | **业务上仅单选题**（行测场景）；`type` 字段保留 0单选/1多选 枚举，判分逻辑兼容多选形态，但 AI 生成、Excel 模板、组卷业务均只产出单选 |
| 数据字典 | role 0管理/1学生；难度 1简单/2中等/3困难；试卷 0草稿/1发布/2结束；记录 0进行中/1已交卷；公告类型 1通知/2警告/3活动 |
| 时间格式 | 所有时间字段为字符串 `yyyy-MM-dd'T'HH:mm:ss`，展示层自行格式化 |
| options 双形态 | **出参**是数组 `["A. …","B. …"]`；**入参**（新增/编辑题目）是 JSON 字符串——前端已在提交前自动序列化 |
| 答案形态 | 单选为单个字母 `A`；交卷 answers 为 `{"题目ID":"答案"}` 对象 |
| 分页双风格 | banner/notice/exam/record-admin 列表用 query `?page=&size=`；`question/page` 用 POST body |
| 上传限制 | 图片 ≤10MB（前端校验 + 后端 multipart 限制一致） |

---

## 十、已知边界与后续规划

### 10.1 已知边界（当前版本）

1. **AI 出题依赖外部 Key**：`llm.api-key` 为占位符时该功能报错（有明确提示），其余功能不受影响；AI 为同步调用，耗时 10~60s，前端已设 180s 超时；
2. **考试无续考机制**：后端无「恢复进行中记录」接口，答题中刷新页面丢失进度，需重新开考（产生新记录）；
3. **排行榜为全量接口**：无 examId 筛选参数、无 startTime，前端按试卷名本地过滤，「用时」不展示；
4. **Excel 导入仅支持单选**：模板答案列为单字母；
5. **安全项待生产化**：数据库密码、JWT secret 目前为开发默认值，部署前必须替换；管理端暂无操作审计。

### 10.2 可选的后续方向

- 后端：续考接口（按 userId+examId 查进行中记录）、ranking 加 examId 参数与用时字段、用户管理页（禁用/启用学生账号）、试卷「已结束」自动流转（到期自动停止）；
- 前端：Element Plus 按需引入（当前主 chunk 1.2MB/gzip 402KB）、答题状态 sessionStorage 缓解刷新丢失、深色模式；
- 工程：根目录 docker-compose 一键起 MySQL+后端+前端、CI 构建。

---

## 十一、文档索引

| 文档 | 定位 | 适合谁读 |
| :--- | :--- | :--- |
| [`README.md`](README.md)（本目录） | 快速上手入口 | 第一次接触项目的人 |
| [`backend-exam/Request.md`](backend-exam/Request.md) | **接口文档**（字段级权威） | 前端对接、联调、测试 |
| [`backend-exam/Target.md`](backend-exam/Target.md) | 前端交付文档（页面→接口映射、流程、交付清单、实现边界） | 验收、回顾交付范围 |
| [`backend-exam/README.md`](backend-exam/README.md) | 后端项目说明（结构、算法、配置、FAQ） | 后端维护者 |
| [`frontend-exam/request.md`](frontend-exam/request.md) | 前端原始需求 + 实现状态与差异对照 | 需求追溯 |
| [`frontend-exam/README.md`](frontend-exam/README.md) | 前端运行手册（启动、结构、对接要点） | 前端维护者 |
| `http://localhost:8080/doc.html` | Knife4j 在线接口文档 | 联调时实时调试 |
