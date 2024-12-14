<script setup>
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

defineProps({
  totalPrice: Number
})

const router = useRouter()

const handleLogout = () => {
  localStorage.removeItem('isAuthenticated')
  localStorage.removeItem('username')
  message.success('Вы успешно вышли из системы')
  router.push('/autorization')
}
</script>

<template>
  <header class="flex justify-between border-b border-slate-200 px-10 py-8">
    <router-link to="/">
      <div class="flex items-center gap-4">
        <img src="/aues.jpg" alt="logo" class="w-10" />
        <div>
          <h2 class="text-xl font-bold uppercase">Energo universitry library</h2>
          <p class="text-slate-500">База знаний для исследовательских групп</p>
        </div>
      </div>
    </router-link>

    <ul class="flex items-center gap-10">
      <router-link to="/favorites">
        <li class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black">
          <img src="/heart.svg" alt="Favorites" />
          <span>Избранное</span>
        </li>
      </router-link>

      <router-link to="/downloads">
        <li class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black">
          <img src="/download.svg" alt="Downloads" />
          <span>Скачанное</span>
        </li>
      </router-link>

      <router-link to="/profile">
        <li class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black">
          <img src="/profile.svg" alt="Profile" />
          <b>Профиль</b>
        </li>
      </router-link>

      <li
        @click="handleLogout"
        class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black"
      >
        <img src="/exit.svg" alt="Logout" />
        <span>Выход</span>
      </li>
    </ul>
  </header>
</template>
