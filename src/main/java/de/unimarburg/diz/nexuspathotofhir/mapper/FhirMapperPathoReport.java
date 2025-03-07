/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReportInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoTextMapperResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FhirMapperPathoReport {

  public static final Logger logger = LoggerFactory.getLogger(FhirMapperPathoReport.class);

  private final ServiceRequestMapper serviceRequestMapper;
  private final DiagnosticReportMapper diagnosticReportMapper;
  private final DiagnosticReportPatchMapper diagnosticReportPatchMapper;
  private final DiagnosticConclusionGrouperMapper diagnosticConclusionGrouperMapper;
  private final PathoFindingDiagConclusionMapper pathoFindingDiagConclusionMapper;
  private final MicroscopicGrouperMapper microscopicGrouperMapper;
  private final PathoFindingMicroMapper pathoFindingMicroMapper;
  private final MacroscopicGrouperMapper macroscopicGrouperMapper;
  private final PathoFindingMacroMapper pathoFindingMacroMapper;
  private final FhirProperties fhirProperties;

  @Autowired
  public FhirMapperPathoReport(
      FhirProperties fhirProperties,
      ServiceRequestMapper serviceRequestMapper,
      DiagnosticReportMapper diagnosticReportMapper,
      DiagnosticReportPatchMapper diagnosticReportPatchMapper,
      MacroscopicGrouperMapper macroscopicGrouperMapper,
      PathoFindingMacroMapper pathoFindingMacroMapper,
      MicroscopicGrouperMapper microscopicGrouperMapper,
      PathoFindingMicroMapper pathoFindingMicroMapper,
      DiagnosticConclusionGrouperMapper diagnosticConclusionGrouperMapper,
      PathoFindingDiagConclusionMapper pathoFindingDiagConclusionMapper) {
    this.fhirProperties = fhirProperties;
    this.serviceRequestMapper = serviceRequestMapper;
    this.diagnosticReportMapper = diagnosticReportMapper;
    this.diagnosticReportPatchMapper = diagnosticReportPatchMapper;
    this.macroscopicGrouperMapper = macroscopicGrouperMapper;
    this.pathoFindingMacroMapper = pathoFindingMacroMapper;
    this.microscopicGrouperMapper = microscopicGrouperMapper;
    this.pathoFindingMicroMapper = pathoFindingMicroMapper;
    this.diagnosticConclusionGrouperMapper = diagnosticConclusionGrouperMapper;
    this.pathoFindingDiagConclusionMapper = pathoFindingDiagConclusionMapper;
  }

  // Map the string
  public Bundle mapperHandler(PathoReportInputBase inputBase, Bundle bundle) {
    if (!(inputBase instanceof PathoReport input)) {
      throw new IllegalArgumentException("input must be a PathoReport");
    }
    String befundId = inputBase.getBefundID();
    String befundTyp = inputBase.getBefundtyp().replaceAll("\\s+", "");
    // construct serviceRequest
    bundle.addEntry(serviceRequestMapper.apply(inputBase));

    // Handle DiagnosticConclusion
    // TODO: Decide whether the pathoReport of type Hauptbefund oder andere typ

    // Handle MacroBefund
    if (StringUtils.hasText(inputBase.getMakroskopischerBefund())) {
      // ToDo: Adjust me with the result of AI Part
      ArrayList<String> extractedWords =
          new ArrayList<>(Arrays.asList(inputBase.getMakroskopischerBefund().split(" ")));
      PathoTextMapperResult pathoTextMapperResultMacro =
          createCodeValueIds(befundId, befundTyp, extractedWords);
      var codeValueMap = pathoTextMapperResultMacro.getCodeMap();
      var idList = pathoTextMapperResultMacro.getIdentifiersCodeMapArray();
      int index = 0;
      bundle.addEntry(
          macroscopicGrouperMapper.apply(
              inputBase, idList, fhirProperties.getSystems().getPathoFindingMacroId()));
      // Iterate over the ArrayList using a for loop
      // Iterate over the entries of the current HashMap
      for (String key : codeValueMap.keySet()) {
        String value = codeValueMap.get(key);
        // Create an Observation - PathoFinding
        // Add this to the Bundle
        bundle.addEntry(
            pathoFindingMacroMapper.apply(
                inputBase,
                idList.get(index),
                fhirProperties.getSystems().getPathoFindingMacroId(),
                key,
                value));
        index++;
      }

    } else {
      logger.info("No makroskopischerBefund found");
    }

    // Handle MicroBefund
    if (StringUtils.hasText(inputBase.getMikroskopischerBefund())) {
      // ToDo: Adjust me with the result of AI Part
      ArrayList<String> extractedWords =
          new ArrayList<>(Arrays.asList(inputBase.getMikroskopischerBefund().split(" ")));
      PathoTextMapperResult pathoTextMapperMicro =
          createCodeValueIds(befundId, befundTyp, extractedWords);
      var codeValueMap = pathoTextMapperMicro.getCodeMap();
      var idList = pathoTextMapperMicro.getIdentifiersCodeMapArray();
      int index = 0;
      bundle.addEntry(
          microscopicGrouperMapper.apply(
              inputBase, idList, fhirProperties.getSystems().getPathoFindingMicroId()));
      // Iterate over the ArrayList using a for loop
      // Iterate over the entries of the current HashMap
      for (String key : codeValueMap.keySet()) {
        String value = codeValueMap.get(key);
        // Create an Observation - PathoFinding
        // Add this to the Bundle
        bundle.addEntry(
            pathoFindingMicroMapper.apply(
                inputBase,
                idList.get(index),
                fhirProperties.getSystems().getPathoFindingMicroId(),
                key,
                value));
        index++;
      }

    } else {
      logger.info("No makroskopischerBefund found");
    }

    // Handle DiagnoseConclusion

    if (StringUtils.hasText(inputBase.getDiagnoseConclusion())) {
      // ToDo: Adjust me with the result of AI Part
      ArrayList<String> extractedWords =
          new ArrayList<>(Arrays.asList(inputBase.getDiagnoseConclusion().split(" ")));
      PathoTextMapperResult pathoTextMapperDiagConc =
          createCodeValueIds(befundId, befundTyp, extractedWords);
      var codeValueMap = pathoTextMapperDiagConc.getCodeMap();
      var idList = pathoTextMapperDiagConc.getIdentifiersCodeMapArray();
      int index = 0;
      bundle.addEntry(
          diagnosticConclusionGrouperMapper.apply(
              inputBase, idList, fhirProperties.getSystems().getPathoFindingDiagConcId()));
      // Iterate over the ArrayList using a for loop
      // Iterate over the entries of the current HashMap
      for (String key : codeValueMap.keySet()) {
        String value = codeValueMap.get(key);
        // Create an Observation - PathoFinding
        // Add this to the Bundle
        bundle.addEntry(
            pathoFindingDiagConclusionMapper.apply(
                inputBase,
                idList.get(index),
                fhirProperties.getSystems().getPathoFindingDiagConcId(),
                key,
                value));
        index++;
      }
    } else {
      logger.info("No makroskopischerBefund found");
    }
    if (befundTyp.contains("Hauptbefund")) {
      bundle.addEntry(diagnosticReportMapper.apply(inputBase));
    } else
      // Create a Patch DianosticReport
      // TODO
      bundle.addEntry(diagnosticReportPatchMapper.apply(inputBase));
    return bundle;
  }

  public static PathoTextMapperResult createCodeValueIds(
      String befundId, String bedundTyp, ArrayList<String> extractedWords) {
    HashMap<String, String> codeValueFromText = new HashMap<>();
    ArrayList<String> customPathoIds = new ArrayList<>();
    for (int i = 0; i < extractedWords.size(); i++) {
      System.out.println(extractedWords.get(i));
      codeValueFromText.put(i + "", extractedWords.get(i)); // Serial number starts from 1
      customPathoIds.add(befundId + "-" + bedundTyp + "-" + i); // Generate new string
    }
    System.out.println(customPathoIds);
    return new PathoTextMapperResult(codeValueFromText, customPathoIds);
  }
}
