<template>
  <div class="layout-1">
    <a-row type="flex">
      <a-col :flex="auto"><router-link to="/"><h1>Runline</h1></router-link></a-col>
    </a-row>
    <a-list item-layout="horizontal" :data-source="files">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta>
            <template #avatar>
              <file-outlined />
            </template>
            <template #title>
              <a @click="showRunline(item.file)">{{item.file}}</a>
            </template>
            <template #description>
              <span v-if="null != item.runline">cover line:{{item.runline.line}} diff:{{item.runline.diff}}<br/></span>
              last commit:{{item.commit.author}} {{item.commit.time}} {{item.commit.message}}
            </template>
          </a-list-item-meta>
        </a-list-item>
      </template>
    </a-list>


     <a-modal
      v-model:visible="visible"
      :title="title"
      width="100%"
      wrap-class-name="full-modal"
    >
      <div id="editor"></div>
      <template #footer></template>
    </a-modal>
  </div>
</template>

<script>
import { message } from 'ant-design-vue';
import { FileOutlined } from '@ant-design/icons-vue';
import * as monaco from 'monaco-editor'

import http from '../http.js'

export default {
  name: 'Runline',
  components: {
    FileOutlined,
  },
  data () {
    return {
      files: [],
      visible: false,
    }
  },
  mounted () {
    console.log(this.$route)
    http.get(`/runline/${this.$route.params.project}`, {}).then(res=>{
      console.log(res)
      this.files = res;
    })
  },
  methods: {
    showRunline(file) {
      this.visible = true;
      this.title = file;
      if (this.editor) {
      this.editor.setValue('loading...')
      }
      http.get(`/runline/${this.$route.params.project}/${file}`).then(res=>{
        console.log(res);
        this.openEditor(res);
      })
    },
    openEditor(res) {

      if (!this.editor) {
        this.editor = monaco.editor.create(document.getElementById(`editor`), {
          value: '',
          theme: 'vs',
          readOnly: true,
          language: 'java',
          automaticLayout: true,
          scrollBeyondLastLine: false,
        })
      }
      this.editor.setValue(res.content.join('\n'))
      
      let runline = res.runline;

      let data = [];
      for (let i in runline) {
        let line = Number(runline[i].split(';')[0])
        data.push({
          range: new monaco.Range(line, 1, line, 1),
          options: {
            isWholeLine: true,
            className: 'insert'
          }
        })
      }
      this.editor.deltaDecorations([], data);
    }
  }
}
</script>

<style>
  .layout-1 {
    width: 1200px;
    height: 100%;
    margin: 0 auto;
  }
  #editor {
    width: 100%;
    height: calc(100vh - 60px);
    text-align: left;
  }
  .insert {
    background: lightblue;
  }
  .full-modal .ant-modal {
    max-width: 100%;
    top: 0;
    padding-bottom: 0;
    margin: 0;
  }
  .full-modal .ant-modal-content {
    display: flex;
    flex-direction: column;
    height: calc(100vh);
  }
  .full-modal .ant-modal-body {
    flex: 1;
    padding: 0
  }
</style>
