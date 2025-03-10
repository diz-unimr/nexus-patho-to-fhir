/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.configuration;

import ca.uhn.fhir.context.FhirContext;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class FhirConfiguration {

  private final Logger log = LoggerFactory.getLogger(FhirConfiguration.class);

  public FhirConfiguration(FhirProperties fhirProperties) {
    this.fhirProperties = fhirProperties;
  }

  @PostConstruct
  private void logConfig() {
    log.info(this.toString());
  }

  @Bean
  public FhirContext fhirContext() {
    return PathoFhirContext.getInstance();
  }

  public final FhirProperties fhirProperties;

  @Override
  public String toString() {
    return "FhirConfiguration{" + "fhirProperties=" + fhirProperties + '}';
  }
}
