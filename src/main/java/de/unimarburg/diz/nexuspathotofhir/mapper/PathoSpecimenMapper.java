/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PathoSpecimenMapper extends ToFhirMapper {

  private final Logger log = LoggerFactory.getLogger(PathoSpecimenMapper.class);

  public PathoSpecimenMapper(FhirProperties fhirProperties) {
    super(fhirProperties);
  }

  @Override
  public Specimen map(PathoInputBase input) {

    // fixme this method should consume from specimen topic and therefore have other input type
    var result = new Specimen();
    return result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierType.SPECIMEN,
            fhirProperties.getSystems().getSpecimenRequestId(),
            "TODO: specific per specimen"));
  }

  public Specimen createSpecimen(PathoReport pathoReport, PathoSpecimen pathoSpecimen) {
    var specimen = new Specimen();
    // Set Sepecimendate
    var probeEinnahmeDatum =
        Date.from(
            pathoSpecimen
                .getSpecimenCollectionDate()
                .atZone(ZoneId.of("Europe/Berlin"))
                .toInstant());
    specimen.getMeta().setLastUpdated(probeEinnahmeDatum);
    // specimen type
    var specimenCoding = new Coding().setSystem("UKMR").setCode("ABCD").setVersion("1");
    specimen.setType(new CodeableConcept().addCoding(specimenCoding));
    // add Profile
    specimen.setMeta(
        new Meta()
            .setProfile(
                List.of(
                    new CanonicalType(
                        "https://www.medizininformatik-initiative.de/fhir/ext/modul-patho/StructureDefinition/mii-pr-patho-specimen")))
            .setSource("#nexus"));
    // add Status
    specimen.setStatus(Specimen.SpecimenStatus.AVAILABLE);
    // probe id
    specimen.setAccessionIdentifier(
        new Identifier()
            .setValue(pathoSpecimen.getProbeId())
            .setSystem(fhirProperties.getSystems().getSpecimenRequestId()));
    // patient
    specimen.setSubject(new Reference().setReference(pathoSpecimen.getPatientennummer()));
    // service request reference (TODO)
    var requestIdentifier =
        pathoReport.getAuftragnummer()
            + pathoReport.getPathologieBefundId()
            + pathoReport.getPatientennummer()
            + pathoReport.getFallnummer();
    List<Reference> references = new ArrayList<>();
    // fixme
    // references.add(new Reference().setReference("ServiceRequest/" +
    // IdentifierHasher.hasher.apply(requestIdentifier)));
    specimen.setRequest(references);
    // collection (TODO)
    var specimenCollectionCode =
        new Coding().setSystem("UKMR").setCode("ABCD").setVersion("1").setDisplay("Lunge");
    var specimenCollectionMethod =
        new Coding().setSystem("UKMR").setCode("ABCD").setVersion("1").setDisplay("Lunge");
    specimen.setCollection(
        new Specimen.SpecimenCollectionComponent()
            .setCollector(new Reference().setReference("Practitioner/2346545"))
            .setBodySite(new CodeableConcept().addCoding(specimenCollectionCode))
            .setMethod(new CodeableConcept(specimenCollectionMethod)));

    // container(TODO) // Es ist noch zu entscheiden, welche Id wir hier nehemen soll
    List<Specimen.SpecimenContainerComponent> container = new ArrayList<>();
    container.add(
        new Specimen.SpecimenContainerComponent()
            .setSpecimenQuantity(new Quantity().setValue(pathoSpecimen.getProbemenge())));
    specimen.setContainer(container);
    return specimen;
  }

  @Override
  public Bundle.BundleEntryComponent apply(PathoInputBase value) {
    var mapped = map(value);
    return new Bundle.BundleEntryComponent()
        .setResource(mapped)
        .setRequest(buildPutRequest(mapped, mapped.getIdentifierFirstRep().getSystem()));
  }
}
