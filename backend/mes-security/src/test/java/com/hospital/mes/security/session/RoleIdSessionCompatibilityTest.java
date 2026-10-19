package com.hospital.mes.security.session;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RoleIdSessionCompatibilityTest {
    Map<Object,Object> old(){var m=new HashMap<Object,Object>();m.put("userId","11");m.put("organizationId","1");m.put("loginName","user");m.put("displayName","User");m.put("roleCodes","[\"OLD\"]");m.put("permissionCodes","[\"ebr:sign\"]");m.put("mustChange","false");m.put("createdAt","1000");m.put("absoluteExpiresAt","20000");m.put("idleExpiresAt","10000");return m;}
    RedisSessionStore store(){return new RedisSessionStore(mock(StringRedisTemplate.class),new ObjectMapper(),Clock.systemUTC());}
    @Test void oldRedisSnapshotRemainsLegacyWithoutGuessingIds(){var identity=store().fromHash("s",old()).identity();assertEquals(1,identity.identityFormatVersion());assertTrue(identity.roleIds().isEmpty());assertEquals(Set.of("OLD"),identity.roleCodes());}
    @Test void newSnapshotPreservesTrustedRoleIds(){var m=old();m.put("roleIds","[\"290\",\"291\"]");m.put("identityFormatVersion","2");var identity=store().fromHash("s",m).identity();assertEquals(2,identity.identityFormatVersion());assertEquals(Set.of("290","291"),identity.roleIds());}
    @Test void missingFormatDoesNotSilentlyUpgradeOldSession(){var m=old();m.put("roleIds","[\"290\"]");assertEquals(1,store().fromHash("s",m).identity().identityFormatVersion());}
}
