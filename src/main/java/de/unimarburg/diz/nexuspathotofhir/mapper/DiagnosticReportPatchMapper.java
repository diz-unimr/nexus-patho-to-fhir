/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReportInputBase;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

public class DiagnosticReportPatchMapper extends ToFhirMapper {

  public DiagnosticReportPatchMapper(FhirProperties fhirProperties, CsvMappings csvMappings) {
    super(fhirProperties, csvMappings);
  }

  @Override
  public Resource map(PathoReportInputBase input) {
    return null;
  }

  @Override
  public Bundle.BundleEntryComponent apply(PathoReportInputBase input) {
    return null;
  }
}
