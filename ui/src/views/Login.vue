<template>
    <div class="container col-md-4">
        <div class="login-top row">
            <div class="card-header">
                <h3 class="text-center">{{ $t("login.title") }}</h3>
            </div>
            <div class="card-body">
                <form @submit.prevent="handleLogin">
                    <div class="mb-3">
                        <label for="username" class="form-label">{{ $t("login.username") }}</label>
                        <input type="text" class="form-control" id="username" v-model="username" required>
                    </div>
                    <div class="mb-3">
                        <span class="d-flex justify-content-between">
                            <label for="password" class="form-label">{{ $t("login.password") }}</label>
                            <label @click="showPassword" id="showPassword">{{ $t("login.show") }}</label>
                        </span>
                        <input type="password" class="form-control" id="password" v-model="password" required>

                    </div>
                    <div class="text-center">
                        <button type="submit" class="btn btn-outline-dark rounded-pill login-btn">{{ $t("login.submit") }}</button>
                        <div v-if="errorMessage" class="text-danger mt-2">{{ errorMessage }}</div>
                    </div>
                </form>
            </div>
        </div>
        <hr>
        <div class="login-bottom text-center row">
            <p class="register-text" >{{ $t("login.signup_text") }}</p>
            <RouterLink to="/register"><button class="btn btn-outline-secondary rounded-pill register-btn">{{ $t("login.signup") }}</button></RouterLink>
        </div>
    </div>
</template>

<script>
import {authService} from '../service/authService.js';
import {API_BASE_URL} from '../../config.js';

export default {
    name: 'Login',
    data() {
        return {
            username: '',
            password: '',
            errorMessage: ''
        };
    },
    methods: {
        async handleLogin() {
            this.errorMessage = '';
            try {
                const response = await fetch(`${API_BASE_URL}/auth/login`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username: this.username, password: this.password })
                });
                if (!response.ok) {
                  this.errorMessage = await response.text();
                    return;
                }
                const data = await response.json();
                authService.setTokens(data.accessToken, data.refreshToken, data.role, data.userId, data.username);
                console.log(authService.getTokens())
                this.$router.push('/home');
            } catch (error) {
                this.errorMessage = error.message;
            }
        },
        showPassword() {
          const input = document.getElementById("password");
          const showPass = document.getElementById("showPassword");
          if (input.type === "password") {
                input.type = "text";
                showPass.textContent =  this.$t("login.hide")
            } else {
                input.type = "password";
                showPass.textContent = this.$t("login.show")
            }
        }
    }
}
</script>

<style scoped>
.form-control {
    height: 50px !important;
    font-size: 20px !important;
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
