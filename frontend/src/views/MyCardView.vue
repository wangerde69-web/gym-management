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

    <div class="content-wrap">
      <div class="page-top">
        <h1 class="page-h1" style="margin-bottom: 0">我的会员卡</h1>
        <router-link to="/" class="back-link">← 返回首页</router-link>
      </div>

      <!-- 我的会员卡 -->
      <div v-if="mc.length" class="my-cards-section">
        <div v-for="c in mc" :key="c.id" class="card-row" :style="{ opacity: [2, 3, 4].includes(c.status) ? '.35' : '1' }">
          <div class="card-row-info">
            <h3>{{ c.cardTypeName }}</h3>
            <div class="card-meta">
              <span v-if="c.status === 1">📅 过期时间: {{ c.endDate || '—' }}</span>
            </div>
            <b>¥{{ c.price }}</b>
          </div>
          <div class="card-row-actions">
            <n-tag :type="['warning', 'success', 'info', 'error', 'info', 'warning'][c.status] || 'info'" size="medium">
              {{ ['已支付待审', '生效中', '已过期', '已退款', '未支付', '未支付待审'][c.status] || '未知' }}
            </n-tag>
            <n-button v-if="c.status === 1" type="error" size="medium" secondary @click="refundCard(c.id)">申请退款</n-button>
            <n-button v-if="c.status === 3" type="primary" size="medium" secondary @click="cfmRefund(c.id)">确认退款</n-button>
          </div>
        </div>
      </div>
      <div v-else-if="logged" class="empty-state">暂无购卡记录</div>

      <!-- 购买新卡 -->
      <div v-if="logged" class="buy-section">
        <h2>购买新卡</h2>
        <div class="card-grid">
          <div v-for="ct in cts" :key="ct.id" class="card-select" :class="{ selected: sel === ct.cardKey }" @click="sel = ct.cardKey">
            <div class="card-cover">
              <img v-if="ct.coverImage" :src="ct.coverImage" />
              <span v-else class="card-cover-empty">⚡</span>
            </div>
            <h3>{{ ct.cardName }}</h3>
            <span class="card-validity">{{ ct.validityDays }}{{ { DAY: '天', MONTH: '个月', YEAR: '年' }[ct.validityUnit] || '天' }}</span>
            <b>¥{{ ct.price }}</b>
          </div>
        </div>
        <n-button type="primary" size="large" :loading="ld" :disabled="!sel" block @click="showPay">{{ ld ? '处理中' : '立即购买' }}</n-button>
      </div>
    </div>

    <PayDialog ref="payRef" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { NButton, NTag, useMessage, useDialog } from 'naive-ui'
import request from '@/api'
import UserMenu from '@/components/UserMenu.vue'
import PayDialog from '@/components/PayDialog.vue'
import { useUserMenu } from '@/composables/useUserMenu'
import { fetchPayQr } from '@/composables/usePayment'

// 页面状态和消息/对话框初始化
const message = useMessage()
const { onMenu } = useUserMenu()
const dialog = useDialog()

// 登录状态、我的卡列表、可选卡种、选中卡种、加载状态
// 使用 ref + 监听 storage 事件以保持响应式（避免 computed 读取 localStorage 不响应的问题）
const logged = ref(!!localStorage.getItem('token'))
function onStorage() { logged.value = !!localStorage.getItem('token') }
onMounted(() => window.addEventListener('storage', onStorage))
onUnmounted(() => window.removeEventListener('storage', onStorage))
const mc = ref([]), cts = ref([]), sel = ref(''), ld = ref(false)
const payRef = ref(null), pid = ref(null)
// 根据选中卡种计算支付价格
const sp = computed(() => { const ct = cts.value.find(c => c.cardKey === sel.value); return ct ? ct.price : 0 })

// 用户菜单命令处理（由 useUserMenu 提供）

// 加载我的已有会员卡列表
function fMc() {
  request.get('/card/my').then(r => { if (r.code === 200) mc.value = r.data || [] }).catch(() => message.error('加载失败'))
}

// 加载可购买的卡种列表
function fCt() {
  request.get('/cardtype/all').then(r => { if (r.code === 200) cts.value = r.data || [] }).catch(() => message.error('加载失败'))
}

/* 会员卡购买流程：创建购卡订单 → 打开支付弹窗 → 确认或取消支付 */
async function showPay() {
  if (!sel.value) { message.warning('请选择卡种'); return }
  ld.value = true
  request.post('/card/buy', { cardType: sel.value }).then(async r => {
    ld.value = false
    if (r.code === 200) {
      pid.value = r.cardId
      const qr = await fetchPayQr()
      const result = await payRef.value.open(qr, sp.value)
      if (result) confirmPay()
      else rejectPay()
    } else message.error(r.msg || '创建失败')
  }).catch(() => { ld.value = false; message.error('请求失败') })
}

// 确认支付：通知后端支付已完成
function confirmPay() {
  if (!pid.value) return
  request.post('/card/confirm-payment/' + pid.value).then(r => {
    if (r.code === 200) { message.success('已提交'); sel.value = ''; pid.value = null; fMc() }
  }).catch(() => message.error('请求失败'))
}

// 取消支付：通知后端放弃该笔支付
function rejectPay() {
  if (pid.value) request.post('/card/reject-payment/' + pid.value).catch(() => message.error('请求失败'))
  sel.value = ''; pid.value = null
}

// 会员卡退款申请
function refundCard(id) {
  dialog.warning({
    title: '申请退款',
    content: '确定退款？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      request.post('/card/refund/' + id).then(r => { if (r.code === 200) { message.success('已提交'); fMc() } }).catch(() => message.error('请求失败'))
    }
  })
}

// 确认退款完成
function cfmRefund(id) {
  dialog.warning({
    title: '确认退款',
    content: '确认退款完成？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      request.post('/card/confirm-refund/' + id).then(r => {
        if (r.code === 200) { message.success('已完成'); mc.value = mc.value.filter(c => c.id !== id) }
      }).catch(() => message.error('请求失败'))
    }
  })
}

onMounted(() => { fMc(); fCt() })
</script>

<style scoped>
/* ====== 我的会员卡页样式 ====== */

/* 我的会员卡 */
.my-cards-section { margin-bottom: 48px; }
.card-row { display: flex; align-items: center; justify-content: space-between; padding: 22px 24px; border-radius: var(--radius-lg); border: 1px solid var(--border-subtle); background: rgba(255, 255, 255, 0.01); margin-bottom: 12px; }
.card-row-info h3 { font-size: var(--text-lg); font-weight: 800; color: #fff; margin: 0 0 8px; }
.card-meta { display: flex; gap: 20px; font-size: var(--text-sm); color: var(--text-secondary); margin-bottom: 8px; }
.card-row-info b { font-size: 22px; color: var(--gold); display: block; }
.card-row-actions { display: flex; flex-direction: column; align-items: flex-end; gap: 12px; flex-shrink: 0; }

/* 购买区域 */
.buy-section h2 { font-size: var(--text-xl); font-weight: 800; color: #fff; margin: 0 0 22px; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }
.card-select { padding: 22px; border-radius: var(--radius-md); text-align: center; cursor: pointer; border: 1px solid var(--border-subtle); background: rgba(255, 255, 255, 0.01); transition: all 0.3s; }
.card-select:hover, .card-select.selected { border-color: rgba(201, 169, 110, 0.3); background: rgba(201, 169, 110, 0.03); }
.card-cover { height: 100px; border-radius: var(--radius-sm); margin-bottom: 16px; background: rgba(255, 255, 255, 0.02); display: flex; align-items: center; justify-content: center; overflow: hidden; }
.card-cover img { width: 100%; height: 100%; object-fit: cover; }
.card-cover-empty { font-size: 36px; opacity: 0.1; }
.card-select h3 { font-size: var(--text-base); font-weight: 700; color: #fff; margin-bottom: 6px; }
.card-validity { font-size: var(--text-sm); color: var(--text-muted); display: block; }
.card-select b { font-size: 22px; color: var(--gold); display: block; margin-top: 6px; }
</style>