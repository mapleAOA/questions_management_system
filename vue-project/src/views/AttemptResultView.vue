<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { appealApi, attemptApi } from '@/api/services'
import { formatDateTime } from '@/utils/format'
import { ATTEMPT_STATUS_OPTIONS, labelBy, typeBy } from '@/constants/enums'

const route = useRoute()
const router = useRouter()

const attemptId = computed(() => Number(route.params.attemptId))
const loading = ref(false)
const result = ref(null)

const appealVisible = ref(false)
const appealLoading = ref(false)
const appealForm = reactive({
  answerId: undefined,
  reasonText: '',
  attachmentsText: '',
})

async function loadResult() {
  loading.value = true
  try {
    result.value = await attemptApi.result(attemptId.value)
  } catch (error) {
    ElMessage.error(error.message || '加载结果失败')
  } finally {
    loading.value = false
  }
}

function openAppeal(answerId) {
  appealForm.answerId = answerId
  appealForm.reasonText = ''
  appealForm.attachmentsText = ''
  appealVisible.value = true
}

async function submitAppeal() {
  appealLoading.value = true
  try {
    const attachments = appealForm.attachmentsText
      .split(/\r?\n/)
      .map((x) => x.trim())
      .filter(Boolean)
    await appealApi.create({
      answerId: appealForm.answerId,
      reasonText: appealForm.reasonText,
      attachments,
    })
    ElMessage.success('申诉提交成功')
    appealVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '申诉提交失败')
  } finally {
    appealLoading.value = false
  }
}

onMounted(loadResult)
</script>

<template>
  <el-card class="page-card" v-loading="loading">
    <div class="page-toolbar">
      <el-button @click="router.push('/attempts/history')">返回作答记录</el-button>
      <el-button type="primary" @click="loadResult">刷新结果</el-button>
    </div>

    <template v-if="result">
      <h3 class="card-title">作答结果 #{{ result.attemptId }}</h3>
      <div class="result-summary">
        <div class="summary-item">
          <span class="muted">状态</span>
          <el-tag :type="typeBy(ATTEMPT_STATUS_OPTIONS, result.status)">
            {{ labelBy(ATTEMPT_STATUS_OPTIONS, result.status) }}
          </el-tag>
        </div>
        <div class="summary-item">
          <span class="muted">总分</span>
          <strong>{{ result.totalScore }}</strong>
        </div>
        <div class="summary-item">
          <span class="muted">客观分</span>
          <strong>{{ result.objectiveScore }}</strong>
        </div>
        <div class="summary-item">
          <span class="muted">主观分</span>
          <strong>{{ result.subjectiveScore }}</strong>
        </div>
        <div class="summary-item">
          <span class="muted">需复核</span>
          <strong>{{ result.needsReview ? '是' : '否' }}</strong>
        </div>
      </div>
      <p class="muted">
        开始: {{ formatDateTime(result.startedAt) }}
        &nbsp;|&nbsp;
        提交: {{ formatDateTime(result.submittedAt) }}
        &nbsp;|&nbsp;
        用时: {{ result.durationSec || 0 }} 秒
      </p>
    </template>
  </el-card>

  <el-card class="page-card">
    <h3 class="card-title">每题明细</h3>
    <el-table :data="result?.answers || []" border>
      <el-table-column prop="answerId" label="答案ID" width="90" />
      <el-table-column prop="questionId" label="题目ID" width="90" />
      <el-table-column prop="autoScore" label="自动分" width="90" />
      <el-table-column prop="finalScore" label="最终分" width="90" />
      <el-table-column label="判定" width="90">
        <template #default="{ row }">{{ row.isCorrect ? '正确' : '未满分' }}</template>
      </el-table-column>
      <el-table-column prop="answerContent" label="作答内容" min-width="240" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="warning" @click="openAppeal(row.answerId)">发起申诉</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="appealVisible" title="发起申诉" width="640px">
    <el-form label-width="90px">
      <el-form-item label="答案ID">
        <el-input v-model="appealForm.answerId" disabled />
      </el-form-item>
      <el-form-item label="申诉理由">
        <el-input
          v-model="appealForm.reasonText"
          type="textarea"
          :rows="6"
          placeholder="请描述你的申诉理由"
        />
      </el-form-item>
      <el-form-item label="附件链接">
        <el-input
          v-model="appealForm.attachmentsText"
          type="textarea"
          :rows="4"
          placeholder="可选，一行一个链接"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="appealVisible = false">取消</el-button>
      <el-button type="primary" :loading="appealLoading" @click="submitAppeal">提交申诉</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.result-summary {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 10px;
}

.summary-item {
  border: 1px solid #d9e4ef;
  border-radius: 10px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

@media (max-width: 980px) {
  .result-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
