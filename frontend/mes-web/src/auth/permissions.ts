export function canUseRoute(granted: readonly string[], menu: string): boolean {
  return granted.includes(menu)
}

export function canUseAction(granted: readonly string[], menu: string, action: string): boolean {
  return canUseRoute(granted, menu) && granted.includes(action)
}
