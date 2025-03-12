/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirConfiguration;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.DummyDataUtilTest;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
      FhirConfiguration.class,
      FhirProperties.class,
      CsvMappings.class,
      DiagnosticReportMapper.class
    })
class DiagnosticReportMapperTest {

  @Autowired DiagnosticReportMapper diagnosticReportMapper;

  @Test
  void map_empty_is_illegal_argument() {
    Throwable thrownFindings = catchThrowable(() -> diagnosticReportMapper.map(new PathoReport()));
    assertThat(thrownFindings)
        .as("invalid input will not be accepted")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void mapMinimalDiagosticReport() {
    final PathoReport input = DummyDataUtilTest.getDummyReport();
    var result = diagnosticReportMapper.map(input);
    assertThat(result).isNotNull();
    assertThat(result.fhirType()).isEqualTo("DiagnosticReport");
    assertThat(result.getEncounter()).isInstanceOf(Reference.class);
    assertThat(result.getEncounter().getReference())
        .isEqualTo(
            IdentifierAndReferenceUtil.getReferenceTo(
                    "Encounter",
                    input.getFallnummer(),
                    "https://your-local-system/pathology/encounterId")
                .getReference());

    assertThat(result.getSubject()).isInstanceOf(Reference.class);
    assertThat(result.getSubject().getReference())
        .isEqualTo(
            IdentifierAndReferenceUtil.getReferenceTo(
                    "Patient",
                    input.getPatientennummer(),
                    "https://your-local-system/pathology/patientId")
                .getReference());
  }
}
