package com.hospital.mes.audit.attachment;

import com.hospital.mes.audit.application.*;
import com.hospital.mes.audit.attachment.application.*;
import com.hospital.mes.audit.attachment.infrastructure.AttachmentMapper;
import com.hospital.mes.audit.idempotency.PlatformIdempotencyService;
import com.hospital.mes.common.exception.PermissionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Set;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GeneratedAttachmentAccessTest {
    CurrentPlatformContext context(long org,Set<String> permissions){return new CurrentPlatformContext(org,3,Set.of("QA"),permissions,"session","request");}
    @Test void generatedEvidenceRequiresItsBusinessPermissionAndCurrentOwner(){
        var resolver=mock(CurrentPlatformContextResolver.class);var mapper=mock(AttachmentMapper.class);
        var service=new AttachmentService(mapper,resolver,mock(PlatformIdempotencyService.class),mock(AuditApplicationService.class),new ObjectMapper());
        when(resolver.current()).thenReturn(context(1,Set.of("attachment:upload")));
        assertThatThrownBy(()->service.createGenerated(context(1,Set.of()),"eBR.pdf","application/pdf",new byte[]{1})).isInstanceOf(PermissionException.class);
        when(resolver.current()).thenReturn(context(1,Set.of("ebr:pdf:generate")));
        assertThatThrownBy(()->service.createGenerated(context(2,Set.of()),"eBR.pdf","application/pdf",new byte[]{1})).isInstanceOf(PermissionException.class);
        verifyNoInteractions(mapper);
    }
    @Test @SuppressWarnings("unchecked") void businessReadDoesNotGrantUnassociatedFileAccess(){
        var resolver=mock(CurrentPlatformContextResolver.class);var mapper=mock(AttachmentMapper.class);
        var service=new AttachmentService(mapper,resolver,mock(PlatformIdempotencyService.class),mock(AuditApplicationService.class),new ObjectMapper());
        var policies=mock(ObjectProvider.class);var policy=mock(AttachmentAccessPolicy.class);
        when(policies.orderedStream()).thenAnswer(inv->Stream.of(policy));ReflectionTestUtils.setField(service,"accessPolicies",policies);
        var c=context(1,Set.of("ebr:form:view"));when(resolver.current()).thenReturn(c);
        when(policy.mayRead(c,17)).thenReturn(false);
        assertThatThrownBy(()->service.download("17")).isInstanceOf(PermissionException.class);
        verify(policy).mayRead(c,17);verifyNoInteractions(mapper);
    }
}
