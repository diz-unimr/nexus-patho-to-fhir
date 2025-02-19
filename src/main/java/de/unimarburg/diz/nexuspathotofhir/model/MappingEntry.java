/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.model;

import lombok.Data;
import org.hl7.fhir.r4.model.Coding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

@Data
public class MappingEntry {
  private String snomedCode;
  private String snomedDisplayName;
  private String localCode;
  private String localShortName;

  public static final String SNOMED_SYSTEM = "http://snomed.info/sct";
  public static final String SNOMED_VERSION =
      "http://snomed.info/sct/900000000000207008/version/20250101";

  private static final Logger log = LoggerFactory.getLogger(MappingEntry.class);

  public MappingEntry(
      String localCode, String localShortName, String snomedCode, String snomedDisplayName) {
    this.snomedCode = snomedCode;
    this.snomedDisplayName = snomedDisplayName;
    this.localCode = localCode;
    this.localShortName = localShortName;
  }

  public Coding asFhirCoding() {
    var coding = new Coding(SNOMED_SYSTEM, this.snomedCode, this.snomedDisplayName);
    coding.setVersion(SNOMED_VERSION);

    if (!StringUtils.hasText(this.snomedCode)) {
      log.warn("unknown code '{}' - please update your mapping file.", localCode);
    }
    return coding;
  }
}
