<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { classApi } from '@/api/services'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const list = ref([])

const createVisible = ref(false)
const createLoading = ref(false)
const createForm = reactive({
  className: '',
  classDesc: '',
})

const studentVisible = ref(false)
const studentLoading = ref(false)
const studentClass = ref(null)
const students = ref([])

function resetCreateForm() {
  createForm.className = ''
  createForm.classDesc = ''
}

async function loadClasses() {
  loading.value = true
  try {
    const data = await classApi.mine()
    list.value = data || []
  } catch (error) {
    ElMessage.error(error.message || '加载班级失败')
  } finally {
    loading.value = false
  }
}

async function createClass() {
  createLoading.value = true
  try {
    await classApi.create({
      className: createForm.className,
      classDesc: createForm.classDesc || null,
    })
    ElMessage.success('班级创建成功')
    createVisible.value = false
    resetCreateForm()
    loadClasses()
  } catch (error) {
    ElMessage.error(error.message || '创建班级失败')
  } finally {
    createLoading.value = false
  }
}

async function openStudents(row) {
  studentVisible.value = true
  studentClass.value = row
  studentLoading.value = true
  try {
    const data = await classApi.students(row.id)
    students.value = data || []
  } catch (error) {
    ElMessage.error(error.message || '加载学生失败')
    students.value = []
  } finally {
    studentLoading.value = false
  }
}

async function removeStudent(row) {
  if (!studentClass.value?.id || !row?.studentId) {
    return
  }
  try {
    await ElMessageBox.confirm(`确认将学生 ${row.displayName || row.username || row.studentId} 移出班级？`, '提示', {
      type: 'warning',
    })
    await classApi.kickStudent(studentClass.value.id, row.studentId)
    ElMessage.success('已移出班级')
    await openStudents(studentClass.value)
    await loadClasses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '移出失败')
    }
  }
}

onMounted(loadClasses)
</script>

<template>
  <el-card class="page-card">
    <h3 class="card-title">班级管理</h3>
    <div class="page-toolbar">
      <el-button type="success" @click="createVisible = true">新建班级</el-button>
      <el-button type="primary" @click="loadClasses">刷新</el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="班级ID" width="90" />
      <el-table-column prop="className" label="班级名称" min-width="180" />
      <el-table-column prop="classCode" label="班级码" width="140" />
      <el-table-column prop="classDesc" label="描述" min-width="220" />
      <el-table-column prop="studentCount" label="学生数" width="100" />
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openStudents(row)">学生名单</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="createVisible" title="新建班级" width="620px">
    <el-form label-width="100px">
      <el-form-item label="班级名称">
        <el-input v-model="createForm.className" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="createForm.classDesc" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" :loading="createLoading" @click="createClass">创建</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="studentVisible" :title="`学生列表 - ${studentClass?.className || ''}`" width="760px">
    <div class="class-code-line">
      班级码：<el-tag type="warning">{{ studentClass?.classCode || '-' }}</el-tag>
    </div>
    <el-table :data="students" border v-loading="studentLoading">
      <el-table-column prop="studentId" label="学生ID" width="110" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="displayName" label="显示名" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="加入时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.joinedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="removeStudent(row)">移出</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<style scoped>
.class-code-line {
  margin-bottom: 12px;
  color: #44566c;
}
</style>
