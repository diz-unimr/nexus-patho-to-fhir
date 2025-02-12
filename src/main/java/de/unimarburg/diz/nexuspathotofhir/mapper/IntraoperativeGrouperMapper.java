/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierResourceType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class IntraoperativeGrouperMapper extends ToFhirMapper {
  public IntraoperativeGrouperMapper(FhirProperties fhirProperties) {
    super(fhirProperties);
  }

  @Override
  public Observation map(PathoInputBase inputBase) {
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");
    var result = super.mapBaseGrouper(input);

    if (result == null) return null;

    result.addIdentifier(
        IdentifierAndReferenceUtil.getIdentifier(
            input,
            PathologyIdentifierResourceType.INTERAOPERATIVE_GROUPER,
            fhirProperties.getSystems().getDiagnosticFindingGrouperId()));

    return result;
  }

    @Override
    @Nullable
    public Bundle.BundleEntryComponent apply(PathoInputBase value) {
        var mapped = map(value);
        if (mapped == null) return null;

        final Identifier identifierFirstRep = mapped.getIdentifierFirstRep();
        return buildBundleComponent(mapped, identifierFirstRep);
    }

    @NotNull
    protected Bundle.BundleEntryComponent buildBundleComponent(
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
