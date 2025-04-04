<script setup>
import { ref, provide } from 'vue'
import Header from './components/Header.vue'
import Filters from './components/Filters.vue'

const showFilters = ref(false)
const filters = ref({
  sortBy: 'title',
  searchQuery: '',
  searchField: 'title',
  selectedGenres: [],
  selectedLanguages: [],
  selectedAges: [],
  selectedRating: null
})

const toggleFilters = () => {
  showFilters.value = !showFilters.value
  console.log('ТoggleFilters вызван, showFilters:', showFilters.value)
}

const updateFilters = (newFilters) => {
  filters.value = { ...filters.value, ...newFilters }
  console.log('Фильтры обновлены:', filters.value)
}

provide('filters', filters)
provide('showFilters', showFilters)
provide('toggleFilters', toggleFilters)
provide('updateFilters', updateFilters)
</script>

<template>
  <div class="relative">
    <div class="bg-white w-4/5 m-auto rounded-xl shadow-xl mt-14">
      <Header />
      <div class="p-10">
        <router-view></router-view>
      </div>
    </div>

    <Filters
      v-if="showFilters"
      :initial-filters="filters"
      @close="showFilters = false"
      @apply-filters="updateFilters"
    />
  </div>
</template>
