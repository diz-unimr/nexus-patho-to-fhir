/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierResourceType;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Represents german diagnostic report section named 'Diagnostische Schlussfolgerung' It is
 * referenced by its findings (observations)
 *
 * <p>FIXME: profile allows/suggests multiple grouper instance one per specimen referenced within a
 * report - FIXME check if multiple specimen references applicable to nexus
 */
@Service
public class DiagnosticConclusionGrouperMapper extends ToFhirMapper {

  private final Logger log = LoggerFactory.getLogger(DiagnosticConclusionGrouperMapper.class);

  public DiagnosticConclusionGrouperMapper(FhirProperties fhirProperties, CsvMappings csvMappings) {
    super(fhirProperties, csvMappings);
  }

  @Override
  public Observation map(PathoInputBase inputBase) {
    log.debug("creating DiagnosticConclusionGrouper from patho-guid '{}'", inputBase.getUUID());
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");
    var pathoFindingGrouper = super.mapBaseGrouper(input);

    // Add identifier
    pathoFindingGrouper.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.DIAGNOSTIC_CONCLUSION_GROUPER,
            fhirProperties.getSystems().getDiagnosticFindingGrouperId(),
            "",
            input.getBefundtyp(),
            input.getBefundID()));

    // Add Meta: source, profile
    pathoFindingGrouper.setMeta(
        new Meta()
            .setSource(META_SOURCE)
            .setProfile(
                List.of(
                    new CanonicalType(
                        "https://www.medizininformatik-initiative.de/fhir/ext/modul-patho/StructureDefinition/mii-pr-patho-diagnostic-conclusion-grouper"))));

    // Add hasMember
    // TODO: For multiple PathoFindings
    ArrayList<Reference> hasMembers = new ArrayList<>();
    Identifier identifierPathoFinding =
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.PATHO_FINDING,
            fhirProperties.getSystems().getDiagnosticFindingId(),
            input.getBefundtyp(),
            input.getBefundID(),
            "DIAGNOSE_CONCLUSION");
    hasMembers.add(
        IdentifierAndReferenceUtil.getReferenceTo("Observation", identifierPathoFinding));
    pathoFindingGrouper.setHasMember(hasMembers);

    // Add derivedFrom
    ArrayList<Reference> derievedFrom = new ArrayList<>();

    // Add macroscopic-grouper
    Identifier idPathoFindingGrouperMacro =
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.MACROSCOPIC_GROUPER,
            fhirProperties.getSystems().getDiagnosticFindingGrouperId(),
            "",
            input.getBefundtyp(),
            input.getBefundID());

    // Add microscopic-grouper
    Identifier idPathoFindingGrouperMicro =
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.MICROSCOPIC_GROUPER,
            fhirProperties.getSystems().getDiagnosticFindingGrouperId(),
            "",
            input.getBefundtyp(),
            input.getBefundID());
    derievedFrom.add(
        IdentifierAndReferenceUtil.getReferenceTo("Observation", idPathoFindingGrouperMacro));
    derievedFrom.add(
        IdentifierAndReferenceUtil.getReferenceTo("Observation", idPathoFindingGrouperMicro));
    pathoFindingGrouper.setDerivedFrom(derievedFrom);

    // Add ValueString
    pathoFindingGrouper.getValueStringType().setValueAsString(input.getDiagnoseConclusion());
    return pathoFindingGrouper;
  }

  protected boolean hasDiagnosticConclusionData(PathoReport input) {
    // FIXME: implement
    return true;
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
