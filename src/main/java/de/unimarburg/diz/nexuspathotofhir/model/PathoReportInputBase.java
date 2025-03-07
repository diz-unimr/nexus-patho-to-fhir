/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.model;

import java.io.Serializable;

public interface PathoReportInputBase extends Serializable {

  String getBefundID();

  String getPatientennummer();

  String getFallnummer();

  String getAuftragsnummer();

  boolean isBaseValid();

  String getUUID();

  String getBefundtyp();

  String getMikroskopischerBefund();

  String getMakroskopischerBefund();

  String getDiagnoseConclusion();
}
