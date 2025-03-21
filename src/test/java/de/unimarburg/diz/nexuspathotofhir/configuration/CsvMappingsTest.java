/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.unimarburg.diz.nexuspathotofhir.model.MappingEntry;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {CsvMappings.class})
class CsvMappingsTest {

  @Autowired CsvMappings fixture;

  @Test
  void specimenTypes() throws Exception {
    final Map<String, MappingEntry> stringMappingEntryMap = fixture.specimenTypes();
    assertThat(stringMappingEntryMap.size()).isGreaterThan(10);
  }

  @Test
  void specimenExtractionMethod() throws Exception {
    final Map<String, MappingEntry> stringMappingEntryMap = fixture.specimenExtractionMethod();
    assertThat(stringMappingEntryMap.size()).isGreaterThan(10);
  }

  @Test
  void specimenContainerType() throws Exception {
    final Map<String, MappingEntry> stringMappingEntryMap = fixture.specimenContainerType();
    assertThat(stringMappingEntryMap.size()).isGreaterThan(10);
  }
}
