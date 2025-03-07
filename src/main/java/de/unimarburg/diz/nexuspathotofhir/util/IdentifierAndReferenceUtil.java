/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.util;

import de.unimarburg.diz.nexuspathotofhir.model.PathoReportInputBase;
import org.hl7.fhir.r4.model.*;
import org.springframework.util.StringUtils;

public class IdentifierAndReferenceUtil {

  /**
   * Build identifier encounterNumber-journalNumber-arg1-arg2-...
   *
   * <p>exception is patient: only patient id is returned
   *
   * @param inputBase input raw data
   * @param identType target resource type
   * @param args values from which we build identifiers
   * @return identifier string
   */
  public static String getPathoIdentifierValue(
      PathoReportInputBase inputBase, PathologyIdentifierResourceType identType, String[] args) {
    if (identType == null) throw new IllegalArgumentException("identType was null");
    if (inputBase == null) throw new IllegalArgumentException("inputBase was null");
    if (!StringUtils.hasText(inputBase.getAuftragsnummer()))
      throw new IllegalArgumentException("inputBase.Auftragnummer was null");
    var builder = new StringBuilder();
    if (identType == PathologyIdentifierResourceType.PATIENT) {
      return inputBase.getPatientennummer();
    } else if (identType == PathologyIdentifierResourceType.SERVICE_REQUEST) {
      return inputBase.getAuftragsnummer();
    } else if (identType == PathologyIdentifierResourceType.DIAGNOSTIC_REPORT) {
      return inputBase.getAuftragsnummer();
    } else if (identType == PathologyIdentifierResourceType.MICROSCOPIC_GROUPER
        || identType == PathologyIdentifierResourceType.MACROSCOPIC_GROUPER
        || identType == PathologyIdentifierResourceType.DIAGNOSTIC_CONCLUSION_GROUPER) {
      builder.append(inputBase.getAuftragsnummer());
      builder.append("-");
      builder.append(inputBase.getBefundtyp().replaceAll("\\s+", ""));
    } else if (identType == PathologyIdentifierResourceType.PATHO_FINDING) {
    }
    if (args != null && args.length > 0) builder.append(String.join("-", args));
    return builder.toString();
  }

  public static Identifier getIdentifier(
      PathoReportInputBase inputBase,
      PathologyIdentifierResourceType identType,
      String system,
      String... args) {
    return new Identifier()
        .setSystem(system)
        .setValue(getPathoIdentifierValue(inputBase, identType, args));
  }

  public static Identifier getIdentifierWithType(
      PathoReportInputBase inputBase,
      PathologyIdentifierType pathologyIdentifierType,
      PathologyIdentifierResourceType identType,
      String system,
      String... args) {

    Identifier identifier = new Identifier();
    identifier.setValue(getPathoIdentifierValue(inputBase, identType, args));
    identifier.setSystem(system);
    // Type
    CodeableConcept codeableConceptIdType = new CodeableConcept();
    switch (pathologyIdentifierType) {
      case PLAC:
        codeableConceptIdType.addCoding(
            new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                .setCode("PLAC"));
        break;
      case FILL:
        codeableConceptIdType.addCoding(
            new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                .setCode("FILL"));
        break;
      case ACSN:
        codeableConceptIdType.addCoding(
            new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                .setCode("ACSN"));
        break;
    }

    identifier.setType(codeableConceptIdType);
    return identifier;
  }

  public static Identifier getIdentifier(
      PathoReportInputBase inputBase, PathologyIdentifierResourceType identType, String system) {
    return new Identifier()
        .setSystem(system)
        .setValue(getPathoIdentifierValue(inputBase, identType, null));
  }

  public static Reference getReferenceTo(String resourceR4Name, Identifier identifier) {
    return new Reference(
        String.format(
            "%s?identifier=%s|%s", resourceR4Name, identifier.getSystem(), identifier.getValue()));
  }

  public static Reference getReferenceTo(String resourceR4Name, String value, String system) {
    return new Reference(String.format("%s?identifier=%s|%s", resourceR4Name, system, value));
  }
}
