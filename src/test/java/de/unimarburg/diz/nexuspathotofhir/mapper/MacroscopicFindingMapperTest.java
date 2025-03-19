/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.DummyDataUtilTest;
import org.junit.jupiter.api.Test;

class MacroscopicFindingMapperTest<T extends ToFhirMapper> extends BasePathoFindingMapperTest<T> {

  public MacroscopicFindingMapperTest() {
    //noinspection unchecked
    super((Class<T>) PathoFindingMacroMapper.class);
  }

  @Test
  public void testMap() {
    if (fixture instanceof PathoFindingMacroMapper pathoFindingMacroMapper) {
      final PathoReport input = DummyDataUtilTest.getDummyReport();

      String identifiers = "xzy-Hauptbefund-0";
      String idSystems = fhirProperties.getSystems().getPathoFindingMacroId();

      String code = "ABC";
      String value = "112";
      var result = pathoFindingMacroMapper.map(input, identifiers, idSystems, code, value);
      // Identifier
      assertThat(result.getIdentifier().getFirst().getValue()).isEqualTo(identifiers);
      assertThat(result.getIdentifier().getFirst().getSystem()).isEqualTo("PathoFindingMacroId");
    }
  }
}
