<!-- 应用根组件：Naive UI 全局配置 + 消息/对话框提供者 + 路由视图（带淡入淡出过渡） -->
<template>
  <n-config-provider :theme="naiveTheme" :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <router-view v-slot="{ Component, route }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { NConfigProvider, NMessageProvider, NDialogProvider, darkTheme } from 'naive-ui'

// Naive UI 主题色覆写：金色调色板
const themeOverrides = {
  common: {
    primaryColor: '#c9a96e',
    primaryColorHover: '#d4b87a',
    primaryColorPressed: '#b8944e',
    primaryColorSuppl: '#c9a96e'
  }
}

// 检测当前路由是否在 admin 下，admin 用 lightTheme，其他用 darkTheme
import { useRoute } from 'vue-router'
const route = useRoute()
const naiveTheme = computed(() => {
  return route.path.startsWith('/admin') ? null : darkTheme
})
</script>

<style>
/* 页面路由切换淡入淡出过渡动画 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease;
}
.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}
</style>