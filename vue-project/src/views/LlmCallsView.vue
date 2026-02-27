<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { llmApi } from '@/api/services'
import { formatDateTime } from '@/utils/format'
import JsonPreview from '@/components/JsonPreview.vue'

const loading = ref(false)
const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const query = reactive({
  bizType: undefined,
  bizId: undefined,
})

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

async function loadData() {
  loading.value = true
  try {
    const data = await llmApi.page({
      bizType: query.bizType,
      bizId: query.bizId,
      page: page.value,
      size: size.value,
    })
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载记录失败')
  } finally {
    loading.value = false
  }
}

async function openDetail(llmCallId) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await llmApi.detail(llmCallId)
  } catch (error) {
    ElMessage.error(error.message || '加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <el-card class="page-card">
    <h3 class="card-title">大模型调用记录</h3>
    <div class="page-toolbar">
      <el-input-number v-model="query.bizType" :min="1" placeholder="业务类型" />
      <el-input-number v-model="query.bizId" :min="1" placeholder="业务ID" />
      <el-button type="primary" @click="page = 1; loadData()">查询</el-button>
      <el-button @click="query.bizType = undefined; query.bizId = undefined; page = 1; loadData()">重置</el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="llmCallId" label="调用ID" width="90" />
      <el-table-column prop="bizType" label="业务类型" width="90" />
      <el-table-column prop="bizId" label="业务ID" width="90" />
      <el-table-column prop="modelName" label="模型" min-width="160" />
      <el-table-column prop="callStatus" label="状态" width="80" />
      <el-table-column prop="latencyMs" label="耗时ms" width="90" />
      <el-table-column label="时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.llmCallId)">详情</el-button>
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

  <el-drawer v-model="detailVisible" title="大模型调用详情" size="72%">
    <div v-loading="detailLoading">
      <el-card class="page-card">
        <h3 class="card-title">基础信息</h3>
        <p class="muted">
          调用ID：{{ detail?.llmCallId }}
          | 业务类型：{{ detail?.bizType }}
          | 业务ID：{{ detail?.bizId }}
          | 模型：{{ detail?.modelName }}
          | 状态：{{ detail?.callStatus }}
        </p>
        <p class="muted">
          令牌数(提示/补全)：{{ detail?.tokensPrompt || 0 }}/{{ detail?.tokensCompletion || 0 }}
          | 费用：{{ detail?.costAmount || '-' }}
          | 耗时：{{ detail?.latencyMs || 0 }} ms
        </p>
      </el-card>

      <el-card class="page-card">
        <h3 class="card-title">提示词</h3>
        <JsonPreview :data="detail?.promptText" max-height="220px" />
      </el-card>

      <el-card class="page-card">
        <h3 class="card-title">响应文本</h3>
        <JsonPreview :data="detail?.responseText" max-height="220px" />
      </el-card>

      <el-card class="page-card">
        <h3 class="card-title">响应JSON</h3>
        <JsonPreview :data="detail?.responseJson" max-height="220px" />
      </el-card>
    </div>
  </el-drawer>
</template>

<style scoped>
.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
