/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.model;

import lombok.Data;

@Data
public class MappingEntry {
  private String snomedCode;
  private String snomedDisplayName;
  private String localCode;
  private String localShortName;

  public MappingEntry(
      String localCode, String localShortName, String snomedCode, String snomedDisplayName) {
    this.snomedCode = snomedCode;
    this.snomedDisplayName = snomedDisplayName;
    this.localCode = localCode;
    this.localShortName = localShortName;
  }
}
