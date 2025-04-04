<script setup>
import { inject, watch, ref, onMounted, computed } from 'vue'
import { useLanguageStore } from '@/language'
import debounce from 'lodash.debounce'
import api from '../Axios'
import FaqChat from '../components/FaqChat.vue'

const languageStore = useLanguageStore()

// Computed translation method
const $t = computed(() => (key) => {
  const translation = languageStore.translations[key] || key
  return translation
})

// Helper function to calculate average rating
const calculateAverageRating = (reviews) => {
  if (!reviews || reviews.length === 0) return 0
  const totalRating = reviews.reduce((sum, review) => sum + review.rating, 0)
  return Math.round(totalRating / reviews.length)
}

const books = ref([])
const loading = ref(true)
const error = ref(null)
const filters = inject('filters')
const showFilters = inject('showFilters')
const toggleFilters = inject('toggleFilters')

// Dynamically get userId from localStorage
const userId = ref(localStorage.getItem('userId') || null)

if (!userId.value) {
  console.error($t.value('home.user_not_authorized'))
}

const selectedRating = ref(0)
const comment = ref('')
const showReviewModal = ref(false)
const currentBook = ref(null)
const showComments = ref({})

const onChangeSelect = (event) => {
  filters.value.sortBy = event.target.value
  console.log('Sorting changed:', filters.value.sortBy)
}

const onChangeSearchInput = debounce((event) => {
  filters.value.searchQuery = event.target.value
  console.log('Search changed:', filters.value.searchQuery)
}, 150)

const onChangeSearchField = (event) => {
  filters.value.searchField = event.target.value
  console.log('Search field changed:', filters.value.searchField)
}

const addToFavorite = async (book) => {
  try {
    if (!userId.value) {
      throw new Error($t.value('home.user_not_authorized'))
    }
    if (!book.isFavorite) {
      book.isFavorite = true
      const { data } = await api.post(`/favorites/${userId.value}`, { bookId: book.id })
      book.favoriteId = data.id
    } else {
      book.isFavorite = false
      await api.delete(`/favorites/${userId.value}/${book.id}`)
      book.favoriteId = null
    }
    await fetchBooks()
  } catch (err) {
    book.isFavorite = !book.isFavorite
    console.error('Favorite update error:', err)
  }
}

const downloadBook = async (book) => {
  try {
    saveBookToLocalDownloads(book)
    await saveBookToApiDownloads(book)
    alert($t.value('home.book_added_to_downloads').replace('{title}', book.title))
  } catch (err) {
    console.error('Download error:', err)
    alert($t.value('home.download_error_message'))
  }
}

const saveBookToLocalDownloads = (book) => {
  const downloadedBooks = JSON.parse(localStorage.getItem('downloadedBooks') || '[]')
  if (!downloadedBooks.some((downloadedBook) => downloadedBook.id === book.id)) {
    downloadedBooks.push({ ...book, downloadDate: new Date().toISOString() })
    localStorage.setItem('downloadedBooks', JSON.stringify(downloadedBooks))
  }
}

const saveBookToApiDownloads = async (book) => {
  try {
    if (!userId.value) {
      throw new Error($t.value('home.user_not_authorized'))
    }
    await api.post('/downloads', { bookId: book.id, userId: userId.value })
  } catch (err) {
    console.error('API download error:', err)
    throw err
  }
}

const openReviewModal = (book) => {
  currentBook.value = book
  selectedRating.value = 0
  comment.value = ''
  showReviewModal.value = true
}

const submitReview = async () => {
  try {
    if (!userId.value) {
      throw new Error($t.value('home.user_not_authorized'))
    }
    if (selectedRating.value < 1 || selectedRating.value > 5) {
      alert($t.value('home.rating_validation'))
      return
    }
    await api.post('/reviews', {
      bookId: currentBook.value.id,
      userId: userId.value,
      rating: selectedRating.value,
      comment: comment.value
    })
    showReviewModal.value = false
    await fetchBooks()
  } catch (err) {
    console.error('Review submit error:', err)
    alert($t.value('home.review_submit_error_message'))
  }
}

const fetchReviews = async (bookId) => {
  try {
    const { data } = await api.get(`/reviews/${bookId}`)
    return data || []
  } catch (err) {
    console.error('Reviews fetch error:', err)
    return []
  }
}

const toggleComments = (bookId) => {
  showComments.value = {
    ...showComments.value,
    [bookId]: !showComments.value[bookId]
  }
}

const fetchBooks = async () => {
  try {
    if (!userId.value) {
      throw new Error($t.value('home.user_not_authorized'))
    }

    const params = { sort: filters.value.sortBy }
    if (filters.value.searchQuery) {
      params.search = filters.value.searchQuery
      params.field = filters.value.searchField
    }

    if (filters.value.selectedGenres?.length) params.genre = filters.value.selectedGenres.join(',')

    if (filters.value.selectedLanguages?.length)
      params.language = filters.value.selectedLanguages.join(',')

    if (filters.value.selectedAges?.length) params.age = filters.value.selectedAges.join(',')

    if (filters.value.selectedRating) params.ratings = filters.value.selectedRating.toString()

    const [booksResponse, favoritesResponse] = await Promise.all([
      api.get('/books', { params }),
      api.get(`/favorites/${userId.value}`)
    ])

    books.value = await Promise.all(
      booksResponse.data.map(async (book) => {
        const isFavorite = favoritesResponse.data.some(
          (favoriteBook) => favoriteBook.id === book.id
        )
        const reviews = await fetchReviews(book.id)

        return {
          ...book,
          isFavorite,
          imageUrl: book.imageUrl || '/default-book.jpg',
          reviews: reviews,
          averageRating: calculateAverageRating(reviews)
        }
      })
    )

    loading.value = false
  } catch (err) {
    error.value = $t.value('home.books_load_error') + ': ' + (err.message || err)
    loading.value = false
    console.error('Books load error:', err)
  }
}

onMounted(fetchBooks)
watch(() => filters.value, fetchBooks, { deep: true })
</script>

<template>
  <div class="home">
    <div class="container">
      <div class="flex justify-between items-center mb-8">
        <h2 class="text-3xl font-bold text-gray-800">{{ $t('home.all_literature') }}</h2>
        <div class="flex gap-4 flex-wrap">
          <select
            @change="onChangeSelect"
            class="py-2 px-3 border rounded-md outline-none bg-white shadow-sm"
          >
            <option value="title">{{ $t('home.sort_by_title') }}</option>
            <option value="-year">{{ $t('home.sort_by_date_newest') }}</option>
            <option value="year">{{ $t('home.sort_by_date_oldest') }}</option>
          </select>
          <select
            @change="onChangeSearchField"
            class="py-2 px-3 border rounded-md outline-none bg-white shadow-sm"
          >
            <option value="title">{{ $t('home.sort_by_title') }}</option>
            <option value="author">{{ $t('home.search_by_author') }}</option>
          </select>
          <div class="relative">
            <img class="absolute left-4 top-3" src="/search.svg" alt="search" />
            <input
              @input="onChangeSearchInput"
              class="border rounded-md py-2 pl-11 pr-4 outline-none focus:border-blue-300 bg-white shadow-sm"
              type="text"
              :placeholder="$t('home.search_placeholder')"
            />
          </div>
          <button
            @click="toggleFilters"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition shadow-md"
          >
            {{ $t('home.filters') }}
          </button>
        </div>
      </div>

      <div v-if="loading" class="text-center py-10 text-gray-600">
        {{ $t('home.loading_books') }}
      </div>
      <div v-else-if="error" class="text-center py-10 text-red-500">{{ error }}</div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div
          v-for="book in books"
          :key="book.id"
          class="relative bg-white border border-gray-200 rounded-xl p-6 shadow-md hover:shadow-lg transition-transform hover:-translate-y-1"
        >
          <img
            :src="!book.isFavorite ? '/like-1.svg' : '/like-2.svg'"
            alt="like"
            class="absolute top-6 left-6 cursor-pointer w-6 h-6"
            @click="addToFavorite(book)"
          />
          <img
            :src="book.imageUrl"
            :alt="book.title"
            class="w-full h-48 object-cover rounded-md mb-4"
          />
          <h3 class="font-semibold text-lg text-gray-800 mb-2">{{ book.title }}</h3>
          <p class="text-gray-600 text-sm mb-2">{{ book.author }}</p>
          <div class="mb-4 flex items-center gap-2">
            <a-rate
              :value="Number(book.averageRating || book.ratings)"
              disabled
              class="text-yellow-400"
            />
            <span class="text-gray-600 text-sm">
              {{ $t('home.reviews_count').replace('{}', book.reviews ? book.reviews.length : 0) }}
            </span>
          </div>
          <button
            @click="downloadBook(book)"
            class="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 mb-2 transition shadow-md"
          >
            {{ $t('home.download') }}
          </button>
          <button
            @click="openReviewModal(book)"
            class="w-full bg-gray-200 text-gray-800 py-2 px-4 rounded-md hover:bg-gray-300 transition shadow-md"
          >
            {{ $t('home.leave_review') }}
          </button>

          <button
            @click="toggleComments(book.id)"
            class="mt-4 w-full text-blue-600 hover:text-blue-800 text-sm font-medium hover:underline"
          >
            {{ showComments[book.id] ? $t('home.hide_comments') : $t('home.comments') }} ({{
              book.reviews ? book.reviews.length : 0
            }})
          </button>

          <div v-if="showComments[book.id]" class="mt-2 max-h-40 overflow-y-auto">
            <div
              v-for="review in book.reviews"
              :key="review.id"
              class="text-sm text-gray-600 p-2 border-t border-gray-200"
            >
              <p>
                <strong>{{ $t('home.rating_label') }}</strong> {{ review.rating }} / 5
              </p>
              <p>
                <strong>{{ $t('home.comment_label') }}</strong>
                {{ review.comment || $t('home.no_comment') }}
              </p>
              <p class="text-xs text-gray-500">
                {{ $t('home.author_label') }}
                {{ review.user?.username || $t('home.anonymous') }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showReviewModal"
      class="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center"
    >
      <div class="bg-white p-6 rounded-lg shadow-lg w-96 max-w-full">
        <h3 class="text-xl font-semibold text-gray-800 mb-4">
          {{ $t('home.review_book_title').replace('{title}', currentBook?.title || '') }}
        </h3>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            {{ $t('home.rating_label') }}
          </label>
          <a-rate v-model:value="selectedRating" class="text-yellow-400" />
        </div>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            {{ $t('home.comment_label') }}
          </label>
          <textarea
            v-model="comment"
            class="w-full border border-gray-300 rounded-md p-2 text-sm focus:border-blue-300 focus:ring-0"
            rows="4"
            :placeholder="$t('home.your_comment_placeholder')"
          />
        </div>
        <div class="flex justify-end gap-2">
          <button
            @click="showReviewModal = false"
            class="px-4 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 text-sm transition"
          >
            {{ $t('home.cancel') }}
          </button>
          <button
            @click="submitReview"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm transition shadow-md"
          >
            {{ $t('home.submit') }}
          </button>
        </div>
      </div>
    </div>

    <FaqChat />
  </div>
</template>

<style lang="scss">
.home {
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;

    @media (max-width: 2560px) {
      max-width: 1920px;
      padding: 3rem;
    }

    @media (max-width: 1440px) {
      max-width: 1200px;
      padding: 2rem;
    }

    @media (max-width: 1024px) {
      padding: 1.5rem;

      .flex {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;

        h2 {
          width: 100%;
          margin-bottom: 1rem;
        }
      }

      .grid {
        grid-template-columns: repeat(3, 1fr);
      }
    }

    @media (max-width: 768px) {
      padding: 1rem;

      .flex {
        flex-direction: column;

        select,
        input,
        button {
          width: 100%;
          margin-bottom: 0.5rem;
        }
      }

      .grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 1rem;
      }
    }

    @media (max-width: 425px) {
      padding: 0.5rem;

      .grid {
        grid-template-columns: 1fr;
        gap: 0.75rem;
      }

      .flex {
        flex-direction: column;
        gap: 0.5rem;

        select,
        input,
        button {
          font-size: 0.875rem;
          padding: 0.5rem;
        }
      }
    }
  }
}
</style>
