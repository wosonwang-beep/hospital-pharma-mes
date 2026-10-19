package com.hospital.mes.ebr.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.ebr.domain.EbrCommands.*;
import com.hospital.mes.ebr.domain.EbrDefinitionRules;
import com.hospital.mes.system.application.SystemReferenceQuery;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EbrRoleSchemaTest {
    Definition input(String id,String legacy,String code,String name){return new Definition(List.of(),List.of(new Form("F","Form",null,"1.0",1,List.of())),List.of(),List.of(new SignatureRule("FORM","F","VERIFY",legacy,true,1,true,id,code,name)),List.of(new ReviewRule("FORM","F","VERIFY",legacy,true,1,id,code,name)),"2.0");}
    SystemReferenceQuery roles(){var r=mock(SystemReferenceQuery.class);when(r.requireRoleId("290")).thenReturn(new SystemReferenceQuery.RoleOption("290","MES_QC_REVIEWER","QC reviewer",true));return r;}
    @Test void saveGeneratesBothSnapshotsFromTrustedRoleId(){var result=EbrRoleSchema.prepare(input("290",null,null,null),roles());assertEquals("MES_QC_REVIEWER",result.signatureRules().getFirst().roleCodeSnapshot());assertEquals("QC reviewer",result.reviewRules().getFirst().roleNameSnapshot());assertNull(result.signatureRules().getFirst().requiredRole());}
    @Test void clientSnapshotsAreRejected(){assertThrows(IllegalArgumentException.class,()->EbrRoleSchema.prepare(input("290",null,"FORGED","FORGED"),roles()));}
    @Test void oldCodeCannotBeSubmittedAsSchema2Authority(){assertThrows(IllegalArgumentException.class,()->EbrRoleSchema.prepare(input("290","MES_QC_REVIEWER",null,null),roles()));}
    @Test void missingIdNeverFallsBackToMatchingCode(){assertTrue(EbrDefinitionRules.lint(input(null,"MES_QC_REVIEWER",null,null),false).stream().anyMatch(i->i.code().equals("REQUIRED_ROLE_ID")));assertThrows(IllegalArgumentException.class,()->EbrRoleSchema.prepare(input(null,null,null,null),roles()));}
    @Test void disabledOrUnknownRoleCannotBeSaved(){var r=roles();when(r.requireRoleId("290")).thenThrow(new java.util.NoSuchElementException());assertThrows(java.util.NoSuchElementException.class,()->EbrRoleSchema.prepare(input("290",null,null,null),r));}
    @Test void staleDisplayBlocksTransitionUntilNormalDraftSave(){var r=roles();assertThrows(IllegalArgumentException.class,()->EbrRoleSchema.validate("2.0","290",null,"OLD","old",false,r));var fresh=EbrRoleSchema.prepare(input("290",null,null,null),r).signatureRules().getFirst();assertDoesNotThrow(()->EbrRoleSchema.validate("2.0",fresh.requiredRoleId(),null,fresh.roleCodeSnapshot(),fresh.roleNameSnapshot(),false,r));}
    @Test void frozenReadNeverJoinsCurrentRoles(){var r=roles();EbrRoleSchema.validate("2.0","290",null,"OLD","Old frozen label",true,r);verifyNoInteractions(r);}
    @Test void legacyFrozenReadNeverReauthorizes(){var r=roles();EbrRoleSchema.validate(null,null,"OLD",null,null,true,r);verifyNoInteractions(r);}
    @Test void frozenSchema2StillRequiresCompleteSnapshots(){assertThrows(IllegalArgumentException.class,()->EbrRoleSchema.validate("2.0",null,null,"OLD","Old",true,roles()));assertThrows(IllegalArgumentException.class,()->EbrRoleSchema.validate("2.0","290",null,"OLD",null,true,roles()));}
    @Test void copyConvertsOnlyNewDraftUsingUniqueLegacyMapping(){var r=roles();when(r.enabledRoleByCode("OLD")).thenReturn(new SystemReferenceQuery.RoleOption("290","MES_QC_REVIEWER","QC reviewer",true));var old=new Definition(List.of(),List.of(),List.of(),List.of(new SignatureRule("TEMPLATE","T","VERIFY","OLD",true,1,true)),List.of());var copy=EbrRoleSchema.copyInput(old,r);assertEquals("2.0",copy.schemaVersion());assertEquals("290",copy.signatureRules().getFirst().requiredRoleId());assertNull(copy.signatureRules().getFirst().roleCodeSnapshot());assertEquals("OLD",old.signatureRules().getFirst().requiredRole());}
    @Test void legacyJsonDoesNotGainNewNullFields()throws Exception{var json=new ObjectMapper();var old=new Definition(List.of(),List.of(),List.of(),List.of(new SignatureRule("FORM","F","VERIFY","OLD",true,1,true)),List.of());var tree=json.valueToTree(old);assertFalse(tree.has("schemaVersion"));var rule=tree.path("signatureRules").get(0);assertFalse(rule.has("requiredRoleId"));assertFalse(rule.has("roleNameSnapshot"));assertFalse(rule.has("roleCodeSnapshot"));assertEquals("OLD",rule.path("requiredRole").asText());}
    @Test void policyEvidenceBindsIdAndSnapshotWithoutLiveRoleLookup(){var old=new SignatureRule("FORM","F","VERIFY","OLD",true,1,true);assertEquals("VERIFY|FORM|F|1",EbrRoleEvidence.policy(old));var current=new SignatureRule("FORM","F","VERIFY",null,true,1,true,"290","NEW","QC reviewer");assertTrue(EbrRoleEvidence.policy(current).startsWith("2.0|290|VERIFY|FORM|F|1|"));assertNotEquals(EbrRoleEvidence.policy(current),EbrRoleEvidence.policy(new SignatureRule("FORM","F","VERIFY",null,true,1,true,"291","NEW","QC reviewer")));}
}
