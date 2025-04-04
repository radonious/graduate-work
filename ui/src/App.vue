<template>
  <!-- Навигационная панель -->
  <Navbar />
  <!-- Отображение текущей страницы по route -->
  <router-view></router-view>
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

<style>
body {
  justify-self: center;
  width: 75%;
  max-width: 1440px;
  min-width: 700px;
  font-family: 'Poppins', sans-serif;
  user-select: none;
}

a,
input,
button,
select,
option {
  min-width: 90px;
  text-align: center;
  vertical-align: middle;
  cursor: pointer;
}
</style>