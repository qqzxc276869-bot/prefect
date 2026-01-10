import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '@/views/Login.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/student',
    name: 'Student',
    component: () => import('@/views/Student/Index.vue'),
    meta: { requireAuth: true, role: 'STUDENT' }
  },
  {
    path: '/teacher',
    name: 'Teacher',
    component: () => import('@/views/Teacher/Index.vue'),
    meta: { requireAuth: true, role: 'TEACHER' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/Admin/Index.vue'),
    meta: { requireAuth: true, role: 'ADMIN' }
  },
  {
    path: '/ai-demo',
    name: 'AIDemo',
    component: () => import('@/views/AiDemo.vue'),
    meta: { requireAuth: true }
  },
  {
    path: '/ai-assistant',
    name: 'AiAssistant',
    component: () => import('@/views/AiAssistant.vue'),
    meta: { requireAuth: true }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  
  if (to.meta.requireAuth) {
    if (!token) {
      next('/login')
    } else {
      // 检查角色权限
      if (to.meta.role && to.meta.role !== userInfo.role) {
        next('/')
      } else {
        next()
      }
    }
  } else {
    next()
  }
})

export default router







