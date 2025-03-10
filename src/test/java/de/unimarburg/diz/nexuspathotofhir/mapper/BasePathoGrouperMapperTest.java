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
import java.util.ArrayList;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public abstract class BasePathoGrouperMapperTest<T extends ToFhirMapper> {
  private final Class<T> fixtureClass;
  public PathologyIdentifierResourceType baseIdentifierType = null;
  @Mock public FhirProperties fhirProperties;
  @Mock public CsvMappings csvMappings;
  @Mock public FhirProperties.FhirSystems fhirSystems = new FhirProperties.FhirSystems();
  private T fixture;

  public BasePathoGrouperMapperTest(Class<T> fixtureClass) {
    this.fixtureClass = fixtureClass;
  }

  public abstract void setBaseIdentifierType();

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
    String dummyGrouperSystemName = "DummyGrouperSystemName";
    Mockito.when(fhirSystems.getPathoFindingGrouperDigConcId()).thenReturn(dummyGrouperSystemName);
    Mockito.when(fhirSystems.getPathoFindingGrouperMacroId()).thenReturn(dummyGrouperSystemName);
    Mockito.when(fhirSystems.getPathoFindingGrouperMicroId()).thenReturn(dummyGrouperSystemName);
    Mockito.when(fhirSystems.getPatientId()).thenReturn("dummyPatientIdSystem");
    Mockito.when(fhirSystems.getEncounterId()).thenReturn("dummyEncounterSystem");
    setBaseIdentifierType();
  }

  @Test
  void map_empty_is_illegal_argument() {
    ArrayList<String> refPathoFiningIds = new ArrayList<>();
    refPathoFiningIds.add("t1");
    String idSystems = "thisSystem";
    Throwable thrown =
        catchThrowable(
            () -> fixture.mapBaseGrouper(new PathoReport(), refPathoFiningIds, idSystems));
    assertThat(thrown)
        .as("invalid input will not be accepted")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void map_minimal() {
    final PathoReport input = DummyDataUtilTest.getDummyReport();
    System.out.println(input);
    ArrayList<String> refPathoFiningIds = new ArrayList<>();
    refPathoFiningIds.add("t1");
    String idSystems = "thisSystem";

    var result = fixture.mapBaseGrouper(input, refPathoFiningIds, idSystems);

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
