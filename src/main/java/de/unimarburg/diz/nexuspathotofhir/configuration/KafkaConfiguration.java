/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.configuration;

import de.unimarburg.diz.nexuspathotofhir.serde.FhirDeserializer;
import de.unimarburg.diz.nexuspathotofhir.serde.FhirSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;

@Configuration
@EnableKafka
public class KafkaConfiguration {

  private static final Logger log = LoggerFactory.getLogger(KafkaConfiguration.class);

  @Bean
  public StreamsBuilderFactoryBeanConfigurer streamsBuilderFactoryBeanCustomizer() {
    return factoryBean -> {
      factoryBean.setKafkaStreamsCustomizer(
          kafkaStreams ->
              kafkaStreams.setUncaughtExceptionHandler(
                  e -> {
                    log.error("Uncaught exception occurred.", e);
                    // default handler response
                    return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse
                        .SHUTDOWN_CLIENT;
                  }));
    };
  }

  @Bean
  public Serde<Bundle> fhirSerde() {
    return Serdes.serdeFrom(new FhirSerializer<>(), new FhirDeserializer<>(Bundle.class));
  }
}
