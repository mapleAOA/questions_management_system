<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Rank } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { paperApi, questionApi } from '@/api/services'
import { formatDateTime, parseJsonSafe } from '@/utils/format'
import { PAPER_TYPE_OPTIONS, labelBy } from '@/constants/enums'

const loading = ref(false)
const list = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const saveLoading = ref(false)
const editingId = ref(null)
const form = reactive({
  paperTitle: '',
  paperDesc: '',
  paperType: 1,
  status: 1,
})

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const addQLoading = ref(false)
const saveQuestionConfigLoading = ref(false)
const addQForm = reactive({
  questionId: undefined,
  score: 5,
})

const questionOptions = ref([])
const paperQuestionDrafts = ref([])
const draggedQuestionId = ref(null)
const dropTargetQuestionId = ref(null)

function paperStatusText(status) {
  return Number(status) === 2 ? '已发布' : '草稿'
}

function normalizeScore(value) {
  const score = Number(value)
  if (!Number.isFinite(score) || score < 0) {
    return 0
  }
  return Math.round(score)
}

function nextPaperQuestionOrderNo(questions = []) {
  return questions.reduce((maxValue, item) => Math.max(maxValue, Number(item?.orderNo || 0)), 0) + 1
}

function questionSnapshot(snapshotJson) {
  return parseJsonSafe(snapshotJson, {})
}

function questionTextFromSnapshot(snapshotJson) {
  const obj = questionSnapshot(snapshotJson)
  return obj?.stem || obj?.title || '-'
}

function syncQuestionDrafts(questions = []) {
  paperQuestionDrafts.value = questions.map((item) => ({
    ...item,
    score: normalizeScore(item.score),
  }))
  draggedQuestionId.value = null
  dropTargetQuestionId.value = null
}

function resetForm() {
  editingId.value = null
  form.paperTitle = ''
  form.paperDesc = ''
  form.paperType = 1
  form.status = 1
}

function syncAddQuestionDefaults() {
  if (addQForm.score === undefined || addQForm.score === null || Number(addQForm.score) < 0) {
    addQForm.score = 5
  }
}

function hasUnsavedQuestionChangesWarning(actionText) {
  if (!hasQuestionDraftChanges.value) {
    return false
  }
  ElMessage.warning(`请先保存当前题目顺序和分值，再${actionText}`)
  return true
}

async function loadQuestionsForSelect() {
  try {
    const data = await questionApi.search({ page: 1, size: 200, status: 2 })
    questionOptions.value = data.list || []
  } catch {
    questionOptions.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await paperApi.page(page.value, size.value)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载试卷失败')
  } finally {
    loading.value = false
  }
}

async function reloadPaperDetail(paperId) {
  detail.value = await paperApi.detail(paperId)
  syncQuestionDrafts(detail.value?.questions || [])
  syncAddQuestionDefaults()
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.paperTitle = row.paperTitle || ''
  form.paperDesc = row.paperDesc || ''
  form.paperType = row.paperType || 1
  form.status = row.status || 1
  dialogVisible.value = true
}

async function savePaper() {
  saveLoading.value = true
  try {
    const payload = {
      paperTitle: form.paperTitle,
      paperDesc: form.paperDesc || null,
      paperType: Number(form.paperType || 1),
      status: Number(form.status || 1),
    }
    if (editingId.value) {
      await paperApi.update(editingId.value, payload)
      ElMessage.success('试卷更新成功')
    } else {
      await paperApi.create(payload)
      ElMessage.success('试卷创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存试卷失败')
  } finally {
    saveLoading.value = false
  }
}

async function removePaper(paperId) {
  try {
    await ElMessageBox.confirm('确认删除这张试卷？', '提示', { type: 'warning' })
    await paperApi.remove(paperId)
    ElMessage.success('试卷删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除试卷失败')
    }
  }
}

async function openDetail(paperId) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    await reloadPaperDetail(paperId)
  } catch (error) {
    ElMessage.error(error.message || '加载试卷详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function addQuestionToPaper() {
  if (!detail.value?.id) return
  if (hasUnsavedQuestionChangesWarning('添加题目')) {
    return
  }
  if (!addQForm.questionId) {
    ElMessage.warning('请先选择题目')
    return
  }
  addQLoading.value = true
  try {
    await paperApi.addQuestion(detail.value.id, {
      questionId: addQForm.questionId,
      orderNo: nextPaperQuestionOrderNo(paperQuestionDrafts.value),
      score: normalizeScore(addQForm.score),
    })
    ElMessage.success('题目添加成功')
    addQForm.questionId = undefined
    addQForm.score = 5
    await reloadPaperDetail(detail.value.id)
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '添加题目失败')
  } finally {
    addQLoading.value = false
  }
}

async function saveQuestionConfig() {
  if (!detail.value?.id || !paperQuestionDrafts.value.length) {
    return
  }
  if (!hasQuestionDraftChanges.value) {
    ElMessage.info('当前题目顺序和分值没有变化')
    return
  }

  saveQuestionConfigLoading.value = true
  try {
    await paperApi.batchUpdateQuestions(detail.value.id, {
      questions: paperQuestionDrafts.value.map((question, index) => ({
        id: question.id,
        orderNo: index + 1,
        score: normalizeScore(question.score),
      })),
    })
    ElMessage.success('题目顺序和分值已保存')
    await reloadPaperDetail(detail.value.id)
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存题目配置失败')
  } finally {
    saveQuestionConfigLoading.value = false
  }
}

function resetQuestionDrafts() {
  syncQuestionDrafts(detail.value?.questions || [])
}

async function removePaperQuestion(row) {
  if (!detail.value?.id) return
  if (hasUnsavedQuestionChangesWarning('移除题目')) {
    return
  }
  try {
    await ElMessageBox.confirm('确认将这道题移出试卷？', '提示', { type: 'warning' })
    await paperApi.removePaperQuestion(row.id)
    ElMessage.success('题目已移出试卷')
    await reloadPaperDetail(detail.value.id)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '移除题目失败')
    }
  }
}

async function recalculatePaper() {
  if (!detail.value?.id) return
  if (hasUnsavedQuestionChangesWarning('重算总分')) {
    return
  }
  try {
    await paperApi.recalculate(detail.value.id)
    ElMessage.success('总分已重算')
    await reloadPaperDetail(detail.value.id)
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '重算总分失败')
  }
}

function moveQuestion(draggedId, targetId) {
  const fromIndex = paperQuestionDrafts.value.findIndex((question) => question.id === draggedId)
  const targetIndex = paperQuestionDrafts.value.findIndex((question) => question.id === targetId)
  if (fromIndex === -1 || targetIndex === -1 || fromIndex === targetIndex) {
    return
  }

  const nextQuestions = [...paperQuestionDrafts.value]
  const [draggedQuestion] = nextQuestions.splice(fromIndex, 1)
  nextQuestions.splice(targetIndex, 0, draggedQuestion)
  paperQuestionDrafts.value = nextQuestions
}

function handleQuestionDragStart(questionId) {
  draggedQuestionId.value = questionId
}

function handleQuestionDragOver(questionId) {
  if (!draggedQuestionId.value || draggedQuestionId.value === questionId) {
    return
  }
  dropTargetQuestionId.value = questionId
}

function handleQuestionDrop(questionId) {
  if (!draggedQuestionId.value) {
    return
  }
  moveQuestion(draggedQuestionId.value, questionId)
  draggedQuestionId.value = null
  dropTargetQuestionId.value = null
}

function handleQuestionDragEnd() {
  draggedQuestionId.value = null
  dropTargetQuestionId.value = null
}

function questionCardClass(questionId) {
  return {
    'question-card': true,
    'is-drop-target': dropTargetQuestionId.value === questionId && draggedQuestionId.value !== questionId,
    'is-dragging': draggedQuestionId.value === questionId,
  }
}

const currentPaperQuestions = computed(() => paperQuestionDrafts.value)

const hasQuestionDraftChanges = computed(() => {
  const sourceQuestions = detail.value?.questions || []
  const draftQuestions = paperQuestionDrafts.value
  if (!sourceQuestions.length && !draftQuestions.length) {
    return false
  }
  if (sourceQuestions.length !== draftQuestions.length) {
    return true
  }

  return draftQuestions.some((question, index) => {
    const source = sourceQuestions[index]
    if (!source) {
      return true
    }
    return question.id !== source.id || normalizeScore(question.score) !== normalizeScore(source.score)
  })
})

onMounted(async () => {
  await loadQuestionsForSelect()
  loadData()
})
</script>

<template>
  <el-card class="page-card">
    <h3 class="card-title">试卷管理</h3>
    <div class="page-toolbar">
      <el-button type="success" @click="openCreate">新建试卷</el-button>
      <el-button type="primary" @click="loadData">刷新</el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="paperTitle" label="标题" min-width="220" />
      <el-table-column label="类型" width="120">
        <template #default="{ row }">{{ labelBy(PAPER_TYPE_OPTIONS, row.paperType) }}</template>
      </el-table-column>
      <el-table-column prop="totalScore" label="总分" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ paperStatusText(row.status) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
          <el-button link type="warning" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removePaper(row.id)">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑试卷' : '新建试卷'" width="680px">
    <el-form label-width="90px">
      <el-form-item label="标题">
        <el-input v-model="form.paperTitle" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.paperDesc" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.paperType" style="width: 100%">
          <el-option
            v-for="opt in PAPER_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="草稿" :value="1" />
          <el-option label="已发布" :value="2" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saveLoading" @click="savePaper">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" size="76%" title="试卷详情">
    <el-card class="page-card" v-loading="detailLoading">
      <template v-if="detail">
        <h3 class="card-title">{{ detail.paperTitle }}（总分 {{ detail.totalScore }}）</h3>
        <p class="muted">试卷编号: {{ detail.id }} | 创建者: {{ detail.creatorId }} | {{ formatDateTime(detail.updatedAt) }}</p>
        <p class="muted">{{ detail.paperDesc || '暂无描述' }}</p>
      </template>
    </el-card>

    <el-card class="page-card">
      <h3 class="card-title">添加题目</h3>
      <div class="page-toolbar add-question-toolbar">
        <el-select v-model="addQForm.questionId" filterable style="width: 420px" placeholder="选择题目">
          <el-option
            v-for="q in questionOptions"
            :key="q.id"
            :label="`${q.id} - ${q.title}`"
            :value="q.id"
          />
        </el-select>
        <div class="add-question-field">
          <span class="add-question-label">分值</span>
          <el-input-number v-model="addQForm.score" :min="0" />
          <span class="add-question-hint">新题会自动追加到当前试卷末尾</span>
        </div>
        <el-button type="primary" :loading="addQLoading" @click="addQuestionToPaper">添加</el-button>
        <el-button type="warning" @click="recalculatePaper">重算总分</el-button>
      </div>
    </el-card>

    <el-card class="page-card">
      <div class="question-list-header">
        <div>
          <h3 class="card-title">题目列表</h3>
          <p class="question-list-tip">拖动卡片右侧手柄可调整顺序，完成后点击“保存更新”统一生效。</p>
        </div>
        <div class="question-list-actions">
          <el-button :disabled="!hasQuestionDraftChanges" @click="resetQuestionDrafts">撤销改动</el-button>
          <el-button
            type="primary"
            :disabled="!hasQuestionDraftChanges"
            :loading="saveQuestionConfigLoading"
            @click="saveQuestionConfig"
          >
            保存更新
          </el-button>
        </div>
      </div>

      <div v-if="currentPaperQuestions.length" class="question-card-list">
        <div
          v-for="(row, index) in currentPaperQuestions"
          :key="row.id"
          :class="questionCardClass(row.id)"
          @dragover.prevent="handleQuestionDragOver(row.id)"
          @drop.prevent="handleQuestionDrop(row.id)"
        >
          <div class="question-card-head">
            <div class="question-index-badge">第 {{ index + 1 }} 题</div>
            <div
              class="question-drag-handle"
              draggable="true"
              @dragstart="handleQuestionDragStart(row.id)"
              @dragend="handleQuestionDragEnd"
            >
              <el-icon><Rank /></el-icon>
              <span>拖动排序</span>
            </div>
          </div>

          <div class="question-card-body">{{ questionTextFromSnapshot(row.snapshotJson) }}</div>

          <div class="question-card-footer">
            <span class="question-card-meta">题目编号：{{ row.questionId }}</span>
            <div class="question-score-editor">
              <span class="question-score-label">分值</span>
              <el-input-number v-model="row.score" :min="0" />
            </div>
            <el-button link type="danger" @click.stop="removePaperQuestion(row)">移除</el-button>
          </div>
        </div>
      </div>

      <el-empty v-else description="当前试卷还没有题目" />
    </el-card>
  </el-drawer>
</template>

<style scoped>
.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}

.add-question-toolbar {
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
}

.add-question-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.add-question-label {
  color: #486581;
  font-size: 13px;
  font-weight: 600;
}

.add-question-hint {
  color: #829ab1;
  font-size: 12px;
}

.question-list-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.question-list-tip {
  margin: 6px 0 0;
  color: #6b879f;
  font-size: 13px;
}

.question-list-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.question-card-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-card {
  border: 1px solid #d9e2ec;
  border-radius: 18px;
  padding: 18px 20px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.05);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.question-card.is-drop-target {
  border-color: #409eff;
  box-shadow: 0 16px 32px rgba(64, 158, 255, 0.16);
  transform: translateY(-2px);
}

.question-card.is-dragging {
  opacity: 0.72;
}

.question-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.question-index-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 82px;
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: #eaf2ff;
  color: #2457a6;
  font-size: 13px;
  font-weight: 700;
}

.question-drag-handle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #5b6f86;
  font-size: 13px;
  cursor: grab;
  user-select: none;
}

.question-drag-handle:active {
  cursor: grabbing;
}

.question-card-body {
  color: #243b53;
  font-size: 17px;
  line-height: 1.7;
  word-break: break-word;
}

.question-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid #e9eef5;
  flex-wrap: wrap;
}

.question-card-meta {
  color: #7b8794;
  font-size: 13px;
}

.question-score-editor {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.question-score-label {
  color: #486581;
  font-size: 13px;
  font-weight: 600;
}

@media (max-width: 900px) {
  .question-list-header {
    flex-direction: column;
  }

  .question-card-head,
  .question-card-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
