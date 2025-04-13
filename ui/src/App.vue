<template>
  <!-- Навигационная панель -->
  <Navbar />
  <!-- Отображение текущей страницы по route -->
  <router-view></router-view>
  <Footer />
</template>

<script>
import Navbar from './components/Navbar.vue';
import Footer from "./components/Footer.vue";

export default {
  name: 'App',
  components: {
    Navbar,
    Footer
  }
}
</script>

<style>
* {
  font-family: "Poppins", sans-serif;
}

body {
  justify-self: center;
  width: 75%;
  max-width: 1440px;
  min-width: 700px;
  user-select: auto;
}

a,
button,
select,
option {
  min-width: 90px;
  text-align: center;
  vertical-align: middle;
  cursor: pointer;
}
</style>
