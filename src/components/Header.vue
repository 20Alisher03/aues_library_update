<script setup>
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ref, computed } from 'vue' // Импортируем computed
import LanguageSwitcher from './LanguageSwitcher.vue'
import { useLanguageStore } from '@/language'

defineProps({
  totalPrice: Number
})

const router = useRouter()
const isMenuOpen = ref(false)
const languageStore = useLanguageStore()

// Реактивный метод для получения переводов
const $t = computed(() => (key) => {
  return languageStore.translations[key] || key
})

const handleLogout = () => {
  localStorage.removeItem('isAuthenticated')
  localStorage.removeItem('username')
  message.success($t.value('header.logout_success') || 'Вы успешно вышли из системы')
  router.push('/autorization')
  isMenuOpen.value = false
}

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

const closeMenu = () => {
  isMenuOpen.value = false
}
</script>

<template>
  <header class="flex justify-between border-b border-slate-200 px-4 sm:px-10 py-8">
    <router-link to="/" @click="closeMenu">
      <div class="flex items-center gap-4">
        <img src="/aues.jpg" alt="logo" class="w-10" />
        <div>
          <h2 class="text-xl font-bold uppercase">{{ $t('header.library_name') }}</h2>
          <p class="text-slate-500">{{ $t('header.tagline') }}</p>
        </div>
      </div>
    </router-link>

    <!-- Обычное меню для десктопа -->
    <ul class="hidden md:flex items-center gap-10">
      <li>
        <router-link
          to="/favorites"
          class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black"
        >
          <img src="/heart.svg" alt="Favorites" />
          <span>{{ $t('header.favorites') }}</span>
        </router-link>
      </li>

      <li>
        <router-link
          to="/downloads"
          class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black"
        >
          <img src="/download.svg" alt="Downloads" />
          <span>{{ $t('header.downloads') }}</span>
        </router-link>
      </li>

      <li>
        <router-link
          to="/profile"
          class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black"
        >
          <img src="/profile.svg" alt="Profile" />
          <b>{{ $t('header.profile') }}</b>
        </router-link>
      </li>

      <li>
        <LanguageSwitcher />
      </li>

      <li
        @click="handleLogout"
        class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black"
      >
        <img src="/exit.svg" alt="Logout" />
        <span>{{ $t('header.logout') }}</span>
      </li>
    </ul>

    <!-- Бургер-кнопка для мобильной версии -->
    <button class="md:hidden z-50" @click="toggleMenu" aria-label="Toggle menu">
      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          v-if="!isMenuOpen"
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M4 6h16M4 12h16M4 18h16"
        />
        <path
          v-else
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M6 18L18 6M6 6l12 12"
        />
      </svg>
    </button>

    <!-- Мобильное меню -->
    <div
      :class="[
        'md:hidden fixed top-0 right-0 h-full bg-white shadow-lg transform transition-transform duration-300 z-50',
        isMenuOpen ? 'translate-x-0' : 'translate-x-full',
        'w-52 sm:w-64'
      ]"
    >
      <ul class="flex flex-col gap-4 p-4 mt-16">
        <li>
          <router-link
            to="/favorites"
            class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black p-2"
            @click="closeMenu"
          >
            <img src="/heart.svg" alt="Favorites" />
            <span>{{ $t('header.favorites') }}</span>
          </router-link>
        </li>

        <li>
          <router-link
            to="/downloads"
            class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black p-2"
            @click="closeMenu"
          >
            <img src="/download.svg" alt="Downloads" />
            <span>{{ $t('header.downloads') }}</span>
          </router-link>
        </li>

        <li>
          <router-link
            to="/profile"
            class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black p-2"
            @click="closeMenu"
          >
            <img src="/profile.svg" alt="Profile" />
            <b>{{ $t('header.profile') }}</b>
          </router-link>
        </li>

        <li>
          <LanguageSwitcher />
        </li>

        <li
          @click="handleLogout"
          class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black p-2"
        >
          <img src="/exit.svg" alt="Logout" />
          <span>{{ $t('header.logout') }}</span>
        </li>
      </ul>
    </div>

    <!-- Оверлей для мобильного меню -->
    <div
      v-if="isMenuOpen"
      class="md:hidden fixed inset-0 bg-black/20 z-40"
      @click="toggleMenu"
    ></div>
  </header>
</template>

<style scoped>
header {
  position: relative;
}

ul {
  margin: 0;
}

ul li {
  position: relative;
  z-index: 50;
}

@media (max-width: 320px) {
  .w-52 {
    width: 13rem;
  }
}
</style>
