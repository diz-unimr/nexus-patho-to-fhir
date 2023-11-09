/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappingReader;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.MappingEntry;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpecimenMapper extends ToFhirMapper {

  private final Logger log = LoggerFactory.getLogger(SpecimenMapper.class);
  private final Map<String, MappingEntry> specimentTypes;
  private Map<String, MappingEntry> specimenExtractionMethod;

  @Autowired
  public SpecimenMapper(FhirProperties fhirProperties, CsvMappingReader csvMappingReader) {
    super(fhirProperties);
    try {
      this.specimentTypes = csvMappingReader.specimenTypes();
      this.specimenExtractionMethod = csvMappingReader.specimenExtractionMethod();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Nullable public Specimen map(PathoInputBase inputBase) {
    if (inputBase == null || !inputBase.isBaseValid()) return null;
    if (!(inputBase instanceof PathoSpecimen input))
      throw new IllegalArgumentException("input must be a PathoSpecimen");
    if (specimentTypes == null || specimentTypes.isEmpty())
      throw new RuntimeException("specimentTypes mapping is missing");
    var result = new Specimen();

    setMeta(result);
    setIdentifiers(result, input);

    result.setSubject(
        IdentifierAndReferenceUtil.getReferenceTo(
            "Patient", input.getPatientennummer(), fhirProperties.getSystems().getPatientId()));
    result.addRequest(
        IdentifierAndReferenceUtil.getReferenceTo(
            "ServiceRequest",
            input.getAuftragnummer(),
            fhirProperties.getSystems().getServiceRequestId()));

    mapSpecimenType(result, input);
    mapContainer(result, input);

    // add Status
    // FIXME: may depend on {@link PathoSpecimen#getProbemenge }
    result.setStatus(Specimen.SpecimenStatus.AVAILABLE);

    mapCollection(result, input);
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
            .setValue(input.getProbeId())
            .setSystem(fhirProperties.getSystems().getSpecimenRequestId()));
  }

  protected void mapCollection(Specimen result, PathoSpecimen input) {

    // TODO: collection & collection method
    var specimenCollectionCode =
        new Coding().setSystem("UKMR").setCode("ABCD").setVersion("1").setDisplay("Lunge");
    var specimenCollectionMethod =
        new Coding().setSystem("UKMR").setCode("ABCD").setVersion("1").setDisplay("Lunge");
    result.setCollection(
        // TODO method
        new Specimen.SpecimenCollectionComponent()
            .setMethod(new CodeableConcept())
            .setCollector(new Reference().setReference("Practitioner/2346545"))
            .setBodySite(new CodeableConcept().addCoding(specimenCollectionCode))
            .setMethod(new CodeableConcept(specimenCollectionMethod)));
    throw new NotImplementedException("collection & collection method");
  }

  /**
   * @param result
   */
  protected void mapContainer(Specimen result, PathoSpecimen input) {

    // container(TODO) // Es ist noch zu entscheiden, welche Id wir hier nehmen soll
    List<Specimen.SpecimenContainerComponent> container = new ArrayList<>();
    container.add(
        new Specimen.SpecimenContainerComponent()
            .setType(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setSystem(SNOMED_SYSTEM)
                            .setCode("433472003")
                            .setDisplay("Microscope slide coverslip (physical object)")))
            .setSpecimenQuantity(new Quantity().setValue(input.getProbemenge())));

    result.setContainer(container);
    throw new NotImplementedException("mapContainer");
  }

  /**
   * Map input material name {@link PathoSpecimen#getProbename() to SNOMED coded version}
   *
   * @param specimen fhir resource to be modified
   * @return specimen
   */
  protected void mapSpecimenType(Specimen specimen, PathoSpecimen input) {
    var type = specimentTypes.get(input.getProbename());
    specimen.setType(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem(SNOMED_SYSTEM)
                    .setCode(type.getSnomedCode())
                    .setDisplay(type.getSnomedDisplayName())));
  }

  private void setMeta(Specimen specimen) {
    specimen.setMeta(
        new Meta()
            .setProfile(List.of(new CanonicalType(ToFhirMapper.MII_PR_Patho_Specimen)))
            .setSource(ToFhirMapper.META_SOURCE));
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
