// В файле main.js
import './assets/main.css'
import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { autoAnimatePlugin } from '@formkit/auto-animate/vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

import App from './App.vue'
import Home from './pages/Home.vue'
import Login from './pages/Authorization.vue'
import Favorites from './pages/Favorites.vue'
import Profile from './pages/Profile.vue'
import Downloads from './pages/Downloads.vue'
import FaqBot from './pages/Faq.vue'
import AdminPanel from './pages/AdminPanel.vue' // Новый импорт

import { useLanguageStore } from './language'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)

const routes = [
  {
    path: '/autorization',
    name: 'Autorization',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: Favorites,
    meta: { requiresAuth: true }
  },
  {
    path: '/downloads',
    name: 'Downloads',
    component: Downloads,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true }
  },
  {
    path: '/faq',
    name: 'Faq',
    component: FaqBot,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminPanel',
    component: AdminPanel,
    meta: { requiresAuth: true, requiresAdmin: true } // Добавляем метаданные для админов
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('isAuthenticated') === 'true'
  const role = localStorage.getItem('role') || 'USER'

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/autorization')
  } else if (to.path === '/autorization' && isAuthenticated) {
    next(role === 'ADMIN' ? '/admin' : '/')
  } else if (to.meta.requiresAdmin && role !== 'ADMIN') {
    next('/')
  } else if (to.path === '/' && !isAuthenticated) {
    next('/autorization')
  } else {
    next()
  }
})

const languageStore = useLanguageStore(pinia)
languageStore.setLanguage(localStorage.getItem('language') || 'ru').then(() => {
  console.log('Initial language set:', languageStore.currentLanguage)
  console.log('Initial translations:', languageStore.translations)
})

app.use(router)
app.use(autoAnimatePlugin)
app.use(Antd)
app.use(pinia)

app.mount('#app')
