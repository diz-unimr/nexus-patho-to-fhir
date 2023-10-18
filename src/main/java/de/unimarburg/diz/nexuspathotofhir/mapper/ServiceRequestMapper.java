/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ServiceRequestMapper extends ToFhirMapper {
  private final Logger log = LoggerFactory.getLogger(ServiceRequestMapper.class);

  public ServiceRequestMapper(FhirProperties fhirProperties) {
    super(fhirProperties);
  }

  @Override
  public ServiceRequest map(PathoInputBase input) {
    log.debug(
        String.format(
            "creating service_request '%s' from patho-guid '%s'",
            input.getAuftragnummer(), input.getUUID()));

    ServiceRequest result = new ServiceRequest();
    result.setMeta(new Meta().setSource("#nexus-pathology"));

    result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierType.SERVICE_REQUEST,
            fhirProperties.getSystems().getServiceRequestId()));
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
