<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { teacherApi } from '@/api/services'
import { formatDateTime } from '@/utils/format'
import {
  APPEAL_STATUS_OPTIONS,
  GRADING_MODE_OPTIONS,
  labelBy,
  typeBy,
} from '@/constants/enums'
import JsonPreview from '@/components/JsonPreview.vue'

const reviewLoading = ref(false)
const reviewList = ref([])
const reviewPage = ref(1)
const reviewSize = ref(10)
const reviewTotal = ref(0)
const reviewQuery = reactive({
  assignmentId: undefined,
  needsReview: true,
})

const evidenceVisible = ref(false)
const evidenceLoading = ref(false)
const evidence = ref(null)

const gradeVisible = ref(false)
const gradeLoading = ref(false)
const gradeForm = reactive({
  answerId: undefined,
  score: 0,
  comment: '',
})

const scoreLoading = ref(false)
const scoreList = ref([])
const scorePage = ref(1)
const scoreSize = ref(10)
const scoreTotal = ref(0)
const scoreAssignmentId = ref(undefined)

const appealLoading = ref(false)
const appealList = ref([])
const appealPage = ref(1)
const appealSize = ref(10)
const appealTotal = ref(0)
const appealStatus = ref(undefined)

const handleVisible = ref(false)
const handleLoading = ref(false)
const handleForm = reactive({
  appealId: undefined,
  action: 'approve',
  finalScore: undefined,
  decisionComment: '',
})

async function loadReviews() {
  reviewLoading.value = true
  try {
    const data = await teacherApi.reviewAnswers({
      assignmentId: reviewQuery.assignmentId,
      needsReview: reviewQuery.needsReview,
      page: reviewPage.value,
      size: reviewSize.value,
    })
    reviewList.value = data.list || []
    reviewTotal.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载待复核列表失败')
  } finally {
    reviewLoading.value = false
  }
}

async function openEvidence(answerId) {
  evidenceVisible.value = true
  evidenceLoading.value = true
  try {
    evidence.value = await teacherApi.answerEvidence(answerId)
  } catch (error) {
    ElMessage.error(error.message || '加载证据失败')
  } finally {
    evidenceLoading.value = false
  }
}

function openGrade(answerId, currentScore = 0) {
  gradeForm.answerId = answerId
  gradeForm.score = currentScore || 0
  gradeForm.comment = ''
  gradeVisible.value = true
}

async function submitGrade() {
  gradeLoading.value = true
  try {
    await teacherApi.manualGrade(gradeForm.answerId, {
      score: gradeForm.score,
      comment: gradeForm.comment,
    })
    ElMessage.success('人工评分已提交')
    gradeVisible.value = false
    loadReviews()
  } catch (error) {
    ElMessage.error(error.message || '评分失败')
  } finally {
    gradeLoading.value = false
  }
}

async function llmRetry(answerId) {
  try {
    const res = await teacherApi.llmRetry(answerId, { times: 1 })
    const ids = res?.llmCallIds || []
    ElMessage.success(`已触发重试，调用数 ${ids.length}`)
    loadReviews()
  } catch (error) {
    ElMessage.error(error.message || '重试失败')
  }
}

async function loadAssignmentScores() {
  if (!scoreAssignmentId.value) {
    scoreList.value = []
    scoreTotal.value = 0
    return
  }
  scoreLoading.value = true
  try {
    const data = await teacherApi.assignmentScores(scoreAssignmentId.value, scorePage.value, scoreSize.value)
    scoreList.value = data.list || []
    scoreTotal.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载作业成绩失败')
  } finally {
    scoreLoading.value = false
  }
}

async function loadAppeals() {
  appealLoading.value = true
  try {
    const data = await teacherApi.appeals({
      status: appealStatus.value,
      page: appealPage.value,
      size: appealSize.value,
    })
    appealList.value = data.list || []
    appealTotal.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载申诉列表失败')
  } finally {
    appealLoading.value = false
  }
}

function openHandleAppeal(row) {
  handleForm.appealId = row.appealId
  handleForm.action = 'approve'
  handleForm.finalScore = undefined
  handleForm.decisionComment = ''
  handleVisible.value = true
}

async function submitHandleAppeal() {
  handleLoading.value = true
  try {
    await teacherApi.handleAppeal(handleForm.appealId, {
      action: handleForm.action,
      finalScore: handleForm.finalScore,
      decisionComment: handleForm.decisionComment,
    })
    ElMessage.success('申诉处理成功')
    handleVisible.value = false
    loadAppeals()
  } catch (error) {
    ElMessage.error(error.message || '处理失败')
  } finally {
    handleLoading.value = false
  }
}

onMounted(() => {
  loadReviews()
  loadAppeals()
})
</script>

<template>
  <el-tabs>
    <el-tab-pane label="待复核答卷">
      <el-card class="page-card">
        <div class="page-toolbar">
          <el-input-number v-model="reviewQuery.assignmentId" :min="1" placeholder="作业ID" />
          <el-select v-model="reviewQuery.needsReview" style="width: 140px">
            <el-option :value="true" label="仅需复核" />
            <el-option :value="false" label="无需复核" />
          </el-select>
          <el-button type="primary" @click="reviewPage = 1; loadReviews()">查询</el-button>
          <el-button @click="reviewQuery.assignmentId = undefined; reviewQuery.needsReview = true; reviewPage = 1; loadReviews()">重置</el-button>
        </div>

        <el-table :data="reviewList" border v-loading="reviewLoading">
          <el-table-column prop="answerId" label="答案ID" width="90" />
          <el-table-column prop="attemptId" label="作答ID" width="90" />
          <el-table-column prop="studentId" label="学生ID" width="90" />
          <el-table-column prop="questionId" label="题目ID" width="90" />
          <el-table-column prop="questionType" label="题型" width="90" />
          <el-table-column prop="score" label="满分" width="70" />
          <el-table-column prop="currentFinalScore" label="当前分" width="80" />
          <el-table-column label="需复核" width="80">
            <template #default="{ row }">
              <el-tag :type="row.needsReview ? 'warning' : 'success'">{{ row.needsReview ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEvidence(row.answerId)">证据</el-button>
              <el-button link type="success" @click="openGrade(row.answerId, row.currentFinalScore)">评分</el-button>
              <el-button link type="warning" @click="llmRetry(row.answerId)">大模型重试</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="table-pager"
          background
          layout="total, sizes, prev, pager, next"
          :current-page="reviewPage"
          :page-size="reviewSize"
          :page-sizes="[10, 20, 50]"
          :total="reviewTotal"
          @size-change="(v) => { reviewSize = v; reviewPage = 1; loadReviews() }"
          @current-change="(v) => { reviewPage = v; loadReviews() }"
        />
      </el-card>
    </el-tab-pane>

    <el-tab-pane label="作业成绩总览">
      <el-card class="page-card">
        <div class="page-toolbar">
          <el-input-number v-model="scoreAssignmentId" :min="1" placeholder="作业ID" />
          <el-button type="primary" @click="scorePage = 1; loadAssignmentScores()">查询成绩</el-button>
        </div>

        <el-table :data="scoreList" border v-loading="scoreLoading">
          <el-table-column prop="studentId" label="学生ID" width="90" />
          <el-table-column prop="displayName" label="姓名" width="120" />
          <el-table-column prop="attemptId" label="作答ID" width="90" />
          <el-table-column prop="totalScore" label="总分" width="90" />
          <el-table-column label="需复核" width="90">
            <template #default="{ row }">
              <el-tag :type="row.needsReview ? 'warning' : 'success'">{{ row.needsReview ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.submittedAt) }}</template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="table-pager"
          background
          layout="total, sizes, prev, pager, next"
          :current-page="scorePage"
          :page-size="scoreSize"
          :page-sizes="[10, 20, 50]"
          :total="scoreTotal"
          @size-change="(v) => { scoreSize = v; scorePage = 1; loadAssignmentScores() }"
          @current-change="(v) => { scorePage = v; loadAssignmentScores() }"
        />
      </el-card>
    </el-tab-pane>

    <el-tab-pane label="申诉处理">
      <el-card class="page-card">
        <div class="page-toolbar">
          <el-select v-model="appealStatus" clearable style="width: 180px" placeholder="申诉状态">
            <el-option
              v-for="opt in APPEAL_STATUS_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <el-button type="primary" @click="appealPage = 1; loadAppeals()">查询</el-button>
          <el-button @click="appealStatus = undefined; appealPage = 1; loadAppeals()">重置</el-button>
        </div>

        <el-table :data="appealList" border v-loading="appealLoading">
          <el-table-column prop="appealId" label="申诉ID" width="90" />
          <el-table-column prop="answerId" label="答案ID" width="90" />
          <el-table-column prop="studentId" label="学生ID" width="90" />
          <el-table-column prop="reasonText" label="申诉理由" min-width="220" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="typeBy(APPEAL_STATUS_OPTIONS, row.appealStatus)">
                {{ labelBy(APPEAL_STATUS_OPTIONS, row.appealStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openHandleAppeal(row)">处理</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="table-pager"
          background
          layout="total, sizes, prev, pager, next"
          :current-page="appealPage"
          :page-size="appealSize"
          :page-sizes="[10, 20, 50]"
          :total="appealTotal"
          @size-change="(v) => { appealSize = v; appealPage = 1; loadAppeals() }"
          @current-change="(v) => { appealPage = v; loadAppeals() }"
        />
      </el-card>
    </el-tab-pane>
  </el-tabs>

  <el-drawer v-model="evidenceVisible" title="答题证据" size="72%">
    <div v-loading="evidenceLoading">
      <el-card class="page-card">
        <h3 class="card-title">基础信息</h3>
        <p class="muted">答案ID：{{ evidence?.answerId }} | 学生：{{ evidence?.student?.displayName }} ({{ evidence?.student?.id }})</p>
        <h4>学生答案</h4>
        <pre class="answer-text">{{ evidence?.studentAnswer || '-' }}</pre>
      </el-card>

      <el-card class="page-card">
        <h3 class="card-title">题目快照</h3>
        <JsonPreview :data="evidence?.questionSnapshot" max-height="260px" />
      </el-card>

      <el-card class="page-card">
        <h3 class="card-title">评分记录</h3>
        <div v-for="(record, idx) in evidence?.gradingRecords || []" :key="idx" class="record-item">
          <p class="muted">
            模式: {{ labelBy(GRADING_MODE_OPTIONS, record.gradingMode) }} |
            分数: {{ record.score }} |
            置信度: {{ record.confidence ?? '-' }} |
            需复核: {{ record.needsReview ? '是' : '否' }}
          </p>
          <JsonPreview :data="record.detailJson" max-height="120px" />
          <p class="muted">评语: {{ record.reviewComment || '-' }}</p>
          <el-collapse v-if="record.llmCall">
            <el-collapse-item title="大模型调用详情">
              <p class="muted">调用ID：{{ record.llmCall.llmCallId }} | 模型：{{ record.llmCall.modelName }}</p>
              <JsonPreview :data="record.llmCall.promptText" max-height="120px" />
              <JsonPreview :data="record.llmCall.responseText" max-height="120px" />
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-card>
    </div>
  </el-drawer>

  <el-dialog v-model="gradeVisible" title="人工评分" width="520px">
    <el-form label-width="90px">
      <el-form-item label="答案ID">
        <el-input v-model="gradeForm.answerId" disabled />
      </el-form-item>
      <el-form-item label="分数">
        <el-input-number v-model="gradeForm.score" :min="0" />
      </el-form-item>
      <el-form-item label="评语">
        <el-input v-model="gradeForm.comment" type="textarea" :rows="4" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="gradeVisible = false">取消</el-button>
      <el-button type="primary" :loading="gradeLoading" @click="submitGrade">提交评分</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="handleVisible" title="处理申诉" width="580px">
    <el-form label-width="100px">
      <el-form-item label="申诉ID">
        <el-input v-model="handleForm.appealId" disabled />
      </el-form-item>
      <el-form-item label="处理动作">
        <el-radio-group v-model="handleForm.action">
          <el-radio-button label="approve">通过</el-radio-button>
          <el-radio-button label="reject">驳回</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="最终分数">
        <el-input-number v-model="handleForm.finalScore" :min="0" />
      </el-form-item>
      <el-form-item label="处理说明">
        <el-input v-model="handleForm.decisionComment" type="textarea" :rows="4" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleVisible = false">取消</el-button>
      <el-button type="primary" :loading="handleLoading" @click="submitHandleAppeal">提交处理</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}

.answer-text {
  margin: 0;
  padding: 10px;
  border-radius: 10px;
  border: 1px solid #d9e4ef;
  background: #f7fbff;
  white-space: pre-wrap;
  line-height: 1.6;
}

.record-item {
  border: 1px solid #d9e4ef;
  border-radius: 10px;
  padding: 10px;
  margin-bottom: 10px;
}
</style>
