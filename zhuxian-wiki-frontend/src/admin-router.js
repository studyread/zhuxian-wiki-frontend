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
    path: '/',
    component: () => import('./views/admin/AdminLayout.vue'),
    beforeEnter: requireAdminAuth,
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('./views/admin/AdminDashboard.vue')
      },
      {
        path: 'knowledge',
        name: 'AdminKnowledge',
        component: () => import('./views/admin/KnowledgeManage.vue')
      },
      {
        path: 'knowledge/create',
        name: 'AdminKnowledgeCreate',
        component: () => import('./views/admin/KnowledgeEdit.vue')
      },
      {
        path: 'knowledge/edit/:id',
        name: 'AdminKnowledgeEdit',
        component: () => import('./views/admin/KnowledgeEdit.vue')
      },
      {
        path: 'category',
        name: 'AdminCategory',
        component: () => import('./views/admin/CategoryManage.vue')
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('./views/admin/LogManage.vue')
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('./views/admin/Settings.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
