<script>
import { ref, onMounted } from 'vue'
import api from '../Axios'

export default {
  data() {
    return {
      downloads: [],
      currentPdfUrl: null,
      isLoading: true,
      isRemoving: false,
      error: null,
      notification: null
    }
  },

  async mounted() {
    await this.loadDownloadedBooks()
  },

  methods: {
    async loadDownloadedBooks() {
      try {
        this.isLoading = true
        this.error = null

        // Загрузка списка скачанных книг с API
        const { data } = await api.get('/downloads')

        // Добавляем путь к PDF для каждой книги
        this.downloads = data.map((book) => ({
          ...book,
          pdfUrl: `/library/pdfs/book-${book.id}.pdf`,
          imageUrl: `/library/book-${book.id}.jpg`
        }))
      } catch (error) {
        this.error = 'Произошла ошибка при загрузке книг: ' + error.message
      } finally {
        this.isLoading = false
      }
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      })
    },

    openBook(book) {
      if (book.pdfUrl) {
        this.currentPdfUrl = book.pdfUrl
      } else {
        this.showNotification('error', 'PDF не найден')
      }
    },

    closePdfViewer() {
      this.currentPdfUrl = null
    },

    async downloadPDF(book) {
      try {
        if (!book.pdfUrl) {
          throw new Error('PDF не найден')
        }

        // Создаем ссылку на локальный PDF файл
        const link = document.createElement('a')
        link.href = book.pdfUrl
        link.download = `${book.title}.pdf`
        document.body.appendChild(link)
        link.click()
        link.remove()

        this.showNotification('success', 'Файл успешно скачан')
      } catch (error) {
        this.showNotification('error', 'Ошибка при скачивании: ' + error.message)
      }
    },

    async removeBook(bookId) {
      try {
        this.isRemoving = true

        // Удаление записи о скачивании из API
        await api.delete(`/downloads/${bookId}`)

        // Удаляем книгу из локального состояния
        this.downloads = this.downloads.filter((book) => book.id !== bookId)

        // Закрываем просмотрщик PDF, если он открыт
        if (this.currentPdfUrl) {
          this.closePdfViewer()
        }

        this.showNotification('success', 'Книга успешно удалена из загрузок')
      } catch (error) {
        this.showNotification('error', 'Не удалось удалить книгу: ' + error.message)
      } finally {
        this.isRemoving = false
      }
    },

    showNotification(type, message) {
      this.notification = { type, message }
      setTimeout(() => {
        this.notification = null
      }, 3000)
    }
  }
}
</script>

<template>
  <div class="downloads">
    <div class="container">
      <h1>Мои загрузки</h1>

      <div v-if="notification" :class="['notification', notification.type]">
        {{ notification.message }}
      </div>

      <div v-if="isLoading" class="loading">Загрузка...</div>

      <div v-else-if="error" class="error">
        {{ error }}
      </div>

      <div v-else-if="downloads.length === 0" class="no-downloads">
        У вас пока нет загруженных книг
      </div>

      <div v-else class="downloads-grid">
        <div v-for="book in downloads" :key="book.id" class="download-card">
          <img :src="book.imageUrl" :alt="book.title" class="book-image" />
          <div class="book-details">
            <h3>{{ book.title }}</h3>
            <p class="download-date">Скачано: {{ formatDate(book.downloadDate) }}</p>
            <div class="book-actions">
              <button @click="openBook(book)" class="open-btn">Открыть</button>
              <button @click="downloadPDF(book)" class="download-btn">Скачать PDF</button>
              <button @click="removeBook(book.id)" :disabled="isRemoving" class="remove-btn">
                {{ isRemoving ? 'Удаление...' : 'Удалить' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- PDF Viewer -->
      <div v-if="currentPdfUrl" class="pdf-viewer-container">
        <div class="pdf-viewer-header">
          <button @click="closePdfViewer" class="close-btn">Закрыть</button>
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
    border-radius: 8px;
    overflow: hidden;
    background: white;
    transition: transform 0.3s ease;

    &:hover {
      transform: scale(1.03);
    }

    @media screen and (max-width: $breakpoint-sm) {
      border-radius: 4px;
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

      @media screen and (max-width: $breakpoint-sm) {
        font-size: 1rem;
        margin-bottom: 5px;
      }
    }

    .download-date {
      font-size: 0.9rem;
      color: #666;

      @media screen and (max-width: $breakpoint-sm) {
        font-size: 0.8rem;
      }
    }
  }

  .book-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 15px;

    @media screen and (max-width: $breakpoint-sm) {
      gap: 6px;
      margin-top: 10px;
    }

    button {
      padding: 8px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
      transition: background-color 0.3s ease;

      @media screen and (max-width: $breakpoint-sm) {
        padding: 6px;
        font-size: 0.8rem;
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
}
</style>