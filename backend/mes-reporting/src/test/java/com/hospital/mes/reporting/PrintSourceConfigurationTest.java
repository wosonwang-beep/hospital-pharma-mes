package com.hospital.mes.reporting;
import com.hospital.mes.reporting.domain.PrintSourceConfiguration;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.*;
class PrintSourceConfigurationTest {
 private final ObjectMapper json=new ObjectMapper();
 @Test void approvedGoldenCanonicalStringsAndHashesRemainExact()throws Exception{
  var vectors=json.readTree(Files.readString(Path.of("../../docs/development/PRINT_SOURCE_HASH_GOLDEN_V1.json"))).path("vectors");assertThat(vectors.size()).isEqualTo(7);
  for(var vector:vectors){var projection=vector.path("normalizedProjection");if(vector.path("kind").asText().equals("configHash"))assertThat(PrintSourceConfiguration.normalize(vector.path("input"))).as(vector.path("name").asText()).isEqualTo(projection);assertThat(PrintSourceConfiguration.canonical(projection,json)).isEqualTo(vector.path("canonicalString").asText());assertThat(PrintSourceConfiguration.hash(projection,json)).isEqualTo(vector.path("sha256").asText());}
 }
 @Test void zeroFalseAndPreciseDecimalsAreNotMissing()throws Exception{
  var field=(ObjectNode)json.readTree("{\"dataType\":\"NUMBER\",\"missing\":\"ERROR\",\"label\":\"数量\",\"format\":{\"kind\":\"NUMBER\",\"scale\":3,\"roundingMode\":\"HALF_UP\",\"dateStyle\":null}}");
  assertThat(PrintSourceConfiguration.display("0.000000",field)).isEqualTo("0.000");assertThat(PrintSourceConfiguration.display("1.234500",field)).isEqualTo("1.235");assertThat(PrintSourceConfiguration.raw("1.234500","NUMBER")).isEqualTo("1.234500");field.put("dataType","BOOLEAN");field.set("format",json.readTree("{\"kind\":\"RAW\",\"scale\":null,\"roundingMode\":null,\"dateStyle\":null}"));assertThat(PrintSourceConfiguration.display(false,field)).isEqualTo(false);assertThat(PrintSourceConfiguration.raw(1,"BOOLEAN")).isEqualTo(true);assertThatThrownBy(()->PrintSourceConfiguration.display(null,field)).hasMessageContaining("数量");
 }
 @Test void exactNumericDefaultAndUnknownStateAreRejected()throws Exception{
  var vector=json.readTree(Files.readString(Path.of("../../docs/development/PRINT_SOURCE_HASH_GOLDEN_V1.json"))).path("vectors").get(0);var config=(ObjectNode)vector.path("input").deepCopy();config.put("sourceStatus","DISABLED");assertThatThrownBy(()->PrintSourceConfiguration.normalize(config)).hasMessageContaining("Unknown field");assertThatThrownBy(()->PrintSourceConfiguration.validateValue("NUMBER",new TextNode("-"))).hasMessageContaining("decimal");assertThatThrownBy(()->PrintSourceConfiguration.key("field_key","[A-Za-z][A-Za-z0-9]{0,79}")).hasMessageContaining("identifier");
 }
}
