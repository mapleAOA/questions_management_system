<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { assignmentApi, classApi, paperApi } from '@/api/services'
import { formatDateTime, splitCsv, toLocalDateTimeString } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)

const papers = ref([])
const classes = ref([])

const dialogVisible = ref(false)
const saveLoading = ref(false)
const editingId = ref(null)
const form = reactive({
  paperId: undefined,
  assignmentTitle: '',
  assignmentDesc: '',
  startTime: '',
  endTime: '',
  timeLimitMin: 0,
  maxAttempts: 1,
  shuffleOptions: 0,
})

const targetVisible = ref(false)
const targetLoading = ref(false)
const targetForm = reactive({
  assignmentId: undefined,
  userIdsCsv: '',
  classIds: [],
})

const statusLabel = {
  1: '草稿',
  2: '已发布',
  3: '已关闭',
}

const statusType = {
  1: 'info',
  2: 'success',
  3: 'danger',
}

async function loadPapers() {
  try {
    const data = await paperApi.page(1, 200)
    papers.value = data.list || []
  } catch {
    papers.value = []
  }
}

async function loadClasses() {
  try {
    const data = await classApi.mine()
    classes.value = Array.isArray(data) ? data : []
  } catch {
    classes.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await assignmentApi.page(page.value, size.value)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载作业/考试失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.paperId = undefined
  form.assignmentTitle = ''
  form.assignmentDesc = ''
  form.startTime = ''
  form.endTime = ''
  form.timeLimitMin = 0
  form.maxAttempts = 1
  form.shuffleOptions = 0
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.paperId = row.paperId
  form.assignmentTitle = row.assignmentTitle
  form.assignmentDesc = row.assignmentDesc
  form.startTime = toLocalDateTimeString(row.startTime) || ''
  form.endTime = toLocalDateTimeString(row.endTime) || ''
  form.timeLimitMin = row.timeLimitMin ?? 0
  form.maxAttempts = row.maxAttempts ?? 1
  form.shuffleOptions = row.shuffleOptions ?? 0
  dialogVisible.value = true
}

async function saveAssignment() {
  saveLoading.value = true
  try {
    const payload = {
      paperId: form.paperId,
      assignmentTitle: form.assignmentTitle,
      assignmentDesc: form.assignmentDesc || null,
      startTime: form.startTime || null,
      endTime: form.endTime,
      timeLimitMin: Number(form.timeLimitMin || 0),
      maxAttempts: Number(form.maxAttempts || 1),
      shuffleOptions: Number(form.shuffleOptions || 0),
    }
    if (editingId.value) {
      await assignmentApi.update(editingId.value, payload)
      ElMessage.success('作业/考试更新成功')
    } else {
      await assignmentApi.create(payload)
      ElMessage.success('作业/考试创建成功，当前为草稿状态')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存作业/考试失败')
  } finally {
    saveLoading.value = false
  }
}

async function removeAssignment(id) {
  try {
    await ElMessageBox.confirm('确认删除该作业/考试？', '提示', { type: 'warning' })
    await assignmentApi.remove(id)
    ElMessage.success('作业/考试已删除')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除作业/考试失败')
    }
  }
}

async function publishAssignment(id) {
  try {
    await assignmentApi.publish(id)
    ElMessage.success('作业/考试已发布')
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '发布作业/考试失败')
  }
}

async function closeAssignment(id) {
  try {
    await assignmentApi.close(id)
    ElMessage.success('作业/考试已关闭')
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '关闭作业/考试失败')
  }
}

function openTargetDialog(row) {
  targetForm.assignmentId = row.id
  targetForm.userIdsCsv = ''
  targetForm.classIds = []
  targetVisible.value = true
}

async function saveTargets() {
  targetLoading.value = true
  try {
    const userIds = splitCsv(targetForm.userIdsCsv, (v) => Number(v)).filter((v) => !Number.isNaN(v))
    const classIds = (targetForm.classIds || []).map((v) => Number(v)).filter((v) => !Number.isNaN(v))

    await assignmentApi.setTargets(targetForm.assignmentId, {
      userIds,
      classIds,
    })
    ElMessage.success('目标设置已更新')
    targetVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '更新目标设置失败')
  } finally {
    targetLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadPapers(), loadClasses()])
  loadData()
})
</script>

<template>
  <el-card class="page-card">
    <h3 class="card-title">作业/考试管理</h3>
    <div class="page-toolbar">
      <el-button type="success" @click="openCreate">新建作业/考试</el-button>
      <el-button type="primary" @click="loadData">刷新</el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="assignmentTitle" label="标题" min-width="200" />
      <el-table-column prop="paperId" label="试卷ID" width="90" />
      <el-table-column label="时间范围" min-width="280">
        <template #default="{ row }">
          {{ formatDateTime(row.startTime) }} ~ {{ formatDateTime(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="maxAttempts" label="最大作答次数" width="110" />
      <el-table-column prop="timeLimitMin" label="限时(分钟)" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType[row.publishStatus] || 'info'">
            {{ statusLabel[row.publishStatus] || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="360" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="success" @click="publishAssignment(row.id)">发布</el-button>
          <el-button link type="warning" @click="closeAssignment(row.id)">关闭</el-button>
          <el-button link type="info" @click="openTargetDialog(row)">目标</el-button>
          <el-button link type="danger" @click="removeAssignment(row.id)">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑作业/考试' : '新建作业/考试'" width="760px">
    <el-form label-width="110px">
      <el-alert
        title="保存后默认为草稿，可在列表中再执行发布。题目顺序固定，选项乱序不会影响客观题判分。"
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      />
      <el-form-item label="试卷">
        <el-select v-model="form.paperId" filterable style="width: 100%">
          <el-option v-for="paper in papers" :key="paper.id" :value="paper.id" :label="`${paper.id} - ${paper.paperTitle}`" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="form.assignmentTitle" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.assignmentDesc" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker
          v-model="form.startTime"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
          placeholder="可选"
        />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker
          v-model="form.endTime"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="限时(分钟)">
            <el-input-number v-model="form.timeLimitMin" :min="0" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大作答次数">
            <el-input-number v-model="form.maxAttempts" :min="1" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="选项乱序">
        <el-switch v-model="form.shuffleOptions" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saveLoading" @click="saveAssignment">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="targetVisible" title="配置目标范围" width="620px">
    <el-form label-width="110px">
      <el-form-item label="作业ID">
        <el-input v-model="targetForm.assignmentId" disabled />
      </el-form-item>
      <el-form-item label="目标班级">
        <el-select v-model="targetForm.classIds" multiple filterable style="width: 100%" placeholder="请选择班级">
          <el-option v-for="clazz in classes" :key="clazz.id" :value="clazz.id" :label="`${clazz.className} (${clazz.classCode})`" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标用户ID">
        <el-input
          v-model="targetForm.userIdsCsv"
          type="textarea"
          :rows="4"
          placeholder="逗号分隔，例如 10001,10002"
        />
      </el-form-item>
      <el-alert
        title="两个字段都留空时，作业对全部学生可见。"
        type="info"
        :closable="false"
      />
    </el-form>
    <template #footer>
      <el-button @click="targetVisible = false">取消</el-button>
      <el-button type="primary" :loading="targetLoading" @click="saveTargets">保存目标设置</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.table-pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
