<script setup lang="ts">
import {computed,onMounted,ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {PlusOutlined,SearchOutlined,DownOutlined,RightOutlined,FolderOutlined,FileTextOutlined} from '@ant-design/icons-vue'
import {api,errorMessage,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import {catalogTree,type MenuEntry} from './menuCatalog'
import './menuConfiguration.css'
const auth=useAuthStore(),route=useRoute(),router=useRouter()
const menus=ref<MenuEntry[]>([]),matches=ref(new Set<string>()),loading=ref(false),error=ref(''),search=ref(String(route.query.keyword??'')),keyword=ref(search.value),page=ref(Number(route.query.page)||1),opened=ref(new Set<string>())
async function fetchMenus(query?:string) {const items:MenuEntry[]=[];for(let n=0;;n++){const response=await api<Page<MenuEntry>>({url:'/menus',params:{page:n,size:100,keyword:query||undefined}});items.push(...response.items);if(items.length>=response.total)return items;if(!response.items.length)throw Error('Incomplete menu catalog')}}
async function load(){loading.value=true;error.value='';try{menus.value=await fetchMenus();opened.value=new Set(menus.value.filter(m=>!m.routePath).map(m=>m.id));matches.value=new Set((keyword.value?await fetchMenus(keyword.value):menus.value).map(m=>m.id))}catch(cause){error.value=errorMessage(cause)}finally{loading.value=false}}
const visible=computed(()=>{const result:{menu:MenuEntry;depth:number}[]=[];function walk(nodes:ReturnType<typeof catalogTree>,depth:number){for(const node of nodes){const included=(n:typeof node):boolean=>matches.value.has(n.key)||n.children.some(included);if(!included(node))continue;result.push({menu:node.entry,depth});if(keyword.value||opened.value.has(node.key))walk(node.children,depth+1)}}walk(catalogTree(menus.value),0);return result})
const pageRows=computed(()=>visible.value.slice((page.value-1)*12,page.value*12))
async function query(reset=false){keyword.value=reset?'':search.value.trim();if(reset)search.value='';page.value=1;await router.replace({query:{keyword:keyword.value||undefined}});await load()}
function expand(id:string){const next=new Set(opened.value);next.has(id)?next.delete(id):next.add(id);opened.value=next;page.value=1}
function enter(menu:MenuEntry){router.push({path:`/admin/menus/${menu.id}${auth.can('iam:menu:update')?'/edit':''}`,query:{keyword:keyword.value||undefined,page:page.value}})}
onMounted(load)
</script>
<template><main data-ui-template="T1" class="menu-prototype">
<header class="menu-page-title"><h1>菜单管理</h1><a-button v-if="auth.can('iam:menu:create')" type="primary" aria-label="新增菜单" @click="router.push('/admin/menus/create')"><PlusOutlined aria-hidden="true"/>新增菜单</a-button></header>
<a-alert v-if="error" type="error" :message="error" show-icon/>
<section class="menu-panel menu-query"><h2>查询条件</h2><form class="menu-query-form" @submit.prevent="query()"><label class="menu-pair"><span class="form-field-label">名称/编码/地址</span><a-input v-model:value="search" aria-label="菜单关键词" allow-clear placeholder="搜索菜单"/></label><a-button type="primary" html-type="submit" :loading="loading" aria-label="查询"><SearchOutlined aria-hidden="true"/>查询</a-button><a-button aria-label="重置" :disabled="loading" @click="query(true)">重置</a-button></form></section>
<section class="menu-panel menu-result"><div class="menu-panel-header"><h2>菜单列表</h2><a-space><a-button @click="opened=new Set(menus.filter(m=>!m.routePath).map(m=>m.id));page=1">展开全部</a-button><a-button @click="opened=new Set();page=1">收起全部</a-button></a-space></div>
<a-spin :spinning="loading"><div class="menu-table-scroll"><table class="menu-list-table"><thead><tr><th>菜单名称</th><th>菜单编码</th><th>页面地址</th><th>节点类型</th><th>状态</th><th>操作</th></tr></thead><tbody><tr v-for="{menu,depth} in pageRows" :key="menu.id"><td><div class="menu-tree-cell" :style="{paddingLeft:depth*24+'px'}"><button v-if="!menu.routePath" class="menu-tree-button" :aria-label="(opened.has(menu.id)?'收起':'展开')+menu.menuName" :aria-expanded="opened.has(menu.id)" @click="expand(menu.id)"><DownOutlined v-if="opened.has(menu.id)"/><RightOutlined v-else/></button><span v-else class="menu-tree-space"/><FolderOutlined v-if="!menu.routePath" class="menu-tree-icon"/><FileTextOutlined v-else class="menu-tree-icon"/>{{menu.menuName}}</div></td><td>{{menu.menuCode}}</td><td class="muted">{{menu.routePath||'—'}}</td><td>{{menu.routePath?'页面菜单':'目录'}}</td><td><span :class="menu.status==='ACTIVE'?'menu-green':'menu-gray'">{{menu.status==='ACTIVE'?'● 启用':'● 停用'}}</span></td><td><a-button type="link" @click="enter(menu)">{{auth.can('iam:menu:update')?'编辑与配置':'查看'}}</a-button></td></tr></tbody></table><a-empty v-if="!visible.length&&!loading" description="无匹配菜单"/></div></a-spin>
<div class="menu-pager">共 {{visible.length}} 条 <a-pagination v-model:current="page" :total="visible.length" :page-size="12" :show-size-changer="false"/></div></section>
</main></template>
