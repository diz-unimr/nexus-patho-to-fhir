/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticFindingMapper extends ToFhirMapper {
  private final Logger log = LoggerFactory.getLogger(SpecimenMapper.class);

  public DiagnosticFindingMapper(FhirProperties fhirProperties) {
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
            PathologyIdentifierType.FINDING,
            fhirProperties.getSystems().getDiagnosticFindingId(),
            "TODO: specific per finding"));
  }

  public void setPathoFindingCategory(PathoReport pathoReport, Observation pathoFinding) {
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
    pathoBefundSection.put("micro", pathoReport.getMikroskopischer_Befund());
    pathoBefundSection.put("macro", pathoReport.getMakroskopischer_Befund());
    pathoBefundSection.put("diagnose", pathoReport.getDiagnose());

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

  public void setConditionalPathoFindingStatus(Observation pathoFinding, String befundArt) {
    /*
     * Find and set the status of the report.
     *
     * @param diagnosticReport The diagnostic report object.
     * @param befundArt String The befund type as String.
     * @return find and set the correct status of the report.
     */
    ArrayList<String> befundarte = new ArrayList<>();
    befundarte.add("Hauptbefund");
    befundarte.add("Nachbericht");
    befundarte.add("Zusatzbericht");
    befundarte.add("Korrekturbericht");

    for (int i = 0; i < befundarte.size(); i++) {
      String befundart = befundarte.get(i);
      Pattern pattern = Pattern.compile(befundart);
      Matcher matcher = pattern.matcher(befundArt);
      if (matcher.matches()) {
        switch (befundart) {
          case "Hauptbefund":
            pathoFinding.setStatus(Observation.ObservationStatus.FINAL);
          case "Nachbericht":
            // Need to check
            pathoFinding.setStatus(Observation.ObservationStatus.AMENDED);
          case "Zusatzbericht":
            pathoFinding.setStatus(Observation.ObservationStatus.valueOf("zusatz"));
          case "Korrekturbericht":
            pathoFinding.setStatus(Observation.ObservationStatus.CORRECTED);
        }
      }
    }
  }

  // TODO Add the information from AHD extraction section

  public Observation createPathFinding(
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
        pathoReport.getAuftragnummer()
            + pathoReport.getPatientennummer()
            + pathoReport.getFallnummer();

    // fixme
    // pathoFinding.setId(IdentifierHasher.hasher.apply(pathoFindingIdentifier));

    // Set Sepecimendate
    var probeEinnahmeDatum =
        Date.from(
            pathoSpecimen
                .getSpecimenCollectionDate()
                .atZone(ZoneId.of("Europe/Berlin"))
                .toInstant());
    pathoFinding.setEffective(new DateTimeType().setValue(probeEinnahmeDatum));

    // status
    setConditionalPathoFindingStatus(pathoFinding, pathoReport.getDocumentart());

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
