/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

public abstract class ToFhirMapperSpecimen
    implements ValueMapper<PathoInputBase, Bundle.BundleEntryComponent> {

  protected static final String META_SOURCE = "#nexus-pathology";

  protected final FhirProperties fhirProperties;

  public ToFhirMapperSpecimen(final FhirProperties fhirProperties) {
    this.fhirProperties = fhirProperties;
  }

  public abstract Resource map(PathoInputBase input);

  public abstract Bundle.BundleEntryComponent apply(PathoInputBase input);

  protected Bundle.BundleEntryRequestComponent buildPutRequest(
      Resource resource, String identifierSystem) {
    var request = new Bundle.BundleEntryRequestComponent();

    return request
        .setMethod(Bundle.HTTPVerb.PUT)
        .setUrl(
            String.format(
                "%s?identifier=%s|%s",
                resource.getResourceType().name(), identifierSystem, resource.getId()));
  }
}
