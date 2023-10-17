/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;

public abstract class ToFhirMapper
    implements ValueMapper<PathoReport, Bundle.BundleEntryComponent> {
  protected final FhirProperties fhirProperties;

  public ToFhirMapper(final FhirProperties fhirProperties) {
    this.fhirProperties = fhirProperties;
  }

  public abstract Resource map(PathoReport input);

  public Observation mapBaseGrouper(PathoReport input) {
    if (input == null) return null;
    final Observation result = new Observation();
    result.setMeta(new Meta().setSource("#nexus-pathology"));
    result.setEncounter(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Encounter", input.getFallnummer(), fhirProperties.getSystems().getEncounterId()));

    result.setSubject(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Patient", input.getPatientennummer(), fhirProperties.getSystems().getPatientId()));
    return result;
  }

  public abstract Bundle.BundleEntryComponent apply(PathoReport input);

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