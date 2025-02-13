/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.MappingEntry;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierResourceType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SpecimenMapper extends ToFhirMapperSpecimen {

  private final Logger log = LoggerFactory.getLogger(SpecimenMapper.class);

  private final CsvMappings csvMappings;

  @Autowired
  public SpecimenMapper(FhirProperties fhirProperties, CsvMappings csvMappings) {
    super(fhirProperties);

    this.csvMappings = csvMappings;
  }

  @Override
  @Nullable public Specimen map(PathoInputBase inputBase) {
    if (inputBase == null || !inputBase.isBaseValid()) return null;
    if (!(inputBase instanceof PathoSpecimen input))
      throw new IllegalArgumentException("input must be a PathoSpecimen");
    if (csvMappings.specimenTypes() == null || csvMappings.specimenTypes().isEmpty())
      throw new RuntimeException("specimentTypes mapping is missing");
    Specimen result = new Specimen();

    setMeta(result);
    setIdentifiers(result, input);

    // ProbenID name aus Nexus z.b. H202500123S1 - muss pseudonymisiert werden!
    result.setAccessionIdentifier(
        new Identifier()
            .setValue(input.getProbeID())
            .setSystem(fhirProperties.getSystems().getAssignerId()));

    /*
     * fixme: result.addParent() Referenz auf die Parent Probe
     *
     * Aufbau:
     * Root Specimen: Behälter Ebene
     * Blattebene - Referenz auf Root - hier werden die Färbungen abgebildet
     */

    result.setSubject(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Patient", input.getPatientennummer(), fhirProperties.getSystems().getPatientId()));
    result.addRequest(
        IdentifierAndReferenceUtil.getReferenceTo(
            "ServiceRequest",
            input.getAuftragsnummer(),
            fhirProperties.getSystems().getServiceRequestId()));

    mapSpecimenType(result, input);
    // fixme: never null since profile mandatory value > error topic?
    final boolean isInvalidResource = result.getType().getCoding().getFirst().getCode() != null;
    log.error(
        "resource cannot be processed since we hav no type mapping for current input: '%s'", input);
    assert isInvalidResource;

    mapContainer(result, input);

    // set Status
    result.setStatus(Specimen.SpecimenStatus.AVAILABLE);

    mapSpecimenCollection(result, input);

    return result;
  }

  @NotNull(
      "specimen FHIR profile has mandatory collection information, therefore result is never null")
  private Specimen mapSpecimenCollection(Specimen target, PathoSpecimen input) {
    final Specimen.SpecimenCollectionComponent collection =
        new Specimen.SpecimenCollectionComponent();

    /*
    minimum mandatory collection date
    */
    collection.setCollected(
        new DateTimeType(Date.from(Instant.ofEpochMilli(input.getProbeEntnahmedatum()))));
    /*
     * mandatory from patho profile
     */
    collection.setMethod(new CodeableConcept().addCoding(getExtractionMethodCoding(input)));

    /*
    optional body site
     */
    var bodySite = mapBodySite(input);
    collection.setBodySite(bodySite);

    target.setCollection(collection);
    return target;
  }

  private Coding getExtractionMethodCoding(PathoSpecimen input) {

    /*
    großer schnitt
    kleiner schnitt
    Abstrich
    biopsie
     */
    // TODO
    return null;
  }

  private CodeableConcept mapBodySite(PathoSpecimen input) {
    // todo: map organ/tissue to SNOMED CT code
    return null;
  }

  private void setIdentifiers(Specimen result, PathoSpecimen input) {
    // fixme: should setAccessionIdentifier and main identifier have different values ? why?

    // main resource identifier
    // FIXME: get container GUID
    result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.SPECIMEN,
            fhirProperties.getSystems().getSpecimenRequestId(),
            "TODO: specific per specimen"));

    // probe id
    // fixme kläre was hier rein soll
    result.setAccessionIdentifier(
        new Identifier()
            .setValue(input.getProbeID())
            .setSystem(fhirProperties.getSystems().getSpecimenRequestId()));
  }

  /**
   * Create {@link Specimen.SpecimenContainerComponent} resource and assign it to specimen
   *
   * @param specimen specimen in container
   */
  protected void mapContainer(Specimen specimen, PathoSpecimen input) {

    List<Specimen.SpecimenContainerComponent> container = new ArrayList<>();

    final Specimen.SpecimenContainerComponent specimenContainerComponent =
        new Specimen.SpecimenContainerComponent()
            .addIdentifier(
                new Identifier()
                    .setSystem(fhirProperties.getSystems().getSpecimenContainer())
                    .setValue(input.getProbeID()));

    specimenContainerComponent.setType(mapContainerTyp(input));
    container.add(specimenContainerComponent);

    specimen.setContainer(container);
  }

  private CodeableConcept mapContainerTyp(PathoSpecimen input) {

    final CodeableConcept codeableConcept = new CodeableConcept();
    addContainerType(input, codeableConcept, 3);

    return codeableConcept;
  }

  /**
   * @param specimen fhir resource to be modified
   */
  protected void mapSpecimenType(Specimen specimen, PathoSpecimen input) {
    final CodeableConcept specimenTypeCoding = new CodeableConcept();

    var type = csvMappings.specimenTypes().get(input.getProbeGewinnungsmethode());
    specimenTypeCoding.addCoding(type.asFhirCoding());

    specimen.setType(specimenTypeCoding);
  }

  /**
   * nexus container type = 1 has only 2 specimen types cytology vial and formalin-fixed
   * paraffin-embedded block container type = 3 is parent specimen container type = 2 are microscope
   * slides (433466003,Microscope slide (physical object))
   *
   * @param input
   * @param specimentTypeCoding
   */
  private static void addContainerType(
      PathoSpecimen input, CodeableConcept specimentTypeCoding, int nexusContainerTyp) {

    // alternativ kann man den namen der Probe prüfen '-0-X' ist Zytologie Proeb und '-1-X' sind
    // Gewebe Proben
    switch (nexusContainerTyp) {
      case 1:
        {
          if (StringUtils.endsWithIgnoreCase(input.getProbeGewinnungsmethode(), " ZY")) {
            specimentTypeCoding
                .addCoding()
                .setCode("1331905008")
                .setDisplay(
                    "Cytology specimen vial containing preservative fluid (physical object)")
                .setSystem(MappingEntry.SNOMED_SYSTEM)
                .setVersion(MappingEntry.SNOMED_VERSION);
          } else {
            specimentTypeCoding
                .addCoding()
                .setCode("441652008")
                .setDisplay("Formalin-fixed paraffin-embedded tissue specimen")
                .setSystem(MappingEntry.SNOMED_SYSTEM)
                .setVersion("http://snomed.info/sct/900000000000207008/version/20240501");
          }
          break;
        }
      case 2:
        {
          // todo: Objektträger gefärbt oder auch nicht
          break;
        }
      case 3:
        {
          throw new IllegalArgumentException(
              "unspecific parent container of nexus typ 3 should not be created here. It represents the specimen as a whole resource.");
        }
    }
    // FIXME: wir haben noch Objektträger mit und ohne Färbung, Probe hat dann in Nexus den
    // Containertyp = 2
  }

  private void setMeta(Specimen specimen) {
    specimen.setMeta(
        new Meta()
            .setProfile(
                List.of(
                    new CanonicalType(ToFhirMapperSpecimen.MII_PR_Patho_Specimen),
                    new CanonicalType(ToFhirMapperSpecimen.MII_Biobank_Specimen)))
            .setSource(ToFhirMapperSpecimen.META_SOURCE));
  }

  @Override
  @Nullable public Bundle.BundleEntryComponent apply(PathoInputBase value) {
    var mapped = map(value);
    if (mapped == null) return null;

    final Identifier identifierFirstRep = mapped.getIdentifierFirstRep();
    return buildBundleComponent(mapped, identifierFirstRep);
  }

  @NotNull protected Bundle.BundleEntryComponent buildBundleComponent(
      Specimen mapped, Identifier identifierFirstRep) {
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
