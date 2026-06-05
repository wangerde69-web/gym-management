<template>
  <div class="page-dark">
    <header class="top-nav">
      <div class="top-nav-inner">
        <router-link to="/" class="brand">GYMCORE</router-link>
        <div class="nav-right">
          <router-link to="/login"><n-button type="primary" size="medium">会员登录</n-button></router-link>
        </div>
      </div>
    </header>

    <div class="content-wrap">
      <div class="page-top">
        <h1 class="page-h1" style="margin-bottom: 0">我的预约</h1>
        <router-link to="/" class="back-link">← 返回首页</router-link>
      </div>

      <div v-if="bs.length">
        <div v-for="b in bs" :key="b.id" class="booking-card">
          <div class="booking-info">
            <h3>{{ b.courseName }}</h3>
            <span class="booking-time">⏰ {{ b.bookingTime }}</span>
          </div>
          <div class="booking-actions">
            <n-tag :type="['warning', 'success', 'info'][b.status] || 'info'" size="medium">
              {{ ['待确认', '已确认', '已取消'][b.status] || '未知' }}
            </n-tag>
            <n-button v-if="b.status === 0 || b.status === 1" type="error" size="medium" secondary @click="cancel(b.id)">取消预约</n-button>
            <n-button v-if="b.status === 2" type="primary" size="medium" secondary @click="refund(b.id)">确认退款</n-button>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <p>{{ loaded ? '暂无预约记录' : '正在加载...' }}</p>
        <n-button v-if="loaded" type="primary" size="large" @click="$router.push('/')">去预约课程</n-button>
        <n-button v-else type="primary" size="large" @click="loadBookings">重新加载</n-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NTag, useMessage, useDialog } from 'naive-ui'
import request from '@/api'

// 预约列表状态和消息/对话框初始化
const router = useRouter(), bs = ref([])
const message = useMessage()
const dialog = useDialog()
// 加载是否已完成，用于区分"暂无记录"和"加载失败"
const loaded = ref(false)

// 加载当前用户的预约列表
function loadBookings() {
  request.get('/booking/my').then(r => {
    loaded.value = true
    if (r.code === 200) bs.value = r.data || []
    else if (r.code === 401) { message.warning('请先登录'); router.push('/login') }
  }).catch(() => { loaded.value = true; message.error('加载失败，请稍后重试') })
}

// 取消预约：弹出确认框后调用取消接口
function cancel(id) {
  dialog.warning({
    title: '取消预约',
    content: '确定要取消该预约吗？',
    positiveText: '确定',
    negativeText: '返回',
    onPositiveClick: () => {
      request.put('/booking/cancel/' + id).then(r => {
        if (r.code === 200) { message.success('已取消'); loadBookings() }
      }).catch(() => message.error('请求失败'))
    }
  })
}

// 确认退款：弹出确认框后调用退款接口
function refund(id) {
  dialog.warning({
    title: '确认退款',
    content: '确认退款？',
    positiveText: '确定',
    negativeText: '返回',
    onPositiveClick: () => {
      request.post('/booking/confirm-refund/' + id).then(r => {
        if (r.code === 200) { message.success('退款已完成'); bs.value = bs.value.filter(b => b.id !== id) }
      }).catch(() => message.error('请求失败'))
    }
  })
}

onMounted(loadBookings)
</script>

<style scoped>
/* ====== 我的预约页样式 ====== */
/* 预约记录卡片 */
.booking-card { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 22px 24px; border-radius: var(--radius-lg); border: 1px solid var(--border-subtle); background: rgba(255, 255, 255, 0.01); margin-bottom: 14px; }
.booking-info h3 { font-size: var(--text-lg); font-weight: 700; color: #fff; margin: 0 0 8px; }
.booking-time { font-size: var(--text-sm); color: var(--text-secondary); }
.booking-actions { display: flex; align-items: center; gap: 14px; flex-shrink: 0; }
</style>