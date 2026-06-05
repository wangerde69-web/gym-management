import request from '@/api'

// 提取公共的扫码支付二维码获取逻辑（使用公开端点，无需认证）
export async function fetchPayQr() {
  try {
    const res = await request.get('/front/pay-qr')
    return res?.data?.payQr || ''
  } catch {
    return ''
  }
}
