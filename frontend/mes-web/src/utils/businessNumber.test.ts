import {it,expect} from 'vitest'
import {businessQuantity} from './businessNumber'
it('formats business decimals without changing precision, unit or stored value',()=>{
 expect(businessQuantity('400000.000000')).toBe('400,000')
 expect(businessQuantity('5000.120000')).toBe('5,000.12')
 expect(businessQuantity('0.00001000')).toBe('0.00001')
 expect(businessQuantity('-1000.0100')).toBe('-1,000.01')
 expect(businessQuantity(null)).toBe('—')
 expect(businessQuantity('unknown')).toBe('unknown')
})
