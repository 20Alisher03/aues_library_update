<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import api from '../Axios'
import { useLanguageStore } from '@/language'

const isVisible = ref(false)
const isOpen = ref(true)
const showFaqList = ref(false)
const faqs = ref([])
const userInput = ref('')
const messages = ref([])

const languageStore = useLanguageStore()
const $t = computed(() => (key) => {
  const translation = languageStore.translations[key] || key
  console.log(`Translating key "${key}":`, translation)
  return translation
})
const currentLanguage = computed(() => languageStore.currentLanguage)

// Загрузка FAQ с переводом
const fetchFaqs = async () => {
  try {
    const response = await api.get('/faqs', {
      headers: {
        'Accept-Language': currentLanguage.value
      }
    })
    // Переводим вопросы и ответы при получении данных
    faqs.value = response.data.map((faq) => ({
      ...faq,
      question: $t.value(faq.question),
      answer: $t.value(faq.answer)
    }))
  } catch (error) {
    console.error($t.value('faq_chat.error_loading'), error)
    messages.value.push({ text: $t.value('faq_chat.error_loading'), from: 'bot' })
  }
}

// Инициализация при монтировании
onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
    messages.value.push({ text: $t.value('faq_chat.hello'), from: 'bot' })
  }, 5000)
  fetchFaqs()
})

// Реактивное обновление FAQ при смене языка
watch(currentLanguage, () => {
  fetchFaqs()
})

// Отправка сообщения
const sendMessage = () => {
  if (!userInput.value.trim()) return

  const userMessage = userInput.value.trim().toLowerCase()
  messages.value.push({ text: userInput.value, from: 'user' })
  userInput.value = ''

  if (userMessage.includes($t.value('faq_chat.hello').toLowerCase())) {
    messages.value.push({ text: $t.value('faq_chat.how.can_help'), from: 'bot' })
    return
  }

  if (userMessage.includes($t.value('faq_chat.goodbye').toLowerCase())) {
    messages.value.push({ text: $t.value('faq_chat.goodbye_response'), from: 'bot' })
    return
  }

  // Поиск в переведённых вопросах
  const faq = faqs.value.find((f) => f.question.toLowerCase().includes(userMessage))
  if (faq) {
    messages.value.push({ text: faq.answer, from: 'bot' })
  } else {
    messages.value.push({ text: $t.value('faq_chat.unknown_response'), from: 'bot' })
  }

  setTimeout(() => {
    const chatMessages = document.querySelector('.chat-messages')
    chatMessages.scrollTop = chatMessages.scrollHeight
  }, 0)
}

// Выбор FAQ
const selectFaq = (faq) => {
  messages.value.push({ text: faq.question, from: 'user' })
  messages.value.push({ text: faq.answer, from: 'bot' })
  showFaqList.value = false

  setTimeout(() => {
    const chatMessages = document.querySelector('.chat-messages')
    chatMessages.scrollTop = chatMessages.scrollHeight
  }, 0)
}
</script>

<template>
  <div v-if="isVisible" class="faq-chat fixed bottom-4 left-4 z-50 transition-all duration-300">
    <div v-if="!isOpen" @click="isOpen = true" class="cursor-pointer">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-12 w-12 text-white-500 hover:text-blue-600"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
        />
      </svg>
    </div>

    <div v-else class="w-80 bg-white rounded-lg shadow-lg p-4 min-h-64 flex flex-col">
      <div class="flex justify-between items-center mb-2">
        <h3 class="text-lg font-semibold">{{ $t('faq_chat.title') }}</h3>
        <button
          @click="isOpen = false"
          class="text-gray-500 hover:text-gray-700 focus:outline-none"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <div class="faq-content flex-1">
        <div class="chat-messages max-h-48 overflow-y-auto mb-2 p-2 border rounded-md">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="{
              'text-right': message.from === 'user',
              'text-left': message.from === 'bot'
            }"
          >
            <span
              class="inline-block p-2 rounded-md"
              :class="{
                'bg-blue-100 text-blue-800': message.from === 'user',
                'bg-gray-100 text-gray-800': message.from === 'bot'
              }"
            >
              {{ message.text }}
            </span>
          </div>
        </div>

        <button
          v-if="!showFaqList"
          @click="showFaqList = true"
          class="w-full p-2 bg-gray-100 text-left rounded-md mb-2 hover:bg-gray-200"
        >
          {{ $t('faq.title') }} <span class="float-right">+</span>
        </button>

        <div v-if="showFaqList" class="max-h-32 overflow-y-auto mb-2">
          <div
            v-for="faq in faqs"
            :key="faq.id"
            @click="selectFaq(faq)"
            class="p-2 hover:bg-gray-100 cursor-pointer border-b last:border-b-0"
          >
            {{ faq.question }}
          </div>
        </div>
      </div>

      <input
        v-model="userInput"
        @keyup.enter="sendMessage"
        type="text"
        :placeholder="$t('faq_chat.input_placeholder')"
        class="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
  </div>
</template>

<style scoped>
.chat-messages {
  background-color: #f9f9f9;
}
.faq-chat {
  transition: all 0.3s ease;
}
</style>
