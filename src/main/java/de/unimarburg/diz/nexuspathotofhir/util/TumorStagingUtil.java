/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.util;

public class TumorStagingUtil {

  /**
   * T = Größe und Ausdehnung des Primärtumors: Tx: keine Messung des Primärtumors möglich T0:
   * Fehlen des Primärtumors T1–T4: Zuordnung nach Tumorart unter Berücksichtigung von Kriterien wie
   * Größe, Invasionstiefe und Infiltration benachbarter Gewebe und Organe
   *
   * @param staging
   */
  public void getT_grading(String staging) {}

  /**
   * N = Lymphknotenbefall: Nx: keine Beurteilung von benachbarten Lymphknoten möglich N0: keine
   * Beteiligung benachbarter Lymphknoten N1–N3: Anzahl und Lokalisation von Lymphknoten mit
   * Tumorzellen
   *
   * @param staging
   */
  public void getN_Grading(String staging) {}

  /**
   * M = Metastasierungsstatus: Mx: keine Beurteilung von Fernmetastasen möglich M0: keine
   * Metastasen M1: Fernmetastasen
   *
   * @param staging
   */
  public static Boolean getM_Grading(String staging) {
    if (staging == null) return null;
    var metastasierung = ".*M[0x1]$";

    return staging.matches(metastasierung);
  }
}
