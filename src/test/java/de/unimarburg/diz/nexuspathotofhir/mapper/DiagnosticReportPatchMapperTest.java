/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import static org.assertj.core.api.Assertions.*;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirConfiguration;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.DummyDataUtilTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
      FhirProperties.class,
      FhirConfiguration.class,
      CsvMappings.class,
      DiagnosticReportPatchMapper.class
    })
public class DiagnosticReportPatchMapperTest {

  @Autowired DiagnosticReportPatchMapper diagnosticReportPatchMapper;

  @Test
  void map_empty_is_illegal_argument() {
    Throwable thrown = catchThrowable(() -> diagnosticReportPatchMapper.map(new PathoReport()));
    assertThat(thrown).as("invalid input will not be accepted");
  }

  @Test
  void mapMinimalDiagosticReportPatch() {
    final PathoReport input = DummyDataUtilTest.getDummyReport();
    var result = diagnosticReportPatchMapper.map(input);
    assertThat(result).isNotNull();
    assertThat(result.fhirType()).isEqualTo("Parameters");
    assertThat(result.getParameter().getFirst().getName()).isEqualTo("operation");
    assertThat(result.getParameter().getFirst().getPart().getFirst().getName()).isEqualTo("type");
    assertThat(result.getParameter().getFirst().getPart().getFirst().getValue().toString())
        .isEqualTo("add");
    assertThat(result.getParameter().getFirst().getPart().get(2).getName()).isEqualTo("name");
    assertThat(result.getParameter().getFirst().getPart().get(2).getValue().toString())
        .isEqualTo("result");
    assertThat(result.getParameter().getFirst().getPart().getLast().getPart().getFirst().getName())
        .isEqualTo("reference");
    assertThat(
            result
                .getParameter()
                .getFirst()
                .getPart()
                .getLast()
                .getPart()
                .getFirst()
                .getValue()
                .toString())
        .isEqualTo(
            "Observation?identifier=https://your-local-system/pathology/patho-diagnostic-conclusion-grouper-id|H/20223/00001-Hauptbefund");
  }

  @Test
  void batchEntryComponent() {
    final PathoReport input = DummyDataUtilTest.getDummyReport();
    var result = diagnosticReportPatchMapper.apply(input);
    assertThat(result).isNotNull();
    assertThat(result.fhirType()).isEqualTo("Bundle.entry");
    assertThat(result.getRequest().getMethod().toString()).isEqualTo("PATCH");
    assertThat(result.getRequest().getUrl())
        .isEqualTo(
            "DiagnosticReport?identifier=https://your-local-system/pathology/diagnosticReportId|H/20223/00001");
  }
}
