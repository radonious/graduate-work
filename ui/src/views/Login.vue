<template>
    <div class="container col-md-5">
        <div class="login-top">
            <div class="card-header">
                <h3 class="text-center">Login</h3>
            </div>
            <div class="card-body">
                <form @submit.prevent="handleLogin">
                    <div class="mb-3">
                        <label for="username" class="form-label">Username</label>
                        <input type="text" class="form-control" id="username" v-model="username" required>
                    </div>
                    <div class="mb-3">
                        <span class="d-flex justify-content-between cursor-pointer">
                            <label for="password" class="form-label">Password</label>
                            <label @click="showPassword" class="cursor-pointer" id="showPassword">Hide</label>
                        </span>
                        <input type="password" class="form-control" id="password" v-model="password" required>

                    </div>
                    <div class="text-center">
                        <button type="submit" class="btn btn-primary">Login</button>
                        <div v-if="errorMessage" class="text-danger mt-2">{{ errorMessage }}</div>
                    </div>
                </form>
            </div>
        </div>
        <hr>
        <div class="login-bottom text-center">
            <p>Don't have an account?</p>
            <RouterLink to="/register"><button class="btn btn-primary">Sign Up</button></RouterLink>
        </div>
    </div>
</template>

<script>
import { authService } from '@/service/authService.js';
import { API_BASE_URL } from '../config.js';

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
                    const errorData = await response.text();
                    this.errorMessage = errorData;
                    return;
                }
                const data = await response.json();
                authService.setTokens(data.accessToken, data.refreshToken, data.role, data.userId, data.username);
                console.log(authService.getTokens())
                this.$router.push('/form-list');
            } catch (error) {
                this.errorMessage = error.message;
            }
        },
        showPassword() {
            var input = document.getElementById("password");
            var showPass = document.getElementById("showPassword");
            if (input.type === "password") {
                input.type = "text";
                showPass.textContent = "Hide"
            } else {
                input.type = "password";
                showPass.textContent = "Show"
            }
        }
    }
}
</script>

<style>
.form-control {
    height: 50px;
    font-size: 20px;
}
</style>
