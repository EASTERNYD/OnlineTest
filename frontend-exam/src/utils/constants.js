// 与后端《Request.md》第 2 节数据字典保持一致

/** 用户角色 */
export const ROLE = { ADMIN: 0, STUDENT: 1 }
export const ROLE_TEXT = { 0: '管理员', 1: '学生' }

/** 题目类型 */
export const QUESTION_TYPE = { SINGLE: 0, MULTIPLE: 1 }
export const QUESTION_TYPE_TEXT = { 0: '单选', 1: '多选' }

/** 题目难度（1 简单 / 2 中等 / 3 困难） */
export const DIFFICULTY_TEXT = { 1: '简单', 2: '中等', 3: '困难' }
export const DIFFICULTY_TAG = { 1: 'success', 2: 'warning', 3: 'danger' }

/** 试卷状态（0 草稿 / 1 已发布 / 2 已结束） */
export const EXAM_STATUS = { DRAFT: 0, PUBLISHED: 1, FINISHED: 2 }
export const EXAM_STATUS_TEXT = { 0: '草稿', 1: '已发布', 2: '已结束' }
export const EXAM_STATUS_TAG = { 0: 'info', 1: 'success', 2: 'warning' }

/** 考试记录状态（0 进行中 / 1 已交卷） */
export const RECORD_STATUS_TEXT = { 0: '进行中', 1: '已交卷' }

/** 公告类型（1 通知 / 2 警告 / 3 活动） */
export const NOTICE_TYPE_TEXT = { 1: '通知', 2: '警告', 3: '活动' }
export const NOTICE_TYPE_TAG = { 1: 'primary', 2: 'danger', 3: 'success' }

/** 公告状态（0 草稿 / 1 发布） */
export const NOTICE_STATUS_TEXT = { 0: '草稿', 1: '发布' }

/** 轮播图状态（0 禁用 / 1 启用） */
export const BANNER_STATUS_TEXT = { 0: '禁用', 1: '启用' }

/** localStorage 键名 */
export const TOKEN_KEY = 'exam_token'
export const USER_KEY = 'exam_user'
