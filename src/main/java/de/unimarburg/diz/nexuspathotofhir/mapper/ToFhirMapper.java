/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.DecideStatusOfBefund;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public abstract class ToFhirMapper {

  private final Logger log = LoggerFactory.getLogger(ToFhirMapper.class);

  protected static final String META_SOURCE = "#nexus-pathology";

  protected static final String PERFORMER = "UMR_Pathology";

  protected final FhirProperties fhirProperties;

  @Autowired private CsvMappings csvMappings;

  public ToFhirMapper(final FhirProperties fhirProperties, CsvMappings csvMappings) {
    this.fhirProperties = fhirProperties;
    this.csvMappings = csvMappings;
  }

  // PathoFinding Grouper
  public Observation mapBaseGrouper(
      PathoReport input, ArrayList<String> refPathoFiningIds, String idSystems) {
    if (input == null) return null;
    final Observation observationGrouper = new Observation();
    // Encounter
    observationGrouper.setEncounter(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Encounter", input.getFallnummer(), fhirProperties.getSystems().getEncounterId()));
    // Patient
    observationGrouper.setSubject(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Patient", input.getPatientennummer(), fhirProperties.getSystems().getPatientId()));

    // ServiceRequestIdentifier
    ArrayList<Reference> basedOnRef = new ArrayList<>();
    basedOnRef.add(
        IdentifierAndReferenceUtil.getReferenceTo(
            "ServiceRequest",
            input.getAuftragsnummer(),
            fhirProperties.getSystems().getServiceRequestId()));
    observationGrouper.setBasedOn(basedOnRef);
    // EffectiveDate
    if (input.getProbeEntnahmedatum() != null) {
      Date probeEntnahmeDatum = new Date(input.getProbeEntnahmedatum());
      observationGrouper.setEffective(new DateTimeType().setValue(probeEntnahmeDatum));
    } else {
      throw new IllegalArgumentException("probeEntnahmedatum is null");
    }
    // Report Issued Date
    if (input.getBefundErstellungsdatum() != null) {
      Date probeEntnahmeDatum = new Date(input.getBefundErstellungsdatum());
      observationGrouper.setIssued(probeEntnahmeDatum);
    } else {
      throw new IllegalArgumentException("Befunderstellungsdatum is null");
    }
    // Status
    DecideStatusOfBefund.setFindingStatus(observationGrouper, input.getDocType());
    ArrayList<Reference> hasMembers = new ArrayList<>();
    for (String id : refPathoFiningIds) {
      Identifier identifier = new Identifier();
      identifier.setValue(id);
      identifier.setSystem(idSystems);
      hasMembers.add(IdentifierAndReferenceUtil.getReferenceTo("Observation", identifier));
    }
    observationGrouper.setHasMember(hasMembers);
    return observationGrouper;
  }

  // PathoFinding
  public Observation mapBasePathoFinding(
      PathoReport input, String id, String idSystem, String code, String value) {
    if (input == null) return null;
    if (csvMappings.specimenTypes() == null || csvMappings.specimenTypes().isEmpty())
      // throw new RuntimeException("specimentTypes mapping is missing");
      log.error("specimentTypes mapping is missing");
    final Observation observationFinding = new Observation();
    // Identifier
    Identifier identifier = new Identifier();
    identifier.setSystem(idSystem);
    identifier.setValue(id);
    observationFinding.addIdentifier(identifier);
    // Encounter
    observationFinding.setEncounter(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Encounter", input.getFallnummer(), fhirProperties.getSystems().getEncounterId()));
    // Subject/Patient
    observationFinding.setSubject(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Patient", input.getPatientennummer(), fhirProperties.getSystems().getPatientId()));
    // Metadata: Profile and Source
    observationFinding.setMeta(
        new Meta()
            .setProfile(
                List.of(
                    new CanonicalType(
                        "https://www.medizininformatik-initiative.de/fhir/ext/modul-patho/StructureDefinition/mii-pr-patho-finding")))
            .setSource(META_SOURCE));
    // EffectiveDate
    if (input.getProbeEntnahmedatum() != null) {
      Date probeEinnahmeDatum = new Date(input.getProbeEntnahmedatum());
      observationFinding.setEffective(new DateTimeType().setValue(probeEinnahmeDatum));
    } else {
      throw new IllegalArgumentException("probeEntnahmedatum is null");
    }

    if (input.getBefundErstellungsdatum() != null) {
      Date probeEntnahmeDatum = new Date(input.getBefundErstellungsdatum());
      observationFinding.setIssued(probeEntnahmeDatum);
    } else {
      throw new IllegalArgumentException("Befunderstellungsdatum is null");
    }
    // ServiceRequestIdentifier
    ArrayList<Reference> basedOnRef = new ArrayList<>();
    basedOnRef.add(
        IdentifierAndReferenceUtil.getReferenceTo(
            "ServiceRequest",
            input.getAuftragsnummer(),
            fhirProperties.getSystems().getServiceRequestId()));
    observationFinding.setBasedOn(basedOnRef);
    //
    // mapProbeNameToCode(observationFinding, input);
    // status
    DecideStatusOfBefund.setFindingStatus(observationFinding, input.getDocType());
    // CodeFinding
    ArrayList<Coding> coding = new ArrayList<>();
    coding.add(new Coding().setCode(code).setSystem("http://loinc.org"));
    observationFinding.setCode(new CodeableConcept().setCoding(coding));
    observationFinding.getValueStringType().setValueAsString(value);

    return observationFinding;
  }

  protected void mapProbeNameToCode(Observation observation, PathoReport input) {
    if (StringUtils.hasText(input.getProbeName())) {
      log.debug("ProbeName is present");
      // Split the String by ','
      if (input.getProbeName().contains(",")) {
        ArrayList<Coding> coding = new ArrayList<>();
        log.debug("Contains multiple Probe");
        String[] arrayProbeName =
            Arrays.stream(input.getProbeName().split(",")).map(String::trim).toArray(String[]::new);
        for (String probeName : arrayProbeName) {
          var code = csvMappings.specimenTypes().get(probeName);
          if (code != null) {
            coding.add(code.asFhirCoding());
          } else {
            log.warn("ProbeName cannot be mapped");
          }
        }
        observation.setCode(new CodeableConcept().setCoding(coding));
      } else {
        log.debug("Contains single ProbeID");
        var code = csvMappings.specimenTypes().get(input.getProbeName());
        if (code != null) {
          observation.setCode(new CodeableConcept().addCoding(code.asFhirCoding()));
        } else {
          log.warn("ProbeName cannot be mapped");
        }
      }
    } else {
      log.error("The Probename is not valid");
    }
  }

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

  protected Bundle.BundleEntryRequestComponent buildPatchRequest(
      Resource resource, String identifierSystem) {
    var request = new Bundle.BundleEntryRequestComponent();
    return request
        .setMethod(Bundle.HTTPVerb.PATCH)
        .setUrl(resource.getResourceType().name())
        .setIfMatch(
            String.format(
                "%s?identifier=%s|%s",
                resource.getResourceType().name(), identifierSystem, resource.getId()));
  }

  public void mapBaseGrouper(PathoReport pathoReport) {}
}
