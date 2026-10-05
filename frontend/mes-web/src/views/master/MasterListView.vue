<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, errorMessage, type Page } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import { resource, statusLabel, type MasterRecord } from '../../master/resources'
const route=useRoute(),router=useRouter(),auth=useAuthStore()
const def=computed(()=>resource(String(route.meta.resource)))
const data=ref<Page<MasterRecord>>({items:[],total:0,page:0,size:20}),busy=ref(false),error=ref('')
const query=reactive<Record<string,string>>({}),keyword=ref(''),sort=ref('id,desc')
const columns=computed(()=>[{title:'ID',dataIndex:'id'},...def.value.fields.map(f=>({title:f.label,dataIndex:f.key,sorter: f.kind!=='date' && !f.key.endsWith('Id') && f.key!=='baseUnitName'})),...(def.value.key==='units'||def.value.key==='unit-conversions'?[]:[{title:'状态',dataIndex:def.value.key==='suppliers'?'qualificationStatus':'status',sorter:true}]),{title:'更新时间',dataIndex:'updatedAt',sorter:true},{title:'操作',key:'actions'}])
async function load(page=0){busy.value=true;error.value='';try{
 const params={keyword:keyword.value||undefined,...Object.fromEntries(Object.entries(query).filter(([,v])=>v)),page,size:20,sort:sort.value}
 await router.replace({query:Object.fromEntries(Object.entries(params).map(([k,v])=>[k,v===undefined?undefined:String(v)]))})
 data.value=await api<Page<MasterRecord>>({url:`/${def.value.key}`,params})
}catch(e){error.value=errorMessage(e)}finally{busy.value=false}}
function reset(){keyword.value='';for(const k of Object.keys(query))query[k]='';sort.value='id,desc';void load(0)}
function sorted(_p:unknown,_f:unknown,s:{field?:string;order?:string}){sort.value=s.field&&s.order?`${s.field},${s.order==='ascend'?'asc':'desc'}`:'id,desc';void load(0)}
function open(id?:string,edit=false){void router.push({path:`/master/${def.value.key}${id?`/${id}${edit?'/edit':''}`:'/create'}`,query:route.query})}
function display(value:unknown){if(value===null||value===undefined||value==='')return '—';if(typeof value==='string'&&statusLabel[value])return statusLabel[value];return String(value)}
watch(()=>route.meta.resource,()=>{keyword.value=String(route.query.keyword??'');sort.value=String(route.query.sort??'id,desc');for(const k of Object.keys(query))delete query[k];for(const f of def.value.filters)query[f.key]=String(route.query[f.key]??'');void load(Number(route.query.page??0))},{immediate:true})
</script>
<!-- UI Template: T1 Query/List; Phase 2A pilot applies only to Material. -->
<template><main class="admin-page master-page" :class="{'material-query-page':def.key==='materials','t1-query-list':def.key==='materials'}"><header class="admin-page-header"><div><h1>{{def.title}}</h1><p>受控主数据 · 查询条件随页面保留</p></div><a-button v-if="auth.can(`master:${def.permission}:create`)" type="primary" @click="open()">＋ 新增</a-button></header>
<a-card title="查询条件" class="query-card"><form class="query-form" @submit.prevent="load(0)"><label><span :class="{'form-field-label':def.key==='materials'}">编码 / 关键字</span><a-input v-model:value="keyword" allow-clear placeholder="请输入" /></label>
<label v-for="field in def.filters" :key="field.key"><span :class="{'form-field-label':def.key==='materials'}">{{field.label}}</span><a-select v-if="field.kind==='select'" v-model:value="query[field.key]" allow-clear style="min-width:170px"><a-select-option v-for="v in field.choices" :key="v" :value="v">{{statusLabel[v]??v}}</a-select-option></a-select><input v-else-if="field.kind==='date'" v-model="query[field.key]" type="date" class="master-native-input"/><a-input v-else v-model:value="query[field.key]" allow-clear/></label>
<a-space><a-button @click="reset">重置</a-button><a-button type="primary" html-type="submit">查询</a-button></a-space></form></a-card>
<a-alert v-if="error" type="error" :message="error" show-icon/>
<a-card title="数据列表" class="result-card"><div class="master-toolbar"><a-space><a-button v-if="def.permission==='uom'&&def.key==='units'" @click="router.push('/master/unit-conversions')">单位换算</a-button><a-button v-if="def.key==='equipment'&&auth.can('master:qualification:view')" @click="router.push('/master/qualifications')">人员资格</a-button></a-space><span>共 {{data.total}} 条记录</span></div>
<a-table :columns="columns" :data-source="data.items" row-key="id" :loading="busy" :pagination="false" :scroll="{x:900}" @change="sorted"><template #bodyCell="{column,record}"><template v-if="column.key==='actions'"><a-button type="link" @click="open(record.id)">查看</a-button><a-button v-if="auth.can(`master:${def.permission}:update`)&&record.allowedActions.includes('UPDATE')" type="link" @click="open(record.id,true)">编辑</a-button></template><a-tag v-else-if="column.dataIndex==='status'||column.dataIndex==='qualificationStatus'" :color="record.status==='ACTIVE'?'success':record.status==='MAINTENANCE'?'warning':'default'">{{display(record[column.dataIndex])}}</a-tag><strong v-else-if="def.key==='materials'&&column.dataIndex==='materialCode'" class="t1-business-id">{{display(record[column.dataIndex])}}</strong><template v-else>{{display(record[column.dataIndex])}}</template></template></a-table>
<div class="table-footer"><span>第 {{data.page+1}} 页</span><a-pagination :current="data.page+1" :page-size="data.size" :total="data.total" :show-size-changer="false" @change="(p:number)=>load(p-1)"/></div></a-card></main></template>
