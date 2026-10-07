<script setup lang="ts">
import {computed,ref,onMounted,onBeforeUnmount} from 'vue'
import {useRouter} from 'vue-router'
import {useAuthStore} from '../../stores/auth'
import {api,type Page} from '../../api/http'
import {queryAuditEvents} from '../../api/audit'
import type {AuditEvent} from '../../types/audit'
import {FileTextOutlined,ExperimentOutlined,WarningOutlined,CodeSandboxOutlined,RightOutlined,FileAddOutlined,ShoppingCartOutlined,HomeOutlined,CarOutlined,CalendarOutlined,NotificationOutlined,MedicineBoxOutlined} from '@ant-design/icons-vue'
import * as echarts from 'echarts/core'
import {LineChart,BarChart,PieChart} from 'echarts/charts'
import {GridComponent,TooltipComponent,GraphicComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import {dailyCounts,completeRows} from './dashboardReadModel'
echarts.use([LineChart,BarChart,PieChart,GridComponent,TooltipComponent,GraphicComponent,CanvasRenderer])
const router=useRouter(),auth=useAuthStore()
const now=ref(new Date()),activity=ref<AuditEvent[]>([]),activityError=ref(''),loadError=ref(''),loading=ref(true)
const batches=ref<Record<string,unknown>[]|null>(null),samples=ref<Record<string,unknown>[]|null>(null),investigations=ref<Record<string,unknown>[]|null>(null)
const trendNode=ref<HTMLDivElement>(),qualityNode=ref<HTMLDivElement>(),inventoryNode=ref<HTMLDivElement>()
const charts:echarts.ECharts[]=[]
const greeting=computed(()=>now.value.getHours()<12?'上午好':now.value.getHours()<18?'下午好':'晚上好')
const dateLabel=computed(()=>new Intl.DateTimeFormat('zh-CN',{year:'numeric',month:'long',day:'numeric',weekday:'long'}).format(now.value))
const actions=computed(()=>[
 {title:'新建生产批次',path:'/production/batches/create',icon:FileAddOutlined},
 {title:'领料申请',path:'/wms/requests/create',icon:ShoppingCartOutlined},
 {title:'出库单',path:'/wms/issues/create',icon:CodeSandboxOutlined},
 {title:'请验单',path:'/quality/inspection-requests/create',icon:ExperimentOutlined},
 {title:'成品入库',path:'/finished/inbound/create',icon:HomeOutlined},
 {title:'发货出库',path:'/finished/shipments/create',icon:CarOutlined}
].map(item=>({...item,enabled:canOpen(item.path)})))
const metrics=computed(()=>[
 {label:'待处理任务',value:null,icon:FileTextOutlined,tone:'blue',path:null,help:'当前服务未提供统一个人待办统计'},
 {label:'在生产批次',value:batches.value?.filter(row=>row.status==='IN_PRODUCTION').length??null,icon:MedicineBoxOutlined,tone:'blue',path:'/production/batches',help:'当前可见生产批中状态为生产中的批次'},
 {label:'待检样品',value:samples.value?.filter(row=>['CREATED','RECEIVED'].includes(String(row.status))).length??null,icon:ExperimentOutlined,tone:'blue',path:'/quality/samples',help:'当前可见来料样品中已创建或已接收的样品'},
 {label:'库存预警',value:null,icon:CodeSandboxOutlined,tone:'orange',path:'/wms/inventory',help:'当前服务未提供库存预警统计'},
 {label:'质量事件',value:investigations.value?.length??null,icon:WarningOutlined,tone:'red',path:'/deviations',help:'当前账号可见的来料质量调查记录数量'}
])
function canOpen(path:string|null){if(!path)return false;const p=router.resolve(path).meta.permission;return typeof p==='string'&&auth.can(p)}
async function read(url:string,permission:string,target:typeof batches){if(!auth.can(permission))return;try{target.value=completeRows(await api<Page<Record<string,unknown>>>({url,params:{page:0,size:100,...(url==='/deviations'?{investigationScope:'INCOMING_MATERIAL'}:{})}}))}catch{loadError.value='部分业务数据暂时无法加载，请刷新重试'}}
function chart(node:HTMLDivElement|undefined,rows:Record<string,unknown>[]|null,color:string,kind:'line'|'bar'){
 if(!node)return
 const instance=echarts.init(node);charts.push(instance)
 const trend=rows===null?null:dailyCounts(rows,now.value)
 instance.setOption({animation:false,tooltip:{trigger:'axis'},grid:{left:30,right:12,top:24,bottom:26},xAxis:{type:'category',boundaryGap:kind==='bar',data:trend?.map(day=>day.label)??[],axisLine:{lineStyle:{color:'#dbe7f7'}},axisTick:{show:false},axisLabel:{color:'#607aa9',fontSize:11}},yAxis:{type:'value',minInterval:1,axisLabel:{color:'#607aa9',fontSize:11},splitLine:{lineStyle:{color:'#eef3fa'}}},series:trend?[{type:kind,data:trend.map(day=>day.count),smooth:kind==='line',symbolSize:6,itemStyle:{color},lineStyle:{color,width:2},areaStyle:kind==='line'?{color:'#e5f0ff'}:undefined,barMaxWidth:14}]:[],graphic:!rows?.length?[{type:'text',left:'center',top:'42%',style:{text:rows===null?'暂无可用统计数据':'暂无业务记录',fill:'#8094b2',fontSize:13}}]:[]})
}
function resize(){charts.forEach(c=>c.resize())}
const clock=window.setInterval(()=>{now.value=new Date()},60000)
onMounted(async()=>{
 await Promise.all([read('/main-batches','production:batch:view',batches),read('/quality/samples','qms:test:view',samples),read('/deviations','qms:deviation:view',investigations),(async()=>{if(auth.can('audit:view'))try{activity.value=(await queryAuditEvents({page:0,size:5})).items}catch{activityError.value='近期受控活动暂时无法加载'}})()])
 loading.value=false
 chart(trendNode.value,batches.value,'#1677ff','line');chart(qualityNode.value,investigations.value,'#4d98ff','bar')
 if(inventoryNode.value){const c=echarts.init(inventoryNode.value);charts.push(c);c.setOption({animation:false,series:[{type:'pie',radius:['65%','84%'],center:['50%','50%'],silent:true,label:{show:false},data:[{value:1,itemStyle:{color:'#edf3fb'}}]}],graphic:[{type:'text',left:'center',top:'40%',style:{text:'—',fill:'#14285c',fontSize:26,fontWeight:600}},{type:'text',left:'center',top:'61%',style:{text:'暂无统计数据',fill:'#7186a9',fontSize:12}}]})}
 window.addEventListener('resize',resize)
})
onBeforeUnmount(()=>{clearInterval(clock);window.removeEventListener('resize',resize);charts.forEach(c=>c.dispose())})
</script>
<template>
 <main class="dashboard-reference" data-ui-template="Specialized" :aria-busy="loading">
  <header class="dashboard-greeting"><div><h1>{{greeting}}，{{auth.identity?.displayName??'您好'}}</h1><p>专注质量 · 保障安全 · 造福患者</p></div><div class="dashboard-date"><strong>{{dateLabel}}</strong><span>精制良药 · 守护健康</span></div></header>
  <a-alert v-if="loadError" :message="loadError" type="warning" show-icon/>
  <section class="dashboard-metrics" aria-label="业务概况"><button v-for="m in metrics" :key="m.label" :disabled="!canOpen(m.path)" :title="m.help" @click="m.path&&router.push(m.path)"><span class="dashboard-metric-icon" :class="m.tone"><component :is="m.icon"/></span><span class="dashboard-metric-copy"><span>{{m.label}}</span><strong>{{loading?'—':m.value??'—'}}</strong></span><RightOutlined/></button></section>
  <div class="dashboard-columns"><section class="dashboard-left">
   <section class="dashboard-panel dashboard-shortcuts"><h2>快捷操作</h2><div class="dashboard-shortcut-grid"><button v-for="item in actions" :key="item.path" :disabled="!item.enabled" @click="router.push(item.path)"><component :is="item.icon"/><span>{{item.title}}</span></button></div></section>
   <section class="dashboard-panel dashboard-todos"><header><h2><MedicineBoxOutlined/>我的待办</h2></header><div class="dashboard-table-wrap"><table><thead><tr><th>任务类型</th><th>单据编号</th><th>标题</th><th>当前节点</th><th>到期时间</th><th>操作</th></tr></thead><tbody><tr><td colspan="6" class="dashboard-empty">暂无可用的个人待办数据</td></tr></tbody></table></div></section>
   <section class="dashboard-panel dashboard-notices"><header><h2><NotificationOutlined/>系统公告</h2></header><div class="dashboard-empty">暂无公告</div><details v-if="auth.can('audit:view')" class="dashboard-activity"><summary>近期受控活动</summary><p v-if="activityError">{{activityError}}</p><p v-else-if="!activity.length">暂无受控活动</p><ul v-else><li v-for="item in activity" :key="item.id"><span>{{item.reason??item.objectType}} · {{item.occurredAt}}</span><a-button type="link" @click="router.push({path:'/audit',query:{objectType:item.objectType,objectId:item.objectId}})">查看审计</a-button></li></ul></details></section>
  </section><aside class="dashboard-right">
   <section class="dashboard-panel dashboard-trend"><header><h2>生产批次趋势</h2><span class="dashboard-period"><CalendarOutlined/>近7天</span></header><div ref="trendNode" class="dashboard-chart" role="img" aria-label="近七天创建的生产批次趋势"/></section>
   <section class="dashboard-panel dashboard-stock"><header><h2>库存状态概览</h2><span class="dashboard-legend"><i class="blue"/>正常<i class="orange"/>预警<i class="red"/>超限</span></header><div class="dashboard-stock-body"><div ref="inventoryNode" class="dashboard-donut" role="img" aria-label="库存统计未提供，当前不显示示例比例"/><div class="dashboard-stock-values"><span><i class="blue"/>正常<b>—</b><small>—</small></span><span><i class="orange"/>预警<b>—</b><small>—</small></span><span><i class="red"/>超限<b>—</b><small>—</small></span></div></div></section>
   <section class="dashboard-panel dashboard-quality"><header><h2>质量事件趋势</h2><span class="dashboard-period"><CalendarOutlined/>近7天</span></header><div class="dashboard-chart-note">来料质量调查</div><div ref="qualityNode" class="dashboard-chart" role="img" aria-label="近七天创建的来料质量调查趋势"/></section>
  </aside></div>

 </main>
</template>
<style scoped src="./dashboard-reference.css"/>
