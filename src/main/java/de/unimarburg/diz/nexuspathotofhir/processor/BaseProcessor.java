/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.processor;

import com.google.common.hash.Hashing;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import java.nio.charset.StandardCharsets;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;

public class BaseProcessor {

  protected static Bundle getBasicBundle(PathoInputBase report) {

    var hash =
        Hashing.farmHashFingerprint64()
            .hashString(report.getUUID(), StandardCharsets.UTF_8)
            .toString();
    return (Bundle) new Bundle().setId(hash).setMeta(new Meta().setSource("#nexus-pathology"));
  }
}
