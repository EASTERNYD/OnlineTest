# 智能在线考试系统 — 前端开发目标文档

> 本文档是**前端开发的功能清单 + 页面→接口映射**，配合《后端接口文档 Request.md》一起使用。
> 后端已全部完成；**前端已完成开发**（位于 `../frontend-exam`，第六节功能清单已全部交付）。接口详情、字段结构、错误码以 `Request.md` 为准。

## 技术栈

| 端 | 技术 |
| :--- | :--- |
| 前端（已完成） | Vue 3 (Composition API)、Element Plus、Vue Router、Pinia、Axios、Vite |
| 后端（已完成） | Spring Boot 4.1.1 + MyBatis + MySQL，接口见 `Request.md` |
| 接口文档 | `http://localhost:8080/doc.html` |

## 前端运行方式

```bash
cd frontend-exam
npm install   # 首次
npm run dev   # 默认 http://localhost:5173（被占用自动换端口，以终端输出为准）
```

- 前端已配置 Vite 代理：`/api` 与 `/upload` 自动转发到 `http://localhost:8080`，无跨域问题，图片相对路径无需手动拼接域名。
- 联调账号：管理员 `admin/admin123`（role=0），学生 `student/123456`（role=1）。

---

## 一、系统角色与端划分

系统分两类使用者，对应两套页面：

| 端 | 角色 | role | 说明 |
| :--- | :--- | :--- | :--- |
| **管理端（后台）** | 管理员 | 0 | 轮播图/公告/分类/题目/试卷的管理维护 |
| **学生端（前台）** | 学生 | 1 | 浏览题库、参加考试、查看成绩与排行 |

登录后根据返回的 `role` 决定进入哪一端；管理端路由需做权限守卫（`role !== 0` 则跳回登录）。

---

## 二、页面与路由规划

### 2.1 通用

| 页面 | 路由 | 说明 |
| :--- | :--- | :--- |
| 登录页 | `/login` | 账号密码登录，成功后按 role 跳转 |

### 2.2 学生端（前台）

| 页面 | 路由 | 说明 |
| :--- | :--- | :--- |
| 首页 | `/home` | 轮播图 + 最新公告 + 热门题目 |
| 公告列表 | `/notice` | 已发布公告列表 |
| 公告详情 | `/notice/:id` | 公告全文 |
| 题库浏览 | `/question` | 按分类/难度/关键词筛选题目 |
| 考试列表 | `/exam` | 已发布试卷列表 |
| 考试答题 | `/exam/:id/do` | 开始考试 → 答题 → 交卷 |
| 我的成绩 | `/record` | 考试记录列表 + 详情 |
| 排行榜 | `/rank` | 成绩排行 |

### 2.3 管理端（后台）

| 页面 | 路由 | 说明 |
| :--- | :--- | :--- |
| 数据概览 | `/admin/dashboard` | 题目/试卷/用户/记录等统计 |
| 轮播图管理 | `/admin/banner` | 轮播图增删改查 + 图片上传 |
| 公告管理 | `/admin/notice` | 公告增删改查 + 发布/草稿 |
| 分类管理 | `/admin/category` | 分类树/列表 + 增删改 |
| 题目管理 | `/admin/question` | 题目条件分页 + 增删改 + AI 生成 |
| 题目导入 | `/admin/question/import` | Excel 模板下载/预览/导入 |
| 试卷管理 | `/admin/exam` | 手动组卷 + 智能组卷 + 发布 |
| 考试记录管理 | `/admin/record` | 查看/删除所有学生记录 |

---

## 三、页面 → 接口映射（核心）

> 权限：`公开` = 无需 token；`登录` = 需 token；`管理员` = 需 role=0。
> 接口完整说明见 `Request.md` 第 4 节。

### 3.1 登录

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 登录 | `POST /user/login` | 公开 |
| 获取当前用户信息 | `GET /user/info` | 登录 |
| 退出登录 | `POST /user/logout` | 登录 |

### 3.2 学生端首页 `/home`

| 区块 | 接口 | 权限 |
| :--- | :--- | :--- |
| 轮播图 | `GET /banner/enabled` | 公开 |
| 最新公告（前 5 条） | `GET /notice/latest?limit=5` | 公开 |
| 热门题目（前 10 条） | `GET /question/hot?limit=10` | 公开 |

### 3.3 公告 `/notice`

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 公告列表 | `GET /notice/enabled` | 公开 |
| 公告详情 | `GET /notice/{id}` | 登录 |

### 3.4 题库浏览 `/question`

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 分类列表 | `GET /category/list` | 登录 |
| 题目条件分页 | `POST /question/page` | 登录 |

筛选条件：`categoryId`（分类）、`difficulty`（难度）、`type`（题型）、`keyword`（题干关键词）。

### 3.5 考试流程 `/exam`

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 试卷列表 | `GET /exam/list` | 登录 |
| 开始考试 | `POST /exam/{id}/start` | 登录 |
| 提交答案 | `POST /exam/{id}/submit` | 登录 |

### 3.6 成绩与排行

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 我的记录列表 | `GET /record/list` | 登录 |
| 记录详情（答题明细） | `GET /record/{id}` | 登录 |
| 排行榜 | `GET /record/ranking` | 登录 |

### 3.7 管理端：轮播图 `/admin/banner`

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 分页列表 | `GET /banner/list?page=&size=` | 管理员 |
| 上传图片 | `POST /file/upload` | 管理员 |
| 新增 | `POST /banner` | 管理员 |
| 编辑 | `PUT /banner/{id}` | 管理员 |
| 状态切换 | `PATCH /banner/{id}/status?status=` | 管理员 |
| 删除 | `DELETE /banner/{id}` | 管理员 |

### 3.8 管理端：公告 `/admin/notice`

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 分页列表 | `GET /notice/list?page=&size=` | 管理员 |
| 新增 | `POST /notice` | 管理员 |
| 编辑 | `PUT /notice/{id}` | 管理员 |
| 状态切换 | `PATCH /notice/{id}/status?status=` | 管理员 |
| 删除 | `DELETE /notice/{id}` | 管理员 |

### 3.9 管理端：分类 `/admin/category`

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 列表（含题目数） | `GET /category/list` | 登录 |
| 树形结构 | `GET /category/tree` | 登录 |
| 新增 | `POST /category` | 管理员 |
| 编辑 | `PUT /category/{id}` | 管理员 |
| 删除 | `DELETE /category/{id}` | 管理员 |

### 3.10 管理端：题目 `/admin/question`

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 条件分页 | `POST /question/page` | 登录 |
| 新增 | `POST /question` | 管理员 |
| 编辑 | `PUT /question/{id}` | 管理员 |
| 删除 | `DELETE /question/{id}` | 管理员 |
| AI 生成题目 | `POST /question/ai/generate` | 管理员 |

### 3.11 管理端：题目导入 `/admin/question/import`

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 下载模板 | `GET /question/template` | 公开 |
| 预览 Excel | `POST /question/excel/preview` | 管理员 |
| 批量导入 | `POST /question/excel/import` | 管理员 |

### 3.12 管理端：试卷 `/admin/exam`

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 试卷列表 | `GET /exam/list?page=&size=` | 登录 |
| 试卷详情（含答案） | `GET /exam/{id}` | 管理员 |
| 手动创建（选题） | `POST /exam` | 管理员 |
| 智能组卷 | `POST /exam/ai/compose` | 管理员 |
| 编辑 | `PUT /exam/{id}` | 管理员 |
| 发布/停止 | `PATCH /exam/{id}/status?status=` | 管理员 |
| 删除 | `DELETE /exam/{id}` | 管理员 |

> 手动创建/编辑试卷时的「选题」，用 `POST /question/page` 查询题目列表让管理员勾选，把选中的题目 ID 数组传给 `POST /exam`。

### 3.13 管理端：数据概览 `/admin/dashboard`

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| Dashboard 统计 | `GET /stats/dashboard` | 管理员 |

返回题目/试卷/用户/记录/分类总数，以及难度分布、各分类题目数。

### 3.14 管理端：考试记录管理 `/admin/record`

| 功能 | 接口 | 权限 |
| :--- | :--- | :--- |
| 所有学生记录（分页） | `GET /record/admin/list?page=&size=&userId=&examId=` | 管理员 |
| 查看任意记录详情 | `GET /record/admin/{id}` | 管理员 |
| 删除任意记录 | `DELETE /record/admin/{id}` | 管理员 |

---

## 四、关键业务流程

### 4.1 登录流程
1. 登录页提交 `{username, password}` → `POST /user/login`；
2. 保存返回的 `token`（如 localStorage / Pinia）；
3. 根据 `role` 跳转：`0` 进管理端，`1` 进学生端；
4. 后续所有请求在 `Authorization: Bearer <token>` 头带上 token；
5. 收到 `401` 时清 token 并跳回登录页。

### 4.2 考试流程（学生）
1. 考试列表 → `GET /exam/list`，只展示 `status === 1`（已发布）的试卷；
2. 点击「开始」→ `POST /exam/{id}/start`，拿到 `recordId` + 题目列表（**不含答案**）；
3. 前端倒计时 `duration` 分钟，学生逐题作答；
4. 交卷 → `POST /exam/{id}/submit`，body：`{ recordId, answers: { "题目ID": "答案" } }`；
5. 交卷后跳「我的成绩」→ `GET /record/{id}` 查看判分与逐题明细。

### 4.3 组卷流程（管理员）
1. **手动组卷**：`POST /question/page` 选题 → 勾选题目 → `POST /exam` 传 `questionIds` 数组；
2. **智能组卷**：填 `name`、`duration` 和多条规则 `{categoryId, count, difficulty?}` → `POST /exam/ai/compose`，返回新试卷 ID；
3. 两种方式生成的试卷都是「草稿」状态，需 `PATCH /exam/{id}/status?status=1` 发布后才能被学生看到/开考。

### 4.4 Excel 导入流程（管理员）
1. `GET /question/template` 下载模板；
2. 管理员填好数据后上传 → 可选先 `POST /question/excel/preview` 预览；
3. `POST /question/excel/import` 批量导入，返回成功条数。

---

## 五、前端开发注意事项

1. **token 处理**：登录后存储；请求拦截器统一加 `Authorization` 头；响应拦截器统一处理 `401`（跳登录）、`403`（提示无权限）。
2. **`options` 字段差异**（易错）：
   - 接口**返回**的题目 `options` 是**数组** `["A. ...", "B. ..."]`；
   - **新增/编辑题目**时，请求体的 `options` 要传 **JSON 字符串** `"[\"A. ...\",\"B. ...\"]"`。
3. **时间格式**：后端时间为 `yyyy-MM-dd'T'HH:mm:ss` 字符串，展示时自行格式化。
4. **分页参数**：`/banner/list`、`/notice/list`、`/exam/list` 用 query `?page=&size=`；`/question/page` 用 POST body `{page, size, ...}`。
5. **状态枚举**：见 `Request.md` 第 2 节数据字典（试卷状态 0草稿/1发布/2结束、题目 0单选/1多选 等）。
6. **多选答案**：多选 `answer` 为排序后的字母串（如 `ABD`），交卷时前端将所选选项按字母排序后拼接。
7. **图片上传**：先 `POST /file/upload` 拿 `url`，再填进轮播图的 `imageUrl`；`url` 是相对路径，拼接 `http://localhost:8080` 访问。
8. **管理端路由守卫**：`role !== 0` 时禁止进入 `/admin/*`。

---

## 六、功能清单（交付范围）— ✅ 已全部完成

### 学生端（前台）
- [x] 登录 / 退出
- [x] 首页：轮播图、最新公告、热门题目
- [x] 公告列表 / 详情
- [x] 题库浏览（分类、难度、关键词筛选）
- [x] 试卷列表（仅已发布）
- [x] 在线答题 + 倒计时 + 交卷
- [x] 我的成绩（列表 + 逐题明细）
- [x] 排行榜

### 管理端（后台）
- [x] 登录（role=0）
- [x] 数据概览（Dashboard 统计）
- [x] 轮播图管理（含图片上传）
- [x] 公告管理（发布/草稿切换）
- [x] 分类管理（列表 + 树形）
- [x] 题目管理（条件分页 + 增删改 + AI 生成）
- [x] 题目批量导入（Excel 模板/预览/导入）
- [x] 试卷管理（手动组卷 + 智能组卷 + 发布/停止）
- [x] 考试记录管理（查看/删除所有学生记录）

---

## 七、实现说明与已知边界

> 以下为前端实际实现中对文档细节的适配说明，详见 `../frontend-exam/README.md`。

1. **错误处理双形态**：响应拦截器同时处理「HTTP 200 + 业务 code ≠ 200」与「HTTP 401/403」（后端 JWT 拦截器直接返回 HTTP 状态码）两种错误形态；401 清 token 跳登录，403 提示无权限。
2. **路由守卫双向重定向**：学生访问 `/admin/*` → 提示并跳 `/home`；管理员访问学生端页面 → 跳 `/admin/dashboard`。
3. **options 双形态**：请求封装层在新增/编辑题目时自动将选项数组序列化为 JSON 字符串，页面代码只操作数组。
4. **考试流程**：倒计时归零自动交卷；交卷二次确认；多选答案按字母排序后拼接；`beforeunload` 离开提醒；误触返回后短时间内重进可恢复内存中的考试状态。
5. **AI 生成题目**：`POST /question/ai/generate` 为同步调用 DeepSeek，前端单独设置 180s 超时；返回题目不落库，页面勾选后逐条调 `POST /question` 保存；依赖后端 `llm.api-key` 配置。
6. **智能组卷**：按后端实际的规则式入参 `{categoryId, count, difficulty?}` 实现 UI（多条规则增删），非「分类占比」式。
7. **排行榜**：`GET /record/ranking` 无筛选参数、无 `startTime`，页面按试卷名做前端本地过滤，「用时」无法精确计算故未展示。
8. **考试无续考接口**：答题中刷新页面会丢失答题状态（Pinia 非持久化），需重新开考（生成新记录）。
9. **图片路径**：经 Vite 代理后直接使用后端返回的相对路径（如 `/upload/xxx.png`），无需拼接 `http://localhost:8080`。
10. **公告富文本**：轻量 contenteditable 编辑器（加粗/斜体/下划线），内容以 HTML 存储、`v-html` 渲染。
