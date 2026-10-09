<script setup lang="ts">
import {ref,watch,computed,onBeforeUnmount} from 'vue'
import {api,errorMessage,type Page} from '../../api/http'
import type {Row} from './model'
const props=defineProps<{modelValue?:string;disabled?:boolean}>(),emit=defineEmits<{'update:modelValue':[string]}>(),rows=ref<Row[]>([]),keyword=ref(''),page=ref(0),more=ref(false),error=ref(''),busy=ref(false)
let sequence=0,timer:ReturnType<typeof setTimeout>|undefined
async function load(append=false){const ticket=++sequence;busy.value=true;error.value='';if(!append)page.value=0;try{const data=await api<Page<Row>>({url:'/finished-inbound-requests',params:{page:page.value,size:50,status:'CONFIRMED',keyword:keyword.value||undefined}});if(ticket!==sequence)return;rows.value=append?[...rows.value,...data.items.filter(item=>!rows.value.some(row=>String(row.id)===String(item.id)))]:data.items;more.value=(page.value+1)*50<data.total}catch(e){if(ticket===sequence)error.value=errorMessage(e)}finally{if(ticket===sequence)busy.value=false}}
function search(value:string){keyword.value=value;clearTimeout(timer);++sequence;busy.value=false;timer=setTimeout(()=>void load(),250)}
function nextPage(event:UIEvent){const target=event.target as HTMLElement;if(more.value&&!busy.value&&target.scrollTop+target.clientHeight>=target.scrollHeight-24){page.value++;void load(true)}}
const choices=computed(()=>{const found=rows.value.map(r=>({value:String(r.id),label:`${r.requestNo} · ${r.batch?.batchNo??''} · ${r.quantity}`}));return props.modelValue&&!found.some(o=>o.value===props.modelValue)?[{value:props.modelValue,label:`已绑定入库申请 ${props.modelValue}`},...found]:found})
watch(()=>props.disabled,()=>{if(!props.disabled)void load()},{immediate:true})
onBeforeUnmount(()=>{clearTimeout(timer);++sequence})
</script><template><div class="finished-reference"><a-select aria-label="已确认成品入库" :value="modelValue||undefined" :disabled="disabled" :loading="busy" show-search allow-clear :filter-option="false" :options="choices" placeholder="输入入库申请编号或批号搜索" :not-found-content="busy?'正在搜索…':'没有匹配记录'" @search="search" @popup-scroll="nextPage" @change="(value:unknown)=>emit('update:modelValue',value==null?'':String(value))"/><p v-if="error" role="alert">{{error}}</p></div></template><style scoped>.finished-reference{min-width:0;flex:1;width:100%}.finished-reference :deep(.ant-select){width:100%}p{font-size:12px;color:#b42318}</style>
