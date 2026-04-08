<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { paperApi, questionApi } from '@/api/services'
import { PAPER_TYPE_OPTIONS, labelBy } from '@/constants/enums'
import { formatDateTime, parseJsonSafe } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)

const editorVisible = ref(false)
const editorLoading = ref(false)
const saveBaseLoading = ref(false)
const addQuestionLoading = ref(false)
const saveQuestionConfigLoading = ref(false)
const currentPaperId = ref(null)

const editorForm = reactive({
  paperTitle: '',
  paperDesc: '',
  paperType: 1,
})

const addQuestionForm = reactive({
  questionId: undefined,
  score: 5,
})

const detail = ref(null)
const questionOptions = ref([])
const questionDrafts = ref([])

function resetEditor() {
  currentPaperId.value = null
  detail.value = null
  editorForm.paperTitle = ''
  editorForm.paperDesc = ''
  editorForm.paperType = 1
  addQuestionForm.questionId = undefined
  addQuestionForm.score = 5
  questionDrafts.value = []
}

function questionSnapshot(snapshotJson) {
  return parseJsonSafe(snapshotJson, {})
}

function questionTextFromSnapshot(snapshotJson) {
  const snapshot = questionSnapshot(snapshotJson)
  return snapshot?.stem || snapshot?.title || '-'
}

function syncQuestionDrafts(questions = []) {
  questionDrafts.value = [...questions]
    .sort((a, b) => Number(a.orderNo || 0) - Number(b.orderNo || 0))
    .map((item) => ({
      ...item,
      score: Number(item.score || 0),
    }))
}

async function loadQuestionOptions() {
  try {
    const data = await questionApi.search({ page: 1, size: 300, status: 2 })
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

async function reloadDetail(paperId) {
  const data = await paperApi.detail(paperId)
  detail.value = data
  currentPaperId.value = data.id
  editorForm.paperTitle = data.paperTitle || ''
  editorForm.paperDesc = data.paperDesc || ''
  editorForm.paperType = data.paperType || 1
  syncQuestionDrafts(data.questions || [])
}

function openCreate() {
  resetEditor()
  editorVisible.value = true
}

async function openEdit(row) {
  editorVisible.value = true
  editorLoading.value = true
  try {
    await reloadDetail(row.id)
  } catch (error) {
    ElMessage.error(error.message || '加载试卷失败')
  } finally {
    editorLoading.value = false
  }
}

async function savePaperBase() {
  saveBaseLoading.value = true
  try {
    const payload = {
      paperTitle: editorForm.paperTitle,
      paperDesc: editorForm.paperDesc || null,
      paperType: Number(editorForm.paperType || 1),
    }
    if (currentPaperId.value) {
      await paperApi.update(currentPaperId.value, payload)
      ElMessage.success('试卷更新成功')
      await reloadDetail(currentPaperId.value)
    } else {
      const paperId = await paperApi.create(payload)
      currentPaperId.value = paperId
      ElMessage.success('试卷创建成功')
      await reloadDetail(paperId)
    }
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存试卷失败')
  } finally {
    saveBaseLoading.value = false
  }
}

async function removePaper(row) {
  try {
    await ElMessageBox.confirm(`确认删除试卷“${row.paperTitle}”？`, '提示', { type: 'warning' })
    await paperApi.remove(row.id)
    ElMessage.success('试卷已删除')
    if (currentPaperId.value === row.id) {
      editorVisible.value = false
      resetEditor()
    }
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除试卷失败')
    }
  }
}

async function addQuestionToPaper() {
  if (!currentPaperId.value) {
    ElMessage.warning('请先保存试卷基础信息')
    return
  }
  if (!addQuestionForm.questionId) {
    ElMessage.warning('请先选择题目')
    return
  }
  addQuestionLoading.value = true
  try {
    await paperApi.addQuestion(currentPaperId.value, {
      questionId: addQuestionForm.questionId,
      orderNo: questionDrafts.value.length + 1,
      score: Number(addQuestionForm.score || 0),
    })
    ElMessage.success('题目添加成功')
    addQuestionForm.questionId = undefined
    addQuestionForm.score = 5
    await reloadDetail(currentPaperId.value)
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '添加题目失败')
  } finally {
    addQuestionLoading.value = false
  }
}

function moveQuestion(index, delta) {
  const targetIndex = index + delta
  if (targetIndex < 0 || targetIndex >= questionDrafts.value.length) {
    return
  }
  const next = [...questionDrafts.value]
  const [row] = next.splice(index, 1)
  next.splice(targetIndex, 0, row)
  questionDrafts.value = next
}

function resetQuestionDrafts() {
  syncQuestionDrafts(detail.value?.questions || [])
}

async function saveQuestionConfig() {
  if (!currentPaperId.value || !questionDrafts.value.length) {
    return
  }
  saveQuestionConfigLoading.value = true
  try {
    await paperApi.batchUpdateQuestions(currentPaperId.value, {
      questions: questionDrafts.value.map((row, index) => ({
        id: row.id,
        orderNo: index + 1,
        score: Number(row.score || 0),
      })),
    })
    ElMessage.success('题目配置已保存')
    await reloadDetail(currentPaperId.value)
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存题目配置失败')
  } finally {
    saveQuestionConfigLoading.value = false
  }
}

async function removePaperQuestion(row) {
  if (!currentPaperId.value) {
    return
  }
  try {
    await ElMessageBox.confirm('确认将该题移出试卷？', '提示', { type: 'warning' })
    await paperApi.removePaperQuestion(row.id)
    ElMessage.success('题目已移出试卷')
    await reloadDetail(currentPaperId.value)
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '移出题目失败')
    }
  }
}

const hasQuestionDraftChanges = computed(() => {
  const source = [...(detail.value?.questions || [])].sort((a, b) => Number(a.orderNo || 0) - Number(b.orderNo || 0))
  if (source.length !== questionDrafts.value.length) {
    return true
  }
  return questionDrafts.value.some((row, index) => {
    const original = source[index]
    return !original || row.id !== original.id || Number(row.score || 0) !== Number(original.score || 0)
  })
})

onMounted(async () => {
  await Promise.all([loadQuestionOptions(), loadData()])
})
</script>

<template>
  <el-card class="page-card">
    <div class="page-head">
      <div>
        <h3 class="card-title">试卷管理</h3>
        <p class="muted">试卷基础信息和题目配置统一在编辑抽屉中完成。</p>
      </div>
      <div class="page-toolbar">
        <el-button type="success" @click="openCreate">新建试卷</el-button>
        <el-button type="primary" @click="loadData">刷新</el-button>
      </div>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="paperTitle" label="标题" min-width="220" />
      <el-table-column label="类型" width="120">
        <template #default="{ row }">{{ labelBy(PAPER_TYPE_OPTIONS, row.paperType) }}</template>
      </el-table-column>
      <el-table-column prop="paperDesc" label="描述" min-width="260" show-overflow-tooltip />
      <el-table-column prop="totalScore" label="总分" width="90" />
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removePaper(row)">删除</el-button>
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

  <el-drawer v-model="editorVisible" :title="currentPaperId ? '编辑试卷' : '新建试卷'" size="72%">
    <div v-loading="editorLoading" class="drawer-body">
      <el-card class="page-card">
        <div class="editor-head">
          <div>
            <h3 class="card-title">基础信息</h3>
            <p class="muted">先保存基础信息，再继续添加题目。</p>
          </div>
          <el-button type="primary" :loading="saveBaseLoading" @click="savePaperBase">保存基础信息</el-button>
        </div>

        <el-form label-width="100px">
          <el-form-item label="标题">
            <el-input v-model="editorForm.paperTitle" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editorForm.paperDesc" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="editorForm.paperType" style="width: 100%">
              <el-option
                v-for="opt in PAPER_TYPE_OPTIONS"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card v-if="!currentPaperId" class="page-card">
        <el-alert type="info" :closable="false" title="当前还没有试卷编号，请先保存基础信息。" />
      </el-card>

      <template v-else>
        <el-card class="page-card">
          <div class="editor-head">
            <div>
              <h3 class="card-title">添加题目</h3>
              <p class="muted">当前总分：{{ detail?.totalScore || 0 }}</p>
            </div>
          </div>
          <div class="page-toolbar">
            <el-select v-model="addQuestionForm.questionId" filterable style="width: 420px" placeholder="请选择题目">
              <el-option
                v-for="q in questionOptions"
                :key="q.id"
                :label="`${q.id} - ${q.title}`"
                :value="q.id"
              />
            </el-select>
            <el-input-number v-model="addQuestionForm.score" :min="0" />
            <el-button type="primary" :loading="addQuestionLoading" @click="addQuestionToPaper">添加题目</el-button>
          </div>
        </el-card>

        <el-card class="page-card">
          <div class="editor-head">
            <div>
              <h3 class="card-title">题目配置</h3>
              <p class="muted">可直接调整顺序和分值，保存后自动重算总分。</p>
            </div>
            <div class="page-toolbar">
              <el-button :disabled="!hasQuestionDraftChanges" @click="resetQuestionDrafts">恢复原顺序</el-button>
              <el-button type="primary" :disabled="!hasQuestionDraftChanges" :loading="saveQuestionConfigLoading" @click="saveQuestionConfig">
                保存题目配置
              </el-button>
            </div>
          </div>

          <el-table :data="questionDrafts" border>
            <el-table-column label="序号" width="80">
              <template #default="{ $index }">{{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column prop="questionId" label="题目ID" width="100" />
            <el-table-column label="题干" min-width="360" show-overflow-tooltip>
              <template #default="{ row }">{{ questionTextFromSnapshot(row.snapshotJson) }}</template>
            </el-table-column>
            <el-table-column label="分值" width="130">
              <template #default="{ row }">
                <el-input-number v-model="row.score" :min="0" />
              </template>
            </el-table-column>
            <el-table-column label="顺序" width="160">
              <template #default="{ $index }">
                <div class="sort-actions">
                  <el-button link type="primary" :disabled="$index === 0" @click="moveQuestion($index, -1)">上移</el-button>
                  <el-button link type="primary" :disabled="$index === questionDrafts.length - 1" @click="moveQuestion($index, 1)">下移</el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="danger" @click="removePaperQuestion(row)">移出</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!questionDrafts.length" description="当前试卷还没有题目" />
        </el-card>
      </template>
    </div>
  </el-drawer>
</template>

<style scoped>
.page-head,
.editor-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.page-head {
  margin-bottom: 12px;
}

.drawer-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}

.sort-actions {
  display: flex;
  gap: 10px;
}
</style>