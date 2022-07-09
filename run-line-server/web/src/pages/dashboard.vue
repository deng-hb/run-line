<template>
  <div class="layout">
    <a-row type="flex">
      <a-col :flex="auto"><h1>Runline</h1></a-col>
      <a-col flex="100px"><a @click="showAdd">Add</a></a-col>
    </a-row>
    <a-list item-layout="horizontal" :data-source="projects">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta :title="item.name" :description="'[' + item.git.branch + '] ' + item.git.remote">
            <template #avatar>
              <project-outlined />
            </template>
          </a-list-item-meta>
          <template #actions>
            <a key="list-loadmore-pull" @click="onPull(item.name)">pull</a>
            <router-link :to="'/workspace/' + item.name">edit</router-link>
            <router-link :to="'/runline/' + item.name">runline</router-link>
          </template>
        </a-list-item>
      </template>
    </a-list>


    <a-modal
      title="Add Project"
      v-model:visible="addModal.visible"
      @ok="onClone"
    >
      <template #footer>
        <a-button key="submit" type="primary" :loading="addModal.loading" @click="onClone">Clone</a-button>
      </template>
      <a-input v-model:value="addModal.remoteUrl" placeholder="Git SSH URL" />
    </a-modal>
  </div>
</template>

<script>
import { message } from 'ant-design-vue';
import { ProjectOutlined } from '@ant-design/icons-vue';

import http from '../http.js'

export default {
  name: 'Dashboard',
  components: {
    ProjectOutlined,
  },
  data () {
    return {
      projects: [],
      addModal: {
        visible: false,
        remoteUrl: '',
        loading: false
      }
    }
  },
  mounted () {
    http.get('/projects', {}).then(res=>{
      console.log(res)
      this.projects = res;
    })
  },
  methods: {
    onPull(name) {
      message.loading({ content: 'Pulling...', key: name });
      http.get(`/git/pull/${name}`).then(res=>{
        console.log(res)
        if ('ok' == res) {
          message.success({ content: 'Pulled!', key: name, duration: 2 });
        } else {
          message.error({ content: res, key: name, duration: 2 });
        }
      })
    },
    showAdd() {
      this.addModal.visible = true;
    },
    onClone() {
      if (this.addModal.remoteUrl.indexOf('git@') == -1) {
        message.error('请输入正确的地址');
        return
      }
      this.addModal.loading = true
      http.get(`/git/clone/${this.addModal.remoteUrl}`).then(res=>{
        console.log(res)
        this.addModal.loading = false
        if ('ok' != res) {
          message.error(res);
        } else {
          this.addModal.visible = false;
        }
      })
    },
  }
}
</script>

<style scoped>
  .layout{
    width: 1200px;
    height: 100%;
    margin: 0 auto;
  }
</style>
