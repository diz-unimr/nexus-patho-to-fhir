/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

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

@Service
public class MacroscopicGrouperMapper extends ToFhirMapper {
  private final Logger log = LoggerFactory.getLogger(MacroscopicGrouperMapper.class);

  public MacroscopicGrouperMapper(FhirProperties fhirProperties) {
    super(fhirProperties);
  }

  @Override
  public Observation map(PathoInputBase inputBase) {
    log.debug("creating MacroscopicGrouper from patho-guid '{}'", inputBase.getUUID());
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");
    var pathoFindingGrouper = super.mapBaseGrouper(input);

    // Add identifier
    pathoFindingGrouper.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.MACROSCOPIC_GROUPER,
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
                        "https://www.medizininformatik-initiative.de/fhir/ext/modul-patho/StructureDefinition/mii-pr-patho-macroscopic-grouper"))));

    // Add hasMember
    // TODO: For multiple PathoFindings
    ArrayList<Reference> hasMembers = new ArrayList<>();
    Identifier identifier =
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.PATHO_FINDING,
            fhirProperties.getSystems().getDiagnosticFindingId(),
            "",
            input.getBefundtyp(),
            input.getBefundID(),
            "MACRO");
    hasMembers.add(IdentifierAndReferenceUtil.getReferenceTo("Observation", identifier));
    pathoFindingGrouper.setHasMember(hasMembers);

    // Add ValueString
    pathoFindingGrouper.getValueStringType().setValueAsString(input.getMakroskopischerBefund());
    return pathoFindingGrouper;
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
