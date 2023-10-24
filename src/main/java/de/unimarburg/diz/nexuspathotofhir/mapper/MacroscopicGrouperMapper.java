/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Service;

@Service
public class MacroscopicGrouperMapper extends ToFhirMapper {
  public MacroscopicGrouperMapper(FhirProperties fhirProperties) {
    super(fhirProperties);
  }

  @Override
  public Observation map(PathoInputBase inputBase) {
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");
    var result = super.mapBaseGrouper(input);
    result.setMeta(new Meta().setSource("#nexus-pathology"));

    result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierType.MACROSCOPIC_GROUPER,
            fhirProperties.getSystems().getMacroscopicGrouperId()));
    return result;
  }

  @Override
  public Bundle.BundleEntryComponent apply(PathoInputBase value) {
    var mapped = map(value);
    return new Bundle.BundleEntryComponent()
        .setResource(mapped)
        .setRequest(buildPutRequest(mapped, mapped.getIdentifierFirstRep().getSystem()));
  }
}
