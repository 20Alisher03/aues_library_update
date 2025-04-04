import { defineStore } from 'pinia'
import { ref } from 'vue' // Импортируем ref для реактивности
import axios from 'axios'

export const useLanguageStore = defineStore('language', {
  state: () => ({
    currentLanguage: localStorage.getItem('language') || 'ru',
    translations: ref({}) // Используем ref для реактивности
  }),
  actions: {
    async setLanguage(lang) {
      this.currentLanguage = lang
      localStorage.setItem('language', lang)
      try {
        const response = await axios.get('http://localhost:8080/api/translations', {
          headers: {
            'Accept-Language': lang
          }
        })
        // Бэкенд уже возвращает Map<String, String>, поэтому просто присваиваем
        this.translations = response.data
        console.log('Updated translations:', this.translations) // Для отладки
      } catch (error) {
        console.error('Error fetching translations:', error)
      }
    }
  }
})
