/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.MappingEntry;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
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

    result.setSubject(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Patient", input.getPatientennummer(), fhirProperties.getSystems().getPatientId()));
    result.addRequest(
        IdentifierAndReferenceUtil.getReferenceTo(
            "ServiceRequest",
            input.getAuftragsnummer(),
            fhirProperties.getSystems().getServiceRequestId()));

    mapSpecimenType(result, input);
    mapContainer(result, input);

    // set Status
    if (0 > input.getProbemenge()) result.setStatus(Specimen.SpecimenStatus.UNAVAILABLE);
    else result.setStatus(Specimen.SpecimenStatus.AVAILABLE);

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
    result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierType.SPECIMEN,
            fhirProperties.getSystems().getSpecimenRequestId(),
            "TODO: specific per specimen"));

    // probe id
    // fixme kläre was hier rein soll
    result.setAccessionIdentifier(
        new Identifier()
            .setValue(input.getContainerID())
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
                    .setValue(input.getContainerGUID()))
            .setSpecimenQuantity(new Quantity().setValue(input.getProbemenge()));

    specimenContainerComponent.setType(mapContainerTyp(input));
    container.add(specimenContainerComponent);

    specimen.setContainer(container);
  }

  private CodeableConcept mapContainerTyp(PathoSpecimen input) {
    // fixme
    var dummytype = csvMappings.specimenContainerType().get("0");

    final CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept
        .addCoding()
        .setCode(dummytype.getSnomedCode())
        .setSystem(ToFhirMapperSpecimen.SNOMED_SYSTEM);
    return codeableConcept;
  }

  /**
   * Map input material name {@link PathoSpecimen#getProbeName() to SNOMED coded version}
   *
   * @param specimen fhir resource to be modified
   */
  protected void mapSpecimenType(Specimen specimen, PathoSpecimen input) {
    final CodeableConcept specimenTypeCoding = new CodeableConcept();

    addGeneralizedTypeCoding(input, specimenTypeCoding);

    var type = csvMappings.specimenTypes().get(input.getProbeName());

    specimenTypeCoding.addCoding(type.asFhirCoding());
    specimen.setType(specimenTypeCoding);
  }

  private static void addGeneralizedTypeCoding(
      PathoSpecimen input, CodeableConcept specimentTypeCoding) {
    if (StringUtils.endsWithIgnoreCase(input.getProbeName(), " ZY")) {
      specimentTypeCoding
          .addCoding()
          .setCode("258433009")
          .setDisplay("Smear specimen (specimen)")
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
  }

  private void setMeta(Specimen specimen) {
    specimen.setMeta(
        new Meta()
            .setProfile(
                List.of(
                    new CanonicalType(ToFhirMapperSpecimen.MII_Biobank_Specimen),
                    new CanonicalType(ToFhirMapperSpecimen.MII_PR_Patho_Specimen)))
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
