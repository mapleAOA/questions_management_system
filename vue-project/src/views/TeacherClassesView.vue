<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { classApi } from '@/api/services'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'

const auth = useAuthStore()
const isAdmin = computed(() => auth.hasAnyRole(['ADMIN']))

const loading = ref(false)
const list = ref([])
const teacherOptions = ref([])

const editorVisible = ref(false)
const saveLoading = ref(false)
const editingId = ref(null)
const form = reactive({
  className: '',
  classDesc: '',
  classCode: '',
  teacherId: undefined,
})

const studentVisible = ref(false)
const studentLoading = ref(false)
const studentClass = ref(null)
const students = ref([])

function resetForm() {
  editingId.value = null
  form.className = ''
  form.classDesc = ''
  form.classCode = ''
  form.teacherId = isAdmin.value ? undefined : auth.user?.id
}

async function loadClasses() {
  loading.value = true
  try {
    const data = await classApi.mine()
    list.value = Array.isArray(data) ? data : []
  } catch (error) {
    ElMessage.error(error.message || '加载班级失败')
  } finally {
    loading.value = false
  }
}

async function loadTeacherOptions() {
  if (!isAdmin.value) {
    teacherOptions.value = []
    return
  }
  try {
    const data = await classApi.teacherOptions()
    teacherOptions.value = Array.isArray(data) ? data : []
  } catch (error) {
    teacherOptions.value = []
    ElMessage.error(error.message || '加载教师列表失败')
  }
}

function openCreate() {
  resetForm()
  editorVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.className = row.className || ''
  form.classDesc = row.classDesc || ''
  form.classCode = row.classCode || ''
  form.teacherId = row.teacherId || undefined
  editorVisible.value = true
}

async function saveClass() {
  saveLoading.value = true
  try {
    const payload = {
      className: form.className,
      classDesc: form.classDesc || null,
      classCode: form.classCode || null,
      teacherId: isAdmin.value ? Number(form.teacherId || 0) || undefined : undefined,
    }
    if (editingId.value) {
      await classApi.update(editingId.value, payload)
      ElMessage.success('班级更新成功')
    } else {
      await classApi.create(payload)
      ElMessage.success('班级创建成功')
    }
    editorVisible.value = false
    await loadClasses()
  } catch (error) {
    ElMessage.error(error.message || '保存班级失败')
  } finally {
    saveLoading.value = false
  }
}

async function removeClass(row) {
  try {
    await ElMessageBox.confirm(`确认删除班级“${row.className}”？`, '提示', { type: 'warning' })
    await classApi.remove(row.id)
    ElMessage.success('班级已删除')
    await loadClasses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除班级失败')
    }
  }
}

async function openStudents(row) {
  studentVisible.value = true
  studentClass.value = row
  studentLoading.value = true
  try {
    const data = await classApi.students(row.id)
    students.value = Array.isArray(data) ? data : []
  } catch (error) {
    students.value = []
    ElMessage.error(error.message || '加载学生失败')
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

onMounted(async () => {
  await Promise.all([loadClasses(), loadTeacherOptions()])
})
</script>

<template>
  <el-card class="page-card">
    <div class="card-head">
      <div>
        <h3 class="card-title">班级管理</h3>
        <p class="muted">{{ isAdmin ? '当前为管理员视角，可查看并维护全部班级。' : '当前为教师视角，可维护自己的班级。' }}</p>
      </div>
      <div class="page-toolbar">
        <el-button type="success" @click="openCreate">新建班级</el-button>
        <el-button type="primary" @click="loadClasses">刷新</el-button>
      </div>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="班级ID" width="90" />
      <el-table-column prop="className" label="班级名称" min-width="180" />
      <el-table-column label="教师" min-width="140">
        <template #default="{ row }">{{ row.teacherName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="classCode" label="班级码" width="140" />
      <el-table-column prop="classDesc" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column prop="studentCount" label="学生数" width="100" />
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="info" @click="openStudents(row)">学生名单</el-button>
          <el-button link type="danger" @click="removeClass(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="editorVisible" :title="editingId ? '编辑班级' : '新建班级'" width="640px">
    <el-form label-width="100px">
      <el-form-item label="班级名称">
        <el-input v-model="form.className" />
      </el-form-item>
      <el-form-item v-if="isAdmin" label="负责教师">
        <el-select v-model="form.teacherId" filterable style="width: 100%" placeholder="请选择教师">
          <el-option
            v-for="teacher in teacherOptions"
            :key="teacher.id"
            :label="teacher.displayName || teacher.username"
            :value="teacher.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="班级码">
        <el-input v-model="form.classCode" placeholder="留空则自动生成" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.classDesc" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editorVisible = false">取消</el-button>
      <el-button type="primary" :loading="saveLoading" @click="saveClass">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="studentVisible" :title="`学生列表 - ${studentClass?.className || ''}`" width="760px">
    <div class="student-meta">
      <span>班级码：<el-tag type="warning">{{ studentClass?.classCode || '-' }}</el-tag></span>
      <span>教师：{{ studentClass?.teacherName || '-' }}</span>
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
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}

.student-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 12px;
  color: #44566c;
  flex-wrap: wrap;
}
</style>