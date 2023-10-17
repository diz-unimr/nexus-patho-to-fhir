/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MicroscopicGrouperMapper extends ToFhirMapper {
  private final Logger log = LoggerFactory.getLogger(MicroscopicGrouperMapper.class);

  public MicroscopicGrouperMapper(FhirProperties fhirProperties) {
    super(fhirProperties);
  }

  @Override
  public Observation map(PathoReport input) {
    log.debug(
        String.format(
            "creating MicroscopicGrouper from patho-guid '%s'", input.getPathologieBefundId()));
    var result = super.mapBaseGrouper(input);
    result.setMeta(new Meta().setSource("#nexus-pathology"));

    result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierType.MICROSCOPIC_GROUPER,
            fhirProperties.getSystems().getMicroscopicGrouperId()));
    return result;
  }

  @Override
  public Bundle.BundleEntryComponent apply(PathoReport value) {
    var mapped = map(value);
    return new Bundle.BundleEntryComponent()
        .setResource(mapped)
        .setRequest(buildPutRequest(mapped, mapped.getIdentifierFirstRep().getSystem()));
  }
}
