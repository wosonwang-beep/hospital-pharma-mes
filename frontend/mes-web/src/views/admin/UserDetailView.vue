<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, errorMessage, idempotencyKey, type Page, type Role, type User } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import {type Department,getUserDepartment} from './departmentModel'
const route=useRoute(); const router=useRouter(); const auth=useAuthStore()
const mode=computed(()=>String(route.meta.mode)); const isCreate=computed(()=>mode.value==='create'); const readonly=computed(()=>mode.value==='view')
const user=ref<User|null>(null); const roles=ref<Role[]>([]); const username=ref(''); const displayName=ref(''); const status=ref<'ACTIVE'|'INACTIVE'>('ACTIVE'); const roleIds=ref<string[]>([])
const temporaryPassword=ref(''); const busy=ref(false); const error=ref(''); const notice=ref('')
const departments=ref<Department[]>([]),departmentId=ref<string|null>(null),originalDepartment=ref<string|null>(null)
async function load(){try{if(!isCreate.value){user.value=await api<User>({url:`/users/${route.params.id}`});username.value=user.value.username;displayName.value=user.value.displayName;status.value=user.value.status;roleIds.value=[...user.value.roleIds]}
if(auth.can('iam:role:view')){const result=await api<Page<Role>>({url:'/roles',params:{page:0,size:100,status:'ACTIVE'}});roles.value=result.items}
if(auth.can('iam:department:view')){
 departments.value=await api<Department[]>({url:'/departments/tree'})
 if(user.value){const association=await getUserDepartment(user.value.id);departmentId.value=association.departmentId;originalDepartment.value=association.departmentId}
}}catch(cause){error.value=errorMessage(cause)}}
async function save(){busy.value=true;error.value='';notice.value='';try{if(isCreate.value){const created=await api<{user:User;temporaryPassword:string}>({method:'POST',url:'/users',headers:{'Idempotency-Key':idempotencyKey()},data:{username:username.value.trim(),displayName:displayName.value.trim(),roleIds:roleIds.value,reason:'IAM user administration'}});user.value=created.user;temporaryPassword.value=created.temporaryPassword;notice.value='用户已创建'
if(auth.can('iam:department:update')&&auth.can('iam:user:update')&&departmentId.value) {
 await api({method:'PUT',url:`/users/${created.user.id}/department`,headers:{'Idempotency-Key':idempotencyKey()},data:{departmentId:departmentId.value}})
 originalDepartment.value=departmentId.value
}}else if(user.value){let updated=await api<User>({method:'PUT',url:`/users/${user.value.id}`,headers:{'Idempotency-Key':idempotencyKey(),'If-Match':`"${user.value.version}"`},data:{displayName:displayName.value.trim(),status:status.value,reason:'IAM user administration'}});if(auth.can('iam:role:view'))updated=await api<User>({method:'POST',url:`/users/${updated.id}/roles`,headers:{'Idempotency-Key':idempotencyKey(),'If-Match':`"${updated.version}"`},data:{roleIds:roleIds.value,reason:'IAM role assignment'}});user.value=updated;notice.value='用户已保存'
if(auth.can('iam:department:update')&&auth.can('iam:user:update')&&departmentId.value!==originalDepartment.value){
 await api({method:'PUT',url:`/users/${updated.id}/department`,headers:{'Idempotency-Key':idempotencyKey()},data:{departmentId:departmentId.value}})
 originalDepartment.value=departmentId.value
}}}catch(cause){error.value=errorMessage(cause)}finally{busy.value=false}}
onMounted(load)
</script>
<template><main :data-ui-template="readonly?'T3':'T2'" class="admin-page"><header class="admin-page-header"><div><a-button type="link" @click="router.push({path:'/admin/users',query:route.query})">← 返回用户列表</a-button><h1>{{isCreate?'新增用户':readonly?'用户详情':'编辑用户'}}</h1></div><a-button v-if="readonly&&auth.can('iam:user:update')&&user" type="primary" @click="router.push({path:`/admin/users/${user.id}/edit`,query:route.query})">编辑</a-button></header>
<a-alert v-if="error" type="error" :message="error" show-icon/><a-alert v-if="notice" type="success" :message="notice" show-icon/>
<a-card v-if="temporaryPassword" title="临时密码（仅显示一次）" class="form-section"><a-alert type="warning" message="请通过院内安全渠道交付，页面关闭后不再显示。" show-icon/><p class="temporary-secret">{{temporaryPassword}}</p><a-button @click="router.push({path:'/admin/users',query:route.query})">返回列表</a-button></a-card>
<a-card v-else title="基本信息" class="form-section"><a-form layout="horizontal" class="detail-form"><a-form-item label="登录账号"><a-input v-model:value="username" :disabled="readonly||!isCreate"/></a-form-item><a-form-item label="员工姓名"><a-input v-model:value="displayName" :disabled="readonly"/></a-form-item><a-form-item label="状态"><a-select show-search option-filter-prop="children" v-model:value="status" :disabled="readonly||isCreate"><a-select-option value="ACTIVE">启用</a-select-option><a-select-option value="INACTIVE">停用</a-select-option></a-select></a-form-item>
<a-form-item label="角色"><a-select show-search option-filter-prop="children" v-model:value="roleIds" mode="multiple" :disabled="readonly||!auth.can('iam:role:view')" placeholder="选择角色"><a-select-option v-for="role in roles" :key="role.id" :value="role.id">{{role.roleName}}（{{role.roleCode}}）</a-select-option></a-select></a-form-item>
<a-form-item v-if="auth.can('iam:department:view')" label="所属部门"><a-select show-search option-filter-prop="label" :options="departments.map(d=>({value:d.id,label:d.name,disabled:d.status!=='ACTIVE'&&d.id!==departmentId}))" v-model:value="departmentId" :disabled="readonly||!auth.can('iam:department:update')||!auth.can('iam:user:update')" allow-clear placeholder="选择所属部门"/></a-form-item>
<a-space v-if="!readonly"><a-button type="primary" :loading="busy" @click="save">保存</a-button><a-button @click="router.push({path:'/admin/users',query:route.query})">取消</a-button></a-space></a-form></a-card></main></template>
