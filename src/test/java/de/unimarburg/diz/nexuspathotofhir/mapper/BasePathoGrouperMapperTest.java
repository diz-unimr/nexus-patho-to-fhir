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
import java.util.Date;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public abstract class BasePathoGrouperMapperTest<T extends ToFhirMapper> {
  protected final Class<T> fixtureClass;
  public PathologyIdentifierResourceType baseIdentifierType = null;
  @Mock public FhirProperties fhirProperties;
  @Mock public CsvMappings csvMappings;
  @Mock public FhirProperties.FhirSystems fhirSystems = new FhirProperties.FhirSystems();
  protected T fixture;

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
    Mockito.when(fhirSystems.getPathoMacroGrouperId()).thenReturn("GrouperMacroId");
    Mockito.when(fhirSystems.getPathoMicroGrouperId()).thenReturn("GrouperMicroId");
    Mockito.when(fhirSystems.getPathoDiagnosticConclusionGrouperId())
        .thenReturn("GrouperDiagConId");
    Mockito.when(fhirSystems.getPatientId()).thenReturn("dummyPatientIdSystem");
    Mockito.when(fhirSystems.getEncounterId()).thenReturn("dummyEncounterSystem");
    Mockito.when(fhirSystems.getServiceRequestId()).thenReturn("dummyServiceRequestSystem");
    setBaseIdentifierType();
  }

  @Test
  void map_empty_is_illegal_argument() {
    ArrayList<String> refPathoFiningIds = new ArrayList<>();
    refPathoFiningIds.add("t1");
    String idSystems = "thisSystem";
    // Test Grouper
    Throwable thrownGrouper =
        catchThrowable(
            () -> fixture.mapBaseGrouper(new PathoReport(), refPathoFiningIds, idSystems));
    assertThat(thrownGrouper)
        .as("invalid input will not be accepted")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void mapMinimalPathoGrouper() {
    final PathoReport input = DummyDataUtilTest.getDummyReport();
    System.out.println(input);
    ArrayList<String> refPathoFiningIds = new ArrayList<>();
    refPathoFiningIds.add("t1");
    String idSystems = "thisSystem";
    var result = fixture.mapBaseGrouper(input, refPathoFiningIds, idSystems);
    assertThat(result).isNotNull();
    assertThat(result.fhirType()).isEqualTo("Observation");
    String dummyGrouperSystemName = "DummyGrouperSystemName";

    // Enconter
    assertThat(result.getEncounter()).isInstanceOf(Reference.class);
    assertThat(result.getEncounter().getReference())
        .isEqualTo(
            IdentifierAndReferenceUtil.getReferenceTo(
                    "Encounter", input.getFallnummer(), "dummyEncounterSystem")
                .getReference());

    // Patient
    assertThat(result.getSubject()).isInstanceOf(Reference.class);
    assertThat(result.getSubject().getReference())
        .isEqualTo(
            IdentifierAndReferenceUtil.getReferenceTo(
                    "Patient", input.getPatientennummer(), "dummyPatientIdSystem")
                .getReference());

    // ServiceRequest
    assertThat(result.getBasedOn().getFirst()).isInstanceOf(Reference.class);
    assertThat(result.getBasedOn().getFirst().getReference())
        .isEqualTo(
            IdentifierAndReferenceUtil.getReferenceTo(
                    "ServiceRequest", input.getAuftragsnummer(), "dummyServiceRequestSystem")
                .getReference());

    // Befunderstllungsdatum
    Date probeEntnahmeDatum = new Date(1719565355113L);
    DateTimeType dateTimeType = new DateTimeType(probeEntnahmeDatum);
    assertThat(result.getEffective().toString()).isEqualTo(dateTimeType.toString());
  }
}
