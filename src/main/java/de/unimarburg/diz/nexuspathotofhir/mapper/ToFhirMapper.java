/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.DecideStatusOfBefund;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public abstract class ToFhirMapper
    implements ValueMapper<PathoInputBase, Bundle.BundleEntryComponent> {
  private final Logger log = LoggerFactory.getLogger(ToFhirMapper.class);

  protected static final String META_SOURCE = "#nexus-pathology";

  protected static final String PERFORMER = "UMR_Pathology";

  protected final FhirProperties fhirProperties;

  @Autowired private CsvMappings csvMappings;

  public ToFhirMapper(final FhirProperties fhirProperties, CsvMappings csvMappings) {
    this.fhirProperties = fhirProperties;
    this.csvMappings = csvMappings;
  }

  public abstract Resource map(PathoInputBase input);

  // PathoFinding Grouper
  public Observation mapBaseGrouper(PathoReport input) {
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
    Date probeEinnahmeDatum = new Date(input.getProbeEntnahmedatum());
    observationGrouper.setEffective(new DateTimeType().setValue(probeEinnahmeDatum));

    // Status
    DecideStatusOfBefund.setFindingStatus(observationGrouper, input.getDocType());
    // Specimen

    return observationGrouper;
  }

  // PathoFinding
  public Observation mapBasePathoFinding(PathoReport input) {
    if (input == null) return null;
    if (csvMappings.specimenTypes() == null || csvMappings.specimenTypes().isEmpty())
      // throw new RuntimeException("specimentTypes mapping is missing");
      log.error("specimentTypes mapping is missing");
    final Observation observationFinding = new Observation();
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
    Date probeEinnahmeDatum = new Date(input.getProbeEntnahmedatum());
    observationFinding.setEffective(new DateTimeType().setValue(probeEinnahmeDatum));

    // ServiceRequestIdentifier
    ArrayList<Reference> basedOnRef = new ArrayList<>();
    basedOnRef.add(
        IdentifierAndReferenceUtil.getReferenceTo(
            "ServiceRequest",
            input.getAuftragsnummer(),
            fhirProperties.getSystems().getServiceRequestId()));
    observationFinding.setBasedOn(basedOnRef);

    //
    mapProbeNameToCode(observationFinding, input);

    // status
    DecideStatusOfBefund.setFindingStatus(observationFinding, input.getDocType());

    return observationFinding;
  }

  protected void mapProbeNameToCode(Observation observation, PathoReport input) {
    if (StringUtils.hasText(input.getProbeName())) {
      log.debug("ProbeName is present");
      // Split the String by ','
      if (input.getProbeName().contains(",")) {
        ArrayList<Coding> coding = new ArrayList<>();
        log.debug("Contains multiple Probe");
        String[] arrayProbeName = input.getProbeName().split(",");
        for (String probeName : arrayProbeName) {
          var code = csvMappings.specimenTypes().get(probeName);
          log.debug("Code is {}", code);
          coding.add(code.asFhirCoding());
        }
        observation.setCode(new CodeableConcept().setCoding(coding));
      } else {
        log.debug("Contains single ProbeID");
        var code = csvMappings.specimenTypes().get(input.getProbeName());
        observation.setCode(new CodeableConcept().addCoding(code.asFhirCoding()));
      }
    } else {
      log.error("The Probename is not valid");
    }
  }

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
