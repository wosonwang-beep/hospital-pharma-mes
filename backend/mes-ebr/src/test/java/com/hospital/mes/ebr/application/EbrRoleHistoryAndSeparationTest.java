package com.hospital.mes.ebr.application;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.audit.domain.*;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.ebr.domain.*;
import com.hospital.mes.ebr.infrastructure.*;
import com.hospital.mes.common.exception.PermissionException;
import java.util.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class EbrRoleHistoryAndSeparationTest {
    @Test void frozenHistoricalEvidenceRemainsValidAfterCurrentAuthorityChanges(){
        var query=mock(AuditQueryService.class);var evidence=new EbrRoleEvidence(mock(AuditApplicationService.class),query);
        var rule=new EbrCommands.SignatureRule("FORM","F","VERIFY",null,true,1,true,"290","OLD","Frozen reviewer");
        var signature=new SignatureRecord(1,1,11,SignatureMeaning.VERIFY,"EBR_FORM_INSTANCE","3","abc",Instant.now(),SignatureStatus.VALID,null,null,"{}",null,0);
        var event=new AuditEvent(1,1,11,"OLD","EBR_FORM_SIGNATURE_ROLE","EBR_SIGNATURE_POLICY","1",null,"abc",EbrRoleEvidence.policy(rule),null,Instant.now(),"tx","req",AuditSource.API);
        when(query.query(eq(1L),any())).thenReturn(new AuditEventPage(List.of(event),0,200,1));
        assertTrue(evidence.matches(signature,rule));
        // No current role, code, permission, or membership service participates in this check.
        assertTrue(evidence.matches(signature,rule));
        assertFalse(evidence.matches(signature,new EbrCommands.SignatureRule("FORM","F","VERIFY",null,true,1,true,"291","OLD","Frozen reviewer")));
    }
    @Test void multipleRolesCannotBypassSameSubmitterRestriction(){separation(true);}
    @Test void multipleRolesCannotBypassAnyFieldAuthorRestriction(){separation(false);}
    void separation(boolean submitter){
        var json=new ObjectMapper();var db=mock(EbrRuntimeStore.class);var model=mock(EbrRuntimeModel.class);var authority=mock(EbrRoleAuthorization.class);var signing=mock(SignatureApplicationService.class);
        var actor=new CurrentPlatformContext(1,11,Set.of("REVIEWER","QA"),Set.of("ebr:sign","ebr:review:verify"),"s","r",Set.of("290","291"),2);
        when(authority.current(actor,"ebr:sign","ebr:review:verify")).thenReturn(actor);
        var form=new RuntimeFormEntity();form.setId(3L);form.setOrgId(1L);form.setStatus("SUBMITTED");form.setRevision(1);form.setSubmittedBy(submitter?11L:12L);
        var rule=json.createObjectNode();rule.put("reviewType","VERIFY");rule.put("requiredRoleId","290");rule.put("independentUserRequired",true);
        when(model.reviewRule(form,8)).thenReturn(rule);when(model.definition(form)).thenReturn(new EbrCommands.Definition(List.of(),List.of(),List.of(),List.of(),List.of(),"2.0"));
        if(!submitter){var value=new RuntimeValueEntity();value.setRecordedBy(11L);when(db.values(form)).thenReturn(List.of(value));}
        var worker=new EbrRuntimeTransactions(db,model,null,null,json,signing,null,null,null,authority);
        var request=new EbrRuntimeCommands.Review(1L,"8","PASS","reviewed","test","token");
        assertThrows(PermissionException.class,()->ReflectionTestUtils.invokeMethod(worker,"review",actor,form,request,"key"));verifyNoInteractions(signing);
    }
}
