<script setup>
import { reactive, toRaw, computed } from 'vue'
import { useLanguageStore } from '@/language' // Импортируем useLanguageStore

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

// Получаем хранилище языка
const languageStore = useLanguageStore()

// Реактивный метод для получения переводов
const $t = computed(() => (key) => {
  const translation = languageStore.translations[key] || key
  console.log(`Translating key "${key}":`, translation) // Логирование для отладки
  return translation
})

// Переводим статические данные
const ratingDescriptions = computed(() => [
  $t.value('filters.rating.poor'),
  $t.value('filters.rating.not_good'),
  $t.value('filters.rating.average'),
  $t.value('filters.rating.good'),
  $t.value('filters.rating.excellent')
])

const genres = computed(() => [
  $t.value('filters.genre.programming'),
  $t.value('filters.genre.physics'),
  $t.value('filters.genre.ai'),
  $t.value('filters.genre.math'),
  $t.value('filters.genre.systems'),
  $t.value('filters.genre.system_programming'),
  $t.value('filters.genre.computer_science'),
  $t.value('filters.genre.networks'),
  $t.value('filters.genre.automation'),
  $t.value('filters.genre.engineering'),
  $t.value('filters.genre.architecture')
])

const languages = computed(() => [
  $t.value('filters.language.russian'),
  $t.value('filters.language.english'),
  $t.value('filters.language.french'),
  $t.value('filters.language.spanish'),
  $t.value('filters.language.portuguese')
])

const ages = computed(() => [$t.value('filters.age.12_plus'), $t.value('filters.age.14_plus')])

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
        <h2 class="text-2xl font-bold">{{ $t('filters.title') }}</h2>
        <button @click="emit('close')" class="close-button">
          <img src="/close.svg" alt="close" class="w-6 h-6" />
        </button>
      </div>

      <div class="filters-content">
        <div class="filter-section">
          <h3 class="text-lg font-semibold mb-2">{{ $t('filters.genre_filter') }}</h3>
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
          <h3 class="text-lg font-semibold mb-2">{{ $t('filters.language_filter') }}</h3>
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
          <h3 class="text-lg font-semibold mb-2">{{ $t('filters.age_filter') }}</h3>
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
          <h3 class="text-lg font-semibold mb-2">{{ $t('filters.rating_filter') }}</h3>
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
          {{ $t('filters.apply_filters') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
}

.filters-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-button {
  padding: 0.5rem;
  border-radius: 0.375rem;
  transition: background-color 0.2s;
}

.close-button:hover {
  background-color: #f3f4f6;
}

.filters-content {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
}

.filter-section {
  margin-bottom: 2rem;
}

.filter-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  border-radius: 0.375rem;
  transition: background-color 0.2s;
  cursor: pointer;
}

.filter-checkbox:hover {
  background-color: #f3f4f6;
}

.filters-footer {
  padding: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

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
