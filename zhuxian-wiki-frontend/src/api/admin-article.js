import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动携带管理员ID
request.interceptors.request.use(
  config => {
    const adminInfo = localStorage.getItem('admin_info')
    if (adminInfo) {
      try {
        const admin = JSON.parse(adminInfo)
        if (admin && admin.id) {
          config.headers['X-Admin-Id'] = admin.id
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

export const adminArticleApi = {
  /**
   * 获取文章列表（分页+搜索+筛选）
   * @param {Object} params - { page, size, keyword, categoryId, status, sort }
   */
  list: (params) => request.get('/admin/articles', { params }),

  /**
   * 获取文章详情
   * @param {Number} id - 文章ID
   */
  getById: (id) => request.get(`/admin/articles/${id}`),

  /**
   * 新增文章
   * @param {Object} data - 文章数据
   */
  create: (data) => request.post('/admin/articles', data),

  /**
   * 更新文章
   * @param {Number} id - 文章ID
   * @param {Object} data - 文章数据
   */
  update: (id, data) => request.put(`/admin/articles/${id}`, data),

  /**
   * 删除文章
   * @param {Number} id - 文章ID
   */
  delete: (id) => request.delete(`/admin/articles/${id}`),

  /**
   * 置顶文章
   * @param {Number} id - 文章ID
   */
  toggleTop: (id) => request.post(`/admin/articles/${id}/top`),

  /**
   * 取消置顶
   * @param {Number} id - 文章ID
   */
  untoggleTop: (id) => request.delete(`/admin/articles/${id}/top`)
}

export default adminArticleApi
