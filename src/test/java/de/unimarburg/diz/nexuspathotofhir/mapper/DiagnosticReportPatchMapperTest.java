/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
  }
}
