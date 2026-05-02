<template>
  <div class="user-center">
    <!-- 未登录状态 -->
    <div v-if="!isLoggedIn" class="auth-container">
      <div class="auth-tabs">
        <button 
          :class="['tab-btn', { active: activeTab === 'login' }]" 
          @click="activeTab = 'login'"
        >
          登录
        </button>
        <button 
          :class="['tab-btn', { active: activeTab === 'register' }]" 
          @click="activeTab = 'register'"
        >
          注册
        </button>
      </div>

      <!-- 登录表单 -->
      <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrapper" :class="{ focused: loginFocus.username }">
            <span class="input-icon" v-html="userIcon"></span>
            <input 
              v-model="loginForm.username" 
              type="text" 
              placeholder=" "
              required
              @focus="loginFocus.username = true"
              @blur="loginFocus.username = false"
            />
            <label class="floating-label" v-if="!loginForm.username && !loginFocus.username">请输入账号</label>
          </div>
        </div>
        
        <div class="form-group">
          <label>密码</label>
          <div class="input-wrapper" :class="{ focused: loginFocus.password }">
            <span class="input-icon" v-html="lockIcon"></span>
            <input 
              v-model="loginForm.password" 
              :type="showPassword ? 'text' : 'password'" 
              placeholder=" "
              required
              @focus="loginFocus.password = true"
              @blur="loginFocus.password = false"
            />
            <label class="floating-label" v-if="!loginForm.password && !loginFocus.password">请输入密码</label>
            <button type="button" class="toggle-btn" @click="showPassword = !showPassword">
              <span v-html="showPassword ? eyeOffIcon : eyeIcon"></span>
            </button>
          </div>
        </div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>登 录</span>
        </button>
      </form>

      <!-- 注册表单 -->
      <form v-else @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrapper" :class="{ focused: regFocus.username }">
            <span class="input-icon" v-html="userIcon"></span>
            <input 
              v-model="registerForm.username" 
              type="text" 
              placeholder=" "
              required
              minlength="3"
              maxlength="20"
              @focus="regFocus.username = true"
              @blur="regFocus.username = false"
            />
            <label class="floating-label" v-if="!registerForm.username && !regFocus.username">请输入用户名（3-20位）</label>
          </div>
        </div>

        <div class="form-group">
          <label>昵称</label>
          <div class="input-wrapper" :class="{ focused: regFocus.nickname }">
            <span class="input-icon" v-html="nicknameIcon"></span>
            <input 
              v-model="registerForm.nickname" 
              type="text" 
              placeholder=" "
              required
              @focus="regFocus.nickname = true"
              @blur="regFocus.nickname = false"
            />
            <label class="floating-label" v-if="!registerForm.nickname && !regFocus.nickname">请输入昵称</label>
          </div>
        </div>
        
        <div class="form-group">
          <label>密码</label>
          <div class="input-wrapper" :class="{ focused: regFocus.password }">
            <span class="input-icon" v-html="lockIcon"></span>
            <input 
              v-model="registerForm.password" 
              :type="showPassword ? 'text' : 'password'" 
              placeholder=" "
              required
              minlength="6"
              @focus="regFocus.password = true"
              @blur="regFocus.password = false"
            />
            <label class="floating-label" v-if="!registerForm.password && !regFocus.password">请输入密码（6位以上）</label>
            <button type="button" class="toggle-btn" @click="showPassword = !showPassword">
              <span v-html="showPassword ? eyeOffIcon : eyeIcon"></span>
            </button>
          </div>
        </div>

        <div class="form-group">
          <label>确认密码</label>
          <div class="input-wrapper" :class="{ focused: regFocus.confirm }">
            <span class="input-icon" v-html="lockIcon"></span>
            <input 
              v-model="registerForm.confirmPassword" 
              :type="showPassword ? 'text' : 'password'" 
              placeholder=" "
              required
              @focus="regFocus.confirm = true"
              @blur="regFocus.confirm = false"
            />
            <label class="floating-label" v-if="!registerForm.confirmPassword && !regFocus.confirm">请再次输入密码</label>
          </div>
        </div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>注 册</span>
        </button>
      </form>
    </div>

    <!-- 已登录状态 -->
    <div v-else class="profile-container">
      <!-- 用户信息区域 - 非子路由页面时显示 -->
      <template v-if="!isSubPage">
        <div class="profile-header">
          <div class="avatar-wrapper">
            <img v-if="userInfo?.avatar" :src="userInfo.avatar" alt="Avatar" />
            <span v-else class="avatar-placeholder">{{ userInfo?.nickname?.charAt(0) || 'U' }}</span>
          </div>
          <div class="profile-info">
            <h2>{{ userInfo?.nickname || '用户' }}</h2>
            <p>@{{ userInfo?.username }}</p>
            <span class="user-role">{{ userInfo?.role === 'admin' ? '管理员' : '普通用户' }}</span>
          </div>
          <button @click="handleLogout" class="logout-btn">
            <span v-html="logoutIcon"></span>
            退出登录
          </button>
        </div>

        <div class="profile-stats">
          <div class="stat-item">
            <span class="stat-value">{{ userInfo?.articleCount || 0 }}</span>
            <span class="stat-label">发布文章</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ userInfo?.favoriteCount || 0 }}</span>
            <span class="stat-label">收藏文章</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ userInfo?.points || 0 }}</span>
            <span class="stat-label">积分</span>
          </div>
        </div>

        <div class="profile-actions">
          <router-link to="/user/favorites" class="action-card">
            <span class="action-icon" v-html="starIcon"></span>
            <span class="action-text">我的收藏</span>
            <span class="action-arrow" v-html="arrowIcon"></span>
          </router-link>
          <router-link to="/user/settings" class="action-card">
            <span class="action-icon" v-html="settingsIcon"></span>
            <span class="action-text">账号设置</span>
            <span class="action-arrow" v-html="arrowIcon"></span>
          </router-link>
        </div>
      </template>

      <!-- 子路由页面 -->
      <router-view v-else></router-view>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const showPassword = ref(false)
const activeTab = ref('login')

const loginFocus = ref({ username: false, password: false })
const regFocus = ref({ username: false, nickname: false, password: false, confirm: false })

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const userInfo = ref(null)

const isLoggedIn = computed(() => !!userInfo.value)
const isSubPage = computed(() => {
  return ['UserFavorites', 'UserSettings'].includes(route.name)
})

const userIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>'
const nicknameIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>'
const lockIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>'
const eyeIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>'
const eyeOffIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>'
const logoutIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>'
const starIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>'
const settingsIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>'
const arrowIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>'

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    alert('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const response = await axios.post('/api/users/login', {
      username: loginForm.value.username,
      password: loginForm.value.password
    })

    if (response.data.code === 200) {
      userInfo.value = response.data.data
      localStorage.setItem('user_info', JSON.stringify(response.data.data))
      localStorage.setItem('user_token', response.data.data.token || 'logged_in')
      
      // 刷新页面
      window.location.reload()
    } else {
      alert(response.data.message || '登录失败')
    }
  } catch (error) {
    console.error('Login error:', error)
    alert('登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.nickname || !registerForm.value.password) {
    alert('请填写所有字段')
    return
  }

  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    alert('两次密码输入不一致')
    return
  }

  if (registerForm.value.password.length < 6) {
    alert('密码至少6位')
    return
  }

  loading.value = true
  try {
    const response = await axios.post('/api/users/register', {
      username: registerForm.value.username,
      nickname: registerForm.value.nickname,
      password: registerForm.value.password
    })

    if (response.data.code === 200) {
      alert('注册成功！请登录')
      activeTab.value = 'login'
      loginForm.value.username = registerForm.value.username
    } else {
      alert(response.data.message || '注册失败')
    }
  } catch (error) {
    console.error('Register error:', error)
    alert('注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleLogout = () => {
  localStorage.removeItem('user_info')
  localStorage.removeItem('user_token')
  // 清除 AI 聊天会话信息，下次进入聊天页面时会创建新会话
  localStorage.removeItem('ai_chat_session_id')
  localStorage.removeItem('ai_chat_user_session')
  userInfo.value = null
  window.location.reload()
}

onMounted(() => {
  const stored = localStorage.getItem('user_info')
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }
})
</script>

<style scoped>
.user-center {
  max-width: 480px;
  margin: 0 auto;
  padding: 32px 16px;
}

/* 未登录 - 登录/注册表单 */
.auth-container {
  background: var(--color-white);
  border-radius: var(--radius-lg);
  padding: 28px;
  box-shadow: var(--shadow-md);
}

.auth-tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 24px;
  background: #f5f6f8;
  padding: 5px;
  border-radius: 10px;
}

.tab-btn {
  flex: 1;
  padding: 10px;
  border: none;
  background: transparent;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.tab-btn.active {
  background: var(--color-white);
  color: var(--color-ink);
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  font-weight: 500;
  color: #555;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.3s;
  background: #fff;
}

.input-wrapper.focused {
  border-color: var(--color-cinnabar);
  box-shadow: 0 0 0 3px rgba(196, 92, 72, 0.1);
}

.input-icon {
  padding: 0 12px;
  color: #999;
  display: flex;
  align-items: center;
  background: #fafafa;
  border-right: 1px solid #e8e8e8;
  height: 46px;
  flex-shrink: 0;
}

.input-wrapper input {
  flex: 1;
  border: none;
  outline: none;
  padding: 14px 10px;
  font-size: 14px;
  background: transparent;
  position: relative;
  z-index: 1;
}

.input-wrapper input::placeholder {
  color: transparent;
}

.floating-label {
  position: absolute;
  left: 46px;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #bbb;
  font-size: 14px;
  pointer-events: none;
  transition: all 0.2s;
  z-index: 0;
}

.toggle-btn {
  padding: 0 12px;
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  z-index: 1;
  position: relative;
}

.submit-btn {
  width: 100%;
  padding: 13px;
  background: linear-gradient(135deg, var(--color-cinnabar), #e07b6d);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 6px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(196, 92, 72, 0.35);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 已登录 - 用户资料 */
.profile-container {
  background: var(--color-white);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.profile-header {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 28px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
}

.avatar-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
  background: rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 24px;
  font-weight: 600;
  font-family: var(--font-serif);
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-info h2 {
  font-size: 20px;
  font-family: var(--font-serif);
  margin: 0 0 2px 0;
}

.profile-info p {
  font-size: 13px;
  opacity: 0.7;
  margin: 0;
}

.user-role {
  display: inline-block;
  margin-top: 6px;
  padding: 3px 10px;
  background: rgba(255,255,255,0.15);
  border-radius: 20px;
  font-size: 11px;
}

.logout-btn {
  padding: 8px 14px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  color: rgba(255,255,255,0.9);
  border-radius: 8px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s;
  flex-shrink: 0;
}

.logout-btn:hover {
  background: rgba(220, 53, 69, 0.3);
  border-color: rgba(220, 53, 69, 0.4);
}

.profile-stats {
  display: flex;
  border-bottom: 1px solid #eee;
}

.stat-item {
  flex: 1;
  padding: 18px;
  text-align: center;
  border-right: 1px solid #eee;
}

.stat-item:last-child {
  border-right: none;
}

.stat-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: var(--color-ink);
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #888;
  margin-top: 2px;
}

.profile-actions {
  padding: 14px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #f9fafb;
  border-radius: 10px;
  text-decoration: none;
  color: #333;
  transition: all 0.2s;
  margin-bottom: 10px;
}

.action-card:last-child {
  margin-bottom: 0;
}

.action-card:hover {
  background: #f0f1f3;
}

.action-icon {
  width: 40px;
  height: 40px;
  background: var(--color-white);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-cinnabar);
}

.action-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.action-arrow {
  color: #ccc;
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .user-center {
    padding-bottom: 70px;
  }

  .auth-container {
    padding: 24px 20px;
  }

  .auth-tabs {
    margin-bottom: 24px;
  }

  .tab-btn {
    flex: 1;
    padding: 12px;
    font-size: 15px;
  }

  .form-group {
    margin-bottom: 18px;
  }

  .input-wrapper {
    height: 50px;
  }

  .form-input {
    font-size: 15px;
  }

  .submit-btn {
    height: 48px;
    font-size: 15px;
  }

  .user-profile {
    padding: 20px 16px;
    margin: 16px;
    border-radius: 12px;
  }

  .user-info {
    gap: 16px;
  }

  .avatar {
    width: 64px;
    height: 64px;
    font-size: 24px;
  }

  .user-details {
    flex: 1;
  }

  .user-name {
    font-size: 18px;
  }

  .user-bio {
    font-size: 12px;
  }

  .user-stats {
    margin-top: 12px;
  }

  .stat-value {
    font-size: 18px;
  }

  .profile-actions {
    padding: 12px 16px;
  }

  .action-card {
    padding: 12px;
    margin-bottom: 8px;
  }

  .action-icon {
    width: 36px;
    height: 36px;
  }

  .action-text {
    font-size: 13px;
  }
}
</style>
