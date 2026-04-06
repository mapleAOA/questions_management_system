<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionApi, tagApi } from '@/api/services'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import {
  CHAPTER_OPTIONS,
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

const query = reactive({
  keyword: '',
  chapter: '',
  difficulty: undefined,
  questionType: undefined,
  status: undefined,
  source: 'all',
  tagIds: [],
})

const dialogVisible = ref(false)
const saveLoading = ref(false)
const editingId = ref(null)
const llmLoadingId = ref(null)

const isAdmin = computed(() => auth.hasAnyRole(['ADMIN']))
const isTeacher = computed(() => auth.hasAnyRole(['TEACHER']))

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
  tagIds: [],
  options: [newOption(1, 'A'), newOption(2, 'B')],
  cases: [],
})

const showOptions = computed(() => [1, 2].includes(Number(form.questionType)))
const showCases = computed(() => [6].includes(Number(form.questionType)))
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

function resetForm() {
  editingId.value = null
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
      ElMessage.success('更新成功')
    } else {
      await questionApi.create(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
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

async function llmAnalysis(questionId) {
  llmLoadingId.value = questionId
  ElMessage.info('已提交大模型解析请求，正在处理...')
  try {
    const llmCallId = await questionApi.generateLlmAnalysis(questionId)
    ElMessage.success(`大模型解析已生成，调用编号: ${llmCallId}`)
    await loadData()
  } catch (error) {
    if (error?.code === 'TIMEOUT') {
      ElMessage.success('大模型解析请求已提交，后台仍在生成，请稍后刷新列表查看结果')
      await loadData()
      return
    }
    ElMessage.error(error.message || '生成失败')
  } finally {
    llmLoadingId.value = null
  }
}

function appendOption() {
  const sortOrder = form.options.length + 1
  const nextLabel = String.fromCharCode(64 + Math.min(sortOrder, 26))
  form.options.push(newOption(sortOrder, nextLabel))
}

function appendCase() {
  form.cases.push(newCase(form.cases.length + 1))
}

function resetQuery() {
  query.keyword = ''
  query.chapter = ''
  query.questionType = undefined
  query.difficulty = undefined
  query.status = undefined
  query.source = 'all'
  query.tagIds = []
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
        <el-option
          v-for="opt in CHAPTER_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-select v-model="query.questionType" clearable style="width: 130px" placeholder="题型">
        <el-option
          v-for="opt in QUESTION_TYPE_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-select v-model="query.difficulty" clearable style="width: 130px" placeholder="难度">
        <el-option v-for="n in [1, 2, 3, 4, 5]" :key="n" :label="`${n}`" :value="n" />
      </el-select>
      <el-select v-model="query.status" clearable style="width: 130px" placeholder="状态">
        <el-option
          v-for="opt in QUESTION_STATUS_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-select
        v-if="isTeacher && !isAdmin"
        v-model="query.source"
        style="width: 150px"
        placeholder="题目来源"
      >
        <el-option label="全部可用" value="all" />
        <el-option label="我出的题" value="mine" />
        <el-option label="题库题" value="bank" />
      </el-select>
      <el-select v-model="query.tagIds" multiple collapse-tags style="width: 260px" placeholder="筛选标签">
        <el-option v-for="tag in tags" :key="tag.id" :label="tag.tagName" :value="tag.id" />
      </el-select>
      <el-button type="primary" @click="page = 1; loadData()">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button type="success" @click="openCreate">新建题目</el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="题目编号" width="80" />
      <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
      <el-table-column label="题型" width="110">
        <template #default="{ row }">{{ labelBy(QUESTION_TYPE_OPTIONS, row.questionType) }}</template>
      </el-table-column>
      <el-table-column prop="difficulty" label="难度" width="80" />
      <el-table-column prop="chapter" label="章节" width="130" show-overflow-tooltip />
      <el-table-column label="标签" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ (row.tagNames || []).join('、') || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="typeBy(QUESTION_STATUS_OPTIONS, row.status)">
            {{ labelBy(QUESTION_STATUS_OPTIONS, row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button v-if="canManageRow(row)" link type="primary" @click="openEdit(row.id)">编辑</el-button>
          <el-button v-if="canManageRow(row)" link type="success" @click="publishQuestion(row.id)">发布</el-button>
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

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑题目' : '新建题目'" width="960px" destroy-on-close>
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
              <el-option
                v-for="opt in QUESTION_TYPE_OPTIONS"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
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
              <el-option
                v-for="opt in CHAPTER_OPTIONS"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option
                v-for="opt in QUESTION_STATUS_OPTIONS"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
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

      <el-form-item label="题干">
        <el-input v-model="form.stem" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item v-if="showStandardAnswer" label="标准答案">
        <el-input v-model="form.standardAnswer" type="textarea" :rows="3" />
      </el-form-item>
      <el-alert
        v-else
        type="info"
        :closable="false"
        title="单选/多选题会根据“正确选项”自动生成标准答案"
      />
      <el-form-item label="解析">
        <el-input v-model="form.analysisText" type="textarea" :rows="8" class="analysis-input" />
      </el-form-item>

      <template v-if="showOptions">
        <el-divider />
        <div class="sub-title-row">
          <h4>选项（选择题使用）</h4>
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
          <h4>测试样例（编程题可用）</h4>
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
          <el-table-column label="样例" width="90">
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
</style>
