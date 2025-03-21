/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.configuration;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import de.unimarburg.diz.nexuspathotofhir.model.MappingEntry;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Configuration
@Component
public class CsvMappings {
  private static final Logger log = LoggerFactory.getLogger(CsvMappings.class);
  private final String specimenTypeMappingLocation;
  private final String specimenExtractionMethod;
  private final String specimenContainerType;
  private final String specimenBodysiteMappingLocation;

  private Map<String, MappingEntry> specimenContainerTypeMap;
  private Map<String, MappingEntry> specimenExtractionMethodMap;
  private Map<String, MappingEntry> specimentTypeMappingLocationMap;
  private Map<String, MappingEntry> bodysiteMappingLocationMap;

  @Autowired
  public CsvMappings(
      @Value("${mapping.location.specimenType}") String specimenTypeMappingLocation,
      @Value("${mapping.location.specimenExtractionMethod}") String specimenExtractionMethod,
      @Value("${mapping.location.specimenContainerType}") String specimenContainerType,
      @Value("${mapping.location.specimenBodysite}") String specimenBodysiteMappingLocation) {

    if (!StringUtils.hasLength(specimenTypeMappingLocation))
      throw new IllegalArgumentException("'mapping.location.specimenType' must be set.");
    if (!StringUtils.hasLength(specimenExtractionMethod))
      throw new IllegalArgumentException(
          "'mapping.location.specimenExtractionMethod' must be set.");
    if (!StringUtils.hasLength(specimenContainerType))
      throw new IllegalArgumentException("'mapping.location.specimenContainerType' must be set.");

    this.specimenContainerType = specimenContainerType;
    this.specimenTypeMappingLocation = specimenTypeMappingLocation;
    this.specimenExtractionMethod = specimenExtractionMethod;
    this.specimenBodysiteMappingLocation = specimenBodysiteMappingLocation;
  }

  public static Map<String, MappingEntry> readLineByLine(Path filePath) {
    var map = new HashMap<String, MappingEntry>();

    CsvMappings.log.info("Reading mapping csv from '{}'", filePath);

    try (Reader reader = Files.newBufferedReader(filePath)) {
      try (CSVReader csvReader = new CSVReader(reader)) {
        String[] line;
        while ((line = csvReader.readNext()) != null) {
          if (map.containsKey(line[0]))
            throw new IllegalArgumentException(
                String.format(
                    "mapping file '%s' has duplicate key '%s', please cleanup your mapping definition.",
                    filePath, line[0]));
          try {
            map.put(line[0], new MappingEntry(line[0], line[1], line[2], line[3]));
          } catch (Exception unexpectedArrayLength) {
            CsvMappings.log.error(
                "line length unexpected: '{}'", String.join(",", line), unexpectedArrayLength);
            throw unexpectedArrayLength;
          }
        }
      } catch (CsvValidationException e) {
        CsvMappings.log.error("csv format invalid", e);
        throw new RuntimeException(e);
      }
    } catch (IOException e) {
      CsvMappings.log.error("could not read csv", e);
      throw new RuntimeException(e);
    }

    return map;
  }

  public Map<String, MappingEntry> specimenTypes() {
    if (specimentTypeMappingLocationMap == null) {
      if (!StringUtils.hasText(specimenTypeMappingLocation))
        throw new IllegalArgumentException("path to specimenTypeMappingLocation CSV is empty");
      var path = getPath(specimenTypeMappingLocation);
      specimentTypeMappingLocationMap = CsvMappings.readLineByLine(path);
    }
    return specimentTypeMappingLocationMap;
  }

  public Map<String, MappingEntry> specimenBodysite() {
    if (bodysiteMappingLocationMap == null) {
      if (!StringUtils.hasText(specimenBodysiteMappingLocation))
        throw new IllegalArgumentException("path to specimenBodysiteMappingLocation CSV is empty");
      var path = getPath(specimenBodysiteMappingLocation);
      bodysiteMappingLocationMap = CsvMappings.readLineByLine(path);
    }
    return bodysiteMappingLocationMap;
  }

  public Map<String, MappingEntry> specimenExtractionMethod() {
    if (specimenExtractionMethodMap == null) {
      if (!StringUtils.hasText(specimenExtractionMethod))
        throw new IllegalArgumentException("path to specimenExtractionMethod CSV is empty");
      var path = getPath(specimenExtractionMethod);

      specimenExtractionMethodMap = CsvMappings.readLineByLine(path);
    }
    return specimenExtractionMethodMap;
  }

  public Map<String, MappingEntry> specimenContainerType() {
    if (specimenContainerTypeMap == null) {
      if (!StringUtils.hasText(specimenContainerType))
        throw new IllegalArgumentException("path to specimenContainerType CSV is empty");
      var path = getPath(specimenContainerType);
      specimenContainerTypeMap = CsvMappings.readLineByLine(path);
    }
    return specimenContainerTypeMap;
  }

  @NotNull private Path getPath(String location) {
    Path path;
    try {
      path = Paths.get(location);
    } catch (InvalidPathException e) {
      throw new RuntimeException(e);
    }
    return path;
  }
}
