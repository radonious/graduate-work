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
    </div>

    <!-- Временный вывод, пока нет она результата -->
    <div class="row">
      <textarea id="res" rows="25"/>
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

// TODO: удалить после поддержки настрйек
const settingsStub = {
  "minFileLength": 10,
  "maxFileLengthDiffRate": 0.25,
  "lexicalPlagiarismThreshold": 0.2,
  "lexicalAnalysisEnable": true,
  "syntaxPlagiarismThreshold": 0.2,
  "syntaxAnalysisEnable": true,
  "saveSourcesIntoDatabase": false,
};

const extensions = [
  basicSetup,
  java(),
  lintGutter(),
  autocompletion(),
  EditorView.lineWrapping
];


export default {
  name: "Home",
  data() {
    return {
      editorView: null,
      currentCode: this.$t("home.snippet.comment"),
      isLoading: false,
      showFileLoader: false,
      textContent: '',
      selectedFile: null,
      isDragging: false,
      fileError: null
    }
  },
  mounted() {
    this.initEditor();
  },
  beforeUnmount() {
    this.editorView?.destroy();
  },
  methods: {
    async handleCheck() {
      this.isLoading = true;

      try {
        const formData = new FormData()
        formData.append('settings', new Blob([JSON.stringify(settingsStub)], {type: 'application/json'}), 'settings.json')

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

        const data = await response.json();
        console.log(data)

        const res = document.getElementById("res");
        res.textContent = JSON.stringify(data, null, 2)

      } catch (error) {
        this.errorMessage = error.message;
        console.error('Ошибка при проверке: ', error);
      } finally {
        this.isLoading = false;
      }
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
  }
}
</script>

<style >
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

#res {
  margin: 25px 0;
}

.home-check-btn {
  max-width: 200px !important;
}

.settings {
  gap: 20px;
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
</style>