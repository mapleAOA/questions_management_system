<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/services'
import { ROLE_OPTIONS } from '@/constants/enums'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)

const createVisible = ref(false)
const createLoading = ref(false)
const createForm = reactive({
  username: '',
  password: '',
  displayName: '',
  email: '',
  status: 1,
  role: 'STUDENT',
})

const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  id: undefined,
  displayName: '',
  email: '',
  status: 1,
})

const roleVisible = ref(false)
const roleLoading = ref(false)
const roleForm = reactive({
  id: undefined,
  role: 'STUDENT',
})

async function loadData() {
  loading.value = true
  try {
    const data = await adminApi.pageUsers(page.value, size.value)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载用户失败')
  } finally {
    loading.value = false
  }
}

function resetCreateForm() {
  createForm.username = ''
  createForm.password = ''
  createForm.displayName = ''
  createForm.email = ''
  createForm.status = 1
  createForm.role = 'STUDENT'
}

async function createUser() {
  createLoading.value = true
  try {
    await adminApi.createUser({
      username: createForm.username,
      password: createForm.password,
      displayName: createForm.displayName || null,
      email: createForm.email || null,
      status: createForm.status,
      role: createForm.role,
    })
    ElMessage.success('用户创建成功')
    createVisible.value = false
    resetCreateForm()
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '创建失败')
  } finally {
    createLoading.value = false
  }
}

function openEdit(row) {
  editForm.id = row.id
  editForm.displayName = row.displayName
  editForm.email = row.email
  editForm.status = row.status
  editVisible.value = true
}

async function saveEdit() {
  editLoading.value = true
  try {
    await adminApi.updateUser(editForm.id, {
      displayName: editForm.displayName,
      email: editForm.email,
      status: editForm.status,
    })
    ElMessage.success('用户信息已更新')
    editVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    editLoading.value = false
  }
}

function openRole(row) {
  roleForm.id = row.id
  roleForm.role = row.role || 'STUDENT'
  roleVisible.value = true
}

async function saveRole() {
  roleLoading.value = true
  try {
    await adminApi.updateUserRole(roleForm.id, roleForm.role)
    ElMessage.success('角色更新成功')
    roleVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '更新角色失败')
  } finally {
    roleLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <el-card class="page-card">
    <h3 class="card-title">用户管理</h3>
    <div class="page-toolbar">
      <el-button type="success" @click="createVisible = true">新建用户</el-button>
      <el-button type="primary" @click="loadData">刷新</el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="displayName" label="显示名" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="role" label="角色" width="120" />
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="openRole(row)">角色</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="table-pager"
      background
      layout="total, sizes, prev, pager, next"
      :current-page="page"
      :page-size="size"
      :page-sizes="[10, 20, 50]"
      :total="total"
      @size-change="(v) => { size = v; page = 1; loadData() }"
      @current-change="(v) => { page = v; loadData() }"
    />
  </el-card>

  <el-dialog v-model="createVisible" title="新建用户" width="620px">
    <el-form label-width="90px">
      <el-form-item label="用户名">
        <el-input v-model="createForm.username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="createForm.password" type="password" show-password />
      </el-form-item>
      <el-form-item label="显示名">
        <el-input v-model="createForm.displayName" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="createForm.email" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="createForm.status" style="width: 100%">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="createForm.role" style="width: 100%">
          <el-option
            v-for="role in ROLE_OPTIONS"
            :key="role.value"
            :label="role.label"
            :value="role.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" :loading="createLoading" @click="createUser">创建</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="editVisible" title="编辑用户信息" width="560px">
    <el-form label-width="90px">
      <el-form-item label="用户ID">
        <el-input v-model="editForm.id" disabled />
      </el-form-item>
      <el-form-item label="显示名">
        <el-input v-model="editForm.displayName" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="editForm.email" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="editForm.status" style="width: 100%">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="editLoading" @click="saveEdit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="roleVisible" title="设置用户角色" width="540px">
    <el-form label-width="90px">
      <el-form-item label="用户ID">
        <el-input v-model="roleForm.id" disabled />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="roleForm.role" style="width: 100%">
          <el-option
            v-for="role in ROLE_OPTIONS"
            :key="role.value"
            :label="role.label"
            :value="role.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="roleVisible = false">取消</el-button>
      <el-button type="primary" :loading="roleLoading" @click="saveRole">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
