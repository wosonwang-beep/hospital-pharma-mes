import { render, screen } from '@testing-library/vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { registerAntDesign } from './ui/antDesign'

test('requires login before rendering administration', async () => {
  render(App, { global: { plugins: [createPinia(), router, registerAntDesign] } })
  await router.isReady()
  expect(await screen.findByRole('heading', { name: '医院制剂生产管理系统' })).toBeTruthy()
  expect(screen.getByRole('button', { name: '登录' })).toBeTruthy()
  expect(screen.queryByRole('heading', { name: '基础服务已就绪' })).toBeNull()
})
