/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierResourceType;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PathoFindingDiagConclusionMapper extends ToFhirMapper {
  private final Logger log = LoggerFactory.getLogger(PathoFindingDiagConclusionMapper.class);

  public PathoFindingDiagConclusionMapper(FhirProperties fhirProperties, CsvMappings csvMappings) {
    super(fhirProperties, csvMappings);
  }

  @Override
  public Observation map(PathoInputBase inputBase) {
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");
    var pathoFinding = super.mapBasePathoFinding(input);
    // Add identifier TODO: For multiple observations
    // Add Numbers after befundtype
    pathoFinding.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.PATHO_FINDING,
            fhirProperties.getSystems().getDiagnosticFindingId(),
            "",
            input.getBefundtyp(),
            input.getBefundID(),
            "DIAGNOSE_CONCLUSION"));

    // CategoryCode
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

    // valueCodeableConcept (TODO) SNOMED CODE for the diagnose
    /*      pathoFinding
    .getValueCodeableConcept()
    .setCoding(
        List.of(
            new Coding()
                .setCode("716917000")
                .setSystem("http://snomed.info/sct")
                .setDisplay(
                    "Structure of lateral middle regional part of peripheral zone of right half prostate (body structure)")));*/
    // For first version add the valueString but according to the patho type

    // Add valueString
    pathoFinding.getValueStringType().setValueAsString(input.getDiagnoseConclusion());
    return pathoFinding;
  }

  @Override
  @Nullable public Bundle.BundleEntryComponent apply(PathoInputBase value) {
    var mapped = map(value);
    if (mapped == null) return null;

    final Identifier identifierFirstRep = mapped.getIdentifierFirstRep();
    return buildBundleComponent(mapped, identifierFirstRep);
  }

  @NotNull protected Bundle.BundleEntryComponent buildBundleComponent(
      Observation mapped, Identifier identifierFirstRep) {
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
