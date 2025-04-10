<template>
  <div class="container col-md-9" v-if="isAuthenticated">
    <hr class="row">
    <footer class="foo d-flex">

      <!-- Левая часть -->
      <div class="footer-left-part" >
        <div class="footer-text">{{ $t("footer.copyright") }}</div>
        <div class="links">
          <router-link to="/about">{{ $t("footer.help") }}</router-link>
          <router-link to="/about">{{ $t("footer.privacy") }}</router-link>
          <router-link to="/about">{{ $t("footer.terms") }}</router-link>
        </div>
      </div>

      <!-- Правая часть -->
      <div class="footer-right-part">
        <div class="links">
          <a href="https://github.com/radonious" target="_blank">
            <img src="../assets/github.svg" alt="Telegram">
          </a>
          <a href="https://vk.com/ne.radon" target="_blank">
            <img src="../assets/vk.svg" alt="Telegram">
          </a>
          <a href="https://t.me/neradon" target="_blank">
            <img src="../assets/tg.svg" alt="Telegram">
          </a>
        </div>
      </div>
    </footer>
  </div>

</template>

<script>
import { authService } from '@/service/authService';

export default {
  name: "Footer",
  data() {
    return {
      selectedLanguage: localStorage.getItem("appLanguage") || "en",
      isAuthenticated: authService.hasRefreshToken()
    };
  },
  watch: {
    '$route': function () {
      this.updateAuthenticationStatus();
    }
  },
  methods: {
    updateAuthenticationStatus() {
      this.isAuthenticated = authService.hasRefreshToken();
    }
  },
  mounted() {
    this.$i18n.locale = this.selectedLanguage;
  },
};
</script>

<style scoped>
.foo {
  display: flex;
  justify-content: space-around;
  padding: 0 0 20px 0;
}

.footer-left-part {
  display: flex;
  align-items: center;
  gap: 15px;
}

.footer-right-part {
  display: flex;
  align-items: center;
  gap: 15px;
}

.links a {
  padding-left: 15px;
  text-decoration: none;
  color: black;
}

.links img {
  opacity: 0.70;
  max-height: 24px;
}
</style>
