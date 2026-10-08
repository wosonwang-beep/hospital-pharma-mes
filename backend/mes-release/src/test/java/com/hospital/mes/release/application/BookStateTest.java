package com.hospital.mes.release.application;
import com.fasterxml.jackson.databind.*;import java.util.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class BookStateTest {
 private final ObjectMapper json=new ObjectMapper();
 private BookDefinition.Entry entry(boolean required,int min){return new BookDefinition.Entry("FILL","过程记录","分装记录","FORM",1,required,min,"BATCH","FILL","F_FILL",null,List.of("ACTUAL"),List.of());}
 @Test void missingAndNotApplicableAreDifferent(){assertEquals("MISSING",BookService.state(entry(true,1),List.of()));assertEquals("NOT_APPLICABLE",BookService.state(entry(false,0),List.of()));}
 @Test void draftAndPendingReviewCannotBecomeComplete(){assertEquals("DRAFT",BookService.state(entry(true,1),List.of(json.createObjectNode().put("status","DRAFT"))));assertEquals("PENDING_REVIEW",BookService.state(entry(true,1),List.of(json.createObjectNode().put("status","PENDING_REVIEW"))));assertEquals("MISSING",BookService.state(entry(true,2),List.of(json.createObjectNode().put("status","COMPLETE"))));}
 @Test void actorUiActionsAreExcludedFromStableSourceDigest()throws Exception{var a=json.readTree("{\"value\":12,\"allowedActions\":[\"SAVE\"],\"fields\":[{\"value\":1,\"readonly\":false}]}");var b=json.readTree("{\"value\":12,\"allowedActions\":[],\"fields\":[{\"value\":1,\"readonly\":true}]}");assertEquals(BookSourceAdapter.stable(a),BookSourceAdapter.stable(b));}
}
