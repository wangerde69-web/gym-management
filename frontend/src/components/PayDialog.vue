<!-- 扫码支付弹窗：展示收款二维码和金额，返回 Promise 表示用户支付确认结果 -->
<template>
  <n-modal v-model:show="visible" :title="title" preset="dialog" positive-text="已完成支付" negative-text="取消" style="width: 420px" @positive-click="onConfirm" @negative-click="onCancel">
    <div style="text-align: center; padding: 16px 0">
      <img v-if="qrUrl" :src="qrUrl" style="width: 200px; max-width: 80%; border-radius: var(--radius-md); background: #fff; margin: 0 auto; display: block" />
      <p style="margin-top: 16px; font-size: var(--text-base); color: var(--text-secondary)">请使用微信 / 支付宝扫码支付</p>
      <p style="margin-top: 8px; font-size: var(--text-2xl); font-weight: 800; color: var(--gold)">¥{{ price }}</p>
    </div>
  </n-modal>
</template>

<script setup>
import { ref } from 'vue'
import { NModal } from 'naive-ui'

// 支付弹窗状态和 Promise 回调
const visible = ref(false)
const qrUrl = ref('')
const price = ref(0)
const title = ref('扫码支付')

let resolvePromise = null

// 打开支付弹窗，返回 Promise<boolean> 表示用户是否确认支付
function open(qr, p, t = '扫码支付') {
  qrUrl.value = qr || ''
  price.value = p || 0
  title.value = t
  visible.value = true
  return new Promise(resolve => { resolvePromise = resolve })
}

// 用户确认已完成支付
function onConfirm() {
  visible.value = false
  resolvePromise?.(true)
}

// 用户取消支付
function onCancel() {
  visible.value = false
  resolvePromise?.(false)
}

defineExpose({ open })
</script>