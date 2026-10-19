package com.hospital.mes.ebr.application;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.common.exception.PermissionException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class EbrRoleAuthorizationTest {
    CurrentPlatformContext actor(){return new CurrentPlatformContext(1,11,Set.of("STALE"),Set.of("ebr:sign"),"s","r",Set.of("290"),2);}
    @Test void legacySessionRequiresReloginBeforeNewSigning(){var port=mock(CurrentSigningAuthority.class);var service=new EbrRoleAuthorization(port);assertThrows(PermissionException.class,()->service.current(new CurrentPlatformContext(1,11,Set.of("OLD"),Set.of("ebr:sign"),"s","r"),"ebr:sign"));verifyNoInteractions(port);}
    @Test void currentAuthorityOverridesCachedRoleIds(){var port=mock(CurrentSigningAuthority.class);var fresh=new CurrentPlatformContext(1,11,Set.of("NEW"),Set.of("ebr:sign"),"s","r",Set.of("291"),2);when(port.requireCurrent(actor(),Set.of("ebr:sign"))).thenReturn(fresh);var result=new EbrRoleAuthorization(port).current(actor(),"ebr:sign");assertFalse(EbrRoleAuthorization.matches("2.0","290","STALE",result));assertTrue(EbrRoleAuthorization.matches("2.0","291",null,result));}
    @Test void revokedPermissionRejectsOldTokenAtCommandBoundary(){var port=mock(CurrentSigningAuthority.class);when(port.requireCurrent(actor(),Set.of("ebr:sign"))).thenThrow(new PermissionException("PERMISSION_DENIED","revoked"));assertThrows(PermissionException.class,()->new EbrRoleAuthorization(port).current(actor(),"ebr:sign"));}
    @Test void missingSchema2IdHasNoCodeFallback(){assertFalse(EbrRoleAuthorization.matches("2.0",null,"STALE",actor()));}
    @Test void sameRoleIdSurvivesCodeAndLabelChange(){assertTrue(EbrRoleAuthorization.matches("2.0","290","DIFFERENT_CODE",actor()));}
}
