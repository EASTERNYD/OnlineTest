# 智能在线考试系统 - 前端需求文档 (Vue 3 + Element Plus)

> **技术栈**：Vue 3 (Composition API), Vue Router, Pinia (状态管理), Element Plus, Axios, Vite
> **布局划分**：项目采用双布局模式 —— `AdminLayout` (管理后台) 和 `StudentLayout` (学生考试端)。
>
> **✅ 实现状态（2026-09-08）**：本文档需求已全部实现，交付清单与页面→接口映射以 `../backend-exam/Target.md` 为准，启动方式与已知边界见本目录 `README.md`。与本文档原始描述的差异适配汇总见文末「5. 实现状态与差异说明」。

## 1. 项目结构与路由规划
src/
├── api/ # 接口请求封装 (按模块划分)
│ ├── user.js
│ ├── banner.js
│ ├── notice.js
│ ├── question.js
│ └── exam.js
├── assets/ # 静态资源
├── components/ # 公共组件
│ ├── UploadImage.vue # 图片上传组件 (轮播图专用)
│ ├── RichText.vue # 富文本编辑器 (公告)
│ └── AnswerCard.vue # 答题卡组件 (学生考试页)
├── layout/
│ ├── AdminLayout.vue # 后台：侧边栏 + 顶部导航
│ └── StudentLayout.vue # 前台：顶部导航 + 主体
├── router/
│ └── index.js # 路由配置 (含鉴权守卫)
├── store/ # Pinia 状态
│ ├── user.js # 存储用户信息、token
│ └── app.js # 存储主题、侧边栏折叠
├── views/
│ ├── login/ # 登录页
│ ├── admin/ # 后台页面 (权限控制)
│ │ ├── dashboard/ # 首页 (数据概览)
│ │ ├── banner/ # 轮播管理
│ │ ├── notice/ # 公告管理
│ │ ├── category/ # 分类管理
│ │ ├── question/ # 题目管理 (含手动录入、Excel导入、AI生成)
│ │ ├── exam/ # 试卷管理 (含手动组卷、AI组卷)
│ │ └── records/ # 考试记录管理 (查看所有学生记录)
│ └── student/ # 前台页面
│ ├── home/ # 首页 (展示轮播、公告)
│ ├── exam/list/ # 考试列表 (可参加的试卷)
│ ├── exam/room/ # 考试答题界面 (核心)
│ └── exam/result/ # 考试成绩页 (含排行)

## 2. 核心功能页面与 UI/UX 要求

### 2.1 登录页 (`/login`)
- **元素**：账号输入框、密码输入框、登录按钮。
- **交互**：点击登录调用后端接口，成功后存储 Token 并跳转至对应角色的首页（管理员进后台，学生进前台）。
- **状态**：登录按钮防抖（防止重复提交）。

### 2.2 后台 - 轮播图管理 (`/admin/banner`)
- **列表展示**：表格展示图片缩略图、标题、状态（启用/禁用开关）、操作按钮。
- **新增/编辑**：使用 `Dialog` 弹窗，内含图片上传组件（支持拖拽）、标题输入、链接输入。
- **特殊操作**：状态切换使用 `el-switch` 即时生效。

### 2.3 后台 - 题目管理 (`/admin/question`) —— **开发重点**
- **筛选区域**：分类下拉树、难度下拉框、题干关键词搜索。
- **数据表格**：展示题干（超长截断）、所属分类、难度标签、浏览次数。
- **批量操作按钮**：
  - 【导入题目】：点击弹出 `Dialog`，上传 Excel 文件，解析后展示预览表格（带分页），确认后调用导入接口。
  - 【下载模板】：直接下载后端提供的 `.xlsx` 文件。
  - 【AI生成】：弹出对话框，输入“知识点/指令”（如：生成5道关于牛顿力学的单选题），点击确认后流式或等待返回结果，并展示在表格中供勾选保存。
- **新增/编辑弹窗**：包含题干（富文本或长文本）、选项（至少2个输入框，可动态增删）、正确答案（单选框）、解析、分类、难度。

### 2.4 后台 - 试卷管理 (`/admin/exam`)
- **组卷方式**：
  1. **手动组卷**：弹出可筛选题目列表的弹窗，勾选题目后点击“加入试卷”，最后保存试卷名称和时长。
  2. **AI智能组卷**：填写“试卷名称”、“总题数”、“各分类占比（如言语40%，逻辑30%）”、“难度比例”，点击生成，后台返回生成的题目列表预览，确认后保存。
- **状态操作**：试卷列表页直接使用 `el-switch` 或下拉按钮控制“发布/停止”。

### 2.5 前台 - 考试答题界面 (`/exam/room/:id`) —— **重中之重**
- **布局要求**：
  - **顶部栏**：试卷名称、倒计时（显眼红色）。
  - **左侧/中间**：题目列表（一次只显示一题或全部滚动展示，推荐一次性全部展示便于检查），每题展示题干与 ABCD 选项（使用 `el-radio-group`）。
  - **右侧（或底部浮动）**：**答题卡**（网格布局，显示题号，点击可跳转，已答/未答颜色区分）。
- **交互限制**：进入页面即调用“开始考试”接口，计时器开始倒计时。
- **提交**：点击“提交试卷”按钮，弹窗二次确认，确认后将所有答案打包提交。提交后跳转至结果页。
- **防作弊**：监听 `beforeunload` 事件，提示用户“离开将自动交卷”（非强制，但需提醒）。

### 2.6 前台 - 考试排行 (`/exam/ranking`)
- 展示成绩列表（排名、头像/昵称、得分、用时）。
- 支持按试卷 ID 筛选，展示当前试卷的整体排名情况。

## 3. 状态管理 (Pinia) 关键设计

- **userStore**：`token`, `userInfo` (包含 role), `isLoggedIn`。
  - `login()`: 存储 token 到 localStorage。
  - `logout()`: 清空状态并跳转登录。
- **examStore**：用于保存当前考试状态（非持久化）。
  - `currentExamId`, `timeLeft`, `answerMap` (存储 `{ questionId: 'A' }`)。
  - `startTimer()` / `pauseTimer()`。

## 4. 全局拦截与开发规范

1. **Axios 请求拦截**：自动在 Header 添加 Token；若 Token 过期（401），自动跳转登录页。
2. **响应拦截**：统一处理 `code` 状态码（如 200 成功，500 服务器错误），使用 `ElMessage` 全局提示错误。
3. **权限控制**：路由守卫中判断 `role`，若管理员误入学生页（或反之），强制重定向到对应首页。
4. **组件复用**：
   - 封装 `ImageUpload` 组件，用于所有图片上传场景，返回图片 URL。
   - 封装 `Pagination` 组件（或直接使用 Element Plus 的分页），统一每页大小（如 10/20/50）。

## 5. 实现状态与差异说明

> 逐项对照本文档需求的实际实现情况。「差异」均因后端接口现状所致，属合理适配。

### 5.1 已实现（与需求一致）

| 需求条目 | 实现位置 |
| :--- | :--- |
| 双布局 AdminLayout / StudentLayout | `src/layout/` |
| 登录页（防抖、按 role 跳转） | `src/views/login/LoginView.vue` |
| 轮播图管理（缩略图、el-switch 即时切换、Dialog + 拖拽上传） | `src/views/admin/BannerManageView.vue`、`src/components/UploadImage.vue` |
| 题目管理（分类下拉树、难度、关键词筛选、题干截断、动态增删选项、答案单/多选） | `src/views/admin/QuestionManageView.vue` |
| 【AI生成】输入知识点 → 表格展示供勾选保存 | 同上（180s 长超时适配 DeepSeek 同步调用） |
| 【导入题目】Dialog 上传 Excel → 预览表格（分页）→ 确认导入；【下载模板】 | `src/views/admin/QuestionImportView.vue` |
| 手动组卷（弹窗筛选题目列表 + 勾选 + 保存名称/时长） | `src/views/admin/ExamManageView.vue` |
| 试卷发布/停止（列表页按钮切换） | 同上 |
| 考试答题界面（顶部倒计时红色、题目全量滚动展示、答题卡网格跳转、已答/未答颜色区分、进入即开考、二次确认提交、beforeunload 提醒） | `src/views/student/ExamDoView.vue`、`src/components/AnswerCard.vue` |
| userStore（token/userInfo/isLoggedIn，localStorage 持久化）、examStore（answerMap、计时器） | `src/store/user.js`、`src/store/exam.js` |
| Axios 拦截（自动带 Token、401 跳登录、统一 code 处理 + ElMessage 提示） | `src/api/request.js` |
| 路由守卫按 role 双向强制重定向 | `src/router/index.js` |
| 公告富文本组件 | `src/components/RichText.vue`（轻量 contenteditable：加粗/斜体/下划线） |

### 5.2 差异适配（后端接口现状所致）

| 本文档原始描述 | 实际实现 | 原因 |
| :--- | :--- | :--- |
| AI 智能组卷：填「总题数、各分类占比、难度比例」 | 规则式 UI：多条 `{分类 + 数量 + 难度(可选)}` 规则 | 后端 `POST /exam/ai/compose` 入参即规则式，无占比语义 |
| AI 智能组卷：返回题目列表预览后确认保存 | 直接生成草稿试卷，可在「详情」中预览题目后再发布 | 后端返回新试卷 ID，题目已绑定 |
| AI 生成题目「流式或等待返回」 | 等待返回（loading 态 + 180s 超时） | 后端为同步阻塞调用，无流式接口 |
| 排行支持按试卷 ID 筛选、展示用时 | 按试卷名前端本地过滤；不展示用时 | `GET /record/ranking` 无参数、返回无 `startTime` |
| 答题选项使用 `el-radio-group` | 单选 radio / 多选 checkbox 按题型切换 | 后端支持多选题（type=1，答案字母排序拼接） |
| 考试记录管理路由 `/admin/records` | `/admin/record` | 与 Target.md 路由规划对齐 |
| 前台路由 `/exam/room/:id`、`/exam/ranking` | `/exam/:id/do`、`/rank` | 与 Target.md 路由规划对齐 |

### 5.3 已知边界

1. **考试无续考接口**：答题中刷新页面丢失答题状态（examStore 非持久化），需重新开考；误触返回后短时间内重进可恢复内存态。
2. **AI 生成题目依赖后端配置**：`llm.api-key` 未配置时接口报错，页面已做错误兜底提示。
3. **公告内容以 HTML 存储**，学生端 `v-html` 渲染（内容来源为管理端富文本，无 XSS 注入面；如后续开放学生投稿需引入 sanitize）。