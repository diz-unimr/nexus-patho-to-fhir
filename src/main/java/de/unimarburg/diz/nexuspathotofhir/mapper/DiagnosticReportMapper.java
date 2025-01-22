/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import static org.springframework.cloud.stream.binder.kafka.properties.KafkaConsumerProperties.StandardHeaders.timestamp;

@Service
public class DiagnosticReportMapper extends ToFhirMapper {

  public DiagnosticReportMapper(FhirProperties fhirProperties) {
    super(fhirProperties);
  }

  @Override
  public DiagnosticReport map(PathoInputBase inputBase) {
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");

    var result = new DiagnosticReport();

    return result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierType.DIAGNOSTIC_REPORT,
            fhirProperties.getSystems().getDiagnosticReportId()));
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

  public DiagnosticReport createDiagnosticReport(PathoReport rawInput) {
    var diagnosticReport = new DiagnosticReport();

    var identifierType =
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                    .setCode("ACSN")
                    .setDisplay("Accession ID"));

    // map meta
    diagnosticReport.setMeta(
        new Meta()
            .setProfile(
                List.of(
                    new CanonicalType(
                        "https://www.medizininformatik-initiative.de/fhir/ext/modul-patho/StructureDefinition/mii-pr-patho-report")))
            .setSource("#nexus-pathology"));

    // map identifier
    diagnosticReport.setIdentifier(
        List.of(
            new Identifier()
                .setType(identifierType)
                .setValue(rawInput.getBefundID())
                .setSystem(fhirProperties.getSystems().getDiagnosticReportId())));
    // map based on
    diagnosticReport.addBasedOn(
        IdentifierAndReferenceUtil.getReferenceTo(
            "ServiceRequest",
            IdentifierAndReferenceUtil.getIdentifier(
                rawInput,
                PathologyIdentifierType.SERVICE_REQUEST,
                fhirProperties.getSystems().getServiceRequestId())));

    // map status
    getConditionalReportStatus(diagnosticReport, rawInput.getBefundtyp());
    // map code
    diagnosticReport.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setCode("60568-3")
                    .setSystem("http://loinc.org")
                    .setDisplay("Pathology Synoptic report")));
    // TODO map performer

    // map encounter
    diagnosticReport.setEncounter(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Encounter", fhirProperties.getSystems().getEncounterId(), rawInput.getFallnummer()));

    // map result (TODO)
    diagnosticReport.addResult().setReference("Observation/ref-to-patho-macro-grouper-b");
    diagnosticReport.addResult().setReference("Observation/ref-to-mii-exa-patho-micro-grouper-a");
    diagnosticReport
        .addResult()
        .setReference("Observation/ref-to-patho-diagnostic-conclusion-grouper");
    // map conclusion
    diagnosticReport.setConclusion(rawInput.getDiagnoseConclusion());
    // map conclusion code
    diagnosticReport.addConclusionCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setCode("1234")
                    .setDisplay("Snomed diagnose")
                    .setSystem("http://snomed.info/sct")));
    // map effectiveDateTime


     // var probeEinnahmeDatum = LocalDateTime.ofInstant(Instant.ofEpochMilli(rawInput.getBefundErstellungsdatum()), ZoneId.systemDefault());
    // Converting the long value to date
      Date probeEinnahmeDatum = new Date(rawInput.getBefundErstellungsdatum());
    diagnosticReport.setEffective(
        new DateTimeType().setValue(probeEinnahmeDatum));
    return diagnosticReport;
  }

  @Override
  public Bundle.BundleEntryComponent apply(PathoInputBase value) {
    var mapped = map(value);
    return new Bundle.BundleEntryComponent()
        .setResource(mapped)
        .setRequest(buildPutRequest(mapped, mapped.getIdentifierFirstRep().getSystem()));
  }
}
