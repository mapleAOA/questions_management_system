<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { appealApi } from '@/api/services'
import { formatDateTime } from '@/utils/format'
import { APPEAL_STATUS_OPTIONS, labelBy, typeBy } from '@/constants/enums'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const status = ref(undefined)

const createLoading = ref(false)
const form = reactive({
  answerId: undefined,
  reasonText: '',
  attachmentsText: '',
})

async function loadData() {
  loading.value = true
  try {
    const data = await appealApi.my({
      status: status.value,
      page: page.value,
      size: size.value,
    })
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载申诉列表失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.answerId = undefined
  form.reasonText = ''
  form.attachmentsText = ''
}

async function createAppeal() {
  createLoading.value = true
  try {
    const attachments = form.attachmentsText
      .split(/\r?\n/)
      .map((v) => v.trim())
      .filter(Boolean)
    await appealApi.create({
      answerId: form.answerId,
      reasonText: form.reasonText,
      attachments,
    })
    ElMessage.success('申诉已提交')
    resetForm()
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    createLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <el-card class="page-card">
    <h3 class="card-title">提交新申诉</h3>
    <el-form label-width="88px">
      <el-form-item label="答案ID">
        <el-input-number v-model="form.answerId" :min="1" />
      </el-form-item>
      <el-form-item label="申诉理由">
        <el-input
          v-model="form.reasonText"
          type="textarea"
          :rows="5"
          placeholder="请填写申诉理由"
        />
      </el-form-item>
      <el-form-item label="附件链接">
        <el-input
          v-model="form.attachmentsText"
          type="textarea"
          :rows="3"
          placeholder="可选，一行一个链接"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="createLoading" @click="createAppeal">提交申诉</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-card class="page-card">
    <h3 class="card-title">我的申诉记录</h3>

    <div class="page-toolbar">
      <el-select v-model="status" clearable style="width: 180px" placeholder="状态筛选">
        <el-option
          v-for="opt in APPEAL_STATUS_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-button type="primary" @click="page = 1; loadData()">查询</el-button>
      <el-button @click="status = undefined; page = 1; loadData()">重置</el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="appealId" label="申诉ID" width="90" />
      <el-table-column prop="answerId" label="答案ID" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="typeBy(APPEAL_STATUS_OPTIONS, row.appealStatus)">
            {{ labelBy(APPEAL_STATUS_OPTIONS, row.appealStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
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
</template>

<style scoped>
.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
