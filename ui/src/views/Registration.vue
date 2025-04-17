<template>
  <div class="container col-md-4">
    <div class="login-top row">
      <div class="card-header">
        <h3 class="text-center">{{ $t("register.title") }}</h3>
      </div>
      <div class="card-body">
        <form @submit.prevent="handleRegister">
          <div class="mb-3">
            <label for="username" class="form-label">{{ $t("register.username") }}</label>
            <input type="text" class="form-control" id="username" v-model="username" required>
          </div>
          <div class="mb-3">
                        <span class="d-flex justify-content-between">
                            <label for="password" class="form-label">{{ $t("register.password") }}</label>
                            <label @click="showPassword('password')" id="showPassword">{{ $t("register.show") }}</label>
                        </span>
            <input type="password" class="form-control" id="password" v-model="password" required>
          </div>
          <div class="mb-3">
                        <span class="d-flex justify-content-between">
                            <label for="confirmPassword" class="form-label">{{ $t("register.confirm_password") }}</label>
                            <label @click="showPassword('confirmPassword')" id="showConfirmPassword">{{ $t("register.show") }}</label>
                        </span>
            <input type="password" class="form-control" id="confirmPassword" v-model="confirmPassword" required>
          </div>
          <div class="text-center">
            <button type="submit" class="btn btn-outline-dark rounded-pill login-btn">{{ $t("register.submit") }}</button>
            <div v-if="errorMessage" class="text-danger mt-2">{{ errorMessage }}</div>
          </div>
        </form>
      </div>
    </div>
    <hr>
    <div class="login-bottom text-center row">
      <p class="register-text">{{ $t("register.login_text") }}</p>
      <RouterLink to="/login"><button class="btn btn-outline-secondary rounded-pill register-btn">{{ $t("register.login") }}</button></RouterLink>
    </div>
  </div>
</template>

<script>
import {API_BASE_URL} from '../../config.js';

export default {
  name: 'Register',
  data() {
    return {
      username: '',
      password: '',
      confirmPassword: '',
      errorMessage: ''
    };
  },
  methods: {
    async handleRegister() {
      this.errorMessage = '';

      // Проверка совпадения паролей
      if (this.password !== this.confirmPassword) {
        this.errorMessage = this.$t("register.passwords_not_match");
        return;
      }

      try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            username: this.username,
            password: this.password,
            role: "USER"
          })
        });

        if (!response.ok) {
          this.errorMessage =  this.$t("register.error");
          return;
        }

        this.$router.push('/login');

      } catch (error) {
        this.errorMessage =  this.$t("register.error_server");
      }
    },
    showPassword(fieldId) {
      const input = document.getElementById(fieldId);
      const showPass = document.getElementById(`show${fieldId.charAt(0).toUpperCase() + fieldId.slice(1)}`);

      if (input.type === "password") {
        input.type = "text";
        showPass.textContent = this.$t("register.hide");
      } else {
        input.type = "password";
        showPass.textContent = this.$t("register.show");
      }
    }
  }
}
</script>

<style scoped>
.form-control {
  height: 50px;
  font-size: 20px;
}

.login-btn {
  margin: 10px 0;
  width: 90%;
  height: 50px;
}

.register-btn {
  width: 60%;
  height: 50px;
}

.register-text {
  font-size: 20px;
}
</style>
