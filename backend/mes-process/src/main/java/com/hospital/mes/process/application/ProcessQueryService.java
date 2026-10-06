package com.hospital.mes.process.application;
import org.springframework.transaction.support.TransactionSynchronizationManager;
@org.springframework.stereotype.Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProcessQueryService {
 private final ProcessService process;
 private final com.hospital.mes.process.infrastructure.ProcessStore store;
 public ProcessQueryService(ProcessService process,com.hospital.mes.process.infrastructure.ProcessStore store){this.process=process;this.store=store;}
 public com.fasterxml.jackson.databind.JsonNode snapshot(long org,long versionId){return process.snapshot(org,versionId,false);}
 public String productName(long org,long productId){return store.products().get(org,productId).getProductName();}
 public com.fasterxml.jackson.databind.JsonNode requireUsable(long org,long versionId){return process.snapshot(org,versionId,true);}
 /** Exact physical identities are attached to the detached, validated published definition. */
 public com.fasterxml.jackson.databind.JsonNode requireUsableIdentified(long org,long versionId){
  if(TransactionSynchronizationManager.isActualTransactionActive())store.versions().lock(org,versionId);
  var result=(com.fasterxml.jackson.databind.node.ObjectNode)requireUsable(org,versionId);
  var formula=store.formula(org,versionId);var route=store.route(org,versionId);
  if(formula==null||route==null)throw new com.hospital.mes.common.exception.ComplianceException("BATCH_DEFINITION_NOT_READY","Formula and route required");
  result.put("packageVersionId",Long.toString(versionId));result.put("formulaVersionId",formula.getId().toString());result.put("routeVersionId",route.getId().toString());
  for(var item:result.path("formula").path("items")){
   var source=store.items(org,formula.getId()).stream().filter(x->x.getLineNo().intValue()==item.path("lineNo").asInt()).findFirst().orElseThrow();
   ((com.fasterxml.jackson.databind.node.ObjectNode)item).put("formulaItemId",source.getId().toString());
  }
  for(var operation:result.path("route").path("operations")){
   var source=store.operations(org,route.getId()).stream().filter(x->x.getOperationCode().equals(operation.path("operationCode").asText())).findFirst().orElseThrow();
   ((com.fasterxml.jackson.databind.node.ObjectNode)operation).put("operationDefId",source.getId().toString());
   for(var parameter:operation.path("parameters")){
    var parameterSource=store.parameters(org,source.getId()).stream().filter(x->x.getParameterCode().equals(parameter.path("parameterCode").asText())).findFirst().orElseThrow();
    ((com.fasterxml.jackson.databind.node.ObjectNode)parameter).put("parameterDefId",parameterSource.getId().toString());
   }
  }
  return result;
 }
 public void requireProduct(long org,long productId){if(!"ACTIVE".equals(store.products().get(org,productId).getStatus()))throw new com.hospital.mes.common.exception.ComplianceException("PRODUCT_NOT_USABLE","Active product required");}
 public record OperationReference(String id,String operationCode,String operationName){}
 public java.util.List<OperationReference> operations(long org,long versionId){
  store.versions().get(org,versionId);
  var route=store.route(org,versionId);
  if(route==null)return java.util.List.of();
  return store.operations(org,route.getId()).stream().map(o->new OperationReference(o.getId().toString(),o.getOperationCode(),o.getOperationName())).toList();
 }
 public OperationReference requireOperation(long org,long versionId,long operationId){
  return operations(org,versionId).stream().filter(o->o.id().equals(Long.toString(operationId))).findFirst().orElseThrow(()->new java.util.NoSuchElementException("Operation does not belong to process version"));
 }
}
