<script setup>
import { reactive, toRaw } from 'vue'

const emit = defineEmits(['close', 'apply-filters'])

const props = defineProps({
  initialFilters: {
    type: Object,
    required: true
  }
})

// Создаем локальную копию фильтров
const localFilters = reactive({
  ...toRaw(props.initialFilters)
})

const ratingDescriptions = [
  'Terrible (1★)',
  'Bad (2★)',
  'Normal (3★)',
  'Good (4★)',
  'Wonderful (5★)'
]

const genres = [
  'Научная',
  'Фантастика',
  'Социальная',
  'Для детей',
  'Спортивный',
  'Бизнес',
  'Драма',
  'Детектив',
  'Ужасы',
  'Историческая',
  'Художественная литература'
]
const languages = ['русский', 'английский', 'китайский', 'казахский']
const ages = ['Для детей', 'Для подростков', 'Для взрослых', 'Для пожилых']

const toggleGenre = (genre) => {
  if (localFilters.selectedGenres.includes(genre)) {
    localFilters.selectedGenres = localFilters.selectedGenres.filter((g) => g !== genre)
  } else {
    localFilters.selectedGenres.push(genre)
  }
}

const toggleLanguage = (language) => {
  if (localFilters.selectedLanguages.includes(language)) {
    localFilters.selectedLanguages = localFilters.selectedLanguages.filter((l) => l !== language)
  } else {
    localFilters.selectedLanguages.push(language)
  }
}

const toggleAge = (age) => {
  if (localFilters.selectedAges.includes(age)) {
    localFilters.selectedAges = localFilters.selectedAges.filter((a) => a !== age)
  } else {
    localFilters.selectedAges.push(age)
  }
}

const onRatingChange = (value) => {
  localFilters.selectedRating = value
}

const applyFilters = () => {
  emit('apply-filters', { ...localFilters })
  emit('close')
}
</script>

<template>
  <div class="filters-overlay" @click="emit('close')">
    <div class="filters-panel" @click.stop>
      <div class="filters-header">
        <h2 class="text-2xl font-bold">Фильтры</h2>
        <button @click="emit('close')" class="close-button">
          <img src="/close.svg" alt="close" class="w-6 h-6" />
        </button>
      </div>

      <div class="filters-content">
        <div class="filter-section">
          <h3 class="text-lg font-semibold mb-2">Фильтрация по жанру:</h3>
          <div class="flex gap-4 flex-wrap">
            <label v-for="genre in genres" :key="genre" class="filter-checkbox">
              <input
                type="checkbox"
                :value="genre"
                :checked="localFilters.selectedGenres.includes(genre)"
                @change="toggleGenre(genre)"
              />
              {{ genre }}
            </label>
          </div>
        </div>

        <div class="filter-section">
          <h3 class="text-lg font-semibold mb-2">Фильтрация по языку:</h3>
          <div class="flex gap-4 flex-wrap">
            <label v-for="language in languages" :key="language" class="filter-checkbox">
              <input
                type="checkbox"
                :value="language"
                :checked="localFilters.selectedLanguages.includes(language)"
                @change="toggleLanguage(language)"
              />
              {{ language }}
            </label>
          </div>
        </div>

        <div class="filter-section">
          <h3 class="text-lg font-semibold mb-2">Фильтрация по возрасту:</h3>
          <div class="flex gap-4 flex-wrap">
            <label v-for="age in ages" :key="age" class="filter-checkbox">
              <input
                type="checkbox"
                :value="age"
                :checked="localFilters.selectedAges.includes(age)"
                @change="toggleAge(age)"
              />
              {{ age }}
            </label>
          </div>
        </div>

        <div class="filter-section">
          <h3 class="text-lg font-semibold mb-2">Фильтрация по рейтингу:</h3>
          <div class="flex items-center gap-2">
            <a-rate
              v-model:value="localFilters.selectedRating"
              :tooltips="ratingDescriptions"
              @change="onRatingChange"
            />
            <span class="text-sm text-gray-600" v-if="localFilters.selectedRating">
              {{ ratingDescriptions[localFilters.selectedRating - 1] }}
            </span>
          </div>
        </div>
      </div>

      <div class="filters-footer">
        <button
          @click="applyFilters"
          class="w-full bg-blue-500 text-white py-3 px-6 rounded-lg hover:bg-blue-600 transition"
        >
          Применить фильтры
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
// Variables for breakpoints
$breakpoints: (
  '2xl': 2560px,
  'xl': 1440px,
  'lg': 1024px,
  'md': 768px,
  'sm': 425px,
  'xs': 375px,
  'xxs': 320px
);

// Mixin for media queries
@mixin respond-to($breakpoint) {
  @if map-has-key($breakpoints, $breakpoint) {
    @media screen and (max-width: map-get($breakpoints, $breakpoint)) {
      @content;
    }
  }
}

.filters-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 50;
}

.filters-panel {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 400px;
  background-color: white;
  z-index: 51;
  display: flex;
  flex-direction: column;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
  animation: slideIn 0.3s ease-out forwards;

  @include respond-to('xl') {
    width: 360px;
  }

  @include respond-to('lg') {
    width: 320px;
  }

  @include respond-to('md') {
    width: 100%;
    max-width: 400px;
  }

  @include respond-to('sm') {
    width: 100%;
    max-width: none;
  }
}

.filters-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;

  @include respond-to('sm') {
    padding: 1rem;
  }

  h2 {
    @include respond-to('sm') {
      font-size: 1.5rem;
    }

    @include respond-to('xxs') {
      font-size: 1.25rem;
    }
  }
}

.filters-content {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;

  @include respond-to('sm') {
    padding: 1rem;
  }
}

.filter-section {
  margin-bottom: 2rem;

  @include respond-to('sm') {
    margin-bottom: 1.5rem;
  }

  h3 {
    @include respond-to('sm') {
      font-size: 1rem;
      margin-bottom: 0.5rem;
    }
  }
}

.filter-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  border-radius: 0.375rem;
  transition: background-color 0.2s;
  cursor: pointer;

  @include respond-to('sm') {
    font-size: 0.875rem;
    padding: 0.375rem;
  }

  @include respond-to('xxs') {
    font-size: 0.813rem;
  }
}

.filters-footer {
  padding: 1.5rem;
  border-top: 1px solid #e5e7eb;

  @include respond-to('sm') {
    padding: 1rem;
  }

  button {
    @include respond-to('sm') {
      padding: 0.75rem 1rem;
      font-size: 0.875rem;
    }
  }
}

// Animation keyframes
@keyframes slideIn {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}

@keyframes slideOut {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(100%);
  }
}
</style>
