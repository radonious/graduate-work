<template>
  <div id="app">
    <!-- Навигационная панель -->
    <Navbar/>
    <!-- Здесь будут отображаться меняющиеся компоненты -->
    <router-view></router-view>
  </div>
</template>

<script>
import Navbar from './components/Navbar.vue';

export default {
  name: 'App',
  components: {
    Navbar
  }
}
</script>