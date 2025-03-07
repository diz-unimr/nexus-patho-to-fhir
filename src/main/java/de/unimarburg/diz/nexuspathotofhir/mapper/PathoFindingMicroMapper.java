/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReportInputBase;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PathoFindingMicroMapper extends ToFhirMapper {
  private final Logger log = LoggerFactory.getLogger(PathoFindingMicroMapper.class);

  public PathoFindingMicroMapper(FhirProperties fhirProperties, CsvMappings csvMappings) {
    super(fhirProperties, csvMappings);
  }

  public Observation map(
      PathoReportInputBase inputBase, String id, String idSystem, String code, String value) {
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");

    var pathoFinding = super.mapBasePathoFinding(input, id, idSystem, code, value);
    // CodeCategory
    pathoFinding.setCategory(
        List.of(
            new CodeableConcept()
                .setCoding(
                    List.of(
                        new Coding()
                            .setCode("laboratory")
                            .setSystem(
                                "http://terminology.hl7.org/CodeSystem/observation-category"))),
            new CodeableConcept()
                .setCoding(
                    List.of(new Coding().setCode("22635-7").setSystem("http://loinc.org")))));

    return pathoFinding;
  }

  @Nullable public Bundle.BundleEntryComponent apply(
      PathoReportInputBase value, String id, String idSystem, String code, String stringValue) {
    var mapped = map(value, id, idSystem, code, stringValue);
    if (mapped == null) return null;

    final Identifier identifierFirstRep = mapped.getIdentifierFirstRep();
    return buildBundleComponent(mapped, identifierFirstRep);
  }

  @NotNull protected Bundle.BundleEntryComponent buildBundleComponent(
      Observation mapped, Identifier identifierFirstRep) {
    final Bundle.BundleEntryComponent bundleEntryComponent =
        new Bundle.BundleEntryComponent()
            .setResource(mapped)
            .setRequest(buildPutRequest(mapped, identifierFirstRep.getSystem()));

    bundleEntryComponent.setRequest(
        new Bundle.BundleEntryRequestComponent()
            .setMethod(Bundle.HTTPVerb.PUT)
            .setUrl(
                String.format(
                    "%s?identifier=%s|%s",
                    mapped.fhirType(),
                    identifierFirstRep.getSystem(),
                    identifierFirstRep.getValue())));
    return bundleEntryComponent;
  }
}
