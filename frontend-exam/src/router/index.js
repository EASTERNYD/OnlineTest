import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { ROLE } from '@/utils/constants'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录' }
  },
  // ===== 学生端（前台）=====
  {
    path: '/',
    component: () => import('@/layout/StudentLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'home', component: () => import('@/views/student/HomeView.vue'), meta: { title: '首页' } },
      { path: 'notice', name: 'notice', component: () => import('@/views/student/NoticeListView.vue'), meta: { title: '公告' } },
      { path: 'notice/:id', name: 'notice-detail', component: () => import('@/views/student/NoticeDetailView.vue'), meta: { title: '公告详情' } },
      { path: 'question', name: 'question', component: () => import('@/views/student/QuestionBrowseView.vue'), meta: { title: '题库' } },
      { path: 'exam', name: 'exam', component: () => import('@/views/student/ExamListView.vue'), meta: { title: '考试' } },
      { path: 'exam/:id/do', name: 'exam-do', component: () => import('@/views/student/ExamDoView.vue'), meta: { title: '考试答题' } },
      { path: 'record', name: 'record', component: () => import('@/views/student/RecordListView.vue'), meta: { title: '我的成绩' } },
      { path: 'rank', name: 'rank', component: () => import('@/views/student/RankView.vue'), meta: { title: '排行榜' } }
    ]
  },
  // ===== 管理端（后台）=====
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue'), meta: { title: '数据概览' } },
      { path: 'banner', name: 'admin-banner', component: () => import('@/views/admin/BannerManageView.vue'), meta: { title: '轮播图管理' } },
      { path: 'notice', name: 'admin-notice', component: () => import('@/views/admin/NoticeManageView.vue'), meta: { title: '公告管理' } },
      { path: 'category', name: 'admin-category', component: () => import('@/views/admin/CategoryManageView.vue'), meta: { title: '分类管理' } },
      { path: 'question', name: 'admin-question', component: () => import('@/views/admin/QuestionManageView.vue'), meta: { title: '题目管理' } },
      { path: 'question/import', name: 'admin-question-import', component: () => import('@/views/admin/QuestionImportView.vue'), meta: { title: '题目导入' } },
      { path: 'exam', name: 'admin-exam', component: () => import('@/views/admin/ExamManageView.vue'), meta: { title: '试卷管理' } },
      { path: 'record', name: 'admin-record', component: () => import('@/views/admin/RecordManageView.vue'), meta: { title: '考试记录管理' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局守卫：
 * 1. 未登录 → /login（登录页除外）
 * 2. 学生访问 /admin/* → 提示无权限，跳回学生首页
 * 3. 管理员访问学生端页面 → 跳回后台首页（request.md 4.3）
 * 4. 已登录访问 /login → 按 role 跳对应首页
 */
router.beforeEach((to) => {
  const userStore = useUserStore()
  const role = userStore.userInfo?.role

  if (to.path === '/login') {
    if (userStore.isLoggedIn) {
      return role === ROLE.ADMIN ? '/admin/dashboard' : '/home'
    }
    return true
  }

  if (!userStore.isLoggedIn) {
    return '/login'
  }

  if (to.path.startsWith('/admin') && role !== ROLE.ADMIN) {
    ElMessage.warning('无管理员权限')
    return '/home'
  }

  if (!to.path.startsWith('/admin') && role === ROLE.ADMIN) {
    return '/admin/dashboard'
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta?.title ? `${to.meta.title} - 智能在线考试系统` : '智能在线考试系统'
})

export default router
