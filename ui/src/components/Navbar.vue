<template>
    <nav class="navbar">

        <!-- Левая часть -->
        <div class="nav-left-part" v-if="isAuthenticated">
            <!-- Кнопки навигации -->
            <RouterLink to="/home" class="nav-button">{{ $t("navbar.home") }}</RouterLink>
            <RouterLink to="/history" class="nav-button">{{ $t("navbar.history") }}</RouterLink>
            <RouterLink to="/about" class="nav-button">{{ $t("navbar.about") }}</RouterLink>
            <RouterLink to="/profile" class="nav-button">{{ $t("navbar.profile") }}</RouterLink>
        </div>

        <!-- Правая часть -->
        <div class="nav-right-part">
            <!-- Cсмена языка -->
            <select class="language-switcher" v-model="selectedLanguage" @change="changeLanguage">
                <option value="ru">Русский</option>
                <option value="en">English</option>
                <option value="zh">中国人</option>
            </select>
            <!-- Кнопка логина / выхода -->
            <RouterLink class="auth-button" to="/login" @click="logout">
                {{ isAuthenticated ? $t("navbar.logout") : $t("navbar.login") }}
            </RouterLink>

        </div>
    </nav>
</template>

<script>
import { authService } from '@/service/authService';

export default {
    name: "Navbar",
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
        changeLanguage() {
            this.$i18n.locale = this.selectedLanguage;
            localStorage.setItem("appLanguage", this.selectedLanguage);
        },
        updateAuthenticationStatus() {
            this.isAuthenticated = authService.hasRefreshToken();
        },
        logout() {
            authService.clearTokens();
            this.isAuthenticated = false;
        }
    },
    mounted() {
        this.$i18n.locale = this.selectedLanguage;
    },
};
</script>

<style scoped>
.navbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 0px;
    color: white;
}

.nav-left-part {
    display: flex;
    align-items: center;
    gap: 15px;
}

.nav-right-part {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-left: auto;
}

.nav-button {
    color: rgb(39, 39, 39);
    text-decoration: none;
    font-size: 16px;
    padding: 5px 10px;
    border-radius: 5px;
}

.language-switcher {
    padding: 5px;
    border: none
}

.language-switcher:focus {
    outline: 0;
}

.auth-button {
    background: black;
    color: white;
    border: none;
    padding: 5px 15px;
    cursor: pointer;
    border-radius: 5px;
    opacity: 1;
    transition: opacity 0.2s ease-in-out;
    text-decoration: none;
}

.auth-button:hover {
    opacity: 0.7;
}
</style>