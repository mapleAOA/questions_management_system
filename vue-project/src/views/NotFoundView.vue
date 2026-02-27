<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="not-found">
    <el-card class="not-found-card">
      <h1>404</h1>
      <p>页面不存在或你没有权限访问。</p>
      <el-button type="primary" @click="router.push('/dashboard')">返回控制台</el-button>
    </el-card>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100vh;
  display: grid;
  place-content: center;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa, #e4ecf7);
}

.not-found-card {
  width: 420px;
  max-width: 90vw;
  text-align: center;
}

h1 {
  margin: 0 0 8px;
  font-size: 62px;
  color: #0b3954;
}

p {
  margin: 0 0 16px;
  color: #486581;
}
</style>
