<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { paperApi, questionApi } from '@/api/services'
import { formatDateTime, parseJsonSafe } from '@/utils/format'
import { PAPER_TYPE_OPTIONS, labelBy } from '@/constants/enums'
import JsonPreview from '@/components/JsonPreview.vue'

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
  ruleJson: '',
})

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const addQLoading = ref(false)
const addQForm = reactive({
  questionId: undefined,
  orderNo: 1,
  score: 5,
})

const questionOptions = ref([])

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

function resetForm() {
  editingId.value = null
  form.paperTitle = ''
  form.paperDesc = ''
  form.paperType = 1
  form.status = 1
  form.ruleJson = ''
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.paperTitle = row.paperTitle
  form.paperDesc = row.paperDesc
  form.paperType = row.paperType
  form.status = row.status
  form.ruleJson = row.ruleJson || ''
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
      ruleJson: form.ruleJson || null,
    }
    if (editingId.value) {
      await paperApi.update(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await paperApi.create(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function removePaper(paperId) {
  try {
    await ElMessageBox.confirm('确认删除该试卷？', '提示', { type: 'warning' })
    await paperApi.remove(paperId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

async function openDetail(paperId) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await paperApi.detail(paperId)
  } catch (error) {
    ElMessage.error(error.message || '加载试卷详情失败')
  } finally {
    detailLoading.value = false
  }
}

function questionTitleFromSnapshot(snapshotJson) {
  const obj = parseJsonSafe(snapshotJson, {})
  return obj?.title || '-'
}

async function addQuestionToPaper() {
  if (!detail.value?.id) return
  addQLoading.value = true
  try {
    await paperApi.addQuestion(detail.value.id, {
      questionId: addQForm.questionId,
      orderNo: addQForm.orderNo,
      score: addQForm.score,
    })
    ElMessage.success('添加成功')
    addQForm.questionId = undefined
    addQForm.orderNo = (detail.value.questions?.length || 0) + 1
    addQForm.score = 5
    detail.value = await paperApi.detail(detail.value.id)
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '添加失败')
  } finally {
    addQLoading.value = false
  }
}

async function updatePaperQuestion(row) {
  try {
    await paperApi.updatePaperQuestion(row.id, {
      orderNo: row.orderNo,
      score: row.score,
    })
    ElMessage.success('更新成功')
    detail.value = await paperApi.detail(detail.value.id)
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  }
}

async function removePaperQuestion(row) {
  try {
    await paperApi.removePaperQuestion(row.id)
    ElMessage.success('移除成功')
    detail.value = await paperApi.detail(detail.value.id)
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '移除失败')
  }
}

async function recalculatePaper() {
  try {
    await paperApi.recalculate(detail.value.id)
    ElMessage.success('总分已重算')
    detail.value = await paperApi.detail(detail.value.id)
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '重算失败')
  }
}

const currentPaperQuestions = computed(() => detail.value?.questions || [])

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
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ labelBy(PAPER_TYPE_OPTIONS, row.paperType) }}</template>
      </el-table-column>
      <el-table-column prop="totalScore" label="总分" width="80" />
      <el-table-column prop="status" label="状态" width="80" />
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
          <el-option label="启用" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="规则JSON">
        <el-input v-model="form.ruleJson" type="textarea" :rows="4" placeholder="可选" />
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
        <JsonPreview :data="detail.ruleJson || {}" max-height="180px" />
      </template>
    </el-card>

    <el-card class="page-card">
      <h3 class="card-title">添加题目</h3>
      <div class="page-toolbar">
        <el-select v-model="addQForm.questionId" filterable style="width: 360px" placeholder="选择题目">
          <el-option
            v-for="q in questionOptions"
            :key="q.id"
            :label="`${q.id} - ${q.title}`"
            :value="q.id"
          />
        </el-select>
        <el-input-number v-model="addQForm.orderNo" :min="1" />
        <el-input-number v-model="addQForm.score" :min="0" />
        <el-button type="primary" :loading="addQLoading" @click="addQuestionToPaper">添加</el-button>
        <el-button type="warning" @click="recalculatePaper">重算总分</el-button>
      </div>
    </el-card>

    <el-card class="page-card">
      <h3 class="card-title">题目列表</h3>
      <el-table :data="currentPaperQuestions" border>
        <el-table-column prop="id" label="试卷题编号" width="90" />
        <el-table-column prop="questionId" label="题目编号" width="90" />
        <el-table-column label="标题" min-width="220">
          <template #default="{ row }">{{ questionTitleFromSnapshot(row.snapshotJson) }}</template>
        </el-table-column>
        <el-table-column label="顺序" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.orderNo" :min="1" />
          </template>
        </el-table-column>
        <el-table-column label="分值" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.score" :min="0" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="updatePaperQuestion(row)">更新</el-button>
            <el-button link type="danger" @click="removePaperQuestion(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </el-drawer>
</template>

<style scoped>
.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
