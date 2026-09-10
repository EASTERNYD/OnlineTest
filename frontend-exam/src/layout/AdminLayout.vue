<template>
  <el-container class="admin-layout">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="aside">
      <div class="logo" @click="router.push('/admin/dashboard')">
        <el-icon :size="24" color="#409eff"><Monitor /></el-icon>
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">考试管理后台</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        router
        class="aside-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>数据概览</template>
        </el-menu-item>
        <el-menu-item index="/admin/banner">
          <el-icon><Picture /></el-icon>
          <template #title>轮播图管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/notice">
          <el-icon><Bell /></el-icon>
          <template #title>公告管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/category">
          <el-icon><FolderOpened /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/question">
          <el-icon><Collection /></el-icon>
          <template #title>题目管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/question/import">
          <el-icon><Upload /></el-icon>
          <template #title>题目导入</template>
        </el-menu-item>
        <el-menu-item index="/admin/exam">
          <el-icon><Document /></el-icon>
          <template #title>试卷管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/record">
          <el-icon><Tickets /></el-icon>
          <template #title>考试记录管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" :size="20" @click="appStore.toggleSidebar()">
            <Expand v-if="appStore.sidebarCollapsed" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">管理端</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta?.title || '' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="30" class="avatar">{{ (userStore.userInfo?.nickname || '管')[0] }}</el-avatar>
            <span class="nickname">{{ userStore.userInfo?.nickname || '管理员' }}</span>
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
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

async function handleCommand(cmd) {
  if (cmd !== 'logout') return
  await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.aside {
  background: #fff;
  border-right: 1px solid var(--el-border-color-light);
  transition: width 0.2s;
  overflow-x: hidden;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 60px;
  padding: 0 16px;
  cursor: pointer;
  border-bottom: 1px solid var(--el-border-color-lighter);
  white-space: nowrap;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
}

.aside-menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid var(--el-border-color-light);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
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
}

.main {
  padding: 16px;
}
</style>
