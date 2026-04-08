<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/services'
import { formatDateTime, prettyJson } from '@/utils/format'

const activeTab = ref('login')

const detailVisible = ref(false)
const detailTitle = ref('')
const detailPayload = ref('')

const loginLoading = ref(false)
const loginList = ref([])
const loginPage = ref(1)
const loginSize = ref(10)
const loginTotal = ref(0)
const loginQuery = reactive({
  username: '',
  successFlag: undefined,
  range: [],
})

const auditLoading = ref(false)
const auditList = ref([])
const auditPage = ref(1)
const auditSize = ref(10)
const auditTotal = ref(0)
const auditQuery = reactive({
  username: '',
  action: '',
  entityType: '',
  range: [],
})

const auditActionOptions = [
  'USER_REGISTER',
  'USER_CREATE',
  'USER_UPDATE',
  'USER_ROLE_UPDATE',
  'QUESTION_CREATE',
  'QUESTION_UPDATE',
  'QUESTION_DELETE',
  'QUESTION_PUBLISH',
  'QUESTION_BANK_SUBMIT',
  'QUESTION_BANK_CANCEL',
  'QUESTION_BANK_REVIEW',
  'PAPER_CREATE',
  'PAPER_UPDATE',
  'PAPER_DELETE',
  'PAPER_ADD_QUESTION',
  'PAPER_BATCH_UPDATE',
  'PAPER_UPDATE_QUESTION',
  'PAPER_REMOVE_QUESTION',
  'ASSIGNMENT_CREATE',
  'ASSIGNMENT_UPDATE',
  'ASSIGNMENT_DELETE',
  'ASSIGNMENT_PUBLISH',
  'ASSIGNMENT_CLOSE',
  'ASSIGNMENT_SET_TARGETS',
  'ATTEMPT_START_ASSIGNMENT',
  'ATTEMPT_START_PRACTICE',
  'ATTEMPT_SUBMIT',
]

const auditEntityOptions = ['USER', 'QUESTION', 'PAPER', 'ASSIGNMENT', 'ATTEMPT']

function rangeToStartEnd(range) {
  if (!Array.isArray(range) || range.length !== 2) {
    return { startTime: undefined, endTime: undefined }
  }
  return { startTime: range[0], endTime: range[1] }
}

async function loadLoginLogs() {
  loginLoading.value = true
  try {
    const { startTime, endTime } = rangeToStartEnd(loginQuery.range)
    const data = await adminApi.loginLogs({
      username: loginQuery.username,
      successFlag: loginQuery.successFlag,
      startTime,
      endTime,
      page: loginPage.value,
      size: loginSize.value,
    })
    loginList.value = data.list || []
    loginTotal.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载登录日志失败')
  } finally {
    loginLoading.value = false
  }
}

async function loadAuditLogs() {
  auditLoading.value = true
  try {
    const { startTime, endTime } = rangeToStartEnd(auditQuery.range)
    const data = await adminApi.auditLogs({
      username: auditQuery.username,
      action: auditQuery.action,
      entityType: auditQuery.entityType,
      startTime,
      endTime,
      page: auditPage.value,
      size: auditSize.value,
    })
    auditList.value = data.list || []
    auditTotal.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载系统日志失败')
  } finally {
    auditLoading.value = false
  }
}

function resetLoginQuery() {
  loginQuery.username = ''
  loginQuery.successFlag = undefined
  loginQuery.range = []
  loginPage.value = 1
  loadLoginLogs()
}

function resetAuditQuery() {
  auditQuery.username = ''
  auditQuery.action = ''
  auditQuery.entityType = ''
  auditQuery.range = []
  auditPage.value = 1
  loadAuditLogs()
}

function openDetail(title, payload) {
  detailTitle.value = title
  detailPayload.value = prettyJson(payload) || '无详细内容'
  detailVisible.value = true
}

onMounted(() => {
  loadLoginLogs()
  loadAuditLogs()
})
</script>

<template>
  <el-card class="page-card">
    <h3 class="card-title">系统日志</h3>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="登录日志" name="login">
        <div class="page-toolbar">
          <el-input v-model="loginQuery.username" clearable placeholder="用户名" style="width: 180px" />
          <el-select v-model="loginQuery.successFlag" clearable placeholder="是否成功" style="width: 140px">
            <el-option :value="true" label="成功" />
            <el-option :value="false" label="失败" />
          </el-select>
          <el-date-picker
            v-model="loginQuery.range"
            type="datetimerange"
            value-format="YYYY-MM-DDTHH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
          <el-button type="primary" @click="loginPage = 1; loadLoginLogs()">查询</el-button>
          <el-button @click="resetLoginQuery">重置</el-button>
        </div>

        <el-table :data="loginList" border v-loading="loginLoading">
          <el-table-column prop="logId" label="日志ID" width="90" />
          <el-table-column prop="username" label="用户名" width="140" />
          <el-table-column label="结果" width="90">
            <template #default="{ row }">
              <el-tag :type="row.successFlag ? 'success' : 'danger'">
                {{ row.successFlag ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="ipAddr" label="IP地址" width="150" />
          <el-table-column label="登录时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.loginAt) }}</template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="table-pager"
          background
          layout="total, sizes, prev, pager, next"
          :current-page="loginPage"
          :page-size="loginSize"
          :page-sizes="[10, 20, 50]"
          :total="loginTotal"
          @size-change="(v) => { loginSize = v; loginPage = 1; loadLoginLogs() }"
          @current-change="(v) => { loginPage = v; loadLoginLogs() }"
        />
      </el-tab-pane>

      <el-tab-pane label="系统操作日志" name="audit">
        <div class="page-toolbar">
          <el-input v-model="auditQuery.username" clearable placeholder="操作人" style="width: 180px" />
          <el-select v-model="auditQuery.action" clearable filterable placeholder="操作类型" style="width: 220px">
            <el-option v-for="action in auditActionOptions" :key="action" :label="action" :value="action" />
          </el-select>
          <el-select v-model="auditQuery.entityType" clearable placeholder="对象类型" style="width: 150px">
            <el-option v-for="entityType in auditEntityOptions" :key="entityType" :label="entityType" :value="entityType" />
          </el-select>
          <el-date-picker
            v-model="auditQuery.range"
            type="datetimerange"
            value-format="YYYY-MM-DDTHH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
          <el-button type="primary" @click="auditPage = 1; loadAuditLogs()">查询</el-button>
          <el-button @click="resetAuditQuery">重置</el-button>
        </div>

        <el-table :data="auditList" border v-loading="auditLoading">
          <el-table-column prop="logId" label="日志ID" width="90" />
          <el-table-column prop="username" label="操作人" width="140" />
          <el-table-column prop="action" label="操作类型" min-width="220" show-overflow-tooltip />
          <el-table-column prop="entityType" label="对象类型" width="120" />
          <el-table-column prop="entityId" label="对象ID" width="100" />
          <el-table-column prop="ipAddr" label="IP地址" width="150" />
          <el-table-column label="操作时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail('变更前', row.beforeJson)">变更前</el-button>
              <el-button link type="success" @click="openDetail('变更后', row.afterJson)">变更后</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="table-pager"
          background
          layout="total, sizes, prev, pager, next"
          :current-page="auditPage"
          :page-size="auditSize"
          :page-sizes="[10, 20, 50]"
          :total="auditTotal"
          @size-change="(v) => { auditSize = v; auditPage = 1; loadAuditLogs() }"
          @current-change="(v) => { auditPage = v; loadAuditLogs() }"
        />
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <el-dialog v-model="detailVisible" :title="detailTitle" width="760px">
    <el-input :model-value="detailPayload" type="textarea" :rows="18" readonly />
  </el-dialog>
</template>

<style scoped>
.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
