<script setup>
import { ref, onMounted } from 'vue'
import { LeftCircleOutlined, RightCircleOutlined } from '@ant-design/icons-vue'
import api from '../Axios'

const favorites = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const { data } = await api.get('/favorites?_relations=books')
    favorites.value = data.map((obj) => obj.book)
    loading.value = false
  } catch (err) {
    console.log(err)
    loading.value = false
  }
})
</script>

<template>
  <div class="favorites-container">
    <h2 class="text-3xl font-bold mb-8">Мои Избранные</h2>

    <div v-if="loading" class="text-center py-10">Загрузка избранных книг...</div>

    <div v-else-if="!favorites.length" class="text-center py-10">У вас пока нет избранных книг</div>

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

/* Carousel Slides */
:deep(.slick-slide) {
  text-align: center;
  height: 350px;
  background: #fff;
  padding: 15px;
}

/* Navigation Arrows */
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

/* Book Card Styling */
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

.rating {
  margin-top: -15px;
}
</style>
