/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.DummyDataUtilTest;
import org.junit.jupiter.api.Test;

public class DiagConFindingMapperTest<T extends ToFhirMapper>
    extends BasePathoFindingMapperTest<T> {

  public DiagConFindingMapperTest() {
    //noinspection unchecked
    super((Class<T>) PathoFindingDiagConclusionMapper.class);
  }

  @Test
  public void testMap() {
    if (fixture instanceof PathoFindingDiagConclusionMapper pathoFindingDiagConclusionMapper) {
      final PathoReport input = DummyDataUtilTest.getDummyReport();

      String identifiers = "xzy-Hauptbefund-0";
      String idSystems = fhirProperties.getSystems().getPathoFindingDiagnosticConclusionId();

      String code = "ABC";
      String value = "112";
      var result = pathoFindingDiagConclusionMapper.map(input, identifiers, idSystems, code, value);
      // Identifier
      assertThat(result.getIdentifier().getFirst().getValue()).isEqualTo(identifiers);
      assertThat(result.getIdentifier().getFirst().getSystem()).isEqualTo("PathoFindingDiagConcId");
    }
  }
}
