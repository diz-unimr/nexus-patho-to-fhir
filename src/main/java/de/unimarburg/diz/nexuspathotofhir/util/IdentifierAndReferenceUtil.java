/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.util;

import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.util.StringUtils;

public class IdentifierAndReferenceUtil {

  /**
   * @param pathoReport
   * @param identType
   * @param args
   * @return
   */
  public static String getPathoIdentifierValue(
      PathoReport pathoReport, PathologyIdentifierType identType, String[] args) {
    if (identType == null) throw new IllegalArgumentException("identType was null");
    if (pathoReport == null) throw new IllegalArgumentException("pathoReport was null");
    if (!StringUtils.hasText(pathoReport.getAuftragnummer()))
      throw new IllegalArgumentException("pathoReport.Auftragnummer was null");

    var builder = new StringBuilder();

    if (identType == PathologyIdentifierType.PATIENT) {
      return pathoReport.getPatientennummer();
    } else {
      builder.append(pathoReport.getFallnummer());
      builder.append("-");
      builder.append(pathoReport.getAuftragnummer());
      builder.append("-");
      builder.append(identType.name());
    }

    if (args != null && args.length > 0) builder.append(String.join("-", args));

    return builder.toString();
  }

  public static Identifier getIdentifier(
      PathoReport pathoReport, PathologyIdentifierType identType, String system, String... args) {
    return new Identifier()
        .setSystem(system)
        .setValue(getPathoIdentifierValue(pathoReport, identType, args));
  }

  public static Identifier getIdentifier(
      PathoReport pathoReport, PathologyIdentifierType identType, String system) {
    return new Identifier()
        .setSystem(system)
        .setValue(getPathoIdentifierValue(pathoReport, identType, null));
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
