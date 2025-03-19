/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.DummyDataUtilTest;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierResourceType;
import java.lang.reflect.InvocationTargetException;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public abstract class BasePathoFindingMapperTest<T extends ToFhirMapper> {
  protected final Class<T> fixtureClass;
  public PathologyIdentifierResourceType baseIdentifierType = null;
  @Mock public FhirProperties fhirProperties;
  @Mock public CsvMappings csvMappings;
  @Mock public FhirProperties.FhirSystems fhirSystems = new FhirProperties.FhirSystems();
  protected T fixture;

  public BasePathoFindingMapperTest(Class<T> fixtureClass) {
    this.fixtureClass = fixtureClass;
  }

  @BeforeEach
  public void beforeEachBase()
      throws NoSuchMethodException,
          InvocationTargetException,
          InstantiationException,
          IllegalAccessException {
    MockitoAnnotations.openMocks(this);

    this.fixture =
        fixtureClass
            .getConstructor(FhirProperties.class, CsvMappings.class)
            .newInstance(fhirProperties, csvMappings);
    Mockito.when(fhirProperties.getSystems()).thenReturn(fhirSystems);

    Mockito.when(fhirSystems.getPathoFindingMacroId()).thenReturn("PathoFindingMacroId");
    Mockito.when(fhirSystems.getPathoFindingMicroId()).thenReturn("PathoFindingMicroId");
    Mockito.when(fhirSystems.getPathoFindingDiagnosticConclusionId())
        .thenReturn("PathoFindingDiagConcId");
    Mockito.when(fhirSystems.getPatientId()).thenReturn("dummyPatientIdSystem");
    Mockito.when(fhirSystems.getEncounterId()).thenReturn("dummyEncounterSystem");
  }

  @Test
  void map_empty_is_illegal_argument() {
    String id = "123";
    String idSystem = "patho-finding-system";
    String code = "ABC";
    String value = "Test";
    Throwable thrownFindings =
        catchThrowable(
            () -> fixture.mapBasePathoFinding(new PathoReport(), id, idSystem, code, value));
    assertThat(thrownFindings)
        .as("invalid input will not be accepted")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void mapMinimalPathoGrouper() {
    final PathoReport input = DummyDataUtilTest.getDummyReport();
    String id = "123";
    String idSystem = "patho-finding-system";
    String code = "ABC";
    String value = "Test";
    var result = fixture.mapBasePathoFinding(input, id, idSystem, code, value);
    assertThat(result).isNotNull();
    assertThat(result.fhirType()).isEqualTo("Observation");

    // assertThat(identifier.getSystem()).isEqualTo(dummyGrouperSystemName);
    // assertThat(identifier.getValue()).contains(baseIdentifierType.name());

    assertThat(result.getEncounter()).isInstanceOf(Reference.class);
    assertThat(result.getEncounter().getReference())
        .isEqualTo(
            IdentifierAndReferenceUtil.getReferenceTo(
                    "Encounter", input.getFallnummer(), "dummyEncounterSystem")
                .getReference());

    assertThat(result.getSubject()).isInstanceOf(Reference.class);
    assertThat(result.getSubject().getReference())
        .isEqualTo(
            IdentifierAndReferenceUtil.getReferenceTo(
                    "Patient", input.getPatientennummer(), "dummyPatientIdSystem")
                .getReference());
  }
}
