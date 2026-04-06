<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { answerApi, attemptApi } from '@/api/services'
import { parseJsonSafe } from '@/utils/format'
import { QUESTION_TYPE_OPTIONS, labelBy } from '@/constants/enums'

const route = useRoute()
const router = useRouter()
const attemptId = computed(() => Number(route.params.attemptId))

const loading = ref(false)
const actionLoading = ref(false)
const questions = ref([])
const currentIndex = ref(0)

const answerInput = ref('')
const multiAnswer = ref([])
const syncingEditor = ref(false)

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)
const currentSnapshot = computed(() => parseJsonSafe(currentQuestion.value?.snapshotJson, {}) || {})
const currentQuestionType = computed(() => Number(currentSnapshot.value.questionType || 0))
const currentOptions = computed(() => {
  const raw = Array.isArray(currentSnapshot.value.options) ? currentSnapshot.value.options : []
  return [...raw].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
})
const currentCases = computed(() => {
  const raw = Array.isArray(currentSnapshot.value.cases) ? currentSnapshot.value.cases : []
  return [...raw].sort((a, b) => (a.caseNo || 0) - (b.caseNo || 0))
})

const isSingleChoice = computed(() => [1, 3].includes(currentQuestionType.value))
const isMultiChoice = computed(() => currentQuestionType.value === 2)

function syncEditorFromCurrent() {
  syncingEditor.value = true
  const value = currentQuestion.value?.answerContent || ''
  if (isMultiChoice.value) {
    multiAnswer.value = value
      .split(',')
      .map((v) => v.trim())
      .filter(Boolean)
    answerInput.value = ''
  } else {
    answerInput.value = value
    multiAnswer.value = []
  }
  nextTick(() => {
    syncingEditor.value = false
  })
}

function syncCurrentQuestionFromEditor() {
  if (syncingEditor.value || !currentQuestion.value) {
    return
  }
  currentQuestion.value.answerContent = buildAnswerContent()
}

watch(currentQuestion, () => {
  syncEditorFromCurrent()
})

watch(answerInput, () => {
  syncCurrentQuestionFromEditor()
})

watch(
  multiAnswer,
  () => {
    syncCurrentQuestionFromEditor()
  },
  { deep: true },
)

function answerStatusText(code) {
  if (code === 2) return '已提交'
  return '草稿'
}

function buildAnswerContent() {
  if (isMultiChoice.value) {
    return multiAnswer.value.join(',')
  }
  return answerInput.value || ''
}

async function loadQuestions() {
  loading.value = true
  try {
    const data = await attemptApi.questions(attemptId.value)
    questions.value = data || []
    if (questions.value.length === 0) {
      ElMessage.warning('该作答没有题目')
      return
    }
    currentIndex.value = 0
    syncEditorFromCurrent()
  } catch (error) {
    ElMessage.error(error.message || '加载题目失败')
  } finally {
    loading.value = false
  }
}

function jumpTo(index) {
  if (index < 0 || index >= questions.value.length) return
  syncCurrentQuestionFromEditor()
  currentIndex.value = index
}

async function saveAllDrafts(options = {}) {
  syncCurrentQuestionFromEditor()
  const draftQuestions = questions.value.filter((item) => item?.answerId)
  if (!draftQuestions.length) {
    if (!options.silent) {
      ElMessage.warning('当前作答没有可保存的题目')
    }
    return
  }
  actionLoading.value = true
  try {
    await Promise.all(
      draftQuestions.map((item) => answerApi.saveDraft(item.answerId, item.answerContent || '')),
    )
    draftQuestions.forEach((item) => {
      item.answerStatus = 1
    })
    if (!options.silent) {
      ElMessage.success('草稿已保存')
    }
  } catch (error) {
    if (!options.silent) {
      ElMessage.error(error.message || '保存失败')
    }
    throw error
  } finally {
    actionLoading.value = false
  }
}

async function submitWholeAttempt() {
  try {
    await ElMessageBox.confirm('确认提交整份作答？提交后不可修改。', '提交确认', { type: 'warning' })
    await saveAllDrafts({ silent: true })
    actionLoading.value = true
    await attemptApi.submit(attemptId.value)
    ElMessage.success('整份作答提交成功，请等待系统批改后到作答记录查看成绩')
    router.replace('/attempts/history')
  } catch (error) {
    if (error?.code === 'TIMEOUT') {
      ElMessage.success('作答已提交，系统正在批改中，请稍后到作答记录查看成绩')
      router.replace('/attempts/history')
      return
    }
    if (error !== 'cancel') {
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadQuestions)
</script>

<template>
  <div class="attempt-root">
    <el-card class="page-card nav-card">
      <h3 class="card-title">题目导航</h3>
      <div class="question-nav">
        <el-tag
          v-for="(item, idx) in questions"
          :key="item.attemptQuestionId"
          :effect="idx === currentIndex ? 'dark' : 'plain'"
          :type="item.answerStatus === 2 ? 'success' : 'info'"
          class="nav-tag"
          @click="jumpTo(idx)"
        >
          {{ idx + 1 }}. {{ answerStatusText(item.answerStatus) }}
        </el-tag>
      </div>
    </el-card>

    <el-card class="page-card" v-loading="loading">
      <template v-if="currentQuestion">
        <h3 class="card-title">
          第{{ currentIndex + 1 }} 题
          <el-tag type="warning" effect="plain" style="margin-left: 8px;">
            {{ labelBy(QUESTION_TYPE_OPTIONS, currentQuestionType, '未知题型') }}
          </el-tag>
          <el-tag type="success" effect="plain" style="margin-left: 8px;">
            分值 {{ currentQuestion.score || 0 }}
          </el-tag>
        </h3>
        <p class="question-stem">{{ currentSnapshot.stem || '-' }}</p>

        <template v-if="isSingleChoice">
          <el-radio-group v-model="answerInput" class="vertical-options">
            <el-radio
              v-for="opt in currentOptions"
              :key="opt.optionLabel"
              :label="opt.optionLabel"
            >
              {{ opt.optionLabel }}. {{ opt.optionContent }}
            </el-radio>
          </el-radio-group>
        </template>

        <template v-else-if="isMultiChoice">
          <el-checkbox-group v-model="multiAnswer" class="vertical-options">
            <el-checkbox
              v-for="opt in currentOptions"
              :key="opt.optionLabel"
              :label="opt.optionLabel"
            >
              {{ opt.optionLabel }}. {{ opt.optionContent }}
            </el-checkbox>
          </el-checkbox-group>
        </template>

        <template v-else>
          <el-input
            v-model="answerInput"
            type="textarea"
            :rows="8"
            placeholder="请输入你的答案"
          />
        </template>

        <el-divider />

        <template v-if="currentCases.length > 0">
          <h4 class="cases-title">测试样例</h4>
          <el-table :data="currentCases" border>
            <el-table-column prop="caseNo" label="编号" width="80" />
            <el-table-column prop="inputData" label="输入" min-width="180" />
            <el-table-column prop="expectedOutput" label="输出" min-width="180" />
            <el-table-column prop="caseScore" label="分值" width="80" />
            <el-table-column label="示例" width="80">
              <template #default="{ row }">{{ row.isSample ? '是' : '否' }}</template>
            </el-table-column>
          </el-table>
          <el-divider />
        </template>

        <div class="attempt-actions">
          <el-button @click="jumpTo(currentIndex - 1)" :disabled="currentIndex === 0">上一题</el-button>
          <el-button @click="jumpTo(currentIndex + 1)" :disabled="currentIndex >= questions.length - 1">下一题</el-button>
          <el-button type="info" :loading="actionLoading" @click="saveAllDrafts">保存草稿</el-button>
          <el-button type="success" :loading="actionLoading" @click="submitWholeAttempt">提交</el-button>
        </div>
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.attempt-root {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.nav-card .card-title {
  margin-bottom: 10px;
}

.question-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.nav-tag {
  cursor: pointer;
}

.question-stem {
  margin: 8px 0 12px;
  white-space: pre-wrap;
  line-height: 1.7;
}

.vertical-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cases-title {
  margin: 0 0 8px;
  color: #334e68;
}

.attempt-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
