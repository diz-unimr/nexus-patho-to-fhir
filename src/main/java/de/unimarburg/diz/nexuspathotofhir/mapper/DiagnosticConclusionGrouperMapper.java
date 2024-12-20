/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Represents german diagnostic report section named 'Diagnostische Schlussfolgerung' It is
 * referenced by its findings (observations)
 *
 * <p>FIXME: profile allows/suggests multiple grouper instance one per specimen referenced within a
 * report - FIXME check if multiple specimen references applicable to nexus
 */
@Service
public class DiagnosticConclusionGrouperMapper extends ToFhirMapper {
  private final Logger log = LoggerFactory.getLogger(DiagnosticConclusionGrouperMapper.class);

  public DiagnosticConclusionGrouperMapper(FhirProperties fhirProperties) {
    super(fhirProperties);
  }

  @Override
  public Observation map(PathoInputBase inputBase) {
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");

    var result = super.mapBaseGrouper(input);
    if (result == null) return null;
    if (!hasDiagnosticConclusionData(input)) return null;

    result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierType.DIAGNOSTIC_CONCLUSION_GROUPER,
            fhirProperties.getSystems().getDiagnosticConclusionGrouperId()));

    return result;
  }

  protected boolean hasDiagnosticConclusionData(PathoReport input) {
    // FIXME: implement
    return true;
  }

  @Override
  public Bundle.BundleEntryComponent apply(PathoInputBase value) {
    var mapped = map(value);
    return new Bundle.BundleEntryComponent()
        .setResource(mapped)
        .setRequest(buildPutRequest(mapped, mapped.getIdentifierFirstRep().getSystem()));
  }
}
