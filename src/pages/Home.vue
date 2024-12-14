<script setup>
import { inject, watch, ref, onMounted } from 'vue'
import debounce from 'lodash.debounce'
import api from '../Axios'

const books = ref([])
const loading = ref(true)
const error = ref(null)

const filters = inject('filters')
const showFilters = inject('showFilters')
const toggleFilters = inject('toggleFilters')

const onChangeSelect = (event) => {
  filters.value.sortBy = event.target.value
}

const onChangeSearchInput = debounce((event) => {
  filters.value.searchQuery = event.target.value
}, 150)

const onChangeSearchField = (event) => {
  filters.value.searchField = event.target.value
}

const addToFavorite = async (book) => {
  try {
    if (!book.isFavorite) {
      const obj = { book_id: book.id }
      book.isFavorite = true
      const { data } = await api.post('/favorites', obj)
      book.favoriteId = data.id
    } else {
      book.isFavorite = false
      await api.delete(`/favorites/${book.favoriteId}`)
      book.favoriteId = null
    }
  } catch (err) {
    console.error('Error updating favorites:', err)
  }
}

const downloadBook = async (book) => {
  try {
    saveBookToLocalDownloads(book)
    await saveBookToApiDownloads(book)
    alert(`Книга "${book.title}" добавлена в загрузки`)
  } catch (err) {
    console.error('Error downloading book:', err)
    alert('Произошла ошибка при скачивании книги')
  }
}

const saveBookToLocalDownloads = (book) => {
  const downloadedBooks = JSON.parse(localStorage.getItem('downloadedBooks') || '[]')
  if (!downloadedBooks.some((downloadedBook) => downloadedBook.id === book.id)) {
    downloadedBooks.push({
      ...book,
      downloadDate: new Date().toISOString()
    })
    localStorage.setItem('downloadedBooks', JSON.stringify(downloadedBooks))
  }
}

const saveBookToApiDownloads = async (book) => {
  try {
    await api.post('/downloads', {
      book_id: book.id,
      downloadDate: new Date().toISOString()
    })
  } catch (err) {
    console.error('Error saving to API downloads:', err)
    throw err
  }
}

const fetchBooks = async () => {
  try {
    const params = {
      sortBy: filters.value.sortBy
    }

    if (filters.value.searchQuery) {
      params[filters.value.searchField] = `*${filters.value.searchQuery}*`
    }

    if (filters.value.selectedGenres.length) {
      params.genre = filters.value.selectedGenres
    }

    if (filters.value.selectedLanguages.length) {
      params.language = filters.value.selectedLanguages
    }

    if (filters.value.selectedAges.length) {
      params.age = filters.value.selectedAges
    }

    if (filters.value.selectedRating) {
      params.ratings = filters.value.selectedRating.toString()
    }

    const [booksResponse, favoritesResponse] = await Promise.all([
      api.get('/books', { params }),
      api.get('/favorites')
    ])

    books.value = booksResponse.data.map((book) => {
      const favorite = favoritesResponse.data.find((f) => f.book_id === book.id)
      return {
        ...book,
        isFavorite: !!favorite,
        favoriteId: favorite?.id || null
      }
    })

    loading.value = false
  } catch (err) {
    error.value = 'Не удалось загрузить книги'
    loading.value = false
    console.error(err)
  }
}

onMounted(fetchBooks)
watch(() => filters.value, fetchBooks, { deep: true })
</script>

<template>
  <div class="home">
    <div class="container">
      <div class="flex justify-between items-center mb-8">
        <h2 class="text-3xl font-bold">Вся литература</h2>

        <div class="flex gap-4">
          <select @change="onChangeSelect" class="py-2 px-3 border rounded-md outline-none">
            <option value="title">По названию</option>
            <option value="-year">По дате публикации (сначала новые)</option>
            <option value="year">По дате публикации (сначала старые)</option>
          </select>

          <select @change="onChangeSearchField" class="py-2 px-3 border rounded-md outline-none">
            <option value="title">Поиск по названию</option>
            <option value="author">Поиск по автору</option>
          </select>

          <div class="relative">
            <img class="absolute left-4 top-3" src="/search.svg" alt="search" />
            <input
              @input="onChangeSearchInput"
              class="border rounded-md py-2 pl-11 pr-4 outline-none focus:border-gray-400"
              type="text"
              placeholder="Поиск..."
            />
          </div>

          <button
            @click="toggleFilters"
            class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition"
          >
            Фильтры
          </button>
        </div>
      </div>

      <div v-if="loading" class="text-center py-10">Загрузка книг...</div>

      <div v-else-if="error" class="text-center py-10 text-red-500">
        {{ error }}
      </div>

      <div v-else class="grid grid-cols-4 gap-5">
        <div
          v-for="book in books"
          :key="book.id"
          class="relative bg-white border border-slate-100 rounded-3xl p-8 transition hover:-translate-y-2 hover:shadow-xl"
        >
          <img
            :src="!book.isFavorite ? '/like-1.svg' : '/like-2.svg'"
            alt="like"
            class="absolute top-8 left-8 cursor-pointer"
            @click="addToFavorite(book)"
          />

          <img :src="book.imageUrl" :alt="book.title" class="w-full h-48 object-cover mb-4" />

          <h3 class="font-semibold mb-2">{{ book.title }}</h3>
          <p class="text-gray-600 mb-1">{{ book.author }}</p>

          <div class="mb-2">
            <a-rate :value="Number(book.ratings)" disabled />
          </div>

          <button
            @click="downloadBook(book)"
            class="w-full bg-blue-500 text-white py-2 px-4 rounded-lg hover:bg-blue-600 transition"
          >
            Скачать
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss">
@mixin responsive-breakpoint($breakpoint) {
  @media screen and (max-width: $breakpoint) {
    @content;
  }
}

.home {
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;

    // 2560px - Large Desktop
    @include responsive-breakpoint(2560px) {
      max-width: 1920px;
      padding: 3rem;
    }

    // 1440px - Desktop
    @include responsive-breakpoint(1440px) {
      max-width: 1200px;
      padding: 2rem;
    }

    // 1024px - Tablet Landscape
    @include responsive-breakpoint(1024px) {
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

    // 786px - Tablet Portrait
    @include responsive-breakpoint(786px) {
      padding: 1rem;

      .flex {
        flex-direction: column;
        
        select, input, button {
          width: 100%;
          margin-bottom: 0.5rem;
        }
      }

      .grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 1rem;
      }
    }

    // 425px - Large Mobile
    @include responsive-breakpoint(425px) {
      padding: 0.5rem;

      .grid {
        grid-template-columns: 1fr;
        gap: 0.75rem;
      }

      .flex {
        flex-direction: column;
        gap: 0.5rem;

        select, input, button {
          font-size: 0.875rem;
          padding: 0.5rem;
        }
      }
    }

    // 375px - Medium Mobile
    @include responsive-breakpoint(375px) {
      .grid {
        gap: 0.5rem;
      }

      .book-card {
        padding: 1rem;

        img {
          height: 12rem;
        }

        h3 {
          font-size: 0.95rem;
        }

        p {
          font-size: 0.85rem;
        }
      }
    }

    // 320px - Small Mobile
    @include responsive-breakpoint(320px) {
      padding: 0.25rem;

      .flex {
        flex-direction: column;
        gap: 0.25rem;

        select, input, button {
          font-size: 0.75rem;
          padding: 0.375rem;
        }
      }

      .grid {
        gap: 0.25rem;
      }

      .book-card {
        padding: 0.5rem;

        img {
          height: 10rem;
        }

        h3 {
          font-size: 0.85rem;
          margin-bottom: 0.25rem;
        }

        p {
          font-size: 0.75rem;
          margin-bottom: 0.25rem;
        }

        .download-button {
          font-size: 0.75rem;
          padding: 0.375rem;
        }
      }
    }
  }
}
</style>
