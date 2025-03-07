/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.processor;

import de.unimarburg.diz.nexuspathotofhir.mapper.*;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import java.util.function.Function;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class PathologyReportProcessor extends BaseProcessor {

  private final FhirMapperPathoReport pathoMapper;

  PathologyReportProcessor(FhirMapperPathoReport pathoMapper) {
    this.pathoMapper = pathoMapper;
  }

  public Bundle processReport(PathoReport report) {
    return pathoMapper.mapperHandler(report, getBasicBundleReport(report));
  }

  @Bean
  public Function<KStream<String, PathoReport>, KStream<String, Bundle>> processPathoReport() {
    return input -> input.map((k, v) -> new KeyValue<>(k, processReport(v)));
  }
}
