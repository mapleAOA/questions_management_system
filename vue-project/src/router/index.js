import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/components/AppLayout.vue'
import { pinia } from '@/stores'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    component: AppLayout,
    children: [
      { path: '', redirect: { name: 'dashboard' } },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '首页' },
      },

      {
        path: 'assignments/my',
        name: 'student-assignments',
        component: () => import('@/views/StudentAssignmentsView.vue'),
        meta: { roles: ['STUDENT'], title: '我的作业' },
      },
      {
        path: 'question-bank',
        name: 'student-question-bank',
        component: () => import('@/views/StudentAssignmentsView.vue'),
        meta: { roles: ['STUDENT'], title: '题库练习' },
      },
      {
        path: 'classes/my',
        name: 'student-classes',
        component: () => import('@/views/StudentClassesView.vue'),
        meta: { roles: ['STUDENT'], title: '我的班级' },
      },
      {
        path: 'attempts/history',
        name: 'attempt-history',
        component: () => import('@/views/AttemptHistoryView.vue'),
        meta: { roles: ['STUDENT'], title: '作答记录' },
      },
      {
        path: 'attempts/:attemptId/work',
        name: 'attempt-work',
        component: () => import('@/views/AttemptWorkView.vue'),
        meta: { roles: ['STUDENT'], title: '作答中' },
      },
      {
        path: 'attempts/:attemptId/result',
        name: 'attempt-result',
        component: () => import('@/views/AttemptResultView.vue'),
        meta: { roles: ['STUDENT'], title: '作答结果' },
      },
      {
        path: 'stats',
        name: 'stats',
        component: () => import('@/views/StatsView.vue'),
        meta: { roles: ['STUDENT'], title: '学习统计' },
      },
      {
        path: 'appeals/my',
        name: 'my-appeals',
        component: () => import('@/views/StudentAppealsView.vue'),
        meta: { roles: ['STUDENT'], title: '我的申诉' },
      },

      {
        path: 'questions',
        name: 'questions',
        component: () => import('@/views/QuestionManageView.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'], title: '题目管理' },
      },
      {
        path: 'tags',
        name: 'tags',
        component: () => import('@/views/TagManageView.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'], title: '标签管理' },
      },
      {
        path: 'papers',
        name: 'papers',
        component: () => import('@/views/PaperManageView.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'], title: '试卷管理' },
      },
      {
        path: 'assignments/manage',
        name: 'manage-assignments',
        component: () => import('@/views/AssignmentManageView.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'], title: '作业管理' },
      },
      {
        path: 'classes/manage',
        name: 'manage-classes',
        component: () => import('@/views/TeacherClassesView.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'], title: '班级管理' },
      },
      {
        path: 'teacher/review',
        name: 'teacher-review',
        component: () => import('@/views/TeacherReviewView.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'], title: '复核中心' },
      },
      {
        path: 'llm/calls',
        name: 'llm-calls',
        component: () => import('@/views/LlmCallsView.vue'),
        meta: { roles: ['TEACHER', 'ADMIN'], title: 'LLM 调用记录' },
      },

      {
        path: 'admin/users',
        name: 'admin-users',
        component: () => import('@/views/AdminUsersView.vue'),
        meta: { roles: ['ADMIN'], title: '用户管理' },
      },
      {
        path: 'admin/logs',
        name: 'admin-logs',
        component: () => import('@/views/AdminLogsView.vue'),
        meta: { roles: ['ADMIN'], title: '系统日志' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { public: true, title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const auth = useAuthStore(pinia)
  const requiresAuth = !to.meta.public

  if (!requiresAuth) {
    if (to.name === 'login' && auth.isLoggedIn) {
      return { name: 'dashboard' }
    }
    return true
  }

  if (!auth.token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  try {
    await auth.ensureProfile()
  } catch {
    auth.clearSession()
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  const requiredRoles = Array.isArray(to.meta.roles) ? to.meta.roles : []
  if (requiredRoles.length > 0 && !auth.hasAnyRole(requiredRoles)) {
    return { name: 'dashboard' }
  }
  return true
})

router.afterEach((to) => {
  const title = to.meta?.title ? `${to.meta.title} - 题库系统` : '题库系统'
  document.title = title
})

export default router
