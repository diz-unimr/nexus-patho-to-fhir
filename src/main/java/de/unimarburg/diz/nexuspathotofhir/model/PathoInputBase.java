/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.model;

import java.io.Serializable;

public interface PathoInputBase extends Serializable {
  String getPatientennummer();

  String getFallnummer();

  String getAuftragsnummer();

  String getUUID();

  boolean isBaseValid();

}
