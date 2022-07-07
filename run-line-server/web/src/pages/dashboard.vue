<template>
  <div class="layout">
    <a-row type="flex">
      <a-col :flex="auto"><h1>RunLine</h1></a-col>
      <a-col flex="100px"><a @click="showAdd">Add</a></a-col>
    </a-row>
    <a-list item-layout="horizontal" :data-source="projects">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta :description="'[' + item.git.branch + '] ' + item.git.remote">
            <template #title>
              <a href="https://www.antdv.com/"></a>
              <router-link :to="'/workspace/' + item.name">{{ item.name }}</router-link>
            </template>
            <template #avatar>
              <a-avatar src="https://joeschmoe.io/api/v1/random" />
            </template>
          </a-list-item-meta>
          <template #actions>
            <a key="list-loadmore-pull" @click="pull(item.name)">pull</a>
            <a key="list-loadmore-edit">edit</a>
            <a key="list-loadmore-more">more</a>
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

import http from '../http.js'

export default {
  name: 'Dashboard',
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
    pull(name) {
      message.loading({ content: 'Pulling...', name });
      http.get(`/git/pull/${name}`).then(res=>{
        console.log(res)
        message.success({ content: 'Pulled!', name, duration: 2 });
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
    }
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
