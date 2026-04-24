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

export const articleApi = {
  getList: (params) => request.get('/articles', { params }),
  getHot: () => request.get('/articles/hot'),
  getLatest: () => request.get('/articles/latest'),
  getById: (id) => request.get(`/articles/${id}`),
  search: (keyword) => request.get('/articles/search', { params: { keyword } }),
  save: (data) => request.post('/articles', data),
  update: (id, data) => request.put(`/articles/${id}`, data),
  delete: (id) => request.delete(`/articles/${id}`)
}

export const categoryApi = {
  getAll: () => request.get('/categories'),
  getRoot: () => request.get('/categories/root'),
  getChildren: (id) => request.get(`/categories/${id}/children`),
  getById: (id) => request.get(`/categories/${id}`),
  getByName: (name) => request.get(`/categories/by-name/${encodeURIComponent(name)}`)
}

export const userApi = {
  login: (data) => request.post('/users/login', data),
  register: (data) => request.post('/users/register', data),
  getInfo: () => request.get('/users/info'),
  logout: () => request.post('/users/logout')
}

export const aiApi = {
  chat: (data) => request.post('/ai/chat', data),
  getHistory: (sessionId) => request.get(`/ai/history/${sessionId}`),
  clearHistory: (sessionId) => request.delete(`/ai/history/${sessionId}`)
}

export default request
