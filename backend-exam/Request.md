# 智能在线考试系统 — 后端接口文档（前端对接用）

> **技术栈**：Spring Boot 4.1.1 (Java 21)、Spring MVC、MyBatis、MySQL 8.0、Maven
> **核心依赖**：Lombok、EasyExcel（Excel 导入导出）、Jackson、JWT（jjwt）、spring-security-crypto（BCrypt）、Knife4j（接口文档）、DeepSeek（AI 生成题目）
> **接口文档在线地址**：`http://localhost:8080/doc.html`（OpenAPI JSON 规范：`/v3/api-docs`）
> **服务地址**：`http://localhost:8080`，统一前缀 `/api/v1`

---

## 0. 默认账号（联调用）

| 角色 | 用户名 | 密码 | role |
| :--- | :--- | :--- | :--- |
| 管理员 | `admin` | `admin123` | 0 |
| 学生 | `student` | `123456` | 1 |

---

## 1. 全局约定

### 1.1 统一返回格式 `Result<T>`

所有接口（含异常）统一返回以下 JSON 结构：

```json
{
  "code": 200,
  "msg": "ok",
  "data": { }
}
```

- `code`：状态码，见 1.3。
- `msg`：提示信息，成功为 `ok`，失败为具体原因。
- `data`：业务数据，无数据时为 `null`。

### 1.2 分页返回格式 `PageResult<T>`

所有「分页列表」接口的 `data` 结构：

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "total": 12,
    "list": [ { }, { } ]
  }
}
```

### 1.3 错误码

| code | 含义 | 说明 |
| :--- | :--- | :--- |
| 200 | 成功 | |
| 401 | 未登录 / token 失效 | 需重新登录 |
| 403 | 无权限 | 非管理员调用了管理端接口，或操作了他人数据 |
| 404 | 资源不存在 | |
| 500 | 服务器内部错误 | `msg` 会带具体原因 |

### 1.4 鉴权方式

除「公开接口」外，其余接口均需在请求头携带：

```
Authorization: Bearer <token>
```

`token` 由 `/user/login` 返回。登录后前端应保存并在每次请求带上。

**公开接口（无需 token）**：

| 接口 | 说明 |
| :--- | :--- |
| `POST /user/login` | 登录 |
| `GET /banner/enabled` | 前台轮播图 |
| `GET /notice/enabled` | 前台公告 |
| `GET /notice/latest` | 最新公告 |
| `GET /question/hot` | 热门题目 |
| `GET /question/template` | Excel 导入模板下载 |

**管理员接口（需 `role = 0`）**：见各接口标注「管理员」。

### 1.5 时间格式

所有时间字段（`createTime` / `startTime` / `submitTime` 等）为字符串，格式：`yyyy-MM-dd'T'HH:mm:ss`，例如 `2026-09-08T18:25:14`。

### 1.6 关于 `options` / `answers` 字段（重要）

- **响应里**：题目的 `options` 一律返回为**字符串数组**，如 `["A. 北京", "B. 上海"]`。
- **新增/修改题目的请求里**：`options` 需传 **JSON 字符串**（数组序列化后的字符串），如 `"[\"A. 北京\",\"B. 上海\"]"`。
- **提交答案**：`answers` 传 **对象**，键为题目ID字符串，值为答案字母串。

---

## 2. 数据字典（枚举值汇总）

| 字段 | 取值 | 含义 |
| :--- | :--- | :--- |
| 用户 `role` | 0 / 1 | 管理员 / 学生 |
| 用户 `status` | 0 / 1 | 禁用 / 启用 |
| 轮播图 `status` | 0 / 1 | 禁用 / 启用 |
| 公告 `type` | 1 / 2 / 3 | 通知 / 警告 / 活动 |
| 公告 `status` | 0 / 1 | 草稿 / 发布 |
| 题目 `type` | 0 / 1 | 单选 / 多选 |
| 题目 `difficulty` | 1 / 2 / 3 | 简单 / 中等 / 困难 |
| 试卷 `status` | 0 / 1 / 2 | 草稿 / 已发布 / 已结束 |
| 考试记录 `status` | 0 / 1 | 进行中 / 已交卷（已判分） |

---

## 3. 数据库表结构（7 张）

> 说明：`options`、`answers`、`question_ids` 在数据库中以 **JSON 字符串存于 TEXT 列**，由后端序列化/解析，前端按第 1.6 节的规则处理即可。

### 3.1 sys_user（用户）
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | bigint | 主键 |
| username | varchar(50) | 登录账号（唯一） |
| password | varchar(255) | BCrypt 加密密码 |
| nickname | varchar(50) | 昵称 |
| role | tinyint | 0管理员 / 1学生 |
| status | tinyint | 0禁用 / 1启用 |
| create_time | datetime | 创建时间 |

### 3.2 banner（轮播图）
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | bigint | 主键 |
| title | varchar(100) | 标题 |
| image_url | varchar(255) | 图片地址 |
| link_url | varchar(255) | 跳转链接 |
| sort_order | int | 排序权重（越大越靠前） |
| status | tinyint | 0禁用 / 1启用 |
| create_time | datetime | |

### 3.3 notice（公告）
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | bigint | 主键 |
| title | varchar(100) | 标题 |
| content | text | 内容 |
| type | tinyint | 1通知 / 2警告 / 3活动 |
| status | tinyint | 0草稿 / 1发布 |
| create_time | datetime | |

### 3.4 category（分类）
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | bigint | 主键 |
| name | varchar(50) | 分类名 |
| parent_id | bigint | 父级ID（0 表示顶级，支持树形） |
| sort_order | int | 排序 |
| create_time | datetime | |

### 3.5 question（题目）
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | bigint | 主键 |
| category_id | bigint | 分类ID |
| stem | text | 题干 |
| options | text(JSON数组) | 选项，如 `["A. 北京","B. 上海"]` |
| answer | varchar(10) | 答案，单选 `A`，多选 `ABD`（排序后拼接） |
| analysis | text | 解析 |
| difficulty | tinyint | 1简单 / 2中等 / 3困难 |
| view_count | int | 浏览量（热门排序） |
| type | tinyint | 0单选 / 1多选 |
| create_time | datetime | |

### 3.6 exam（试卷）
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | bigint | 主键 |
| name | varchar(100) | 试卷名 |
| total_score | int | 总分 |
| duration | int | 时长（分钟） |
| question_ids | text(JSON数组) | 题目ID列表，如 `[1,2,3]` |
| status | tinyint | 0草稿 / 1发布 / 2结束 |
| create_time | datetime | |

### 3.7 exam_record（考试记录）
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | bigint | 主键 |
| user_id | bigint | 考生ID |
| exam_id | bigint | 试卷ID |
| answers | text(JSON对象) | 考生答案，如 `{"1":"A","2":"BD"}` |
| score | int | 得分 |
| correct_count | int | 正确题数 |
| wrong_count | int | 错误题数 |
| status | tinyint | 0进行中 / 1已交卷 |
| start_time | datetime | 开始时间 |
| submit_time | datetime | 提交时间 |
| create_time | datetime | |

---

## 4. 接口详设

> 权限列：`公开` = 无需 token；`登录` = 需 token（任意角色）；`管理员` = 需 role=0。

### 4.1 用户 `/user`

#### 登录 `POST /user/login`（公开）

请求体：
```json
{ "username": "admin", "password": "admin123" }
```

响应 `data`：
```json
{
  "token": "eyJhbGciOi...",
  "userId": 1,
  "username": "admin",
  "nickname": "管理员",
  "role": 0
}
```

#### 当前用户信息 `GET /user/info`（登录）

响应 `data`（password 恒为 null）：
```json
{ "id": 1, "username": "admin", "password": null, "nickname": "管理员", "role": 0, "status": 1, "createTime": "2026-09-08T18:25:14" }
```

#### 退出登录 `POST /user/logout`（登录）

无参数，响应 `data` 为 `null`（JWT 无状态，前端丢弃本地 token 即可）。

---

### 4.2 轮播图 `/banner`

| 方法 | 路径 | 权限 | 说明 |
| :--- | :--- | :--- | :--- |
| GET | `/banner/list` | 管理员 | 分页列表，`?page=1&size=10` |
| GET | `/banner/enabled` | 公开 | 已启用列表 |
| GET | `/banner/{id}` | 登录 | 详情 |
| POST | `/banner` | 管理员 | 新增 |
| PUT | `/banner/{id}` | 管理员 | 修改 |
| PATCH | `/banner/{id}/status?status=1` | 管理员 | 切换状态 |
| DELETE | `/banner/{id}` | 管理员 | 删除 |

轮播图对象字段：
```json
{ "id": 1, "title": "标题", "imageUrl": "/upload/xxx.png", "linkUrl": "https://...", "sortOrder": 1, "status": 1, "createTime": "..." }
```

- **新增** `POST /banner`：请求体传 `title / imageUrl / linkUrl / sortOrder / status`（`id`、`createTime` 由后端生成，无需传）。
- **修改** `PUT /banner/{id}`：请求体同上。
- 图片地址建议先调 `POST /file/upload` 拿 URL 再填入 `imageUrl`。

---

### 4.3 公告 `/notice`

| 方法 | 路径 | 权限 | 说明 |
| :--- | :--- | :--- | :--- |
| GET | `/notice/list` | 管理员 | 分页，`?page=1&size=10` |
| GET | `/notice/enabled` | 公开 | 已发布公告列表 |
| GET | `/notice/latest?limit=5` | 公开 | 最新 N 条 |
| GET | `/notice/{id}` | 登录 | 详情 |
| POST | `/notice` | 管理员 | 发布 |
| PUT | `/notice/{id}` | 管理员 | 更新 |
| PATCH | `/notice/{id}/status?status=1` | 管理员 | 切换状态 |
| DELETE | `/notice/{id}` | 管理员 | 删除 |

公告对象字段：
```json
{ "id": 1, "title": "标题", "content": "内容", "type": 1, "status": 1, "createTime": "..." }
```

---

### 4.4 分类 `/category`

| 方法 | 路径 | 权限 | 说明 |
| :--- | :--- | :--- | :--- |
| GET | `/category/list` | 登录 | 分类列表（含题目数） |
| GET | `/category/tree` | 登录 | 树形结构 |
| POST | `/category` | 管理员 | 新增 |
| PUT | `/category/{id}` | 管理员 | 更新 |
| DELETE | `/category/{id}` | 管理员 | 删除（被题目引用时删除失败） |

**列表** `GET /category/list` 响应 `data`（数组）：
```json
[
  { "id": 1, "name": "言语理解", "parentId": 0, "sortOrder": 0, "createTime": "...", "questionCount": 3, "children": null }
]
```

**树形** `GET /category/tree` 响应 `data`（数组，`children` 为子分类数组）：
```json
[
  { "id": 1, "name": "言语理解", "parentId": 0, "sortOrder": 0, "questionCount": null, "children": [ ] }
]
```

---

### 4.5 题目 `/question`

| 方法 | 路径 | 权限 | 说明 |
| :--- | :--- | :--- | :--- |
| POST | `/question/page` | 登录 | 复杂条件分页 |
| GET | `/question/hot?limit=10` | 公开 | 热门题目 |
| GET | `/question/{id}` | 登录 | 详情（浏览量+1） |
| POST | `/question` | 管理员 | 新增 |
| PUT | `/question/{id}` | 管理员 | 修改 |
| DELETE | `/question/{id}` | 管理员 | 删除 |
| POST | `/question/excel/preview` | 管理员 | 预览 Excel（`multipart/form-data`） |
| POST | `/question/excel/import` | 管理员 | 导入 Excel（`multipart/form-data`） |
| GET | `/question/template` | 公开 | 下载导入模板 |
| POST | `/question/ai/generate` | 管理员 | AI 生成题目（DeepSeek） |

**分页查询** `POST /question/page` 请求体（字段均可选）：
```json
{ "page": 1, "size": 10, "categoryId": 1, "difficulty": 2, "type": 0, "keyword": "词语" }
```
- `categoryId` 分类ID；`difficulty` 难度；`type` 题型；`keyword` 题干模糊匹配。
- 响应 `data` 为分页结构，`list` 元素是题目对象（`options` 为数组）。

**题目对象（响应，`options` 为数组）**：
```json
{
  "id": 1,
  "categoryId": 1,
  "stem": "下列词语书写完全正确的一项是：",
  "options": ["A. 川流不息", "B. 再接再励", "C. 世外桃园", "D. 按步就班"],
  "answer": "A",
  "analysis": "其他三项正确写法：再接再厉、世外桃源、按部就班。",
  "difficulty": 1,
  "viewCount": 0,
  "type": 0,
  "createTime": "2026-09-08T18:25:14"
}
```

**新增/修改** `POST /question`、`PUT /question/{id}` 请求体（注意 `options` 为**字符串**）：
```json
{
  "categoryId": 1,
  "stem": "题干内容",
  "options": "[\"A. 选项一\",\"B. 选项二\",\"C. 选项三\",\"D. 选项四\"]",
  "answer": "A",
  "analysis": "解析",
  "difficulty": 1,
  "type": 0
}
```

---

#### AI 生成题目 `POST /question/ai/generate`（管理员，DeepSeek）

请求体：
```json
{ "knowledgePoint": "等差数列求和", "count": 5, "difficulty": 2, "categoryId": 1 }
```

- `knowledgePoint`：必填，知识点 / 出题指令。
- `count`：生成数量，默认 5。
- `difficulty`：难度 1/2/3，默认 1。
- `categoryId`：可选，指定题目归属分类。

响应 `data`（数组，题目对象，`options` 为数组；**不落库**，前端确认后可调 `POST /question` 逐个保存）：
```json
[
  { "categoryId": 1, "stem": "题干", "options": ["A. ...", "B. ...", "C. ...", "D. ..."], "answer": "A", "analysis": "解析", "difficulty": 2, "type": 0 }
]
```

> 依赖 DeepSeek 大模型：需在 `application.yml` 配置 `llm.api-key`；`llm.base-url` 默认 `https://api.deepseek.com`，`llm.model` 默认 `deepseek-chat`。

---

### 4.6 试卷与考试 `/exam`

| 方法 | 路径 | 权限 | 说明 |
| :--- | :--- | :--- | :--- |
| GET | `/exam/list` | 登录 | 试卷分页，`?page=1&size=10` |
| GET | `/exam/{id}` | 管理员 | 详情（含答案） |
| POST | `/exam` | 管理员 | 手动创建 |
| POST | `/exam/ai/compose` | 管理员 | 智能组卷 |
| PUT | `/exam/{id}` | 管理员 | 更新 |
| PATCH | `/exam/{id}/status?status=1` | 管理员 | 发布/停止 |
| DELETE | `/exam/{id}` | 管理员 | 删除 |
| POST | `/exam/{id}/start` | 登录 | 开始考试 |
| POST | `/exam/{id}/submit` | 登录 | 提交答案 |

**手动创建** `POST /exam` 请求体：
```json
{ "name": "行测模拟卷一", "totalScore": 100, "duration": 30, "questionIds": [1, 2, 3] }
```

**智能组卷** `POST /exam/ai/compose` 请求体：
```json
{
  "name": "行测模拟卷一",
  "duration": 30,
  "rules": [
    { "categoryId": 1, "count": 2, "difficulty": 1 },
    { "categoryId": 2, "count": 1 }
  ]
}
```
- 每条规则按「分类 + 难度 + 数量」随机抽题；`difficulty` 可省略表示不限难度；指定难度抽不足时自动放宽难度补齐。
- 响应 `data` 为**新试卷ID**（数字），如 `1`。生成状态为草稿，需调发布接口后才能开考。

**试卷详情** `GET /exam/{id}` 响应 `data`：
```json
{
  "id": 1,
  "name": "行测模拟卷一",
  "totalScore": 100,
  "duration": 30,
  "status": 1,
  "createTime": "...",
  "questions": [ { "题目对象，含 answer 和 analysis" } ]
}
```

**开始考试** `POST /exam/{id}/start` 响应 `data`（题目**不含答案**，`answer`/`analysis` 为 null）：
```json
{
  "recordId": 1,
  "examId": 1,
  "name": "行测模拟卷一",
  "duration": 30,
  "totalScore": 5,
  "questions": [ { "id": 1, "stem": "...", "options": ["..."], "answer": null, "analysis": null, "type": 0 } ]
}
```
> `recordId` 是本次考试记录ID，提交答案时需要带上。

**提交答案** `POST /exam/{id}/submit` 请求体：
```json
{ "recordId": 1, "answers": { "1": "A", "4": "BD" } }
```
- `answers`：对象，键为题目ID字符串，值为答案字母串（单选 `A`，多选 `ABD`）。
- 提交后自动判分，响应 `data` 为 `null`。判分结果通过 `/record/{recordId}` 查看。

---

### 4.7 考试记录与排行 `/record`

| 方法 | 路径 | 权限 | 说明 |
| :--- | :--- | :--- | :--- |
| GET | `/record/list` | 登录 | 当前用户的记录列表 |
| GET | `/record/{id}` | 登录 | 记录详情（含逐题明细，仅本人） |
| DELETE | `/record/{id}` | 登录 | 删除记录（仅本人） |
| GET | `/record/ranking` | 登录 | 成绩排行 |
| GET | `/record/admin/list?page=&size=&userId=&examId=` | 管理员 | 查看所有学生记录（分页，可按学生/试卷筛选） |
| GET | `/record/admin/{id}` | 管理员 | 查看任意记录详情（含答题明细） |
| DELETE | `/record/admin/{id}` | 管理员 | 删除任意记录 |

**记录列表** `GET /record/list` 响应 `data`（数组，元素字段）：
```json
{
  "id": 1, "userId": 2, "examId": 1, "examName": "行测模拟卷一",
  "score": 10, "correctCount": 2, "wrongCount": 0, "status": 1,
  "startTime": "...", "submitTime": "...", "createTime": "..."
}
```

**记录详情** `GET /record/{id}` 响应 `data`：
```json
{
  "id": 1, "examId": 1, "examName": "行测模拟卷一",
  "score": 10, "correctCount": 2, "wrongCount": 0, "status": 1,
  "startTime": "...", "submitTime": "...",
  "answers": [
    {
      "questionId": 1,
      "stem": "题干",
      "options": ["A. ...", "B. ..."],
      "type": 0,
      "correctAnswer": "A",
      "userAnswer": "A",
      "correct": true,
      "analysis": "解析"
    }
  ]
}
```

**排行** `GET /record/ranking` 响应 `data`（数组，按总分、正确数降序）：
```json
{
  "id": 1, "userId": 2, "nickname": "学生", "examId": 1, "examName": "行测模拟卷一",
  "score": 10, "correctCount": 2, "wrongCount": 0, "submitTime": "..."
}
```

---

### 4.8 文件上传 `/file`

#### 上传 `POST /file/upload`（管理员）

- 请求：`multipart/form-data`，字段名 `file`。
- 响应 `data`：
```json
{ "url": "/upload/ed975a340d274043a3cb284e6b91ce4e.png", "name": "原文件名.png" }
```
- `url` 为可访问的相对路径，前端拼接 `http://localhost:8080` 即可访问（已配置静态资源映射）。

---

**管理员查看记录** `GET /record/admin/list` 响应元素比学生端多 `nickname`、`username` 两个字段（考生昵称/账号）；`GET /record/admin/{id}` 与 `DELETE /record/admin/{id}` 无归属校验，可操作任意记录。

---

### 4.9 数据统计 `/stats`

#### Dashboard 数据概览 `GET /stats/dashboard`（管理员）

响应 `data`：
```json
{
  "totalQuestions": 12,
  "totalExams": 2,
  "publishedExams": 2,
  "totalUsers": 2,
  "totalStudents": 1,
  "totalRecords": 2,
  "totalCategories": 4,
  "difficultyDistribution": [
    { "difficulty": 1, "count": 4 },
    { "difficulty": 2, "count": 4 },
    { "difficulty": 3, "count": 4 }
  ],
  "categoryDistribution": [
    { "categoryId": 1, "name": "言语理解", "count": 3 }
  ]
}
```

- `totalRecords`：已交卷的记录数。
- `difficultyDistribution`：各难度题目数（1简单/2中等/3困难）。
- `categoryDistribution`：各分类题目数。

---

## 5. 业务规则

1. **判分逻辑**：纯选择题按标准答案逐题匹配，多选按「排序后的字母集合相等」判定；每题分值 = 总分 ÷ 题目数；自动计算 `score`、`correctCount`、`wrongCount`。提交后记录状态置为「已交卷」，不可重复提交。
2. **试卷状态流转**：`草稿(0) → 已发布(1) → 已结束(2)`。只有「已发布」状态才能被学生开考。
3. **组卷**：手动组卷与智能组卷的题目ID均存为 JSON 数组；组卷结果默认「草稿」状态。
4. **安全**：密码 BCrypt 加密；管理端写接口（轮播/公告/分类/题目的增删改、试卷管理、文件上传）校验 `role=0`；考试记录详情/删除校验本人。

---

## 6. Excel 批量导入

**模板下载**：`GET /question/template`（公开），返回 `题目导入模板.xlsx`。

**模板表头**（顺序固定）：

| 题干 | 选项A | 选项B | 选项C | 选项D | 答案 | 解析 | 难度 | 分类 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |

- `答案`：填写 A/B/C/D（单选）。
- `难度`：1/2/3。
- `分类`：填分类名，不存在时会自动创建。
- 选项字母前缀（A. / B. …）由后端自动拼接，Excel 里只填选项文字。

**预览**：`POST /question/excel/preview`（`multipart/form-data`，字段名 `file`），返回解析出的题目数组（不落库）。
**导入**：`POST /question/excel/import`（同上），返回 `data` 为成功导入条数。
