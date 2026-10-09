<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave, onBeforeRouteUpdate } from 'vue-router'
import { api, errorMessage, idempotencyKey, type Page, type Permission, type Role } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import { catalogTree, descendants, type MenuEntry } from './menuCatalog'
const route = useRoute(), router = useRouter(), auth = useAuthStore()
const isCreate = computed(() => route.meta.mode === 'create')
const readonly = computed(() => route.meta.mode === 'view')
const role = ref<Role | null>(null), permissions = ref<Permission[]>([])
const menus = ref<MenuEntry[]>([]), selectedMenus = ref<string[]>([])
const roleCode = ref(''), roleName = ref(''), status = ref<'ACTIVE' | 'INACTIVE'>('ACTIVE')
const selected = ref<string[]>([]), reason = ref(''), search = ref(''), selectedOnly = ref(false)
const loading = ref(false), busy = ref(false), error = ref(''), notice = ref('')
const canAssign = computed(() => auth.can('iam:role:update') && auth.can('iam:permission:view') && auth.can('iam:menu:view'))
const canSave = computed(() => isCreate.value ? auth.can('iam:role:create') : auth.can('iam:role:update'))
const state = () => JSON.stringify([roleCode.value, roleName.value, status.value, [...selected.value].sort(), [...selectedMenus.value].sort(), reason.value])
const baseline = ref('')
const dirty = computed(() => !readonly.value && baseline.value !== '' && baseline.value !== state())
const names: Record<string, string> = { iam: '身份与权限', master: '基础主数据', process: '产品与工艺', production: '生产管理', mes: '生产执行', wms: '仓储管理', qms: '质量管理', qa: '质量决定', ebr: '电子批记录', audit: '审计追踪', trace: '完整追溯', integration: '集成运维', balance: '物料平衡', print: '打印管理', attachment: '附件管理', auth: '身份认证', home: '首页' }
const resources: Record<string, string> = { home: '首页', users: '用户', user: '用户', roles: '角色', role: '角色', permission: '权限目录', menu: '菜单', sign: '电子签名', template: '模板', designer: '模板设计器', pdf: 'PDF归档', form: '批记录表单', record: '批记录', review: '批记录复核', org: '组织', uom: '单位与换算', equipment: '设备', qualification: '人员资格', material: '物料主数据', supplier: '供应商', product: '产品', package: '生产工艺', inventory: '库存', receipt: '收货记录', issue: '出库单', reservation: '库存预留', request: '领料申请', 'finished-inbound': '成品入库', 'finished-shipment': '成品发货', specification: '质量标准', deviation: '质量调查', 'inspection-request': '请验单', report: '检验报告', sampling: '取样', test: '检验记录', ipc: '过程检验', plan: '生产质量计划', sample: '样品', capa: '纠正预防措施', 'finished-request': '成品请验', 'finished-sampling': '成品取样', 'finished-report': '成品检验报告', 'material-release': '物料放行', 'material-inventory': '物料库存', 'batch-review': 'QA批审核', release: '成品放行', clearance: '清场', quantity: '生产数量', weigh: '称量', charge: '投料', execution: '生产执行', operation: '工序', param: '工序参数', batch: '生产批', order: '生产订单', subbatch: '子批', trail: '审计追踪' }
const actions: Record<string, string> = { view: '查看', create: '新增', update: '编辑', edit: '编辑', disable: '停用', submit: '提交', export: '导出', print: '打印', approve: '批准', publish: '发布', generate: '生成', correct: '更正', verify: '复核', adjust: '调整', move: '移库', confirm: '确认', return: '退回', reverse: '冲销', cancel: '取消', close: '关闭', decide: '决定', investigate: '调查', accept: '受理', assign: '分配', complete: '完成', execute: '执行', record: '记录', receive: '接收', retest: '复检', freeze: '冻结', unfreeze: '解除冻结', start: '开始', pause: '暂停', bind: '绑定', retire: '停用', retry: '重试', 'approve-plan': '批准计划' }
const legacyLabels: Record<string, string> = { 'menu:home': '首页', 'menu:iam:users': '用户管理', 'menu:iam:roles': '角色管理', 'action:iam:user.manage': '维护用户账号', 'action:iam:role.manage': '维护角色与权限', 'audit:view': '查看审计追踪', 'ebr:sign': '电子签名', 'integration:view': '查看集成运维', 'integration:retry': '重试集成消息', 'trace:view': '查看完整追溯', 'balance:view': '查看物料平衡', 'balance:investigate': '调查物料平衡', 'balance:approve': '批准物料平衡', 'qa:batch-review': 'QA批审核', 'qa:release': '成品放行' }
function permissionLabel(permission: Permission) {
  const code = permission.permissionCode
  if (legacyLabels[code]) return legacyLabels[code]
  const parts = code.split(':')
  if (parts.length === 3 && resources[parts[1]!] && actions[parts[2]!]) { const resource = parts[1] === 'template' ? `${parts[0] === 'print' ? '打印' : parts[0] === 'ebr' ? 'eBR' : ''}模板` : resources[parts[1]!] ; return `${actions[parts[2]!] }${resource}` }
  return permission.permissionName
}
function menuPermissions(menu: MenuEntry) {
  const codes = new Set([menu.permissionCode, ...(menu.permissionCodes ?? [])])
  return permissions.value.filter(p => codes.has(p.permissionCode))
}
const permissionGroups = computed(() => {
  const groups = menus.value.filter(m => m.routePath).map(menu => ({ key: menu.id, title: menu.menuName, items: menuPermissions(menu) }))
  const linked = new Set(groups.flatMap(g => g.items.map(p => p.permissionCode)))
  const unlinked = permissions.value.filter(p => !linked.has(p.permissionCode))
  if (unlinked.length) groups.push({ key: 'unlinked', title: '公共及兼容功能权限', items: unlinked })
  const keyword = search.value.trim().toLowerCase()
  return groups.map(group => ({ ...group, items: group.items.filter(p => (!selectedOnly.value || selected.value.includes(p.permissionCode)) && (!keyword || `${group.title} ${permissionLabel(p)} ${p.permissionName} ${p.permissionCode}`.toLowerCase().includes(keyword))) })).filter(group => group.items.length)
})
const unavailable = computed(() => selected.value.filter(code => !permissions.value.some(p => p.permissionCode === code)))
const activeDomain = ref('')
watch(permissionGroups, groups => { if (groups.length && !groups.some(group => group.key === activeDomain.value)) activeDomain.value = groups[0]!.key })
const displayedGroups = computed(() => search.value.trim() ? permissionGroups.value : permissionGroups.value.filter(group => group.key === activeDomain.value))
interface GrantNode { key: string; title: string; kind: string; children: GrantNode[]; checkable?: boolean; items?: Permission[]; permission?: Permission; parentKey?: string }
function functionNodes(key: string, items: Permission[]): GrantNode[] {
  return [...ordinaryResources(items,key).map(resource=>({key:`ordinary:${key}:${resource.key}`,title:resource.title,kind:'普通权限',children:[],checkable:false,items:resource.items,parentKey:key})),...specialPermissions(items).filter(p=>p.permissionCode!==menus.value.find(m=>m.id===key)?.permissionCode).map(permission=>({key:`function:${key}:${permission.permissionCode}`,title:permissionLabel(permission),kind:'特殊操作权限',children:[],checkable:false,permission,parentKey:key}))]
}
const authorizationTree = computed(()=>{
  function build(nodes: ReturnType<typeof catalogTree>): GrantNode[] { return nodes.flatMap(node=>{
    const group=permissionGroups.value.find(g=>g.key===node.key)
    const children=node.entry.routePath ? functionNodes(node.key,group?.items??[]) : build(node.children)
    if((search.value.trim() || selectedOnly.value) && !children.length)return []
    return [{key:node.key,title:node.title,kind:node.entry.routePath?'菜单':'目录',children}]
  }) }
  const nodes=build(catalogTree(menus.value)), unlinked=permissionGroups.value.find(g=>g.key==='unlinked')
  if(unlinked)nodes.push({key:'unlinked',title:unlinked.title,kind:'公共权限',children:functionNodes('unlinked',unlinked.items),checkable:false})
  return nodes
})
function expandTree(keys:(string|number)[]){expandedKeys.value=keys.map(String)}
function hasView(items:Permission[]){return items.some(p=>p.permissionCode.endsWith(':view'))}
function hasEditing(items:Permission[]){return items.some(p=>!p.permissionCode.endsWith(':view'))}
const expandedKeys=ref<string[]>([])
const allBranchKeys=computed(()=>{const keys:string[]=[];function visit(nodes:GrantNode[]){for(const n of nodes){if(n.children.length)keys.push(n.key);visit(n.children)}}visit(authorizationTree.value);return keys})
watch(allBranchKeys,keys=>{expandedKeys.value=keys}, {immediate:true})
const checkedMenuKeys = computed(() => menus.value.filter(m=>m.routePath ? selectedMenus.value.includes(m.menuCode) : descendants(m.id,menus.value).filter(child=>child.routePath).length>0 && descendants(m.id,menus.value).filter(child=>child.routePath).every(child=>selectedMenus.value.includes(child.menuCode))).map(m=>m.id))
function chooseMenu(keys: (string|number)[]) { const key = String(keys[0] ?? ''); if (menus.value.find(m=>m.id===key)?.routePath || key==='unlinked') activeDomain.value=key }
function changeMenu(_keys: unknown, info: {node: {key: string|number}; checked: boolean}) {
  if (!canAssign.value || readonly.value || busy.value) return
  const codes = new Set(selectedMenus.value), grants = new Set(selected.value)
  const affected = descendants(String(info.node.key),menus.value).filter(m=>m.routePath && m.status==='ACTIVE')
  for (const menu of affected) {
    if (info.checked) { codes.add(menu.menuCode); if (menu.permissionCode) grants.add(menu.permissionCode); for(const code of menu.permissionCodes??[])grants.add(code); for (const code of (menu.requiredPermissions ?? '').split(',').filter(Boolean)) grants.add(code) }
    else codes.delete(menu.menuCode)
  }
  if(!info.checked){ const removed=new Set(affected.flatMap(m=>[m.permissionCode,...(m.permissionCodes??[]),...(m.requiredPermissions??'').split(',')]));const retained=new Set(menus.value.filter(m=>codes.has(m.menuCode)).flatMap(m=>[m.permissionCode,...(m.permissionCodes??[]),...(m.requiredPermissions??'').split(',')]));for(const code of removed)if(code && !retained.has(code))grants.delete(code) }
  selectedMenus.value=[...codes]; selected.value=[...grants]
}
const crudActions = new Set(['view', 'create', 'update', 'edit', 'delete', 'remove'])
function isOrdinary(p: Permission) { const parts = p.permissionCode.split(':'); return parts.length === 3 && !['menu', 'action'].includes(parts[0]!) && crudActions.has(parts[2]!) }
function ordinaryResources(items: Permission[], groupKey = activeDomain.value) {
  const groups = new Map<string, Permission[]>()
  for (const item of items.filter(isOrdinary)) { const key = item.permissionCode.split(':').slice(0, 2).join(':'); groups.set(key, [...(groups.get(key) ?? []), item]) }
  // Build presets from the complete catalog, even when only one action matches a search.
  return [...groups].map(([key]) => { const source = menus.value.find(m=>m.id===groupKey); const available = source ? menuPermissions(source) : items; const all = available.filter(p => isOrdinary(p) && p.permissionCode.startsWith(`${key}:`)); return { key, title: resources[key.split(':')[1]!] ?? all[0]?.permissionName ?? key, items: all } })
}
function specialPermissions(items: Permission[]) { return items.filter(p => !isOrdinary(p)) }
function accessLevel(items: Permission[]) {
  const chosen = items.filter(p => selected.value.includes(p.permissionCode))
  if (!chosen.length) return 'none'
  if (chosen.length === items.length && items.some(p => !p.permissionCode.endsWith(':view'))) return 'crud'
  if (chosen.every(p => p.permissionCode.endsWith(':view'))) return 'view'
  return 'custom'
}
function setAccess(items: Permission[], level: string) {
  if (readonly.value || !canAssign.value || busy.value) return
  const codes = new Set(selected.value)
  for (const item of items) { if (level === 'crud' || (level === 'view' && item.permissionCode.endsWith(':view'))) codes.add(item.permissionCode); else codes.delete(item.permissionCode) }
  selected.value = [...codes]
  selectedMenus.value=selectedMenus.value.filter(code=>{const m=menus.value.find(m=>m.menuCode===code);return !m || ((!m.permissionCode || codes.has(m.permissionCode)) && (m.requiredPermissions??'').split(',').filter(Boolean).every(p=>codes.has(p)))})
}
function visibleMenu(key: string) { const menu=menus.value.find(m=>m.id===key);return !menu || selectedMenus.value.includes(menu.menuCode) }
function accessChanged(items: Permission[], event: { target: { value: string } }) { setAccess(items, event.target.value) }
function supportedActions(items: Permission[]) { return [...new Set(items.map(p => actions[p.permissionCode.split(':')[2]!] ?? ({ delete: '删除', remove: '删除' } as Record<string, string>)[p.permissionCode.split(':')[2]!] ?? '操作'))].join('、') }
const accessNames: Record<string, string> = { none: '未授权', view: '只查看', crud: '增删改查', custom: '已有自定义授权' }
const added = computed(() => selected.value.filter(code => !(role.value?.permissionCodes ?? []).includes(code)))
const removed = computed(() => (role.value?.permissionCodes ?? []).filter(code => !selected.value.includes(code)))
function labelFor(code: string) { const item = permissions.value.find(p => p.permissionCode === code); return item ? permissionLabel(item) : code }
const visibleSelection = computed({
  get: () => selected.value.filter(code => permissionGroups.value.some(group => specialPermissions(group.items).some(item => item.permissionCode === code))),
  set: (value: string[]) => {
    const visible = new Set(permissionGroups.value.flatMap(group => specialPermissions(group.items).map(item => item.permissionCode)))
    selected.value = [...new Set([...selected.value.filter(code => !visible.has(code)), ...value])]
  }
})
function groupSelected(items: Permission[]) { return items.filter(p => selected.value.includes(p.permissionCode)).length }
function setGroupAccess(items: Permission[], level: string, groupKey: string) { if (!visibleMenu(groupKey)) return; for (const resource of ordinaryResources(items, groupKey)) setAccess(resource.items, level) }
let generation = 0
async function load() {
  const current = ++generation
  loading.value = true; error.value = ''; notice.value = ''; baseline.value = ''
  role.value = null; roleCode.value = ''; roleName.value = ''; status.value = 'ACTIVE'; selected.value = []; selectedMenus.value=[]; menus.value=[]; permissions.value = []; reason.value = ''; search.value = ''
  selectedOnly.value = readonly.value
  try {
    const loadedRole = isCreate.value ? null : await api<Role>({ url: `/roles/${route.params.id}` })
    const catalog: Permission[] = []
    const directory: MenuEntry[] = []
    if (auth.can('iam:menu:view')) for(let page=0;;page++) { const result=await api<Page<MenuEntry>>({url:'/menus',params:{page,size:100}}); if(current!==generation)return; directory.push(...result.items); if(directory.length>=result.total)break; if(!result.items.length)throw new Error('Incomplete menu catalog') }
    if (auth.can('iam:permission:view')) {
      for (let page = 0; ; page++) {
        const result = await api<Page<Permission>>({ url: '/permissions', params: { page, size: 100, status: 'ACTIVE' } })
        if (current !== generation) return
        catalog.push(...result.items)
        if (catalog.length >= result.total) break
        if (!result.items.length) throw new Error('Incomplete permission catalog')
      }
    }
    if (current !== generation) return
    activeDomain.value = directory.find(m=>m.routePath)?.id ?? 'unlinked'; role.value = loadedRole; permissions.value = catalog; menus.value=directory
    if (loadedRole) { roleCode.value = loadedRole.roleCode; roleName.value = loadedRole.roleName; status.value = loadedRole.status; selected.value = [...loadedRole.permissionCodes]; selectedMenus.value=[...loadedRole.menuCodes] }
    baseline.value = state()
  } catch (cause) { if (current === generation) error.value = errorMessage(cause) }
  finally { if (current === generation) loading.value = false }
}
async function save() {
  if (busy.value || loading.value || readonly.value || !canSave.value || !baseline.value) return
  error.value = ''; notice.value = ''
  if (!roleName.value.trim()) { error.value = '请输入角色名称'; return }
  if (roleName.value.trim().length > 128) { error.value = '角色名称不能超过 128 个字符'; return }
  if (isCreate.value && !/^[A-Z][A-Z0-9_]{1,127}$/.test(roleCode.value.trim())) { error.value = '角色编码须为 2–128 位大写字母、数字或下划线，并以字母开头'; return }
  busy.value = true
  let infoSaved = false
  try {
    if (!role.value && isCreate.value) {
      role.value = await api<Role>({ method: 'POST', url: '/roles', headers: { 'Idempotency-Key': idempotencyKey() }, data: { roleCode: roleCode.value.trim(), roleName: roleName.value.trim(), reason: '角色配置维护（系统记录）' } })
      infoSaved = true
    } else if (role.value && (roleName.value.trim() !== role.value.roleName || status.value !== role.value.status)) {
      role.value = await api<Role>({ method: 'PUT', url: `/roles/${role.value.id}`, headers: { 'Idempotency-Key': idempotencyKey(), 'If-Match': `"${role.value.version}"` }, data: { roleName: roleName.value.trim(), status: status.value, reason: '角色配置维护（系统记录）' } })
      infoSaved = true
    }
    if (role.value && canAssign.value && (JSON.stringify([...selected.value].sort()) !== JSON.stringify([...role.value.permissionCodes].sort()) || JSON.stringify([...selectedMenus.value].sort()) !== JSON.stringify([...role.value.menuCodes].sort()))) {
      const eligibleMenus = selectedMenus.value.filter(code => { const menu=menus.value.find(m=>m.menuCode===code); return !menu || ((!menu.permissionCode || selected.value.includes(menu.permissionCode)) && (menu.requiredPermissions??'').split(',').filter(Boolean).every(p=>selected.value.includes(p))) })
      role.value = await api<Role>({ method: 'POST', url: `/roles/${role.value.id}/permissions`, headers: { 'Idempotency-Key': idempotencyKey(), 'If-Match': `"${role.value.version}"` }, data: { permissionCodes: selected.value, menuCodes: eligibleMenus, reason: '角色配置维护（系统记录）' } })
    }
    if (role.value) { roleCode.value = role.value.roleCode; roleName.value = role.value.roleName; status.value = role.value.status; selected.value = [...role.value.permissionCodes]; selectedMenus.value=[...role.value.menuCodes] }
    reason.value = ''; baseline.value = state(); notice.value = '角色及权限已保存'
    if (isCreate.value && role.value) await router.replace({ path: `/admin/roles/${role.value.id}/edit`, query: route.query })
  } catch (cause) { error.value = `${infoSaved ? '角色信息已保存，权限分配未完成；当前选择已保留，请核对后重试。' : ''}${errorMessage(cause)}` }
  finally { busy.value = false }
}
function confirmLeave() { return !dirty.value || window.confirm('离开将放弃未保存的角色与权限修改，是否继续？') }
onBeforeRouteLeave(() => !busy.value && confirmLeave())
onBeforeRouteUpdate(to => (to.params.id === route.params.id && to.meta.mode === route.meta.mode) || (!busy.value && confirmLeave()))
watch(() => [route.params.id, route.meta.mode], load, { immediate: true })
</script>
<template>
  <main :data-ui-template="readonly ? 'T3' : 'T2'" class="admin-page role-detail-page">
    <header class="admin-page-header">
      <div><a-button type="link" :disabled="busy" @click="router.push({ path: '/admin/roles', query: route.query })">← 返回角色列表</a-button><h1>{{ isCreate ? '新增角色' : readonly ? '角色详情' : '编辑角色' }}</h1><p>在菜单树上勾选可见菜单，再配置菜单下的功能权限。</p></div>
      <a-button v-if="readonly && auth.can('iam:role:update') && role" type="primary" @click="router.push({ path: `/admin/roles/${role.id}/edit`, query: route.query })">编辑</a-button>
    </header>
    <a-alert v-if="error" type="error" :message="error" show-icon />
    <a-alert v-if="notice" type="success" :message="notice" show-icon />
    <a-spin :spinning="loading"><div class="role-authorization-layout"><aside class="role-information">
      <a-card title="角色信息" class="form-section">
        <a-form layout="vertical" class="role-information-form">
          <a-form-item label="角色编码" :required="!readonly"><a-input v-model:value="roleCode" aria-label="角色编码" :disabled="readonly || !isCreate || !!role || busy" :maxlength="128" placeholder="例如 WAREHOUSE_OPERATOR" /></a-form-item>
          <a-form-item label="角色名称" :required="!readonly"><a-input v-model:value="roleName" aria-label="角色名称" :disabled="readonly || busy" :maxlength="128" placeholder="请输入清晰的岗位名称" /></a-form-item>
          <a-form-item label="状态"><a-select show-search option-filter-prop="children" v-model:value="status" aria-label="状态" :disabled="readonly || isCreate || busy"><a-select-option value="ACTIVE">启用</a-select-option><a-select-option value="INACTIVE">停用</a-select-option></a-select></a-form-item>
        </a-form>
      </a-card>
      <a-card v-if="!readonly" title="操作" class="form-section"><div class="role-save-bar"><span class="muted">{{ dirty ? '有未保存的修改' : '修改后请保存' }}</span><a-space><a-button :disabled="busy" @click="router.push({ path: '/admin/roles', query: route.query })">取消</a-button><a-button type="primary" :loading="busy" :disabled="loading || !baseline || !canSave" @click="save">保存</a-button></a-space></div></a-card>
</aside>
      <a-card title="角色菜单权限分配" class="form-section">
        <a-alert v-if="!auth.can('iam:permission:view')" type="warning" message="当前账号没有查看权限目录的权限；已有权限不会被清空。" show-icon />
        <template v-else>
          <div class="permission-toolbar"><a-input v-model:value="search" aria-label="搜索权限" allow-clear placeholder="搜索模块、权限名称或编码" /><a-checkbox v-model:checked="selectedOnly">仅看已选</a-checkbox><span class="permission-summary">已选 {{ selected.length }} 项 / 可用 {{ permissions.length }} 项</span></div>
          <p class="muted">普通权限选择“只查看”或“增删改查”；增删改查仅包含该对象现有的普通操作。勾选菜单时同时选中其特殊权限；不需要的再取消。搜索不会清空已有选择。</p>
          <a-alert v-if="unavailable.length" type="warning" :message="`已选权限中有 ${unavailable.length} 项未在可用目录展示，保存时会保留。`" show-icon />
          <details v-if="unavailable.length" class="unavailable-permissions"><summary>查看未展示的已选权限</summary><ul><li v-for="code in unavailable" :key="code">{{ code }}</li></ul></details>
          <a-empty v-if="!loading && !permissionGroups.length" :description="search ? '没有匹配的权限，请调整搜索条件' : selectedOnly ? '没有已选的可用权限' : '暂无可用权限'" />
          <a-alert v-if="!auth.can('iam:menu:view')" type="warning" message="当前账号没有查看菜单目录的权限，已有菜单授权会保持。" show-icon />
          <div class="grant-tree-actions"><span>已选菜单 {{selectedMenus.length}} 项 · 勾选菜单默认选中全部功能</span><a-space><a-button type="link" @click="expandedKeys=allBranchKeys">展开</a-button><a-button type="link" @click="expandedKeys=[]">折叠</a-button></a-space></div>
          <a-checkbox-group v-model:value="visibleSelection" :disabled="readonly || !canAssign || busy" class="grant-tree-group">
            <a-tree aria-label="角色菜单授权树" :tree-data="authorizationTree" :checkable="!readonly && canAssign" :checked-keys="checkedMenuKeys" check-strictly :expanded-keys="expandedKeys" :disabled="busy" block-node @expand="expandTree" @check="changeMenu">
              <template #title="node"><div class="grant-node" @click.stop="node.items || node.permission ? undefined : chooseMenu([node.key])">
                <template v-if="node.items"><span>{{node.title}}</span><a-tag>普通权限</a-tag><a-radio-group v-if="!readonly" :value="accessLevel(node.items)" :aria-label="`${node.title}权限级别`" :disabled="!canAssign || busy || !visibleMenu(node.parentKey)" @change="accessChanged(node.items,$event)"><a-radio-button v-if="node.parentKey==='unlinked'" value="none">不授权</a-radio-button><a-radio-button value="view" :disabled="!hasView(node.items)">只查看</a-radio-button><a-radio-button value="crud" :disabled="!hasEditing(node.items)">增删改查</a-radio-button></a-radio-group><a-tag v-else>{{accessNames[accessLevel(node.items)]}}</a-tag></template>
                <template v-else-if="node.permission"><a-checkbox v-if="!readonly" :value="node.permission.permissionCode" :disabled="!visibleMenu(node.parentKey)">{{node.title}} <span class="muted">特殊操作权限</span></a-checkbox><span v-else>{{node.title}} · {{selected.includes(node.permission.permissionCode)?'已授权':'未授权'}}</span><details class="grant-code"><summary>编码</summary><code>{{node.permission.permissionCode}}</code></details></template>
                <template v-else><strong>{{node.title}}</strong><a-tag :color="node.kind==='目录'?'geekblue':'green'">{{node.kind}}</a-tag></template>
              </div></template>
            </a-tree>
          </a-checkbox-group>
          <div v-if="!readonly" class="permission-change-preview" aria-live="polite"><strong>本次权限变更：新增 {{ added.length }} 项，移除 {{ removed.length }} 项</strong><details v-if="added.length || removed.length"><summary>核对变更明细</summary><ul><li v-for="code in added" :key="`add-${code}`">新增 · {{ labelFor(code) }}</li><li v-for="code in removed" :key="`remove-${code}`">移除 · {{ labelFor(code) }}</li></ul></details><span v-else class="muted">权限范围未改变</span></div>
        </template>
      </a-card>
      </div>
    </a-spin>
  </main>
</template>
<style scoped>
.role-detail-page :deep(.ant-alert){margin-bottom:16px}
.permission-menu-tree{width:280px;flex-shrink:0;padding:16px;background:#f5f8fc;border-radius:8px;max-height:660px;overflow:auto}.permission-menu-tree :deep(.ant-tree){background:transparent}.permission-menu-tree p{font-size:12px;line-height:1.7}.permission-menu-tree>strong{font-size:14px}@media(max-width:900px){.permission-menu-tree{width:100%;max-height:320px}}
.permission-resource{grid-column:1/-1;display:flex;align-items:center;justify-content:space-between;gap:16px;flex-wrap:wrap;padding:16px;background:#fafcff;border:1px solid #edf2f8;border-radius:8px}.permission-resource-title{display:grid;gap:6px}.permission-resource-title small,.permission-resource>small{color:#6b7f99;font-size:12px}.permission-resource-title strong{font-size:14px;font-weight:500}.special-permissions-title{grid-column:1/-1;font-size:13px;color:#58708f;margin:12px 0 0}
.permission-workspace{display:flex;gap:24px;align-items:flex-start}.permission-modules{width:190px;flex-shrink:0;display:grid;gap:4px;padding:8px;background:#f5f8fc;border-radius:8px}.permission-modules button{display:flex;align-items:center;justify-content:space-between;gap:8px;border:0;background:transparent;border-radius:6px;padding:11px 12px;text-align:left;color:#58708f;cursor:pointer;font:inherit;font-size:13px}.permission-modules button[aria-pressed=true]{background:#e6efff;color:#2563eb;font-weight:600}.permission-modules button:focus-visible{outline:2px solid #2563eb;outline-offset:2px}.permission-modules small{font-size:11px;font-weight:400}.permission-grid{flex:1;min-width:0;display:block}.permission-change-preview{margin-top:24px;padding:16px;background:#f5f8fc;border-radius:8px;display:grid;gap:8px;font-size:13px}.permission-change-preview summary{cursor:pointer}
.permission-toolbar{display:flex;align-items:center;gap:20px;flex-wrap:wrap}.permission-toolbar>.ant-input-affix-wrapper{max-width:420px}.permission-summary{margin-left:auto;color:#58708f;font-size:13px}
.permission-domain{grid-column:1/-1;margin-bottom:4px}.permission-items{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px;padding-top:12px}.permission-group-select{grid-column:1/-1}.permission-domain-header{cursor:pointer;display:flex;justify-content:space-between;align-items:center;padding:12px 0;border-bottom:1px solid #edf2f8}.permission-domain h3{font-size:15px;margin:0;color:#14254e}.permission-domain h3 span{font-size:12px;font-weight:400;color:#6b7f99;margin-left:10px}
.permission-row{display:flex;align-items:flex-start;justify-content:space-between;gap:12px;padding:12px 14px;background:#fafcff}.permission-row :deep(.ant-checkbox-wrapper){flex:1;min-width:0;align-items:flex-start}.permission-copy{display:grid;gap:5px;white-space:normal}.permission-copy strong{font-size:13px;font-weight:500}.permission-copy small{font-size:12px}.permission-row details{font-size:12px;color:#6b7f99;max-width:45%}.permission-row summary,.unavailable-permissions summary{cursor:pointer}.permission-row code{display:block;overflow-wrap:anywhere;margin-top:8px}.unavailable-permissions{margin-bottom:16px;font-size:12px;color:#58708f}
.role-save-bar{display:flex;align-items:center;justify-content:space-between;gap:16px}
@media(max-width:900px){.permission-workspace{flex-direction:column;gap:16px}.permission-modules{width:100%;display:flex;overflow:auto}.permission-modules button{flex-shrink:0}.permission-grid{width:100%}.permission-items{grid-template-columns:1fr}.permission-summary{margin-left:0}.permission-toolbar{gap:12px}.permission-domain-header{gap:12px;flex-wrap:wrap}.role-save-bar{flex-wrap:wrap}}
</style>

<style scoped>
.role-authorization-layout{display:grid;grid-template-columns:minmax(280px,340px) minmax(0,1fr);gap:20px;align-items:start}.role-information{min-width:0}.role-information .detail-form{display:block}.role-information :deep(.ant-form-item){display:block}.role-information .role-save-bar{display:grid;gap:12px}.grant-tree-group{display:block;width:100%}.grant-tree-actions{display:flex;justify-content:space-between;align-items:center;gap:12px;font-size:12px;color:#61738e}.grant-node{display:flex;align-items:center;flex-wrap:wrap;gap:10px;min-height:34px;font-size:13px}.grant-node strong{font-weight:500}.grant-node :deep(.ant-tag){margin:0;font-size:11px;line-height:19px}.grant-node :deep(.ant-radio-button-wrapper){font-size:12px;height:28px;line-height:26px;padding:0 12px}.grant-code{font-size:11px;color:#8493a8}.grant-code code{font-size:11px;overflow-wrap:anywhere}.grant-tree-group :deep(.ant-tree-node-content-wrapper){min-width:0;flex:1}.grant-tree-group :deep(.ant-tree-treenode){padding:3px 0}.grant-tree-group :deep(.ant-tree-checkbox){margin-top:9px}.grant-tree-group :deep(.ant-tree-switcher){line-height:40px}@media(max-width:1100px){.role-authorization-layout{grid-template-columns:1fr}.role-information{display:grid;grid-template-columns:1fr 1fr;gap:16px}}@media(max-width:700px){.role-information{display:block}.grant-node{gap:6px}}
</style>

<style scoped>
.role-information-form :deep(.ant-form-item){margin-bottom:18px}.role-information-form :deep(.ant-form-item:last-child){margin-bottom:0}.role-information-form :deep(.ant-form-item-label){padding-bottom:8px}.role-information-form :deep(.ant-select){width:100%}
@media(min-width:1101px){.role-information{position:sticky;top:84px}.grant-tree-group{max-height:calc(100svh - 375px);min-height:300px;overflow:auto;overscroll-behavior:contain}}
</style>
