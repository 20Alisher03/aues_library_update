<template>
  <div class="admin-panel">
    <a-layout>
      <a-layout-header class="header">
        <a-button class="menu-toggle" @click="toggleSider" v-if="isMobile">
          <MenuOutlined />
        </a-button>
        <h2>{{ $t('admin.title') }}</h2>
      </a-layout-header>
      <a-layout>
        <a-layout-sider :class="{ sider: true, open: siderOpen && isMobile }" width="200">
          <a-menu mode="inline" v-model:selectedKeys="selectedKeys">
            <a-menu-item key="faq">{{ $t('admin.menu.faq') }}</a-menu-item>
            <a-menu-item key="books">{{ $t('admin.menu.books') }}</a-menu-item>
          </a-menu>
        </a-layout-sider>
        <a-layout-content class="content">
          <a-card v-if="selectedKeys[0] === 'faq'" :title="$t('admin.faq.title')">
            <a-button type="primary" @click="showAddFaqModal">{{
              $t('admin.faq.add_button')
            }}</a-button>
            <a-table :columns="faqColumns" :data-source="faqs" :pagination="false">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'actions'">
                  <a-button type="link" danger @click="deleteFaq(record.id)">{{
                    $t('admin.faq.delete_button')
                  }}</a-button>
                </template>
              </template>
            </a-table>
          </a-card>
          <a-card v-else-if="selectedKeys[0] === 'books'" :title="$t('admin.books.title')">
            <div class="search-bar">
              <a-input
                v-model:value="searchQuery"
                :placeholder="$t('admin.books.search_placeholder')"
                @input="handleSearch"
                allow-clear
              >
                <template #prefix>
                  <SearchOutlined />
                </template>
              </a-input>
            </div>
            <a-button type="primary" @click="showAddBookModal">{{
              $t('admin.books.add_button')
            }}</a-button>
            <a-table :columns="bookColumns" :data-source="filteredBooks" :pagination="false">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'actions'">
                  <a-button type="link" danger @click="deleteBook(record.id)">{{
                    $t('admin.books.delete_button')
                  }}</a-button>
                  <a-button type="link" @click="showReviews(record.id)">{{
                    $t('admin.books.reviews_button')
                  }}</a-button>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-layout-content>
      </a-layout>
    </a-layout>

    <!-- Модальное окно для добавления FAQ -->
    <a-modal v-model:visible="addFaqModalVisible" :title="$t('admin.faq.modal.title')" @ok="addFaq">
      <a-form :model="newFaq" layout="vertical">
        <a-form-item :label="$t('admin.faq.modal.question_label')">
          <a-input v-model:value="newFaq.question" />
        </a-form-item>
        <a-form-item :label="$t('admin.faq.modal.answer_label')">
          <a-input v-model:value="newFaq.answer" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Модальное окно для добавления книги -->
    <a-modal
      v-model:visible="addBookModalVisible"
      :title="$t('admin.books.modal.title')"
      @ok="addBook"
    >
      <a-form :model="newBook" layout="vertical">
        <a-form-item :label="$t('admin.books.modal.title_label')">
          <a-input v-model:value="newBook.title" />
        </a-form-item>
        <a-form-item :label="$t('admin.books.modal.author_label')">
          <a-input v-model:value="newBook.author" />
        </a-form-item>
        <a-form-item :label="$t('admin.books.modal.genre_label')">
          <a-input v-model:value="newBook.genre" />
        </a-form-item>
        <a-form-item :label="$t('admin.books.modal.language_label')">
          <a-input v-model:value="newBook.language" />
        </a-form-item>
        <a-form-item :label="$t('admin.books.modal.age_label')">
          <a-input v-model:value="newBook.age" />
        </a-form-item>
        <a-form-item :label="$t('admin.books.modal.year_label')">
          <a-input v-model:value="newBook.year" />
        </a-form-item>
        <a-form-item :label="$t('admin.books.modal.image_url_label')">
          <a-input v-model:value="newBook.imageUrl" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Модальное окно для отзывов -->
    <a-modal
      v-model:visible="reviewsModalVisible"
      :title="$t('admin.reviews.modal.title')"
      @ok="reviewsModalVisible = false"
    >
      <a-table :columns="reviewColumns" :data-source="currentReviews" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'actions'">
            <a-button type="link" danger @click="deleteReview(record.id)">{{
              $t('admin.reviews.delete_button')
            }}</a-button>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { message } from 'ant-design-vue'
import { MenuOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { useLanguageStore } from '@/language' // Импортируем useLanguageStore

const selectedKeys = ref(['faq'])
const faqs = ref([])
const books = ref([])
const currentReviews = ref([])
const addFaqModalVisible = ref(false)
const addBookModalVisible = ref(false)
const reviewsModalVisible = ref(false)
const newFaq = ref({ question: '', answer: '' })
const newBook = ref({
  title: '',
  author: '',
  genre: '',
  language: '',
  age: '',
  year: '',
  imageUrl: ''
})
const isMobile = ref(window.innerWidth <= 768)
const siderOpen = ref(false)
const searchQuery = ref('')

// Получаем хранилище языка
const languageStore = useLanguageStore()

// Реактивный метод для получения переводов
const $t = computed(() => (key) => {
  const translation = languageStore.translations[key] || key
  return translation
})

const toggleSider = () => {
  siderOpen.value = !siderOpen.value
}

window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) siderOpen.value = false
})

const faqColumns = computed(() => [
  { title: $t.value('admin.faq.table.id'), dataIndex: 'id', key: 'id' },
  { title: $t.value('admin.faq.table.question'), dataIndex: 'question', key: 'question' },
  { title: $t.value('admin.faq.table.answer'), dataIndex: 'answer', key: 'answer' },
  { title: $t.value('admin.faq.table.actions'), key: 'actions' }
])

const bookColumns = computed(() => [
  { title: $t.value('admin.books.table.id'), dataIndex: 'id', key: 'id' },
  { title: $t.value('admin.books.table.title'), dataIndex: 'title', key: 'title' },
  { title: $t.value('admin.books.table.author'), dataIndex: 'author', key: 'author' },
  { title: $t.value('admin.books.table.genre'), dataIndex: 'genre', key: 'genre' },
  { title: $t.value('admin.books.table.actions'), key: 'actions' }
])

const reviewColumns = computed(() => [
  { title: $t.value('admin.reviews.table.id'), dataIndex: 'id', key: 'id' },
  { title: $t.value('admin.reviews.table.comment'), dataIndex: 'comment', key: 'comment' },
  { title: $t.value('admin.reviews.table.rating'), dataIndex: 'rating', key: 'rating' },
  { title: $t.value('admin.reviews.table.actions'), key: 'actions' }
])

// Локальная фильтрация книг
const filteredBooks = computed(() => {
  if (!searchQuery.value) return books.value
  const query = searchQuery.value.toLowerCase()
  return books.value.filter(
    (book) =>
      book.title.toLowerCase().includes(query) ||
      book.author.toLowerCase().includes(query) ||
      book.genre.toLowerCase().includes(query)
  )
})

// Функция для серверного поиска (если нужно)
const handleSearch = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/books', {
      params: { search: searchQuery.value }
    })
    books.value = response.data
  } catch (error) {
    message.error($t.value('admin.books.error_search'))
  }
}

const fetchFaqs = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/faqs')
    faqs.value = response.data
  } catch (error) {
    message.error($t.value('admin.faq.error_load'))
  }
}

const fetchBooks = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/books')
    books.value = response.data
  } catch (error) {
    message.error($t.value('admin.books.error_load'))
  }
}

const showAddFaqModal = () => {
  addFaqModalVisible.value = true
}

const addFaq = async () => {
  try {
    const response = await axios.post('http://localhost:8080/api/faqs', newFaq.value)
    faqs.value.push(response.data)
    message.success($t.value('admin.faq.success_add'))
    addFaqModalVisible.value = false
    newFaq.value = { question: '', answer: '' }
  } catch (error) {
    message.error($t.value('admin.faq.error_add'))
  }
}

const deleteFaq = async (id) => {
  try {
    await axios.delete(`http://localhost:8080/api/faqs/${id}`)
    faqs.value = faqs.value.filter((faq) => faq.id !== id)
    message.success($t.value('admin.faq.success_delete'))
  } catch (error) {
    message.error($t.value('admin.faq.error_delete'))
  }
}

const showAddBookModal = () => {
  addBookModalVisible.value = true
}

const addBook = async () => {
  try {
    const response = await axios.post('http://localhost:8080/api/books', newBook.value)
    books.value.push(response.data)
    message.success($t.value('admin.books.success_add'))
    addBookModalVisible.value = false
    newBook.value = {
      title: '',
      author: '',
      genre: '',
      language: '',
      age: '',
      year: '',
      imageUrl: ''
    }
  } catch (error) {
    message.error($t.value('admin.books.error_add'))
  }
}

const deleteBook = async (id) => {
  try {
    await axios.delete(`http://localhost:8080/api/books/${id}`)
    books.value = books.value.filter((book) => book.id !== id)
    message.success($t.value('admin.books.success_delete'))
  } catch (error) {
    message.error($t.value('admin.books.error_delete'))
  }
}

const showReviews = async (bookId) => {
  try {
    const response = await axios.get(`http://localhost:8080/api/reviews/${bookId}`)
    currentReviews.value = response.data
    reviewsModalVisible.value = true
  } catch (error) {
    message.error($t.value('admin.reviews.error_load'))
  }
}

const deleteReview = async (id) => {
  try {
    await axios.delete(`http://localhost:8080/api/reviews/${id}`)
    currentReviews.value = currentReviews.value.filter((review) => review.id !== id)
    message.success($t.value('admin.reviews.success_delete'))
  } catch (error) {
    message.error($t.value('admin.reviews.error_delete'))
  }
}

onMounted(() => {
  fetchFaqs()
  fetchBooks()
})
</script>

<style scoped>
.admin-panel {
  min-height: 100vh;
  background: #f0f2f5;
}

.header {
  background: #001529;
  color: white;
  padding: 0 20px;
  height: 64px;
  line-height: 64px;
  display: flex;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header h2 {
  margin: 0;
}

.menu-toggle {
  margin-right: 20px;
  display: none;
}

.sider {
  background: #fff;
  position: sticky;
  top: 64px;
  height: calc(100vh - 64px);
}

.sider.open {
  left: 0;
}

.content {
  padding: 20px;
  min-height: calc(100vh - 64px);
}

.search-bar {
  margin-bottom: 16px;
}

/* Адаптивные стили */

/* 1440px */
@media (max-width: 1440px) {
  .content {
    padding: 15px;
  }
  .ant-table {
    font-size: 14px;
  }
}

/* 1024px */
@media (max-width: 1024px) {
  .sider {
    width: 150px !important;
  }
  .content {
    padding: 10px;
  }
  .ant-table {
    font-size: 13px;
  }
  .ant-table-thead > tr > th,
  .ant-table-tbody > tr > td {
    padding: 10px;
  }
}

/* 768px */
@media (max-width: 768px) {
  .menu-toggle {
    display: block;
  }
  .sider {
    position: fixed;
    left: -200px;
    width: 200px !important;
    transition: left 0.3s ease;
    z-index: 1000;
    height: 100%;
    top: 64px;
  }
  .sider.open {
    left: 0;
  }
  .content {
    margin-left: 0;
    padding: 10px;
  }
  .ant-table-wrapper {
    overflow-x: auto;
  }
  .ant-table {
    font-size: 12px;
  }
  .ant-table-thead > tr > th,
  .ant-table-tbody > tr > td {
    padding: 8px;
  }
  .header h2 {
    font-size: 18px;
  }
  .search-bar {
    margin-bottom: 12px;
  }
}

/* 425px */
@media (max-width: 425px) {
  .header h2 {
    font-size: 16px;
  }
  .ant-btn {
    width: 100%;
    margin-bottom: 10px;
    font-size: 14px;
  }
  .ant-table {
    font-size: 11px;
  }
  .ant-table-thead > tr > th,
  .ant-table-tbody > tr > td {
    padding: 6px;
  }
  .ant-modal {
    width: 90% !important;
    top: 20px;
  }
  .ant-modal-body {
    padding: 15px;
  }
  .ant-form-item {
    margin-bottom: 12px;
  }
  .ant-form-item-label > label {
    font-size: 12px;
  }
  .ant-input {
    font-size: 12px;
  }
  .search-bar {
    margin-bottom: 10px;
  }
}

/* 375px */
@media (max-width: 375px) {
  .header h2 {
    font-size: 14px;
  }
  .ant-btn {
    font-size: 12px;
  }
  .ant-table {
    font-size: 10px;
  }
  .ant-table-thead > tr > th,
  .ant-table-tbody > tr > td {
    padding: 5px;
  }
  .ant-modal {
    width: 95% !important;
  }
  .ant-form-item-label > label {
    font-size: 11px;
  }
  .ant-input {
    font-size: 11px;
  }
}

/* 320px */
@media (max-width: 320px) {
  .header h2 {
    font-size: 12px;
  }
  .menu-toggle {
    padding: 5px;
  }
  .ant-btn {
    font-size: 10px;
    padding: 4px 8px;
  }
  .ant-table {
    font-size: 9px;
  }
  .ant-table-thead > tr > th,
  .ant-table-tbody > tr > td {
    padding: 4px;
  }
  .ant-modal {
    width: 98% !important;
  }
  .ant-modal-title {
    font-size: 14px;
  }
  .ant-form-item-label > label {
    font-size: 10px;
  }
  .ant-input {
    font-size: 10px;
  }
}
</style>
