package com.hospital.mes.reporting.infrastructure;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class ConverterEnvironmentTest {
 @Test void preservesWindowsFontDiscoveryWithoutInheritingCredentials(){
  var selected=Docx4jFopPdfConverter.systemEnvironment(Map.of("windir","C:\\Windows","SYSTEMROOT","C:\\Windows","ComSpec","cmd.exe","MES_DB_PASSWORD","secret","JAVA_TOOL_OPTIONS","unsafe"));
  assertThat(selected).containsExactlyInAnyOrderEntriesOf(Map.of("WINDIR","C:\\Windows","SystemRoot","C:\\Windows","ComSpec","cmd.exe"));
 }
}
