/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.util;

import static org.assertj.core.api.Assertions.assertThat;

import de.unimarburg.diz.nexuspathotofhir.model.PathoReport;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import java.time.*;
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
    report.setMikroskopischerBefund("dummy Mikro");
    report.setMakroskopischerBefund("dummy Makro");
    report.setDiagnoseConclusion("C41.1");
    // Probe
    report.setProbeName("Magen PE");
    report.setProbeID("agfag");
    long currentTimeMillis = System.currentTimeMillis();
    report.setProbeEntnahmedatum(currentTimeMillis);
    report.setAuftragsgeberFABCode("KAR");
    return report;
  }

  public static PathoReport getDummyReportOtherType() {
    var report = new PathoReport();
    report.setBefundID(UUID.randomUUID().toString());
    report.setBefundErstellungsdatum(1719565355113L);
    report.setLetzteBearbeitungsdatum(1719565355113L);
    // report meta
    report.setAuftragsnummer("H/20223/00001");
    // patient
    report.setPatientennummer("0000001");
    report.setFallnummer("5000001");
    report.setBefundtyp("Zusatzbefund 1");
    // report clinical data
    report.setTnm("pT1-3c N2a M1");
    report.setMikroskopischerBefund("dummy1");
    report.setMakroskopischerBefund("dummy2");
    report.setDiagnoseConclusion("C41.1");
    // Probe
    report.setProbeName("Magen PE");
    report.setProbeID("agfag");
    long currentTimeMillis = System.currentTimeMillis();
    report.setProbeEntnahmedatum(currentTimeMillis);
    report.setAuftragsgeberFABCode("KAR");
    return report;
  }

  public static PathoSpecimen getDummySpecimen() {
    return getDummySpecimen(false);
  }

  public static PathoSpecimen getDummySpecimen(boolean doubleAdditiv) {
    // double additiv is a rare use case - usually only one additiv is applied
    final String containerNamesDoubleAdditiv = "1, 1.1-1-p 40, 1.1, 1.1-3, 1.1-2-HE|AFP";
    final String containerNamesSingleAdditiv = "1, 1.1-1-p 40, 1.1, 1.1-3, 1.1-2-HE";

    final PathoSpecimen pathoSpecimen = new PathoSpecimen();
    pathoSpecimen.setProbeID("specimen-dummy-id");
    pathoSpecimen.setPatientennummer("0000001");
    pathoSpecimen.setFallnummer("5000001");
    pathoSpecimen.setAuftragsnummer("H/20223/00001");
    pathoSpecimen.setProbeID("dummy-container-id");
    pathoSpecimen.setProbeGewinnungsmethode("Biopsien");
    pathoSpecimen.setProbeName("Lunge PE");
    pathoSpecimen.setOrgan("Lunge");
    pathoSpecimen.setProbeEntnahmedatum(
        LocalDateTime.of(2023, 1, 2, 20, 1, 33).toEpochSecond(ZoneOffset.UTC));
    final String containerParents =
        "NA,fdlkhj3459+0346uß, 359027354+dsgkjhdgf,fdlkhj3459+0346uß,fdlkhj3459+0346uß";
    pathoSpecimen.setContainerParents(containerParents);
    pathoSpecimen.setContainerGUIDs(
        "359027354+dsgkjhdgf,3240349f+dgdfsg,fdlkhj3459+0346uß,235264dsfdsgdfsh+3,ldhas345636jl");
    pathoSpecimen.setContainerNames(
        doubleAdditiv ? containerNamesDoubleAdditiv : containerNamesSingleAdditiv);
    pathoSpecimen.setContainerLabels(
        "specimen-dummy-id, specimen-dummy-id-1-1-1, specimen-dummy-id-1-1, specimen-dummy-id-1-1-3, specimen-dummy-id-1-1-2");
    pathoSpecimen.setContainerTyps("3, 2, 1, 2, 2");

    return pathoSpecimen;
  }

  public static PathoSpecimen getRandomDummySpecimen() {
    // double additiv is a rare use case - usually only one additiv is applied

    final String containerNamesSingleAdditiv = "1, 1.1-1-p 40, 1.1, 1.1-3, 1.1-2-HE";

    final PathoSpecimen pathoSpecimen = new PathoSpecimen();
    pathoSpecimen.setProbeID("specimen-dummy-id");
    pathoSpecimen.setPatientennummer("0000001");
    pathoSpecimen.setFallnummer("5000001");
    pathoSpecimen.setAuftragsnummer("H/20223/00001");

    pathoSpecimen.setProbeGewinnungsmethode("Biopsien");
    pathoSpecimen.setProbeName("Lunge PE");
    pathoSpecimen.setOrgan("Lunge");
    pathoSpecimen.setProbeEntnahmedatum(
        LocalDateTime.of(2023, 1, 2, 20, 1, 33).toEpochSecond(ZoneOffset.UTC));
    final String containerParents =
        "NA,fdlkhj3459+0346uß, 359027354+dsgkjhdgf,fdlkhj3459+0346uß,fdlkhj3459+0346uß";
    pathoSpecimen.setContainerParents(containerParents);
    pathoSpecimen.setContainerGUIDs(
        "359027354+dsgkjhdgf,3240349f+dgdfsg,fdlkhj3459+0346uß,235264dsfdsgdfsh+3,ldhas345636jl");
    pathoSpecimen.setContainerNames(containerNamesSingleAdditiv);
    pathoSpecimen.setContainerLabels(
        "specimen-dummy-id, specimen-dummy-id-1-1-1, specimen-dummy-id-1-1, specimen-dummy-id-1-1-3, specimen-dummy-id-1-1-2");
    pathoSpecimen.setContainerTyps("3, 2, 1, 2, 2");

    return pathoSpecimen;
  }

  @Test
  public void check() {
    assertThat(getDummyReport()).isNotNull();
    assertThat(getDummySpecimen()).isNotNull();
  }
}
