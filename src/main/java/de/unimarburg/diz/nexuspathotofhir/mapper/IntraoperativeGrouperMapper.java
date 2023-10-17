/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Service;

@Service
public class IntraoperativeGrouperMapper extends ToFhirMapper {
  public IntraoperativeGrouperMapper(FhirProperties fhirProperties) {
    super(fhirProperties);
  }

  @Override
  public Observation map(PathoReport input) {
    var result = super.mapBaseGrouper(input);

    result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierType.INTERAOPERATIVE_GROUPER,
            fhirProperties.getSystems().getInteroperativeGrouperId()));
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