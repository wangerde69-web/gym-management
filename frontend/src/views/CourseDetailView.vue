<template>
  <div class="page-dark">
    <header class="top-nav">
      <div class="top-nav-inner">
        <router-link to="/" class="brand">GYMCORE</router-link>
        <div class="nav-right">
          <UserMenu v-if="logged" @command="onMenu" />
          <router-link v-else to="/login"><n-button type="primary" size="medium">会员登录</n-button></router-link>
        </div>
      </div>
    </header>

    <div class="content-wrap" v-if="c">
      <div class="detail-grid">
        <div class="detail-main">
          <div class="detail-hero">
            <img :src="c.imageUrl || '/images/gym7.jpg'" :alt="c.name" />
            <span class="detail-price">¥{{ c.price }}</span>
          </div>
          <h1>{{ c.name }}</h1>
          <div class="detail-tags">
            <n-tag size="medium">⏱ {{ c.duration || '60分钟' }}</n-tag>
            <n-tag size="medium">👥 {{ c.capacity }}人</n-tag>
            <n-tag v-if="c.timeSlot" size="medium">🕐 {{ c.timeSlot }}</n-tag>
          </div>
          <p class="detail-desc">{{ c.description }}</p>

          <div v-if="coach" class="coach-card">
            <img :src="coach.imageUrl || coach.avatar || '/images/gym7.jpg'" :alt="coach.name" class="coach-avatar" />
            <div class="coach-body">
              <h3>{{ coach.name }}</h3>
              <n-tag type="warning" size="medium">{{ coach.specialty }}</n-tag>
              <div class="coach-exp">{{ coach.experience }}</div>
              <p>{{ coach.intro }}</p>
            </div>
          </div>
        </div>

        <div class="detail-sidebar">
          <h3>预约课程</h3>
          <n-space vertical size="large" style="width: 100%">
            <div>
              <label class="field-label">预约日期</label>
              <n-date-picker v-model:formatted-value="bd" type="date" :is-date-disabled="dd" style="width: 100%" size="large" />
            </div>
            <div>
              <label class="field-label">预约时间</label>
              <div class="time-slots">
                <n-button v-for="s in slots" :key="s" :type="bt === s ? 'primary' : 'default'" size="medium" @click="bt = s" block>{{ s }}</n-button>
              </div>
            </div>
            <n-button type="primary" size="large" :disabled="!ok" block @click="showPay">立即预约</n-button>
          </n-space>
          <p class="booking-note">预约成功后请准时到达场馆</p>
        </div>
      </div>
    </div>
    <div v-else class="empty-state">加载中...</div>

    <PayDialog ref="payRef" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NButton, NTag, NDatePicker, NSpace, useMessage } from 'naive-ui'
import request from '@/api'
import UserMenu from '@/components/UserMenu.vue'
import PayDialog from '@/components/PayDialog.vue'
import { useUserMenu } from '@/composables/useUserMenu'
import { fetchPayQr } from '@/composables/usePayment'

// 课程详情和预约相关状态
const router = useRouter(), route = useRoute()
const { onMenu } = useUserMenu()
const c = ref(null), coach = ref(null), bd = ref(new Date().toISOString().split('T')[0]), bt = ref('')
const payRef = ref(null)
const message = useMessage()

// 登录状态判断（使用 ref + 监听 storage 事件以保持响应式）
const logged = ref(!!localStorage.getItem('token'))
function onStorage() { logged.value = !!localStorage.getItem('token') }
onMounted(() => window.addEventListener('storage', onStorage))
onUnmounted(() => window.removeEventListener('storage', onStorage))
// 可选预约时间段
const allSlots = ['08:00-09:00', '09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00', '16:00-17:00', '19:00-20:00', '20:00-21:00']
// 选今天时自动过滤已过的时间段（按开始时间判断，已开始的不显示）
const slots = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  if (bd.value !== today) return allSlots
  const now = new Date()
  const nowMin = now.getHours() * 60 + now.getMinutes()
  return allSlots.filter(s => {
    const [startH, startM] = s.split('-')[0].split(':').map(Number)
    return nowMin < startH * 60 + startM
  })
})
// 预约按钮启用条件：日期和时间都已选择
const ok = computed(() => bd.value && bt.value)

// 用户菜单命令处理（由 useUserMenu 提供）

// 禁用今天之前的日期选择
function dd(ts) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return ts < today.getTime()
}

// 预约支付流程：校验登录 → 打开支付弹窗 → 提交预约请求
async function showPay() {
  if (!localStorage.getItem('token')) {
    message.warning('请先登录')
    // 登录成功后跳回课程详情页
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!ok.value) { message.warning('请选择日期和时间'); return }
  const qr = await fetchPayQr()
  const result = await payRef.value.open(qr, c.value.price)
  if (!result) return
  // 同时发送日期（bd）和时间段（bt），后端才能知道用户预约的是哪一天哪一时段
  request.post('/booking/add', { courseId: c.value.id, bookingTime: bt.value, bookingDate: bd.value }).then(r => {
    if (r.code === 200) { message.success('预约成功'); router.push('/my-bookings') }
    else message.error(r.msg || '预约失败')
  }).catch(() => message.error('请求失败'))
}

// 页面挂载时根据路由参数加载课程详情
onMounted(() => {
  const id = route.params.id
  request.get('/front/course/' + id).then(r => {
    if (r.code === 200 && r.data) {
      c.value = r.data.course
      if (r.data.coach) coach.value = r.data.coach
    }
  }).catch(() => message.error('课程加载失败'))
})
</script>

<style scoped>
/* ====== 课程详情页布局样式 ====== */
/* 主体内容：左侧课程详情 + 右侧预约栏双列布局 */
.detail-grid { display: grid; grid-template-columns: 1fr min(380px, 100%); gap: 40px; align-items: start; }
.detail-hero { position: relative; height: 420px; border-radius: var(--radius-lg); overflow: hidden; margin-bottom: 24px; }
.detail-hero img { width: 100%; height: 100%; object-fit: cover; }
.detail-price { position: absolute; top: 20px; right: 20px; background: var(--gold); color: #000; padding: 8px 22px; border-radius: 20px; font-size: var(--text-lg); font-weight: 700; }
.detail-main h1 { font-size: var(--text-2xl); font-weight: 800; color: #fff; margin-bottom: 14px; }
.detail-tags { display: flex; gap: 12px; margin-bottom: 22px; }
.detail-desc { font-size: var(--text-base); color: var(--text-secondary); line-height: 1.8; margin-bottom: 40px; }

/* 教练信息卡片 */
.coach-card { display: flex; gap: 20px; padding: 22px; border-radius: var(--radius-lg); background: rgba(255, 255, 255, 0.01); border: 1px solid var(--border-subtle); }
.coach-avatar { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.coach-body h3 { font-size: var(--text-lg); font-weight: 700; color: #fff; margin: 0 0 6px; }
.coach-exp { font-size: var(--text-sm); color: var(--text-muted); margin: 8px 0; }
.coach-body p { font-size: var(--text-sm); color: var(--text-secondary); line-height: 1.6; }

/* 右侧预约面板：固定悬浮 */
.detail-sidebar { padding: 28px; border-radius: var(--radius-lg); border: 1px solid var(--border-subtle); background: rgba(255, 255, 255, 0.01); position: sticky; top: calc(var(--nav-height) + 20px); }
.detail-sidebar h3 { font-size: var(--text-lg); font-weight: 700; color: #fff; margin: 0 0 22px; }
.time-slots { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
.booking-note { text-align: center; font-size: var(--text-sm); color: var(--text-muted); margin-top: 18px; }
</style>