<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { tagApi } from '@/api/services'
import { TAG_TYPE_OPTIONS, labelBy } from '@/constants/enums'

const loading = ref(false)
const treeLoading = ref(false)
const list = ref([])
const treeData = ref([])
const query = reactive({ keyword: '' })

const dialogVisible = ref(false)
const saveLoading = ref(false)
const editingId = ref(null)

const form = reactive({
  tagName: '',
  tagCode: '',
  parentId: null,
  tagLevel: 1,
  tagType: 1,
  sortOrder: 0,
})

async function loadList() {
  loading.value = true
  try {
    list.value = await tagApi.list(query.keyword)
  } catch (error) {
    ElMessage.error(error.message || '加载标签列表失败')
  } finally {
    loading.value = false
  }
}

async function loadTree() {
  treeLoading.value = true
  try {
    treeData.value = await tagApi.tree()
  } catch {
    treeData.value = []
  } finally {
    treeLoading.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.tagName = ''
  form.tagCode = ''
  form.parentId = null
  form.tagLevel = 1
  form.tagType = 1
  form.sortOrder = 0
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.tagName = row.tagName
  form.tagCode = row.tagCode
  form.parentId = row.parentId || null
  form.tagLevel = row.tagLevel || 1
  form.tagType = row.tagType || 1
  form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

async function saveTag() {
  saveLoading.value = true
  try {
    const payload = {
      tagName: form.tagName,
      tagCode: form.tagCode || null,
      parentId: form.parentId || null,
      tagLevel: Number(form.tagLevel || 1),
      tagType: Number(form.tagType || 1),
      sortOrder: Number(form.sortOrder || 0),
    }
    if (editingId.value) {
      await tagApi.update(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await tagApi.create(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
    loadTree()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function removeTag(tagId) {
  try {
    await ElMessageBox.confirm('确认删除该标签？', '提示', { type: 'warning' })
    await tagApi.remove(tagId)
    ElMessage.success('删除成功')
    loadList()
    loadTree()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadList()
  loadTree()
})
</script>

<template>
  <div class="tag-layout">
    <el-card class="page-card tree-card" v-loading="treeLoading">
      <h3 class="card-title">标签树</h3>
      <el-tree
        :data="treeData"
        node-key="id"
        :props="{ label: 'tagName', children: 'children' }"
        default-expand-all
      >
        <template #default="{ data }">
          <span>{{ data.tagName }} (#{{ data.id }})</span>
        </template>
      </el-tree>
    </el-card>

    <el-card class="page-card">
      <h3 class="card-title">标签管理</h3>
      <div class="page-toolbar">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="关键词"
          style="width: 220px"
          @keyup.enter="loadList"
        />
        <el-button type="primary" @click="loadList">查询</el-button>
        <el-button @click="query.keyword = ''; loadList()">重置</el-button>
        <el-button type="success" @click="openCreate">新建标签</el-button>
      </div>

      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tagName" label="标签名" min-width="160" />
        <el-table-column prop="tagCode" label="编码" width="130" />
        <el-table-column prop="parentId" label="父ID" width="80" />
        <el-table-column prop="tagLevel" label="层级" width="70" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ labelBy(TAG_TYPE_OPTIONS, row.tagType) }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeTag(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑标签' : '新建标签'" width="620px">
    <el-form label-width="90px">
      <el-form-item label="标签名">
        <el-input v-model="form.tagName" />
      </el-form-item>
      <el-form-item label="编码">
        <el-input v-model="form.tagCode" />
      </el-form-item>
      <el-form-item label="父标签">
        <el-select v-model="form.parentId" clearable style="width: 100%">
          <el-option :value="null" label="无（一级标签）" />
          <el-option
            v-for="item in list"
            :key="item.id"
            :value="item.id"
            :label="`${item.id}-${item.tagName}`"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="层级">
        <el-input-number v-model="form.tagLevel" :min="1" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.tagType" style="width: 100%">
          <el-option
            v-for="opt in TAG_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sortOrder" :min="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saveLoading" @click="saveTag">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.tag-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 14px;
}

.tree-card {
  max-height: calc(100vh - 120px);
  overflow: auto;
}

@media (max-width: 1100px) {
  .tag-layout {
    grid-template-columns: 1fr;
  }
}
</style>
