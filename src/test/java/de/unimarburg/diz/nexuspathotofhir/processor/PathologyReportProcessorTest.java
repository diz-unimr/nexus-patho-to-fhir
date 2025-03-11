/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.processor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirConfiguration;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.configuration.PathoFhirContext;
import de.unimarburg.diz.nexuspathotofhir.mapper.*;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.serde.FhirDeserializer;
import de.unimarburg.diz.nexuspathotofhir.serde.FhirSerializer;
import de.unimarburg.diz.nexuspathotofhir.util.DummyDataUtilTest;
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
      PathologyReportProcessor.class,
      PathoFhirContext.class,
      FhirConfiguration.class,
      DiagnosticConclusionGrouperMapper.class,
      DiagnosticReportMapper.class,
      MacroscopicGrouperMapper.class,
      MicroscopicGrouperMapper.class,
      ServiceRequestMapper.class,
      PathoFindingDiagConclusionMapper.class,
      PathoFindingMacroMapper.class,
      PathoFindingMicroMapper.class,
      DiagnosticReportPatchMapper.class,
      CsvMappings.class,
      FhirMapperPathoReport.class
    })
public class PathologyReportProcessorTest {

  @Autowired PathologyReportProcessor processor;

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
        .processPathoReport()
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

      inputTopic.pipeInput("key1", DummyDataUtilTest.getDummyReport());

      var result = outputTopic.readRecordsToList();

      assertThat(result.isEmpty()).isFalse();
      var resultString = result.getFirst().getValue();
      System.out.println(resultString);
      assertThat(result.getFirst().getValue().getEntry().size()).isGreaterThanOrEqualTo(10);
    }
  }
}
