/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.model;

import lombok.Data;
import org.hl7.fhir.r4.model.Coding;

@Data
public class MappingEntry {
  private String snomedCode;
  private String snomedDisplayName;
  private String localCode;
  private String localShortName;

  public static final String SNOMED_SYSTEM = "http://snomed.info/sct";

  public MappingEntry(
      String localCode, String localShortName, String snomedCode, String snomedDisplayName) {
    this.snomedCode = snomedCode;
    this.snomedDisplayName = snomedDisplayName;
    this.localCode = localCode;
    this.localShortName = localShortName;
  }

  public Coding asFhirCoding() {
    return new Coding(SNOMED_SYSTEM, this.snomedCode, this.snomedDisplayName);
  }
}
