<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { classApi } from '@/api/services'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const joining = ref(false)
const classCode = ref('')
const list = ref([])

async function loadClasses() {
  loading.value = true
  try {
    const data = await classApi.my()
    list.value = data || []
  } catch (error) {
    ElMessage.error(error.message || '加载班级失败')
  } finally {
    loading.value = false
  }
}

async function joinClass() {
  const code = classCode.value.trim().toUpperCase()
  if (!code) {
    ElMessage.warning('请输入班级码')
    return
  }

  joining.value = true
  try {
    await classApi.join(code)
    ElMessage.success('加入班级成功')
    classCode.value = ''
    loadClasses()
  } catch (error) {
    ElMessage.error(error.message || '加入班级失败')
  } finally {
    joining.value = false
  }
}

onMounted(loadClasses)
</script>

<template>
  <el-card class="page-card">
    <h3 class="card-title">加入班级</h3>
    <div class="join-line">
      <el-input v-model="classCode" maxlength="12" placeholder="请输入班级码" class="code-input" @keyup.enter="joinClass" />
      <el-button type="primary" :loading="joining" @click="joinClass">加入</el-button>
      <el-button @click="loadClasses">刷新</el-button>
    </div>
  </el-card>

  <el-card class="page-card">
    <h3 class="card-title">我的班级</h3>
    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="className" label="班级名称" min-width="180" />
      <el-table-column prop="classCode" label="班级码" width="140" />
      <el-table-column prop="teacherName" label="教师" width="140" />
      <el-table-column prop="classDesc" label="描述" min-width="220" />
      <el-table-column label="加入时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.joinedAt) }}</template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.join-line {
  display: flex;
  gap: 10px;
}

.code-input {
  max-width: 360px;
}
</style>
