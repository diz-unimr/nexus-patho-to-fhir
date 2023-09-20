package de.unimarburg.diz.nexuspathotofhir;

import java.util.function.Function;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class Processor {

    @Bean
    public Function<KStream<String, String>, KStream<String, String>> process() {
        return input -> input;
    }

}
