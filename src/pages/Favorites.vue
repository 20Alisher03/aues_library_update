<script setup>
import { ref, onMounted } from 'vue'
import api from '../Axios'
import { useLanguageStore } from '@/language'

const favorites = ref([])
const loading = ref(true)
const languageStore = useLanguageStore()

// Динамически получаем userId из localStorage
const userId = ref(localStorage.getItem('userId') || null)

if (!userId.value) {
  console.error(
    languageStore.translations['favorites.error_not_authorized'] ||
      'Пользователь не авторизован. Пожалуйста, войдите в систему.'
  )
}

onMounted(async () => {
  try {
    if (!userId.value) {
      throw new Error(
        languageStore.translations['favorites.error_not_authorized'] ||
          'Пользователь не авторизован'
      )
    }
    const { data } = await api.get(`/favorites/${userId.value}`)
    favorites.value = data
    loading.value = false
  } catch (err) {
    console.log(err)
    loading.value = false
  }
})

// Метод для получения переводов
const $t = (key) => {
  return languageStore.translations[key] || key
}
</script>

<template>
  <div class="favorites-container">
    <h2 class="text-3xl font-bold mb-8">{{ $t('favorites.title') }}</h2>

    <div v-if="loading" class="text-center py-10">{{ $t('favorites.loading') }}</div>

    <div v-else-if="!favorites.length" class="text-center py-10">{{ $t('favorites.empty') }}</div>

    <div v-else class="carousel-wrapper">
      <a-carousel arrows>
        <template #prevArrow>
          <div class="custom-slick-arrow" style="left: 10px; z-index: 1">
            <left-circle-outlined />
          </div>
        </template>
        <template #nextArrow>
          <div class="custom-slick-arrow" style="right: 10px">
            <right-circle-outlined />
          </div>
        </template>

        <div v-for="book in favorites" :key="book.id" class="carousel-slide">
          <div class="book-card">
            <img :src="book.imageUrl" :alt="book.title" class="book-image" />
            <h3 class="book-title">{{ book.title }}</h3>
            <p class="book-author">{{ book.author }}</p>
            <div class="rating" v-if="book.ratings">
              <a-rate :value="Number(book.ratings)" disabled />
            </div>
          </div>
        </div>
      </a-carousel>
    </div>
  </div>
</template>

<style scoped>
.favorites-container {
  padding: 20px;
}

.carousel-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

:deep(.slick-slide) {
  text-align: center;
  height: 300px;
  background: #fff;
  padding: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
}

:deep(.slick-arrow.custom-slick-arrow) {
  width: 30px;
  height: 30px;
  font-size: 30px;
  color: #1890ff;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  transition: all 0.3s ease;
  opacity: 0.7;
  z-index: 1;
}

:deep(.slick-arrow.custom-slick-arrow:before) {
  display: none;
}

:deep(.slick-arrow.custom-slick-arrow:hover) {
  opacity: 1;
  background-color: #fff;
}

.book-card {
  position: relative;
  padding: 15px;
  background: rgba(83, 76, 79, 0.8);
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.book-image {
  width: 150px;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 10px;
}

.book-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 6px;
  color: #1f2937;
  text-align: center;
}

.book-author {
  font-size: 0.875rem;
  color: black;
  margin-bottom: 8px;
  text-align: center;
}
</style>
