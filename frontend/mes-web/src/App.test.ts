import { fireEvent, render, screen } from '@testing-library/vue'
import Antd from 'ant-design-vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

test('renders the Foundation application shell', async () => {
  render(App, { global: { plugins: [createPinia(), router, Antd] } })
  await router.isReady()

  expect(screen.getByText('医院制剂生产管理系统')).toBeTruthy()
  expect(screen.getByRole('heading', { name: '基础服务已就绪' })).toBeTruthy()
  expect(screen.queryByText('登录')).toBeNull()

  const collapseButton = screen.getByRole('button', { name: '折叠侧栏' })
  await fireEvent.click(collapseButton)
  expect(screen.getByRole('button', { name: '展开侧栏' }).getAttribute('aria-expanded')).toBe('false')
})
