<template>
  <div class="layout">
    <h1>RunLine</h1>
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
            <a key="list-loadmore-edit">edit</a>
            <a key="list-loadmore-more">more</a>
          </template>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script>

import http from '../http.js'

export default {
  name: 'Dashboard',
  data () {
    return {
      projects: []
    }
  },
  mounted () {
    http.get('/projects', {}).then(res=>{
      console.log(res)
      this.projects = res;
    })
  },
  methods: {
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
