<script setup>
import { ref, computed, watch } from 'vue'
import api from '../Axios'
import { useLanguageStore } from '@/language'
import {
  PageHeader as APageHeader,
  Input as AInputSearch,
  Collapse as ACollapse,
  CollapsePanel as ACollapsePanel,
  Empty as AEmpty,
  Spin as ASpin
} from 'ant-design-vue'

const faqs = ref([])
const searchKeyword = ref('')
const loading = ref(false)

const languageStore = useLanguageStore()
const $t = computed(() => (key) => {
  const translation = languageStore.translations[key] || key
  console.log(`Translating key "${key}":`, translation)
  return translation
})
const currentLanguage = computed(() => languageStore.currentLanguage)

// Загрузка FAQ с переводом
const fetchFaqs = async () => {
  loading.value = true
  try {
    const response = await api.get('/faqs', {
      headers: {
        'Accept-Language': currentLanguage.value
      }
    })
    faqs.value = response.data.map((faq) => ({
      id: faq.id,
      question: $t.value(faq.question),
      answer: $t.value(faq.answer)
    }))
  } catch (error) {
    console.error($t.value('faq.error_loading'), error)
  } finally {
    loading.value = false
  }
}

// Поиск FAQ с переводом
const searchFaqs = async () => {
  if (!searchKeyword.value) {
    fetchFaqs()
    return
  }
  loading.value = true
  try {
    const response = await api.get('/faqs/search', {
      params: { keyword: searchKeyword.value },
      headers: {
        'Accept-Language': currentLanguage.value
      }
    })
    faqs.value = response.data.map((faq) => ({
      id: faq.id,
      question: $t.value(faq.question),
      answer: $t.value(faq.answer)
    }))
  } catch (error) {
    console.error($t.value('faq.error_searching'), error)
  } finally {
    loading.value = false
  }
}

// Инициализация и реакция на смену языка
fetchFaqs()
watch(currentLanguage, () => {
  fetchFaqs()
})
</script>

<template>
  <div class="faq-container p-10">
    <a-page-header
      :title="$t('faq.title')"
      style="margin-bottom: 20px"
      @back="() => $router.go(-1)"
    />

    <a-input-search
      v-model:value="searchKeyword"
      :placeholder="$t('faq.search_placeholder')"
      @search="searchFaqs"
      style="margin-bottom: 20px; width: 100%; max-width: 600px"
      allow-clear
    />

    <a-collapse v-if="faqs.length" accordion>
      <a-collapse-panel v-for="faq in faqs" :key="faq.id" :header="faq.question">
        <p>{{ faq.answer }}</p>
      </a-collapse-panel>
    </a-collapse>

    <a-empty v-if="faqs.length === 0 && searchKeyword" :description="$t('faq.not_found')" />
    <a-spin v-if="loading" size="large" style="display: block; margin: 20px auto" />
  </div>
</template>

<style scoped>
.faq-container {
  max-width: 800px;
  margin: 0 auto;
}
</style>
