<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const mode = ref('login')
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
})

const registerForm = reactive({
  username: '',
  password: '',
  displayName: '',
  email: '',
  role: 'STUDENT',
})
const REGISTER_PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d])\S{8,20}$/

async function handleLogin() {
  loading.value = true
  try {
    await auth.login(loginForm)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    router.replace(typeof redirect === 'string' ? redirect : '/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!REGISTER_PASSWORD_PATTERN.test(registerForm.password || '')) {
    ElMessage.warning('密码需为8-20位，且包含字母、数字和特殊字符')
    return
  }
  loading.value = true
  try {
    await auth.register(registerForm)
    ElMessage.success('注册成功')
    const redirect = route.query.redirect
    router.replace(typeof redirect === 'string' ? redirect : '/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-root">
    <div class="login-bg-shape shape-1" />
    <div class="login-bg-shape shape-2" />
    <div class="login-card">
      <div class="login-brand">
        <h1>C语言课程题库系统</h1>
        <p>题库与作业/考试管理系统</p>
      </div>

      <el-radio-group v-model="mode" class="mode-switch">
        <el-radio-button label="login">登录</el-radio-button>
        <el-radio-button label="register">注册</el-radio-button>
      </el-radio-group>

      <el-form v-if="mode === 'login'" :model="loginForm" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" :prefix-icon="User" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            show-password
            :prefix-icon="Lock"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>

      <el-form v-else :model="registerForm" label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="角色">
          <el-radio-group v-model="registerForm.role">
            <el-radio label="STUDENT">学生</el-radio>
            <el-radio label="TEACHER">教师</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" :prefix-icon="User" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="registerForm.password"
            type="password"
            show-password
            :prefix-icon="Lock"
            placeholder="8-20位，需含字母、数字、特殊字符"
            @keyup.enter="handleRegister"
          />
          <p class="password-hint">密码需为 8-20 位，且包含字母、数字和特殊字符</p>
        </el-form-item>
        <el-form-item label="显示名">
          <el-input v-model="registerForm.displayName" placeholder="可选" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="registerForm.email" placeholder="可选" />
        </el-form-item>
        <el-button type="success" class="login-btn" :loading="loading" @click="handleRegister">
          注册
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-root {
  min-height: 100vh;
  display: grid;
  place-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0b3954 0%, #1f7a8c 45%, #f58a07 100%);
}

.login-bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.25;
  filter: blur(2px);
}

.shape-1 {
  width: 460px;
  height: 460px;
  background: #ffe66d;
  top: -120px;
  right: -100px;
}

.shape-2 {
  width: 380px;
  height: 380px;
  background: #9ad1f7;
  bottom: -140px;
  left: -100px;
}

.login-card {
  width: 460px;
  max-width: 94vw;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 48px rgba(6, 26, 45, 0.24);
  padding: 24px;
  position: relative;
  z-index: 2;
}

.login-brand h1 {
  margin: 0;
  color: #0b3954;
  font-size: 28px;
}

.login-brand p {
  margin: 8px 0 18px;
  color: #486581;
  font-size: 14px;
}

.mode-switch {
  margin-bottom: 14px;
}

.login-btn {
  width: 100%;
}

.password-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: #627d98;
  line-height: 1.4;
}

@media (max-width: 768px) {
  .login-card {
    padding: 18px;
  }

  .login-brand h1 {
    font-size: 24px;
  }
}
</style>
