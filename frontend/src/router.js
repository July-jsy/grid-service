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
import Profile from './views/Profile.vue'
import MapView from './views/MapView.vue'

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
        { path: 'users', component: UserList, meta: { title: '系统管理', adminOnly: true } },
        { path: 'profile', component: Profile, meta: { title: '个人中心' } },
        { path: 'map', component: MapView, meta: { title: '地图可视化', adminOnly: true } },
      ],
    },
  ],
})

router.beforeEach((to) => {
  // 检查是否有 token（即已登录）
  const stored = sessionStorage.getItem('grid-user')
  if (!stored) {
    if (to.path === '/login') return true
    return '/login'
  }
  const user = JSON.parse(stored)
  if (to.path === '/login') return user.role === '管理员' ? '/' : '/services'
  if (to.meta.adminOnly && user.role !== '管理员') return '/services'
})

export default router
