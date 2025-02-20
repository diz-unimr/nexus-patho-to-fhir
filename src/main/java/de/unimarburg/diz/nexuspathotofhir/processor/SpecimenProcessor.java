/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.processor;

import de.unimarburg.diz.nexuspathotofhir.mapper.SpecimenMapper;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import java.util.function.Function;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class SpecimenProcessor extends BaseProcessor {
  private final SpecimenMapper specimenMapper;

  @Autowired
  public SpecimenProcessor(SpecimenMapper specimenMapper) {
    this.specimenMapper = specimenMapper;
  }

  public Bundle processSpecimen(PathoInputBase inputBase) {
    final Bundle bundle = getBasicBundle(inputBase).addEntry(specimenMapper.apply(inputBase));
    // do not create empty bundles
    if (bundle.getEntry().isEmpty()) return null;
    return bundle;
  }

  @Bean
  public Function<KStream<String, PathoSpecimen>, KStream<String, Bundle>> processPathoSpecimen() {
    return input -> input.map((k, v) -> new KeyValue<>(k, processSpecimen(v)));
  }
}
