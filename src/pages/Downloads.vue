<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '../Axios'
import { useLanguageStore } from '@/language' // Импортируем useLanguageStore

// Реактивные данные
const downloads = ref([])
const currentPdfUrl = ref(null)
const isLoading = ref(true)
const isRemoving = ref(false)
const error = ref(null)
const notification = ref(null)

// Получаем хранилище языка
const languageStore = useLanguageStore()

// Реактивный метод для получения переводов
const $t = computed(() => (key) => {
  const translation = languageStore.translations[key] || key
  console.log(`Translating key "${key}":`, translation) // Логирование для отладки
  return translation
})

const loadDownloadedBooks = async () => {
  try {
    isLoading.value = true
    error.value = null

    // Получаем userId из localStorage
    const userId = localStorage.getItem('userId')
    if (!userId) {
      throw new Error($t.value('downloads.error_not_authorized'))
    }

    // Загрузка списка скачанных книг с API, передаем userId как параметр
    const { data } = await api.get('/downloads', {
      params: { userId }
    })

    // Формируем URL для PDF и изображений
    downloads.value = data.map((book) => ({
      ...book,
      pdfUrl: `/library/pdfs/book-${book.bookId}.pdf`,
      imageUrl: `/library/book-${book.bookId}.jpg`
    }))
  } catch (err) {
    error.value = $t.value('downloads.error_loading') + err.message
  } finally {
    isLoading.value = false
  }
}

const addToDownloads = async (bookId) => {
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) {
      throw new Error($t.value('downloads.error_not_authorized'))
    }

    await api.post('/downloads', {
      bookId,
      userId
    })

    showNotification('success', $t.value('downloads.add_success'))
    await loadDownloadedBooks() // Обновляем список
  } catch (err) {
    showNotification('error', $t.value('downloads.add_error') + err.message)
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

const openBook = (book) => {
  if (book.pdfUrl) {
    currentPdfUrl.value = book.pdfUrl
  } else {
    showNotification('error', $t.value('downloads.pdf_not_found'))
  }
}

const closePdfViewer = () => {
  currentPdfUrl.value = null
}

const downloadPDF = async (book) => {
  try {
    if (!book.pdfUrl) {
      throw new Error($t.value('downloads.pdf_not_found'))
    }

    const link = document.createElement('a')
    link.href = book.pdfUrl
    link.download = `${book.title}.pdf`
    document.body.appendChild(link)
    link.click()
    link.remove()

    showNotification('success', $t.value('downloads.download_success'))
  } catch (err) {
    showNotification('error', $t.value('downloads.download_error') + err.message)
  }
}

const removeBook = async (bookId) => {
  try {
    isRemoving.value = true

    await api.delete(`/downloads/${bookId}`)

    downloads.value = downloads.value.filter((book) => book.id !== bookId)

    if (currentPdfUrl.value) {
      closePdfViewer()
    }

    showNotification('success', $t.value('downloads.remove_success'))
  } catch (err) {
    showNotification('error', $t.value('downloads.remove_error') + err.message)
  } finally {
    isRemoving.value = false
  }
}

const showNotification = (type, message) => {
  notification.value = { type, message }
  setTimeout(() => {
    notification.value = null
  }, 3000)
}

// Загружаем книги при монтировании компонента
onMounted(loadDownloadedBooks)
</script>

<template>
  <div class="downloads">
    <div class="container">
      <h1>{{ $t('downloads.title') }}</h1>

      <div v-if="notification" :class="['notification', notification.type]">
        {{ notification.message }}
      </div>

      <div v-if="isLoading" class="loading">{{ $t('downloads.loading') }}</div>

      <div v-else-if="error" class="error">
        {{ error }}
      </div>

      <div v-else-if="downloads.length === 0" class="no-downloads">
        {{ $t('downloads.empty') }}
      </div>

      <div v-else class="downloads-grid">
        <div v-for="book in downloads" :key="book.id" class="download-card">
          <img :src="book.imageUrl" :alt="book.title" class="book-image" />
          <div class="book-details">
            <h3>{{ book.title }}</h3>
            <p class="download-date">
              {{ $t('downloads.downloaded') }} {{ formatDate(book.downloadDate) }}
            </p>
            <div class="book-actions">
              <button @click="openBook(book)" class="action-btn open-btn">
                {{ $t('downloads.open') }}
              </button>
              <button @click="downloadPDF(book)" class="action-btn download-btn">
                {{ $t('downloads.download_pdf') }}
              </button>
              <button
                @click="removeBook(book.id)"
                :disabled="isRemoving"
                class="action-btn remove-btn"
              >
                {{ isRemoving ? $t('downloads.removing') : $t('downloads.remove') }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- PDF Viewer -->
      <div v-if="currentPdfUrl" class="pdf-viewer-container">
        <div class="pdf-viewer-header">
          <button @click="closePdfViewer" class="close-btn">{{ $t('downloads.close') }}</button>
        </div>
        <iframe :src="currentPdfUrl" class="pdf-viewer"></iframe>
      </div>
    </div>
  </div>
</template>

<style lang="scss">
// Responsive breakpoints
$breakpoint-xxl: 2560px;
$breakpoint-xl: 1440px;
$breakpoint-lg: 1024px;
$breakpoint-md: 786px;
$breakpoint-sm: 425px;
$breakpoint-xs: 375px;
$breakpoint-xxs: 320px;

.downloads {
  padding: 20px;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;

    @media screen and (max-width: $breakpoint-md) {
      padding: 0 10px;
    }
  }

  h1 {
    font-size: 2.5rem;
    margin-bottom: 30px;
    text-align: center;

    @media screen and (max-width: $breakpoint-lg) {
      font-size: 2rem;
    }

    @media screen and (max-width: $breakpoint-sm) {
      font-size: 1.5rem;
      margin-bottom: 20px;
    }
  }

  .notification {
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 15px 20px;
    border-radius: 4px;
    color: white;
    z-index: 1001;
    max-width: 300px;
    word-wrap: break-word;

    @media screen and (max-width: $breakpoint-sm) {
      top: 10px;
      right: 10px;
      padding: 10px 15px;
      font-size: 0.9rem;
    }
  }

  .downloads-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 20px;
    margin-top: 20px;

    @media screen and (max-width: $breakpoint-lg) {
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 15px;
    }

    @media screen and (max-width: $breakpoint-md) {
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 10px;
    }

    @media screen and (max-width: $breakpoint-sm) {
      grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
      gap: 8px;
    }
  }

  .download-card {
    border: 1px solid #e0e0e0;
    border-radius: 12px; // Увеличен радиус для более современного вида
    overflow: hidden;
    background: white;
    transition:
      transform 0.3s ease,
      box-shadow 0.3s ease;

    &:hover {
      transform: scale(1.03);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1); // Тень при наведении
    }

    @media screen and (max-width: $breakpoint-sm) {
      border-radius: 8px;
    }
  }

  .book-image {
    width: 100%;
    height: 350px;
    object-fit: cover;

    @media screen and (max-width: $breakpoint-lg) {
      height: 300px;
    }

    @media screen and (max-width: $breakpoint-md) {
      height: 250px;
    }

    @media screen and (max-width: $breakpoint-sm) {
      height: 200px;
    }
  }

  .book-details {
    padding: 15px;

    @media screen and (max-width: $breakpoint-sm) {
      padding: 10px;
    }

    h3 {
      font-size: 1.2rem;
      margin-bottom: 10px;
      color: #333;

      @media screen and (max-width: $breakpoint-sm) {
        font-size: 1rem;
        margin-bottom: 5px;
      }
    }

    .download-date {
      font-size: 0.9rem;
      color: #666;
      margin-bottom: 15px;

      @media screen and (max-width: $breakpoint-sm) {
        font-size: 0.8rem;
        margin-bottom: 10px;
      }
    }
  }

  .book-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-top: 15px;

    @media screen and (max-width: $breakpoint-sm) {
      gap: 8px;
      margin-top: 10px;
    }

    .action-btn {
      padding: 10px 20px;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      transition:
        background-color 0.3s ease,
        transform 0.2s ease;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

      &:hover {
        transform: scale(1.05);
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }

    .open-btn {
      background: linear-gradient(45deg, #4caf50, #45a049); // Зелёный градиент для "Открыть"
      color: white;

      &:hover {
        background: linear-gradient(45deg, #45a049, #388e3c);
      }
    }

    .download-btn {
      background: linear-gradient(45deg, #2196f3, #1976d2); // Синий градиент для "Скачать PDF"
      color: white;

      &:hover {
        background: linear-gradient(45deg, #1976d2, #1565c0);
      }
    }

    .remove-btn {
      background: linear-gradient(45deg, #f44336, #d32f2f); // Красный градиент для "Удалить"
      color: white;

      &:hover {
        background: linear-gradient(45deg, #d32f2f, #b71c1c);
      }

      &:disabled {
        background: linear-gradient(45deg, #f44336, #d32f2f);
        opacity: 0.6;
      }
    }
  }

  .pdf-viewer-container {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.8);
    z-index: 1000;
    display: flex;
    flex-direction: column;

    @media screen and (max-width: $breakpoint-sm) {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
    }
  }

  .pdf-viewer-header {
    padding: 10px;
    background: #333;
    display: flex;
    justify-content: flex-end;

    @media screen and (max-width: $breakpoint-sm) {
      padding: 8px;
    }

    .close-btn {
      padding: 8px 16px;
      background: #fff;
      color: #333;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      transition:
        background-color 0.3s ease,
        transform 0.2s ease;

      &:hover {
        background: #ddd;
        transform: scale(1.05);
      }
    }
  }

  .pdf-viewer {
    flex: 1;
    width: 100%;
    height: calc(100vh - 50px);
    border: none;

    @media screen and (max-width: $breakpoint-sm) {
      height: 100%;
    }
  }

  .loading,
  .error,
  .no-downloads {
    text-align: center;
    padding: 40px;
    font-size: 18px;

    @media screen and (max-width: $breakpoint-sm) {
      padding: 20px;
      font-size: 16px;
    }
  }

  .notification.success {
    background-color: #4caf50; // Зеленый для успеха
  }

  .notification.error {
    background-color: #f44336; // Красный для ошибок
  }
}
</style>
