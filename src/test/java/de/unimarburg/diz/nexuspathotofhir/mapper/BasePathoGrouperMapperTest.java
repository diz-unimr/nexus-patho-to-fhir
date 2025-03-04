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
import org.hl7.fhir.r4.model.Observation;
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
  protected T fixture;

  public BasePathoGrouperMapperTest(Class<T> fixtureClass) {
    this.fixtureClass = fixtureClass;
  }

  /** must set target PathologyIdentifierType */
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

    Throwable thrown = catchThrowable(() -> fixture.map(new PathoReport()));
    assertThat(thrown)
        .as("invalid input will not be accepted")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void map_minimal() {
    final PathoReport input = DummyDataUtilTest.getDummyReport();
    System.out.println(input);

    var result = fixture.map(input);

    assertThat(result).isNotNull();
    assertThat(result.fhirType()).isEqualTo("Observation");

    final Observation grouperObservation = (Observation) result;
    var identifier = grouperObservation.getIdentifierFirstRep();

    // assertThat(identifier.getSystem()).isEqualTo(dummyGrouperSystemName);
    // assertThat(identifier.getValue()).contains(baseIdentifierType.name());

    assertThat(grouperObservation.getEncounter()).isInstanceOf(Reference.class);
    assertThat(grouperObservation.getEncounter().getReference())
        .isEqualTo(
            IdentifierAndReferenceUtil.getReferenceTo(
                    "Encounter", input.getFallnummer(), "dummyEncounterSystem")
                .getReference());

    assertThat(grouperObservation.getSubject()).isInstanceOf(Reference.class);
    assertThat(grouperObservation.getSubject().getReference())
        .isEqualTo(
            IdentifierAndReferenceUtil.getReferenceTo(
                    "Patient", input.getPatientennummer(), "dummyPatientIdSystem")
                .getReference());
  }
}
