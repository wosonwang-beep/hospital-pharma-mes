package com.hospital.mes.ebr.domain;
import com.fasterxml.jackson.databind.*;
import com.hospital.mes.ebr.application.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.masterdata.application.MasterMutation;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class EbrRuntimeBoundaryTest {
 @Test void blockedSubmitCommitsWorkerBeforeFacadeRaisesError(){
  var worker=mock(EbrRuntimeTransactions.class);
  var context=new CurrentPlatformContext(1,2,Set.of("OPERATOR"),Set.of("ebr:form:submit"),"session","request");
  var body=new EbrRuntimeCommands.Command(0L,"reason");var tx=new RecordingTransactions();
  when(worker.command(context,"SUBMIT",3,body,"\"0\"","key")).thenReturn(EbrRuntimeTransactions.Outcome.blocked("EBR_RULE_BLOCKED","required",new ObjectMapper().createObjectNode()));
  var proxy=new ProxyFactory(worker);proxy.setProxyTargetClass(true);proxy.addAdvice(new TransactionInterceptor(tx,new AnnotationTransactionAttributeSource()));
  var mutation=mock(MasterMutation.class);when(mutation.context("ebr:form:submit")).thenReturn(context);
  var service=new EbrRuntimeService(null,null,null,(EbrRuntimeTransactions)proxy.getProxy(),mutation,null,null,null);
  assertThatThrownBy(()->service.command("SUBMIT","3",body,"\"0\"","key")).hasMessageContaining("required");
  assertThat(tx.committed).isTrue();assertThat(tx.rolledBack).isFalse();
 }
 @Test void requestSerializationNeverRetainsReauthentication(){
  var json=new ObjectMapper();var review=new EbrRuntimeCommands.Review(2L,"8","PASS","reviewed","release","secret-token");
  assertThat(json.valueToTree(review).has("reauthToken")).isFalse();assertThat(review.toString()).doesNotContain("secret-token");
 }
 @Test void evenPermissiveMapperRejectsSourceSpoofing()throws Exception{
  var json=new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  assertThatThrownBy(()->json.readValue("{\"fieldCode\":\"F\",\"occurrencePath\":\"\",\"value\":\"1\",\"sourceType\":\"INSTRUMENT\"}",EbrRuntimeCommands.Value.class)).hasMessageContaining("sourceType");
 }
 @Test void runtimeEvaluationExposesExactDeterministicDefaults(){
  var field=new EbrCommands.Field("A",null,"Default","NUMBER","MANUAL","DECIMAL",null,3,true,false,"1.250",null,null,1,null,List.of());
  var form=new EbrCommands.Form("F","Form","1","1",1,List.of(field));
  var definition=new EbrCommands.Definition(List.of(),List.of(form),List.of(),List.of(),List.of());
  var result=new EbrEngine(new ObjectMapper()).evaluateWithValues(definition,List.of(),"ON_SAVE",Set.of(),Map.of(),java.time.Instant.now(),(v,a,b)->v,"1:1");
  assertThat(result.result().allowed()).isTrue();assertThat(result.effectiveInputs().getFirst().values().getFirst().decimalValue()).isEqualByComparingTo("1.250");
 }
 static class RecordingTransactions extends AbstractPlatformTransactionManager {
  boolean committed,rolledBack;
  protected Object doGetTransaction(){return new Object();}
  protected void doBegin(Object transaction,TransactionDefinition definition){}
  protected void doCommit(DefaultTransactionStatus status){committed=true;}
  protected void doRollback(DefaultTransactionStatus status){rolledBack=true;}
 }
}
