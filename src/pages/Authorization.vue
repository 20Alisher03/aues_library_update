<template>
  <div class="login-container">
    <div class="login-card">
      <a-form
        :model="formState"
        name="normal_login"
        class="login-form"
        @finish="onFinish"
        @finishFailed="onFinishFailed"
      >
        <div class="login-title-container">
          <h1 class="login-title">{{ isLogin ? 'Вход' : 'Регистрация' }}</h1>
        </div>

        <a-form-item
          label="Имя пользователя"
          name="username"
          :rules="[{ required: true, message: 'Пожалуйста, введите имя пользователя!' }]"
        >
          <a-input v-model:value="formState.username">
            <template #prefix>
              <user-outlined class="site-form-item-icon" />
            </template>
          </a-input>
        </a-form-item>

        <template v-if="!isLogin">
          <a-form-item
            label="Имя"
            name="first_name"
            :rules="[{ required: true, message: 'Пожалуйста, введите имя!' }]"
          >
            <a-input v-model:value="formState.first_name" />
          </a-form-item>

          <a-form-item
            label="Фамилия"
            name="last_name"
            :rules="[{ required: true, message: 'Пожалуйста, введите фамилию!' }]"
          >
            <a-input v-model:value="formState.last_name" />
          </a-form-item>

          <a-form-item
            label="Отчество"
            name="middle_name"
            :rules="[{ required: true, message: 'Пожалуйста, введите отчество!' }]"
          >
            <a-input v-model:value="formState.middle_name" />
          </a-form-item>

          <a-form-item
            label="Телефон"
            name="phone"
            :rules="[{ required: true, message: 'Пожалуйста, введите телефон!' }]"
          >
            <a-input v-model:value="formState.phone" />
          </a-form-item>

          <a-form-item
            label="Email"
            name="email"
            :rules="[
              { required: true, message: 'Пожалуйста, введите email!' },
              { type: 'email', message: 'Пожалуйста, введите корректный email!' }
            ]"
          >
            <a-input v-model:value="formState.email" />
          </a-form-item>

          <a-form-item
            label="Дата рождения"
            name="birth_date"
            :rules="[{ required: true, message: 'Пожалуйста, введите дату рождения!' }]"
          >
            <a-date-picker v-model:value="formState.birth_date" format="YYYY-MM-DD" />
          </a-form-item>
        </template>

        <a-form-item
          label="Пароль"
          name="password"
          :rules="[{ required: true, message: 'Пожалуйста, введите пароль!' }]"
        >
          <a-input-password v-model:value="formState.password">
            <template #prefix>
              <lock-outlined class="site-form-item-icon" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button
            :disabled="disabled"
            type="primary"
            html-type="submit"
            class="login-form-button"
          >
            {{ isLogin ? 'Войти' : 'Зарегистрироваться' }}
          </a-button>
          <div class="mt-4 text-center">
            <a @click="toggleMode" class="toggle-link">
              {{ isLogin ? 'Зарегистрироваться!' : 'Вернуться ко входу' }}
            </a>
          </div>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref } from 'vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const isLogin = ref(true)

const formState = reactive({
  username: '',
  password: '',
  first_name: '',
  last_name: '',
  middle_name: '',
  phone: '',
  email: '',
  birth_date: null
})

const toggleMode = () => {
  isLogin.value = !isLogin.value
  Object.keys(formState).forEach((key) => {
    formState[key] = ''
  })
}

const onFinish = async (values) => {
  try {
    const endpoint = isLogin.value ? '/api/login' : '/api/register'
    const response = await axios.post(`http://localhost:5000${endpoint}`, {
      ...values,
      birth_date: values.birth_date?.format('YYYY-MM-DD')
    })

    if (response.status === 200 || response.status === 201) {
      localStorage.setItem('isAuthenticated', 'true')
      localStorage.setItem('userId', response.data.user_id)

      if (response.data.user_data) {
        localStorage.setItem('userData', JSON.stringify(response.data.user_data))
      }

      message.success(isLogin.value ? 'Вход выполнен успешно!' : 'Регистрация успешна!')
      router.push('/profile')
    }
  } catch (error) {
    const errorMessage = error.response?.data?.error || 'Произошла ошибка'
    message.error(errorMessage)
  }
}

const onFinishFailed = (errorInfo) => {
  console.log('Failed:', errorInfo)
  message.error('Пожалуйста, проверьте введенные данные!')
}

const disabled = computed(() => {
  if (isLogin.value) {
    return !(formState.username && formState.password)
  }
  return Object.keys(formState).some((key) => !formState[key])
})
</script>

<style lang="scss" scoped>
$gradient-from: #6e8efb;
$gradient-to: #a777e3;

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, $gradient-from, $gradient-to);
  padding: 20px;
}

.login-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  width: 100%;
  padding: 40px;
}

.login-title-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80px;
}

.login-title {
  text-align: center;
  font-weight: bold;
  color: #333;
}

.login-form {
  width: 100%;
}

.login-form-button {
  width: 100%;
  background: linear-gradient(135deg, $gradient-from, $gradient-to);
  border: none;
  color: white;
  font-size: 16px;
  padding: 12px 0;
  border-radius: 8px;
  transition: all 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;

  &:hover {
    background: linear-gradient(135deg, $gradient-to, $gradient-from);
    transform: scale(1.05);
  }
}

.toggle-link {
  color: $gradient-from;
  text-decoration: underline;
  cursor: pointer;
  transition: color 0.3s;

  &:hover {
    color: $gradient-to;
  }
}

// Responsive Breakpoints
@media (min-width: 2560px) {
  .login-card {
    max-width: 800px;
  }
  .login-title {
    font-size: 36px;
  }
}

@media (min-width: 1440px) and (max-width: 2559px) {
  .login-card {
    max-width: 600px;
  }
  .login-title {
    font-size: 32px;
  }
}

@media (min-width: 1024px) and (max-width: 1439px) {
  .login-card {
    max-width: 500px;
  }
  .login-title {
    font-size: 28px;
  }
}

@media (min-width: 786px) and (max-width: 1023px) {
  .login-card {
    max-width: 450px;
    padding: 30px;
  }
  .login-title {
    font-size: 24px;
  }
}

@media (min-width: 425px) and (max-width: 785px) {
  .login-card {
    max-width: 350px;
    padding: 25px;
  }
  .login-title {
    font-size: 22px;
  }
}

@media (min-width: 375px) and (max-width: 424px) {
  .login-card {
    max-width: 300px;
    padding: 20px;
  }
  .login-title {
    font-size: 20px;
  }
}

@media (max-width: 374px) {
  .login-card {
    max-width: 280px;
    padding: 15px;
  }
  .login-title {
    font-size: 18px;
  }
}
</style>