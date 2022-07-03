<template>

<a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" collapsible>
      <div class="logo" />

      <a-directory-tree
          :tree-data="treeData"
          @select="onSelect"
        ></a-directory-tree>
    </a-layout-sider>
    <a-layout>
      <a-layout-header style="background: #fff; padding: 0" />
      <a-layout-content>
          <div id="container" ref="container"></div>
      </a-layout-content>
      <a-layout-footer style="text-align: center">
        Ant Design ©2018 Created by Ant UED
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script>

import * as monaco from 'monaco-editor'
import http from './http.js'

export default {
  name: 'App',
  data () {
    return {
      themeOption: [
        {
          value: 'vs',
          label: '默认'
        },
        {
          value: 'hc-black',
          label: '高亮'
        },
        {
          value: 'vs-dark',
          label: '深色'
        }
      ],
      languageOption: [],
      theme: 'vs',
      language: 'java',
      treeData: [{
        title: 'parent 0',
        key: '0-0',
        children: [{
          title: 'leaf 0-0',
          key: '0-0-0',
          isLeaf: true,
        }, {
          title: 'leaf 0-1',
          key: '0-0-1',
          isLeaf: true,
        }],
      }, {
        title: 'parent 1',
        key: '0-1',
        children: [{
          title: 'leaf 1-0',
          key: '0-1-0',
          isLeaf: true,
        }, {
          title: 'leaf 1-1',
          key: '0-1-1',
          isLeaf: true,
        }],
      }]
    }
  },
  mounted () {
    const self = this
    self.initEditor()
    self.languageOption = monaco.languages.getLanguages()
    http.get('/project/run-line', {}).then(res=>{
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
    initEditor () {
      const self = this
      const domEditor = document.getElementById('container')
      self.monacoEditor = monaco.editor.create(domEditor, {
        value: [
          'function x() {',
          '\tconsole.log("Hello world!");',
          '}'
        ].join('\n'),

        theme: self.theme,
        readOnly: false,
        automaticLayout: true
      })
    },
    themeChange (val) {
      monaco.editor.setTheme(val)
    },
    languageChange (val) {
      monaco.editor.setModelLanguage(this.monacoEditor.getModel(), val)
    },
    onSelect(e) {
      console.log(e)
      let file = e[0];
      if (typeof file == 'string') {
        http.get(`/project${file}`).then(res=>{
          this.monacoEditor.setValue(res.content.join('\n'))
          monaco.editor.setModelLanguage(this.monacoEditor.getModel(), 'java')

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

          this.monacoEditor.deltaDecorations([], data);
        })
      }
    }
  }
}
</script>

<style>
  #container{
    width: calc(100vw - 200px);
    height: calc(100vh - 164px);
    text-align: left;
  }
  .insert {
    background: lightblue;
  }
</style>
