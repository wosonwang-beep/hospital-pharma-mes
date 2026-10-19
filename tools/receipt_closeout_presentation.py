from pathlib import Path
import hashlib,json
from datetime import datetime
p=Path(r'D:\codex\_project\gmp\hospital-pharma-mes');e=p/'docs/review/evidence/RECEIPT_CLOSEOUT_20261019';b=e/'before-presentation';b.mkdir(exist_ok=True)
r=p/'frontend/mes-web/src/views/wms/receiptLiveRuntime.ts';v=p/'frontend/mes-web/src/views/wms/ReceiptLiveView.vue';t=p/'frontend/mes-web/src/views/wms/receiptLiveRuntime.test.ts'
for f in [r,v,t]:
 assert not (b/f.name).exists(),'already applied; inspect instead of overwrite'
 (b/f.name).write_bytes(f.read_bytes())
s=r.read_text(encoding='utf-8');anchor='/** Each live view owns its cache.';assert s.count(anchor)==1
helpers='''/** Display only: do not round stored decimal quantities or modify UTC audit facts. */
export function receiptDisplayQuantity(value: unknown): string {
 const raw = value == null ? '' : String(value)
 if (!raw) return '—'
 if (!/^-?\\d+(\\.\\d+)?$/.test(raw)) return raw
 return raw.includes('.') ? raw.replace(/0+$/, '').replace(/\\.$/, '') : raw
}
export function receiptDisplayTime(value: unknown): string {
 const raw = value == null ? '' : String(value)
 if (!raw) return '—'
 if (!/T.*(?:Z|[+-]\\d{2}:?\\d{2})$/.test(raw)) return raw
 const date = new Date(raw)
 if (!Number.isFinite(date.getTime())) return raw
 return new Intl.DateTimeFormat('sv-SE', {timeZone:'Asia/Shanghai',year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit',second:'2-digit',hourCycle:'h23'}).format(date)
}

'''
s=s.replace(anchor,helpers+anchor)
x=v.read_text(encoding='utf-8')
a='SOURCE_CONTRACT, localDate, checkText, checkValue, type RawRecord';assert x.count(a)==1;x=x.replace(a,'SOURCE_CONTRACT, localDate, checkText, checkValue, receiptDisplayQuantity, receiptDisplayTime, type RawRecord')
if '  createdAt?: string' not in x:x=x.replace('  receivedAt: string','  createdAt?: string\n  receivedAt: string',1)
changes={"{{ row.receivedAt }}":"{{ receiptDisplayTime(row.receivedAt) }}","{{current.arrivalDate||current.receivedAt.slice(0,10)}}":"{{current.arrivalDate||'未记录'}}","{{current.audit.find(a=>a.action==='创建收货单'||a.action==='保存草稿')?.time||current.receivedAt||'—'}}":"{{receiptDisplayTime(current.createdAt||current.receivedAt)}}","{{current.confirmedAt||'尚未确认'}}":"{{current.confirmedAt?receiptDisplayTime(current.confirmedAt):'尚未确认'}}","{{it.receivedQty + ' '}}":"{{receiptDisplayQuantity(it.receivedQty)}}","{{ activeItem.requestQuantity }}":"{{ receiptDisplayQuantity(activeItem.requestQuantity) }}"}
for old,new in changes.items():assert x.count(old)==1,(old,x.count(old));x=x.replace(old,new)
x=x.replace('</style>','\n.receipt-live .bpm-quantity{white-space:nowrap;word-break:normal;font-variant-numeric:tabular-nums}\n</style>')
tests=t.read_text(encoding='utf-8');tests += '''

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
'''
a="import { useReceiptLiveRuntime, SOURCE_CONTRACT }";assert a in tests;tests=tests.replace(a,"import { useReceiptLiveRuntime, SOURCE_CONTRACT, receiptDisplayQuantity, receiptDisplayTime }")
r.write_text(s,encoding='utf-8');v.write_text(x,encoding='utf-8');t.write_text(tests,encoding='utf-8')
(e/'presentation-change.json').write_text(json.dumps({'time':datetime.now().astimezone().isoformat(),'reason':'Actual screenshot quantity wraps and UTC timestamp leaks, historic arrival must not be invented','write_scope':[str(f.relative_to(p)) for f in [r,v,t]],'payload_changed':False,'database_changed':False,'before_hashes':{f.name:hashlib.sha256((b/f.name).read_bytes()).hexdigest() for f in [r,v,t]}},indent=2),encoding='utf-8')
print('PRESENTATION_ONLY_UPDATED; original stored quantity/time/arrival unchanged')
