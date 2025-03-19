/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.DummyDataUtilTest;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierResourceType;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class MicroscopicGrouperMapperTest<T extends ToFhirMapper> extends BasePathoGrouperMapperTest<T> {

  public MicroscopicGrouperMapperTest() {
    //noinspection unchecked
    super((Class<T>) MicroscopicGrouperMapper.class);
  }

  @Override
  public void setBaseIdentifierType() {
    baseIdentifierType = PathologyIdentifierResourceType.MICROSCOPIC_GROUPER;
  }

  @Test
  public void testMap() {
    if (fixture instanceof MicroscopicGrouperMapper microscopicGrouperMapper) {
      final PathoReport input = DummyDataUtilTest.getDummyReport();
      ArrayList<String> identifiers = new ArrayList<>();
      identifiers.add("H/20223/00001-Hauptbefund");
      String idSystems = fhirProperties.getSystems().getPathoFindingMacroId();

      var result = microscopicGrouperMapper.map(input, identifiers, idSystems);
      // Identifier
      assertThat(result.getIdentifier().getFirst().getValue()).isEqualTo(identifiers.getFirst());
      assertThat(result.getIdentifier().getFirst().getSystem()).isEqualTo("GrouperMicroId");
    }
  }
}
