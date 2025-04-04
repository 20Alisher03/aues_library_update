<script setup>
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useLanguageStore } from '@/language' // Импортируем useLanguageStore

const router = useRouter()
const profileData = ref({
  id: null,
  username: '',
  firstName: '',
  lastName: '',
  middleName: '',
  birthDate: '',
  phone: '',
  email: ''
})
const loading = ref(true)
const updating = ref(false)

// Получаем хранилище языка
const languageStore = useLanguageStore()

// Реактивный метод для получения переводов
const $t = computed(() => (key) => {
  const translation = languageStore.translations[key] || key
  console.log(`Translating key "${key}":`, translation) // Логирование для отладки
  return translation
})

const loadUserProfile = async () => {
  try {
    const userId = localStorage.getItem('userId')
    console.log('ID пользователя из localStorage:', userId)

    if (!userId) {
      message.error($t.value('profile.error_not_authorized'))
      router.push('/login')
      return
    }

    const numericUserId = parseInt(userId, 10)
    if (isNaN(numericUserId)) {
      message.error($t.value('profile.invalid_user_id'))
      localStorage.removeItem('userId')
      localStorage.removeItem('isAuthenticated')
      localStorage.removeItem('userData')
      router.push('/login')
      return
    }

    // Сначала пробуем загрузить из localStorage
    const savedUserData = localStorage.getItem('userData')
    if (savedUserData) {
      profileData.value = JSON.parse(savedUserData)
    }

    loading.value = true
    console.log('Отправляем запрос на:', `http://localhost:8080/api/profile/${numericUserId}`)
    const response = await axios.get(`http://localhost:8080/api/profile/${numericUserId}`)
    console.log('Полученные данные:', response.data)

    profileData.value = {
      id: response.data.id,
      username: response.data.username || '',
      firstName: response.data.firstName || '',
      lastName: response.data.lastName || '',
      middleName: response.data.middleName || '',
      birthDate: response.data.birthDate || '',
      phone: response.data.phone || '',
      email: response.data.email || ''
    }
    // Обновляем данные в localStorage
    localStorage.setItem('userData', JSON.stringify(profileData.value))
  } catch (error) {
    console.error('Полная ошибка:', error)
    console.error('Ответ сервера:', error.response)
    if (error.response?.status === 401) {
      message.error($t.value('profile.error_not_authorized'))
      router.push('/login')
    } else if (error.response?.status === 400) {
      message.error($t.value('profile.invalid_user_data'))
      localStorage.removeItem('userId')
      localStorage.removeItem('isAuthenticated')
      localStorage.removeItem('userData')
      router.push('/login')
    } else {
      message.error(
        $t.value('profile.error_loading') + (error.response?.data?.message || error.message)
      )
    }
  } finally {
    loading.value = false
  }
}

const updateProfile = async () => {
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) {
      message.error($t.value('profile.error_not_authorized'))
      router.push('/login')
      return
    }

    const numericUserId = parseInt(userId, 10)
    if (isNaN(numericUserId)) {
      message.error($t.value('profile.invalid_user_id'))
      localStorage.removeItem('userId')
      localStorage.removeItem('isAuthenticated')
      localStorage.removeItem('userData')
      router.push('/login')
      return
    }

    updating.value = true
    await axios.put(`http://localhost:8080/api/profile/${numericUserId}`, {
      firstName: profileData.value.firstName,
      lastName: profileData.value.lastName,
      middleName: profileData.value.middleName,
      phone: profileData.value.phone,
      email: profileData.value.email,
      birthDate: profileData.value.birthDate
    })
    message.success($t.value('profile.update_success'))
    localStorage.setItem('userData', JSON.stringify(profileData.value))
  } catch (error) {
    console.error('Ошибка при обновлении профиля:', error)
    if (error.response?.status === 401) {
      message.error($t.value('profile.error_not_authorized'))
      router.push('/login')
    } else if (error.response?.status === 400) {
      message.error(
        $t.value('profile.invalid_profile_data') + (error.response?.data?.message || error.message)
      )
    } else {
      message.error(
        $t.value('profile.error_updating') + (error.response?.data?.message || error.message)
      )
    }
  } finally {
    updating.value = false
  }
}

onMounted(() => {
  loadUserProfile()
})
</script>

<template>
  <div class="profile-container">
    <h2 class="text-3xl font-bold mb-8">{{ $t('profile.title') }}</h2>
    <h3 class="text-xl">{{ $t('profile.personal_data') }}</h3>

    <div v-if="loading" class="text-center py-8">{{ $t('profile.loading') }}</div>

    <div v-else>
      <div class="grid grid-cols-3 gap-10 mt-10">
        <div class="flex flex-col">
          <label class="mb-2">{{ $t('profile.first_name') }}</label>
          <input
            v-model="profileData.firstName"
            :placeholder="$t('profile.first_name_placeholder')"
            type="text"
            class="p-2 border rounded"
          />
        </div>

        <div class="flex flex-col">
          <label class="mb-2">{{ $t('profile.last_name') }}</label>
          <input
            v-model="profileData.lastName"
            :placeholder="$t('profile.last_name_placeholder')"
            type="text"
            class="p-2 border rounded"
          />
        </div>

        <div class="flex flex-col">
          <label class="mb-2">{{ $t('profile.middle_name') }}</label>
          <input
            v-model="profileData.middleName"
            :placeholder="$t('profile.middle_name_placeholder')"
            type="text"
            class="p-2 border rounded"
          />
        </div>

        <div class="flex flex-col">
          <label class="mb-2">{{ $t('profile.birth_date') }}</label>
          <input
            v-model="profileData.birthDate"
            :placeholder="$t('profile.birth_date_placeholder')"
            type="date"
            class="p-2 border rounded"
          />
        </div>

        <div class="flex flex-col">
          <label class="mb-2">{{ $t('profile.phone') }}</label>
          <input
            v-model="profileData.phone"
            :placeholder="$t('profile.phone_placeholder')"
            type="text"
            class="p-2 border rounded"
          />
        </div>

        <div class="flex flex-col">
          <label class="mb-2">{{ $t('profile.email') }}</label>
          <input
            v-model="profileData.email"
            :placeholder="$t('profile.email_placeholder')"
            type="email"
            class="p-2 border rounded"
          />
        </div>
      </div>

      <div class="flex justify-end mt-6">
        <button
          @click="updateProfile"
          :disabled="updating"
          class="px-4 py-2 bg-blue-500 text-white rounded mr-4 disabled:opacity-50"
        >
          {{ updating ? $t('profile.saving') : $t('profile.save_changes') }}
        </button>
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

.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;

  // Responsive Typography
  h2 {
    font-size: 2.5rem;

    @media screen and (max-width: $breakpoint-lg) {
      font-size: 2rem;
    }

    @media screen and (max-width: $breakpoint-md) {
      font-size: 1.75rem;
      text-align: center;
    }

    @media screen and (max-width: $breakpoint-sm) {
      font-size: 1.5rem;
    }
  }

  h3 {
    font-size: 1.5rem;

    @media screen and (max-width: $breakpoint-md) {
      font-size: 1.25rem;
      text-align: center;
    }
  }

  // Responsive Grid
  .grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 2rem;

    @media screen and (max-width: $breakpoint-xl) {
      gap: 1.5rem;
    }

    @media screen and (max-width: $breakpoint-lg) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media screen and (max-width: $breakpoint-md) {
      grid-template-columns: 1fr;
      gap: 1rem;
    }
  }

  // Responsive Form Elements
  .flex {
    &.flex-col {
      label {
        margin-bottom: 0.5rem;
        font-size: 1rem;

        @media screen and (max-width: $breakpoint-sm) {
          font-size: 0.9rem;
        }
      }

      input {
        padding: 0.75rem;
        border: 1px solid #ddd;
        border-radius: 0.5rem;
        font-size: 1rem;
        transition: all 0.3s ease;

        &:focus {
          outline: none;
          border-color: #4a90e2;
          box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
        }

        @media screen and (max-width: $breakpoint-sm) {
          padding: 0.5rem;
          font-size: 0.9rem;
        }
      }
    }
  }

  // Responsive Button
  .flex.justify-end {
    @media screen and (max-width: $breakpoint-sm) {
      justify-content: center;
    }

    button {
      padding: 0.75rem 1.5rem;
      background-color: #4a90e2;
      color: white;
      border: none;
      border-radius: 0.5rem;
      font-size: 1rem;
      cursor: pointer;
      transition: background-color 0.3s ease;

      &:hover:not(:disabled) {
        background-color: darken(#174880, 10%);
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }

      @media screen and (max-width: $breakpoint-sm) {
        width: 100%;
        padding: 0.75rem;
        font-size: 0.9rem;
      }
    }
  }

  // Ultra Small Screens
  @media screen and (max-width: $breakpoint-xxs) {
    padding: 1rem;

    .grid {
      gap: 0.75rem;
    }

    input {
      font-size: 0.8rem;
      padding: 0.4rem;
    }
  }
}
</style>
