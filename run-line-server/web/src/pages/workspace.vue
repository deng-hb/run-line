<template>
  <div class="layout">
    <div class="layout-header">
    </div>
    <div class="layout-content">
      <div class="layout-content-left">
        <router-link to="/"><h1 style="text-align: center;">RunLine</h1></router-link>
        <a-dropdown :trigger="['contextmenu']">
          <a-directory-tree
            :blockNode="true"

            :tree-data="treeData"
            @select="onTreeSelect"
          ></a-directory-tree>
          <template #overlay>
            <a-menu>
              <a-menu-item key="1">pull</a-menu-item>
              <a-menu-item key="2">checkout</a-menu-item>
              <a-menu-item key="3">fetch</a-menu-item>
            </a-menu>
          </template>
          </a-dropdown>
      </div>

      <div class="layout-content-right">
        <div class="resizable"></div>
        <div class="resize-line"></div>
        <div class="layout-content-right-content">
          <a-tabs v-model:activeKey="tabActiveKey" hide-add type="editable-card" @tabClick="onTabClick" @edit="onTabEdit">
            <a-tab-pane v-for="tab in tabs" :key="tab.key" :tab="tab.title" :closable="tab.closable">
              <div v-show="1 == tabActiveKey">
              Hello World!
              </div>
              <div v-show="1 != tabActiveKey" :id="'editor-' + tab.key" class="editor"></div>
            </a-tab-pane>
          </a-tabs>
        </div>
      </div>
    </div>
  </div>

</template>

<script>

import * as monaco from 'monaco-editor'
import md5 from 'crypto-js/md5'
import http from '../http.js'

export default {
  name: 'Workspace',
  data () {
    return {
      themeOption: ['vs','vs-dark','hc-black'],
      treeData: [],
      tabActiveKey: '1',
      tabs: [{title:'Home', key: '1', closable: false}]
    }
  },
  mounted () {
    console.log(this.$route)
    http.get(`/project/${this.$route.params.project}`, {}).then(res=>{
      console.log(res)
      let i = 0;
      function find(m) {
        let children = [];
        for (let k in m) {
          let v = m[k]
          if (typeof v == 'string') {
            children.push({title: k, key: v})
          } else {
            children.push({title: k, key: i++, children: find(v)})
          }

        }
        return children;
      }
      let tree = [];
      for (let k in res) {
        let v = res[k]
        if (typeof v == 'string') {
          tree.push({title: k, key: v})
        } else {
          tree.push({title: k, key: i++, children: find(v)})
        }
      }
      console.log(tree)
      this.treeData = tree;
    })
  },
  methods: {
    onTabEdit(e) {
      // close
      console.log(e)
      this.tabs = this.tabs.filter(t => t.key !== e);
      this.tabActiveKey = this.tabs[this.tabs.length - 1].key;
      this.editor[e] = null;
    },
    onTabClick(e) {
      console.log(e)
    },
    onTreeSelect(e) {
      console.log(e)
      let file = e[0];
      if (typeof file === 'string') {
        this.loadFile(file);
      }
    
    },
    loadFile(file) {

      let fileName = file.substring(file.lastIndexOf('/') + 1);
      let fileExt = file.substring(file.lastIndexOf('.') + 1);
      let language = fileExt;
      if ('js' == language) {
        language = 'javascript';
      } else if ('md' == language) {
        language = 'markdown';
      } else if ('vue' == language) {
        language = 'html';
      }

      let key = md5(file).toString();
      this.tabActiveKey = key;
      if (this.tabs.filter(t => t.key === key).length == 0) {
        this.tabs.push({title: fileName, key: key});
        
      }
      http.get(`/project${file}`).then(res=>{
        console.log(res);
        this.openEditor(language, key, res);
      })
    },
    openEditor(language, key, res) {
      if (!this.editor) {
        this.editor = {};
      }
      if (null != this.editor[key]) {
        return;
      }
      this.editor[key] = monaco.editor.create(document.getElementById(`editor-${key}`), {
        value: res.content.join('\n'),
        theme: 'vs',
        readOnly: false,
        language: language,
        automaticLayout: true,
        scrollBeyondLastLine: false,
      })
      let runline = res.runline;

      let data = [];
      for (let i in runline) {
        let line = Number(runline[i])
        data.push({
          range: new monaco.Range(line, 1, line, 1),
          options: {
            isWholeLine: true,
            className: 'insert'
          }
        })
      }

      this.editor[key].deltaDecorations([], data);
    }
  }
}
</script>

<style>
  .editor {
    width: 100%;
    height: calc(100vh - 60px);
    text-align: left;
  }
  .insert {
    background: lightblue;
  }
  .layout {
    box-sizing: border-box;
  }
  .layout-header {
    height: 0px;
  }
  .layout-content {
    display: flex;
    width: 100%;
    height: calc(100vh - 0px);
    overflow: hidden;
  }
  .layout-content-left {
    flex: 1;
    height: calc(100vh - 0px);
    overflow: auto;
  }
  .layout-content-right {
    position: relative;
    transform: rotateY(180deg);
  }
  .layout-content-right-content { 
    transform: rotateY(180deg);
    margin: 0;
    height: calc(100vh - 0px);
    position: absolute;
    top: 0;
    right: 5px;
    bottom: 0;
    left: 0;
  }
  .resizable {
    resize: horizontal;
    cursor: ew-resize;
    width: calc(100vw - 200px);
    height: calc(100vh - 110px);
    overflow: scroll;
    border: 1px solid black;
    opacity: 0;
  }
  .resizable::-webkit-scrollbar {
    width: calc(100vh - 100px);
    height: inherit;
  }
  .resize-line {
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    border-left: 1px solid #bbb;
    pointer-events: none;
  }
  .resizable:hover ~ .resize-line,
  .resizable:active ~ .resize-line {
    border-left: 1px dashed skyblue;
  }
</style>
