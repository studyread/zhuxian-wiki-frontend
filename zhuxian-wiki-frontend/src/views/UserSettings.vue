<template>
  <div class="user-settings-page">
    <div class="page-header">
      <button @click="goBack" class="back-btn">
        <span v-html="backIcon"></span>
        返回
      </button>
      <h1>账号设置</h1>
    </div>

    <div class="settings-form">
      <div class="form-section">
        <h3 class="section-title">基本信息</h3>

        <div class="form-group">
          <label>用户名</label>
          <input type="text" v-model="formData.username" disabled class="input-disabled" />
          <span class="input-hint">用户名不可修改</span>
        </div>

        <div class="form-group">
          <label>昵称</label>
          <input type="text" v-model="formData.nickname" placeholder="请输入昵称" />
        </div>

        <div class="form-group">
          <label>邮箱</label>
          <input type="email" v-model="formData.email" placeholder="请输入邮箱" />
        </div>

        <div class="form-group">
          <label>个人简介</label>
          <textarea v-model="formData.bio" placeholder="介绍一下自己吧..." rows="3"></textarea>
        </div>
      </div>

      <div class="form-section">
        <h3 class="section-title">修改密码</h3>

        <div class="form-group">
          <label>当前密码</label>
          <input type="password" v-model="passwordData.oldPassword" placeholder="请输入当前密码" />
        </div>

        <div class="form-group">
          <label>新密码</label>
          <input type="password" v-model="passwordData.newPassword" placeholder="请输入新密码（6位以上）" />
        </div>

        <div class="form-group">
          <label>确认新密码</label>
          <input type="password" v-model="passwordData.confirmPassword" placeholder="请再次输入新密码" />
        </div>
      </div>

      <div class="form-actions">
        <button @click="handleSave" class="save-btn" :disabled="saving">
          <span v-if="saving" class="spinner"></span>
          <span v-else>保存设置</span>
        </button>
      </div>

      <div class="form-section danger-zone">
        <h3 class="section-title">危险区域</h3>
        <button @click="handleLogout" class="logout-btn">退出登录</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const saving = ref(false)

const formData = ref({
  username: '',
  nickname: '',
  email: '',
  bio: ''
})

const passwordData = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const backIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>'

const goBack = () => {
  router.push('/user')
}

const handleSave = async () => {
  // 验证昵称
  if (!formData.value.nickname) {
    alert('请输入昵称')
    return
  }

  saving.value = true

  try {
    const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')

    // 更新基本信息
    const response = await axios.put(`/api/users/${userInfo.id}`, {
      nickname: formData.value.nickname,
      email: formData.value.email,
      bio: formData.value.bio
    })

    if (response.data.code === 200) {
      // 更新本地存储
      const updatedInfo = { ...userInfo, ...formData.value }
      localStorage.setItem('user_info', JSON.stringify(updatedInfo))
      alert('保存成功')
    } else {
      alert(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('Save error:', error)
    alert('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const handleLogout = () => {
  if (!confirm('确定要退出登录吗？')) return

  localStorage.removeItem('user_info')
  localStorage.removeItem('user_token')
  router.push('/user')
}

onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')
  if (!userInfo.id) {
    router.push('/user')
    return
  }

  formData.value = {
    username: userInfo.username || '',
    nickname: userInfo.nickname || '',
    email: userInfo.email || '',
    bio: userInfo.bio || ''
  }
})
</script>

<style scoped>
.user-settings-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px 16px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-ink);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: var(--color-cream);
}

.page-header h1 {
  font-size: 20px;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin: 0;
}

.settings-form {
  background: var(--color-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.form-section {
  padding: 24px;
  border-bottom: 1px solid var(--color-border-light);
}

.form-section:last-child {
  border-bottom: none;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border-light);
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
  margin-bottom: 8px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--color-ink);
  background: var(--color-white);
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--color-cinnabar);
}

.input-disabled {
  background: var(--color-paper) !important;
  color: var(--color-ink-muted) !important;
  cursor: not-allowed;
}

.input-hint {
  display: block;
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-top: 4px;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  padding: 24px;
  background: var(--color-paper);
}

.save-btn {
  width: 100%;
  padding: 14px;
  background: var(--color-cinnabar);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.save-btn:hover:not(:disabled) {
  background: #b34d3d;
}

.save-btn:disabled {
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

.danger-zone {
  background: #fff5f5;
}

.danger-zone .section-title {
  color: var(--color-cinnabar);
}

.logout-btn {
  width: 100%;
  padding: 12px;
  background: transparent;
  color: var(--color-cinnabar);
  border: 1px solid var(--color-cinnabar);
  border-radius: var(--radius-sm);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: var(--color-cinnabar);
  color: white;
}
</style>
