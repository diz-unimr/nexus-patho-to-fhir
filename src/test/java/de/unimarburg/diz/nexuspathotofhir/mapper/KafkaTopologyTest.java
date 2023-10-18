/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.unimarburg.diz.nexuspathotofhir.DummyDataUtil;
import de.unimarburg.diz.nexuspathotofhir.PathologyProcessor;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirConfiguration;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.configuration.PathoFhirContext;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.serde.FhirDeserializer;
import de.unimarburg.diz.nexuspathotofhir.serde.FhirSerializer;
import java.util.Properties;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

@SpringBootTest(
    classes = {
      FhirProperties.class,
      PathologyProcessor.class,
      PathoFhirContext.class,
      FhirConfiguration.class,
      DiagnosticConclusionGrouperMapper.class,
      DiagnosticFindingMapper.class,
      DiagnosticReportMapper.class,
      IntraoperativeGrouperMapper.class,
      MacroscopicGrouperMapper.class,
      MicroscopicGrouperMapper.class,
      PathoSpecimenMapper.class,
      ServiceRequestMapper.class
    })
public class KafkaTopologyTest {

  @Autowired PathologyProcessor processor;

  /** fixme: basic structure - need to be refined */
  @Test
  public void test() {
    String INPUT_TOPIC = "input";
    String OUTPUT_TOPIC = "output";

    StreamsBuilder builder = new StreamsBuilder();

    final KStream<String, PathoReport> inputStream =
        builder.stream("input", Consumed.with(Serdes.String(), new JsonSerde<>(PathoReport.class)));

    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

    processor
        .process()
        .apply(inputStream)
        .to(
            OUTPUT_TOPIC,
            Produced.with(
                Serdes.String(),
                Serdes.serdeFrom(new FhirSerializer<>(), new FhirDeserializer<>(Bundle.class))));

    try (var testDriver = new TopologyTestDriver(builder.build(), config)) {

      TestInputTopic<String, PathoReport> inputTopic =
          testDriver.createInputTopic(
              INPUT_TOPIC, new StringSerializer(), new JsonSerializer<PathoReport>());

      TestOutputTopic<String, Bundle> outputTopic =
          testDriver.createOutputTopic(
              OUTPUT_TOPIC, new StringDeserializer(), new FhirDeserializer<>(Bundle.class));

      inputTopic.pipeInput("key1", DummyDataUtil.getDummyReport());

      var result = outputTopic.readRecordsToList();

      assertThat(result.isEmpty()).isFalse();
      assertThat(result.get(0).getValue().getEntry().size()).isGreaterThanOrEqualTo(8);
    }
  }
}
