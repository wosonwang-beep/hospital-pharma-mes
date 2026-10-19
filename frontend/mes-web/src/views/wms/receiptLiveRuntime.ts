import incomingContract from '../quality/incoming-contract.json'
import { reactive } from 'vue'
import { api, errorMessage, type Page } from '../../api/http'
import { useAuthStore } from '../../stores/auth'

export type RawRecord = Record<string, any>
export const SOURCE_CONTRACT = 'RECEIPT_SOURCE_A_V1'
// Read the existing formal QMS enum instead of carrying the prototype-only HIGH value.
export const receiptRequestPriorityOptions = incomingContract.schemas.InspectionRequestCommand.properties.priority.enum.map(value => ({value, label: ({NORMAL:'常规',URGENT:'加急'} as Record<string,string>)[value] ?? value}))
export const localDate = () => new Intl.DateTimeFormat('sv-SE', { timeZone: 'Asia/Shanghai' }).format(new Date())
export const checkText = (value: boolean | null | undefined) => value == null ? '未检查' : value ? '通过' : '不通过'
export const checkValue = (value: string): boolean | null => value === 'PASS' ? true : value === 'FAIL' ? false : null
const str = (v: unknown) => v == null ? '' : String(v)

/** Display only: do not round stored decimal quantities or modify UTC audit facts. */
export function receiptDisplayQuantity(value: unknown): string {
 const raw = value == null ? '' : String(value)
 if (!raw) return '—'
 if (!/^-?\d+(\.\d+)?$/.test(raw)) return raw
 return raw.includes('.') ? raw.replace(/0+$/, '').replace(/\.$/, '') : raw
}
export function receiptDisplayTime(value: unknown): string {
 const raw = value == null ? '' : String(value)
 if (!raw) return '—'
 if (!/T.*(?:Z|[+-]\d{2}:?\d{2})$/.test(raw)) return raw
 const date = new Date(raw)
 if (!Number.isFinite(date.getTime())) return raw
 return new Intl.DateTimeFormat('sv-SE', {timeZone:'Asia/Shanghai',year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit',second:'2-digit',hourCycle:'h23'}).format(date)
}

/** Each live view owns its cache. There are no demo arrays, browser permissions or storage mutations. */
export function useReceiptLiveRuntime() {
  const auth = useAuthStore()
  const materialCache = reactive<Record<string, RawRecord>>({})
  const labels = reactive<Record<string, string>>({})
  const sourceCache = reactive<Record<string, RawRecord>>({})
  const unitIds = reactive<string[]>([])
  const locationRows = reactive<Record<string, RawRecord>>({})
  async function reference(resource: string, id: string): Promise<string> {
    if (!id) return '—'
    const key = resource + ':' + id
    if (labels[key]) return labels[key]!
    const row = await api<RawRecord>({ url: `/${resource}/${id}` })
    const prefix = ({ suppliers: 'supplier', warehouses: 'warehouse', units: 'unit', locations: 'location', containers: 'container' } as Record<string, string>)[resource]
    const name = str(prefix ? row[prefix + 'Name'] || row[prefix + 'Code'] : row.displayName || row.username || row.loginName)
    labels[key] = name || `编号 ${id}`
    return labels[key]!
  }
  function label(resource: string, id: string) { return labels[resource + ':' + id] || (id ? `编号 ${id}` : '—') }
  async function material(id: string) {
    if (!materialCache[id]) {
      const raw = await api<RawRecord>({ url: '/materials/' + id })
      materialCache[id] = { ...raw, code: str(raw.materialCode), name: str(raw.materialName), spec: str(raw.specification), pack: str(raw.packageSpec), unit: str(raw.baseUnitId) }
      for (const u of [raw.baseUnitId, raw.packUnitId]) if (u) await reference('units', str(u))
    }
    return materialCache[id]!
  }
  async function source(materialId: string, supplierId: string) {
    if (!materialId || !supplierId) return undefined
    const result = await api<RawRecord>({ url: '/wms/receipts/source-context', params: { materialId, supplierId } })
    const value = { ...result.source, manufacturer: str(result.source?.manufacturerName) }
    sourceCache[materialId + ':' + supplierId] = value
    return value
  }
  function sourceFor(mid: string, sid: string) { return sourceCache[mid + ':' + sid] }
  async function locations(warehouse: string) {
    if (!warehouse) return
    const result = await api<Page<RawRecord>>({ url: '/locations', params: { warehouseId: warehouse, page: 0, size: 100 } })
    for (const row of result.items) { locationRows[str(row.id)] = row; labels['locations:' + row.id] = str(row.locationName || row.locationCode) }
  }
  async function units() {
    if (!auth.can('master:uom:view')) return
    const first = await api<Page<RawRecord>>({ url: '/units', params: { page: 0, size: 100 } })
    const rows = [...first.items]
    for (let n = 1; n * 100 < first.total; n++) rows.push(...(await api<Page<RawRecord>>({ url: '/units', params: { page: n, size: 100 } })).items)
    unitIds.splice(0, unitIds.length, ...rows.filter(r => !r.status || r.status === 'ACTIVE').map(r => str(r.id)))
    for (const row of rows) labels['units:' + row.id] = str(row.unitName || row.unitCode)
  }
  async function mapReceipt(raw: RawRecord): Promise<RawRecord> {
    const r: RawRecord = { ...raw, id: str(raw.id), version: raw.versionNo, supplier: str(raw.supplierId), warehouse: str(raw.warehouseId), inputContract: raw.inputContract || null,
      receiptNo: str(raw.receiptNo), purchaseOrderNo: str(raw.purchaseOrderNo), deliveryNoteNo: str(raw.deliveryNoteNo), arrivalDate: str(raw.arrivalDate), receivedAt: str(raw.receivedAt),
      createdBy: str(raw.createdBy), createdAt: str(raw.createdAt), confirmedBy: str(raw.confirmedBy), confirmedAt: str(raw.confirmedAt), acceptanceText: str(raw.acceptanceText), audit: [] }
    const readName = async (resource: string, id: string) => { try { return await reference(resource, id) } catch { return id ? `编号 ${id}（名称读取受限）` : '—' } }
    r.supplierName = await readName('suppliers', r.supplier); r.warehouseName = await readName('warehouses', r.warehouse)
    r.createdBy = await readName('users', str(raw.createdBy)); r.confirmedBy = raw.confirmedBy ? await readName('users', str(raw.confirmedBy)) : ''
    r.items = await Promise.all((raw.items || []).map(async (v: RawRecord) => {
      const snap = v.materialSnapshot || {}, sourceSnap = v.sourceSnapshot || {}
      const it: RawRecord = { ...v, id: str(v.id), material: str(v.materialId), materialName: str(snap.materialName), materialCode: str(snap.materialCode), spec: str(snap.specification),
        manufacturerName: str(sourceSnap.manufacturerName), lotId: str(v.materialLotId), unit: str(v.unitId), location: str(v.locationId), container: str(v.containerId),
        lotNo: str(v.lotNo), supplierLotNo: str(v.supplierLotNo), manufacturerLotNo: str(v.manufacturerLotNo), receivedQty: str(v.receivedQty), manufactureDate: str(v.manufactureDate),
        expiryDate: str(v.expiryDate), retestDate: str(v.retestDate), packageSpec: str(v.packageSpec), packageCount: v.packageCount, saved: true }
      if (raw.recordStatus === 'DRAFT') {
        try { const m = await material(it.material); it.materialName ||= m.name; it.materialCode ||= m.code; it.spec ||= m.spec
          const src = await source(it.material, r.supplier); it.manufacturerName = src?.manufacturer || '' }
        catch (e) { it.sourceError = errorMessage(e) }
      }
      await readName('units', it.unit); await readName('locations', it.location)
      if (it.lotId && auth.can('qms:inspection-request:view')) {
        try {
          const requests = await api<Page<RawRecord>>({ url: '/quality/inspection-requests', params: { materialLotId: it.lotId, page: 0, size: 1 } })
          const q = requests.items[0]
          if (q) Object.assign(it, { requestId: str(q.id), requestNo: str(q.requestNo), requestStatus: q.status, requestQualitySpec: str(q.qcSpecificationVersionId),
            requestQuantity: str(q.requestedQuantity), requestUnit: str(q.requestedUnitId), requestPackages: q.requestedPackageCount, requestDate: q.requestedDate })
        } catch (e) { it.requestError = errorMessage(e) }
      }
      return it
    }))
    // Names and timestamps above are server facts; do not manufacture audit events from client actions.
    return r
  }
  function payload(r: RawRecord) {
    const keys = ['lotNo','supplierLotNo','manufacturerLotNo','manufactureDate','expiryDate','retestDate','receivedQty','packageSpec','packageCount','packageCheckPassed','sealCheckPassed','labelCheckPassed','damageCheckPassed','contaminationCheckPassed']
    return { receiptNo: r.receiptNo, supplierId: r.supplier, warehouseId: r.warehouse, purchaseOrderNo: r.purchaseOrderNo || null, deliveryNoteNo: r.deliveryNoteNo || null,
      transportCheckPassed: r.transportCheckPassed, ...(r.inputContract ? { inputContract: r.inputContract, arrivalDate: r.arrivalDate || null } : {}), ...(r.arrivalDate && !r.inputContract ? { arrivalDate: r.arrivalDate } : {}), ...(r.acceptanceText !== undefined ? { acceptanceText: r.acceptanceText } : {}),
      items: r.items.map((it: RawRecord) => ({ ...Object.fromEntries(keys.map(k => [k, it[k] === '' ? null : it[k] ?? null])), materialId: it.material, unitId: it.unit, locationId: it.location, containerId: it.container || null })) }
  }
  return { labels, label, reference, material, materialCache, source, sourceFor, locations, locationRows, unitIds, units, mapReceipt, payload }
}
