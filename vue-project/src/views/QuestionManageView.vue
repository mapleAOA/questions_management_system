<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionApi, tagApi } from '@/api/services'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import {
  CHAPTER_OPTIONS,
  QUESTION_BANK_REVIEW_STATUS_OPTIONS,
  QUESTION_STATUS_OPTIONS,
  QUESTION_TYPE_OPTIONS,
  labelBy,
  typeBy,
} from '@/constants/enums'

const auth = useAuthStore()

const loading = ref(false)
const list = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const tags = ref([])
const dialogVisible = ref(false)
const saveLoading = ref(false)
const llmLoadingId = ref(null)
const llmLoadingKey = ref('')
const llmDialogVisible = ref(false)
const llmDialogLoading = ref(false)
const llmSubmitting = ref(false)
const llmQuestionId = ref(null)
const llmQuestionTitle = ref('')
const llmSelectedModelKey = ref('')
const llmModelOptions = ref([])
const editingId = ref(null)
const analysisTab = ref('teacher')
const modelAnalyses = ref([])

const isAdmin = computed(() => auth.hasAnyRole(['ADMIN']))
const isTeacher = computed(() => auth.hasAnyRole(['TEACHER']))
const showBankSubmitOption = computed(() => isTeacher.value && !isAdmin.value)
const analysisOptions = computed(() => [
  {
    key: 'teacher',
    label: '教师解析',
    ready: Boolean(form.analysisText?.trim()),
  },
  ...(modelAnalyses.value || []).map((item) => ({
    key: item.analysisKey,
    label: item.analysisLabel,
    ready: hasGeneratedAnalysis(item),
  })),
])
const activeModelAnalysis = computed(() =>
  (modelAnalyses.value || []).find((item) => item.analysisKey === analysisTab.value) || null,
)
const llmSelectedModel = computed(() =>
  (llmModelOptions.value || []).find((item) => item.analysisKey === llmSelectedModelKey.value) || null,
)

const query = reactive({
  keyword: '',
  chapter: '',
  difficulty: undefined,
  questionType: undefined,
  status: undefined,
  bankReviewStatus: undefined,
  source: 'all',
  tagIds: [],
})

function newOption(sortOrder = 1, label = 'A') {
  return {
    optionLabel: label,
    optionContent: '',
    isCorrect: 0,
    sortOrder,
  }
}

function newCase(caseNo = 1) {
  return {
    caseNo,
    inputData: '',
    expectedOutput: '',
    caseScore: 0,
    isSample: 0,
  }
}

const form = reactive({
  title: '',
  questionType: 1,
  difficulty: 3,
  chapter: '',
  stem: '',
  standardAnswer: '',
  answerFormat: 1,
  analysisText: '',
  analysisSource: 1,
  status: 1,
  submitToBankReview: false,
  tagIds: [],
  options: [newOption(1, 'A'), newOption(2, 'B')],
  cases: [],
})

const showOptions = computed(() => [1, 2].includes(Number(form.questionType)))
const showCases = computed(() => Number(form.questionType) === 6)
const showStandardAnswer = computed(() => ![1, 2].includes(Number(form.questionType)))

async function loadTags() {
  try {
    tags.value = await tagApi.list('')
  } catch {
    tags.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await questionApi.search({
      keyword: query.keyword,
      chapter: query.chapter,
      difficulty: query.difficulty,
      questionType: query.questionType,
      status: query.status,
      bankReviewStatus: query.bankReviewStatus,
      source: isTeacher.value && !isAdmin.value ? query.source : undefined,
      tagIds: query.tagIds.length ? query.tagIds.join(',') : undefined,
      page: page.value,
      size: size.value,
    })
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载题目失败')
  } finally {
    loading.value = false
  }
}

function canManageRow(row) {
  if (isAdmin.value) {
    return true
  }
  return Number(row?.createdBy) === Number(auth.user?.id)
}

function canSubmitBankReview(row) {
  return !isAdmin.value && canManageRow(row) && Number(row.status) === 2 && ![1, 2].includes(Number(row.bankReviewStatus))
}

function canCancelBankReview(row) {
  return !isAdmin.value && canManageRow(row) && [1, 2].includes(Number(row.bankReviewStatus))
}

function canReviewPending(row) {
  return isAdmin.value && Number(row.bankReviewStatus) === 1
}

function hasGeneratedAnalysis(item) {
  return Boolean(item?.hasAnalysis || (Number(item?.callStatus) === 1 && item?.analysisText?.trim()))
}

function pickDefaultModelKey(items = []) {
  return (
    items.find((item) => !hasGeneratedAnalysis(item))?.analysisKey ||
    items[0]?.analysisKey ||
    ''
  )
}

function syncQuestionAnalyses(questionId, analyses = []) {
  if (Number(editingId.value) === Number(questionId)) {
    modelAnalyses.value = analyses
    if (analysisTab.value !== 'teacher' && !analyses.some((item) => item.analysisKey === analysisTab.value)) {
      analysisTab.value = 'teacher'
    }
  }
  if (Number(llmQuestionId.value) === Number(questionId)) {
    llmModelOptions.value = analyses
    if (!analyses.some((item) => item.analysisKey === llmSelectedModelKey.value)) {
      llmSelectedModelKey.value = pickDefaultModelKey(analyses)
    }
  }
}

function resetForm() {
  editingId.value = null
  analysisTab.value = 'teacher'
  modelAnalyses.value = []
  form.title = ''
  form.questionType = 1
  form.difficulty = 3
  form.chapter = ''
  form.stem = ''
  form.standardAnswer = ''
  form.answerFormat = 1
  form.analysisText = ''
  form.analysisSource = 1
  form.status = 1
  form.submitToBankReview = false
  form.tagIds = []
  form.options = [newOption(1, 'A'), newOption(2, 'B')]
  form.cases = []
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

async function openEdit(questionId) {
  try {
    const detail = await questionApi.detail(questionId)
    editingId.value = questionId
    analysisTab.value = 'teacher'
    syncQuestionAnalyses(questionId, detail.llmAnalyses || [])
    form.title = detail.title || ''
    form.questionType = detail.questionType ?? 1
    form.difficulty = detail.difficulty ?? 3
    form.chapter = detail.chapter || ''
    form.stem = detail.stem || ''
    form.standardAnswer = detail.standardAnswer || ''
    form.answerFormat = detail.answerFormat ?? 1
    form.analysisText = detail.analysisText || ''
    form.analysisSource = detail.analysisSource ?? 1
    form.status = detail.status ?? 1
    form.submitToBankReview = Boolean(detail.submitToBankReview)
    form.tagIds = detail.tagIds || []
    form.options = (detail.options || []).map((item, index) => ({
      id: item.id,
      optionLabel: item.optionLabel || String.fromCharCode(65 + index),
      optionContent: item.optionContent || '',
      isCorrect: item.isCorrect || 0,
      sortOrder: item.sortOrder || index + 1,
    }))
    form.cases = (detail.cases || []).map((item, index) => ({
      id: item.id,
      caseNo: item.caseNo || index + 1,
      inputData: item.inputData || '',
      expectedOutput: item.expectedOutput || '',
      caseScore: item.caseScore || 0,
      isSample: item.isSample || 0,
    }))
    if (showOptions.value && form.options.length === 0) {
      form.options = [newOption(1, 'A'), newOption(2, 'B')]
    }
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '加载题目详情失败')
  }
}

function normalizedPayload() {
  return {
    title: form.title,
    questionType: Number(form.questionType),
    difficulty: Number(form.difficulty),
    chapter: form.chapter || null,
    stem: form.stem,
    standardAnswer: showStandardAnswer.value ? form.standardAnswer || null : null,
    answerFormat: Number(form.answerFormat),
    analysisText: form.analysisText || null,
    analysisSource: Number(form.analysisSource),
    status: Number(form.status),
    submitToBankReview: showBankSubmitOption.value ? Boolean(form.submitToBankReview) : false,
    tagIds: form.tagIds,
    options: showOptions.value
      ? form.options.map((item, index) => ({
          optionLabel: item.optionLabel,
          optionContent: item.optionContent,
          isCorrect: Number(item.isCorrect) ? 1 : 0,
          sortOrder: Number(item.sortOrder ?? index + 1),
        }))
      : [],
    cases: showCases.value
      ? form.cases.map((item, index) => ({
          caseNo: Number(item.caseNo ?? index + 1),
          inputData: item.inputData,
          expectedOutput: item.expectedOutput,
          caseScore: Number(item.caseScore ?? 0),
          isSample: Number(item.isSample) ? 1 : 0,
        }))
      : [],
  }
}

async function saveQuestion() {
  saveLoading.value = true
  try {
    const payload = normalizedPayload()
    if (editingId.value) {
      await questionApi.update(editingId.value, payload)
      ElMessage.success('题目更新成功')
    } else {
      await questionApi.create(payload)
      ElMessage.success('题目创建成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存题目失败')
  } finally {
    saveLoading.value = false
  }
}

async function removeQuestion(questionId) {
  try {
    await ElMessageBox.confirm('确认删除该题目？', '提示', { type: 'warning' })
    await questionApi.remove(questionId)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

async function publishQuestion(questionId) {
  try {
    await questionApi.publish(questionId)
    ElMessage.success('发布成功')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '发布失败')
  }
}

async function submitBankReview(questionId) {
  try {
    await questionApi.submitBankReview(questionId)
    ElMessage.success('已提交总题库审核')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '提交审核失败')
  }
}

async function cancelBankReview(questionId) {
  try {
    await ElMessageBox.confirm('确认取消总题库审核/入库状态？', '提示', { type: 'warning' })
    await questionApi.cancelBankReview(questionId)
    ElMessage.success('已取消入库状态')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消入库失败')
    }
  }
}

async function approveBankQuestion(questionId) {
  try {
    await ElMessageBox.confirm('确认通过该题目的总题库审核？', '提示', { type: 'warning' })
    await questionApi.reviewBankQuestion(questionId, { reviewStatus: 2 })
    ElMessage.success('已通过审核')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '审核失败')
    }
  }
}

async function rejectBankQuestion(questionId) {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因（可选）', '驳回审核', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：解析不完整、题干有歧义',
    })
    await questionApi.reviewBankQuestion(questionId, {
      reviewStatus: 3,
      reviewComment: value || null,
    })
    ElMessage.success('已驳回该题目')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '驳回失败')
    }
  }
}

async function fetchQuestionAnalyses(questionId) {
  const detail = await questionApi.detail(questionId)
  const analyses = detail.llmAnalyses || []
  syncQuestionAnalyses(questionId, analyses)
  return analyses
}

async function openLlmAnalysisSelector(questionId, preferredModelKey = '') {
  llmQuestionId.value = questionId
  llmQuestionTitle.value =
    list.value.find((item) => Number(item.id) === Number(questionId))?.title || form.title || ''
  llmDialogVisible.value = true
  llmDialogLoading.value = true
  try {
    const analyses = await fetchQuestionAnalyses(questionId)
    llmModelOptions.value = analyses
    if (!analyses.length) {
      llmDialogVisible.value = false
      ElMessage.warning('当前没有可用的大模型配置')
      return
    }
    llmSelectedModelKey.value = analyses.some((item) => item.analysisKey === preferredModelKey)
      ? preferredModelKey
      : pickDefaultModelKey(analyses)
  } catch (error) {
    llmDialogVisible.value = false
    ElMessage.error(error.message || '加载模型列表失败')
  } finally {
    llmDialogLoading.value = false
  }
}

async function triggerLlmAnalysis(questionId, providerKey, providerLabel = '') {
  if (!questionId || !providerKey) {
    ElMessage.warning('请先选择一个模型')
    return false
  }
  llmLoadingId.value = questionId
  llmLoadingKey.value = providerKey
  try {
    const result = await questionApi.generateLlmAnalysis(questionId, { providerKey })
    const label = providerLabel || result?.providerLabel || result?.requestedModels?.[0] || '所选模型'
    ElMessage.success(`已发起 ${label} 解析任务，请稍后刷新题目查看结果`)
    await fetchQuestionAnalyses(questionId)
    await loadData()
    return true
  } catch (error) {
    if (error?.code === 'TIMEOUT') {
      ElMessage.success('解析任务已提交，后台仍在生成，请稍后刷新题目查看结果')
      await fetchQuestionAnalyses(questionId)
      await loadData()
      return true
    }
    ElMessage.error(error.message || '生成失败')
    return false
  } finally {
    llmLoadingId.value = null
    llmLoadingKey.value = ''
  }
}

async function llmAnalysis(questionId, preferredModelKey = '') {
  await openLlmAnalysisSelector(questionId, preferredModelKey)
}

async function submitLlmAnalysis() {
  llmSubmitting.value = true
  try {
    const ok = await triggerLlmAnalysis(
      llmQuestionId.value,
      llmSelectedModelKey.value,
      llmSelectedModel.value?.analysisLabel,
    )
    if (ok) {
      llmDialogVisible.value = false
    }
  } finally {
    llmSubmitting.value = false
  }
}

async function triggerCurrentModelAnalysis() {
  if (!editingId.value || analysisTab.value === 'teacher' || !activeModelAnalysis.value) {
    ElMessage.warning('请先选择一个模型解析选项')
    return
  }
  await triggerLlmAnalysis(
    editingId.value,
    activeModelAnalysis.value.analysisKey,
    activeModelAnalysis.value.analysisLabel,
  )
}

function appendOption() {
  const sortOrder = form.options.length + 1
  const nextLabel = String.fromCharCode(64 + Math.min(sortOrder, 26))
  form.options.push(newOption(sortOrder, nextLabel))
}

function appendCase() {
  form.cases.push(newCase(form.cases.length + 1))
}

function formatAnalysisState(item) {
  if (!item) {
    return '暂无解析'
  }
  if (hasGeneratedAnalysis(item) && Number(item.callStatus) === 0) {
    return '已有解析，正在重新生成'
  }
  if (hasGeneratedAnalysis(item) && Number(item.callStatus) === 2) {
    return '保留上次解析，本次生成失败'
  }
  if (hasGeneratedAnalysis(item)) {
    return '解析已生成'
  }
  if (item.callStatus === 1) {
    return '解析已生成'
  }
  if (item.callStatus === 0) {
    return '解析生成中'
  }
  if (item.callStatus === 2) {
    return '解析生成失败'
  }
  return '尚未生成解析'
}

function analysisOptionClass(item) {
  return {
    'analysis-tab--active': analysisTab.value === item.key,
    'analysis-tab--ready': item.ready,
    'analysis-tab--missing': !item.ready,
  }
}

function copyModelAnalysisToTeacher() {
  if (!activeModelAnalysis.value?.analysisText) {
    ElMessage.warning('当前模型解析内容为空，无法复制')
    return
  }
  form.analysisText = activeModelAnalysis.value.analysisText
  analysisTab.value = 'teacher'
  ElMessage.success('已复制到教师解析，可按需编辑后保存')
}

function resetQuery() {
  query.keyword = ''
  query.chapter = ''
  query.questionType = undefined
  query.difficulty = undefined
  query.status = undefined
  query.bankReviewStatus = undefined
  query.source = 'all'
  query.tagIds = []
  page.value = 1
  loadData()
}

function filterPendingBankReview() {
  query.bankReviewStatus = 1
  query.status = 2
  page.value = 1
  loadData()
}

onMounted(async () => {
  await loadTags()
  await loadData()
})
</script>

<template>
  <el-card class="page-card">
    <h3 class="card-title">题目管理</h3>
    <div class="page-toolbar">
      <el-input v-model="query.keyword" clearable style="width: 180px" placeholder="关键词" />
      <el-select v-model="query.chapter" clearable style="width: 170px" placeholder="章节">
        <el-option v-for="opt in CHAPTER_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="query.questionType" clearable style="width: 130px" placeholder="题型">
        <el-option v-for="opt in QUESTION_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="query.difficulty" clearable style="width: 130px" placeholder="难度">
        <el-option v-for="n in [1, 2, 3, 4, 5]" :key="n" :label="`${n}`" :value="n" />
      </el-select>
      <el-select v-model="query.status" clearable style="width: 130px" placeholder="发布状态">
        <el-option v-for="opt in QUESTION_STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="query.bankReviewStatus" clearable style="width: 150px" placeholder="入库状态">
        <el-option
          v-for="opt in QUESTION_BANK_REVIEW_STATUS_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-select v-if="isTeacher && !isAdmin" v-model="query.source" style="width: 150px" placeholder="题目来源">
        <el-option label="全部可用" value="all" />
        <el-option label="我出的题" value="mine" />
        <el-option label="总题库" value="bank" />
      </el-select>
      <el-select v-model="query.tagIds" multiple collapse-tags style="width: 260px" placeholder="筛选标签">
        <el-option v-for="tag in tags" :key="tag.id" :label="tag.tagName" :value="tag.id" />
      </el-select>
      <el-button type="primary" @click="page = 1; loadData()">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button v-if="isAdmin" type="warning" plain @click="filterPendingBankReview">只看待审核</el-button>
      <el-button type="success" @click="openCreate">新建题目</el-button>
    </div>

    <el-alert
      v-if="isAdmin"
      title="管理员可在本页直接审核教师提交到总题库的题目，使用“只看待审核”可以快速筛选待处理题目。"
      type="info"
      :closable="false"
      style="margin-bottom: 12px"
    />

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="题目编号" width="90" />
      <el-table-column v-if="isAdmin" prop="createdBy" label="创建人ID" width="110" />
      <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
      <el-table-column label="题型" width="110">
        <template #default="{ row }">{{ labelBy(QUESTION_TYPE_OPTIONS, row.questionType) }}</template>
      </el-table-column>
      <el-table-column prop="difficulty" label="难度" width="80" />
      <el-table-column prop="chapter" label="章节" width="130" show-overflow-tooltip />
      <el-table-column label="标签" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ (row.tagNames || []).join('、') || '-' }}</template>
      </el-table-column>
      <el-table-column label="发布状态" width="100">
        <template #default="{ row }">
          <el-tag :type="typeBy(QUESTION_STATUS_OPTIONS, row.status)">{{ labelBy(QUESTION_STATUS_OPTIONS, row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="入库状态" width="160">
        <template #default="{ row }">
          <div class="review-state-cell">
            <el-tag :type="typeBy(QUESTION_BANK_REVIEW_STATUS_OPTIONS, row.bankReviewStatus)">
              {{ labelBy(QUESTION_BANK_REVIEW_STATUS_OPTIONS, row.bankReviewStatus) }}
            </el-tag>
            <span v-if="row.bankReviewComment" class="review-comment">{{ row.bankReviewComment }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="360" fixed="right">
        <template #default="{ row }">
          <el-button v-if="canManageRow(row)" link type="primary" @click="openEdit(row.id)">编辑</el-button>
          <el-button v-if="canManageRow(row)" link type="success" @click="publishQuestion(row.id)">发布</el-button>
          <el-button v-if="canSubmitBankReview(row)" link type="warning" @click="submitBankReview(row.id)">提交审核</el-button>
          <el-button v-if="canCancelBankReview(row)" link type="info" @click="cancelBankReview(row.id)">取消入库</el-button>
          <el-button v-if="canReviewPending(row)" link type="success" @click="approveBankQuestion(row.id)">通过</el-button>
          <el-button v-if="canReviewPending(row)" link type="warning" @click="rejectBankQuestion(row.id)">驳回</el-button>
          <el-button
            v-if="canManageRow(row)"
            link
            type="warning"
            :loading="llmLoadingId === row.id"
            @click="llmAnalysis(row.id)"
          >
            大模型解析
          </el-button>
          <el-button v-if="canManageRow(row)" link type="danger" @click="removeQuestion(row.id)">删除</el-button>
          <span v-if="!canManageRow(row)" class="muted">仅可用于组卷</span>
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

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑题目' : '新建题目'" width="980px" destroy-on-close>
    <el-form label-width="94px">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="标题">
            <el-input v-model="form.title" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="题型">
            <el-select v-model="form.questionType" style="width: 100%">
              <el-option v-for="opt in QUESTION_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="难度">
            <el-select v-model="form.difficulty" style="width: 100%">
              <el-option v-for="n in [1, 2, 3, 4, 5]" :key="n" :label="`${n}`" :value="n" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="章节">
            <el-select v-model="form.chapter" clearable style="width: 100%">
              <el-option v-for="opt in CHAPTER_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="发布状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option v-for="opt in QUESTION_STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="标签">
            <el-select v-model="form.tagIds" multiple collapse-tags style="width: 100%">
              <el-option v-for="tag in tags" :key="tag.id" :label="tag.tagName" :value="tag.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item v-if="showBankSubmitOption" label="总题库审核">
        <div class="bank-submit-block">
          <el-switch v-model="form.submitToBankReview" />
          <span class="muted">打开后，教师发布的题目会进入管理员审核流程，审核通过后进入总题库。</span>
        </div>
      </el-form-item>
      <el-alert
        v-if="showBankSubmitOption && form.submitToBankReview && Number(form.status) !== 2"
        type="warning"
        :closable="false"
        title="若要提交总题库审核，请先把题目发布为“已发布”。"
      />

      <el-form-item label="题干">
        <el-input v-model="form.stem" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item v-if="showStandardAnswer" label="标准答案">
        <el-input v-model="form.standardAnswer" type="textarea" :rows="3" />
      </el-form-item>
      <el-alert v-else type="info" :closable="false" title="单选/多选题会根据“正确选项”自动生成标准答案。" />
      <el-form-item label="解析">
        <div class="analysis-panel">
          <div class="analysis-switch">
            <div class="analysis-tabs">
              <button
                v-for="item in analysisOptions"
                :key="item.key"
                type="button"
                class="analysis-tab"
                :class="analysisOptionClass(item)"
                @click="analysisTab = item.key"
              >
                {{ item.label }}
              </button>
            </div>
            <div class="analysis-actions">
              <el-button
                v-if="editingId"
                type="primary"
                link
                @click="llmAnalysis(editingId, analysisTab !== 'teacher' ? analysisTab : '')"
              >
                选择模型解析
              </el-button>
              <el-button
                v-if="editingId && analysisTab !== 'teacher'"
                type="warning"
                link
                :loading="llmLoadingId === editingId && llmLoadingKey === analysisTab"
                @click="triggerCurrentModelAnalysis"
              >
                {{ hasGeneratedAnalysis(activeModelAnalysis) ? '重新生成当前模型解析' : '生成当前模型解析' }}
              </el-button>
              <el-button
                v-if="analysisTab !== 'teacher' && hasGeneratedAnalysis(activeModelAnalysis) && activeModelAnalysis?.analysisText"
                type="success"
                link
                @click="copyModelAnalysisToTeacher"
              >
                复制到教师解析
              </el-button>
            </div>
          </div>

          <el-input
            v-if="analysisTab === 'teacher'"
            v-model="form.analysisText"
            type="textarea"
            :rows="8"
            class="analysis-input"
          />

          <template v-else>
            <el-input
              :model-value="activeModelAnalysis?.analysisText || ''"
              type="textarea"
              :rows="8"
              readonly
              class="analysis-input"
            />
            <div class="analysis-meta">
              <span>{{ formatAnalysisState(activeModelAnalysis) }}</span>
              <span v-if="activeModelAnalysis?.latencyMs">耗时 {{ activeModelAnalysis.latencyMs }} ms</span>
              <span v-if="activeModelAnalysis?.llmCallId">调用ID {{ activeModelAnalysis.llmCallId }}</span>
            </div>
            <el-alert
              v-if="activeModelAnalysis?.errorMessage"
              type="warning"
              :closable="false"
              :title="activeModelAnalysis.errorMessage"
            />
          </template>
        </div>
      </el-form-item>

      <template v-if="showOptions">
        <el-divider />
        <div class="sub-title-row">
          <h4>选项</h4>
          <el-button type="primary" link @click="appendOption">添加选项</el-button>
        </div>
        <el-table :data="form.options" border>
          <el-table-column label="标识" width="80">
            <template #default="{ row }">
              <el-input v-model="row.optionLabel" />
            </template>
          </el-table-column>
          <el-table-column label="内容" min-width="260">
            <template #default="{ row }">
              <el-input v-model="row.optionContent" />
            </template>
          </el-table-column>
          <el-table-column label="正确" width="80">
            <template #default="{ row }">
              <el-switch v-model="row.isCorrect" :active-value="1" :inactive-value="0" />
            </template>
          </el-table-column>
          <el-table-column label="排序" width="90">
            <template #default="{ row }">
              <el-input-number v-model="row.sortOrder" :min="1" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ $index }">
              <el-button link type="danger" @click="form.options.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <template v-if="showCases">
        <el-divider />
        <div class="sub-title-row">
          <h4>测试样例</h4>
          <el-button type="primary" link @click="appendCase">添加样例</el-button>
        </div>
        <el-table :data="form.cases" border>
          <el-table-column label="编号" width="90">
            <template #default="{ row }">
              <el-input-number v-model="row.caseNo" :min="1" />
            </template>
          </el-table-column>
          <el-table-column label="输入" min-width="220">
            <template #default="{ row }">
              <el-input v-model="row.inputData" />
            </template>
          </el-table-column>
          <el-table-column label="输出" min-width="220">
            <template #default="{ row }">
              <el-input v-model="row.expectedOutput" />
            </template>
          </el-table-column>
          <el-table-column label="分值" width="90">
            <template #default="{ row }">
              <el-input-number v-model="row.caseScore" :min="0" />
            </template>
          </el-table-column>
          <el-table-column label="示例" width="90">
            <template #default="{ row }">
              <el-switch v-model="row.isSample" :active-value="1" :inactive-value="0" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ $index }">
              <el-button link type="danger" @click="form.cases.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saveLoading" @click="saveQuestion">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="llmDialogVisible" title="选择解析模型" width="760px" destroy-on-close>
    <div v-loading="llmDialogLoading" class="llm-dialog-body">
      <p class="muted">题目：{{ llmQuestionTitle || `题目 #${llmQuestionId || '-'}` }}</p>
      <div class="llm-model-list">
        <button
          v-for="item in llmModelOptions"
          :key="item.analysisKey"
          type="button"
          class="llm-model-card"
          :class="{ 'llm-model-card--active': llmSelectedModelKey === item.analysisKey }"
          @click="llmSelectedModelKey = item.analysisKey"
        >
          <div class="llm-model-card__head">
            <strong>{{ item.analysisLabel }}</strong>
            <el-tag :type="hasGeneratedAnalysis(item) ? 'success' : 'danger'">
              {{ hasGeneratedAnalysis(item) ? '已有解析' : '暂无解析' }}
            </el-tag>
          </div>
          <div class="llm-model-card__model">{{ item.modelName }}</div>
          <div class="llm-model-card__state">{{ formatAnalysisState(item) }}</div>
        </button>
      </div>
    </div>
    <template #footer>
      <el-button @click="llmDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="llmSubmitting" @click="submitLlmAnalysis">开始解析</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}

.sub-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.sub-title-row h4 {
  margin: 0;
  color: #334e68;
}

.analysis-input :deep(.el-textarea__inner) {
  min-height: 220px;
  resize: vertical;
  line-height: 1.7;
}

.analysis-panel {
  width: 100%;
}

.analysis-switch {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.analysis-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.analysis-tab {
  border: 1px solid transparent;
  border-radius: 12px;
  background: #f8fafc;
  color: #475467;
  padding: 10px 18px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.analysis-tab--ready {
  background: #ecfdf3;
  border-color: #86efac;
  color: #166534;
}

.analysis-tab--missing {
  background: #fef2f2;
  border-color: #fca5a5;
  color: #b42318;
}

.analysis-tab--active.analysis-tab--ready {
  background: #22c55e;
  border-color: #22c55e;
  color: #fff;
  box-shadow: 0 10px 20px rgba(34, 197, 94, 0.22);
}

.analysis-tab--active.analysis-tab--missing {
  background: #ef4444;
  border-color: #ef4444;
  color: #fff;
  box-shadow: 0 10px 20px rgba(239, 68, 68, 0.2);
}

.analysis-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.analysis-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  color: #7b8794;
  font-size: 13px;
}

.review-state-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.review-comment {
  color: #7b8794;
  font-size: 12px;
  line-height: 1.4;
}

.bank-submit-block {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.llm-dialog-body {
  min-height: 120px;
}

.llm-model-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-top: 14px;
}

.llm-model-card {
  border: 1px solid #d0d5dd;
  border-radius: 14px;
  background: #fff;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.llm-model-card:hover {
  border-color: #60a5fa;
  box-shadow: 0 12px 24px rgba(96, 165, 250, 0.14);
  transform: translateY(-1px);
}

.llm-model-card--active {
  border-color: #2563eb;
  box-shadow: 0 14px 28px rgba(37, 99, 235, 0.16);
}

.llm-model-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.llm-model-card__model {
  color: #475467;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-all;
}

.llm-model-card__state {
  margin-top: 10px;
  color: #667085;
  font-size: 13px;
  line-height: 1.5;
}
</style>
