<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Calendar,
  CollectionTag,
  Cpu,
  DataLine,
  Document,
  EditPen,
  Histogram,
  House,
  Management,
  Notebook,
  Reading,
  SwitchButton,
  UserFilled,
  Warning,
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const menus = computed(() => {
  const base = [
    { path: '/dashboard', title: '首页', icon: House },

    { path: '/assignments/my', title: '我的作业', icon: Notebook, roles: ['STUDENT'] },
    { path: '/question-bank', title: '题库练习', icon: Reading, roles: ['STUDENT'] },
    { path: '/classes/my', title: '我的班级', icon: UserFilled, roles: ['STUDENT'] },
    { path: '/attempts/history', title: '作答记录', icon: EditPen, roles: ['STUDENT'] },
    { path: '/stats', title: '学习统计', icon: DataLine, roles: ['STUDENT'] },
    { path: '/appeals/my', title: '我的申诉', icon: Warning, roles: ['STUDENT'] },

    { path: '/questions', title: '题目管理', icon: Reading, roles: ['TEACHER', 'ADMIN'] },
    { path: '/tags', title: '标签管理', icon: CollectionTag, roles: ['ADMIN'] },
    { path: '/papers', title: '试卷管理', icon: Document, roles: ['TEACHER', 'ADMIN'] },
    { path: '/assignments/manage', title: '作业/考试管理', icon: Calendar, roles: ['TEACHER', 'ADMIN'] },
    { path: '/classes/manage', title: '班级管理', icon: UserFilled, roles: ['TEACHER', 'ADMIN'] },
    { path: '/teacher/review', title: '复核中心', icon: Management, roles: ['TEACHER', 'ADMIN'] },
    { path: '/llm/calls', title: '大模型调用记录', icon: Cpu, roles: ['TEACHER', 'ADMIN'] },

    { path: '/admin/users', title: '用户管理', icon: UserFilled, roles: ['ADMIN'] },
    { path: '/admin/logs', title: '系统日志', icon: Histogram, roles: ['ADMIN'] },
  ]
  return base.filter((item) => !item.roles || auth.hasAnyRole(item.roles))
})

const activeMenu = computed(() => {
  const m = menus.value.find((item) => route.path.startsWith(item.path))
  return m?.path || '/dashboard'
})

const roleDisplay = computed(() => roleText(auth.role))

function roleTagType(role) {
  const normalized = String(role || '').replace(/^ROLE_/, '').toUpperCase()
  if (normalized === 'ADMIN') return 'danger'
  if (normalized === 'TEACHER') return 'warning'
  return 'success'
}

function roleText(role) {
  const normalized = String(role || '').replace(/^ROLE_/, '').toUpperCase()
  if (normalized === 'ADMIN') return '管理员'
  if (normalized === 'TEACHER') return '教师'
  if (normalized === 'STUDENT') return '学生'
  return '未知角色'
}

async function handleLogout() {
  await auth.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="layout-root">
    <el-aside width="240px" class="layout-aside">
      <div class="brand">
        <div class="brand-mark">题库</div>
        <div class="brand-text">
          <div>题库系统</div>
          <small>C语言课程</small>
        </div>
      </div>
      <el-menu :default-active="activeMenu" class="layout-menu" router>
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <h2>{{ route.meta?.title || '题库系统' }}</h2>
          <p>{{ roleDisplay }}</p>
        </div>
        <div class="header-right">
          <el-tag :type="roleTagType(auth.role)" effect="dark" round>{{ roleText(auth.role) }}</el-tag>
          <span class="welcome">{{ auth.displayName }}</span>
          <el-button type="danger" plain :icon="SwitchButton" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="layout-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-root {
  min-height: 100vh;
}

.layout-aside {
  border-right: 1px solid #e5edf5;
  background:
    radial-gradient(circle at 10% 10%, #fcebd6 0, transparent 40%),
    radial-gradient(circle at 80% 20%, #dff2ff 0, transparent 35%),
    #f8fbff;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 16px 14px;
}

.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #084887, #f58a07);
  color: #fff;
  display: grid;
  place-content: center;
  font-weight: 700;
}

.brand-text {
  font-weight: 700;
  color: #073b4c;
}

.brand-text small {
  display: block;
  color: #5c748a;
  font-weight: 500;
  font-size: 12px;
}

.layout-menu {
  border-right: none;
  background: transparent;
}

.layout-header {
  min-height: 76px;
  background: linear-gradient(120deg, #0b3954 0%, #1b6ca8 45%, #f58a07 100%);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  gap: 16px;
}

.header-left h2 {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
}

.header-left p {
  margin: 4px 0 0;
  color: #dcf4ff;
  font-size: 13px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.welcome {
  font-size: 14px;
  padding: 0 4px;
}

.layout-main {
  background:
    linear-gradient(160deg, rgba(11, 57, 84, 0.04), rgba(245, 138, 7, 0.03)),
    #f7f9fc;
}
</style>
