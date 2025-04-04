import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api', // URL для Spring Boot API
  headers: {
    'Content-Type': 'application/json'
  }
})

// Добавляем перехватчик запросов
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Добавляем перехватчик ответов
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('isAuthenticated')
      localStorage.removeItem('token')
      window.location.href = '/authorization'
    }
    return Promise.reject(error)
  }
)

export default api
