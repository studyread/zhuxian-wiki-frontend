import { createApp } from 'vue'
import AdminApp from './AdminApp.vue'
import router from './admin-router.js'
import './styles/global.scss'

const app = createApp(AdminApp)
app.use(router)
app.mount('#app')
