<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api'

const user = JSON.parse(localStorage.getItem('grid-user') || '{}')
const profile = ref({})
const myEvents = ref([])
const myApplications = ref([])
const stats = ref({})
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const profileForm = reactive({ displayName: user.displayName || '', phone: '' })
const showPwd = ref(false)
const showProfile = ref(false)

async function load() {
  try { profile.value = await request.get('/auth/me') } catch {}
  try { stats.value = await request.get('/dashboard/my-stats') } catch {}
  try { myEvents.value = (await request.get('/events')).filter(e => e.ownerUsername === user.username) } catch {}
  try { myApplications.value = (await request.get('/service-applications')).filter(a => a.ownerUsername === user.username) } catch {}
}

async function changePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) { ElMessage.error('两次密码不一致'); return }
  if (passwordForm.newPassword.length < 6) { ElMessage.error('新密码至少6位'); return }
  try {
    await request.put('/auth/password', { oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    ElMessage.success('密码修改成功'); showPwd.value = false
    passwordForm.oldPassword = ''; passwordForm.newPassword = ''; passwordForm.confirmPassword = ''
  } catch (e) { ElMessage.error(e.message) }
}

async function updateProfile() {
  try {
    const updated = await request.put('/auth/profile', profileForm)
    localStorage.setItem('grid-user', JSON.stringify(updated))
    ElMessage.success('资料修改成功'); showProfile.value = false
    await load()
  } catch (e) { ElMessage.error(e.message) }
}

function format(value) { return value?.replace('T', ' ').slice(0, 16) }
onMounted(load)
</script>

<template>
  <div>
    <!-- Stats -->
    <section class="metric-grid" style="grid-template-columns:repeat(4,1fr)">
      <article class="metric"><span>我的事件</span><strong>{{ stats.myEventCount || 0 }}</strong><small>条上报</small></article>
      <article class="metric"><span>处理中事件</span><strong>{{ stats.myPendingEventCount || 0 }}</strong><small>件</small></article>
      <article class="metric"><span>我的申请</span><strong>{{ stats.myApplicationCount || 0 }}</strong><small>条</small></article>
      <article class="metric"><span>待处理申请</span><strong>{{ stats.myPendingApplicationCount || 0 }}</strong><small>条</small></article>
    </section>

    <!-- Profile card -->
    <section class="panel section-gap">
      <div class="panel-head"><h3>个人资料</h3><button class="ghost" @click="showProfile = true">编辑</button></div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ profile.displayName }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ profile.role }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ profile.phone || '未填写' }}</el-descriptions-item>
      </el-descriptions>
      <button class="primary" style="margin-top:16px" @click="showPwd = true">修改密码</button>
    </section>

    <!-- My events -->
    <section class="panel section-gap">
      <div class="panel-head"><h3>我的事件上报</h3><span>{{ myEvents.length }} 条</span></div>
      <el-table :data="myEvents" stripe>
        <el-table-column prop="code" label="编号" width="110" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="gridName" label="网格" width="140" />
        <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === '已办结' ? 'success' : row.status === '处理中' ? '' : 'warning'" size="small">{{ row.status }}</el-tag></template></el-table-column>
        <el-table-column prop="createdAt" label="时间" width="160"><template #default="{ row }">{{ format(row.createdAt) }}</template></el-table-column>
      </el-table>
    </section>

    <!-- My applications -->
    <section class="panel section-gap">
      <div class="panel-head"><h3>我的服务申请</h3><span>{{ myApplications.length }} 条</span></div>
      <el-table :data="myApplications" stripe>
        <el-table-column prop="code" label="编号" width="110" />
        <el-table-column prop="itemName" label="事项" />
        <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === '已办结' ? 'success' : row.status === '已驳回' ? 'danger' : 'warning'" size="small">{{ row.status }}</el-tag></template></el-table-column>
        <el-table-column prop="createdAt" label="时间" width="160"><template #default="{ row }">{{ format(row.createdAt) }}</template></el-table-column>
      </el-table>
    </section>

    <!-- Password dialog -->
    <el-dialog v-model="showPwd" title="修改密码" width="420px">
      <el-form :model="passwordForm" label-width="90px">
        <el-form-item label="旧密码"><el-input v-model="passwordForm.oldPassword" type="password" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="passwordForm.newPassword" type="password" /></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="passwordForm.confirmPassword" type="password" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showPwd = false">取消</el-button><el-button type="primary" @click="changePassword">确认修改</el-button></template>
    </el-dialog>

    <!-- Profile dialog -->
    <el-dialog v-model="showProfile" title="编辑个人资料" width="420px">
      <el-form :model="profileForm" label-width="90px">
        <el-form-item label="姓名"><el-input v-model="profileForm.displayName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="profileForm.phone" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showProfile = false">取消</el-button><el-button type="primary" @click="updateProfile">保存</el-button></template>
    </el-dialog>
  </div>
</template>
