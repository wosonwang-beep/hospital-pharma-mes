import { describe, it, expect, vi, beforeEach } from 'vitest'
const mocks = vi.hoisted(() => ({ api: vi.fn(), permissions: new Set<string>() }))
vi.mock('../../api/http', () => ({ api: mocks.api, errorMessage: (e: unknown) => e instanceof Error ? e.message : '接口失败' }))
vi.mock('../../stores/auth', () => ({ useAuthStore: () => ({ can: (p: string) => mocks.permissions.has(p) }) }))
import { useReceiptLiveRuntime, SOURCE_CONTRACT, receiptDisplayQuantity, receiptDisplayTime, receiptRequestPriorityOptions } from './receiptLiveRuntime'

beforeEach(() => { mocks.api.mockReset(); mocks.permissions.clear() })
const input = () => ({ receiptNo: 'SH-20261018-0087', supplier: '3', warehouse: '2', inputContract: SOURCE_CONTRACT,
 arrivalDate: '2026-10-18', acceptanceText: '按实物核对随货资料', transportCheckPassed: null,
 items: [{ material: '7', lotNo: 'RM-20261018-00001', supplierLotNo: '', manufacturerLotNo: 'GL-20261008', unit: '4', location: '6',
 receivedQty: '12.125', packageCount: 1, packageCheckPassed: null, sealCheckPassed: false, labelCheckPassed: true }] })

describe('live receipt boundary', () => {
 it('keeps production batch separate from an absent supplier mark', () => {
  const payload = useReceiptLiveRuntime().payload(input())
  expect(payload.items[0].manufacturerLotNo).toBe('GL-20261008')
  expect(payload.items[0].supplierLotNo).toBeNull()
  expect(payload.items[0].lotNo).toBe('RM-20261018-00001')
 })
 it('uses real IDs, not entity names as foreign keys', () => {
  const p = useReceiptLiveRuntime().payload(input())
  expect(p.supplierId).toBe('3'); expect(p.warehouseId).toBe('2')
  expect(p.items[0]).toMatchObject({ materialId: '7', unitId: '4', locationId: '6' })
  expect(p.items[0]).not.toHaveProperty('materialName')
 })
 it('preserves unchecked, failed and passed independently', () => {
  const p = useReceiptLiveRuntime().payload(input())
  expect(p.transportCheckPassed).toBeNull()
  expect(p.items[0]).toMatchObject({ packageCheckPassed: null, sealCheckPassed: false, labelCheckPassed: true })
 })
 it('keeps actual arrival/acceptance notes separate from audit reasons', () => {
  const p = useReceiptLiveRuntime().payload({ ...input(), reason: '修改收货数量' })
  expect(p).toMatchObject({ inputContract: SOURCE_CONTRACT, arrivalDate: '2026-10-18', acceptanceText: '按实物核对随货资料' })
  expect(p).not.toHaveProperty('reason')
 })
 it('explicitly clears acceptance notes without changing legacy contract semantics', () => {
  const p = useReceiptLiveRuntime().payload({ ...input(), inputContract: null, arrivalDate: null, acceptanceText: '' })
  expect(p).toHaveProperty('acceptanceText', '')
  expect(p).not.toHaveProperty('inputContract')
  expect(p).not.toHaveProperty('arrivalDate')
 })
 it('does not implicitly upgrade historical contracts or invent arrival dates', () => {
  const p = useReceiptLiveRuntime().payload({ ...input(), inputContract: null, arrivalDate: null })
  expect(p).not.toHaveProperty('inputContract'); expect(p).not.toHaveProperty('arrivalDate')
 })
 it('does not substitute supplier name for missing manufacturer', async () => {
  mocks.api.mockResolvedValue({ material: { id: '7' }, source: { supplierId: '3', manufacturerName: null } })
  const r = useReceiptLiveRuntime(); const source = await r.source('7', '3')
  expect(source?.manufacturer).toBe(''); expect(r.sourceFor('7', '3')?.manufacturer).toBe('')
 })
 it('does not cache unapproved or failed source as valid', async () => {
  mocks.api.mockRejectedValue(new Error('SUPPLIER_NOT_APPROVED'))
  const r = useReceiptLiveRuntime()
  await expect(r.source('7', '3')).rejects.toThrow('SUPPLIER_NOT_APPROVED')
  expect(r.sourceFor('7', '3')).toBeUndefined()
 })
 it('keeps approved historic manufacturer and batch facts unchanged', async () => {
  mocks.api.mockImplementation(async ({ url }: { url: string }) => ({ id: url.split('/').pop(), supplierName: '供货企业', warehouseName: '原辅料仓库', unitName: '千克', locationCode: 'A-01', displayName: '王海燕' }))
  const r = await useReceiptLiveRuntime().mapReceipt({ id: '9', recordStatus: 'APPROVED', supplierId: '3', warehouseId: '2', createdBy: '1',
   items: [{ id: '8', materialId: '7', materialLotId: '10', materialSnapshot: { materialName: '药用甘油', materialCode: 'GLY', specification: '药用级' },
    sourceSnapshot: { manufacturerName: '湖南尔康制药股份有限公司' }, lotNo: 'ML-20261018', manufacturerLotNo: 'GL-20261008', unitId: '4', locationId: '6', packageCount: 1, receivedQty: '12.125' }] })
  expect(r.arrivalDate).toBe(''); expect(r.audit).toEqual([])
  expect(r.items[0]).toMatchObject({ manufacturerName: '湖南尔康制药股份有限公司', manufacturerLotNo: 'GL-20261008', lotNo: 'ML-20261018' })
  expect(mocks.api.mock.calls.some(([c]) => c.url.includes('source-context'))).toBe(false)
 })
})


describe('receipt display keeps stored data intact', () => {
 it('shows exact decimals without meaningless trailing zeroes or float rounding', () => {
  expect(receiptDisplayQuantity('5.500000')).toBe('5.5')
  expect(receiptDisplayQuantity('100.000000')).toBe('100')
  expect(receiptDisplayQuantity('999999999999.123456')).toBe('999999999999.123456')
  expect(receiptDisplayQuantity('0.000001')).toBe('0.000001')
  expect(receiptDisplayQuantity(null)).toBe('—')
 })
 it('displays UTC times in business timezone including month boundary', () => {
  expect(receiptDisplayTime('2026-10-18T22:24:40.519Z')).toBe('2026-10-19 06:24:40')
  expect(receiptDisplayTime('2026-10-31T16:00:00Z')).toBe('2026-11-01 00:00:00')
  expect(receiptDisplayTime('2026-10-19')).toBe('2026-10-19')
  expect(receiptDisplayTime(null)).toBe('—')
  expect(receiptDisplayTime('not-a-time')).toBe('not-a-time')
 })
})

 it('uses the existing QMS urgent code, never the prototype HIGH code',()=>{expect(receiptRequestPriorityOptions).toEqual([{value:'NORMAL',label:'常规'},{value:'URGENT',label:'加急'}])})
