import request from '@/api'

// 提取公共的扫码支付二维码获取逻辑（含错误处理）
export async function fetchPayQr() {
  try {
    const res = await request.get('/config/all')
    return res?.data?.pay_qr || ''
  } catch {
    return ''
  }
}
