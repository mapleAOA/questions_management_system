<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const shortcuts = computed(() => {
  const all = [
    { title: '我的作业', desc: '查看并开始作答', path: '/assignments/my', roles: ['STUDENT'] },
    { title: '题库练习', desc: '从题库选题并开始练习', path: '/question-bank', roles: ['STUDENT'] },
    { title: '我的班级', desc: '输入班级码加入班级', path: '/classes/my', roles: ['STUDENT'] },
    { title: '作答记录', desc: '查看练习和作业结果', path: '/attempts/history', roles: ['STUDENT'] },
    { title: '学习统计', desc: '错题、掌握度、能力值', path: '/stats', roles: ['STUDENT'] },
    { title: '我的申诉', desc: '提交成绩申诉', path: '/appeals/my', roles: ['STUDENT'] },

    { title: '题目管理', desc: '题目检索与编辑', path: '/questions', roles: ['TEACHER', 'ADMIN'] },
    { title: '标签管理', desc: '树形标签维护', path: '/tags', roles: ['TEACHER', 'ADMIN'] },
    { title: '试卷管理', desc: '组卷与快照', path: '/papers', roles: ['TEACHER', 'ADMIN'] },
    { title: '作业管理', desc: '发布与截止控制', path: '/assignments/manage', roles: ['TEACHER', 'ADMIN'] },
    { title: '班级管理', desc: '创建班级并查看学生', path: '/classes/manage', roles: ['TEACHER', 'ADMIN'] },
    { title: '复核中心', desc: '人工复核与申诉处理', path: '/teacher/review', roles: ['TEACHER', 'ADMIN'] },
    { title: '大模型记录', desc: '查看每次调用证据', path: '/llm/calls', roles: ['TEACHER', 'ADMIN'] },

    { title: '用户管理', desc: '管理员维护用户和角色', path: '/admin/users', roles: ['ADMIN'] },
    { title: '系统日志', desc: '登录日志', path: '/admin/logs', roles: ['ADMIN'] },
  ]
  return all.filter((item) => auth.hasAnyRole(item.roles))
})
</script>

<template>
  <div class="dashboard-root">
    <div class="summary-grid">
      <el-card class="page-card">
        <h3 class="card-title">当前用户</h3>
        <div class="summary-value">{{ auth.displayName }}</div>
        <p class="muted">{{ auth.role || '-' }}</p>
      </el-card>
    </div>

    <el-card class="page-card">
      <h3 class="card-title">快捷入口</h3>
      <div class="shortcut-grid">
        <div v-for="item in shortcuts" :key="item.path" class="shortcut-item" @click="router.push(item.path)">
          <h4>{{ item.title }}</h4>
          <p>{{ item.desc }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard-root {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.summary-value {
  font-size: 20px;
  font-weight: 700;
  color: #0b3954;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 12px;
}

.shortcut-item {
  border: 1px solid #d7e4f3;
  background: linear-gradient(150deg, #f9fcff, #f2f8ff);
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.shortcut-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(11, 57, 84, 0.1);
  border-color: #94bde1;
}

.shortcut-item h4 {
  margin: 0 0 6px;
  color: #0b3954;
}

.shortcut-item p {
  margin: 0;
  color: #486581;
  font-size: 13px;
}

@media (max-width: 1100px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
