/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

// Kafka autoconfiguration can be disabled like this
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
class ApplicationTests {

  @Test
  void contextLoads() {}
}
