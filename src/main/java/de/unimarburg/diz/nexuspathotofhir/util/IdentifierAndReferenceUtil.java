/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.util;

import de.unimarburg.diz.nexuspathotofhir.model.PathoInputBase;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
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
      PathoInputBase inputBase, PathologyIdentifierType identType, String[] args) {
    if (identType == null) throw new IllegalArgumentException("identType was null");
    if (inputBase == null) throw new IllegalArgumentException("inputBase was null");
    if (!StringUtils.hasText(inputBase.getAuftragnummer()))
      throw new IllegalArgumentException("inputBase.Auftragnummer was null");

    var builder = new StringBuilder();

    if (identType == PathologyIdentifierType.PATIENT) {
      return inputBase.getPatientennummer();
    } else {
      builder.append(inputBase.getFallnummer());
      builder.append("-");
      builder.append(inputBase.getAuftragnummer());
      builder.append("-");
      builder.append(identType.name());
    }

    if (args != null && args.length > 0) builder.append(String.join("-", args));

    return builder.toString();
  }

  public static Identifier getIdentifier(
      PathoInputBase inputBase, PathologyIdentifierType identType, String system, String... args) {
    return new Identifier()
        .setSystem(system)
        .setValue(getPathoIdentifierValue(inputBase, identType, args));
  }

  public static Identifier getIdentifier(
      PathoInputBase inputBase, PathologyIdentifierType identType, String system) {
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
