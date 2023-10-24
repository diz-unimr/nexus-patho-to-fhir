/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package validator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import java.io.IOException;
import java.util.List;
import org.hl7.fhir.common.hapi.validation.support.*;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FhirProfileValidator {

  private static final Logger log = LoggerFactory.getLogger(FhirProfileValidator.class);

  private final PrePopulatedValidationSupport customValidation;
  private final FhirContext fhirContext;
  private final FhirValidator validator;
  private final NpmPackageValidationSupport npmPackageSupport;

  public FhirProfileValidator(FhirContext ctx) {
    fhirContext = ctx;
    validator = ctx.newValidator();

    customValidation = new PrePopulatedValidationSupport(ctx);
    npmPackageSupport = new NpmPackageValidationSupport(ctx);

    var validationSupportChain =
        new ValidationSupportChain(
            customValidation,
            npmPackageSupport,
            new SnapshotGeneratingValidationSupport(ctx),
            new DefaultProfileValidationSupport(ctx),
            new BestEffortInMemoryTerminologyServerValidationSupport(ctx));

    var cache = new CachingValidationSupport(validationSupportChain);
    var validatorModule = new FhirInstanceValidator(cache);
    validator.registerValidatorModule(validatorModule);
  }

  public static void prettyPrint(Logger log, ValidationResult validationResult) {
    validationResult
        .getMessages()
        .forEach(
            message -> {
              switch (message.getSeverity()) {
                case ERROR:
                  log.error(
                      (char) 27
                          + "[31mFHIR Validation"
                          + (char) 27
                          + "[0m"
                          + ": "
                          + message.getLocationString()
                          + " - "
                          + message.getMessage());
                  break;
                case WARNING:
                  log.warn(
                      (char) 27
                          + "[33mFHIR Validation"
                          + (char) 27
                          + "[0m"
                          + ": "
                          + message.getLocationString()
                          + " - "
                          + message.getMessage());
                  break;
                case INFORMATION:
                  log.info(
                      (char) 27
                          + "[34mFHIR Validation"
                          + (char) 27
                          + "[0m"
                          + ": "
                          + message.getLocationString()
                          + " - "
                          + message.getMessage());
                  break;
                default:
                  log.debug(
                      "Validation issue "
                          + message.getSeverity()
                          + " - "
                          + message.getLocationString()
                          + " - "
                          + message.getMessage());
              }
            });
  }

  public static void prettyPrint(ValidationResult validationResult) {
    prettyPrint(log, validationResult);
  }

  public FhirProfileValidator withResourcesFrom(String inputPath) {
    return withResourcesFrom(inputPath, "*.json", List.of());
  }

  public FhirProfileValidator withResourcesFrom(String inputPath, String fileNamePattern) {
    return withResourcesFrom(inputPath, fileNamePattern, List.of());
  }

  public FhirProfileValidator withResourcesFrom(String inputPath, List<String> resourceTypes) {
    return withResourcesFrom(inputPath, "*.json", resourceTypes);
  }

  public FhirProfileValidator withResourcesFrom(
      String inputPath, String fileNamePattern, List<String> resourceTypes) {
    final List<IBaseResource> iBaseResources =
        FhirResourceLoader.loadFromDirectory(
            fhirContext, inputPath, fileNamePattern, resourceTypes);
    iBaseResources.forEach(
        r -> {
          if (r instanceof StructureDefinition) {
            customValidation.addStructureDefinition(r);
          } else if (r instanceof CodeSystem) {
            customValidation.addCodeSystem(r);
          } else if (r instanceof ValueSet) {
            customValidation.addValueSet(r);
          }
        });

    return this;
  }

  public FhirProfileValidator withPackages(String... packageLocations) {
    for (var location : packageLocations) {

      try {
        npmPackageSupport.loadPackageFromClasspath(location);
      } catch (IOException e) {
        log.error("Unable to load package from: {}", location);
      }
    }
    return this;
  }

  public ValidationResult validateWithResult(IBaseResource theResource) {
    return validator.validateWithResult(theResource);
  }
}
