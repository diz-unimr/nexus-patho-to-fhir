/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReportInputBase;
import de.unimarburg.diz.nexuspathotofhir.util.IdentifierAndReferenceUtil;
import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierResourceType;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DiagnosticReportPatchMapper extends ToFhirMapper {

  public static final Logger logger = LoggerFactory.getLogger(DiagnosticReportPatchMapper.class);

  @Autowired
  public DiagnosticReportPatchMapper(FhirProperties fhirProperties, CsvMappings csvMappings) {
    super(fhirProperties, csvMappings);
  }

  public Parameters map(PathoReportInputBase inputBase) {
    if (!(inputBase instanceof PathoReport input))
      throw new IllegalArgumentException("input must be a PathoReport");

    Parameters parameters = new Parameters();
    Parameters.ParametersParameterComponent operationParam =
        new Parameters.ParametersParameterComponent();
    operationParam.setName("operation");
    // Add parts to the operation parameter
    operationParam.addPart(
        new Parameters.ParametersParameterComponent()
            .setName("type")
            .setValue(new CodeType("add")));
    operationParam.addPart(
        new Parameters.ParametersParameterComponent()
            .setName("path")
            .setValue(new StringType("DignosticReport")));
    operationParam.addPart(
        new Parameters.ParametersParameterComponent()
            .setName("name")
            .setValue(new StringType("result")));

    Parameters.ParametersParameterComponent resultPart =
        new Parameters.ParametersParameterComponent();
    // resultPart.setName("value");

    // Create the operation parameter
    if (StringUtils.hasText(inputBase.getDiagnoseConclusion())) {
      Identifier idPathoFindingGrouperDiagConclusion =
          IdentifierAndReferenceUtil.getIdentifier(
              input,
              PathologyIdentifierResourceType.DIAGNOSTIC_CONCLUSION_GROUPER,
              fhirProperties.getSystems().getPathoDiagnosticConclusionGrouperId());
      Reference reference =
          IdentifierAndReferenceUtil.getReferenceTo(
              "Observation", idPathoFindingGrouperDiagConclusion);
      // Add the operation parameter to the parameters resource
      resultPart.addPart(
          new Parameters.ParametersParameterComponent()
              .setName("reference")
              .setValue(new StringType(reference.getReference())));

    } else {
      logger.info("No makroskopischerBefund found");
    }
    if (StringUtils.hasText(inputBase.getMikroskopischerBefund())) {
      Identifier idPathoFindingGrouperMicro =
          IdentifierAndReferenceUtil.getIdentifier(
              input,
              PathologyIdentifierResourceType.MICROSCOPIC_GROUPER,
              fhirProperties.getSystems().getPathoMicroGrouperId());
      Reference reference =
          IdentifierAndReferenceUtil.getReferenceTo("Observation", idPathoFindingGrouperMicro);
      // Add the operation parameter to the parameters resource
      resultPart.addPart(
          new Parameters.ParametersParameterComponent()
              .setName("reference")
              .setValue(new StringType(reference.getReference())));

    } else {
      logger.info("No makroskopischerBefund found");
    }
    if (StringUtils.hasText(inputBase.getMakroskopischerBefund())) {
      Identifier idPathoFindingGrouperMacro =
          IdentifierAndReferenceUtil.getIdentifier(
              input,
              PathologyIdentifierResourceType.MACROSCOPIC_GROUPER,
              fhirProperties.getSystems().getPathoMacroGrouperId());
      Reference reference =
          IdentifierAndReferenceUtil.getReferenceTo("Observation", idPathoFindingGrouperMacro);
      // Add the operation parameter to the parameters resource
      resultPart.addPart(
          new Parameters.ParametersParameterComponent()
              .setName("reference")
              .setValue(new StringType(reference.getReference())));
    }
    operationParam.addPart(resultPart);
    parameters.addParameter(operationParam);
    return parameters;
  }

  @Nullable public Bundle.BundleEntryComponent apply(PathoReportInputBase value) {
    var mapped = map(value);
    var resourceType = "DiagnosticReport";
    if (mapped == null) return null;
    // identifier

    Identifier identifier =
        IdentifierAndReferenceUtil.getIdentifier(
            value,
            PathologyIdentifierResourceType.DIAGNOSTIC_REPORT,
            fhirProperties.getSystems().getDiagnosticReportId());
    return buildBundleComponent(mapped, identifier, resourceType);
  }

  @NotNull protected Bundle.BundleEntryComponent buildBundleComponent(
      Parameters mapped, Identifier identifierFirstRep, String resourceType) {
    return new Bundle.BundleEntryComponent()
        .setResource(mapped)
        .setRequest(
            new Bundle.BundleEntryRequestComponent()
                .setMethod(Bundle.HTTPVerb.PATCH)
                .setUrl(
                    String.format(
                        "%s?identifier=%s|%s",
                        resourceType,
                        identifierFirstRep.getSystem(),
                        identifierFirstRep.getValue())));
  }
}
