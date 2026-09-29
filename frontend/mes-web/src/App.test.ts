import { render, screen } from '@testing-library/vue'
import Antd from 'ant-design-vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

test('requires the MES-002 login lifecycle before rendering the application shell', async () => {
  render(App, { global: { plugins: [createPinia(), router, Antd] } })
  await router.isReady()

  expect(screen.getByRole('heading', { name: '医院制剂 MES' })).toBeTruthy()
  expect(screen.getByLabelText('账号')).toBeTruthy()
  expect(screen.getByLabelText('密码')).toBeTruthy()
  expect(screen.getByRole('button', { name: /登\s*录/ })).toBeTruthy()
})
