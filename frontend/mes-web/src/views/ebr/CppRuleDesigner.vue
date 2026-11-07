<script setup lang="ts">
import {computed,onMounted,ref,useId,watch} from 'vue'
import {DeleteOutlined,PlusOutlined} from '@ant-design/icons-vue'
import {message} from 'ant-design-vue'
import {api,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import type {EbrDefinition,EbrField,EbrForm,EbrRule} from './types'

type Logic='AND'|'OR'
type CppCondition={id:string;fieldCode:string;operator:'>='|'<='|'>'|'<'|'=='|'!=';value:string}
type CppGroup={id:string;logic:Logic;conditions:CppCondition[]}
type CppConfig={kind:'CPP';name:string;description:string;rootLogic:Logic;groups:CppGroup[];actions:string[]}
const props=defineProps<{definition:EbrDefinition;form:EbrForm|null;editable:boolean}>()
const auth=useAuthStore()
const inputId=useId()
const selectedRuleCode=ref('')
const fieldKeyword=ref('')
const config=ref<CppConfig>(blankConfig())
const unitLabels=ref<Record<string,string>>({})
let hydrating=false

const actionOptions=[
 ['MARK_EXCEPTION','标记异常','字段/规则结果形成异常标记'],
 ['BLOCK_SUBMIT','禁止提交','提交时不满足规则则阻止提交'],
 ['CREATE_DEVIATION','生成偏差记录','提交时不满足规则则进入既有偏差流程'],
 ['REQUIRE_QA_REVIEW','要求QA复核','复用当前表单既有复核规则']
] as const
const currentFields=computed(()=>props.form?.fields??[])
const cppFields=computed(()=>currentFields.value.filter(f=>!['ATTACHMENT','IMAGE','SIGNATURE_PLACEHOLDER'].includes(f.fieldType)))
const visibleCppFields=computed(()=>{const q=fieldKeyword.value.trim().toLowerCase();return q?cppFields.value.filter(f=>(f.label+' '+f.fieldCode).toLowerCase().includes(q)):cppFields.value})
const cppRules=computed(()=>props.definition.rules.filter(r=>r.formCode===props.form?.formCode&&parseConfig(r)?.kind==='CPP'))
const selectedRule=computed(()=>cppRules.value.find(r=>r.ruleCode===selectedRuleCode.value)??null)
const selectedRuleIndex=computed(()=>cppRules.value.findIndex(r=>r.ruleCode===selectedRuleCode.value))
const hasFormReviewRule=computed(()=>!!props.form&&props.definition.reviewRules.some(r=>r.objectScope==='FORM'&&r.objectCode===props.form!.formCode))

function blankCondition():CppCondition{const first=cppFields.value.find(f=>['NUMBER','TIMER','CALCULATED','INSTRUMENT_VALUE'].includes(f.fieldType))??cppFields.value[0];const numeric=!!first&&['NUMBER','TIMER','CALCULATED','INSTRUMENT_VALUE'].includes(first.fieldType);return {id:'C'+Date.now()+Math.random().toString(36).slice(2,6),fieldCode:first?.fieldCode??'',operator:numeric?'>=':'==',value:''}}
function blankGroup():CppGroup{return {id:'G'+Date.now()+Math.random().toString(36).slice(2,6),logic:'AND',conditions:[blankCondition()]}}
function blankConfig():CppConfig{return {kind:'CPP',name:'',description:'',rootLogic:'AND',groups:[{id:'G0',logic:'AND',conditions:[]}],actions:['MARK_EXCEPTION','BLOCK_SUBMIT']}}
function defaultConfig():CppConfig{
 return {kind:'CPP',name:'',description:'',rootLogic:'AND',groups:[{id:'G1',logic:'AND',conditions:[blankCondition()]}],actions:['MARK_EXCEPTION','BLOCK_SUBMIT']}
}
function parseConfig(rule:EbrRule):CppConfig|null{
 if(!rule.cppConfigJson)return null
 try{const value=JSON.parse(rule.cppConfigJson) as CppConfig;return value?.kind==='CPP'?value:null}catch{return null}
}
function clone<T>(value:T):T{return JSON.parse(JSON.stringify(value))}
function loadSelected(){
 hydrating=true
 const rule=selectedRule.value
 config.value=rule?clone(parseConfig(rule)??defaultConfig()):blankConfig()
 queueMicrotask(()=>{hydrating=false})
}
function nextCode(){
 const base='CPP_'+(props.form?.formCode||'FORM')+'_'
 const used=new Set(props.definition.rules.map(r=>r.ruleCode))
 let n=1
 while(used.has(base+n))n++
 return base+n
}
function addRule(){
 if(!props.editable||!props.form)return
 const rule:EbrRule={ruleCode:nextCode(),formCode:props.form.formCode,fieldCode:null,ruleType:'VALIDATION',triggerPoint:'ON_CHANGE',expression:'true',severity:'BLOCK',errorCode:'CPP_RULE_FAILED',messageTemplate:'关键工艺参数不符合要求',deviationTrigger:false,activeFlag:true,cppConfigJson:JSON.stringify(defaultConfig())}
 props.definition.rules.push(rule)
 selectedRuleCode.value=rule.ruleCode
 message.success('已新增CPP规则，请配置条件')
}


function addCondition(group:CppGroup){group.conditions.push(blankCondition())}
function addGroup(){config.value.groups.push(blankGroup())}

function removeCondition(group:CppGroup,index:number){if(group.conditions.length<=1){message.warning('每个条件组至少保留一条条件');return}group.conditions.splice(index,1)}


function removeGroup(index:number){if(config.value.groups.length<=1){message.warning('至少保留一个条件组');return}config.value.groups.splice(index,1)}



function field(code:string){return currentFields.value.find(f=>f.fieldCode===code)}
function operators(f:EbrField|undefined){
 const numeric=f&&['NUMBER','TIMER','CALCULATED','INSTRUMENT_VALUE'].includes(f.fieldType)
 return (numeric?['>=','<=','>','<','==','!=']:['==','!=']).map(value=>({value,label:value==='=='?'=':value}))
}
function normalizeOperator(c:CppCondition){const allowed=operators(field(c.fieldCode)).map(x=>x.value);if(!allowed.includes(c.operator))c.operator=allowed[0] as CppCondition['operator']}
function unit(f:EbrField|undefined){
 if(!f?.unitId)return ''
 return unitLabels.value[f.unitId]??('#'+f.unitId)
}
function valueLiteral(c:CppCondition){
 const f=field(c.fieldCode)
 if(f&&['NUMBER','TIMER','CALCULATED','INSTRUMENT_VALUE'].includes(f.fieldType))return c.value.trim()||'0'
 if(f?.fieldType==='BOOLEAN')return c.value.trim().toLowerCase()==='true'?'true':'false'
 const escaped=c.value.replace(/\\/g,'\\\\').replace(/'/g,"\\'")
 return "'"+escaped+"'"
}
function conditionExpression(c:CppCondition){const safe=c.fieldCode.replace(/'/g,"\\'");return "(value('"+safe+"') "+c.operator+' '+valueLiteral(c)+')'}
function groupExpression(g:CppGroup){return '('+g.conditions.map(conditionExpression).join(' '+(g.logic==='AND'?'&&':'||')+' ')+')'}
const expression=computed(()=>config.value.groups.map(groupExpression).join(' '+(config.value.rootLogic==='AND'?'&&':'||')+' '))
function conditionText(c:CppCondition){
 const f=field(c.fieldCode)
 return (f?.label||c.fieldCode)+' '+(c.operator==='=='?'=':c.operator)+' '+c.value+(unit(f)||'')
}
function groupText(g:CppGroup){
 if(g.logic!=='AND')return g.conditions.map(conditionText).join(' 或 ')
 const rendered:string[]=[];const consumed=new Set<CppCondition>()
 for(const c of g.conditions){
  if(consumed.has(c))continue
  const f=field(c.fieldCode),same=g.conditions.filter(x=>x.fieldCode===c.fieldCode&&!consumed.has(x))
  const lower=same.find(x=>x.operator==='>='||x.operator==='>'),upper=same.find(x=>x.operator==='<='||x.operator==='<')
  if(f&&lower&&upper&&lower!==upper){const u=unit(f);rendered.push(`${lower.value}${u} ${lower.operator==='>='?'≤':'<'} ${f.label} ${upper.operator==='<='?'≤':'<'} ${upper.value}${u}`);consumed.add(lower);consumed.add(upper)}
  else{rendered.push(conditionText(c));consumed.add(c)}
 }
 return rendered.join(' 且 ')
}
const readable=computed(()=>config.value.groups.map(g=>'（'+groupText(g)+'）').join(config.value.rootLogic==='AND'?' 且 ':' 或 '))
const valid=computed(()=>!!config.value.name.trim()&&config.value.groups.every(g=>g.conditions.length&&g.conditions.every(c=>c.fieldCode&&c.value.trim()!=='')))
function syncRule(){
 if(hydrating||!selectedRule.value)return
 const rule=selectedRule.value
 rule.cppConfigJson=JSON.stringify(config.value)
 rule.expression=valid.value?expression.value:'true'
 rule.formCode=props.form?.formCode??null
 rule.fieldCode=null
 rule.ruleType='VALIDATION'
 rule.triggerPoint='ON_CHANGE'
 rule.severity=config.value.actions.includes('BLOCK_SUBMIT')?'BLOCK':'WARN'
 rule.errorCode='CPP_RULE_FAILED'
 rule.messageTemplate=config.value.description.trim()||config.value.name.trim()||'关键工艺参数不符合要求'
 rule.deviationTrigger=false
}
function toggleAction(code:string,checked:boolean){
 const list=config.value.actions
 config.value.actions=checked?[...new Set([...list,code])]:list.filter(x=>x!==code)
}
function saveRule(){
 syncRule()
 if(!valid.value){message.error('请完善规则名称和全部条件');return}
 message.success('CPP规则已写入当前动态表单定义，请使用页面“保存”提交草稿')
}

watch(()=>props.form?.formCode,()=>{selectedRuleCode.value=cppRules.value[0]?.ruleCode??'';loadSelected()},{immediate:true})
watch(()=>cppRules.value.map(r=>r.ruleCode).join('|'),()=>{if(!cppRules.value.some(r=>r.ruleCode===selectedRuleCode.value))selectedRuleCode.value=cppRules.value[0]?.ruleCode??''})
watch(selectedRuleCode,loadSelected)
watch(config,syncRule,{deep:true})

onMounted(async()=>{
 if(!auth.can('master:uom:view'))return
 try{
  const page=await api<Page<Record<string,unknown>>>({url:'/units',params:{size:100}})
  unitLabels.value=Object.fromEntries(page.items.map(x=>[String(x.id),String(x.unitCode??x.unitName??x.id)]))
 }catch{/* unit display gracefully falls back to id */}
})
</script>

<template>
<section class="cpp-rule-designer">
  <aside class="cpp-fields">
    <div class="panel-title"><div><strong>当前表单字段</strong><span>来自“表单设计”页签，可在CPP规则中直接引用</span></div><em>{{cppFields.length}}</em></div>
    <a-empty v-if="!form" :image="false" description="请先选择动态表单"/>
    <template v-else>
      <div class="field-search"><a-input v-model:value="fieldKeyword" allow-clear aria-label="搜索当前表单字段" placeholder="按字段名称或编码查找"/></div>
      <div class="field-list">
        <div v-for="f in visibleCppFields" :key="f.fieldCode" class="field-row">
          <div><b>{{f.label}}</b><small>{{f.fieldCode}}</small></div>
          <span>{{f.fieldType}}</span><em>{{unit(f)||'—'}}</em>
        </div>
      </div>
      <p class="field-note">CPP页只引用字段，不新增或修改字段。</p>
    </template>
  </aside>

  <main class="cpp-builder">
    <div class="builder-head">
      <div><strong>CPP规则配置</strong><span>{{form?.formName||'当前动态表单'}}</span></div>
      <a-button type="primary" :disabled="!editable||!form" @click="addRule"><PlusOutlined/>新建规则</a-button>
    </div>
    <a-empty v-if="!selectedRule" :description="editable?'当前表单尚无CPP规则，点击“新建规则”开始配置':'当前表单尚无CPP规则'"/>
    <template v-else>
      <section class="cpp-step">
        <div class="step-badge">1</div><h3>规则配置</h3>
        <label class="inline-row"><span>规则名称</span><a-input v-model:value="config.name" :disabled="!editable" :maxlength="100"/></label>
        <label class="inline-row"><span>规则描述</span><a-textarea v-model:value="config.description" :disabled="!editable" :rows="2" :maxlength="500"/></label>
      </section>

      <section class="cpp-step">
        <div class="step-badge">2</div>
        <div class="step-title"><h3>判断条件设置</h3><div><a-button :disabled="!editable" @click="addCondition(config.groups[0]!)"><PlusOutlined/>添加条件</a-button><a-button :disabled="!editable" @click="addGroup"><PlusOutlined/>添加条件组</a-button></div></div>
        <div v-if="config.groups.length>1" class="root-logic">
          <span>条件组之间</span>
          <a-radio-group v-model:value="config.rootLogic" aria-label="条件组逻辑" :disabled="!editable" button-style="solid" size="small"><a-radio-button value="AND">全部满足 AND</a-radio-button><a-radio-button value="OR">任一满足 OR</a-radio-button></a-radio-group>
        </div>
        <div v-for="(g,gi) in config.groups" :key="g.id" class="condition-group">
          <div class="group-head">
            <div><b>条件组 {{gi+1}}</b><label class="cpp-accessible-label" :for="`${inputId}-${g.id}-logic`">{{'条件组'+(gi+1)+'逻辑'}}</label><a-select :id="`${inputId}-${g.id}-logic`" v-model:value="g.logic" :disabled="!editable" size="small" style="width:118px" :options="[{value:'AND',label:'组内 AND'},{value:'OR',label:'组内 OR'}]"/></div>
            <div><a-button type="link" :disabled="!editable" @click="addCondition(g)">+ 条件</a-button><a-button v-if="config.groups.length>1" type="link" danger :disabled="!editable" @click="removeGroup(gi)">删除组</a-button></div>
          </div>
          <template v-for="(c,ci) in g.conditions" :key="c.id">
            <div v-if="ci>0" class="logic-chip">{{g.logic}}</div>
            <div class="condition-row">
              <span class="condition-no">{{ci+1}}</span>
              <label class="cpp-accessible-label" :for="`${inputId}-${c.id}-field`">{{'条件'+(ci+1)+'字段'}}</label>
              <a-select :id="`${inputId}-${c.id}-field`" class="condition-field" v-model:value="c.fieldCode" :disabled="!editable" :options="cppFields.map(f=>({value:f.fieldCode,label:f.label}))" @change="normalizeOperator(c)"/>
              <label class="cpp-accessible-label" :for="`${inputId}-${c.id}-operator`">{{'条件'+(ci+1)+'运算符'}}</label>
              <a-select :id="`${inputId}-${c.id}-operator`" class="condition-operator" v-model:value="c.operator" :disabled="!editable" :options="operators(field(c.fieldCode))"/>
              <a-input class="condition-value" v-model:value="c.value" :aria-label="'条件'+(ci+1)+'比较值'" :disabled="!editable" placeholder="比较值"/>
              <span class="condition-unit">{{unit(field(c.fieldCode))||'—'}}</span>
              <a-button class="condition-delete" danger type="text" :aria-label="'删除条件'+(ci+1)" :disabled="!editable||g.conditions.length<=1" @click="removeCondition(g,ci)"><DeleteOutlined/></a-button>
            </div>
          </template>
        </div>
      </section>

      <section class="cpp-step">
        <div class="step-badge">3</div><h3>规则预览</h3>
        <div class="business-preview">{{valid?readable:'请完整配置判断条件'}}</div>
        <details class="system-expression"><summary>系统表达式（安全DSL预览）</summary><code>{{valid?expression:'—'}}</code></details>
      </section>

      <section class="cpp-step actions-step">
        <div class="step-badge">4</div><h3>超限动作设置</h3>
        <div class="action-grid">
          <label v-for="[code,label,tip] in actionOptions" :key="code">
            <a-checkbox :aria-label="label" :checked="config.actions.includes(code)" :disabled="!editable||(code==='REQUIRE_QA_REVIEW'&&!hasFormReviewRule)" @change="(e:any)=>toggleAction(code,e.target.checked)"/><span><b>{{label}}</b><small>{{tip}}{{code==='REQUIRE_QA_REVIEW'&&!hasFormReviewRule?'；请先在“规则与签名”配置表单复核规则':''}}</small></span>
          </label>
        </div>
      </section>
      <div class="builder-actions"><a-button type="primary" :disabled="!editable" @click="saveRule">保存规则配置</a-button></div>
    </template>
  </main>

  <aside class="cpp-help">
    <section class="rule-list-section">
      <div class="side-title"><h3>本表单CPP规则</h3><span>{{cppRules.length}} 条</span></div>
      <a-empty v-if="!cppRules.length" :image="false" description="暂无CPP规则"/>
      <div v-for="r in cppRules" :key="r.ruleCode" :class="['rule-item',{active:r.ruleCode===selectedRuleCode}]">
        <button type="button" class="rule-select" :aria-current="r.ruleCode===selectedRuleCode?'true':undefined" @click="selectedRuleCode=r.ruleCode"><span><b>{{parseConfig(r)?.name||r.ruleCode}}</b><small>{{r.ruleCode}}</small></span></button>
        <a-switch size="small" v-model:checked="r.activeFlag" :aria-label="'启用规则 '+(parseConfig(r)?.name||r.ruleCode)" :disabled="!editable"/>
      </div>
      <p v-if="selectedRule&&editable" class="retention-note">规则停用后仍保留历史受控版本。</p>
    </section>
    <section class="compact-help">
      <h3>配置提示</h3>
      <p>CPP规则只引用当前表单字段；保存后按冻结版本在生产执行时自动判断。</p>
    </section>
    <section class="context-section">
      <h3>当前表单</h3>
      <dl><dt>名称</dt><dd>{{form?.formName||'—'}}</dd><dt>编码</dt><dd>{{form?.formCode||'—'}}</dd><dt>当前规则</dt><dd>{{selectedRuleIndex>=0?selectedRuleIndex+1:'—'}}</dd></dl>
    </section>
  </aside>
</section>
</template>

<style scoped>
.cpp-accessible-label{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip-path:inset(50%);white-space:nowrap;border:0}
.cpp-rule-designer{display:grid;grid-template-columns:270px minmax(640px,1fr) 250px;gap:12px;min-height:720px;padding:12px;background:#f5f8fc}
.cpp-fields,.cpp-help{padding:14px;background:#fff;border:1px solid var(--mes-ui-border);border-radius:8px}.cpp-help{display:flex;flex-direction:column;gap:10px}
.cpp-builder{padding:0;min-width:0}.builder-head,.step-title,.group-head{display:flex;align-items:center;justify-content:space-between;gap:12px}
.panel-title{display:flex;align-items:flex-start;justify-content:space-between;gap:10px;padding-bottom:12px;border-bottom:1px solid var(--mes-ui-border)}.panel-title>div{display:flex;flex-direction:column;min-width:0}.panel-title strong{font-size:15px}.panel-title span{margin-top:4px;font-size:11px;line-height:1.5;color:var(--mes-ui-secondary)}.panel-title em{flex:0 0 auto;min-width:28px;height:28px;display:grid;place-items:center;border-radius:14px;background:#e9f2ff;color:var(--mes-ui-primary);font-style:normal;font-size:12px;font-weight:700}
.field-search{margin:12px 0}.field-list{display:grid;gap:7px;margin-bottom:8px;max-height:620px;overflow:auto;padding-right:2px}.field-row{display:grid;grid-template-columns:minmax(0,1fr) 64px 40px;align-items:center;gap:8px;min-height:52px;padding:9px 10px;border:1px solid #dce4ee;border-radius:7px;background:#fff}.field-row:hover{border-color:#a8c8f5;background:#fbfdff}.field-row div{min-width:0}.field-row b,.field-row small{display:block;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.field-row b{font-size:13px}.field-row small,.field-row span,.field-row em{font-size:11px;color:var(--mes-ui-secondary);font-style:normal}.field-row em{text-align:right}.field-note{margin:8px 2px 0;font-size:11px;line-height:1.5;color:var(--mes-ui-secondary)}
.builder-head{min-height:58px;padding:11px 14px;background:#fff;border:1px solid var(--mes-ui-border);border-radius:8px;margin-bottom:12px}.builder-head strong{font-size:17px}.builder-head span{margin-top:3px;font-size:12px;color:var(--mes-ui-secondary)}.builder-head>div:first-child{display:flex;flex-direction:column}
.cpp-step{position:relative;background:#fff;border:1px solid var(--mes-ui-border);border-radius:8px;padding:15px 15px 16px 52px;margin-bottom:12px;box-shadow:0 1px 2px rgba(31,56,88,.03)}.cpp-step h3{font-size:15px;margin:0 0 12px;color:var(--mes-ui-text)}.step-badge{position:absolute;left:14px;top:14px;width:28px;height:28px;border-radius:50%;display:grid;place-items:center;background:var(--mes-ui-primary);color:#fff;font-weight:700}
.inline-row{display:flex;align-items:center;gap:10px;margin:9px 0}.inline-row>span{flex:0 0 76px;color:var(--mes-ui-secondary);white-space:nowrap}.inline-row :deep(.ant-input),.inline-row :deep(.ant-input-textarea){flex:1;min-width:0}
.root-logic{display:flex;align-items:center;gap:12px;margin:4px 0 10px;color:var(--mes-ui-secondary)}.condition-group{border:1px solid #e7edf5;border-radius:7px;background:#fbfcfe;padding:10px;margin-bottom:10px}.group-head{min-height:30px;margin-bottom:6px}.group-head>div{display:flex;align-items:center;gap:8px}
.condition-row{display:grid;grid-template-columns:30px minmax(170px,1.45fr) 86px minmax(125px,1fr) 54px 34px;align-items:center;gap:8px}.condition-no{width:26px;height:26px;border-radius:50%;display:grid;place-items:center;background:#e9f2ff;color:var(--mes-ui-primary);font-weight:650}.condition-unit{font-size:12px;color:var(--mes-ui-secondary);text-align:center}.logic-chip{width:52px;margin:7px auto;text-align:center;border-radius:12px;background:#e9f2ff;color:var(--mes-ui-primary);font-size:11px;font-weight:700;padding:2px 8px}
.business-preview{padding:15px;text-align:center;border-radius:6px;background:#eef6ff;border:1px solid #d4e8ff;color:#145fbd;font-size:18px;font-weight:700;overflow-wrap:anywhere}.system-expression{margin-top:9px;padding:9px 11px;background:#fafbfc;border-radius:6px;color:var(--mes-ui-secondary)}.system-expression code{display:block;margin-top:7px;color:var(--mes-ui-text);overflow-wrap:anywhere}
.action-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px}.action-grid label{display:flex;align-items:flex-start;gap:8px;min-height:58px;padding:10px;border:1px solid #dce4ee;border-radius:7px;background:#fff}.action-grid span{display:flex;flex-direction:column}.action-grid small{color:var(--mes-ui-secondary);font-size:11px;margin-top:3px}.builder-actions{display:flex;justify-content:flex-end;padding:0 2px 2px}
.cpp-help section{border:1px solid var(--mes-ui-border);border-radius:7px;padding:11px;background:#fff}.cpp-help h3{margin:0;font-size:14px}.side-title{display:flex;align-items:center;justify-content:space-between;margin-bottom:9px}.side-title span{font-size:11px;color:var(--mes-ui-secondary)}.rule-list-section{flex:0 0 auto}.rule-item{width:100%;display:flex;align-items:center;gap:8px;border:1px solid transparent;background:#fff;border-radius:6px;padding:5px 6px;margin:4px 0}.rule-item.active{background:#eaf3ff;border-color:#b6d5ff}.rule-select{flex:1;min-width:0;border:0;background:transparent;padding:4px 2px;text-align:left;cursor:pointer;color:inherit}.rule-select span{min-width:0}.rule-select b,.rule-select small{display:block;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.rule-select small{color:#51627a;font-size:11px;margin-top:2px}.retention-note{margin:8px 2px 0;font-size:11px;line-height:1.5;color:var(--mes-ui-secondary)}.compact-help{background:#fafcff!important}.compact-help p{margin:7px 0 0;font-size:11px;line-height:1.65;color:var(--mes-ui-secondary)}.context-section{margin-top:auto}.cpp-help dl{display:grid;grid-template-columns:64px minmax(0,1fr);gap:7px;margin:9px 0 0;font-size:12px}.cpp-help dt{color:var(--mes-ui-secondary)}.cpp-help dd{margin:0;overflow-wrap:anywhere}
@container ebr-designer (max-width:1200px){.cpp-rule-designer{grid-template-columns:230px minmax(560px,1fr)}.cpp-help{grid-column:1/-1;display:grid;grid-template-columns:minmax(0,1.5fr) minmax(220px,.8fr) minmax(220px,.8fr)}.context-section{margin-top:0}}
@container ebr-designer (max-width:900px){
 .cpp-rule-designer{grid-template-columns:minmax(0,1fr);min-height:0}.cpp-fields{border-right:0;border-bottom:1px solid var(--mes-ui-border)}.cpp-help{grid-column:auto;display:flex;border-left:0;border-top:1px solid var(--mes-ui-border)}
 .cpp-builder{padding:10px 8px}.builder-head,.step-title,.group-head{align-items:flex-start;flex-wrap:wrap}.cpp-step{padding:12px 10px 14px 44px}.step-badge{left:10px;top:12px}
 .condition-row{grid-template-columns:28px minmax(0,1fr) minmax(0,1fr) 34px;grid-template-areas:'no field field del' '. op value value' '. unit unit unit';gap:7px}.condition-no{grid-area:no}.condition-field{grid-area:field;min-width:0}.condition-operator{grid-area:op;min-width:0}.condition-value{grid-area:value;min-width:0;width:100%}.condition-unit{grid-area:unit;min-width:0;overflow:hidden;text-overflow:ellipsis;text-align:right}.condition-delete{grid-area:del;justify-self:end}
 .action-grid{grid-template-columns:1fr}.inline-row>span{flex-basis:72px}.business-preview{font-size:15px;padding:12px}.field-list{max-height:360px}
}
</style>
