<script setup lang="ts">
import { HomeFilled, CodeSandboxOutlined, MedicineBoxOutlined, AuditOutlined, ClusterOutlined, ReconciliationOutlined, SettingOutlined } from '@ant-design/icons-vue'
import type { MenuNode } from './databaseNavigation'
defineOptions({ name: 'DatabaseMenu' })
defineProps<{ nodes: MenuNode[] }>()
const icons: Record<string, unknown> = { nav_master: CodeSandboxOutlined, nav_wms: MedicineBoxOutlined, nav_quality: AuditOutlined, nav_production: ClusterOutlined, nav_finished: ReconciliationOutlined, nav_system: SettingOutlined }
</script>
<template><template v-for="node in nodes" :key="node.id"><a-sub-menu v-if="node.children.length" :key="node.id" :class="`dashboard-menu-${node.menuCode.replace('nav_','')}`"><template #icon><component v-if="icons[node.menuCode]" :is="icons[node.menuCode]" /></template><template #title>{{node.title}}</template><DatabaseMenu :nodes="node.children" /></a-sub-menu><a-menu-item v-else-if="node.path" :key="node.id"><HomeFilled v-if="node.path==='/'" />{{node.title}}</a-menu-item></template></template>
