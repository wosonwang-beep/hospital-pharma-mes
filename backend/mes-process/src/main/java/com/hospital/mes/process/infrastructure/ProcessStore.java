package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.masterdata.application.ScopedStore;
import java.util.*;
@org.springframework.stereotype.Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProcessStore {
 private final ScopedStore<ProductEntity> products;private final ScopedStore<PackageEntity> packages;private final ScopedStore<VersionEntity> versions;private final ScopedStore<FormulaEntity> formulas;private final ScopedStore<FormulaItemEntity> items;private final ScopedStore<RouteEntity> routes;private final ScopedStore<OperationEntity> operations;private final ScopedStore<ParameterEntity> parameters;
 public ScopedStore<ProductEntity> products(){return products;}
 public ScopedStore<PackageEntity> packages(){return packages;}
 public ScopedStore<VersionEntity> versions(){return versions;}
 public ScopedStore<FormulaEntity> formulas(){return formulas;}
 public ScopedStore<FormulaItemEntity> items(){return items;}
 public ScopedStore<RouteEntity> routes(){return routes;}
 public ScopedStore<OperationEntity> operations(){return operations;}
 public ScopedStore<ParameterEntity> parameters(){return parameters;}
 private final PackageMapper packageMapper;private final VersionMapper vm;private final FormulaMapper fm;private final FormulaItemMapper im;private final RouteMapper rm;private final OperationMapper om;private final ParameterMapper pm;
 public ProcessStore(ProductMapper products,PackageMapper packages,VersionMapper vm,FormulaMapper fm,FormulaItemMapper im,RouteMapper rm,OperationMapper om,ParameterMapper pm){
  this.products=new ScopedStore<>(products,List.of("product_code","product_name","specification"),Map.of("status","status"));this.packages=new ScopedStore<>(packages,List.of("package_code"),Map.of("productId","product_id"));this.versions=new ScopedStore<>(vm,List.of("status"),Map.of("status","status","version","version"));this.formulas=new ScopedStore<>(fm,List.of(),Map.of());this.items=new ScopedStore<>(im,List.of(),Map.of());this.routes=new ScopedStore<>(rm,List.of(),Map.of());this.operations=new ScopedStore<>(om,List.of(),Map.of());this.parameters=new ScopedStore<>(pm,List.of(),Map.of());this.packageMapper=packages;this.vm=vm;this.fm=fm;this.im=im;this.rm=rm;this.om=om;this.pm=pm;
 }
 public List<VersionEntity> versions(long org,long pkg){return vm.selectList(new QueryWrapper<VersionEntity>().eq("org_id",org).eq("package_id",pkg).orderByDesc("version"));}
 public List<VersionEntity> matchingVersions(long org,String state,String version,String effective){var q=new QueryWrapper<VersionEntity>().eq("org_id",org);if(state!=null&&!state.isBlank())q.eq("status",state);if(version!=null&&!version.isBlank())q.eq("version",Integer.parseInt(version));if(effective!=null&&!effective.isBlank())q.ge("effective_from",java.time.LocalDateTime.ofInstant(java.time.Instant.parse(effective),java.time.ZoneOffset.UTC));return vm.selectList(q);}
 public FormulaEntity formula(long org,long v){return fm.selectOne(new QueryWrapper<FormulaEntity>().eq("org_id",org).eq("package_version_id",v));}
 public RouteEntity route(long org,long v){return rm.selectOne(new QueryWrapper<RouteEntity>().eq("org_id",org).eq("package_version_id",v));}
 public List<FormulaItemEntity> items(long org,long formula){return im.selectList(new QueryWrapper<FormulaItemEntity>().eq("org_id",org).eq("formula_version_id",formula).orderByAsc("line_no"));}
 public List<OperationEntity> operations(long org,long route){return om.selectList(new QueryWrapper<OperationEntity>().eq("org_id",org).eq("route_version_id",route).orderByAsc("sequence_no","operation_code"));}
 public List<ParameterEntity> parameters(long org,long op){return pm.selectList(new QueryWrapper<ParameterEntity>().eq("org_id",org).eq("operation_def_id",op).orderByAsc("parameter_code"));}
 public ScopedStore.PageData<PackageEntity> packagePage(long org,int page,int size,String keyword,Map<String,String> filters){
  if(page<0||page>1000000||size<1||size>100)throw new IllegalArgumentException("Invalid pagination");
  var q=new QueryWrapper<PackageEntity>().eq("org_id",org);if(keyword!=null&&!keyword.isBlank()){if(keyword.length()>200)throw new IllegalArgumentException("Keyword too long");q.like("package_code",keyword.strip());}
  if(filters.get("productId")!=null&&!filters.get("productId").isBlank())q.eq("product_id",com.hospital.mes.masterdata.application.MasterMutation.id(filters.get("productId")));
  if(java.util.stream.Stream.of("status","version","effectiveFrom").anyMatch(k->filters.get(k)!=null&&!filters.get(k).isBlank())){var ids=matchingVersions(org,filters.get("status"),filters.get("version"),filters.get("effectiveFrom")).stream().map(VersionEntity::getPackageId).distinct().toList();if(ids.isEmpty())return new ScopedStore.PageData<>(List.of(),0,page,size);q.in("id",ids);}
  var sort=filters.getOrDefault("sort","id,desc").split(",");String col=sort[0].replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(java.util.Locale.ROOT);if(sort.length!=2||!Set.of("id","package_code","product_id","updated_at").contains(col)||!Set.of("asc","desc").contains(sort[1]))throw new IllegalArgumentException("Invalid sort");long total=packageMapper.selectCount(q);q.orderBy(true,sort[1].equals("asc"),col);if(!col.equals("id"))q.orderByAsc("id");q.last("LIMIT "+((long)page*size)+","+size);return new ScopedStore.PageData<>(packageMapper.selectList(q),total,page,size);
 }
}
