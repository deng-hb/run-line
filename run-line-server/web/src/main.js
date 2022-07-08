import { createApp } from 'vue';
import { createRouter, createWebHashHistory } from 'vue-router'
import Antd from 'ant-design-vue';
import App from './app';

import 'ant-design-vue/dist/antd.css';

import Dashboard from './pages/dashboard';
import Workspace from './pages/workspace';
import Runline from './pages/runline';

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { name: 'dashboard', path: '/', component: Dashboard },
    { name: 'workspace', path: '/workspace/:project', component: Workspace },
    { name: 'runline', path: '/runline/:project', component: Runline },
  ],
})

const app = createApp(App);
app.use(router);
app.use(Antd);
app.mount('#app');
