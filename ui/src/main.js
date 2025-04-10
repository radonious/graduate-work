import { createApp } from 'vue'
import App from './App.vue'

import { createI18n } from 'vue-i18n';
import en from './locales/en.json';
import ru from './locales/ru.json';
import zh from './locales/zh.json';

import router from './router/router';

import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap/dist/js/bootstrap.bundle.js';

const savedLanguage = localStorage.getItem('appLanguage') || 'en';

const i18n = createI18n({
    locale: savedLanguage,
    fallbackLocale: 'ru',
    messages: { en, ru, zh }
});

const app = createApp(App)
app.use(i18n)
app.use(router)
app.mount("#app")
