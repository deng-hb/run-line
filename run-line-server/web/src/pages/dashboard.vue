<template>
  <div class="layout">
    <a-row type="flex">
      <a-col :flex="auto"><router-link to="/"><h1>Runline</h1></router-link></a-col>
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
            <router-link :to="'/workspace/' + item.name">Edit</router-link>
            <router-link :to="'/runline/' + item.name">Runline</router-link>
            <a-popconfirm
              title="确认要更新?"
              ok-text="Yes"
              cancel-text="No"
              @confirm="onUpd(item.name)"
            >
              <a href="#">Update</a>
            </a-popconfirm>
            <a-popconfirm
              title="确认要删除?"
              ok-text="Yes"
              cancel-text="No"
              @confirm="onDel(item.name)"
            >
              <a href="#">Delete</a>
            </a-popconfirm>
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
        <a-button key="submit" type="primary" :loading="addModal.loading" @click="onAdd">Submit</a-button>
      </template>
      <a-input v-model:value="addModal.project" placeholder="Project Name" />
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
        loading: false,
        project: ''
      }
    }
  },
  mounted () {
    this.loadProject();
  },
  methods: {
    loadProject() {
      http.get('/project', {}).then(res=>{
        console.log(res)
        this.projects = res;
      })
    },
    showAdd() {
      this.addModal.visible = true;
    },
    onAdd() {
      if (this.addModal.remoteUrl.indexOf('git@') == -1) {
        message.error('请输入正确的地址');
        return
      }
      this.addModal.loading = true
      http.get(`/project`, {
        project: this.addModal.project,
        url: this.addModal.remoteUrl,
        opt: 'add',
      }).then(res=>{
        console.log(res)
        this.addModal.loading = false
        if ('ok' != res) {
          message.error(res);
        } else {
          this.addModal.visible = false;
          this.loadProject();
        }
      })
    },
    onUpd(project) {
      message.loading({ content: 'Doing...', key: project });
      http.get(`/project`, { opt: 'upd', project }).then(res=>{
        console.log(res)
        if ('ok' == res) {
          message.success({ content: 'Done!', key: project, duration: 2 });
          this.loadProject();
        } else {
          message.error({ content: res, key: project, duration: 2 });
        }
      })
    },
    onDel(project) {
      message.loading({ content: 'Doing...', key: project });

      http.get(`/project`, { opt: 'del', project }).then(res=>{
        console.log(res)
        if ('ok' == res) {
          message.success({ content: 'Done!', key: project, duration: 2 });
          this.loadProject();
        } else {
          message.error({ content: res, key: project, duration: 2 });
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
