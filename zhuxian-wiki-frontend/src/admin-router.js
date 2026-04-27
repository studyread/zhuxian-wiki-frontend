import { createRouter, createWebHashHistory } from 'vue-router'

// 路由守卫：检查管理后台登录状态
const requireAdminAuth = (to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    next()
  } else {
    next('/login')
  }
}

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'AdminLogin',
    component: () => import('./views/admin/AdminLogin.vue')
  },
  {
    path: '/dashboard',
    name: 'AdminDashboard',
    component: () => import('./views/admin/AdminDashboard.vue'),
    beforeEnter: requireAdminAuth
  },
  {
    path: '/knowledge',
    name: 'AdminKnowledge',
    component: () => import('./views/admin/KnowledgeManage.vue'),
    beforeEnter: requireAdminAuth
  },
  {
    path: '/knowledge/create',
    name: 'AdminKnowledgeCreate',
    component: () => import('./views/admin/KnowledgeEdit.vue'),
    beforeEnter: requireAdminAuth
  },
  {
    path: '/knowledge/edit/:id',
    name: 'AdminKnowledgeEdit',
    component: () => import('./views/admin/KnowledgeEdit.vue'),
    beforeEnter: requireAdminAuth
  },
  {
    path: '/category',
    name: 'AdminCategory',
    component: () => import('./views/admin/CategoryManage.vue'),
    beforeEnter: requireAdminAuth
  },
  {
    path: '/logs',
    name: 'AdminLogs',
    component: () => import('./views/admin/LogManage.vue'),
    beforeEnter: requireAdminAuth
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
