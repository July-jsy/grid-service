import { createRouter, createWebHistory } from 'vue-router'
import Layout from './views/Layout.vue'
import Login from './views/Login.vue'
import Dashboard from './views/Dashboard.vue'
import GridList from './views/GridList.vue'
import ServiceList from './views/ServiceList.vue'
import EventList from './views/EventList.vue'
import BaseInfo from './views/BaseInfo.vue'
import UserList from './views/UserList.vue'
import NoticeList from './views/NoticeList.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: Layout,
      children: [
        { path: '', component: Dashboard, meta: { title: '数据总览', adminOnly: true } },
        { path: 'grids', component: GridList, meta: { title: '网格管理', adminOnly: true } },
        { path: 'base-info', component: BaseInfo, meta: { title: '基础信息采集', adminOnly: true } },
        { path: 'services', component: ServiceList, meta: { title: '便民服务' } },
        { path: 'events', component: EventList, meta: { title: '事件处置' } },
        { path: 'notices', component: NoticeList, meta: { title: '通知公告' } },
        { path: 'users', component: UserList, meta: { title: '用户管理', adminOnly: true } },
      ],
    },
  ],
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !localStorage.getItem('grid-user')) return '/login'
  const user = JSON.parse(localStorage.getItem('grid-user') || '{}')
  if (to.meta.adminOnly && user.role !== '管理员') return '/services'
})

export default router
