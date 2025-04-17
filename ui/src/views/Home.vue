<template>
  <div class="container col-md-9">
    <!-- Заголовок-->
    <div class="row home-title text-center">
      <p>{{ $t("home.title") }}</p>
    </div>

    <!-- Редактор -->
    <div class="block home-code" v-show="!showFileLoader" key="first">
      <div ref="editor" class="editor"></div>
    </div>

    <!-- Файлы -->
    <div class="block rounded home-file-load" v-show="showFileLoader" key="second">
      <div
          class="drop-zone mb-4 p-5 text-center border rounded"
          :class="{ 'drag-active': isDragging, 'border-danger': fileError }"
          @dragover.prevent="handleDragOver"
          @dragleave="handleDragLeave"
          @drop.prevent="handleDrop">
        <img src="https://cdn-icons-png.flaticon.com/512/136/136503.png" width="64" class="mb-2" alt="file">
        <p class="h5">{{ $t("home.files.drag") }}</p>
        <p class="text-muted">{{ $t("home.files.supported") }}</p>
        <p class="text-danger" v-if="fileError">{{ fileError }}</p>
      </div>

      <form @submit.prevent="handleFileSubmit">
        <div class="mb-3">
          <label for="formFile" class="form-label">{{ $t("home.files.manual") }}:</label>
          <input
              class="form-control"
              type="file"
              id="formFile"
              @change="handleFileChange"
              accept=".java,.zip">
        </div>
        <div class="mt-3" v-if="selectedFile && !fileError">
          <div class="alert alert-success">
            <p>{{ $t("home.files.loaded") }}: {{ selectedFile ? selectedFile.name : $t("home.files.nothing") }}</p>
            <p>Размер: {{ formatFileSize(selectedFile.size) }}</p>
          </div>
        </div>
      </form>
    </div>

    <!-- Настройки -->
    <div class="row settings d-flex justify-content-end ">
      <div class="form-switch col-md-auto">
        <input
            class="form-check-input"
            type="checkbox"
            id="blockToggle"
            role="switch"
            v-model="showFileLoader"
        >
        <label class="form-check-label" for="blockToggle">{{ $t("home.settings.load") }}</label>
      </div>

      <button
          class="btn btn-primary home-check-btn col-md-3"
          @click="handleCheck"
          :disabled="isLoading || (showFileLoader && !selectedFile)">
        <span v-if="isLoading" class="spinner-border spinner-border-sm"></span>
        {{ isLoading ? $t("home.settings.progress") : $t("home.settings.check") }}
      </button>

      <div>
        <!-- Настройка профиля -->
        <div class="mb-3">
          <label class="form-label">{{ $t("home.settings.profile.title") }}</label>
          <Tooltip :text="$t('home.tooltip.profile')"/>
          <div class="btn-group" role="group" aria-label="Profile selection">
            <input type="radio"
                   class="btn-check"
                   name="profile"
                   id="profileSpeed"
                   value="speed"
                   v-model="profile"
                   @change="applyProfile"
                   autocomplete="off">
            <label class="btn btn-outline-primary" for="profileSpeed">{{ $t("home.settings.profile.speed") }}</label>

            <input type="radio"
                   class="btn-check"
                   name="profile"
                   id="profileQuality"
                   value="quality"
                   v-model="profile"
                   @change="applyProfile"
                   autocomplete="off">
            <label class="btn btn-outline-primary" for="profileQuality">{{
                $t("home.settings.profile.quality")
              }}</label>

            <input type="radio"
                   class="btn-check"
                   name="profile"
                   id="profileCustom"
                   value="custom"
                   v-model="profile"
                   autocomplete="off">
            <label class="btn btn-outline-primary" for="profileCustom">{{ $t("home.settings.profile.custom") }}</label>
          </div>
        </div>

        <!-- Настройки -->
        <div class="mb-3 d-flex col-md-9">
          <label for="minFileLength" class="form-label mt-2">
            {{ $t("home.settings.minFileLength") }}
          </label>
          <Tooltip class="mt-2" :text="$t('home.tooltip.minFileLength')"/>
          <input type="number" id="minFileLength" class="form-control settings-counter" v-model.number="minFileLength">
        </div>

        <div class="mb-3 d-flex col-md-9">
          <label for="maxFileLengthDiffRate" class="form-label">{{ $t("home.settings.maxFileLengthDiffRate") }}</label>
          <label class="percentage-text">({{ (maxFileLengthDiffRate * 100).toFixed(0) }}%)</label>
          <Tooltip :text="$t('home.tooltip.maxFileLengthDiffRate')"/>
          <input type="range" id="maxFileLengthDiffRate" class="form-range settings-slider"
                 v-model.number="maxFileLengthDiffRate"
                 min="0" max="1" step="0.01">
        </div>

        <div class="mb-3 gap-2 d-flex form-check">
          <input type="checkbox" id="lexicalAnalysisEnable" class="form-check-input" v-model="lexicalAnalysisEnable">
          <label for="lexicalAnalysisEnable" class="form-check-label">
            {{ $t("home.settings.lexicalAnalysisEnable") }}
          </label>
          <Tooltip :text="$t('home.tooltip.lexicalAnalysisEnable')"/>
        </div>

        <div class="mb-3">
          <label for="lexicalPlagiarismThreshold" class="form-label">{{
              $t("home.settings.lexicalPlagiarismThreshold")
            }}</label>
          <label class="percentage-text">({{ (lexicalPlagiarismThreshold * 100).toFixed(0) }}%)</label>
          <Tooltip :text="$t('home.tooltip.lexicalPlagiarismThreshold')"/>
          <input type="range" id="lexicalPlagiarismThreshold" class="form-range settings-slider"
                 v-model.number="lexicalPlagiarismThreshold" min="0" max="1" step="0.01">
        </div>

        <div class="mb-3 gap-2 d-flex form-check">
          <input type="checkbox" id="syntaxAnalysisEnable" class="form-check-input" v-model="syntaxAnalysisEnable">
          <label for="syntaxAnalysisEnable" class="form-check-label">
            {{ $t("home.settings.syntaxAnalysisEnable") }}
          </label>
          <Tooltip :text="$t('home.tooltip.syntaxAnalysisEnable')"/>
        </div>

        <div class="mb-3">
          <label for="syntaxPlagiarismThreshold" class="form-label">{{
              $t("home.settings.syntaxPlagiarismThreshold")
            }}</label>
          <label class="percentage-text">({{ (syntaxPlagiarismThreshold * 100).toFixed(0) }}%)</label>
          <Tooltip :text="$t('home.tooltip.syntaxPlagiarismThreshold')"/>
          <input type="range" id="syntaxPlagiarismThreshold" class="form-range settings-slider"
                 v-model.number="syntaxPlagiarismThreshold" min="0" max="1" step="0.01">
        </div>

        <div class="mb-3 form-check form-switch">
          <input type="checkbox" id="saveSourcesIntoDatabase" class="form-check-input"
                 v-model="saveSourcesIntoDatabase">
          <label for="saveSourcesIntoDatabase" class="form-check-label">
            {{ $t("home.settings.saveSourcesIntoDatabase") }}
          </label>
        </div>
      </div>
    </div>

    <!-- Модальное окно с результатами -->
    <div v-if="showModal" class="modal" @click.self="showModal = false">
      <div class="modal-content">
        <span class="close" @click="showModal = false">&times;</span>

        <h3 class="text-center">Результаты проверки</h3>

        <div v-if="isLoading">
          <br>
          <span v-if="isLoading" class="spinner-border spinner-border-sm"></span>
          Идёт проверка...<br><br>
        </div>

        <div v-else class="result-content">
          <div class="result-item ">
            <span class="label">Дата: </span>
            <span class="value">{{ result.common.date }}</span>
          </div>
          <div class="result-item">
            <span class="label">Время: </span>
            <span class="value">{{ result.common.time.substring(0, result.common.time.indexOf(".")) }}</span>
          </div>
          <div class="result-item">
            <span class="label">Длительность: </span>
            <span class="value">{{ result.common.duration }}</span>
            <span class="label">мс </span>
          </div>
          <div class="result-item">
            <span class="label">Количетсво сравнений: </span>
            <span class="value">{{ result.common.checks }}</span>
          </div>
          <br>
          <div class="result-item">
            <span class="label">Процент уникальности: </span>
            <span class="value">{{ Math.round(result.common.unique * 10000) / 100 }}%</span>
          </div>
          <div class="result-item">
            <span class="label">Процент плагиата: </span>
            <span class="value">{{ Math.round(result.common.plagiarism * 10000) / 100 }}%</span>
          </div>

          <div class="result-item table-container" v-if="susFiles.length > 0">
            <table class="suspicious-table">
              <caption class="table-caption">Подозрительные файлы</caption>
              <thead>
              <tr>
                <th>Файл из базы</th>
                <th>Длина</th>
                <th>Совпадение</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="file in susFiles" :key="file.filename">
                <td class="col-filename" :title="file.filename">{{ file.filename }}</td>
                <td>{{ file.lines }}</td>
                <td>{{ (file.plagiarism * 100).toFixed(1) }} %</td>
              </tr>
              </tbody>
            </table>
          </div>
          <br>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {basicSetup} from 'codemirror';
import {EditorView} from '@codemirror/view';
import {java} from '@codemirror/lang-java';
import {autocompletion} from '@codemirror/autocomplete'
import {lintGutter} from '@codemirror/lint'
import {API_BASE_URL} from "../../config.js";
import {authService} from "../service/authService.js";
import Tooltip from '../components/Tooltip.vue'

const extensions = [
  basicSetup,
  java(),
  lintGutter(),
  autocompletion(),
  EditorView.lineWrapping
];

export default {
  name: "Home",
  components: {
    Tooltip
  },
  data() {
    return {
      // Части страницы
      editorView: null,
      currentCode: this.$t("home.snippet.comment"),
      isLoading: false,
      showFileLoader: false,
      textContent: '',
      selectedFile: null,
      isDragging: false,
      fileError: null,
      result: null,
      showModal: false,
      susFiles: '',
      // Значения настроек
      minFileLength: 0,
      maxFileLengthDiffRate: 0.5,
      lexicalAnalysisEnable: true,
      syntaxAnalysisEnable: true,
      lexicalPlagiarismThreshold: 0.5,
      syntaxPlagiarismThreshold: 0.5,
      saveSourcesIntoDatabase: false,
      profile: "custom",
      // Эталонные значения для профилей
      profilesSettings: {
        speed: {
          minFileLength: 20,
          maxFileLengthDiffRate: 0.3,
          lexicalAnalysisEnable: true,
          syntaxAnalysisEnable: false,
          lexicalPlagiarismThreshold: 0.5,
          syntaxPlagiarismThreshold: 0.5,
        },
        quality: {
          minFileLength: 5,
          maxFileLengthDiffRate: 0.2,
          lexicalAnalysisEnable: true,
          syntaxAnalysisEnable: true,
          lexicalPlagiarismThreshold: 0.75,
          syntaxPlagiarismThreshold: 0.75,
        }
      }
    }
  },
  mounted() {
    this.initEditor();

    // Загрузка сохранённых настроек при монтировании
    if (localStorage.getItem("minFileLength") !== null) {
      this.minFileLength = Number(localStorage.getItem("minFileLength"));
    }
    if (localStorage.getItem("maxFileLengthDiffRate") !== null) {
      this.maxFileLengthDiffRate = Number(localStorage.getItem("maxFileLengthDiffRate"));
    }
    if (localStorage.getItem("lexicalAnalysisEnable") !== null) {
      this.lexicalAnalysisEnable = localStorage.getItem("lexicalAnalysisEnable") === "true";
    }
    if (localStorage.getItem("syntaxAnalysisEnable") !== null) {
      this.syntaxAnalysisEnable = localStorage.getItem("syntaxAnalysisEnable") === "true";
    }
    if (localStorage.getItem("lexicalPlagiarismThreshold") !== null) {
      this.lexicalPlagiarismThreshold = Number(localStorage.getItem("lexicalPlagiarismThreshold"));
    }
    if (localStorage.getItem("syntaxPlagiarismThreshold") !== null) {
      this.syntaxPlagiarismThreshold = Number(localStorage.getItem("syntaxPlagiarismThreshold"));
    }
    if (localStorage.getItem("saveSourcesIntoDatabase") !== null) {
      this.saveSourcesIntoDatabase = localStorage.getItem("saveSourcesIntoDatabase") === "true";
    }
    if (localStorage.getItem("profile") !== null) {
      this.profile = localStorage.getItem("profile");
    }
  },
  beforeUnmount() {
    this.editorView?.destroy();
  },
  methods: {
    async handleCheck() {
      this.isLoading = true
      this.showModal = true
      this.result = null
      this.errorMessage = null
      this.susFiles = ""

      try {
        const settings = {
          "minFileLength": this.minFileLength,
          "maxFileLengthDiffRate": this.maxFileLengthDiffRate,
          "lexicalPlagiarismThreshold": this.lexicalPlagiarismThreshold,
          "syntaxPlagiarismThreshold": this.syntaxPlagiarismThreshold,
          "lexicalAnalysisEnable": this.lexicalAnalysisEnable,
          "syntaxAnalysisEnable": this.syntaxAnalysisEnable,
          "saveSourcesIntoDatabase": this.saveSourcesIntoDatabase,
        };

        const formData = new FormData()
        formData.append('settings', new Blob([JSON.stringify(settings)], {type: 'application/json'}), 'settings.json')

        let endpoint;
        let response;

        if (!this.showFileLoader) {
          // Отправка сниппета кода
          formData.append('snippet', new Blob([this.currentCode], {type: 'text/plain'}), 'snippet.java');
          endpoint = `${API_BASE_URL}/check/snippet`;
        } else {
          // Проверяем, что файл выбран
          if (!this.selectedFile) {
            this.fileError = 'Пожалуйста, выберите файл';
            return;
          }

          formData.append('file', this.selectedFile);

          // Отправка файла или архива
          if (this.selectedFile.name.toLowerCase().endsWith('.java')) {
            endpoint = `${API_BASE_URL}/check/file`;
          } else if (this.selectedFile.name.toLowerCase().endsWith('.zip')) {
            endpoint = `${API_BASE_URL}/check/archive`;
          } else {
            this.fileError = 'Неподдерживаемый тип файла';
            return;
          }
        }

        response = await authService.fetchWithToken(endpoint, {
          method: 'POST',
          body: formData
        })

        if (!response.ok) {
          this.errorMessage = await response.text();
          return;
        }

        this.result = await response.json();
        this.susFiles = this.getSortedSuspiciousFiles(this.result)

        console.log(this.result)

      } finally {
        this.isLoading = false;
      }
    },

    getSortedSuspiciousFiles(data) {
      const list = data.checks.flatMap(check => {
        const lexical = (check.filesSuspectedByLexicalAnalyzer || []).map(f => ({
          filename: f.file.filename,
          plagiarism: f.results.finalScore,
          lines: f.file.lines
        }));
        const syntax = (check.filesSuspectedBySyntaxAnalyzer || []).map(f => ({
          filename: f.file.filename,
          plagiarism: f.results.finalScore,
          lines: f.file.lines
        }));
        return [...lexical, ...syntax];
      });

      // Сортируем: сначала по убыванию plagiarism, затем по убыванию lines
      list.sort((a, b) => {
        if (b.plagiarism !== a.plagiarism) {
          return b.plagiarism - a.plagiarism;
        }
        return b.lines - a.lines;
      });

      return list;
    },

    handleFileChange(event) {
      const file = event.target.files[0]
      this.validateFile(file)
    },

    handleFileSubmit() {
      if (this.selectedFile && !this.fileError) {
        this.resetFileInput()
      }
    },

    handleDragOver() {
      this.isDragging = true
    },

    handleDragLeave() {
      this.isDragging = false
    },

    handleDrop(event) {
      this.isDragging = false
      const file = event.dataTransfer.files[0]
      this.validateFile(file)

      // Для синхронизации с input type="file"
      const dataTransfer = new DataTransfer()
      dataTransfer.items.add(file)
      document.getElementById('formFile').files = dataTransfer.files
    },

    validateFile(file) {
      this.fileError = null

      if (!file) {
        return
      }

      const allowedExtensions = ['.java', '.zip']
      const fileName = file.name.toLowerCase()
      const isValidExtension = allowedExtensions.some(ext => fileName.endsWith(ext))

      if (!isValidExtension) {
        this.fileError = 'Ошибка: разрешены только .java и .zip файлы'
        this.selectedFile = null
        return
      }

      this.selectedFile = file
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },

    resetFileInput() {
      this.selectedFile = null
      this.fileError = null
      document.getElementById('formFile').value = ''
    },

    initEditor() {
      this.editorView = new EditorView({
        doc: this.$t("home.snippet.comment"),
        extensions: [
          extensions,
          EditorView.updateListener.of((update) => {
            if (update.docChanged) {
              this.currentCode = update.state.doc.toString();
              this.$emit('code-change', this.currentCode);
            }
          })
        ],
        parent: this.$refs.editor
      });
    },
    applyProfile() {
      // При выборе профиля speed или quality устанавливаем эталонные значения
      if (this.profile === "speed" || this.profile === "quality") {
        const settings = this.profilesSettings[this.profile];
        this.minFileLength = settings.minFileLength;
        this.maxFileLengthDiffRate = settings.maxFileLengthDiffRate;
        this.lexicalPlagiarismThreshold = settings.lexicalPlagiarismThreshold;
        this.syntaxAnalysisEnable = settings.syntaxAnalysisEnable;
        this.lexicalAnalysisEnable = settings.lexicalAnalysisEnable;
        this.syntaxPlagiarismThreshold = settings.syntaxPlagiarismThreshold;
      }
    },
    checkProfile() {
      // Функция проверяет, совпадают ли текущие настройки с профилями speed/quality или устанавливаем профиль custom.
      if (this.profile !== "custom") {
        const expected = this.profilesSettings[this.profile];
        if (
            this.minFileLength !== expected.minFileLength ||
            this.maxFileLengthDiffRate !== expected.maxFileLengthDiffRate ||
            this.lexicalPlagiarismThreshold !== expected.lexicalPlagiarismThreshold ||
            this.syntaxPlagiarismThreshold !== expected.syntaxPlagiarismThreshold ||
            this.syntaxAnalysisEnable !== expected.syntaxAnalysisEnable ||
            this.lexicalAnalysisEnable !== expected.lexicalAnalysisEnable
        ) {
          this.profile = "custom";
        }
      }
    },
  },
  watch: {
    // Следим за изменениями настроек и вызываем checkProfile
    minFileLength() {
      this.checkProfile();
      localStorage.setItem("minFileLength", this.minFileLength);
    },
    maxFileLengthDiffRate() {
      this.checkProfile();
      localStorage.setItem("maxFileLengthDiffRate", this.maxFileLengthDiffRate);
    },
    lexicalAnalysisEnable(newVal) {
      this.checkProfile();
      localStorage.setItem("lexicalAnalysisEnable", newVal);
    },
    syntaxAnalysisEnable(newVal) {
      this.checkProfile();
      localStorage.setItem("syntaxAnalysisEnable", newVal);
    },
    lexicalPlagiarismThreshold() {
      this.checkProfile();
      localStorage.setItem("lexicalPlagiarismThreshold", this.lexicalPlagiarismThreshold);
    },
    syntaxPlagiarismThreshold() {
      this.checkProfile();
      localStorage.setItem("syntaxPlagiarismThreshold", this.syntaxPlagiarismThreshold);
    },
    saveSourcesIntoDatabase(newVal) {
      localStorage.setItem("saveSourcesIntoDatabase", newVal);
    },
    profile(newVal) {
      localStorage.setItem("profile", newVal);
    }
  },
}
</script>

<style>
.editor {
  height: 410px;
  text-align: left;
}

.cm-editor {
  border: 1px solid #adb5bd;
  height: 100%;
}
</style>

<style scoped>


.home-title p {
  font-size: 20px;
}

.home-code textarea {
  border-radius: 15px;
  border-width: 0;
}

.block {
  height: 430px;
  transition: all 0.3s ease;
}

.drop-zone {
  background-color: #f8f9fa;
  border: 2px dashed #adb5bd;
  cursor: pointer;
  transition: all 0.3s ease;
}

.drop-zone.drag-active {
  background-color: #e9ecef;
  border-color: #0d6efd;
  border-style: solid;
}

.drop-zone.border-danger {
  border-color: #dc3545 !important;
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.home-file-load p {
  margin: 0;
}

.home-check-btn {
  max-width: 200px !important;
}

.settings {
  gap: 20px;
}

.settings label {

}

.settings-slider {
  width: 300px;
  margin-left: 20px;
}

.settings-counter {
  width: 80px;
}

.settings-counter::-webkit-inner-spin-button {
  opacity: 1;
}

.form-switch {
  padding-top: 8px;
}

.form-switch input {
  margin-right: 10px;
  min-width: 40px;
}

.form-control {
  height: 100%;
}

.form-label {
  margin-right: 8px;
}

.modal {
  position: fixed;
  z-index: 1;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: #fefefe;
  padding: 20px;
  border-radius: 8px;
  width: 80%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
}

.close {
  position: absolute;
  right: 15px;
  top: 5px;
  color: #aaa;
  font-size: 28px;
  font-weight: bold;
  cursor: pointer;
}

.close:hover {
  color: black;
}

.error-message {
  color: red;
  padding: 10px;
}

.result-content {
  margin-top: 15px;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background: #f5f5f5;
  padding: 13px;
  border-radius: 4px;
  max-height: 300px;
}

.result-item {
  margin: 10px;
}

.percentage-text {
  min-width: 55px;
}

.modal {
  padding: 30px;
}

.suspicious-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: auto;
}

.suspicious-table th,
.suspicious-table td {
  padding: 8px;
  border: 1px solid #ddd;
  text-align: center;
}

.suspicious-table th {
  position: sticky;
  top: 0;
  background: #f5f5f5;
  z-index: 2;
}

.suspicious-table .col-filename {
  text-align: left;
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suspicious-table th.col-filename {
  background-color: #f5f5f5;
}

.table-container {
  max-height: 330px;
  overflow-y: auto;
  overflow-x: hidden;
}

.table-caption {
  caption-side: top;
  text-align: center;
  font-weight: bold;
  padding: 8px;
  font-size: 1.1em;
}
</style>
