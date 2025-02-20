/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.MappingEntry;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import de.unimarburg.diz.nexuspathotofhir.model.SpecimenContainerTyp;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import java.time.Instant;
import java.util.*;
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

    if (!checkInputIsValid(input)) {
      // error logging in method before
      // fixme: may be add input to error topic
      return null;
    }

    Specimen result = new Specimen();

    setMeta(result);
    setIdentifiers(result, input);

    /*
     * fixme: result.addParent() Referenz auf die Parent Probe
     * TOdo: entscheiden ob man das überhaupt macht oder alle container in der container property sammelt
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
    final boolean isInvalidResource = result.hasType();
    if (isInvalidResource)
      log.error(
          "resource cannot be processed since we hav no type mapping for current input: '{}'",
          input);

    mapContainer(result, input);

    // set Status
    result.setStatus(Specimen.SpecimenStatus.AVAILABLE);

    mapSpecimenCollection(result, input);

    return result;
  }

  public boolean checkInputIsValid(PathoSpecimen input) {

    boolean isValid = true;
    var arrayLength =
        Arrays.asList(
            input.getContainerLabelsArray().length,
            input.getContainerGUIDsArray().length,
            input.getContainerTypesArray().length,
            // note getSubContainerIdsArray() removes 'NA' value which is 'root' therefore we need
            // to add +1
            input.getSubContainerIdsArray().length + 1);

    var countSet = new HashSet<Integer>();
    arrayLength.forEach(a -> countSet.add(a));
    if (countSet.size() > 1) {
      log.error("input has different count of lables,guids,types,ids: '{}'", input);
      isValid = false;
    }

    if (input.getRootIndex() < 0) {
      log.error("input has no root container lable: '{}'", input);
      isValid = false;
    }

    if (!StringUtils.hasText(input.getAuftragsnummer())) {
      log.error("input has encounter number: '{}'", input);
      isValid = false;
    }
    if (!StringUtils.hasText(input.getPatientennummer())) {
      log.error("input has patient number: '{}'", input);
      isValid = false;
    }

    if (input.getProbeEntnahmedatum() == null) {
      log.error("input has no extraction date: '{}'", input);
      isValid = false;
    }

    if (!StringUtils.hasText(input.getProbeName())) {
      log.error("input has specimen name: '{}'", input);

      isValid = false;
    }
    return isValid;
  }

  @NotNull(
      "specimen FHIR profile has mandatory collection information, therefore result is never null")
  private Specimen mapSpecimenCollection(Specimen target, PathoSpecimen input) {
    final Specimen.SpecimenCollectionComponent collection =
        new Specimen.SpecimenCollectionComponent();

    /*
    minimum mandatory collection date
    */
    if (input.getProbeEntnahmedatum() != null)
      collection.setCollected(
          new DateTimeType(Date.from(Instant.ofEpochMilli(input.getProbeEntnahmedatum()))));
    /*
     * mandatory from patho profile
     */
    final Coding extractionMethodCoding = getExtractionMethodCoding(input);
    if (extractionMethodCoding != null)
      collection.setMethod(new CodeableConcept().addCoding(extractionMethodCoding));

    /*
    optional body site
     */
    var bodySite = mapBodySite(input);
    collection.setBodySite(bodySite);

    target.setCollection(collection);
    return target;
  }

  private Coding getExtractionMethodCoding(PathoSpecimen input) {

    if (!csvMappings.specimenExtractionMethod().containsKey(input.getProbeGewinnungsmethode()))
      return null;
    else {
      var method = csvMappings.specimenExtractionMethod().get(input.getProbeGewinnungsmethode());
      return method.asFhirCoding();
    }
  }

  private CodeableConcept mapBodySite(PathoSpecimen input) {
    // todo: map organ/tissue to SNOMED CT code
    return null;
  }

  private void setIdentifiers(Specimen result, PathoSpecimen input) {

    // for easy specimen reference from pathology service request we use lab label her, too.
    result.addIdentifier(
        new Identifier()
            .setValue(input.getProbeID())
            .setSystem(fhirProperties.getSystems().getSpecimenId()));

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

    // hier brauchen wir alle Container
    var containerLabels = input.getContainerLabelsArray();
    var containerTypes = input.getContainerTypesArray();
    for (int i = 0; i < containerLabels.length; i++) {
      // skip root container
      if (i == input.getRootIndex()) continue;

      var subContainerLabel = containerLabels[i];
      var subContainerType = Integer.valueOf(containerTypes[i]);
      var mappedType = SpecimenContainerTyp.valueOf(subContainerType);
      if (mappedType == SpecimenContainerTyp.BLANK_CUT) {
        log.warn(
            "skipping subcontainer '{}' since typ is blank cut. Specimen ID '{}'",
            subContainerLabel,
            input.getProbeID());
        continue;
      }

      final Specimen.SpecimenContainerComponent specimenContainerComponent =
          new Specimen.SpecimenContainerComponent();
      specimenContainerComponent
          .addIdentifier()
          .setUse(Identifier.IdentifierUse.USUAL)
          .setValue(subContainerLabel)
          .setSystem(fhirProperties.getSystems().getSpecimenContainer());

      var containerType = addContainerType(input, mappedType);
      specimenContainerComponent.setType(new CodeableConcept(containerType));
      container.add(specimenContainerComponent);
    }
    specimen.setContainer(container);
  }

  /**
   * @param specimen fhir resource to be modified
   */
  protected void mapSpecimenType(Specimen specimen, PathoSpecimen input) {
    final CodeableConcept specimenTypeCoding = new CodeableConcept();

    if (csvMappings.specimenTypes().containsKey(input.getProbeName())) {
      var type = csvMappings.specimenTypes().get(input.getProbeName());
      specimenTypeCoding.addCoding(type.asFhirCoding());
      specimen.setType(specimenTypeCoding);
    } else {
      log.warn("Specimen type '{}' unknown at input '{}'", input.getProbeName(), input);
    }
  }

  /**
   * nexus container type = 1 has only 2 specimen types cytology vial and formalin-fixed
   * paraffin-embedded block container type = 3 is parent specimen container type = 2 are microscope
   * slides (433466003,Microscope slide (physical object))
   *
   * @param input
   */
  private static Coding addContainerType(
      PathoSpecimen input, SpecimenContainerTyp nexusContainerTyp) {
    var typeCoding = new Coding();
    /*
    brauchen container-ID für
     */

    // alternativ kann man den namen der Probe prüfen '-0-X' ist Zytologie Proeb und '-1-X' sind
    // Gewebe Proben
    switch (nexusContainerTyp) {
      case SUB_CONTAINER:
        {
          if (StringUtils.endsWithIgnoreCase(input.getProbeGewinnungsmethode(), " ZY")) {
            typeCoding
                .setCode("434746001")
                .setDisplay("Specimen vial (physical object)")
                .setSystem(MappingEntry.SNOMED_SYSTEM)
                .setVersion(MappingEntry.SNOMED_VERSION);
            /**
             * specimenTypeCoding .addCoding() .setCode("434746001") .setDisplay( "Cytology specimen
             * vial containing preservative fluid (physical object)")
             * .setSystem(MappingEntry.SNOMED_SYSTEM) .setVersion(MappingEntry.SNOMED_VERSION);*
             */
          } else {
            typeCoding
                .setCode("434464009")
                .setDisplay("Tissue cassette (physical object)")
                .setSystem(MappingEntry.SNOMED_SYSTEM)
                .setVersion("http://snomed.info/sct/900000000000207008/version/20240501");
            /**
             * specimenTypeCoding .addCoding() .setCode("441652008") .setDisplay("Formalin-fixed
             * paraffin-embedded tissue specimen") .setSystem(MappingEntry.SNOMED_SYSTEM)
             * .setVersion("http://snomed.info/sct/900000000000207008/version/20240501");*
             */
          }
          break;
        }
      case MICROSCOPE_SLIDE:
        {
          typeCoding
              .setCode("433466003")
              .setDisplay("Microscope slide (physical object)")
              .setSystem(MappingEntry.SNOMED_SYSTEM)
              .setVersion("http://snomed.info/sct/900000000000207008/version/20240501");
          break;
        }
      case ROOT_CONTAINER:
        {
          throw new IllegalArgumentException(
              "unspecific parent container of nexus typ 3 should not be created here. It represents the specimen as a whole resource.");
        }
    }

    return typeCoding;
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
