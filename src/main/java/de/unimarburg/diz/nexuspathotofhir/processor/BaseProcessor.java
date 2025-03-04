/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.processor;

import com.google.common.hash.Hashing;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReportInputBase;
import java.nio.charset.StandardCharsets;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;

public class BaseProcessor {

  protected static Bundle getBasicBundle(PathoInputBase report) {

    var hash =
        Hashing.farmHashFingerprint64()
            .hashString(report.getUUID(), StandardCharsets.UTF_8)
            .toString();
    return (Bundle)
        new Bundle()
            .setType(Bundle.BundleType.BATCH)
            .setId(hash)
            .setMeta(new Meta().setSource("#nexus-pathology"));
  }

  protected static Bundle getBasicBundle2(PathoReportInputBase report) {

    var hash =
        Hashing.farmHashFingerprint64()
            .hashString(report.getUUID(), StandardCharsets.UTF_8)
            .toString();
    return (Bundle)
        new Bundle()
            .setType(Bundle.BundleType.BATCH)
            .setId(hash)
            .setMeta(new Meta().setSource("#nexus-pathology"));
  }
}
