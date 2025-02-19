/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.util;

import static org.assertj.core.api.Assertions.assertThat;

import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import java.time.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class DummyDataUtilTest {

  public static PathoReport getDummyReport() {

    var report = new PathoReport();
    report.setBefundID(UUID.randomUUID().toString());
    report.setBefundErstellungsdatum(1719565355113L);
    report.setLetzteBearbeitungsdatum(1719565355113L);
    // report meta
    report.setAuftragsnummer("H/20223/00001");
    // patient
    report.setPatientennummer("0000001");
    report.setFallnummer("5000001");
    report.setBefundtyp("Hauptbefund");
    // report clinical data
    report.setTnm("pT1-3c N2a M1");
    report.setMikroskopischerBefund("dummy1");
    report.setMakroskopischerBefund("dummy2");
    report.setDiagnoseConclusion("C41.1");
    // Probe
    report.setProbeName("Magen PE");
    report.setProbeID("agfag");
    report.setProbeEntnahmedatum(1719565355113L);
    report.setAuftragsgeberFABCode("KAR");
    return report;
  }

  /**
   * @return chronologically ordered reports: main, correction 1, correction 2, addition 1
   */
  public static List<PathoReport> getFourReportVariants() {

    PathoReport mainReport = getDummyReport();

    PathoReport reportCorrection1 = getDummyReport();
    reportCorrection1.setBefundtyp("Korrekturbericht 1");
    reportCorrection1.setBefundErstellungsdatum(mainReport.getBefundErstellungsdatum());

    PathoReport reportCorrection2 = getDummyReport();
    reportCorrection2.setBefundtyp("Korrekturbericht 2");
    reportCorrection2.setBefundErstellungsdatum(mainReport.getBefundErstellungsdatum());

    PathoReport reportAddition = getDummyReport();
    reportAddition.setBefundtyp("Zusatzbefund 1");
    reportAddition.setBefundErstellungsdatum(mainReport.getBefundErstellungsdatum());

    return List.of(mainReport, reportCorrection1, reportCorrection2, reportAddition);
  }

  public static PathoSpecimen getDummySpecimen() {
    final PathoSpecimen pathoSpecimen = new PathoSpecimen();
    pathoSpecimen.setProbeID("specimen-dummy-id");
    pathoSpecimen.setPatientennummer("0000001");
    pathoSpecimen.setFallnummer("5000001");
    pathoSpecimen.setAuftragsnummer("H/20223/00001");
    pathoSpecimen.setContainerGUID("dummy-container-id");
    pathoSpecimen.setProbeName("Lunge PE");
    pathoSpecimen.setProbeLaenge("5L");
    pathoSpecimen.setProbeEinnahmedatum(
        LocalDateTime.of(2023, 1, 2, 20, 1, 33).toEpochSecond(ZoneOffset.UTC));
    pathoSpecimen.setProbemenge(1L);
    return pathoSpecimen;
  }

  @Test
  public void check() {
    assertThat(getDummyReport()).isNotNull();
    assertThat(getDummySpecimen()).isNotNull();
  }
}
