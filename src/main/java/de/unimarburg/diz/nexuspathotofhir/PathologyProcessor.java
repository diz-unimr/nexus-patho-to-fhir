/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir;

import de.unimarburg.diz.nexuspathotofhir.mapper.ToFhirMapper;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import java.util.Collection;
import java.util.function.Function;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class PathologyProcessor {

  private final Collection<ToFhirMapper> toFhirMappers;

  @Autowired
  public PathologyProcessor(Collection<ToFhirMapper> toFhirMappers) {
    this.toFhirMappers = toFhirMappers;
  }

  public Bundle processReport(PathoReport report) {
    return getBasicBundle(report)
        .setEntry(toFhirMappers.parallelStream().map(a -> a.apply(report)).toList());
  }

  protected static Bundle getBasicBundle(PathoReport report) {
    return (Bundle)
        new Bundle()
            .setId(report.getPathologieBefundId())
            .setMeta(new Meta().setSource("#nexus-pathology"));
  }

  @Bean
  public Function<KStream<String, PathoReport>, KStream<String, Bundle>> process() {
    return input -> input.map((k, v) -> new KeyValue<>(k, processReport(v)));
  }
}
