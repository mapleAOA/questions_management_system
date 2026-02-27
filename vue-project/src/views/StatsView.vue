<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { statsApi } from '@/api/services'
import { formatDateTime } from '@/utils/format'
import { TAG_TYPE_OPTIONS } from '@/constants/enums'

const ability = ref({ abilityScore: 0 })

const masteryTagType = ref(1)
const masteryList = ref([])

const wrongQuery = reactive({
  tagId: undefined,
  chapter: '',
  resolvedMode: 'all',
})
const wrongList = ref([])
const wrongPage = ref(1)
const wrongSize = ref(10)
const wrongTotal = ref(0)
const wrongLoading = ref(false)

const statQuery = reactive({
  questionId: undefined,
})
const statList = ref([])
const statPage = ref(1)
const statSize = ref(10)
const statTotal = ref(0)
const statLoading = ref(false)

async function loadAbility() {
  try {
    ability.value = await statsApi.ability()
  } catch (error) {
    ElMessage.error(error.message || '加载能力值失败')
  }
}

async function loadMastery() {
  try {
    masteryList.value = await statsApi.mastery(masteryTagType.value)
  } catch (error) {
    ElMessage.error(error.message || '加载掌握度失败')
  }
}

function resolvedModeToBoolean(mode) {
  if (mode === 'resolved') return true
  if (mode === 'unresolved') return false
  return undefined
}

async function loadWrongQuestions() {
  wrongLoading.value = true
  try {
    const data = await statsApi.wrongQuestions({
      tagId: wrongQuery.tagId,
      chapter: wrongQuery.chapter,
      isResolved: resolvedModeToBoolean(wrongQuery.resolvedMode),
      page: wrongPage.value,
      size: wrongSize.value,
    })
    wrongList.value = data.list || []
    wrongTotal.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载错题失败')
  } finally {
    wrongLoading.value = false
  }
}

async function resolveWrong(questionId) {
  try {
    await statsApi.resolveWrongQuestion(questionId)
    ElMessage.success('已标记为已掌握')
    loadWrongQuestions()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

async function loadQuestionStats() {
  statLoading.value = true
  try {
    const data = await statsApi.questionStats({
      questionId: statQuery.questionId,
      page: statPage.value,
      size: statSize.value,
    })
    statList.value = data.list || []
    statTotal.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载题目统计失败')
  } finally {
    statLoading.value = false
  }
}

onMounted(() => {
  loadAbility()
  loadMastery()
  loadWrongQuestions()
  loadQuestionStats()
})
</script>

<template>
  <el-card class="page-card">
    <h3 class="card-title">学习能力总览</h3>
    <div class="ability-box">
      <div class="ability-num">{{ ability.abilityScore ?? 0 }}</div>
      <div class="ability-desc">当前能力值（0-100）</div>
    </div>
  </el-card>

  <el-tabs>
    <el-tab-pane label="掌握度">
      <el-card class="page-card">
        <div class="page-toolbar">
          <el-select v-model="masteryTagType" style="width: 180px">
            <el-option
              v-for="opt in TAG_TYPE_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <el-button type="primary" @click="loadMastery">刷新</el-button>
        </div>
        <el-table :data="masteryList" border>
          <el-table-column prop="tagId" label="标签ID" width="90" />
          <el-table-column prop="tagName" label="标签名" min-width="160" />
          <el-table-column prop="masteryValue" label="掌握度" width="100" />
          <el-table-column prop="correctCount" label="正确次数" width="100" />
          <el-table-column prop="attemptCount" label="作答次数" width="100" />
          <el-table-column label="更新时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-tab-pane>

    <el-tab-pane label="错题记录">
      <el-card class="page-card">
        <div class="page-toolbar">
          <el-input-number v-model="wrongQuery.tagId" :min="1" placeholder="标签ID" />
          <el-input v-model="wrongQuery.chapter" style="width: 200px" clearable placeholder="章节" />
          <el-select v-model="wrongQuery.resolvedMode" style="width: 150px">
            <el-option label="全部" value="all" />
            <el-option label="已解决" value="resolved" />
            <el-option label="未解决" value="unresolved" />
          </el-select>
          <el-button type="primary" @click="wrongPage = 1; loadWrongQuestions()">查询</el-button>
        </div>

        <el-table :data="wrongList" border v-loading="wrongLoading">
          <el-table-column prop="questionId" label="题目ID" width="90" />
          <el-table-column prop="wrongCount" label="错题次数" width="90" />
          <el-table-column label="首次错题" width="180">
            <template #default="{ row }">{{ formatDateTime(row.firstWrongAt) }}</template>
          </el-table-column>
          <el-table-column label="最近错题" width="180">
            <template #default="{ row }">{{ formatDateTime(row.lastWrongAt) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.isResolved ? 'success' : 'warning'">
                {{ row.isResolved ? '已解决' : '未解决' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="success" @click="resolveWrong(row.questionId)">标记掌握</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="table-pager"
          background
          layout="total, sizes, prev, pager, next"
          :current-page="wrongPage"
          :page-size="wrongSize"
          :page-sizes="[10, 20, 50]"
          :total="wrongTotal"
          @size-change="(v) => { wrongSize = v; wrongPage = 1; loadWrongQuestions() }"
          @current-change="(v) => { wrongPage = v; loadWrongQuestions() }"
        />
      </el-card>
    </el-tab-pane>

    <el-tab-pane label="题目统计">
      <el-card class="page-card">
        <div class="page-toolbar">
          <el-input-number v-model="statQuery.questionId" :min="1" placeholder="题目ID" />
          <el-button type="primary" @click="statPage = 1; loadQuestionStats()">查询</el-button>
          <el-button @click="statQuery.questionId = undefined; statPage = 1; loadQuestionStats()">重置</el-button>
        </div>

        <el-table :data="statList" border v-loading="statLoading">
          <el-table-column prop="questionId" label="题目ID" width="90" />
          <el-table-column prop="attemptCount" label="作答次数" width="110" />
          <el-table-column prop="correctCount" label="正确次数" width="110" />
          <el-table-column label="正确率" width="100">
            <template #default="{ row }">
              {{
                row.attemptCount
                  ? `${((row.correctCount / row.attemptCount) * 100).toFixed(1)}%`
                  : '-'
              }}
            </template>
          </el-table-column>
          <el-table-column label="最近作答时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.lastAttemptAt) }}</template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="table-pager"
          background
          layout="total, sizes, prev, pager, next"
          :current-page="statPage"
          :page-size="statSize"
          :page-sizes="[10, 20, 50]"
          :total="statTotal"
          @size-change="(v) => { statSize = v; statPage = 1; loadQuestionStats() }"
          @current-change="(v) => { statPage = v; loadQuestionStats() }"
        />
      </el-card>
    </el-tab-pane>
  </el-tabs>
</template>

<style scoped>
.ability-box {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 220px;
  padding: 18px;
  border-radius: 14px;
  border: 1px solid #dbe8f4;
  background: linear-gradient(140deg, #edf7ff, #fef3e7);
}

.ability-num {
  font-size: 48px;
  line-height: 1;
  color: #0b3954;
  font-weight: 700;
}

.ability-desc {
  margin-top: 8px;
  color: #486581;
}

.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
