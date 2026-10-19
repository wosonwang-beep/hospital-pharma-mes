from pathlib import Path
p=Path(r'D:\codex\_project\gmp\hospital-pharma-mes\frontend\mes-web\src\master\material.test.ts')
s=p.read_text(encoding='utf-8')
needle="describe('basic material and one preferred supplier',()=>{"
assert needle in s
tests="""describe('material shelf life in days',()=>{
 it('accepts integer days and preserves unit semantics',()=>{
  expect(materialPayload({...form(),shelfLifeDays:365}).shelfLifeDays).toBe(365)
  expect(materialPayload({...form(),shelfLifeDays:'24'},true).shelfLifeDays).toBe(24)
 })
 it('rejects zero, negative, fraction, over-limit and invalid shelf life',()=>{
  for(const value of [0,-1,1.5,36501,'abc'])expect(()=>materialPayload({...form(),shelfLifeDays:value})).toThrow()
 })
 it('allows legacy materials without shelf life',()=>{
  expect(materialPayload({...form(),shelfLifeDays:null}).shelfLifeDays).toBeNull()
 })
})
"""
s=s.replace(needle,tests+needle)
p.write_text(s,encoding='utf-8')
print('shelf life tests added')
