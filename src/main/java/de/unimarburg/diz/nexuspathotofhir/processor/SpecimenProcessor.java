/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.mapper.SpecimenMapper;
import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.unimarburg.diz.nexuspathotofhir.serde.PathoSpecimenSerde;
import jakarta.validation.constraints.Null;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Specimen;
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
        return getBasicBundle(inputBase).addEntry(specimenMapper.apply(inputBase));
    }

    @Bean
    public Function<KStream<String, PathoSpecimen>, KStream<String, Bundle>> processPathoSpecimen() {
        return input -> input.map((k, v) -> new KeyValue<>(k, processSpecimen(v)));

    }
}
