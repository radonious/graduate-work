<template>
  <div class="container col-md-9">
    <!-- Заголовок-->
    <div class="row home-title text-center">
      <p>{{ $t("history.title") }}</p>
    </div>

    <div class="table-container d-flex justify-content-center" v-if="results.length > 0">
      <table class="table">
        <thead>
        <tr>
          <th class="border px-4 py-2">{{ $t("history.date") }}</th>
          <th class="border px-4 py-2">{{ $t("history.time") }}</th>
          <th class="border px-4 py-2">{{ $t("history.type") }}</th>
          <th class="border px-4 py-2">{{ $t("history.plagiarism") }}</th>
          <th class="border px-4 py-2">{{ $t("history.uniqueness") }}</th>
          <th class="border px-4 py-2">{{ $t("history.lexical") }}</th>
          <th class="border px-4 py-2">{{ $t("history.syntax") }}</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in results" :key="item.id">
          <td class="border px-4 py-2">{{ getDate(item.timestamp) }}</td>
          <td class="border px-4 py-2">{{ getTime(item.timestamp) }}</td>
          <td class="border px-4 py-2">{{ item.checkType }}</td>
          <td class="border px-4 py-2">{{ (item.plagiarism * 100).toFixed(1) }}%</td>
          <td class="border px-4 py-2">{{ (item.uniqueness * 100).toFixed(1) }}%</td>
          <td class="border px-4 py-2">{{ item.lexicalEnabled ? $t("history.yes") : $t("history.no") }}</td>
          <td class="border px-4 py-2">{{ item.syntaxEnabled ? $t("history.yes") : $t("history.no") }}</td>
        </tr>
        </tbody>
      </table>
    </div>

  </div>
  <br>
</template>

<script>
import {API_BASE_URL} from "../../config.js";
import {authService} from "../service/authService.js";

export default {
  name: "HistoryView",
  data() {
    return {
      results: [],
      isLoading: true,
      error: null,
    };
  },
  async mounted() {
    try {
      const response = await authService.fetchWithToken(`${API_BASE_URL}/results`, {
        method: 'GET',
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      this.results = (await response.json())
          .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
    } catch (e) {
      this.error = `Ошибка при загрузке: ${e.message}`;
    } finally {
      this.isLoading = false;
    }
  },
  methods: {
    getDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString()
    },
    getTime(dateString) {
      const date = new Date(dateString)
      return date.toLocaleTimeString()
    }
  }
};
</script>

<style scoped>
.home-title p {
  font-size: 20px;
}

table {
  border-collapse: collapse;
}

th, td {
  text-align: center;
}

table th {
  position: sticky;
  top: -1px;
  background: #f5f5f5;
  z-index: 2;
}

.table-container {
  max-height: 75vh;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
