# 智能在线考试系统 — 后端

一个基于 **Spring Boot** 的在线考试系统后端，覆盖「系统信息管理、题库管理、智能组卷、考试作答、自动判分、成绩排行」全流程。试卷目前仅支持纯选择题（单选 + 多选），适用于行测等场景。

> 前端已实现（Vue 3 + Element Plus + Vite），位于 `../frontend-exam`，启动方式见其 `README.md`；项目总览见根目录 `../README.md`。

---

## 一、技术栈

| 分类 | 技术 |
| :--- | :--- |
| 语言 / 框架 | Java 21、Spring Boot 4.1.1、Spring MVC |
| 持久层 | MyBatis（`mybatis-spring-boot-starter` 4.1.0，纯注解 + 动态 SQL） |
| 数据库 | MySQL 8.0 |
| 鉴权 | JWT（jjwt 0.12.x）+ BCrypt 密码加密 |
| 工具库 | Lombok、EasyExcel 3.3.4（Excel 导入导出）、Jackson（JSON 字段处理） |
| AI 能力 | DeepSeek（AI 生成题目，通过 RestClient 调用 OpenAI 兼容协议，无需 SDK） |
| 接口文档 | Knife4j 4.5.0（基于 springdoc-openapi，访问 `/doc.html`） |
| 构建 | Maven（项目自带 `mvnw` 包装器） |

---

## 二、功能模块

| 模块 | 说明 |
| :--- | :--- |
| 用户管理 | 登录、当前用户信息、退出（JWT 鉴权 + 角色校验） |
| 轮播图管理 | 增删改查、状态切换、图片上传、前台展示 |
| 公告管理 | 分页查询、发布/草稿切换、最新公告、前台展示 |
| 分类管理 | 列表（含题目数统计）、树形结构、增删改（删除前校验引用） |
| 题目管理 | 复杂条件分页、热门题目、增删改、Excel 预览/导入/模板下载、**AI 生成题目** |
| 试卷与考试 | 手动组卷、**智能组卷**、发布/停止、开始考试、提交自动判分 |
| 考试记录与排行 | 我的记录、答题明细、删除记录、成绩排行、**管理员查看所有记录** |
| 数据统计 | 后台 Dashboard 数据概览（总数 + 难度/分类分布） |

> 「智能组卷」采用**规则随机抽题算法**（按分类占比 + 难度 + 数量随机抽取），不调用大模型；「AI 生成题目」调用 **DeepSeek** 真实生成；判分采用**标准答案自动匹配**，零延迟零成本。

---

## 三、项目结构

```
src/main/java/com/example/demo/
├── DemoApplication.java          # 启动类（@MapperScan）
├── common/                       # Result 统一返回、PageResult 分页、BizException、全局异常、@RequireAdmin 注解
├── config/                       # JwtInterceptor、WebConfig(CORS+静态映射)、Knife4jConfig、BeanConfig、DataInitializer(种子数据)
├── controller/                   # User/Banner/Notice/Category/Question/Exam/Record/File/Stats 9 个控制器
├── dto/                          # 请求体对象（登录、题目查询、组卷规则、提交答案、Excel 行模型等）
├── entity/                       # 7 张表对应的实体类
├── mapper/                       # MyBatis 接口（纯注解）
├── service/                      # 业务层
├── util/                         # JwtUtil、JsonUtil、ExcelUtil
└── vo/                           # 返回对象（登录结果、题目详情、试卷详情、排行等）

src/main/resources/
├── application.yml               # 应用配置（端口、数据源、JWT、文件路径等）
└── db/schema.sql                 # 建表脚本（启动时自动执行，幂等）
```

---

## 四、快速开始

### 1. 环境要求

- **JDK 21**（本项目 `JAVA_HOME` 示例为 `d:\program files\java\jdk21`）
- **MySQL 8.0**（需先启动服务）

### 2. 数据库配置

数据库连接默认 `localhost:3306/exam_system`（库不存在会自动创建），密码通过**环境变量**注入，避免真实密码进入版本库：

```bash
# Windows cmd（当前会话有效）
set MYSQL_PASSWORD=你的MySQL密码
# PowerShell
$env:MYSQL_PASSWORD="你的MySQL密码"
# Linux / macOS
export MYSQL_PASSWORD=你的MySQL密码
```

对应 `application.yml` 中的配置为 `password: ${MYSQL_PASSWORD:root}`（未设置环境变量时回退为 `root`）。

其他可选环境变量：

| 环境变量 | 用途 | 默认值 |
| :--- | :--- | :--- |
| `LLM_API_KEY` | DeepSeek API Key（AI 生成题目） | 占位符（未配置时 AI 功能不可用） |
| `JWT_SECRET` | JWT 签名密钥 | 开发占位值（**生产必须替换**） |
| `FILE_UPLOAD_DIR` | 图片上传目录 | `./upload`（相对后端运行目录） |

> 在 IDEA 中运行 `DemoApplication` 时，在 Run Configuration → Environment variables 里设置以上变量即可。

首次启动会自动：**建库 → 执行 `schema.sql` 建表 → 灌入种子数据**（2 个账号 + 4 个分类 + 12 道样例题目），无需手动建表。

### 3. 启动

```bash
# Windows（项目已包含 mvnw 包装器，无需单独安装 Maven，但需要设置 JAVA_HOME）
set JAVA_HOME=d:\program files\java\jdk21
mvnw.cmd spring-boot:run
```

> 如果系统未安装 `mvn`，直接用项目根目录的 `mvnw.cmd`（或 `./mvnw`）即可，但**必须正确设置 `JAVA_HOME`**。

启动成功后：

- **接口文档（Knife4j）**：http://localhost:8080/doc.html
- **服务地址**：http://localhost:8080 ，统一前缀 `/api/v1`

### 4. 默认账号

| 角色 | 用户名 | 密码 |
| :--- | :--- | :--- |
| 管理员 | `admin` | `admin123` |
| 学生 | `student` | `123456` |

### 5. AI 生成题目配置（可选）

如需使用「AI 生成题目」功能，通过环境变量注入 DeepSeek API Key：

```bash
set LLM_API_KEY=sk-你的真实Key        # Windows cmd
$env:LLM_API_KEY="sk-你的真实Key"     # PowerShell
```

未配置 Key 时，其余功能不受影响，仅 AI 生成题目不可用（接口会返回明确提示）。**请勿将真实 Key 写入 `application.yml` 提交到仓库。**

---

## 五、接口概览

统一前缀 `/api/v1`；除登录与前台只读接口外，均需在请求头携带 `Authorization: Bearer <token>`。标注「管理员」的接口要求 `role=0`。

### 用户 `/user`
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| POST | `/user/login` | 登录，返回 token 与用户信息 |
| GET | `/user/info` | 当前登录用户信息 |
| POST | `/user/logout` | 退出登录 |

### 轮播图 `/banner`
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/banner/list` | 分页列表（管理员） |
| GET | `/banner/enabled` | 已启用的轮播图（公开） |
| GET | `/banner/{id}` | 详情 |
| POST | `/banner` | 新增（管理员） |
| PUT | `/banner/{id}` | 修改（管理员） |
| PATCH | `/banner/{id}/status?status=` | 切换状态（管理员） |
| DELETE | `/banner/{id}` | 删除（管理员） |

### 公告 `/notice`
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/notice/list` | 分页列表（管理员） |
| GET | `/notice/enabled` | 已启用公告（公开） |
| GET | `/notice/latest?limit=` | 最新 N 条（公开） |
| GET | `/notice/{id}` | 详情 |
| POST / PUT / PATCH / DELETE | 增删改 + 状态切换（管理员） | |

### 分类 `/category`
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/category/list` | 分类列表（含题目数统计） |
| GET | `/category/tree` | 树形结构 |
| POST / PUT / DELETE | 增删改（管理员，删除前校验是否被题目引用） | |

### 题目 `/question`
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| POST | `/question/page` | 复杂条件分页（分类/难度/题型/题干模糊） |
| GET | `/question/hot?limit=` | 热门题目（公开，按浏览量降序） |
| GET | `/question/{id}` | 详情（浏览量 +1） |
| POST / PUT / DELETE | 增删改（管理员） | |
| POST | `/question/excel/preview` | 预览上传的 Excel（管理员） |
| POST | `/question/excel/import` | 批量导入 Excel（管理员） |
| GET | `/question/template` | 下载 Excel 导入模板（公开） |
| POST | `/question/ai/generate` | AI 生成题目（管理员，DeepSeek） |

### 试卷与考试 `/exam`
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/exam/list` | 试卷分页列表 |
| GET | `/exam/{id}` | 试卷详情（含答案，管理员） |
| POST | `/exam` | 手动创建试卷（管理员） |
| POST | `/exam/ai/compose` | 智能组卷（管理员，规则抽题） |
| PUT | `/exam/{id}` | 更新试卷（管理员） |
| PATCH | `/exam/{id}/status?status=` | 发布/停止（管理员） |
| DELETE | `/exam/{id}` | 删除（管理员） |
| POST | `/exam/{id}/start` | 开始考试（生成记录，返回题目**不含答案**） |
| POST | `/exam/{id}/submit` | 提交答案 `{recordId, answers}`，自动判分 |

### 记录与排行 `/record`
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/record/list` | 当前用户的考试记录 |
| GET | `/record/{id}` | 记录详情（含逐题答题明细） |
| DELETE | `/record/{id}` | 删除记录（仅本人） |
| GET | `/record/ranking` | 成绩排行（按总分、正确数降序） |
| GET | `/record/admin/list?page=&size=&userId=&examId=` | 查看所有学生记录（管理员） |
| GET | `/record/admin/{id}` | 查看任意记录详情（管理员） |
| DELETE | `/record/admin/{id}` | 删除任意记录（管理员） |

### 文件 `/file`
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| POST | `/file/upload` | 上传图片，返回可访问 URL `/upload/xxx`（管理员） |

### 数据统计 `/stats`
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/stats/dashboard` | 后台数据概览：题目/试卷/用户/记录/分类总数 + 难度/分类分布（管理员） |

---

## 六、核心业务逻辑

### 智能组卷（`POST /exam/ai/compose`）

入参示例：

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

- 按每条规则的 `分类 + 难度 + 数量` 随机抽题；
- 指定难度抽不足时自动放宽难度补齐，仍不足则按实际数量组卷；
- 组卷结果存为草稿（`status=0`），需发布后才能开考。

### 自动判分（`POST /exam/{id}/submit`）

- 纯选择题按标准答案逐题匹配，多选按「排序后字母集合相等」判定；
- 每题分值 = 总分 ÷ 题目数；自动计算 `score`、`correctCount`、`wrongCount`；
- 提交后记录状态置为「已交卷」，不可重复提交。

### AI 生成题目（`POST /question/ai/generate`）

- 入参 `{knowledgePoint, count, difficulty?, categoryId?}`，调用 DeepSeek 根据知识点生成单选题；
- 返回题目数组（**不落库**），管理员确认后可调 `POST /question` 逐个保存；
- 需在 `application.yml` 配置 `llm.api-key`。

---

## 七、数据库表

| 表名 | 说明 | 关键字段 |
| :--- | :--- | :--- |
| `sys_user` | 用户 | username(唯一)、password(BCrypt)、role(0管理/1学生)、status |
| `banner` | 轮播图 | title、image_url、link_url、sort_order、status |
| `notice` | 公告 | title、content、type(1通知/2警告/3活动)、status |
| `category` | 分类 | name、parent_id(树形)、sort_order |
| `question` | 题目 | category_id、stem、options(JSON数组)、answer、analysis、difficulty、view_count、type(0单选/1多选) |
| `exam` | 试卷 | name、total_score、duration、question_ids(JSON数组)、status(0草稿/1发布/2结束) |
| `exam_record` | 考试记录 | user_id、exam_id、answers(JSON对象)、score、correct_count、wrong_count、status(0进行中/1已交卷) |

> `options` / `answers` / `question_ids` 以 JSON 字符串形式存于 `TEXT` 列，由应用层（Jackson）负责序列化与解析，避免 MySQL JSON 类型的空串/校验问题。

---

## 八、说明与约定

- **统一返回格式**：`{"code":200,"msg":"ok","data":...}`，异常由全局处理器统一转为该格式。
- **鉴权**：登录接口与前台只读接口（`banner/enabled`、`notice/enabled`、`notice/latest`、`question/hot`、`question/template`）无需 token，其余均需 token。
- **Excel 导入模板**表头：题干、选项A~D、答案、解析、难度、分类（分类名不存在时自动创建）。
- **AI 范围**：「智能组卷」用规则算法（零外部依赖）；「AI 生成题目」调用 DeepSeek 真实生成（需配置 `llm.api-key`）；「AI 批阅」未实现（纯选择题用标准答案自动判分）。

---

## 九、常见问题

1. **`mvnw.cmd` 报 `JAVA_HOME is not defined correctly`**：请先 `set JAVA_HOME=<你的JDK路径>`。
2. **启动报 `Access denied for user 'root'`**：环境变量 `MYSQL_PASSWORD` 未设置或与你的 MySQL 密码不一致，设置正确密码后重启。
3. **端口 8080 被占用**：修改 `application.yml` 中 `server.port`。
