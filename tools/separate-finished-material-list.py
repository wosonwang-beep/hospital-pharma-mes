from pathlib import Path
root=Path(r'D:\codex\_project\gmp\hospital-pharma-mes')
p=root/'backend/mes-masterdata/src/main/java/com/hospital/mes/masterdata/application/ScopedStore.java'
s=p.read_text(encoding='utf-8')
needle='        filters.forEach((key,col)->{String val=params.get(key);if(val!=null&&!val.isBlank())filter(q,key,col,val);});'
assert needle in s
s=s.replace(needle,needle+'\n        applyAdditionalFilters(q,params);',1)
needle2='    protected void filter(QueryWrapper<E> query,String key,String column,String value)'
assert needle2 in s
s=s.replace(needle2,'    protected void applyAdditionalFilters(QueryWrapper<E> query,Map<String,String> params){}\n'+needle2,1)
p.write_text(s,encoding='utf-8')
p=root/'backend/mes-masterdata/src/main/java/com/hospital/mes/masterdata/infrastructure/MaterialStore.java'
s=p.read_text(encoding='utf-8')
needle=' @Override protected void filter('
assert needle in s
s=s.replace(needle,''' @Override protected void applyAdditionalFilters(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MaterialEntity> query,java.util.Map<String,String> params){
  if("true".equals(params.get("excludeFinished")))query.notIn("material_type",java.util.List.of("FINISHED","成品"));
 }
'''+needle,1)
p.write_text(s,encoding='utf-8')
p=root/'frontend/mes-web/src/views/master/MasterListView.vue'
s=p.read_text(encoding='utf-8')
needle='const params={keyword:keyword.value||undefined,...Object.fromEntries(Object.entries(query).filter(([,v])=>v)),page,size:20,sort:sort.value}'
assert needle in s
s=s.replace(needle,'const params={keyword:keyword.value||undefined,...Object.fromEntries(Object.entries(query).filter(([,v])=>v)),...(def.value.key===\'materials\'?{excludeFinished:true}:{}),page,size:20,sort:sort.value}',1)
p.write_text(s,encoding='utf-8')
print('Added opt-in material list exclusion for finished goods; no database changes')
