/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import de.unimarburg.diz.nexuspathotofhir.model.ReportDocType;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PathoFindingMapper extends ToFhirMapper {
  private final Logger log = LoggerFactory.getLogger(PathoFindingMapper.class);

  public PathoFindingMapper(FhirProperties fhirProperties) {
    super(fhirProperties);
  }

  @Override
  public Observation map(PathoInputBase inputBase) {
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");

    var result = new Observation();

    return result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierType.PATHO_FINDING,
            fhirProperties.getSystems().getDiagnosticFindingId(),
            "TODO: specific per finding"));
  }

  public Collection<Resource> map(PathoInputBase input, int grouperType) {
    var result = new ArrayList<Resource>();

    return result;
  }

  protected void setPathoFindingCategory(PathoReport pathoReport, Observation pathoFinding) {
    /**
     * Find and set the category of pathoFinding of the report.
     *
     * @param pathoRepor The diagnostic report object.
     * @param pathoFinding Observation object.
     * @return find and set the correct status of the report.
     */
    // handle different type of
    // Microscopic Grouper: 22635-7
    // Macroscopic Grouper: 22634-0
    // Diagnostic Conclusion Grouper: 22637-3
    // Intraoperative Grouper: 83321-0
    // Additional Specified Grouper: 100969-5 (In LOINC Org. nicht gefunden)

    HashMap<String, String> pathoBefundSection = new HashMap<String, String>();
    pathoBefundSection.put("micro", pathoReport.getMikroskopischerBefund());
    pathoBefundSection.put("macro", pathoReport.getMakroskopischerBefund());
    pathoBefundSection.put("diagnoseConclusion", pathoReport.getDiagnoseConclusion());

    for (String i : pathoBefundSection.keySet()) {
      String befundSection = pathoBefundSection.get(i);
      String actualKey = i;
      log.debug("Checking for Key: " + actualKey);
      if (befundSection == null) {
        log.debug("Section has null value");
      } else if (befundSection.isEmpty()) {
        log.debug("Section has empty values");
      } else {

        if (Objects.equals(actualKey, "micro")) {
          // Add the category micro
          pathoFinding.setCategory(
              List.of(
                  new CodeableConcept()
                      .setCoding(
                          List.of(
                              new Coding()
                                  .setCode("laboratory")
                                  .setSystem(
                                      "http://terminology.hl7.org/CodeSystem/observation-category"))),
                  new CodeableConcept()
                      .setCoding(
                          List.of(new Coding().setCode("22635-7").setSystem("http://loinc.org")))));
        } else if (Objects.equals(actualKey, "macro")) {
          // Add the category macro
          pathoFinding.setCategory(
              List.of(
                  new CodeableConcept()
                      .setCoding(
                          List.of(
                              new Coding()
                                  .setCode("laboratory")
                                  .setSystem(
                                      "http://terminology.hl7.org/CodeSystem/observation-category"))),
                  new CodeableConcept()
                      .setCoding(
                          List.of(new Coding().setCode("22634-0").setSystem("http://loinc.org")))));
        } else if (Objects.equals(actualKey, "diagnose")) {
          // Add the category diagnose
          pathoFinding.setCategory(
              List.of(
                  new CodeableConcept()
                      .setCoding(
                          List.of(
                              new Coding()
                                  .setCode("laboratory")
                                  .setSystem(
                                      "http://terminology.hl7.org/CodeSystem/observation-category"))),
                  new CodeableConcept()
                      .setCoding(
                          List.of(new Coding().setCode("22637-3").setSystem("http://loinc.org")))));
        }
      }
    }
  }

  protected void setFindingStatus(Observation pathoFinding, ReportDocType befundArt) {

    switch (befundArt) {
      case MAIN_REPORT:
        {
          pathoFinding.setStatus(Observation.ObservationStatus.FINAL);
          break;
        }
      case CORRECTION1:
      case CORRECTION2:
      case CORRECTION3:
      case CORRECTION4:
      case CORRECTION5:
      case CORRECTION6:
        {
          pathoFinding.setStatus(Observation.ObservationStatus.CORRECTED);
          break;
        }

      case ADDITION1:
      case ADDITION2:
      case ADDITION3:
        {
          pathoFinding.setStatus(Observation.ObservationStatus.AMENDED);
          break;
        }
      default:
        // fixme Zusatzbericht?
        pathoFinding.setStatus(Observation.ObservationStatus.UNKNOWN);
    }
  }

  // TODO Add the information from AHD extraction section
  // Use  the valueString for the firstVersion

  protected Observation createPathoFinding(
      PathoReport pathoReport,
      PathoSpecimen pathoSpecimen,
      String pathoGrouperType,
      Specimen specimen) {

    var pathoFinding = new Observation();

    // set Meta
    pathoFinding.setMeta(
        new Meta()
            .setProfile(
                List.of(
                    new CanonicalType(
                        "https://www.medizininformatik-initiative.de/fhir/ext/modul-patho/StructureDefinition/mii-pr-patho-finding"))));
    // setCategory
    setPathoFindingCategory(pathoReport, pathoFinding);

    // Set Identifier (TODO)
    var pathoFindingIdentifier =
        pathoReport.getAuftragsnummer()
            + pathoReport.getPatientennummer()
            + pathoReport.getFallnummer();

    // fixme
    // pathoFinding.setId(IdentifierHasher.hasher.apply(pathoFindingIdentifier));
    Date probeEinnahmeDatum = new Date(pathoSpecimen.getProbeEntnahmedatum());
    // Set Sepecimendate

    pathoFinding.setEffective(new DateTimeType().setValue(probeEinnahmeDatum));

    // status
    setFindingStatus(pathoFinding, pathoReport.getDocType());

    // code coding (TODO), Codes from the ADH Tool (LOINC CODE)
    pathoFinding.setCode(
        new CodeableConcept()
            .setCoding(
                List.of(
                    new Coding()
                        .setCode("22635-7")
                        .setSystem("http://loinc.org")
                        .setDisplay("Test String"))));

    // valueCodeableConcept (TODO) SNOMED CODE for the diagnose from AHD and Mapping
    pathoFinding
        .getValueCodeableConcept()
        .setCoding(
            List.of(
                new Coding()
                    .setCode("716917000")
                    .setSystem("http://snomed.info/sct")
                    .setDisplay(
                        "Structure of lateral middle regional part of peripheral zone of right half prostate (body structure)")));

    // specimen
    pathoFinding.setSpecimen(new Reference().setReference("Specimen/" + specimen.getId()));

    return null;
  }

  @Override
  public Bundle.BundleEntryComponent apply(PathoInputBase value) {
    var mapped = map(value);
    return new Bundle.BundleEntryComponent()
        .setResource(mapped)
        .setRequest(buildPutRequest(mapped, mapped.getIdentifierFirstRep().getSystem()));
  }
}
