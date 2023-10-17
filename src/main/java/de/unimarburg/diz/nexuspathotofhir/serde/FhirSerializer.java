/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.serde;

import de.unimarburg.diz.nexuspathotofhir.configuration.PathoFhirContext;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.serialization.Serializer;
import org.hl7.fhir.r4.model.Resource;

public class FhirSerializer<T extends Resource> implements Serializer<T> {

  @Override
  public byte[] serialize(String topic, T data) {
    if (data == null) {
      return null;
    }

    return PathoFhirContext.getInstance()
        .newJsonParser()
        .encodeResourceToString(data)
        .getBytes(StandardCharsets.UTF_8);
  }
}
