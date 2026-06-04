import { useRouter } from 'vue-router'

// 提取公共用户菜单命令处理逻辑（cards/bookings/profile/logout）
export function useUserMenu() {
  const router = useRouter()

  function onMenu(k) {
    if (k === 'cards') router.push('/my-cards')
    else if (k === 'bookings') router.push('/my-bookings')
    else if (k === 'profile') router.push('/?profile=1')
    else if (k === 'logout') { localStorage.clear(); location.href = '/' }
  }

  return { onMenu }
}
