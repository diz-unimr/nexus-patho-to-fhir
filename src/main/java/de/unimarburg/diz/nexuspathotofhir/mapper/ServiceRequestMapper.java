/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReportInputBase;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierResourceType;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ServiceRequestMapper extends ToFhirMapper {
  private final Logger log = LoggerFactory.getLogger(ServiceRequestMapper.class);

  public ServiceRequestMapper(FhirProperties fhirProperties, CsvMappings csvMappings) {
    super(fhirProperties, csvMappings);
  }

  @Override
  public ServiceRequest map(PathoReportInputBase inputBase) {
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");

    log.debug(
        "creating service_request '{}' from patho-guid '{}'",
        input.getAuftragsnummer(),
        input.getUUID());

    ServiceRequest serviceRequest = new ServiceRequest();

    // Meta
    serviceRequest.setMeta(
        new Meta()
            .setSource("#nexus-pathology")
            .setProfile(
                List.of(
                    new CanonicalType(
                        "https://www.medizininformatik-initiative.de/fhir/ext/modul-patho/StructureDefinition/mii-pr-patho-service-request"))));

    // identifier
    serviceRequest.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifierWithType(
            input,
            PathologyIdentifierType.FILL,
            PathologyIdentifierResourceType.SERVICE_REQUEST,
            fhirProperties.getSystems().getServiceRequestId()));

    // encounter refefence
    serviceRequest.setEncounter(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Encounter", input.getFallnummer(), fhirProperties.getSystems().getEncounterId()));
    // subject refefence
    serviceRequest.setSubject(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Patient", input.getPatientennummer(), fhirProperties.getSystems().getPatientId()));

    // TODO: Add multiple specimen

    // ArraySpecimenrefence
    ArrayList<Reference> specimenRef = new ArrayList<>();
    if (StringUtils.hasText(input.getProbeID())) {
      log.debug("ProbeID is present");
      // Split the String by ','
      if (input.getProbeID().contains(",")) {
        log.debug("Contains multiple ProbeIDs");
        String[] arrayProbeID =
            Arrays.stream(input.getProbeID().split(",")).map(String::trim).toArray(String[]::new);
        for (String probeID : arrayProbeID) {
          specimenRef.add(
              IdentifierAndReferenceUtil.getReferenceTo(
                  "Specimen", probeID, fhirProperties.getSystems().getSpecimenId()));
        }
      } else {
        log.debug("Contains single ProbeID");
        specimenRef.add(
            IdentifierAndReferenceUtil.getReferenceTo(
                "Specimen", input.getProbeID(), fhirProperties.getSystems().getSpecimenId()));
      }
    }

    serviceRequest.setSpecimen(specimenRef);

    // status
    serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.COMPLETED);
    ///  Fixedvalue
    // intent
    serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);

    // requester / Organization/ FAB
    serviceRequest.setRequester(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Organization",
            input.getAuftragsgeberFABCode(),
            fhirProperties.getSystems().getOrganizationId()));
    // category - Fixed value: 726007
    serviceRequest.setCategory(
        List.of(
            new CodeableConcept()
                .setCoding(
                    List.of(
                        new Coding()
                            .setCode("726007")
                            .setSystem("http://snomed.info/sct")
                            .setDisplay(
                                "Pathology consultation, comprehensive, records and specimen with report (procedure)")))));
    return serviceRequest;
  }

  @Override
  @Nullable public Bundle.BundleEntryComponent apply(PathoReportInputBase value) {
    var mapped = map(value);
    if (mapped == null) return null;

    final Identifier identifierFirstRep = mapped.getIdentifierFirstRep();
    return buildBundleComponent(mapped, identifierFirstRep);
  }

  @NotNull protected Bundle.BundleEntryComponent buildBundleComponent(
      ServiceRequest mapped, Identifier identifierFirstRep) {
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
