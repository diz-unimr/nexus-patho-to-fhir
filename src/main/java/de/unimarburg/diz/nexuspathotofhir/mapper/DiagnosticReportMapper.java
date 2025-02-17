/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierResourceType;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticReportMapper extends ToFhirMapper {

  public DiagnosticReportMapper(FhirProperties fhirProperties, CsvMappings csvMappings) {
    super(fhirProperties, csvMappings);
  }

  @Override
  public DiagnosticReport map(PathoInputBase inputBase) {
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

    // map status
    getConditionalReportStatus(diagnosticReport, input.getBefundtyp());

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
    // Create Reference ID MicroBefundGrouper
    Identifier idPathoFindingGrouperMicro =
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.MICROSCOPIC_GROUPER,
            fhirProperties.getSystems().getDiagnosticFindingGrouperId(),
            "",
            input.getBefundtyp(),
            input.getBefundID());

    // Create Reference ID MacroBefundGrouper
    Identifier idPathoFindingGrouperMacro =
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.MACROSCOPIC_GROUPER,
            fhirProperties.getSystems().getDiagnosticFindingGrouperId(),
            "",
            input.getBefundtyp(),
            input.getBefundID());

    // Create Reference ID DiagnoseConclusionGrouper

    Identifier idPathoFindingGrouperDiagConclusion =
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.DIAGNOSTIC_CONCLUSION_GROUPER,
            fhirProperties.getSystems().getDiagnosticFindingGrouperId(),
            "",
            input.getBefundtyp(),
            input.getBefundID());

    // Add each references to result array
    resultRefereces.add(
        IdentifierAndReferenceUtil.getReferenceTo("Observation", idPathoFindingGrouperMicro));
    resultRefereces.add(
        IdentifierAndReferenceUtil.getReferenceTo("Observation", idPathoFindingGrouperMacro));
    resultRefereces.add(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Observation", idPathoFindingGrouperDiagConclusion));
    diagnosticReport.setResult(resultRefereces);

    // map conclusion code
    // Need to be mapped
    // vlt. die Krebs Diagnosis
    diagnosticReport.addConclusionCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setCode("1234")
                    .setDisplay("Snomed diagnose")
                    .setSystem("http://snomed.info/sct")));
    // map effectiveDateTime
    Date probeEinnahmeDatum = new Date(input.getProbeEntnahmedatum());
    diagnosticReport.setEffective(new DateTimeType().setValue(probeEinnahmeDatum));

    // Performer
    ArrayList<Reference> performer = new ArrayList<>();
    var organizationRef =
        IdentifierAndReferenceUtil.getReferenceTo(
            "Organization", PERFORMER, fhirProperties.getSystems().getPerformerId());
    performer.add(organizationRef);
    diagnosticReport.setPerformer(performer);

    return diagnosticReport;
  }

  public static void getConditionalReportStatus(
      DiagnosticReport diagnosticReport, String befundArt) {
    if (befundArt.contains("Hauptbefund")) {
      diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
    } else if (befundArt.contains("Nachbericht")) {
      diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.AMENDED);
    } else if (befundArt.contains("Zusatzbericht")) {
      diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.APPENDED);
    } else if (befundArt.contains("Korrekturbericht")) {
      diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.CORRECTED);
    }
  }

  @Override
  @Nullable public Bundle.BundleEntryComponent apply(PathoInputBase value) {
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
