/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.MappingEntry;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import java.util.ArrayList;
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

    // add Status
    // FIXME: may depend on {@link PathoSpecimen#getProbemenge }
    result.setStatus(Specimen.SpecimenStatus.AVAILABLE);

    // mapCollection(result, input);
    return result;
  }

  private void setIdentifiers(Specimen result, PathoSpecimen input) {
    // fixme: should setAccessionIdentifier and main identifier have different values ? why?

    // main resoruce identifier
    result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierType.SPECIMEN,
            fhirProperties.getSystems().getSpecimenRequestId(),
            "TODO: specific per specimen"));

    // probe id
    result.setAccessionIdentifier(
        new Identifier()
            .setValue(input.getContainerID())
            .setSystem(fhirProperties.getSystems().getSpecimenRequestId()));
  }

  /**
   * Create {@link Specimen.SpecimenCollectionComponent} resource and assign it to specimen
   *
   * @param specimen specimen in container
   */
  /*
    protected void mapCollection(Specimen specimen, PathoSpecimen input) {

      // TODO: collection & collection method
      var specimenCollectionCode =
          new Coding().setSystem("UKMR").setCode("ABCD").setVersion("1").setDisplay("Lunge");
      var specimenCollectionMethod =
          new Coding().setSystem("UKMR").setCode("ABCD").setVersion("1").setDisplay("Lunge");
      specimen.setCollection(
          // TODO method
          new Specimen.SpecimenCollectionComponent()
              .setMethod(new CodeableConcept())
              .setCollector(new Reference().setReference("Practitioner/2346545"))
              .setBodySite(new CodeableConcept().addCoding(specimenCollectionCode))
              .setMethod(new CodeableConcept(specimenCollectionMethod)));
      throw new NotImplementedException("collection & collection method");
    }
  */

  /**
   * Create {@link Specimen.SpecimenContainerComponent} resource and assign it to specimen
   *
   * @param specimen specimen in container
   */
  protected void mapContainer(Specimen specimen, PathoSpecimen input) {

    List<Specimen.SpecimenContainerComponent> container = new ArrayList<>();

    container.add(
        new Specimen.SpecimenContainerComponent()
            .addIdentifier(
                new Identifier()
                    .setSystem(fhirProperties.getSystems().getSpecimenContainer())
                    .setValue(input.getContainerGUID()))
            .setSpecimenQuantity(new Quantity().setValue(input.getProbemenge())));

    specimen.setContainer(container);
  }

  /**
   * Map input material name {@link PathoSpecimen#getProbeName() to SNOMED coded version}
   *
   * @param specimen fhir resource to be modified
   */
  protected void mapSpecimenType(Specimen specimen, PathoSpecimen input) {
    final CodeableConcept specimentTypeCoding = new CodeableConcept();

    addGeneralizedTypeCoding(input, specimentTypeCoding);

    var type = csvMappings.specimenTypes().get(input.getProbeName());

    specimentTypeCoding.addCoding(type.asFhirCoding());
    specimen.setType(specimentTypeCoding);
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
            .setProfile(List.of(new CanonicalType(ToFhirMapperSpecimen.MII_PR_Patho_Specimen)))
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
