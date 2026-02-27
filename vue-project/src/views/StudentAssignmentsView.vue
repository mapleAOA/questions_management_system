<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { assignmentApi, attemptApi, questionApi, tagApi } from '@/api/services'
import { formatDateTime, splitCsv } from '@/utils/format'
import {
  ASSIGNMENT_STATUS_OPTIONS,
  CHAPTER_OPTIONS,
  QUESTION_TYPE_OPTIONS,
  labelBy,
  typeBy,
} from '@/constants/enums'

const router = useRouter()
const route = useRoute()
const bankOnly = computed(() => route.name === 'student-question-bank')

const loading = ref(false)
const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const status = ref('all')

const practiceLoading = ref(false)
const practiceForm = reactive({
  mode: 'random',
  totalScore: 100,
  tagIdsCsv: '',
  questionTypesCsv: '',
  chapters: [],
})

const tags = ref([])
const questionLoading = ref(false)
const questionList = ref([])
const questionPage = ref(1)
const questionSize = ref(10)
const questionTotal = ref(0)
const selectedQuestionIds = ref([])

const questionQuery = reactive({
  keyword: '',
  chapter: '',
  difficulty: undefined,
  questionType: undefined,
  tagIds: [],
})

async function loadAssignments() {
  loading.value = true
  try {
    const data = await assignmentApi.my(status.value, page.value, size.value)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载作业失败')
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  try {
    tags.value = await tagApi.list('')
  } catch {
    tags.value = []
  }
}

async function loadQuestionBank() {
  questionLoading.value = true
  try {
    const data = await questionApi.search({
      studentView: true,
      keyword: questionQuery.keyword,
      chapter: questionQuery.chapter || undefined,
      difficulty: questionQuery.difficulty,
      questionType: questionQuery.questionType,
      tagIds: questionQuery.tagIds.length ? questionQuery.tagIds.join(',') : undefined,
      page: questionPage.value,
      size: questionSize.value,
    })
    questionList.value = data.list || []
    questionTotal.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载题库失败')
  } finally {
    questionLoading.value = false
  }
}

function resetQuestionQuery() {
  questionQuery.keyword = ''
  questionQuery.chapter = ''
  questionQuery.difficulty = undefined
  questionQuery.questionType = undefined
  questionQuery.tagIds = []
  questionPage.value = 1
  loadQuestionBank()
}

function handleQuestionSelectionChange(rows) {
  selectedQuestionIds.value = (rows || []).map((row) => row.id)
}

async function startAssignment(assignmentId) {
  try {
    const data = await attemptApi.startAssignment(assignmentId)
    ElMessage.success(`作答已开始（第${data.attemptNo}次）`)
    router.push(`/attempts/${data.attemptId}/work`)
  } catch (error) {
    ElMessage.error(error.message || '开始作答失败')
  }
}

async function startPracticeByScope(scope) {
  practiceLoading.value = true
  try {
    const payload = {
      mode: practiceForm.mode,
      totalScore: Number(practiceForm.totalScore) || 100,
      scope,
    }
    const data = await attemptApi.startPractice(payload)
    ElMessage.success('练习已生成')
    router.push(`/attempts/${data.attemptId}/work`)
  } catch (error) {
    ElMessage.error(error.message || '生成练习失败')
  } finally {
    practiceLoading.value = false
  }
}

async function startQuickPractice() {
  await startPracticeByScope({
    tagIds: splitCsv(practiceForm.tagIdsCsv, (v) => Number(v)).filter((v) => !Number.isNaN(v)),
    chapters: practiceForm.chapters,
    questionTypes: splitCsv(practiceForm.questionTypesCsv, (v) => Number(v)).filter((v) => !Number.isNaN(v)),
  })
}

async function startSelectedPractice() {
  if (!selectedQuestionIds.value.length) {
    ElMessage.warning('请先勾选至少一道题')
    return
  }
  await startPracticeByScope({ questionIds: selectedQuestionIds.value })
}

onMounted(async () => {
  await loadTags()
  await loadAssignments()
  await loadQuestionBank()
})
</script>

<template>
  <el-card v-if="!bankOnly" class="page-card">
    <h3 class="card-title">我的作业</h3>
    <div class="page-toolbar">
      <el-select v-model="status" style="width: 180px">
        <el-option label="全部" value="all" />
        <el-option label="进行中" value="ongoing" />
        <el-option label="已过期" value="expired" />
      </el-select>
      <el-button type="primary" @click="page = 1; loadAssignments()">查询</el-button>
      <el-button @click="status = 'all'; page = 1; loadAssignments()">重置</el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="assignmentId" label="作业ID" width="90" />
      <el-table-column prop="assignmentTitle" label="标题" min-width="220" />
      <el-table-column label="开始时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="截止时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column prop="timeLimitMin" label="限时(分钟)" width="110" />
      <el-table-column prop="myAttemptCount" label="我的次数" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="typeBy(ASSIGNMENT_STATUS_OPTIONS, row.publishStatus)">
            {{ labelBy(ASSIGNMENT_STATUS_OPTIONS, row.publishStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="startAssignment(row.assignmentId)">开始作答</el-button>
          <el-button link @click="router.push('/attempts/history')">作答记录</el-button>
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
      @size-change="(v) => { size = v; page = 1; loadAssignments() }"
      @current-change="(v) => { page = v; loadAssignments() }"
    />
  </el-card>

  <el-card v-if="!bankOnly" class="page-card">
    <h3 class="card-title">快速练习</h3>
    <el-form label-width="120px">
      <el-form-item label="模式">
        <el-radio-group v-model="practiceForm.mode">
          <el-radio-button label="random">随机模式</el-radio-button>
          <el-radio-button label="adaptive">自适应模式</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="总分">
        <el-input-number v-model="practiceForm.totalScore" :min="10" :step="10" />
      </el-form-item>
      <el-form-item label="章节">
        <el-select v-model="practiceForm.chapters" multiple collapse-tags style="width: 420px" placeholder="可选">
          <el-option
            v-for="opt in CHAPTER_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="标签ID列表">
        <el-input v-model="practiceForm.tagIdsCsv" placeholder="例如 1,2,3（可选）" />
      </el-form-item>
      <el-form-item label="题型列表">
        <el-input v-model="practiceForm.questionTypesCsv" placeholder="例如 1,2,6（可选）" />
      </el-form-item>
      <el-form-item>
        <el-button type="success" :loading="practiceLoading" @click="startQuickPractice">生成练习并开始</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-card class="page-card">
    <h3 class="card-title">题库练习（可勾选多题）</h3>
    <div class="page-toolbar">
      <el-input v-model="questionQuery.keyword" clearable style="width: 190px" placeholder="关键词" />
      <el-select v-model="questionQuery.chapter" clearable style="width: 170px" placeholder="章节">
        <el-option
          v-for="opt in CHAPTER_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-select v-model="questionQuery.questionType" clearable style="width: 130px" placeholder="题型">
        <el-option
          v-for="opt in QUESTION_TYPE_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-select v-model="questionQuery.difficulty" clearable style="width: 110px" placeholder="难度">
        <el-option v-for="n in [1, 2, 3, 4, 5]" :key="n" :label="`${n}`" :value="n" />
      </el-select>
      <el-select v-model="questionQuery.tagIds" multiple collapse-tags style="width: 260px" placeholder="标签">
        <el-option v-for="tag in tags" :key="tag.id" :label="tag.tagName" :value="tag.id" />
      </el-select>
      <el-button type="primary" @click="questionPage = 1; loadQuestionBank()">查询</el-button>
      <el-button @click="resetQuestionQuery">重置</el-button>
      <el-button
        type="success"
        :loading="practiceLoading"
        :disabled="selectedQuestionIds.length === 0"
        @click="startSelectedPractice"
      >
        用选中题开始练习
      </el-button>
    </div>

    <el-table :data="questionList" border v-loading="questionLoading" @selection-change="handleQuestionSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
      <el-table-column label="题型" width="110">
        <template #default="{ row }">{{ labelBy(QUESTION_TYPE_OPTIONS, row.questionType) }}</template>
      </el-table-column>
      <el-table-column prop="difficulty" label="难度" width="80" />
      <el-table-column prop="chapter" label="章节" width="140" show-overflow-tooltip />
      <el-table-column label="标签" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ (row.tagNames || []).join('、') || '-' }}</template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="table-pager"
      background
      layout="total, sizes, prev, pager, next"
      :current-page="questionPage"
      :page-size="questionSize"
      :page-sizes="[10, 20, 50]"
      :total="questionTotal"
      @size-change="(v) => { questionSize = v; questionPage = 1; loadQuestionBank() }"
      @current-change="(v) => { questionPage = v; loadQuestionBank() }"
    />
  </el-card>
</template>

<style scoped>
.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>


