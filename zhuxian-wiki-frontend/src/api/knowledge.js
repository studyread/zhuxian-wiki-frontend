import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// 知识库分类
export const knowledgeCategoryApi = {
  list: () => request.get('/knowledge/category/list'),
  getById: (id) => request.get(`/knowledge/category/${id}`),
  create: (data) => request.post('/knowledge/category', data),
  update: (id, data) => request.put(`/knowledge/category/${id}`, data),
  delete: (id) => request.delete(`/knowledge/category/${id}`)
}

// 知识库词条
export const knowledgeEntryApi = {
  list: (params) => request.get('/knowledge/entry/list', { params }),
  getById: (id) => request.get(`/knowledge/entry/${id}`),
  search: (keyword) => request.get('/knowledge/entry/search', { params: { keyword } }),
  create: (data) => request.post('/knowledge/entry', data),
  update: (id, data) => request.put(`/knowledge/entry/${id}`, data),
  delete: (id) => request.delete(`/knowledge/entry/${id}`),
  export: () => request.get('/knowledge/entry/export')
}

// 知识库日志
export const knowledgeLogApi = {
  list: (params) => axios.get('/knowledge/log/list', { params })
}

// 管理员
export const adminApi = {
  login: (data) => axios.post('/admin/login', data),
  register: (data) => axios.post('/admin/register', data),
  info: () => axios.get('/admin/info')
}
