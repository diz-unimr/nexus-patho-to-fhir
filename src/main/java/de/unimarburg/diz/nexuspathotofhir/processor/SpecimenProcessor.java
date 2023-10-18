/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.processor;

import de.unimarburg.diz.nexuspathotofhir.mapper.PathoSpecimenMapper;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import java.util.function.Function;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class SpecimenProcessor extends BaseProcessor {
  private final PathoSpecimenMapper pathoSpecimenMapper;

  @Autowired
  public SpecimenProcessor(PathoSpecimenMapper pathoSpecimenMapper) {
    this.pathoSpecimenMapper = pathoSpecimenMapper;
  }

  public Bundle processSpecimen(PathoInputBase inputBase) {
    return getBasicBundle(inputBase).addEntry(pathoSpecimenMapper.apply(inputBase));
  }

  @Bean
  public Function<KStream<String, PathoSpecimen>, KStream<String, Bundle>> processPathoSpecimen() {
    return input -> input.map((k, v) -> new KeyValue<>(k, processSpecimen(v)));
  }
}
