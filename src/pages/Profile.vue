<template>
  <div class="profile-container">
    <h2 class="text-3xl font-bold mb-8">Мой Профиль</h2>
    <h3 class="text-xl">Персональные данные</h3>

    <div v-if="loading" class="text-center py-8">Загрузка данных...</div>

    <div v-else>
      <div class="grid grid-cols-3 gap-10 mt-10">
        <div class="flex flex-col">
          <label class="mb-2">Имя</label>
          <input
            v-model="profileData.first_name"
            placeholder="Имя"
            type="text"
            class="p-2 border rounded"
          />
        </div>

        <div class="flex flex-col">
          <label class="mb-2">Фамилия</label>
          <input
            v-model="profileData.last_name"
            placeholder="Фамилия"
            type="text"
            class="p-2 border rounded"
          />
        </div>

        <div class="flex flex-col">
          <label class="mb-2">Отчество</label>
          <input
            v-model="profileData.middle_name"
            placeholder="Отчество"
            type="text"
            class="p-2 border rounded"
          />
        </div>

        <div class="flex flex-col">
          <label class="mb-2">Дата рождения</label>
          <input
            v-model="profileData.birth_date"
            placeholder="Дата рождения"
            type="date"
            class="p-2 border rounded"
          />
        </div>

        <div class="flex flex-col">
          <label class="mb-2">Телефон</label>
          <input
            v-model="profileData.phone"
            placeholder="Телефон"
            type="text"
            class="p-2 border rounded"
          />
        </div>

        <div class="flex flex-col">
          <label class="mb-2">Email</label>
          <input
            v-model="profileData.email"
            placeholder="Email"
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
          {{ updating ? 'Сохранение...' : 'Сохранить изменения' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

export default {
  setup() {
    const router = useRouter()
    const profileData = ref({
      first_name: '',
      last_name: '',
      middle_name: '',
      birth_date: '',
      phone: '',
      email: ''
    })
    const loading = ref(true)
    const updating = ref(false)

    const loadUserProfile = async () => {
      try {
        const userId = localStorage.getItem('userId')
        console.log('ID пользователя из localStorage:', userId)

        if (!userId) {
          message.error('Необходима авторизация')
          router.push('/login')
          return
        }

        // Сначала пробуем загрузить из localStorage
        const savedUserData = localStorage.getItem('userData')
        if (savedUserData) {
          profileData.value = JSON.parse(savedUserData)
        }

        loading.value = true
        console.log('Отправляем запрос на:', `http://localhost:5000/api/profile/${userId}`)
        const response = await axios.get(`http://localhost:5000/api/profile/${userId}`)
        console.log('Полученные данные:', response.data)

        profileData.value = response.data
        // Обновляем данные в localStorage
        localStorage.setItem('userData', JSON.stringify(response.data))
      } catch (error) {
        console.error('Полная ошибка:', error)
        console.error('Ответ сервера:', error.response)
        if (error.response?.status === 401) {
          message.error('Необходима авторизация')
          router.push('/login')
        } else {
          message.error('Не удалось загрузить данные профиля')
        }
      } finally {
        loading.value = false
      }
    }

    const updateProfile = async () => {
      try {
        const userId = localStorage.getItem('userId')
        if (!userId) {
          message.error('Необходима авторизация')
          router.push('/login')
          return
        }

        updating.value = true
        await axios.put(`http://localhost:5000/api/profile/${userId}`, profileData.value)
        message.success('Профиль успешно обновлен')
        localStorage.setItem('userData', JSON.stringify(profileData.value))
      } catch (error) {
        console.error('Ошибка при обновлении профиля:', error)
        if (error.response?.status === 401) {
          message.error('Необходима авторизация')
          router.push('/login')
        } else {
          message.error('Не удалось обновить профиль')
        }
      } finally {
        updating.value = false
      }
    }

    onMounted(() => {
      loadUserProfile()
    })

    return {
      profileData,
      loading,
      updating,
      updateProfile
    }
  }
}
</script>

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