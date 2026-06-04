<!-- 用户菜单组件：显示用户头像和姓名，点击展开下拉菜单（我的卡、预约、修改信息、退出登录） -->
<template>
  <n-dropdown :options="options" placement="bottom-end" @select="onSelect">
    <div class="user-badge">
      <img v-if="avatar" :src="avatar" class="avatar-img" />
      <span v-else class="avatar-fallback">{{ name?.[0] || 'U' }}</span>
      <span class="user-name">{{ name }}</span>
      <n-icon size="16" style="color: var(--text-muted); margin-left: 2px">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
      </n-icon>
    </div>
  </n-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { NDropdown, NIcon } from 'naive-ui'

const emit = defineEmits(['command'])

// 从本地存储读取当前登录用户信息
const name = computed(() => localStorage.getItem('name') || localStorage.getItem('username') || '')
const avatar = computed(() => localStorage.getItem('avatar') || '')

// 下拉菜单选项定义
const options = [
  { key: 'cards', label: '我的会员卡' },
  { key: 'bookings', label: '我的预约' },
  { key: 'profile', label: '修改信息' },
  { type: 'divider', key: 'd1' },
  { key: 'logout', label: '退出登录' }
]

// 菜单项选中事件：向父组件派发命令标识
function onSelect(key) {
  emit('command', key)
}
</script>

<style scoped>
/* 用户徽章容器：头像 + 姓名 + 下拉箭头 */
.user-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 16px;
  border-radius: 24px;
  border: 1px solid rgba(201, 169, 110, 0.15);
  background: rgba(255, 255, 255, 0.04);
  transition: background 0.2s;
}
.user-badge:hover { background: rgba(255, 255, 255, 0.07); }
/* 头像图片和无头像时的字母缩写回退 */
.avatar-img, .avatar-fallback {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}
.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  font-weight: 700;
  background: linear-gradient(135deg, var(--gold), #a86006);
}
.user-name { font-size: var(--text-sm); color: rgba(255, 255, 255, 0.8); }
</style>