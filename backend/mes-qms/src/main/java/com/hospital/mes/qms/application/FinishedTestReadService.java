package com.hospital.mes.qms.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.production.application.ProductionQueryService;
import com.hospital.mes.qms.infrastructure.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/** Finished indexes reference existing raw test facts; IPC never becomes finished QC by its batch alone. */
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url") @Transactional(readOnly=true)
public class FinishedTestReadService {
 private final FinishedSamplingRecordMapper sampling;private final FinishedInspectionRequestMapper requests;private final IncomingStore db;private final ProductionQualityService quality;private final ProductionQueryService batches;private final MasterMutation mutations;private final ObjectMapper json;
 public FinishedTestReadService(FinishedSamplingRecordMapper sampling,FinishedInspectionRequestMapper requests,IncomingStore db,ProductionQualityService quality,ProductionQueryService batches,MasterMutation mutations,ObjectMapper json){this.sampling=sampling;this.requests=requests;this.db=db;this.quality=quality;this.batches=batches;this.mutations=mutations;this.json=json;}
 private record Source(SampleRow sample,FinishedInspectionRequestEntity request){}
 private Map<Long,Source> sources(long org){
  var requestMap=requests.selectList(new QueryWrapper<FinishedInspectionRequestEntity>().eq("org_id",org)).stream().collect(Collectors.toMap(FinishedInspectionRequestEntity::getId,r->r));
  var samples=db.all(ProductionQualityService.SAMPLE,org).stream().map(r->(SampleRow)r).collect(Collectors.toMap(SampleRow::getId,r->r));
  var found=new HashMap<Long,Source>();
  for(var r:sampling.selectList(new QueryWrapper<FinishedSamplingRecordEntity>().eq("org_id",org))){var request=requestMap.get(r.getInspectionRequestId());var sample=samples.get(r.getSampleId());if(request!=null&&sample!=null&&"PRODUCTION".equals(sample.getSampleScope())&&("FinishedInspectionRequest:"+request.getId()).equals(sample.getSourceRef())&&Objects.equals(sample.getMainBatchId(),request.getMainBatchId())&&Objects.equals(sample.getMaterialLotId(),request.getMaterialLotId())&&Objects.equals(r.getMaterialLotId(),request.getMaterialLotId()))found.put(sample.getId(),new Source(sample,request));}
  return found;
 }
 public ScopedStore.PageData<JsonNode> list(int page,int size,Map<String,String> f){
  long org=mutations.context("qms:test:view").organizationId();if(page<0||page>1000000||size<1||size>100)throw new IllegalArgumentException("Invalid pagination");if(!Set.of("page","size","keyword","status","mainBatchId","sampleId").containsAll(f.keySet()))throw new IllegalArgumentException("Unknown finished test filter");
  Long batch=f.containsKey("mainBatchId")?MasterMutation.id(f.get("mainBatchId")):null,sample=f.containsKey("sampleId")?MasterMutation.id(f.get("sampleId")):null;String keyword=f.getOrDefault("keyword","").trim();if(keyword.length()>200)throw new IllegalArgumentException("Keyword too long");var source=sources(org);
  var rows=db.all(ProductionQualityService.TEST,org).stream().map(r->(ProductionTestInstanceRow)r).filter(t->source.containsKey(t.getSampleId())).filter(t->batch==null||Objects.equals(source.get(t.getSampleId()).sample.getMainBatchId(),batch)).filter(t->sample==null||Objects.equals(t.getSampleId(),sample)).filter(t->!f.containsKey("status")||Objects.equals(f.get("status"),t.getStatus())).filter(t->keyword.isBlank()||String.join(" ",t.getTestCode(),source.get(t.getSampleId()).sample.getSampleNo(),source.get(t.getSampleId()).request.getInspectionRequestNo()).toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT))).sorted(Comparator.comparing(ProductionTestInstanceRow::getId).reversed()).toList();
  var items=rows.stream().skip((long)page*size).limit(size).map(t->{var s=source.get(t.getSampleId());var n=json.createObjectNode();n.put("id",t.getId().toString());n.put("testCode",t.getTestCode());n.put("sampleId",s.sample.getId().toString());n.put("sampleNo",s.sample.getSampleNo());n.put("mainBatchId",s.sample.getMainBatchId().toString());n.put("batchNo",batches.batchNumber(org,s.sample.getMainBatchId()));n.put("inspectionRequestId",s.request.getId().toString());n.put("inspectionRequestNo",s.request.getInspectionRequestNo());n.put("status",t.getStatus());n.put("attemptNo",t.getAttemptNo());return (JsonNode)n;}).toList();return new ScopedStore.PageData<>(items,rows.size(),page,size);
 }
 public JsonNode get(String id){long org=mutations.context("qms:test:view").organizationId();var t=(ProductionTestInstanceRow)db.get(ProductionQualityService.TEST,org,MasterMutation.id(id));if(!sources(org).containsKey(t.getSampleId()))throw new NoSuchElementException("Finished test not found");return quality.get(ProductionQualityService.TEST,id);}
}
