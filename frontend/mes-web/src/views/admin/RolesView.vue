<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, errorMessage, type Page, type Role } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
const router=useRouter(); const auth=useAuthStore(); const data=ref<Page<Role>>({items:[],total:0,page:0,size:20})
const keyword=ref(''); const status=ref<string>(); const loading=ref(false); const error=ref('')
const columns=[{title:'角色编码',dataIndex:'roleCode'},{title:'角色名称',dataIndex:'roleName'},{title:'权限数',key:'permissions'},{title:'状态',key:'status'},{title:'操作',key:'actions'}]
async function load(page=0){loading.value=true;error.value='';try{data.value=await api<Page<Role>>({url:'/roles',params:{page,size:20,keyword:keyword.value||undefined,status:status.value}})}catch(cause){error.value=errorMessage(cause)}finally{loading.value=false}}
function reset(){keyword.value='';status.value=undefined;void load()} onMounted(()=>load())
</script>
<template><main class="admin-page"><header class="admin-page-header"><div><h1>角色与权限</h1><p>维护角色、权限集合与菜单可见性。</p></div><a-button v-if="auth.can('iam:role:create')" type="primary" @click="router.push('/admin/roles/create')">新增角色</a-button></header>
<a-card title="查询条件" class="query-card"><form class="query-form" @submit.prevent="load()"><label>编码或名称<a-input v-model:value="keyword" allow-clear /></label><label>状态<a-select v-model:value="status" allow-clear style="width:160px"><a-select-option value="ACTIVE">启用</a-select-option><a-select-option value="INACTIVE">停用</a-select-option></a-select></label><a-space><a-button type="primary" html-type="submit">查询</a-button><a-button @click="reset">重置</a-button></a-space></form></a-card>
<a-alert v-if="error" type="error" :message="error" show-icon /><a-card title="角色列表" class="result-card"><a-table :columns="columns" :data-source="data.items" row-key="id" :loading="loading" :pagination="false"><template #bodyCell="{column,record}"><template v-if="column.key==='permissions'">{{record.permissionCodes.length}}</template><template v-else-if="column.key==='status'"><a-tag :color="record.status==='ACTIVE'?'success':'default'">{{record.status==='ACTIVE'?'启用':'停用'}}</a-tag></template><template v-else-if="column.key==='actions'"><a-button type="link" @click="router.push(`/admin/roles/${record.id}`)">查看</a-button><a-button v-if="auth.can('iam:role:update')" type="link" @click="router.push(`/admin/roles/${record.id}/edit`)">编辑</a-button></template></template></a-table><div class="table-footer"><span>共 {{data.total}} 条</span><a-pagination :current="data.page+1" :page-size="data.size" :total="data.total" :show-size-changer="false" @change="(p:number)=>load(p-1)" /></div></a-card></main></template>
