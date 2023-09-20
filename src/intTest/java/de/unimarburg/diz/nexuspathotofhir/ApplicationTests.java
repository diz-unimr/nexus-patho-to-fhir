package de.unimarburg.diz.nexuspathotofhir;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
