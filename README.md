# 智能在线考试系统

![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![JDK](https://img.shields.io/badge/JDK-21-E76F00?logo=openjdk&logoColor=white)

一套**前后端分离**的智能在线考试系统，面向**行测（行政职业能力测验）**等标准化机考场景：管理员维护题库、智能组卷、发布考试；学生在线答题，交卷即自动判分，成绩与排行实时可查。试卷仅支持**单项选择题**。

> 📄 [项目介绍与需求总览](INTRO.md) ｜ 🏗 [项目总文档（架构与设计）](PROJECT.md) ｜ 🔌 [后端接口文档](backend-exam/Request.md)

---

## ✨ 功能特性

**🎓 学生端** —— 首页（轮播图/公告/热门题）· 公告浏览 · 题库练习（分类/难度/关键词筛选）· 在线考试（倒计时 + 答题卡 + 到时自动交卷）· 成绩明细（逐题对错 + 解析）· 排行榜

**🛠 管理端** —— 数据概览（统计卡片 + 分布图表）· 轮播图管理（图片上传）· 公告管理（富文本 + 发布/草稿）· 分类管理（树形）· 题目管理 · Excel 批量导入 · 试卷管理（手动/智能组卷 + 发布流转）· 全体考生记录管理

**🤖 智能能力**

| 能力 | 说明 |
| :--- | :--- |
| AI 生成题目 | 调用 DeepSeek 大模型，按知识点/难度/数量批量出题，勾选入库 |
| 智能组卷 | 规则随机抽题（分类 + 难度 + 数量），抽不足自动放宽难度 |
| 自动判分 | 交卷瞬间对照标准答案评分，零人工、零延迟 |

## 🛠 技术栈

| 端 | 技术 |
| :--- | :--- |
| 前端 | Vue 3（Composition API）、Element Plus、Vue Router、Pinia、Axios、Vite |
| 后端 | Java 21、Spring Boot 4.1.1、MyBatis、MySQL 8.0、JWT、BCrypt、EasyExcel、Knife4j |
| AI | DeepSeek（OpenAI 兼容协议，RestClient 直调） |

## 📁 项目结构

```
OnlineTest/
├── backend-exam/        # 后端：Spring Boot RESTful API（端口 8080）
│   └── src/main/java/com/example/demo/
│       ├── controller/  #   9 个控制器（User/Banner/Notice/Category/Question/Exam/Record/File/Stats）
│       ├── service/     #   业务层（组卷抽题、自动判分、Excel、AI 出题）
│       ├── mapper/      #   MyBatis 纯注解接口
│       └── ...          #   entity / dto / vo / common / config / util
└── frontend-exam/       # 前端：Vue 3 SPA（端口 5173）
    └── src/
        ├── views/       #   登录页 + 学生端 8 页 + 管理端 8 页
        ├── api/         #   接口封装（axios 拦截器统一鉴权与错误处理）
        ├── store/       #   Pinia（user / exam / app）
        └── ...          #   router / layout / components / utils
```

## 🚀 快速开始

### 环境要求

| 依赖 | 版本 | 说明 |
| :--- | :--- | :--- |
| JDK | 21 | 需正确设置 `JAVA_HOME` |
| MySQL | 8.0 | 服务需先启动；库表与种子数据自动初始化，**无需手工建库** |
| Node.js | ≥ 20 | 前端构建 |
| DeepSeek API Key | 可选 | 仅「AI 生成题目」功能需要 |

### 1. 启动后端

```bash
cd backend-exam
```

首次启动前配置数据库密码（推荐环境变量，避免真实密码进入版本库）：

```bash
# Windows cmd（当前会话有效）
set MYSQL_PASSWORD=你的MySQL密码
# PowerShell
$env:MYSQL_PASSWORD="你的MySQL密码"
```

可选：`LLM_API_KEY`（DeepSeek Key，AI 出题用）、`JWT_SECRET`（生产必换）、`FILE_UPLOAD_DIR`（上传目录，默认 `backend-exam/upload`）。也可直接修改 `application.yml` 中的默认值，但**请勿把真实密钥提交到仓库**。

启动（二选一）：

```bash
mvnw.cmd spring-boot:run      # 命令行（Linux/macOS 用 ./mvnw）
# 或在 IDEA 中直接运行 DemoApplication
```

启动成功标志：日志出现 `Tomcat started on port 8080`；接口文档 → http://localhost:8080/doc.html

### 2. 启动前端

```bash
cd frontend-exam
npm install    # 仅首次
npm run dev
```

浏览器访问终端输出的地址（默认 http://localhost:5173 ）。

> 💡 停止前端：在运行 `npm run dev` 的终端按 `Ctrl + C`。若端口被残留进程占用，Vite 会自动换端口（5174…）；手动清理：`netstat -ano | findstr :5173` 查到 PID 后 `taskkill /PID <PID> /F`。
>
> 已配置 Vite 代理：`/api`、`/upload` 自动转发到 `http://localhost:8080`，无跨域问题。

### 3. 登录

| 角色 | 账号 | 密码 | 进入 |
| :--- | :--- | :--- | :--- |
| 管理员 | `admin` | `admin123` | 管理后台 |
| 学生 | `student` | `123456` | 学生前台 |

## 📖 文档索引

| 文档 | 内容 |
| :--- | :--- |
| [INTRO.md](INTRO.md) | 项目介绍与需求总览：定位、项目结构、全部页面功能需求、数据模型与业务规则 |
| [PROJECT.md](PROJECT.md) | 项目总文档：架构、前后端设计详述、业务流程、配置约定、已知边界 |
| [backend-exam/Request.md](backend-exam/Request.md) | 后端接口文档（对接权威）：全局约定、数据字典、表结构、接口详设 |
| [backend-exam/Target.md](backend-exam/Target.md) | 前端交付文档：页面→接口映射、交付清单、实现边界 |
| [backend-exam/README.md](backend-exam/README.md) | 后端项目说明：结构、算法、配置、FAQ |
| [frontend-exam/README.md](frontend-exam/README.md) | 前端运行手册：启动、目录、对接要点 |

## ⚠️ 注意事项

- **敏感配置**：`application.yml` 中的数据库密码与 JWT secret 为开发默认值，**生产部署前必须替换**；若本仓库公开，请勿在其中提交任何真实密钥（含 DeepSeek API Key）。
- **已知边界**：AI 出题需配置真实 `llm.api-key`；考试无续考机制（答题中刷新需重新开考）；Excel 导入仅支持单选题。详见 [PROJECT.md · 已知边界](PROJECT.md)。

## 🔗 核心流程

```
管理员：备题（手动 / Excel 导入 / AI 生成）→ 组卷（手动选题 / 智能抽题）→ 发布
学生：  考试列表 → 开始考试（倒计时）→ 交卷（自动判分）→ 成绩明细 → 排行榜
```
