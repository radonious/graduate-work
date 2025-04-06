<template>
  <div class="container col-md-9">
    <div class="row home-title text-center">
      <p>Anti-Plagiarism Check</p>
    </div>

    <div class=" home-code">
      <div ref="editor" class="editor"></div>
    </div>
    <div class="row home-settings">
      <button class="btn btn-primary" @click="handleCheck">Check</button>
    </div>
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
// import {oneDark} from '@codemirror/theme-one-dark' // TODO: Убрать если не буду добавлять темную тему

// TODO: удалить после поддержки настрйек
const settingsStub = {
  "minFileSize": 10,
  "lexicalPlagiarismThreshold": 0.2,
  "lexicalAnalysisEnable": true,
  "syntaxPlagiarismThreshold": 0.2,
  "syntaxAnalysisEnable": true,
  "checkComments": false,
  "preset": "NORMAL",
  "saveFileInDatabase": false,
};

const extensions = [
  basicSetup,
  java(),
  // oneDark,
  lintGutter(),
  autocompletion(),
  EditorView.lineWrapping
];

const doc = '// Code'

var currentCode = doc;

export default {
  name: "Home",
  mounted() {
    new EditorView({
      doc: doc,
      extensions: [
        extensions,
        EditorView.updateListener.of((update) => {
          if (update.docChanged) {
            currentCode = update.state.doc.toString()
            this.$emit('code-change', update.state.doc.toString());
          }
        })
      ],
      parent: this.$refs.editor
    });
  },
  methods: {
    async handleCheck() {
      try {
        const formData = new FormData();

        console.log(currentCode)

        formData.append('snippet', new Blob([currentCode], {type: 'text/plain'}), 'snippet.java')
        formData.append('settings', new Blob([JSON.stringify(settingsStub)], {type: 'application/json'}), 'settings.json')

        const response = await authService.fetchWithToken(`${API_BASE_URL}/check/snippet`, {
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
      }
    }
  }
}
</script>

<style>
.editor {
  height: 350px;
  text-align: left;
}

.cm-editor {
  height: 100%;
}

.home-title p {
  font-size: 20px;
}

.home-code {
  padding: 15px 0 35px 0;
}

.home-code textarea {
  border-radius: 15px;
  border-width: 0;
}

</style>