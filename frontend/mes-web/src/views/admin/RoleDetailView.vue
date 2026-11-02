<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave, onBeforeRouteUpdate } from 'vue-router'
import { ArrowLeftOutlined, CompressOutlined, ExpandOutlined, InfoCircleOutlined, SearchOutlined } from '@ant-design/icons-vue'
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
function menuOrdinaryActions(id: string|number): Permission[] {
 const menu=menus.value.find(item=>item.id===String(id))
 return menu?menuPermissions(menu).filter(isOrdinary):[]
}
function menuLevel(id: string|number): string { return accessLevel(menuOrdinaryActions(id)) }
function menuLevelChange(id: string|number, level: string) {
 const menu=menus.value.find(m=>m.id===String(id))
 if(menu&&selectedMenus.value.includes(menu.menuCode))setAccess(menuOrdinaryActions(id),level)
}
const specialMenuGroups=computed(()=>{
 const linked=new Set<string>()
 const groups=menus.value.filter(m=>m.routePath).map(m=>{
  const items=specialPermissions(menuPermissions(m))
  items.forEach(item=>linked.add(item.permissionCode))
  return {key:m.menuCode,title:m.menuName,items}
 }).filter(group=>group.items.length>0)
 const unlinked=specialPermissions(permissions.value).filter(p=>!linked.has(p.permissionCode))
 if(unlinked.length) groups.push({key:'unlinked',title:'公共及兼容特殊权限',items:unlinked})
 const q=search.value.trim().toLowerCase()
 return groups.map(group=>({...group,items:group.items.filter(p=>
   (!selectedOnly.value||selected.value.includes(p.permissionCode))&&
   (!q||group.title.toLowerCase().includes(q)||permissionLabel(p).toLowerCase().includes(q)||p.permissionCode.toLowerCase().includes(q))
 )})).filter(group=>group.items.length>0)
})
const authorizationTree = computed(()=>{
 const q=search.value.trim().toLowerCase()
 function build(nodes: ReturnType<typeof catalogTree>): GrantNode[] {
  return nodes.flatMap(node=>{
    const menu=node.entry
    const children=build(node.children)
    const matches=!q||node.title.toLowerCase().includes(q)||menu.menuCode.toLowerCase().includes(q)||
      menuPermissions(menu).some(p=>(permissionLabel(p)+' '+p.permissionCode).toLowerCase().includes(q))
    const selectedMenu=selectedMenus.value.includes(menu.menuCode)
    if(q&&!matches&&!children.length)return []
    if(selectedOnly.value&&!selectedMenu&&!children.length)return []
    return [{key:node.key,title:node.title,kind:menu.routePath?'菜单':'目录',children}]
  })
 }
 return build(catalogTree(menus.value))
})
function expandTree(keys:(string|number)[]){expandedKeys.value=keys.map(String)}
function hasView(items:Permission[]){return items.some(p=>p.permissionCode.endsWith(':view'))}
function hasEditing(items:Permission[]){return items.some(p=>!p.permissionCode.endsWith(':view'))}
const expandedKeys=ref<string[]>([])
const allBranchKeys=computed(()=>{const keys:string[]=[];function visit(nodes:GrantNode[]){for(const n of nodes){if(n.children.length)keys.push(n.key);visit(n.children)}}visit(authorizationTree.value);return keys})
const expandedInitialized=ref(false)
watch(allBranchKeys,keys=>{if(!expandedInitialized.value&&keys.length){expandedKeys.value=[...keys];expandedInitialized.value=true}}, {immediate:true})
const checkedMenuKeys = computed(() => menus.value.filter(m=>m.routePath ? selectedMenus.value.includes(m.menuCode) : descendants(m.id,menus.value).filter(child=>child.routePath).length>0 && descendants(m.id,menus.value).filter(child=>child.routePath).every(child=>selectedMenus.value.includes(child.menuCode))).map(m=>m.id))
function chooseMenu(keys: (string|number)[]) { const key = String(keys[0] ?? ''); if (menus.value.find(m=>m.id===key)?.routePath || key==='unlinked') activeDomain.value=key }
function changeMenu(_keys: unknown, info: {node: {key: string|number}; checked: boolean}) {
  if (!canAssign.value || readonly.value || busy.value) return
  const codes = new Set(selectedMenus.value), grants = new Set(selected.value)
  const affected = descendants(String(info.node.key),menus.value).filter(m=>m.routePath && m.status==='ACTIVE')
  for (const menu of affected) {
    if (info.checked) {
      const newlyVisible = !codes.has(menu.menuCode)
      codes.add(menu.menuCode)
      // A newly visible menu gets ordinary management only. Explicitly gated
      // approval, release, signature and other special grants are never inferred.
      if (newlyVisible) {
        for (const permission of menuPermissions(menu).filter(isOrdinary)) grants.add(permission.permissionCode)
        if (menu.permissionCode?.endsWith(':view')) grants.add(menu.permissionCode)
        for (const required of (menu.requiredPermissions ?? '').split(',').map(s=>s.trim()).filter(Boolean)) if (required.endsWith(':view')) grants.add(required)
      }
    }
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
  selectedMenus.value=selectedMenus.value.filter(code=>{const m=menus.value.find(m=>m.menuCode===code);return !m || !m.permissionCode || codes.has(m.permissionCode)})
}
function visibleMenu(key: string) { const menu=menus.value.find(m=>m.id===key);return !menu || selectedMenus.value.includes(menu.menuCode) }
function accessChanged(items: Permission[], event: { target: { value: string } }) { setAccess(items, event.target.value) }
function supportedActions(items: Permission[]) { return [...new Set(items.map(p => actions[p.permissionCode.split(':')[2]!] ?? ({ delete: '删除', remove: '删除' } as Record<string, string>)[p.permissionCode.split(':')[2]!] ?? '操作'))].join('、') }
const accessNames: Record<string, string> = { none: '未授权', view: '只查看', crud: '管理', custom: '已有自定义授权' }
const added = computed(() => selected.value.filter(code => !(role.value?.permissionCodes ?? []).includes(code)))
const removed = computed(() => (role.value?.permissionCodes ?? []).filter(code => !selected.value.includes(code)))
function labelFor(code: string) { const item = permissions.value.find(p => p.permissionCode === code); return item ? permissionLabel(item) : code }
const menuDescriptions: Record<string, string> = {
  '首页': '系统首页', '基础管理': '物料、产品与基础资料', 'WMS管理': '收货、库存、领料与出库',
  '质量管理': '请验、取样、检验与报告', '生产管理': '生产计划与执行',
  '成品管理': '成品入库、放行与发货', '系统管理': '系统基础管理',
  '原辅料收货记录': '原辅料收货与入库', '库存管理': '库存查询与管理',
  '领料申请': '生产领料申请', '出库管理': '物料出库', '退料管理': '生产退料管理'
}
function menuDescription(node: GrantNode) { return menuDescriptions[node.title] ?? node.title }
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
async function save(returnToList = false) {
  if (busy.value || loading.value || readonly.value || !canSave.value || !baseline.value) return
  error.value = ''; notice.value = ''
  if (!roleName.value.trim()) { error.value = '请输入角色名称'; return }
  if (roleName.value.trim().length > 128) { error.value = '角色名称不能超过 128 个字符'; return }
  if (isCreate.value && !/^[A-Z][A-Z0-9_]{1,127}$/.test(roleCode.value.trim())) { error.value = '角色编码须为 2–128 位大写字母、数字或下划线，并以字母开头'; return }
  // A visible menu may require a separately approved special permission.
  // Never grant it implicitly or silently drop the menu during save.
  const missing = selectedMenus.value.flatMap(code => {
    const menu=menus.value.find(m=>m.menuCode===code)
    return menu ? [menu.permissionCode,...(menu.requiredPermissions??'').split(',')].filter((p):p is string=>!!p && !!p.trim() && !selected.value.includes(p.trim())).map(p=>`${menu.menuName}：${p.trim()}`) : []
  })
  if (missing.length) { error.value=`已选择的菜单缺少必要权限，请在独立的特殊权限区显式授权：${missing.join('；')}`;return }
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
      const eligibleMenus = [...selectedMenus.value]
      role.value = await api<Role>({ method: 'POST', url: `/roles/${role.value.id}/permissions`, headers: { 'Idempotency-Key': idempotencyKey(), 'If-Match': `"${role.value.version}"` }, data: { permissionCodes: selected.value, menuCodes: eligibleMenus, reason: '角色配置维护（系统记录）' } })
    }
    if (role.value) { roleCode.value = role.value.roleCode; roleName.value = role.value.roleName; status.value = role.value.status; selected.value = [...role.value.permissionCodes]; selectedMenus.value=[...role.value.menuCodes] }
    reason.value = ''; baseline.value = state(); notice.value = '角色及权限已保存'
    if (returnToList) await router.push({ path: '/admin/roles', query: route.query })
    else if (isCreate.value && role.value) await router.replace({ path: `/admin/roles/${role.value.id}/edit`, query: route.query })
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
    <header class="admin-page-header role-page-heading">
      <div class="role-title-with-back"><a-button type="text" aria-label="返回角色列表" :disabled="busy" @click="router.push({ path: '/admin/roles', query: route.query })"><ArrowLeftOutlined /></a-button><h1>{{ isCreate ? '新增角色' : readonly ? '角色详情' : '编辑角色' }}</h1></div>
      <span v-if="dirty" class="role-unsaved" role="status">有未保存的修改</span>
      <a-button v-if="readonly && auth.can('iam:role:update') && role" type="primary" @click="router.push({ path: `/admin/roles/${role.id}/edit`, query: route.query })">编辑</a-button>
    </header>
    <a-alert v-if="error" type="error" :message="error" show-icon />
    <a-alert v-if="notice" type="success" :message="notice" show-icon />
    <a-spin :spinning="loading"><div class="role-authorization-layout"><section class="role-surface role-basic-info role-information">
        <h2>基本信息</h2>
        <a-form layout="horizontal" class="role-information-form">
          <a-form-item label="角色名称" :required="!readonly"><a-input v-model:value="roleName" aria-label="角色名称" :disabled="readonly || busy" :maxlength="128" placeholder="请输入清晰的岗位名称" /></a-form-item>
          <a-form-item label="角色编码" :required="isCreate"><a-input v-model:value="roleCode" aria-label="角色编码" :disabled="readonly || !isCreate || !!role || busy" :maxlength="128" placeholder="例如 WAREHOUSE_OPERATOR" /></a-form-item>
          <a-form-item label="状态"><a-select show-search option-filter-prop="children" v-model:value="status" aria-label="状态" :disabled="readonly || isCreate || busy"><a-select-option value="ACTIVE">启用</a-select-option><a-select-option value="INACTIVE">停用</a-select-option></a-select></a-form-item>
        </a-form>
    </section>
      <section class="role-surface role-permissions-card form-section">
        <a-alert v-if="!auth.can('iam:permission:view')" type="warning" message="当前账号没有查看权限目录的权限；已有权限不会被清空。" show-icon />
        <template v-else>
          <div class="role-permission-heading"><div class="role-permission-title"><h2>菜单权限</h2><span class="role-helper"><InfoCircleOutlined /> 勾选菜单即可见；新勾选默认“管理”</span></div>
            <div class="role-tree-tools"><label class="role-search-pair"><span>菜单搜索</span><a-input v-model:value="search" aria-label="搜索权限" allow-clear placeholder="搜索菜单名称"><template #prefix><SearchOutlined /></template></a-input></label><a-button :aria-label="expandedKeys.length?'收起全部':'展开全部'" @click="expandedKeys=expandedKeys.length?[]:allBranchKeys"><CompressOutlined v-if="expandedKeys.length" /><ExpandOutlined v-else />{{expandedKeys.length?'收起全部':'展开全部'}}</a-button></div>
          </div>
          <span class="role-sr-only permission-summary">已选 {{ selected.length }} 项 / 可用 {{ permissions.length }} 项</span>
          <a-alert v-if="unavailable.length" type="warning" :message="`已选权限中有 ${unavailable.length} 项未在可用目录展示，保存时会保留。`" show-icon />
          <details v-if="unavailable.length" class="unavailable-permissions"><summary>查看未展示的已选权限</summary><ul><li v-for="code in unavailable" :key="code">{{ code }}</li></ul></details>
          <a-empty v-if="!loading && !permissionGroups.length" :description="search ? '没有匹配的权限，请调整搜索条件' : selectedOnly ? '没有已选的可用权限' : '暂无可用权限'" />
          <a-alert v-if="!auth.can('iam:menu:view')" type="warning" message="当前账号没有查看菜单目录的权限，已有菜单授权会保持。" show-icon />
          <a-checkbox-group v-model:value="visibleSelection" :disabled="readonly || !canAssign || busy" class="grant-tree-group">
            <div class="role-tree-scroll"><div class="role-permission-table-head" aria-hidden="true"><span>菜单名称</span><span>功能权限 <InfoCircleOutlined /></span><span>操作说明</span></div><a-tree aria-label="角色菜单授权树" :tree-data="authorizationTree" :checkable="!readonly && canAssign" :checked-keys="checkedMenuKeys" check-strictly :expanded-keys="expandedKeys" :disabled="busy" block-node @expand="expandTree" @check="changeMenu">
              <template #title="node"><div class="grant-node" @click.stop>
                <template v-if="node.kind==='菜单'"><strong>{{node.title}}</strong>
                  <a-radio-group v-if="menuOrdinaryActions(node.key).length" class="role-access-level" :value="menuLevel(node.key)" :aria-label="`${node.title}功能权限`" :disabled="readonly||!canAssign||busy||!selectedMenus.includes(menus.find(m=>m.id===String(node.key))?.menuCode??'')" @change="(e:any)=>menuLevelChange(node.key,e.target.value)">
                    <a-radio value="view" :disabled="!hasView(menuOrdinaryActions(node.key))">只查看</a-radio>
                    <a-radio value="crud" :disabled="!hasEditing(menuOrdinaryActions(node.key))">管理</a-radio>
                  </a-radio-group>
                  <span v-else class="role-directory-summary">仅含独立特殊权限</span><span class="role-menu-description">{{menuDescription(node)}}</span>
                </template>
                <template v-else><strong>{{node.title}}</strong><span class="role-directory-summary">目录</span><span class="role-menu-description">{{menuDescription(node)}}</span></template>
              </div></template>
            </a-tree></div>
            <details class="role-special-section" open>
              <summary>特殊操作权限 <span class="muted">（独立授权，不随普通管理自动勾选）</span></summary>
              <div class="role-special-groups">
                <div v-for="group in specialMenuGroups" :key="group.key" class="role-special-group">
                  <strong>{{group.title}}</strong>
                  <a-checkbox v-for="permission in group.items" :key="permission.permissionCode" :value="permission.permissionCode" :disabled="readonly||!canAssign||busy||(group.key!=='unlinked'&&!selectedMenus.includes(group.key))">
                    {{permissionLabel(permission)}}
                  </a-checkbox>
                </div>
              </div>
              <p v-if="!specialMenuGroups.length" class="muted">当前筛选范围没有特殊权限。</p>
            </details>
          </a-checkbox-group>
        </template>
      </section>
      <footer v-if="!readonly" class="role-actionbar"><div class="role-footer-buttons"><a-button type="primary" :loading="busy" :disabled="loading || !baseline || !canSave" @click="save()">保存</a-button><a-button :loading="busy" :disabled="loading || !baseline || !canSave" @click="save(true)">保存并返回</a-button><a-button :disabled="busy || !dirty" @click="load">重置</a-button><a-button :disabled="busy" @click="router.push({ path: '/admin/roles', query: route.query })">取消</a-button></div></footer>
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

<style scoped>
/* Approved role permission T2: horizontal business fields and a full-width menu tree. */
.role-authorization-layout{display:flex;flex-direction:column;gap:16px}
.role-information{width:100%;position:static}
.role-information-form{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:0 20px}
.role-information-form :deep(.ant-form-item){display:block;margin-bottom:12px}
.role-information-form :deep(.ant-form-item-row){display:flex;align-items:center;gap:12px}
.role-information-form :deep(.ant-form-item-label){width:96px;flex-shrink:0;padding:0;text-align:left}
.role-information-form :deep(.ant-form-item-control){flex:1;min-width:0}
.role-information .form-section{margin-bottom:0}
.grant-tree-group{max-height:none;overflow:visible}
.grant-node{width:100%;display:flex;align-items:center;gap:14px;flex-wrap:nowrap}
.grant-node>strong{min-width:150px;flex:1}
.role-access-level{display:inline-flex;align-items:center;gap:14px;flex-shrink:0}
.role-access-level :deep(.ant-radio-wrapper){font-size:13px;white-space:nowrap;margin:0}
.role-tree-scroll{width:100%;max-width:100%;min-width:0;overflow-x:auto}
.role-tree-scroll :deep(.ant-tree){min-width:390px;width:100%}
.role-authorization-layout>.form-section{min-width:0;width:100%}
.role-special-section{margin-top:18px;border-top:1px solid #e5edf5;padding-top:12px}
.role-special-section summary{cursor:pointer;font-weight:600;font-size:14px}
.role-special-groups{display:grid;gap:12px;padding:14px 0}
.role-special-group{display:flex;flex-wrap:wrap;align-items:center;gap:12px;padding-bottom:10px;border-bottom:1px solid #f0f2f6}
.role-special-group>strong{min-width:140px;font-size:13px}
.role-special-group :deep(.ant-checkbox-wrapper){font-size:13px;white-space:normal}
.role-save-bar{display:flex;justify-content:flex-end;gap:16px;align-items:center}
@media(max-width:900px){.role-information-form{grid-template-columns:1fr}.grant-node>strong{min-width:92px}.role-special-group>strong{width:100%}}
@media(max-width:580px){.role-access-level{gap:7px}.grant-node{gap:8px}.role-tree-scroll :deep(.ant-tree){min-width:390px}.role-information-form :deep(.ant-form-item-row){display:flex!important;flex-direction:row!important;flex-wrap:nowrap!important;align-items:center!important}.role-information-form :deep(.ant-form-item-label){width:78px!important;flex:0 0 78px!important;padding:0!important;text-align:left!important}.role-information-form :deep(.ant-form-item-control){min-width:0!important;flex:1!important}.role-save-bar{justify-content:space-between}}
</style>

<style scoped>
.role-detail-page{--role-ink:#162c4b;--role-muted:#70829b;--role-line:#e5edf5;--role-blue:#1677ff;color:var(--role-ink);font-size:14px;min-width:0}
.role-sr-only{position:absolute!important;width:1px!important;height:1px!important;padding:0!important;margin:-1px!important;overflow:hidden!important;clip:rect(0,0,0,0)!important;white-space:nowrap!important;border:0!important}
.role-page-heading{display:flex;align-items:center;justify-content:space-between;gap:16px;margin:0 0 20px;min-height:32px}
.role-title-with-back{display:flex;align-items:center;gap:12px}.role-title-with-back>.ant-btn{padding:0;width:30px;color:var(--role-ink)}.role-title-with-back h1{margin:0;font-size:22px;font-weight:650;letter-spacing:.2px}.role-unsaved{font-size:12px;color:#8b670a}
.role-surface{background:#fff;border:1px solid var(--role-line);border-radius:9px;margin-bottom:16px;min-width:0}.role-surface h2{font-size:16px;line-height:24px;font-weight:600;margin:0;color:var(--role-ink)}
.role-basic-info{display:block!important;padding:20px}.role-basic-info h2{margin-bottom:20px}.role-information-form{display:grid;grid-template-columns:1fr 1fr minmax(170px,.65fr);gap:32px;align-items:start}.role-information-form :deep(.ant-form-item){margin:0;min-width:0}.role-information-form :deep(.ant-form-item-row){display:flex;align-items:center;gap:12px;flex-wrap:nowrap}.role-information-form :deep(.ant-form-item-label){width:auto;flex:0 0 auto;padding:0;text-align:left}.role-information-form :deep(.ant-form-item-label>label){height:36px}.role-information-form :deep(.ant-form-item-control){flex:1;min-width:0}.role-information-form :deep(.ant-input),.role-information-form :deep(.ant-select-selector){height:36px}.role-information-form :deep(.ant-select){width:100%}
.role-permissions-card{padding:0 20px}.role-permission-heading{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;padding:16px 0}.role-permission-title{display:flex;align-items:center;gap:18px;flex-wrap:wrap}.role-helper{font-size:12px;color:var(--role-muted);display:flex;align-items:center;gap:6px}.role-tree-tools{display:flex;align-items:center;gap:12px;margin-left:auto}.role-search-pair{display:flex;align-items:center;gap:12px;width:285px;font-size:13px;white-space:nowrap}.role-search-pair :deep(.ant-input-affix-wrapper){height:36px;flex:1;min-width:0}.role-tree-tools>.ant-btn{height:36px;color:#536a88}
.role-tree-scroll{width:100%;max-width:100%;min-width:0;max-height:min(620px,calc(100dvh - 352px));overflow:auto;scrollbar-width:thin;scrollbar-color:#cad6e3 transparent;outline-offset:2px}.role-permission-table-head{position:sticky;top:0;z-index:3;display:grid;grid-template-columns:46% 28% 26%;height:40px;align-items:center;background:#f3f6fa;border-bottom:1px solid var(--role-line);font-size:13px;font-weight:600;min-width:720px}.role-permission-table-head>span{padding:0 20px}.role-tree-scroll :deep(.ant-tree){min-width:720px;width:100%;background:#fff}.role-tree-scroll :deep(.ant-tree-treenode){display:flex;align-items:center;width:100%;min-height:44px;padding:0;border-bottom:1px solid #edf1f6}.role-tree-scroll :deep(.ant-tree-switcher){align-self:stretch;display:flex;align-items:center;justify-content:center;line-height:44px}.role-tree-scroll :deep(.ant-tree-checkbox){margin-top:0;align-self:center}.role-tree-scroll :deep(.ant-tree-node-content-wrapper){display:flex;align-items:center;flex:1;min-width:0;padding:0 20px 0 8px!important}.role-tree-scroll :deep(.ant-tree-title){width:100%;min-width:0}
.grant-node{width:100%;display:grid;grid-template-columns:minmax(0,46fr) minmax(210px,28fr) minmax(0,26fr);align-items:center;gap:0;min-height:43px;font-size:13px}.grant-node>strong{min-width:0;font-weight:500;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.role-access-level{display:flex;align-items:center;gap:20px;white-space:nowrap}.role-access-level :deep(.ant-radio-wrapper){margin:0;min-height:34px;align-items:center;font-size:13px}.role-directory-summary{color:#8998ad;font-size:12px}.role-menu-description{color:var(--role-muted);font-size:13px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;padding-left:12px}
.role-special-section{border-top:1px solid var(--role-line);margin-top:12px;padding:12px 0;font-size:13px}.role-special-section summary{min-height:24px;line-height:24px;color:#516985}.role-special-groups{padding-bottom:4px}.permission-change-preview{display:none}.role-actionbar{display:flex;align-items:center;justify-content:flex-end;gap:12px;position:static;background:#fff;border-top:1px solid var(--role-line);padding:16px 20px;margin:16px 0 0}.role-actionbar .ant-btn{height:36px;min-width:88px}.role-footer-buttons{display:flex;gap:12px;align-items:center;justify-content:flex-end;flex-wrap:nowrap}.role-footer-buttons .ant-btn-primary{min-width:100px}
@media(max-width:1280px){.role-information-form{gap:20px;grid-template-columns:1fr 1fr 170px}.role-permission-title{gap:12px}.role-search-pair{width:265px}.role-permission-table-head>span{padding-left:14px;padding-right:12px}.role-tree-scroll{max-height:520px}.role-access-level{gap:14px}}
@media(max-width:1000px){.role-information-form{grid-template-columns:1fr 1fr}.role-information-form>.ant-form-item:last-child{max-width:270px}.role-permission-heading{align-items:flex-start}.role-tree-tools{width:100%;margin-left:0}.role-search-pair{width:310px}.role-actionbar{margin-left:0;margin-right:0}.role-tree-scroll{max-height:560px}}
@media(max-width:700px){.role-title-with-back h1{font-size:20px}.role-page-heading{margin-bottom:16px}.role-basic-info{padding:16px}.role-information-form{grid-template-columns:1fr;gap:14px}.role-information-form>.ant-form-item:last-child{max-width:none}.role-information-form :deep(.ant-form-item-label){width:78px;flex:0 0 78px}.role-permissions-card{padding:0 12px}.role-permission-title{gap:8px}.role-helper{line-height:20px}.role-tree-tools{gap:8px;flex-wrap:wrap}.role-search-pair{width:100%}.role-tree-tools>.ant-btn{height:32px}.role-permission-table-head{grid-template-columns:54% 46%;min-width:344px}.role-permission-table-head>span:nth-child(3),.role-menu-description{display:none}.role-tree-scroll :deep(.ant-tree){min-width:344px}.grant-node{grid-template-columns:minmax(0,54fr) minmax(150px,46fr)}.role-tree-scroll :deep(.ant-tree-node-content-wrapper){padding-right:6px!important}.role-access-level{gap:10px}.role-access-level :deep(.ant-radio-wrapper){font-size:12px}.role-actionbar{margin-left:0;margin-right:0;padding:12px;gap:8px;flex-wrap:wrap}.role-actionbar .ant-btn{min-width:56px;padding-inline:10px}.role-footer-buttons{gap:8px}.role-unsaved{font-size:11px}}
@media(prefers-reduced-motion:reduce){.role-detail-page *{animation:none!important;transition:none!important;scroll-behavior:auto!important}}
</style>
