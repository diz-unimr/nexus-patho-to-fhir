/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {CsvMappings.class})
class CsvMappingsTest {

  @Autowired CsvMappings fixture;

  @Test
  void specimenTypes() throws Exception {
    assertThat(fixture.specimenTypes().size()).isGreaterThan(10);
  }

  @Test
  void specimenExtractionMethod() throws Exception {
    assertThat(fixture.specimenExtractionMethod().size()).isGreaterThan(10);
  }
}
