<template>
  <div class="container col-md-9">
    <div class="row justify-content-center">
      <div class="col-md-9">
        <h3 class="profile-title text-center">{{ $t("profile.title") }}</h3>
        <div class="card-body">
          <div class="mb-3">
            <label class="form-label">{{ $t("profile.username") }}:</label>
            <p>{{ username }}</p>
          </div>
          <div class="mb-3">
            <label class="form-label">{{ $t("profile.role") }}:</label>
            <p>{{ userRole }}</p>
          </div>
          <div class="mb-3">
            <label class="form-label">{{ $t("profile.id") }}:</label>
            <p>{{ userId }}</p>
          </div>
          <div class="d-flex justify-content-evenly">
            <button @click="logout" class="btn btn-outline-dark profile-btn">{{ $t("profile.logout") }}</button>
            <button @click="deleteAccount" class="btn btn-outline-dark profile-btn">{{ $t("profile.delete") }}</button>
          </div>
        </div>
      </div>
    </div>
    <br>
  </div>
</template>

<script>
import {authService} from '../service/authService.js';
import {API_BASE_URL} from '../../config.js';

export default {
  name: 'UserProfilePage',
  data() {
    return {
      username: authService.getUsername(),
      userRole: authService.getUserRole(),
      userId: authService.getUserId(),
    };
  },
  methods: {
    async logout() {
      try {
        const response = await fetch(`${API_BASE_URL}/auth/logout`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({username: this.username}),
        });

        if (!response.ok) {
          const errorData = await response.text();
          console.error('Ошибка при выходе из аккаунта:', errorData);
          return;
        }

        authService.clearTokens();
        this.$router.push('/login');
      } catch (error) {
        console.error('Ошибка при выходе из аккаунта:', error);
      }
    },
    async deleteAccount() {
      try {
        const response = await authService.fetchWithToken(`${API_BASE_URL}/users/${this.userId}`, {
          method: 'DELETE',
        });

        if (!response.ok) {
          const errorData = await response.text();
          throw new Error(errorData);
        }

        authService.clearTokens();
        this.$router.push('/');
      } catch (error) {
        console.error('Ошибка при удалении аккаунта:', error);
      }
    },
  },
};
</script>

<style scoped>
.profile-title {
  font-size: 20px;
}

.profile-btn {
  min-width: 150px;
}
</style>
