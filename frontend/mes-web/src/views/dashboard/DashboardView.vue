<script setup lang="ts">
import { CheckOutlined, ApiOutlined, DatabaseOutlined, InboxOutlined, RightOutlined } from '@ant-design/icons-vue'
const services=[{name:'API',icon:ApiOutlined},{name:'数据库配置',icon:DatabaseOutlined},{name:'对象存储配置',icon:InboxOutlined}]
</script>
<template><main class="dashboard"><section class="hero-panel"><div class="check"><CheckOutlined/></div><div><div class="section-label">系统基础状态</div><h1>基础服务已就绪</h1><p>系统基础环境、核心服务及相关组件运行正常，已完成初始化配置，可支持后续功能使用。</p></div></section><section class="service-panel"><div v-for="service in services" :key="service.name" class="service-row"><div class="service-icon"><component :is="service.icon"/></div><div class="service-copy"><strong>{{service.name}}</strong><span>基础服务已就绪</span></div><RightOutlined class="arrow"/></div></section></main></template>
