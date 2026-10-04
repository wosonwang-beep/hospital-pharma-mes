import {describe,it,expect} from 'vitest'
import {receiptPayload,blankReceipt,blankItem} from './model'
describe('receipt boundary',()=>{
 it('only submits approved input fields and exact decimal strings',()=>{
  const form={...blankReceipt(),receiptNo:'R1',supplierId:'7',warehouseId:'8',items:[{...blankItem(),materialId:'9007199254740993',lotNo:'L1',receivedQty:'0.000001',unitId:'9',locationId:'10',materialSnapshot:{materialName:'historical'},materialLotId:'42'}]}
  const commandBody=receiptPayload(form)
  expect(commandBody.items[0]?.materialId).toBe('9007199254740993');expect(commandBody.items[0]?.receivedQty).toBe('0.000001')
  expect(commandBody.items[0]).not.toHaveProperty('materialSnapshot');expect(commandBody.items[0]).not.toHaveProperty('materialLotId')
 })
 it('new receipt never assumes inspection checks passed',()=>{
  expect(blankReceipt().transportCheckPassed).toBe(false);expect(blankItem().packageCheckPassed).toBe(false)
 })
})
