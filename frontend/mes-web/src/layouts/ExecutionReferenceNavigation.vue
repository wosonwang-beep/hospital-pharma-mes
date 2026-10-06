<script setup lang="ts">
import {computed,ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {HomeOutlined,AppstoreOutlined,InboxOutlined,ExperimentOutlined,SafetyCertificateOutlined,SettingOutlined,ToolOutlined} from '@ant-design/icons-vue'
import {useAuthStore} from '../stores/auth'
import {resources} from '../master/resources'
import hospitalDesignMark from '../assets/execution-reference/hospital-design-mark.png'
const auth=useAuthStore(),router=useRouter(),route=useRoute(),open=ref(['production'])
type Item={key:string;title:string;permission:string}
const groups=computed(()=>[
 {key:'master',title:'基础主数据',icon:AppstoreOutlined,items:resources.filter(r=>!['unit-conversions','equipment'].includes(r.key)).map(r=>({key:`master-${r.key}`,title:r.title,permission:`master:${r.permission}:view`})).concat([{key:'process-products',title:'产品管理',permission:'master:product:view'}])},
 {key:'wms',title:'仓储管理',icon:InboxOutlined,items:[{key:'wms-receipts',title:'原辅料收货记录',permission:'wms:receipt:view'},{key:'wms-inventory',title:'库存管理',permission:'wms:inventory:view'},{key:'wms-requests',title:'领料申请',permission:'wms:request:view'},{key:'wms-issues',title:'出库管理',permission:'wms:issue:view'},{key:'wms-returns',title:'退料管理',permission:'wms:issue:view'}]},
 {key:'production',title:'生产管理',icon:ExperimentOutlined,items:[{key:'production-orders-list',title:'生产订单',permission:'production:order:view'},{key:'execution-execution',title:'生产执行',permission:'mes:operation:view'},{key:'production-batches-list',title:'生产批',permission:'production:batch:view'},{key:'process-packages',title:'工艺包管理',permission:'process:package:view'},{key:'trace',title:'追溯查询',permission:'trace:view'}]},
 {key:'quality',title:'质量管理',icon:SafetyCertificateOutlined,items:[{key:'production-plans-list',title:'生产质量计划',permission:'qms:plan:view'},{key:'production-tests-list',title:'生产检验',permission:'qms:test:view'},{key:'qc-specifications',title:'QC质量标准',permission:'qms:specification:view'},{key:'incoming-inspection-requests-list',title:'请验单',permission:'qms:inspection-request:view'},{key:'incoming-sampling-tasks-list',title:'取样记录',permission:'qms:sampling:view'},{key:'incoming-samples-list',title:'样品',permission:'qms:test:view'},{key:'incoming-inspection-tasks-list',title:'检验记录',permission:'qms:test:view'},{key:'incoming-inspection-reports-list',title:'检验报告',permission:'qms:report:view'},{key:'incoming-deviations-list',title:'质量调查',permission:'qms:deviation:view'}]},
 {key:'equipment',title:'设备管理',icon:ToolOutlined,items:resources.filter(r=>r.key==='equipment').map(r=>({key:`master-${r.key}`,title:r.title,permission:`master:${r.permission}:view`}))},
 {key:'system',title:'系统管理',icon:SettingOutlined,items:[{key:'users',title:'用户管理',permission:'iam:user:view'},{key:'roles',title:'角色与权限',permission:'iam:role:view'},{key:'ebr-templates',title:'eBR 模板',permission:'ebr:template:view'},{key:'audit',title:'GMP Audit Trail',permission:'audit:view'},{key:'integration-operations',title:'Integration Operations',permission:'integration:view'}]}
].map(g=>({...g,items:(route.name==='production-batches-view'&&g.key==='production'?g.items.slice().sort((a,b)=>['production-orders-list','production-batches-list','execution-execution','process-packages','trace'].indexOf(a.key)-['production-orders-list','production-batches-list','execution-execution','process-packages','trace'].indexOf(b.key)):g.items).filter((i:Item)=>auth.can(i.permission))})).filter(g=>g.items.length))
function navigate({key}:{key:string}){if(key==='execution-execution')return;void router.push({name:key})}
</script>
<!-- Only regroups existing authorized navigation; no new route or permission. -->
<template><div class="execution-reference-navigation"><a-menu mode="inline" v-model:open-keys="open" :selected-keys="[route.name==='production-batches-view'?'production-batches-list':String(route.name)]" @click="navigate"><a-menu-item key="dashboard"><HomeOutlined/>首页</a-menu-item><a-sub-menu v-for="group in groups" :key="group.key"><template #icon><component :is="group.icon"/></template><template #title>{{group.title}}</template><a-menu-item v-for="item in group.items" :key="item.key">{{item.title}}</a-menu-item></a-sub-menu></a-menu><div class="navigation-footer"><img :src="hospitalDesignMark" alt=""/>GMP · 合规 · 质量 · 安全</div></div></template>
<style scoped>
.execution-reference-navigation{display:none}
@media(min-width:901px){
 .execution-reference-navigation{display:block;padding-top:8px}
 #app .execution-reference-navigation :deep(.ant-menu){background:transparent;border:0;color:#344660;font-size:13px}
 #app .execution-reference-navigation :deep(.ant-menu-item),#app .execution-reference-navigation :deep(.ant-menu-submenu-title){height:32px;line-height:32px;margin:0 8px;width:calc(100% - 16px);padding-left:16px!important;border-radius:5px}
 #app .execution-reference-navigation :deep(.ant-menu-sub .ant-menu-item){height:30px;line-height:30px;padding-left:44px!important;font-size:12px}
 #app .execution-reference-navigation :deep(.ant-menu-submenu-selected>.ant-menu-submenu-title),#app .execution-reference-navigation :deep(.ant-menu-item-selected){background:#e4efff;color:#1677ff;font-weight:600}
 #app .execution-reference-navigation :deep(.ant-menu-sub){background:transparent}
 .navigation-footer{position:absolute;bottom:18px;width:100%;text-align:center;font-size:12px;color:#8896ac}
 .navigation-footer img{display:block;width:128px;height:66px;object-fit:contain;margin:0 auto 8px}
}
</style>
