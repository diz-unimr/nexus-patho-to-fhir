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
import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DiagnosticReportMapper extends ToFhirMapper {

  public static final Logger logger = LoggerFactory.getLogger(DiagnosticReportMapper.class);

  public DiagnosticReportMapper(FhirProperties fhirProperties, CsvMappings csvMappings) {
    super(fhirProperties, csvMappings);
  }

  public DiagnosticReport map(PathoReportInputBase inputBase) {
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");

    var diagnosticReport = new DiagnosticReport();

    // map meta
    diagnosticReport.setMeta(
        new Meta()
            .setProfile(
                List.of(
                    new CanonicalType(
                        "https://www.medizininformatik-initiative.de/fhir/ext/modul-patho/StructureDefinition/mii-pr-patho-report")))
            .setSource(META_SOURCE));

    // identifier
    diagnosticReport.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifierWithType(
            input,
            PathologyIdentifierType.ACSN,
            PathologyIdentifierResourceType.DIAGNOSTIC_REPORT,
            fhirProperties.getSystems().getDiagnosticReportId()));

    // map based on
    diagnosticReport.addBasedOn(
        IdentifierAndReferenceUtil.getReferenceTo(
            "ServiceRequest",
            IdentifierAndReferenceUtil.getIdentifier(
                input,
                PathologyIdentifierResourceType.SERVICE_REQUEST,
                fhirProperties.getSystems().getServiceRequestId())));

    // map code
    diagnosticReport.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setCode("60568-3")
                    .setSystem("http://loinc.org")
                    .setDisplay("Pathology Synoptic report")));
    // TODO map performer

    // Encounter
    diagnosticReport.setEncounter(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Encounter", input.getFallnummer(), fhirProperties.getSystems().getEncounterId()));

    // Patient
    diagnosticReport.setSubject(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Patient", input.getPatientennummer(), fhirProperties.getSystems().getPatientId()));

    // map result (TODO)

    ArrayList<Reference> resultRefereces = new ArrayList<>();
    if (StringUtils.hasText(inputBase.getMikroskopischerBefund())) {

      // Create Reference ID MicroBefundGrouper
      Identifier idPathoFindingGrouperMicro =
          IdentifierAndReferenceUtil.getIdentifier(
              input,
              PathologyIdentifierResourceType.MICROSCOPIC_GROUPER,
              fhirProperties.getSystems().getPathoFindingGrouperMicroId());
      resultRefereces.add(
          IdentifierAndReferenceUtil.getReferenceTo("Observation", idPathoFindingGrouperMicro));
    } else {
      logger.info("Micro text is empty");
    }

    // Create Reference ID MacroBefundGrouper
    if (StringUtils.hasText(inputBase.getMakroskopischerBefund())) {
      Identifier idPathoFindingGrouperMacro =
          IdentifierAndReferenceUtil.getIdentifier(
              input,
              PathologyIdentifierResourceType.MACROSCOPIC_GROUPER,
              fhirProperties.getSystems().getPathoFindingGrouperMacroId());
      resultRefereces.add(
          IdentifierAndReferenceUtil.getReferenceTo("Observation", idPathoFindingGrouperMacro));

    } else {
      logger.info("Macro text is empty");
    }

    if (StringUtils.hasText(inputBase.getDiagnoseConclusion())) {
      // Create Reference ID DiagnoseConclusionGrouper
      Identifier idPathoFindingGrouperDiagConclusion =
          IdentifierAndReferenceUtil.getIdentifier(
              input,
              PathologyIdentifierResourceType.DIAGNOSTIC_CONCLUSION_GROUPER,
              fhirProperties.getSystems().getPathoFindingGrouperDigConcId());
      resultRefereces.add(
          IdentifierAndReferenceUtil.getReferenceTo(
              "Observation", idPathoFindingGrouperDiagConclusion));
    } else {
      logger.info("DiagnosticConclusion report text is empty");
    }
    // Add each references to result array
    diagnosticReport.setResult(resultRefereces);

    // map conclusion code
    // Need to be mapped
    // vlt. die Krebs Diagnosis
    /*    diagnosticReport.addConclusionCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setCode("1234")
                    .setDisplay("Snomed diagnose")
                    .setSystem("http://snomed.info/sct")));
    // map effectiveDateTime*/
    Date probeEinnahmeDatum = new Date(input.getProbeEntnahmedatum());
    diagnosticReport.setEffective(new DateTimeType().setValue(probeEinnahmeDatum));

    // Performer
    ArrayList<Reference> performer = new ArrayList<>();
    var organizationRef =
        IdentifierAndReferenceUtil.getReferenceTo(
            "Organization", PERFORMER, fhirProperties.getSystems().getPerformerId());
    performer.add(organizationRef);
    diagnosticReport.setPerformer(performer);
    if (input.getBefundtyp().contains("Hauptbefund")) {
      diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
    }
    return diagnosticReport;
  }

  @Nullable public Bundle.BundleEntryComponent apply(PathoReportInputBase value) {
    var mapped = map(value);
    if (mapped == null) return null;

    final Identifier identifierFirstRep = mapped.getIdentifierFirstRep();
    return buildBundleComponent(mapped, identifierFirstRep);
  }

  @NotNull protected Bundle.BundleEntryComponent buildBundleComponent(
      DiagnosticReport mapped, Identifier identifierFirstRep) {
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
