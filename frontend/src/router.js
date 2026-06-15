import { createRouter, createWebHistory } from 'vue-router'
import Layout from './views/Layout.vue'
import Login from './views/Login.vue'
import Dashboard from './views/Dashboard.vue'
import GridList from './views/GridList.vue'
import ServiceList from './views/ServiceList.vue'
import EventList from './views/EventList.vue'
import BaseInfo from './views/BaseInfo.vue'
import UserList from './views/UserList.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: Layout,
      children: [
        { path: '', component: Dashboard, meta: { title: '数据总览' } },
        { path: 'grids', component: GridList, meta: { title: '网格管理' } },
        { path: 'base-info', component: BaseInfo, meta: { title: '基础信息采集' } },
        { path: 'services', component: ServiceList, meta: { title: '便民服务' } },
        { path: 'events', component: EventList, meta: { title: '事件处置' } },
        { path: 'users', component: UserList, meta: { title: '用户管理' } },
      ],
    },
  ],
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !localStorage.getItem('grid-user')) return '/login'
})

export default router
