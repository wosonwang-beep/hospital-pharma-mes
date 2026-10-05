<script setup lang="ts">
import { ConfigProvider } from 'ant-design-vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'

// Ant components and CSS share the approved global visual tokens, including portals.
const css = getComputedStyle(document.documentElement)
const token = (name: string) => css.getPropertyValue(`--mes-ui-${name}`).trim()
const theme = { token: {
  colorPrimary: token('primary'), colorSuccess: token('success'),
  colorWarning: token('warning'), colorError: token('danger'),
  colorText: token('text'), colorTextSecondary: token('secondary'),
  colorBorder: token('border'), colorBgLayout: token('background'),
  colorBgContainer: token('surface'), borderRadius: Number.parseInt(token('radius')),
  fontFamily: token('font'), fontSize: Number.parseInt(token('body-size')),
  controlHeight: Number.parseInt(token('control-height')),
} }
</script>
<template><ConfigProvider :theme="theme" :locale="zhCN"><RouterView /></ConfigProvider></template>
