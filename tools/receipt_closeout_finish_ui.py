from pathlib import Path
p=Path(r'D:\codex\_project\gmp\hospital-pharma-mes');f=p/'frontend/mes-web/src/views/wms/ReceiptLiveView.vue';e=p/'docs/review/evidence/RECEIPT_CLOSEOUT_20261019';s=f.read_text(encoding='utf-8');assert 'receipt-live-empty' not in s;(e/'ReceiptLiveView.before-final-ui.vue').write_bytes(f.read_bytes())
def rep(a,b):
 global s
 assert s.count(a)==1,(a[:90],s.count(a));s=s.replace(a,b)
rep('<td class="receipt-actions-cell"><button','<td class="receipt-actions-cell"><span class="receipt-row-actions"><button')
rep('@click="openEdit(row.id)">编辑</button></td>','@click="openEdit(row.id)">编辑</button></span></td>')
rep('                <tr v-if="!pageRows.length"><td colspan="8" class="receipt-empty">未找到符合条件的收货记录，请调整查询条件</td></tr>','')
rep('          <div class="receipt-list-foot"><span>共 {{ total }} 条记录</span>','          <div v-if="!pageRows.length" class="receipt-live-empty" role="status">未找到符合条件的收货记录，请调整查询条件</div>\n          <div class="receipt-list-foot"><span>共 {{ total }} 条记录</span>')
rep('            <div class="bpm-scroll"','            <p class="receipt-live-scroll-hint">明细表可左右滚动查看全部信息；点击校验提示可定位到对应字段。</p>\n            <div class="bpm-scroll"')
rep("target?.scrollIntoView({block:'center',inline:'nearest'})","target?.scrollIntoView({block:'center',inline:'center'})")
anchor="const requestErrors=ref<string[]>([]), requestBusy=ref(false)"
rep(anchor,anchor+"\nwatch(requestErrors,()=>nextTick(()=>{if(!requestOpen.value||!requestErrors.value.length)return;const alert=document.querySelector<HTMLElement>('.bpm-request-errors');alert?.closest('.ant-modal-body')?.scrollTo({top:0});alert?.scrollIntoView({block:'nearest'});alert?.setAttribute('tabindex','-1');alert?.focus({preventScroll:true})}))")
assert 'class="bpm-request-errors"' in s,'Need precise alert class first'
css='''
.receipt-live .receipt-actions-cell{display:table-cell;vertical-align:middle}
.receipt-live .receipt-row-actions{display:inline-flex;align-items:center;gap:10px}
.receipt-live .receipt-live-empty{padding:28px 16px;text-align:center;color:var(--mes-ui-secondary,#465c75);font-size:13px;overflow-wrap:anywhere}
.receipt-live .receipt-live-scroll-hint{font-size:12px;color:var(--mes-ui-secondary,#465c75);padding:0 20px;margin:0 0 10px}
.receipt-live .bpm-scroll{scroll-padding-right:100px;scroll-padding-left:12px}
@media(min-width:1586px){.receipt-live .receipt-live-scroll-hint{display:none}}
'''
s=s.replace('</style>',css+'</style>');f.write_text(s,encoding='utf-8');print('Four previously recorded source-adaptation UI defects repaired; no layout redesign or business payload changes.')
