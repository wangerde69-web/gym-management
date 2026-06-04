import { createRouter, createWebHistory } from 'vue-router'

// 路由配置表：定义所有页面路径及其对应的视图组件
const routes = [
  { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
  { path: '/admin-login', name: 'admin-login', component: () => import('../views/AdminLoginView.vue') },
  { path: '/admin', name: 'admin', meta: { requireAdmin: true }, component: () => import('../views/AdminView.vue') },
  { path: '/course/:id', name: 'course-detail', component: () => import('../views/CourseDetailView.vue') },
  { path: '/my-bookings', name: 'my-bookings', meta: { requireAuth: true }, component: () => import('../views/MyBookingView.vue') },
  { path: '/my-cards', name: 'my-cards', meta: { requireAuth: true }, component: () => import('../views/MyCardView.vue') },
  { path: '/pose-analysis', name: 'pose-analysis', meta: { requireAuth: true }, component: () => import('../views/PoseAnalysisView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由前置守卫：需要认证的页面自动跳转到登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (to.meta.requireAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.requireAdmin && (!token || role !== 'admin')) {
    next('/admin-login')
  } else {
    next()
  }
})

export default router