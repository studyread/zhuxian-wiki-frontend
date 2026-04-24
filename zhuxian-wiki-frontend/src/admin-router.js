import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from './views/admin/AdminLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'AdminLogin',
    component: () => import('./views/admin/AdminLogin.vue')
  },
  {
    path: '/',
    component: AdminLayout,
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('./views/admin/AdminDashboard.vue')
      },
      {
        path: 'knowledge',
        name: 'KnowledgeManage',
        component: () => import('./views/admin/KnowledgeManage.vue')
      },
      {
        path: 'knowledge/create',
        name: 'KnowledgeCreate',
        component: () => import('./views/admin/KnowledgeEdit.vue')
      },
      {
        path: 'knowledge/edit/:id',
        name: 'KnowledgeEdit',
        component: () => import('./views/admin/KnowledgeEdit.vue')
      },
      {
        path: 'category',
        name: 'CategoryManage',
        component: () => import('./views/admin/CategoryManage.vue')
      },
      {
        path: 'logs',
        name: 'LogManage',
        component: () => import('./views/admin/LogManage.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('./views/admin/Settings.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory('/admin.html'),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  const isLoginPage = to.path === '/login'
  
  if (!isLoginPage && !token) {
    next('/login')
  } else if (isLoginPage && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
