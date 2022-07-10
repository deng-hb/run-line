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
            <router-link :to="'/runline/' + item.name">Runline</router-link>
            <a-popconfirm
              title="确认要更新?"
              ok-text="Yes"
              cancel-text="No"
              @confirm="onUpd(item)"
            >
              <a href="#"><reload-outlined /></a>
            </a-popconfirm>
            <a-popconfirm
              title="确认要删除?"
              ok-text="Yes"
              cancel-text="No"
              @confirm="onDel(item.name)"
            >
              <a href="#"><delete-outlined /></a>
            </a-popconfirm>
          </template>
        </a-list-item>
      </template>
    </a-list>


    <a-modal
      title="Add Project"
      v-model:visible="addModal.visible"
    >
      <template #footer>
        <a-button key="submit" type="primary" :loading="addModal.loading" @click="onAdd">Submit</a-button>
      </template>
      <a-form ref="formRef" :model="addModal.data" layout="vertical" name="form_in_modal">
        <a-form-item label="Project Name" name="project"
          :rules="[{ required: true }]" >
          <a-input v-model:value="addModal.data.project" />
        </a-form-item>
        <a-form-item label="Git SSH URL" name="url"
          :rules="[{ required: true }]" >
          <a-input v-model:value="addModal.data.url" />
        </a-form-item>
        <a-form-item label="Project Branch" name="branch"
          :rules="[{ required: true}]" >
          <a-input v-model:value="addModal.data.branch" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { message } from 'ant-design-vue';
import { ProjectOutlined, ReloadOutlined, DeleteOutlined } from '@ant-design/icons-vue';

import http from '../http.js'

export default {
  name: 'Dashboard',
  components: {
    ProjectOutlined,
    ReloadOutlined,
    DeleteOutlined,
  },
  data () {
    return {
      projects: [],
      addModal: {
        visible: false,
        loading: false,
        data: {
          url: '',
          project: '',
          branch: '',
          opt: 'add',
        }
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
      this.$refs.formRef.validateFields().then(values => {
        console.log(values);
        console.log(this.addModal.data);

        if (values.url.indexOf('git@') == -1) {
          message.error('请输入正确的地址');
          return
        }
        this.addModal.loading = true
        http.get(`/project`, this.addModal.data).then(res=>{
          console.log(res)
          this.addModal.loading = false
          if ('ok' != res) {
            message.error(res);
          } else {
            this.addModal.visible = false;
            this.loadProject();
          }
        })
      });
    },
    onUpd(item) {
      let project = item.name;
      let branch = item.git.branch;
      message.loading({ content: 'Doing...', key: project, }, 0);
      http.get(`/project`, { opt: 'upd', project, branch }).then(res=>{
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
      message.loading({ content: 'Doing...', key: project }, 0);

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
