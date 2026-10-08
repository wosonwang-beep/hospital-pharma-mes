package com.hospital.mes.reporting;
import com.hospital.mes.reporting.application.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class EditorSafetyTest {
 private final Clock clock=Clock.fixed(Instant.parse("2026-10-08T00:00:00Z"),ZoneOffset.UTC);
 @Test void capabilityRejectsWrongAudienceTamperAndExpiry(){var jwt=new EditorJwt("test-only-secret-32-characters-long",clock);String token=jwt.sign(Map.of("aud","document","sid","a","exp",clock.instant().plusSeconds(60).getEpochSecond()));assertEquals("a",jwt.verify(token,"document").path("sid").asText());assertThrows(RuntimeException.class,()->jwt.verify(token,"callback"));assertThrows(RuntimeException.class,()->jwt.verify(token+"x","document"));String expired=jwt.sign(Map.of("aud","document","exp",clock.instant().minusSeconds(1).getEpochSecond()));assertThrows(RuntimeException.class,()->jwt.verify(expired,"document"));}
 @Test void callbackDownloadRejectsOtherOriginAndCredentials(){EditorDownloadPolicy.validate("https://office.internal","https://office.internal/cache/files/a/output.docx");for(String url:List.of("https://evil.internal/a","http://office.internal/a","https://office.internal.evil/a","https://user:pass@office.internal/a","https://office.internal:444/a"))assertThrows(IllegalArgumentException.class,()->EditorDownloadPolicy.validate("https://office.internal",url));}
}
