import { canUseRoute, canUseAction } from './permissions'

test('a menu permission does not grant mutation access', () => {
  const granted = ['menu:iam:users']
  expect(canUseRoute(granted, 'menu:iam:users')).toBe(true)
  expect(canUseAction(granted, 'menu:iam:users', 'action:iam:user.manage')).toBe(false)
})

test('a role action without its module menu cannot expose controls', () => {
  expect(canUseAction(['action:iam:role.manage'], 'menu:iam:roles', 'action:iam:role.manage')).toBe(false)
})
