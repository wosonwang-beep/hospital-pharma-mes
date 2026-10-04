package com.hospital.mes.production;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.common.exception.MesException;
import com.hospital.mes.equipment.application.EquipmentCommands;
import com.hospital.mes.process.domain.ProcessCommands;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.support.TransactionTemplate;

/** Explicitly approved DEV-only retained fixtures. Never delete regulated test history. */
@EnabledIfSystemProperty(named="mes.retained-concurrency-fixtures",matches="true")
class NativeConcurrencyIT extends IncomingProductionIT {
    final ThreadLocal<CurrentPlatformContext> actorContext=new ThreadLocal<>();
    boolean committed;
    String batchId,executionId,formulaId,issueId,weighingId;
    JsonNode batch,issue,weighBody,chargeBody;

    @Override protected void as(long actor){
        actorContext.set(new CurrentPlatformContext(1,actor,Set.of("INCOMING_TEST"),permissions,"native-race","IT_RACE_"+suffix));
        when(contexts.current()).thenAnswer(i->actorContext.get());
    }
    @Test @EnabledIfSystemProperty(named="mes.reconcile-retained-red-trials",matches="true")
    void reconcileOnlyArchivedRedTrialDraftOrdersThroughAuditedCommand() throws Exception {
        // Roll back this method's unused setup. Only exact archived test-owned IDs may be corrected.
        TestTransaction.end();
        Path root=Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while(!Files.exists(root.resolve("AGENTS.md"))&&root.getParent()!=null)root=root.getParent();
        var corrected=json.createArrayNode();
        try(var paths=Files.list(root.resolve("docs/acceptance/incoming-quality/evidence"))){
            for(var path:paths.filter(p->p.getFileName().toString().startsWith("native-concurrency-")).toList()){
                var archived=json.readTree(Files.readString(path));
                if(!archived.hasNonNull("orderId"))continue;
                long archivedAuthor=archived.path("authorId").asLong(),archivedLot=archived.path("lotId").asLong();
                assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_material_lot WHERE id=? AND created_by=? AND quality_status='REJECTED' AND inventory_status='BLOCKED'",Long.class,archivedLot,archivedAuthor)).isEqualTo(1);
                String archivedOrder=archived.path("orderId").asText();
                var rows=jdbc.queryForList("SELECT o.*,(SELECT SUM(b.planned_qty) FROM prd_main_batch b WHERE b.production_order_id=o.id) AS allocated FROM prd_production_order o WHERE o.id=? AND o.created_by=? AND o.order_no LIKE 'IT_RACE_%'",archivedOrder,archivedAuthor);
                assertThat(rows).hasSize(1);var row=rows.getFirst();
                var allocated=(java.math.BigDecimal)row.get("allocated");
                if(allocated==null||allocated.compareTo((java.math.BigDecimal)row.get("planned_qty"))<=0)continue;
                assertThat(row.get("status")).isEqualTo("DRAFT");
                assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM prd_main_batch WHERE production_order_id=? AND (status<>'DRAFT' OR created_by<>? OR unit_id<>?)",Long.class,archivedOrder,archivedAuthor,row.get("unit_id"))).isZero();
                as(archivedAuthor);
                var edit=body("orderNo",row.get("order_no"),"productId",row.get("product_id").toString(),"plannedQty",allocated.toPlainString(),"unitId",row.get("unit_id").toString(),"versionNo",row.get("version_no"),"reason","Reconcile approved retained RED concurrency trial; preserve original allocation audit history");
                JsonNode result=new TransactionTemplate(transactions).execute(s->production.updateOrder(archivedOrder,edit,"\""+row.get("version_no")+"\"",key()));
                corrected.add(body("orderId",archivedOrder,"previousPlannedQty",row.get("planned_qty").toString(),"plannedQty",allocated.toPlainString(),"newVersion",result.path("versionNo").asLong(),"source",path.getFileName().toString()));
            }
        }
        Files.writeString(root.resolve("docs/acceptance/incoming-quality/evidence/native-red-trial-reconciliation-"+UUID.randomUUID()+".json"),json.writerWithDefaultPrettyPrinter().writeValueAsString(body("corrections",corrected,"command","ProductionOrder:EDIT","regulatedHistoryPreserved",true,"recordedAt",Instant.now().toString())),StandardOpenOption.CREATE_NEW);
    }
    void commitFixture(){TestTransaction.flagForCommit();TestTransaction.end();committed=true;}
    JsonNode namedDraft(String planned){
        var draft=preparedBatch(false);
        production.updateOrder(orderId,body("orderNo","IT_RACE_ORDER_"+suffix,"productId",productId,"plannedQty",planned,"unitId",unit,"versionNo",0,"reason","Approved retained concurrency fixture"),"\"0\"",key());
        return production.updateBatch(id(draft),body("batchNo","IT_RACE_BATCH_"+suffix,"productionOrderId",orderId,"productId",productId,"plannedQty","10","unitId",unit,"versionNo",0,"reason","Approved retained fixture"),token(draft),key());
    }
    @AfterEach void retainBlockedEvidence() throws Exception {
        if(!committed)return;
        var tx=new TransactionTemplate(transactions);
        as(qa);
        tx.execute(s->{
            if(!"REJECTED".equals(jdbc.queryForObject("SELECT quality_status FROM md_material_lot WHERE id=?",String.class,lot)))rejectLot();
            return null;
        });
        assertThat(jdbc.queryForObject("SELECT inventory_status FROM md_material_lot WHERE id=?",String.class,lot)).isEqualTo("BLOCKED");
        Path root=Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while(!Files.exists(root.resolve("AGENTS.md"))&&root.getParent()!=null)root=root.getParent();
        Path evidence=root.resolve("docs/acceptance/incoming-quality/evidence/native-concurrency-"+suffix+".json");
        Files.createDirectories(evidence.getParent());
        Files.writeString(evidence,json.writerWithDefaultPrettyPrinter().writeValueAsString(body("fixture","IT_RACE_"+suffix,"materialId",material,"lotId",lot,"orderId",orderId,"batchId",batchId,"authorId",author,"qaId",qa,"qualityStatus","REJECTED","inventoryStatus","BLOCKED","retained",true,"orders",jdbc.queryForList("SELECT id,order_no FROM prd_production_order WHERE created_by=?",author),"batches",jdbc.queryForList("SELECT id,batch_no FROM prd_main_batch WHERE created_by=?",author),"recordedAt",Instant.now().toString())));
    }
    JsonNode rejectLot(){
        var gate=service.releaseReview(lot);
        var command=body("decision","REJECTED","releaseBasis","FULL_INSPECTION","reason","IT_RACE retained test lot; prohibit further production use","reauthToken","token");
        Long previous=jdbc.queryForObject("SELECT MAX(id) FROM qms_release_decision WHERE material_lot_id=?",Long.class,lot);
        if(previous!=null)command.put("supersedesDecisionId",previous.toString());
        return service.decideRelease(lot,command,token(gate),key());
    }
    void awaitDatabaseWait(long waitingConnection,Future<?> waitingResult) throws Exception {
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(8);
        while(System.nanoTime()<deadline){
            // SHOW PROCESSLIST exposes this account's own sessions without global PROCESS privilege.
            for(var row:jdbc.queryForList("SHOW FULL PROCESSLIST")){
                if(((Number)row.get("Id")).longValue()==waitingConnection&&Objects.toString(row.get("Info"),"").toUpperCase(Locale.ROOT).contains("FOR UPDATE")){
                    assertThat(waitingResult.isDone()).as("Second query remains pending while the first physical transaction holds the row lock").isFalse();
                    return;
                }
            }
            Thread.sleep(25);
        }
        throw new AssertionError("No pending native FOR UPDATE observed for connection "+waitingConnection);
    }
    record Race(JsonNode first,JsonNode second,Throwable firstError,Throwable secondError){}
    Race race(String lockSql,Object lockedId,CurrentPlatformContext firstActor,Callable<JsonNode> firstAction,CurrentPlatformContext secondActor,Callable<JsonNode> secondAction) throws Exception {
        var held=new CountDownLatch(1);var proceed=new CountDownLatch(1);var connection=new AtomicLong();var secondConnection=new AtomicLong();
        var pool=Executors.newFixedThreadPool(2);
        try{
            Future<JsonNode> first=pool.submit(()->{actorContext.set(firstActor);return new TransactionTemplate(transactions).execute(s->{
                jdbc.queryForList(lockSql,lockedId);connection.set(jdbc.queryForObject("SELECT CONNECTION_ID()",Long.class));held.countDown();
                try{if(!proceed.await(10,TimeUnit.SECONDS))throw new AssertionError("Race barrier timeout");return firstAction.call();}catch(RuntimeException e){throw e;}catch(Exception e){throw new RuntimeException(e);}
            });});
            assertThat(held.await(5,TimeUnit.SECONDS)).isTrue();
            Future<JsonNode> second=pool.submit(()->{actorContext.set(secondActor);return new TransactionTemplate(transactions).execute(s->{secondConnection.set(jdbc.queryForObject("SELECT CONNECTION_ID()",Long.class));try{return secondAction.call();}catch(RuntimeException e){throw e;}catch(Exception e){throw new RuntimeException(e);}});});
            long startedDeadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);while(secondConnection.get()==0&&System.nanoTime()<startedDeadline)Thread.sleep(10);
            assertThat(secondConnection.get()).isPositive().isNotEqualTo(connection.get());
            awaitDatabaseWait(secondConnection.get(),second);proceed.countDown();
            JsonNode a=null,b=null;Throwable ae=null,be=null;
            try{a=first.get(15,TimeUnit.SECONDS);}catch(ExecutionException e){ae=e.getCause();}
            try{b=second.get(15,TimeUnit.SECONDS);}catch(ExecutionException e){be=e.getCause();}
            return new Race(a,b,ae,be);
        }finally{proceed.countDown();pool.shutdownNow();assertThat(pool.awaitTermination(5,TimeUnit.SECONDS)).isTrue();}
    }
    @Test void concurrentOrderAllocationHasOneWinnerAndNoRejectedCommandFacts() throws Exception {
        var draft=namedDraft("20");batchId=id(draft);commitFixture();as(author);var actor=actorContext.get();
        String firstKey=key(),secondKey=key();
        var a=body("batchNo","IT_RACE_A_"+suffix,"productionOrderId",orderId,"productId",productId,"plannedQty","8","unitId",unit,"reason","Concurrent allocation A");
        var b=a.deepCopy();b.put("batchNo","IT_RACE_B_"+suffix);
        var result=race("SELECT id FROM prd_production_order WHERE id=? FOR UPDATE",orderId,actor,()->production.createBatch(a,firstKey),actor,()->production.createBatch(b,secondKey));
        assertThat(result.firstError()).isNull();assertThat(result.first()).isNotNull();
        assertThat(result.secondError()).isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isIn("ORDER_ALLOCATION_EXCEEDED","CONCURRENT_MODIFICATION"));
        as(author);assertThatThrownBy(()->new TransactionTemplate(transactions).execute(s->production.createBatch(b,secondKey)))
            .isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("ORDER_ALLOCATION_EXCEEDED"));
        assertThat(jdbc.queryForObject("SELECT SUM(planned_qty) FROM prd_main_batch WHERE production_order_id=?",java.math.BigDecimal.class,orderId)).isEqualByComparingTo("18");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM prd_main_batch WHERE batch_no=?",Long.class,"IT_RACE_B_"+suffix)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,secondKey)).isZero();
    }
    @Test void concurrentSignedReleaseSuccessorsHaveOnlyOneWinnerAndRetainOriginalSignature() throws Exception {
        prepareGate("RESERVE");as(qa);var gate=service.releaseReview(lot);
        String original=jdbc.queryForObject("SELECT CAST(MAX(id) AS CHAR) FROM qms_release_decision WHERE material_lot_id=?",String.class,lot);
        var reject=body("decision","REJECTED","releaseBasis","FULL_INSPECTION","supersedesDecisionId",original,"reason","Concurrent retained quality successor","reauthToken","token");
        commitFixture();as(qa);var actor=actorContext.get();String firstKey=key(),secondKey=key();
        var result=race("SELECT id FROM md_material_lot WHERE id=? FOR UPDATE",lot,actor,()->service.decideRelease(lot,reject,token(gate),firstKey),actor,()->service.decideRelease(lot,reject,token(gate),secondKey));
        assertThat(result.firstError()).isNull();assertThat(result.first()).isNotNull();
        assertThat(result.secondError()).isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isIn("VERSION_CONFLICT","CONCURRENT_MODIFICATION"));
        as(qa);assertThatThrownBy(()->new TransactionTemplate(transactions).execute(s->service.decideRelease(lot,reject,token(gate),secondKey)))
            .isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("VERSION_CONFLICT"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_release_decision WHERE material_lot_id=?",Long.class,lot)).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_release_decision WHERE supersedes_decision_id=?",Long.class,original)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,secondKey)).isZero();
        for(long signature:jdbc.queryForList("SELECT signature_id FROM qms_release_decision WHERE material_lot_id=?",Long.class,lot))assertThat(verifier.verify(1,signature)).isTrue();
    }
    @Test void concurrentChargesCannotConsumeSameVerifiedWeighingTwice() throws Exception {
        prepareGate("CHARGE");commitFixture();as(author);var actor=actorContext.get();String firstKey=key(),secondKey=key();
        var result=race("SELECT id FROM prd_production_order WHERE id=? FOR UPDATE",orderId,actor,()->command("CHARGE",firstKey),actor,()->command("CHARGE",secondKey));
        assertThat(result.firstError()).isNull();assertThat(result.first()).isNotNull();
        assertThat(result.secondError()).isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isIn("CHARGE_QTY_INVALID","CONCURRENT_MODIFICATION"));
        as(author);assertThatThrownBy(()->new TransactionTemplate(transactions).execute(s->command("CHARGE",secondKey)))
            .isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("VERSION_CONFLICT"));
        var operation=executionService.operation(1,Long.parseLong(chargeBody.path("operationExecutionId").asText()));
        ((com.fasterxml.jackson.databind.node.ObjectNode)chargeBody).put("versionNo",operation.path("versionNo").asLong());
        assertThatThrownBy(()->new TransactionTemplate(transactions).execute(s->command("CHARGE",secondKey)))
            .isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("CHARGE_QTY_INVALID"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_material_charge WHERE execution_unit_id=?",Long.class,executionId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_genealogy WHERE main_batch_id=?",Long.class,batchId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_quantity_event WHERE main_batch_id=? AND event_type='CHARGE'",Long.class,batchId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE material_lot_id=? AND event_type='CONSUME'",Long.class,lot)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,secondKey)).isZero();
        assertThat(jdbc.queryForObject("SELECT SUM(delta_qty) FROM wms_inventory_ledger WHERE material_lot_id=?",java.math.BigDecimal.class,lot)).isEqualByComparingTo("8");
    }
    @Test void firstConcurrentAllocationPreventsChangingOrderProduct() throws Exception {
        batchId=id(namedDraft("20"));
        var alternate=process.createProduct(new ProcessCommands.ProductCreate("IT_RACE_ALT_"+suffix,"Alternate test product",null,null,unit),key());
        var empty=production.createOrder(body("orderNo","IT_RACE_EMPTY_"+suffix,"productId",productId,"plannedQty","20","unitId",unit,"reason","Approved retained fixture"),key());
        orderId=id(empty);commitFixture();as(author);var actor=actorContext.get();String editKey=key();
        var allocation=body("batchNo","IT_RACE_FIRST_"+suffix,"productionOrderId",orderId,"productId",productId,"plannedQty","8","unitId",unit,"reason","First allocation");
        var edit=body("orderNo","IT_RACE_EMPTY_"+suffix,"productId",id(alternate),"plannedQty","20","unitId",unit,"versionNo",0,"reason","Concurrent product edit");
        var result=race("SELECT id FROM prd_production_order WHERE id=? FOR UPDATE",orderId,actor,()->production.createBatch(allocation,key()),actor,()->production.updateOrder(orderId,edit,token(empty),editKey));
        assertThat(result.firstError()).isNull();
        assertThat(result.secondError()).isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isIn("ORDER_HAS_BATCHES","CONCURRENT_MODIFICATION"));
        as(author);assertThatThrownBy(()->new TransactionTemplate(transactions).execute(s->production.updateOrder(orderId,edit,token(empty),editKey)))
            .isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("ORDER_HAS_BATCHES"));
        assertThat(jdbc.queryForObject("SELECT product_id FROM prd_production_order WHERE id=?",Long.class,orderId)).isEqualTo(Long.parseLong(productId));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,editKey)).isZero();
    }
    @Test void concurrentOrderPlanReductionPreventsStaleBatchAllocation() throws Exception {
        var draft=namedDraft("20");batchId=id(draft);commitFixture();as(author);var actor=actorContext.get();String editKey=key();
        var plan=body("orderNo","IT_RACE_ORDER_"+suffix,"productId",productId,"plannedQty","12","unitId",unit,"versionNo",1,"reason","Concurrent plan reduction");
        var edit=body("batchNo","IT_RACE_BATCH_"+suffix,"productionOrderId",orderId,"productId",productId,"plannedQty","15","unitId",unit,"versionNo",draft.path("versionNo").asLong(),"reason","Concurrent batch edit");
        var result=race("SELECT id FROM prd_production_order WHERE id=? FOR UPDATE",orderId,actor,()->production.updateOrder(orderId,plan,"\"1\"",key()),actor,()->production.updateBatch(batchId,edit,token(draft),editKey));
        assertThat(result.firstError()).isNull();
        assertThat(result.secondError()).isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isIn("ORDER_ALLOCATION_EXCEEDED","CONCURRENT_MODIFICATION"));
        as(author);assertThatThrownBy(()->new TransactionTemplate(transactions).execute(s->production.updateBatch(batchId,edit,token(draft),editKey)))
            .isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("ORDER_ALLOCATION_EXCEEDED"));
        assertThat(jdbc.queryForObject("SELECT planned_qty FROM prd_main_batch WHERE id=?",java.math.BigDecimal.class,batchId)).isEqualByComparingTo("10");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,editKey)).isZero();
    }
    void prepareGate(String gate){
        incomingReportId=id(releaseIncoming(()->{}));as(author);
        var draft=namedDraft("10");batch=production.releaseBatch(id(draft),body("packageVersionId",packageVersionId,"ebrTemplateVersionId",templateVersionId,"reason","IT_RACE real release"),token(draft),key());
        batchId=id(batch);executionId=batch.path("executionUnits").get(0).path("id").asText();
        formulaId=productionQuery.batch(1,Long.parseLong(batchId)).snapshot().path("process").path("formula").path("items").get(0).path("formulaItemId").asText();
        if(gate.equals("RESERVE"))return;
        stock.reserve(batchId,body("items",List.of(Map.of("formulaItemId",formulaId,"materialLotId",lot,"reservedQty","2","unitId",unit)),"reason","IT_RACE reservation"),token(batch),key());
        issue=stock.createIssue(body("mainBatchId",batchId,"issueNo","IT_RACE_ISSUE_"+suffix,"items",List.of(Map.of("formulaItemId",formulaId,"materialLotId",lot,"issuedQty","2","unitId",unit)),"reason","IT_RACE actual issue"),key());issueId=id(issue);
        if(gate.equals("ISSUE"))return;
        issue=stock.confirmIssue(issueId,body("reason","IT_RACE confirm"),token(issue),key());
        batch=production.startBatch(batchId,body("reason","IT_RACE start"),token(batch),key());
        var op=executionService.operations(executionId).getFirst();op=executionService.start(id(op),body("reason","IT_RACE actual operation"),token(op),key());
        var scale=equipment.create(new EquipmentCommands.Create("IT_RACE_SCALE_"+suffix,"IT_RACE scale","PRODUCTION_IT_SCALE",LocalDate.now(ZoneOffset.UTC).plusDays(30),"Test fixture"),key());
        var execution=production.execution(executionId);
        weighBody=body("executionUnitId",executionId,"materialLotId",lot,"formulaItemId",formulaId,"targetQty","2","actualQty","2","unitId",unit,"scaleEquipmentId",id(scale),"versionNo",execution.path("versionNo").asLong(),"reason","IT_RACE actual weighing");
        if(gate.equals("WEIGH"))return;
        var weighed=weighing.createWeighing(weighBody,key());as(reviewer);weighed=weighing.verifyWeighing(id(weighed),body("reason","IT_RACE independent verification","reauthToken","token"),token(weighed),key());as(author);weighingId=id(weighed);
        chargeBody=body("executionUnitId",executionId,"operationExecutionId",id(op),"materialLotId",lot,"weighingRecordId",weighingId,"chargedQty","2","unitId",unit,"versionNo",op.path("versionNo").asLong(),"reason","IT_RACE actual charge");
    }
    JsonNode command(String gate,String commandKey){return switch(gate){
        case "RESERVE"->stock.reserve(batchId,body("items",List.of(Map.of("formulaItemId",formulaId,"materialLotId",lot,"reservedQty","2","unitId",unit)),"reason","IT_RACE concurrent reserve"),token(batch),commandKey);
        case "ISSUE"->stock.confirmIssue(issueId,body("reason","IT_RACE concurrent confirm"),token(issue),commandKey);
        case "WEIGH"->weighing.createWeighing(weighBody,commandKey);
        case "CHARGE"->weighing.createCharge(chargeBody,commandKey);
        default->throw new IllegalArgumentException(gate);
    };}
    @ParameterizedTest @CsvSource({"RESERVE,EXPIRY","RESERVE,RETEST","WEIGH,EXPIRY","WEIGH,RETEST","CHARGE,EXPIRY","CHARGE,RETEST"})
    void releasedLotAtDateBoundaryCannotCreateProductionFacts(String gate,String dateKind){assertDateBoundary(gate,dateKind);}
    @ParameterizedTest @CsvSource({"EXPIRY","RETEST"})
    void releasedLotAtDateBoundaryCannotConfirmIssue(String dateKind){assertDateBoundary("ISSUE",dateKind);}
    void assertDateBoundary(String gate,String dateKind){
        // Real receipt dates and signed QA chain; advance only the command's clock, never stored business facts.
        var source=stockQuery.receipt(1,jdbc.queryForObject("SELECT i.receipt_id FROM wms_material_receipt_item i JOIN md_material_lot l ON l.receipt_item_id=i.id WHERE l.id=?",Long.class,lot));
        var today=LocalDate.now(ZoneOffset.UTC);var due=today.plusDays(1);as(author);
        var item=new com.hospital.mes.wms.domain.WmsCommands.ReceiptItemInput(material,"DATE"+suffix,"SUP-DATE"+suffix,null,today.minusDays(1).toString(),dateKind.equals("EXPIRY")?due.toString():today.plusMonths(2).toString(),dateKind.equals("RETEST")?due.toString():null,"10",unit,null,2L,source.path("items").get(0).path("locationId").asText(),null,true,true,true,true,true);
        var receipt=wms.createReceipt(new com.hospital.mes.wms.domain.WmsCommands.ReceiptCreate("DATE-R"+suffix,source.path("supplierId").asText(),null,null,source.path("warehouseId").asText(),true,List.of(item)),key(),true);
        lot=receipt.path("items").get(0).path("materialLotId").asText();prepareGate(gate);as(author);String commandKey=key();
        long ledger=jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE material_lot_id=?",Long.class,lot);
        long reservations=jdbc.queryForObject("SELECT COUNT(*) FROM wms_reservation WHERE main_batch_id=?",Long.class,batchId);
        long weighed=jdbc.queryForObject("SELECT COUNT(*) FROM mes_weighing_record WHERE material_lot_id=?",Long.class,lot);
        long charged=jdbc.queryForObject("SELECT COUNT(*) FROM mes_material_charge WHERE material_lot_id=?",Long.class,lot);
        var at=due.atStartOfDay().toInstant(ZoneOffset.UTC);
        try(var commandClock=mockStatic(Instant.class,CALLS_REAL_METHODS)){
            commandClock.when(Instant::now).thenReturn(at);
            var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED);
            assertThatThrownBy(()->tx.execute(s->command(gate,commandKey)))
                .isInstanceOfSatisfying(MesException.class,e->{assertThat(e.code()).isEqualTo("MATERIAL_NOT_ELIGIBLE");assertThat(e.getMessage()).contains(dateKind.equals("EXPIRY")?"EXPIRED":"RETEST_DUE");});
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,commandKey)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE material_lot_id=?",Long.class,lot)).isEqualTo(ledger);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_reservation WHERE main_batch_id=?",Long.class,batchId)).isEqualTo(reservations);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_weighing_record WHERE material_lot_id=?",Long.class,lot)).isEqualTo(weighed);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_material_charge WHERE material_lot_id=?",Long.class,lot)).isEqualTo(charged);
        assertThat(jdbc.queryForObject("SELECT quality_status FROM md_material_lot WHERE id=?",String.class,lot)).isEqualTo("RELEASED");
        if(gate.equals("ISSUE"))assertThat(jdbc.queryForObject("SELECT status FROM wms_material_issue WHERE id=?",String.class,issueId)).isEqualTo("DRAFT");
    }
    @ParameterizedTest @CsvSource({"RESERVE,true","RESERVE,false","ISSUE,true","ISSUE,false","WEIGH,true","WEIGH,false","CHARGE,true","CHARGE,false"})
    void finalGateSerializesWithActualSignedQualitySupersession(String gate,boolean rejectionFirst) throws Exception {
        prepareGate(gate);commitFixture();as(author);var productionActor=actorContext.get();as(qa);var qualityActor=actorContext.get();
        String commandKey=key();long ledgerBefore=jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE material_lot_id=?",Long.class,lot);
        Callable<JsonNode> consume=()->command(gate,commandKey),reject=()->rejectLot();
        var result=race("SELECT id FROM md_material_lot WHERE id=? FOR UPDATE",lot,rejectionFirst?qualityActor:productionActor,rejectionFirst?reject:consume,rejectionFirst?productionActor:qualityActor,rejectionFirst?consume:reject);
        assertThat(result.firstError()).isNull();assertThat(result.first()).isNotNull();
        if(rejectionFirst){
            assertThat(result.secondError()).isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isIn("MATERIAL_NOT_ELIGIBLE","CONCURRENT_MODIFICATION"));
            // MariaDB 1020 is the existing controlled409 contract; a fresh transaction must observe the final quality refusal.
            as(author);assertThatThrownBy(()->new TransactionTemplate(transactions).execute(s->command(gate,commandKey)))
                .isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("MATERIAL_NOT_ELIGIBLE"));
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,commandKey)).isZero();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE material_lot_id=?",Long.class,lot)).isEqualTo(ledgerBefore);
        }else{
            if(result.secondError()!=null){
                assertThat(result.secondError()).isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("CONCURRENT_MODIFICATION"));
                as(qa);JsonNode rejection=new TransactionTemplate(transactions).execute(s->rejectLot());assertThat(rejection).isNotNull();
            }else assertThat(result.second()).isNotNull();
            as(author);JsonNode replay=new TransactionTemplate(transactions).execute(s->command(gate,commandKey));assertThat(replay).isEqualTo(result.first());
        }
        assertThat(jdbc.queryForObject("SELECT quality_status FROM md_material_lot WHERE id=?",String.class,lot)).isEqualTo("REJECTED");
    }
}
