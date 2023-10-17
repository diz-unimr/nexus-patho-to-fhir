/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.configuration;

import ca.uhn.fhir.context.FhirContext;

public class PathoFhirContext {
  private static final FhirContext fhirContext = FhirContext.forR4();

  public static FhirContext getInstance() {
    return fhirContext;
  }
}
