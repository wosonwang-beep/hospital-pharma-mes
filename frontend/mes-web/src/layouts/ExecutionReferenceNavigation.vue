<script setup lang="ts">
import {ref,watch} from 'vue'
import {HomeOutlined,AppstoreOutlined,InboxOutlined,ExperimentOutlined,SafetyCertificateOutlined,SettingOutlined} from '@ant-design/icons-vue'
import type {NavigationGroup,NavigationItem} from './navigation'
import hospitalDesignMark from '../assets/execution-reference/hospital-design-mark.png'
const props=defineProps<{groups:NavigationGroup[];home:NavigationItem;selectedKey?:string}>()
const emit=defineEmits<{navigate:[value:{key:string}]}>()
const icons={master:AppstoreOutlined,wms:InboxOutlined,quality:SafetyCertificateOutlined,production:ExperimentOutlined,finished:InboxOutlined,system:SettingOutlined}
function navigate({key}:{key:string|number}){emit('navigate',{key:String(key)})}
const open=ref<string[]>(['production'])
watch(()=>props.selectedKey,key=>{const group=props.groups.find(g=>g.items.some(i=>i.key===key));if(group&&!open.value.includes(group.key))open.value=[...open.value,group.key]},{immediate:true})
</script>
<!-- Presentational shell only: destinations and permission filtering come from shared navigation. -->
<template><div class="execution-reference-navigation"><a-menu mode="inline" v-model:open-keys="open" :selected-keys="selectedKey?[selectedKey]:[]" @click="navigate"><a-menu-item :key="home.key"><HomeOutlined/>{{home.title}}</a-menu-item><a-sub-menu v-for="group in groups" :key="group.key"><template #icon><component :is="icons[group.key as keyof typeof icons]"/></template><template #title>{{group.title}}</template><a-menu-item v-for="item in group.items" :key="item.key">{{item.title}}</a-menu-item></a-sub-menu></a-menu><div class="navigation-footer"><img :src="hospitalDesignMark" alt=""/>GMP · 合规 · 质量 · 安全</div></div></template>
<style scoped>
.execution-reference-navigation{display:none}
@media(min-width:901px){
 .execution-reference-navigation{display:flex;flex:1;min-height:0;flex-direction:column;padding-top:8px}
 #app .execution-reference-navigation :deep(.ant-menu){flex:1;min-height:0;overflow-y:auto;background:transparent;border:0;color:#344660;font-size:13px}
 #app .execution-reference-navigation :deep(.ant-menu-item),#app .execution-reference-navigation :deep(.ant-menu-submenu-title){height:32px;line-height:32px;margin:0 8px;width:calc(100% - 16px);padding-left:16px!important;border-radius:5px}
 #app .execution-reference-navigation :deep(.ant-menu-sub .ant-menu-item){height:30px;line-height:30px;padding-left:44px!important;font-size:12px}
 #app .execution-reference-navigation :deep(.ant-menu-submenu-selected>.ant-menu-submenu-title),#app .execution-reference-navigation :deep(.ant-menu-item-selected){background:#e4efff;color:#1677ff;font-weight:600}
 #app .execution-reference-navigation :deep(.ant-menu-sub){background:transparent}
 .navigation-footer{flex-shrink:0;padding:16px 0 18px;width:100%;text-align:center;font-size:12px;color:#8896ac}
 .navigation-footer img{display:block;width:128px;height:66px;object-fit:contain;margin:0 auto 8px}
}
</style>
