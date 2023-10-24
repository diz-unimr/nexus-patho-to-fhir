/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.configuration;

import com.opencsv.CSVReader;
import de.unimarburg.diz.nexuspathotofhir.model.MappingEntry;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvMappingReader {

  private String specimentTypeMappingLocation;
  private String specimenExtractionMethod;

  @Autowired
  public CsvMappingReader(
      @Value("${mapping.location.specimenType}") String specimentTypeMappingLocation,
      @Value("${mapping.location.specimenExtractionMethod}") String specimenExtractionMethod) {
    this.specimentTypeMappingLocation = specimentTypeMappingLocation;
    this.specimenExtractionMethod = specimenExtractionMethod;
  }

  public static Map<String, MappingEntry> readLineByLine(Path filePath) throws Exception {
    var map = new HashMap<String, MappingEntry>();
    try (Reader reader = Files.newBufferedReader(filePath)) {
      try (CSVReader csvReader = new CSVReader(reader)) {
        String[] line;
        while ((line = csvReader.readNext()) != null) {
          if (map.containsKey(line[0]))
            throw new IllegalArgumentException(
                String.format(
                    "mapping file '%s' has duplicate key '%s', please cleanup your mapping definition.",
                    filePath, line[0]));
          map.put(line[0], new MappingEntry(line[0], line[1], line[2], line[3]));
        }
      }
    }
    return map;
  }

  @Bean
  public Map<String, MappingEntry> specimentTypes() throws Exception {
    Path path = Paths.get(ClassLoader.getSystemResource(specimentTypeMappingLocation).toURI());

    return CsvMappingReader.readLineByLine(path);
  }

  @Bean
  public Map<String, MappingEntry> specimenExtractionMethod() throws Exception {
    Path path = Paths.get(ClassLoader.getSystemResource(specimenExtractionMethod).toURI());

    return CsvMappingReader.readLineByLine(path);
  }
}
