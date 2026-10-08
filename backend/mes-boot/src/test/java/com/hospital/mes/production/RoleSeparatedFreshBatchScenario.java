package com.hospital.mes.production;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.equipment.application.EquipmentCommands;
import com.hospital.mes.masterdata.application.QualificationCommands;
import com.hospital.mes.qms.application.IncomingActorPort;
import com.hospital.mes.wms.domain.WmsCommands.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import java.time.*;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

/** R9: a fresh, role-separated manufacturing-to-QA chain. Inherits rollback-only CI fixture. */
@TestPropertySource(properties={
  "mes.qualification.required-codes.sample-execute=R9_SAMPLING",
  "mes.qualification.required-codes.test-execute=R9_QC_EXECUTE",
  "mes.qualification.required-codes.test-review=R9_QC_REVIEW",
  "mes.qualification.required-codes.qa-release=R9_QA_RELEASE",
  "mes.qualification.required-codes.operation-start=R9_PRODUCTION",
  "mes.qualification.required-codes.operation-resume=R9_PRODUCTION",
  "mes.qualification.required-codes.weigh-create=R9_PRODUCTION",
  "mes.qualification.required-codes.weigh-verify=R9_QC_REVIEW",
  "mes.qualification.required-codes.charge-create=R9_PRODUCTION",
  "mes.qualification.required-codes.charge-reverse=R9_PRODUCTION"
})
class RoleSeparatedFreshBatchScenario extends FinishedReleaseIT {
  @Autowired IncomingActorPort qualifiedActors;
  long warehouseActor, samplerActor;
  Long verificationBatch, verificationQaActor, verificationPdfFile;

  @org.springframework.test.context.transaction.AfterTransaction void verifyRollback(){
    if(verificationBatch==null||verificationQaActor==null)return;
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM prd_main_batch WHERE id=?",Long.class,verificationBatch)).isZero();
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ebr_pdf_manifest WHERE main_batch_id=?",Long.class,verificationBatch)).isZero();
    if(verificationPdfFile!=null)assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_attachment WHERE id=?",Long.class,verificationPdfFile)).isZero();
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE id=?",Long.class,verificationQaActor)).isZero();
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_qualification WHERE user_id=? AND qualification_code='R9_QA_RELEASE'",Long.class,verificationQaActor)).isZero();
    System.out.println("R9_ROLLBACK_CONFIRMED batch="+verificationBatch+" qa="+verificationQaActor+" no batch, archive, user, qualification residue");
  }

  @BeforeEach void prepareR9Roles(){
    warehouseActor=actor("warehouse");
    samplerActor=actor("sampler");
    permissions.add("master:qualification:create");
    qualify(author,"R9_PRODUCTION");
    qualify(samplerActor,"R9_SAMPLING");
    qualify(analyst,"R9_QC_EXECUTE");
    // The inherited incoming fixture assigns its original sampling task to the QC analyst.
    qualify(analyst,"R9_SAMPLING");
    qualify(reviewer,"R9_QC_REVIEW");
    // Incoming report's independent final approver is distinct from the QA batch signer.
    qualify(approver,"R9_QA_RELEASE");
    qualify(qa,"R9_QA_RELEASE");
    as(author);
  }
  private void qualify(long target,String code){
    as(author);
    qualificationAdministration.create(new QualificationCommands.Create(
      Long.toString(target),code,LocalDate.now(ZoneOffset.UTC).minusDays(1),
      LocalDate.now(ZoneOffset.UTC).plusMonths(1)),key());
  }

  @Override JsonNode confirmedFinishedReceipt(JsonNode completed){
    as(author);
    var request=finishedGoods.createInbound(body("requestNo","R9IR"+suffix,"mainBatchId",id(completed),"reason","Real role-separated finished receipt request"),key());
    request=finishedGoods.inboundAction(id(request),"submit",body("versionNo",request.path("versionNo").asLong(),"reason","Submit actual production output"),token(request),key());
    as(warehouseActor);
    var wh=wms.maintenance("Warehouse",null,new WarehouseCreate("R9W"+suffix,"Warehouse role receiving","FINISHED"),null,key());
    var loc=wms.maintenance("Location",null,new LocationCreate(id(wh),"R9L"+suffix,"Warehouse finished location"),null,key());
    return finishedGoods.inboundAction(id(request),"confirm",body("versionNo",request.path("versionNo").asLong(),"locationId",id(loc),"reason","Warehouse operator confirms receiving","signature",Map.of("reauthToken","token")),token(request),key());
  }

  @Override JsonNode acceptedFinishedRequest(JsonNode receipt){
    as(warehouseActor);
    var request=finishedInspection.createRequest(body("inspectionRequestNo","R9IQ"+suffix,"inboundRequestId",id(receipt),"reason","Warehouse handover for finished QC"),key());
    request=finishedInspection.requestAction(id(request),"submit",body("versionNo",0,"reason","Warehouse submits request"),token(request),key());
    as(analyst);
    return finishedInspection.requestAction(id(request),"accept",body("versionNo",request.path("versionNo").asLong(),"reason","QC accepts request independently"),token(request),key());
  }

  @Override protected JsonNode completedProductionBatch(Runnable beforeCompletion){
    var batch=approvedBatch();
    String bid=id(batch),eid=batch.path("executionUnits").get(0).path("id").asText();
    as(author);
    var op=executionService.operations(eid).getFirst();
    op=executionService.start(id(op),body("reason","Qualified production starts manufacturing"),token(op),key());
    String formula=productionQuery.batch(1,Long.parseLong(bid)).snapshot().path("process").path("formula").path("items").get(0).path("formulaItemId").asText();
    var scale=equipment.create(new EquipmentCommands.Create("R9SC"+suffix,"Production scale","PRODUCTION_IT_SCALE",LocalDate.now(ZoneOffset.UTC).plusDays(30),"Production"),key());
    var exec=production.execution(eid);
    var weighed=weighing.createWeighing(body("executionUnitId",eid,"materialLotId",lot,"formulaItemId",formula,"targetQty","2","actualQty","2","unitId",unit,"scaleEquipmentId",id(scale),"versionNo",exec.path("versionNo").asLong(),"reason","Actual BOM weighing"),key());
    as(reviewer);
    weighed=weighing.verifyWeighing(id(weighed),body("reason","Independent reviewer checks weight","reauthToken","token"),token(weighed),key());
    as(author);
    weighing.createCharge(body("executionUnitId",eid,"operationExecutionId",id(op),"materialLotId",lot,"weighingRecordId",id(weighed),"chargedQty","2","unitId",unit,"versionNo",op.path("versionNo").asLong(),"reason","Production role charges material"),key());
    var current=production.batch(bid);
    quantities.record(bid,output(current,"R9OUT"+suffix,"10"),token(current),key());
    observation(production.batch(bid),"R9LOSS"+suffix,"0");
    calculate(production.batch(bid));
    as(samplerActor);
    var sample=productionQuality.createSample(body("investigationScope","PRODUCTION","mainBatchId",bid,"sampleNo","R9PS"+suffix,"sampleType","TEST_SAMPLE","materialLotId",production.batch(bid).path("finishedLotId").asText(),"quantity","1","unitId",unit,"sourceRef","R9_PRODUCTION_SAMPLE"+suffix,"reason","Sampler collects production QC"),key());
    as(analyst);
    sample=productionQuality.receiveSample(id(sample),body("versionNo",0,"reason","QC receives independent sample"),token(sample),key());
    String item=productionQuery.batch(1,Long.parseLong(bid)).snapshot().path("qualityPlan").path("specification").path("items").get(0).path("specificationItemId").asText();
    var test=productionQuality.createTest(body("sampleId",id(sample),"specificationItemId",item,"reason","QC analyst starts assay"),key());
    test=productionQuality.result(id(test),false,body("versionNo",0,"resultNumeric","2","reason","QC analyst original assay","signature",Map.of("reauthToken","token")),token(test),key());
    as(reviewer);
    productionQuality.review(id(test),body("versionNo",test.path("versionNo").asLong(),"resultRevisionId",test.path("currentResultRevisionId").asText(),"disposition","CONFIRMED","reason","Independent QC reviewer","signature",Map.of("reauthToken","token")),token(test),key());
    as(author);
    beforeCompletion.run();
    recordAndSubmitOperationForm(eid);
    var latest=executionService.operation(1,Long.parseLong(id(op)));
    executionService.complete(id(latest),body("reason","Production operation and eBR complete"),token(latest),key());
    return production.completeProduction(bid,body("reason","Complete after QA-ready sources"),token(production.batch(bid)),key());
  }

  @Override protected void completeFinishedQuality(JsonNode completed){
    var received=confirmedFinishedReceipt(completed);
    var request=acceptedFinishedRequest(received);
    as(samplerActor);
    var sampling=finishedInspection.sample(id(request),body(
      "versionNo",request.path("versionNo").asLong(),"samplingNo","R9FSR"+suffix,
      "sampleNo","R9FSM"+suffix,"sampleType","TEST_SAMPLE","samplingLocation","Warehouse sampling booth",
      "quantity","1","unitId",unit,"samplingMethod","Approved representative sample",
      "reason","Qualified sampler","signature",Map.of("reauthToken","token")),token(request),key());
    as(analyst);
    var sample=productionQuality.get(com.hospital.mes.qms.application.ProductionQualityService.SAMPLE,sampling.path("sampleId").asText());
    sample=productionQuality.receiveSample(id(sample),body("versionNo",0,"reason","QC receives finished sample"),token(sample),key());
    String item=request.path("specificationSnapshot").path("specification").path("items").get(0).path("specificationItemId").asText();
    var test=productionQuality.createTest(body("sampleId",id(sample),"specificationItemId",item,"reason","QC analyst finished assay"),key());
    test=productionQuality.result(id(test),false,body("versionNo",0,"resultNumeric","2","reason","QC analyst finished result","signature",Map.of("reauthToken","token")),token(test),key());
    as(reviewer);
    productionQuality.review(id(test),body("versionNo",test.path("versionNo").asLong(),"resultRevisionId",test.path("currentResultRevisionId").asText(),"disposition","CONFIRMED","reason","QC reviewer independent verification","signature",Map.of("reauthToken","token")),token(test),key());
    as(analyst);
    var fresh=finishedInspection.getRequest(id(request));
    var report=finishedInspection.generateReport(id(request),body("versionNo",fresh.path("versionNo").asLong(),"reportNo","R9FREP"+suffix,"reason","QC analyst generates finished report"),token(fresh),key());
    as(reviewer);
    finishedInspection.approveReport(id(report),body("versionNo",0,"reason","QC reviewer approves report","signature",Map.of("reauthToken","token")),token(report),key());
  }


  @Test void freshRoleSplitQualifiedChain() throws Exception {
    assertThat(environment.getProperty("mes.qualification.required-codes.qa-release")).isEqualTo("R9_QA_RELEASE");
    assertThat(environment.getProperty("mes.qualification.required-codes.test-review")).isEqualTo("R9_QC_REVIEW");
    qualifiedActors.requireQualified(1,samplerActor,"sample-execute");
    qualifiedActors.requireQualified(1,analyst,"test-execute");
    qualifiedActors.requireQualified(1,reviewer,"test-review");
    qualifiedActors.requireQualified(1,qa,"qa-release");
    assertThatThrownBy(()->qualifiedActors.requireQualified(1,warehouseActor,"test-execute")).hasMessageContaining("QUALIFICATION_REQUIRED");
    assertThatThrownBy(()->qualifiedActors.requireQualified(1,warehouseActor,"qa-release")).hasMessageContaining("QUALIFICATION_REQUIRED");
    var pending=completedQaBatch();
    String batchId=id(pending);
    verificationBatch=Long.parseLong(batchId);
    verificationQaActor=qa;
    as(qa);
    assertThat(finishedQa.review(batchId).path("blockingCodes")).isEmpty();
    var validCommand=releaseCommand(pending,"RELEASED",null);
    long signaturesBefore=jdbc.queryForObject("SELECT COUNT(*) FROM gxp_signature WHERE org_id=1",Long.class);
    as(warehouseActor);
    blockedCode("QUALIFICATION_REQUIRED",()->finishedQa.decide(validCommand,token(pending),key()));
    assertThat(finishedDecisions.decisions(1,Long.parseLong(batchId))).isEmpty();
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_signature WHERE org_id=1",Long.class)).isEqualTo(signaturesBefore);
    assertThat(production.batch(batchId).path("status").asText()).isEqualTo("PENDING_QA");
    as(qa);
    var released=finishedQa.decide(releaseCommand(pending,"RELEASED",null),token(pending),key());
    assertThat(released.path("decisionBy").asLong()).isEqualTo(qa);
    assertThat(verifier.verify(1,released.path("signatureId").asLong())).isTrue();
    var roleProof=jdbc.queryForMap("SELECT created_at,valid_from,status FROM md_qualification WHERE org_id=1 AND user_id=? AND qualification_code='R9_QA_RELEASE'",qa);
    var signatureTime=jdbc.queryForObject("SELECT decision_at FROM qms_release_decision WHERE org_id=1 AND id=?",java.time.LocalDateTime.class,id(released));
    assertThat(((java.sql.Timestamp)roleProof.get("created_at")).toLocalDateTime().isBefore(signatureTime)).isTrue();
    assertThat(roleProof.get("status")).isEqualTo("ACTIVE");
    assertThat(stockQuery.materialLot(1,pending.path("finishedLotId").asLong()).path("qualityStatus").asText()).isEqualTo("RELEASED");
    var root=production.batch(batchId);
    var manifest=finishedArchive.generate(batchId,
      new com.hospital.mes.release.domain.ArchiveCommand(root.path("versionNo").asLong(),"FINAL","Qualified independent QA completed post-decision archive"),
      token(root),key());
    verificationPdfFile=manifest.path("fileId").asLong();
    assertThat(manifest.path("archiveKind").asText()).isEqualTo("FINAL");
    assertThat(manifest.path("releaseDecisionId").asText()).isEqualTo(id(released));
    var archive=finishedArchive.get(batchId);
    assertThat(archive.path("pdfManifests")).hasSize(1);
    assertThat(archive.path("recordDigest").asText()).isEqualTo(manifest.path("recordDigest").asText());
    var bytes=finishedAttachments.readContent(1,manifest.path("fileId").asLong()).content();
    assertThat(bytes.length).isGreaterThan(2000);
    assertThat(manifest.path("fileHash").asText()).isEqualTo(java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes)));
    System.out.println("R9_FRESH_ROLES_BATCH="+batchId+" qa="+qa+" warehouse="+warehouseActor+" sampler="+samplerActor+" analyst="+analyst+" reviewer="+reviewer+" archive=FINAL verified; transaction will roll back");
  }
}
