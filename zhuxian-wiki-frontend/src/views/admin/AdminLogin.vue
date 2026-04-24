<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧装饰 -->
      <div class="login-decoration">
        <div class="decoration-content">
          <img src="/big_logo.webp" alt="Logo" class="decoration-logo" />
          <h1>诛仙世界Wiki</h1>
          <p>游戏攻略知识库</p>
          <div class="decoration-features">
            <div class="feature-item">
              <span class="feature-icon">📚</span>
              <span>丰富知识库</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🤖</span>
              <span>AI智能助手</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚡</span>
              <span>实时更新</span>
            </div>
          </div>
        </div>
        <div class="decoration-bg"></div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-form-wrapper">
        <div class="login-form">
          <div class="form-header">
            <h2>管理员登录</h2>
            <p>欢迎回来，请登录您的账户</p>
          </div>

          <form @submit.prevent="handleLogin" class="form-body">
            <div class="form-group">
              <label for="username">用户名</label>
              <div class="input-wrapper" :class="{ focused: usernameFocused }">
                <span class="input-icon" v-html="userIcon"></span>
                <input
                  id="username"
                  v-model="formData.username"
                  type="text"
                  placeholder=" "
                  required
                  @focus="usernameFocused = true"
                  @blur="usernameFocused = false"
                />
                <label class="floating-label" v-if="!formData.username && !usernameFocused">请输入账号</label>
              </div>
            </div>

            <div class="form-group">
              <label for="password">密码</label>
              <div class="input-wrapper" :class="{ focused: passwordFocused }">
                <span class="input-icon" v-html="lockIcon"></span>
                <input
                  id="password"
                  v-model="formData.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder=" "
                  required
                  @focus="passwordFocused = true"
                  @blur="passwordFocused = false"
                />
                <label class="floating-label" v-if="!formData.password && !passwordFocused">请输入密码</label>
                <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                  <span v-html="showPassword ? eyeOffIcon : eyeIcon"></span>
                </button>
              </div>
            </div>

            <div class="form-options">
              <label class="remember-me">
                <input type="checkbox" v-model="rememberMe" />
                <span>记住我</span>
              </label>
              <a href="#" class="forgot-link">忘记密码？</a>
            </div>

            <button type="submit" class="submit-btn" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              <span v-else>登 录</span>
            </button>
          </form>

          <div class="form-footer">
            <a href="/" class="back-link">
              <span v-html="arrowLeftIcon"></span>
              返回首页
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const rememberMe = ref(false)
const usernameFocused = ref(false)
const passwordFocused = ref(false)

const formData = ref({
  username: '',
  password: ''
})

const userIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>'
const lockIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>'
const eyeIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>'
const eyeOffIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>'
const arrowLeftIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>'

const handleLogin = async () => {
  if (!formData.value.username || !formData.value.password) {
    alert('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const response = await axios.post('/api/admin/login', {
      username: formData.value.username,
      password: formData.value.password
    })

    if (response.data.code === 200) {
      // 保存登录信息
      localStorage.setItem('admin_token', 'true')
      localStorage.setItem('admin_id', response.data.data.id)
      localStorage.setItem('admin_info', JSON.stringify(response.data.data))

      // 如果勾选记住我
      if (rememberMe.value) {
        localStorage.setItem('admin_username', formData.value.username)
      }

      // 跳转到仪表盘 (history 模式)
      router.push('/dashboard')
    } else {
      alert(response.data.message || '登录失败')
    }
  } catch (error) {
    console.error('Login error:', error)
    alert('登录失败，请检查网络或联系管理员')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #f5f6f8;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  display: flex;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.1);
  max-width: 850px;
  width: 100%;
}

/* 左侧装饰 */
.login-decoration {
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 48px 32px;
  color: #fff;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 480px;
}

.decoration-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  pointer-events: none;
}

.decoration-content {
  text-align: center;
  position: relative;
  z-index: 1;
}

.decoration-logo {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  object-fit: contain;
  background: transparent;
  padding: 0;
  margin-bottom: 20px;
}

.decoration-content h1 {
  font-size: 24px;
  font-family: var(--font-serif);
  margin: 0 0 6px 0;
  letter-spacing: 2px;
}

.decoration-content p {
  font-size: 13px;
  opacity: 0.7;
  margin: 0 0 32px 0;
}

.decoration-features {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  opacity: 0.9;
}

.feature-icon {
  font-size: 18px;
}

/* 右侧登录表单 */
.login-form-wrapper {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-form {
  width: 100%;
  max-width: 300px;
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 22px;
  font-family: var(--font-serif);
  color: #1a1a2e;
  margin: 0 0 6px 0;
}

.form-header p {
  font-size: 13px;
  color: #888;
  margin: 0;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
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
  border-color: #c45c48;
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
  padding: 14px 12px;
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
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #bbb;
  font-size: 14px;
  pointer-events: none;
  transition: all 0.2s;
  z-index: 0;
}

.toggle-password {
  padding: 0 12px;
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  z-index: 1;
  position: relative;
}

.toggle-password:hover {
  color: #666;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}

.remember-me input {
  width: 16px;
  height: 16px;
  accent-color: #c45c48;
}

.forgot-link {
  font-size: 13px;
  color: #c45c48;
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  padding: 13px;
  background: linear-gradient(135deg, #c45c48 0%, #e07b6d 100%);
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

.loading-spinner {
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

.form-footer {
  margin-top: 28px;
  text-align: center;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #888;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.2s;
}

.back-link:hover {
  color: #c45c48;
}

/* 响应式 */
@media (max-width: 700px) {
  .login-container {
    flex-direction: column;
  }

  .login-decoration {
    min-height: auto;
    padding: 36px 24px;
  }

  .decoration-features {
    flex-direction: row;
    justify-content: center;
    flex-wrap: wrap;
    gap: 16px;
  }

  .login-form-wrapper {
    padding: 36px 24px;
  }
}
</style>
