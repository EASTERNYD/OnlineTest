# 智能在线考试系统 — 前端（frontend-exam）

Vue 3 + Element Plus + Vue Router + Pinia + Axios + Vite

对接后端接口文档见 `../backend-exam/Request.md`，交付范围见 `../backend-exam/Target.md`。

## 启动

```bash
npm install
npm run dev        # http://localhost:5173
```

后端需先启动（Spring Boot，默认 `http://localhost:8080`）。开发环境已配置 Vite 代理：

- `/api` → `http://localhost:8080`
- `/upload` → `http://localhost:8080`（后端上传的图片相对路径直接可用，无需手动拼接域名）

## 联调账号

| 角色 | 账号 | 密码 |
| :--- | :--- | :--- |
| 管理员 | admin | admin123 |
| 学生 | student | 123456 |

## 目录结构

```
src/
├── api/            # 接口封装（request.js 为 axios 实例 + 拦截器）
├── components/     # UploadImage / RichText / AnswerCard
├── layout/         # StudentLayout（前台）/ AdminLayout（后台）
├── router/         # 路由 + 角色守卫
├── store/          # Pinia：user / app / exam
├── utils/          # constants（数据字典）/ format（时间格式化）
└── views/
    ├── login/      # 登录页
    ├── student/    # 首页/公告/题库/考试/答题/成绩/排行
    └── admin/      # 概览/轮播/公告/分类/题目/导入/试卷/记录
```

## 已实现的对接要点

- **鉴权**：请求拦截器统一携带 `Authorization: Bearer <token>`；响应拦截器同时处理「HTTP 200 + 业务 code」与「HTTP 401/403」（后端 JWT 拦截器直接返回 HTTP 状态码）两种错误形态
- **角色守卫**：`/admin/*` 仅 role=0 可进；学生误入重定向 `/home`，管理员访问学生页重定向后台
- **options 双形态**：响应为数组，新增/编辑请求体自动转为 JSON 字符串
- **考试流程**：`start` 拿 recordId + 题目（无答案）→ 倒计时 → 交卷 `{recordId, answers}` → 跳转成绩详情；多选答案按字母排序拼接；时间到自动交卷；`beforeunload` 离开提醒
- **AI 生成题目**：独立 180s 长超时（DeepSeek 同步调用耗时 1-2 分钟）；后端 `llm.api-key` 未配置时接口会报错，页面已有错误兜底
- **Excel 导入**：模板下载（blob）→ 预览（分页）→ 确认导入

## 已知边界（后端接口现状所致）

1. **考试无续考接口**：答题中刷新页面会丢失答题状态（Pinia 非持久化），需重新开考（会生成新记录）；误触返回后短时间内再进入可恢复（内存态仍在）
2. **排行榜**：后端 `GET /record/ranking` 无筛选参数、无 `startTime`，页面按试卷名前端过滤，"用时"无法精确计算故未展示
3. **智能组卷**：后端为规则式 `{categoryId, count, difficulty?}`，页面按规则式交互实现
4. **AI 生成仅单选**：后端 prompt 固定生成 4 选项单选题
5. **富文本**：RichText 为轻量 contenteditable 实现（加粗/斜体/下划线），公告内容以 HTML 存储、`v-html` 渲染；如需更强编辑器可替换为 wangeditor 等库
