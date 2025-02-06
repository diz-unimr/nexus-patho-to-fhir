/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.processor;

import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;

public class BaseProcessor {

  protected static Bundle getBasicBundle(PathoInputBase report) {
    return (Bundle)
        new Bundle().setId(report.getUUID()).setMeta(new Meta().setSource("#nexus-pathology"));
  }
}
