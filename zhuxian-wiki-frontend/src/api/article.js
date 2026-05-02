import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：自动携带用户ID到请求头
// 优先级：管理员ID > 普通用户ID
request.interceptors.request.use(
  config => {
    // 优先使用管理员ID
    const adminInfo = localStorage.getItem('admin_info')
    if (adminInfo) {
      try {
        const admin = JSON.parse(adminInfo)
        if (admin && admin.id) {
          config.headers['X-User-Id'] = admin.id
          return config
        }
      } catch (e) {
        // ignore parse error
      }
    }

    // 其次使用普通用户ID
    const userInfo = localStorage.getItem('user_info')
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        if (user && user.id) {
          config.headers['X-User-Id'] = user.id
        }
      } catch (e) {
        // ignore parse error
      }
    }
    return config
  },
  error => Promise.reject(error)
)

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
  delete: (id) => request.delete(`/articles/${id}`),
  like: (id) => request.post(`/articles/${id}/like`),
  likeWithUser: (id) => request.post(`/articles/${id}/like/user`),
  getLikeStatus: (id) => request.get(`/articles/${id}/like/status`),
  collect: (id) => request.post(`/articles/${id}/collect`),
  getCollectStatus: (id) => request.get(`/articles/${id}/collect/status`),
  getUserCollects: () => request.get('/user/collects'),
  getUserArticles: (userId) => request.get(`/articles/user/${userId}`)
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
  clearHistory: (sessionId) => request.delete(`/ai/history/${sessionId}`),
  submitFeedback: (data) => request.post('/ai/feedback', data)
}

export const statsApi = {
  getStatistics: () => request.get('/stats'),
  getCategoryStats: () => request.get('/stats/categories')
}

export default request
