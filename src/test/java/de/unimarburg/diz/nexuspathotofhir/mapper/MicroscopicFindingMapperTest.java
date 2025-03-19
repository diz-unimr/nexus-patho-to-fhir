/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.DummyDataUtilTest;
import org.junit.jupiter.api.Test;

class MicroscopicFindingMapperTest<T extends ToFhirMapper> extends BasePathoFindingMapperTest<T> {

  public MicroscopicFindingMapperTest() {
    //noinspection unchecked
    super((Class<T>) PathoFindingMicroMapper.class);
  }

  @Test
  public void testMap() {
    if (fixture instanceof PathoFindingMicroMapper pathoFindingMicroMapper) {
      final PathoReport input = DummyDataUtilTest.getDummyReport();

      String identifiers = "xzy-Hauptbefund-0";
      String idSystems = fhirProperties.getSystems().getPathoFindingMicroId();

      String code = "ABC";
      String value = "112";
      var result = pathoFindingMicroMapper.map(input, identifiers, idSystems, code, value);
      // Identifier
      assertThat(result.getIdentifier().getFirst().getValue()).isEqualTo(identifiers);
      assertThat(result.getIdentifier().getFirst().getSystem()).isEqualTo("PathoFindingMicroId");
    }
  }
}
