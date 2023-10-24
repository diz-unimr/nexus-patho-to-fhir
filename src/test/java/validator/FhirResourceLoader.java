/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package validator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class FhirResourceLoader {

  private static final Logger log = LoggerFactory.getLogger(FhirResourceLoader.class);

  public static List<IBaseResource> loadFromDirectory(FhirContext ctx, String inputPath) {
    return loadFromDirectory(ctx, inputPath, "*.json", List.of());
  }

  private static List<IBaseResource> loadResources(
      FhirContext ctx, List<String> resourceTypes, Resource[] resources) {
    var parser = ctx.newJsonParser();
    if (resourceTypes.isEmpty()) {
      resourceTypes = List.of("StructureDefinition", "ValueSet", "CodeSystem");
    }

    List<IBaseResource> results = new ArrayList<>();
    for (var r : resources) {
      try (var bufferedReader = new BufferedReader(new InputStreamReader(r.getInputStream()))) {

        var tokener = new JSONTokener(bufferedReader);
        var jsonObject = new JSONObject(tokener);

        if (jsonObject.has("resourceType")
            && resourceTypes.contains(jsonObject.getString("resourceType"))) {
          log.info("Parsing: {}", r.getFilename());
          results.add(parser.parseResource(r.getInputStream()));
        }
      } catch (IOException | JSONException e) {
        e.printStackTrace();
      }
    }
    return results;
  }

  public static List<IBaseResource> loadFromDirectory(
      FhirContext ctx, String inputPath, String fileNamePattern, List<String> resourceTypes) {
    var patternResolver = new PathMatchingResourcePatternResolver();

    try {
      var locationPattern = String.format("file:%s/**/%s", inputPath, fileNamePattern);
      log.info("Looking for input files in: {}", locationPattern);
      var results =
          loadResources(ctx, resourceTypes, patternResolver.getResources(locationPattern));
      log.info("Successfully parsed {} resources.", results.size());
      return results;
    } catch (IOException e) {
      log.debug("Could not get file handle of resource", e);
    }
    return List.of();
  }

  private static IBaseResource tryParseResource(Resource fileResource, FhirContext ctx) {
    try {
      log.debug(fileResource.getFilename());
      var parser = getParser(fileResource.getFile(), ctx);
      if (parser == null) {
        log.debug("Unable to get parser for file {}", fileResource.getFilename());
        return null;
      }
      return parser.parseResource(fileResource.getInputStream());

    } catch (IOException e) {
      log.debug("Could not get file handle of resource", e);
    } catch (DataFormatException de) {
      // not a FHIR resource
      return null;
    }
    return null;
  }

  private static IParser getParser(File file, FhirContext ctx) {
    String fileType = FilenameUtils.getExtension(file.getName()).toLowerCase();
    if (fileType.equals("xml")) {
      return ctx.newXmlParser();
    } else if (fileType.equals("json")) {
      return ctx.newJsonParser();
    }
    return null;
  }

  public static List<IBaseResource> loadFromClasspath(FhirContext ctx, String inputPath) {
    return loadFromDirectory(ctx, inputPath, "*.json", List.of());
  }

  public static List<IBaseResource> loadFromClasspath(
      FhirContext ctx, String inputPath, String fileNamePattern) {
    return loadFromClasspath(ctx, inputPath, fileNamePattern, List.of());
  }

  public static List<IBaseResource> loadFromClasspath(
      FhirContext ctx, String inputPath, String fileNamePattern, List<String> resourceTypes) {
    var patternResolver = new PathMatchingResourcePatternResolver();
    try {
      var locationPattern = String.format("classpath:%s/**/%s", inputPath, fileNamePattern);
      log.info("Looking for input files in: {}", locationPattern);
      var results =
          loadResources(ctx, resourceTypes, patternResolver.getResources(locationPattern));
      log.info("Successfully parsed {} resources.", results.size());
      return results;
    } catch (IOException e) {
      log.debug("Could not get file handle of resource", e);
    }
    return List.of();
  }
}
