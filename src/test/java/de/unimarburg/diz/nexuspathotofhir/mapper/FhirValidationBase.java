/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import ca.uhn.fhir.context.FhirContext;
import org.junit.jupiter.api.BeforeAll;
import validator.FhirProfileValidator;

public abstract class FhirValidationBase {
  protected static FhirProfileValidator validator;
  protected static FhirContext fhirContext;

  @BeforeAll
  static void loadValidation() {
    fhirContext = FhirContext.forR4();
    validator =
        new FhirProfileValidator(fhirContext).withResourcesFrom("node_modules", "*patho*.json");
  }
}
