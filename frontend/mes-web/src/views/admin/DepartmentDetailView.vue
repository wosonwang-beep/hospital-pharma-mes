<script setup lang="ts">
import {onMounted,ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,errorMessage} from '../../api/http'
import {type Department,departmentStatus} from './departmentModel'
import {useAuthStore} from '../../stores/auth'
const route=useRoute(),router=useRouter(),auth=useAuthStore()
const value=ref<Department|null>(null),error=ref('')
onMounted(async()=>{try{value.value=await api<Department>({url:'/departments/'+route.params.id})}catch(e){error.value=errorMessage(e)}})
</script>
<template>
 <main class="admin-page department-details" data-ui-template="T3">
  <header class="admin-page-header"><div><a-button type="link" @click="router.push('/admin/departments')">← 返回部门列表</a-button><h1>部门详情</h1></div>
    <a-button v-if="value&&auth.can('iam:department:update')" type="primary" @click="router.push('/admin/departments')">返回部门管理</a-button>
  </header>
  <a-alert v-if="error" type="error" :message="error" show-icon/>
  <a-card v-if="value" class="form-section" title="部门基本信息">
   <div class="dept-details-grid">
    <label><span>部门编码</span><strong>{{value.code}}</strong></label>
    <label><span>部门名称</span><strong>{{value.name}}</strong></label>
    <label><span>上级部门</span><strong>{{value.parentName||'—'}}</strong></label>
    <label><span>所属组织</span><strong>{{value.organizationName}}</strong></label>
    <label><span>负责人</span><strong>{{value.leaderName||'—'}}</strong></label>
    <label><span>联系电话</span><strong>{{value.phone||'—'}}</strong></label>
    <label><span>状态</span><a-tag :color="value.status==='ACTIVE'?'success':'default'">{{departmentStatus[value.status]}}</a-tag></label>
    <label><span>显示排序</span><strong>{{value.sortNo}}</strong></label>
    <label class="dept-full"><span>备注</span><strong>{{value.description||'—'}}</strong></label>
   </div>
  </a-card>
 </main>
</template>
<style scoped>
.dept-details-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:18px;padding:12px 4px}
.dept-details-grid label{display:grid;grid-template-columns:130px minmax(0,1fr);gap:12px;align-items:center}
.dept-details-grid label span{color:#63788e;text-align:right;white-space:nowrap}
.dept-details-grid label strong{font-weight:500;color:#2c3e50}
.dept-details-grid label.dept-full{grid-column:1/-1}
</style>
