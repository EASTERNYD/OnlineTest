<template>
  <el-container class="student-layout">
    <el-header class="header">
      <div class="brand" @click="router.push('/home')">
        <el-icon :size="26" color="#409eff"><Reading /></el-icon>
        <span class="brand-name">智能在线考试系统</span>
      </div>
      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        :ellipsis="false"
        router
        class="nav-menu"
      >
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/question">题库</el-menu-item>
        <el-menu-item index="/exam">考试</el-menu-item>
        <el-menu-item index="/notice">公告</el-menu-item>
        <el-menu-item index="/rank">排行榜</el-menu-item>
        <el-menu-item index="/record">我的成绩</el-menu-item>
      </el-menu>
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="30" class="avatar">{{ (userStore.userInfo?.nickname || '学')[0] }}</el-avatar>
          <span class="nickname">{{ userStore.userInfo?.nickname || '同学' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 高亮当前一级菜单（详情页归属父级菜单）
const activeMenu = computed(() => {
  if (route.path.startsWith('/notice')) return '/notice'
  if (route.path.startsWith('/exam')) return '/exam'
  return route.path
})

async function handleCommand(cmd) {
  if (cmd !== 'logout') return
  await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.student-layout {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  gap: 24px;
  background: #fff;
  border-bottom: 1px solid var(--el-border-color-light);
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}

.brand-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-color-primary);
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.avatar {
  background: var(--el-color-primary);
}

.nickname {
  font-size: 14px;
  color: #303133;
}

.main {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 16px;
}
</style>
