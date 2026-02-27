<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/services'
import { formatDateTime } from '@/utils/format'

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

onMounted(() => {
  loadLoginLogs()
})
</script>

<template>
  <el-card class="page-card">
    <h3 class="card-title">登录日志</h3>
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
      <el-button
        @click="
          loginQuery.username = '';
          loginQuery.successFlag = undefined;
          loginQuery.range = [];
          loginPage = 1;
          loadLoginLogs();
        "
      >
        重置
      </el-button>
    </div>

    <el-table :data="loginList" border v-loading="loginLoading">
      <el-table-column prop="logId" label="日志ID" width="90" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column label="结果" width="90">
        <template #default="{ row }">
          <el-tag :type="row.successFlag ? 'success' : 'danger'">
            {{ row.successFlag ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ipAddr" label="IP地址" width="140" />
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
  </el-card>
</template>

<style scoped>
.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
