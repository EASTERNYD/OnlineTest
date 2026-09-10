<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="title">
        <el-icon :size="30" color="#409eff"><Reading /></el-icon>
        <h2>智能在线考试系统</h2>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中…' : '登 录' }}
        </el-button>
      </el-form>
      <div class="tip">
        默认账号：管理员 admin / admin123 ｜ 学生 student / 123456
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { ROLE } from '@/utils/constants'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 登录按钮防抖：loading 期间禁止重复提交
async function handleLogin() {
  if (loading.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await userStore.login({ ...form })
    ElMessage.success(`欢迎，${data.nickname}`)
    router.push(data.role === ROLE.ADMIN ? '/admin/dashboard' : '/home')
  } catch (e) {
    // 错误提示由响应拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f6feb 0%, #4facfe 100%);
}

.login-card {
  width: 400px;
  padding: 8px 12px 16px;
  border-radius: 10px;
}

.title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 24px;
}

.title h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.login-btn {
  width: 100%;
  margin-top: 4px;
}

.tip {
  margin-top: 16px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
