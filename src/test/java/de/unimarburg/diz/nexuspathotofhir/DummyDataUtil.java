/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir;

import static org.assertj.core.api.Assertions.assertThat;

import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class DummyDataUtil {

  public static PathoReport getDummyReport() {
    ZoneId zoneId = ZoneId.of("UTC+1");

    var report = new PathoReport();
    // meta data
    report.setIndexID(1);

    // patient
    report.setPatientGUID(UUID.randomUUID().toString());
    report.setPatientennummer("0000001");
    report.setFallnummer("5000001");
    report.setNachname("Dummy_Surname");
    report.setVorname("Dummy_Firstname");
    report.setGeschlecht("1");
    report.setGeburtsdatum(LocalDate.of(1973, 2, 4));
    report.setIstVerstorben(1);
    report.setSterbedatum(ZonedDateTime.of(2023, 7, 7, 11, 15, 19, 1234, zoneId).toInstant());

    // report meta
    report.setAuftragnummer("H/20223/00001");
    report.setEingangsdatum(LocalDateTime.of(2023, 1, 2, 20, 1, 33).toEpochSecond(ZoneOffset.UTC));
    report.setPathologieBefundId(UUID.randomUUID().toString());
    report.setBefunddatum(ZonedDateTime.of(2023, 1, 20, 23, 45, 59, 1234, zoneId).toInstant());
    report.setLetzteBearbeitung(report.getBefunddatum().plus(14, ChronoUnit.DAYS));

    // report clinical data
    report.setTNM("pT1-3c N2a M1");
    report.setMaterial_Lokalisation("L");
    report.setBefundXML(
        """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <item>TODO</item>
                """);
    report.setMikroskopischer_Befund("dummy1");
    report.setMakroskopischer_Befund("dummy2");
    report.setDiagnose("C41.1");
    report.setDocumentart("Hauptbefund");
    report.setMaterial_Lokalisation("Brust L");

    return report;
  }

  /**
   * @return chronologically ordered reports: main, correction 1, correction 2, addition 1
   */
  public static List<PathoReport> getFourReportVariants() {

    PathoReport mainReport = getDummyReport();

    PathoReport reportCorrection1 = getDummyReport();
    reportCorrection1.setDocumentart("Korrekturbericht 1");
    reportCorrection1.setIndexID(mainReport.getIndexID() + 1);
    reportCorrection1.setBefunddatum(mainReport.getBefunddatum().plus(2, ChronoUnit.DAYS));

    PathoReport reportCorrection2 = getDummyReport();
    reportCorrection2.setDocumentart("Korrekturbericht 2");
    reportCorrection2.setIndexID(reportCorrection1.getIndexID() + 1);
    reportCorrection2.setBefunddatum(reportCorrection1.getBefunddatum().plus(3, ChronoUnit.DAYS));

    PathoReport reportAddition = getDummyReport();
    reportAddition.setDocumentart("Zusatzbefund 1");
    reportAddition.setIndexID(reportCorrection2.getIndexID() + 1);
    reportAddition.setBefunddatum(reportCorrection2.getBefunddatum().plus(7, ChronoUnit.DAYS));

    return List.of(mainReport, reportCorrection1, reportCorrection2, reportAddition);
  }

  @Test
  public void check() {
    assertThat(getDummyReport()).isNotNull();
  }
}
