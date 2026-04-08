<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { appealApi, attemptApi } from '@/api/services'
import { formatDateTime, parseJsonSafe } from '@/utils/format'
import { ATTEMPT_STATUS_OPTIONS, QUESTION_TYPE_OPTIONS, labelBy, typeBy } from '@/constants/enums'

const route = useRoute()
const router = useRouter()

const attemptId = computed(() => Number(route.params.attemptId))
const loading = ref(false)
const result = ref(null)

const detailVisible = ref(false)
const detailRow = ref(null)

const appealVisible = ref(false)
const appealLoading = ref(false)
const appealForm = reactive({
  answerId: undefined,
  reasonText: '',
  attachmentsText: '',
})

function answerRows() {
  return Array.isArray(result.value?.answers) ? result.value.answers : []
}

function rowSnapshot(row) {
  return parseJsonSafe(row?.snapshotJson, {}) || {}
}

function rowTitle(row) {
  const snapshot = rowSnapshot(row)
  return snapshot.title || snapshot.stem || `题目 #${row?.questionId || '-'}`
}

function rowTypeText(row) {
  return labelBy(QUESTION_TYPE_OPTIONS, Number(rowSnapshot(row).questionType || 0), '未知题型')
}

function rowScoreText(row) {
  const finalScore = row?.finalScore ?? 0
  const maxScore = row?.maxScore ?? 0
  return `${finalScore} / ${maxScore}`
}

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

function openDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

function openAppeal(row) {
  appealForm.answerId = row.answerId
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
    <el-table :data="answerRows()" border>
      <el-table-column prop="orderNo" label="题号" width="80" />
      <el-table-column label="题目" min-width="260" show-overflow-tooltip>
        <template #default="{ row }">
          {{ rowTitle(row) }}
        </template>
      </el-table-column>
      <el-table-column label="题型" width="120">
        <template #default="{ row }">{{ rowTypeText(row) }}</template>
      </el-table-column>
      <el-table-column label="得分" width="100">
        <template #default="{ row }">{{ rowScoreText(row) }}</template>
      </el-table-column>
      <el-table-column label="判定" width="90">
        <template #default="{ row }">{{ row.isCorrect ? '正确' : '未满分' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
          <el-button link type="warning" @click="openAppeal(row)">发起申诉</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="detailVisible" title="题目详情" width="900px">
    <template v-if="detailRow">
      <div class="detail-block">
        <div class="detail-meta">
          <el-tag type="primary">第 {{ detailRow.orderNo || '-' }} 题</el-tag>
          <el-tag type="warning">{{ rowTypeText(detailRow) }}</el-tag>
          <el-tag type="success">得分 {{ rowScoreText(detailRow) }}</el-tag>
        </div>

        <h4 class="detail-title">{{ rowTitle(detailRow) }}</h4>

        <div class="detail-section">
          <div class="detail-label">题干</div>
          <div class="detail-content pre-wrap">{{ rowSnapshot(detailRow).stem || '-' }}</div>
        </div>

        <div class="detail-section" v-if="Array.isArray(rowSnapshot(detailRow).options) && rowSnapshot(detailRow).options.length">
          <div class="detail-label">选项</div>
          <div class="detail-content option-list">
            <div v-for="opt in rowSnapshot(detailRow).options" :key="`${detailRow.answerId}-${opt.optionLabel}`" class="option-item">
              {{ opt.optionLabel }}. {{ opt.optionContent }}
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="Array.isArray(rowSnapshot(detailRow).cases) && rowSnapshot(detailRow).cases.length">
          <div class="detail-label">测试样例</div>
          <el-table :data="rowSnapshot(detailRow).cases" border>
            <el-table-column prop="caseNo" label="编号" width="80" />
            <el-table-column prop="inputData" label="输入" min-width="180" />
            <el-table-column prop="expectedOutput" label="输出" min-width="180" />
            <el-table-column prop="caseScore" label="分值" width="80" />
          </el-table>
        </div>

        <div class="detail-section">
          <div class="detail-label">我的答案</div>
          <div class="detail-content pre-wrap">{{ detailRow.answerContent || '未作答' }}</div>
        </div>

        <div class="detail-section">
          <div class="detail-label">参考答案</div>
          <div class="detail-content pre-wrap">{{ rowSnapshot(detailRow).standardAnswer || '-' }}</div>
        </div>

        <div class="detail-section">
          <div class="detail-label">题目解析</div>
          <div class="detail-content pre-wrap">{{ rowSnapshot(detailRow).analysisText || '-' }}</div>
        </div>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="appealVisible" title="发起申诉" width="640px">
    <el-form label-width="90px">
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

.detail-block {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-title {
  margin: 0;
  color: #102a43;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-label {
  font-weight: 700;
  color: #334e68;
}

.detail-content {
  padding: 12px;
  border: 1px solid #d9e4ef;
  border-radius: 8px;
  background: #f8fbff;
}

.pre-wrap {
  white-space: pre-wrap;
  line-height: 1.7;
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  line-height: 1.6;
}

@media (max-width: 980px) {
  .result-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
