/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package validator;

import static org.apache.commons.lang3.StringUtils.isBlank;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

public class BestEffortInMemoryTerminologyServerValidationSupport
    extends InMemoryTerminologyServerValidationSupport {

  public BestEffortInMemoryTerminologyServerValidationSupport(FhirContext theCtx) {
    super(theCtx);
  }

  @Override
  public boolean isCodeSystemSupported(
      ValidationSupportContext theValidationSupportContext, String theSystem) {
    if (isBlank(theSystem)) {
      return false;
    }

    IBaseResource cs = getCodeSystem(theValidationSupportContext, theSystem);

    if (!getFhirContext().getVersion().getVersion().isEqualOrNewerThan(FhirVersionEnum.DSTU2_1)) {
      return cs != null;
    }

    if (cs != null) {
      IPrimitiveType<?> content = getCodeSystemContent(cs);
      // explicitly allow "not-present" here
      return StringUtils.isNotBlank(content.getValueAsString());
    }

    return false;
  }

  private IBaseResource getCodeSystem(
      ValidationSupportContext theValidationSupportContext, String theSystem) {
    return theValidationSupportContext.getRootValidationSupport().fetchCodeSystem(theSystem);
  }

  private IPrimitiveType<?> getCodeSystemContent(IBaseResource cs) {
    return getFhirContext().newTerser().getSingleValueOrNull(cs, "content", IPrimitiveType.class);
  }

  @Override
  @Nullable public CodeValidationResult validateCode(
      ValidationSupportContext theValidationSupportContext,
      ConceptValidationOptions theOptions,
      String theCodeSystem,
      String theCode,
      String theDisplay,
      String theValueSetUrl) {

    if ("not-present"
        .equals(
            getCodeSystemContent(getCodeSystem(theValidationSupportContext, theCodeSystem))
                .getValueAsString())) {
      return new CodeValidationResult()
          .setSeverity(IssueSeverity.WARNING)
          .setMessage("Unable to validate code. CodeSystem is known but content is 'not-present'");
    }

    return super.validateCode(
        theValidationSupportContext,
        theOptions,
        theCodeSystem,
        theCode,
        theDisplay,
        theValueSetUrl);
  }
}
