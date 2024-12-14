// main.js
import './assets/main.css'
import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { autoAnimatePlugin } from '@formkit/auto-animate/vue'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

import App from './App.vue'
import Home from './pages/Home.vue'
import Login from './pages/Authorization.vue'
import Favorites from './pages/Favorites.vue'
import Profile from './pages/Profile.vue'
import Downloads from './pages/Downloads.vue'

const app = createApp(App)

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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard с улучшенной логикой
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('isAuthenticated') === 'true'

  // Если путь требует авторизации и пользователь не авторизован
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/autorization')
  }
  // Если пользователь пытается попасть на страницу логина будучи авторизованным
  else if (to.path === '/autorization' && isAuthenticated) {
    next('/')
  }
  // Если это корневой путь '/' и пользователь не авторизован
  else if (to.path === '/' && !isAuthenticated) {
    next('/autorization')
  }
  // Во всех остальных случаях разрешаем переход
  else {
    next()
  }
})

app.use(router)
app.use(autoAnimatePlugin)
app.use(Antd)

app.mount('#app')
