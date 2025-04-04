<template>
  <div class="language-switcher">
    <span :class="{ active: currentLanguage === 'ru' }" @click="switchLanguage('ru')"> RU </span>
    |
    <span :class="{ active: currentLanguage === 'en' }" @click="switchLanguage('en')"> EN </span>
  </div>
</template>

<script>
import { useLanguageStore } from '@/language'

export default {
  name: 'LanguageSwitcher',
  setup() {
    const languageStore = useLanguageStore()
    return { languageStore }
  },
  computed: {
    currentLanguage() {
      return this.languageStore.currentLanguage
    }
  },
  methods: {
    switchLanguage(lang) {
      // Проверяем, отличается ли выбранный язык от текущего
      if (this.currentLanguage !== lang) {
        this.languageStore.setLanguage(lang)
      }
    }
  }
}
</script>

<style scoped>
.language-switcher {
  cursor: pointer;
  color: white;
  font-weight: bold;
}

.language-switcher span.active {
  color: black; /* Подсветка активного языка */
}
</style>
