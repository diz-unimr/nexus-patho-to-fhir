/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class PathoReport implements PathoInputBase {

  /** incremental number for this object instance to allow order */


  @JsonProperty("BefundID")
  private String befundID;
  /**
   * is fixes for one diagnostic report episode (including report corrections and additions)
   *
   * @implNote also called 'Journal Nummer' in german
   */
  // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
  // private ZonedDateTime eingangsdatum;
  @JsonProperty ("BefundErstellungsdatum")
  private Long befundErstellungsdatum;

    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    // private ZonedDateTime eingangsdatum;
    /**
     * last date time when report content has been modified
     *
     * <p>note that ou need consider {@link #getAuftragsnummer()}
     */
  @JsonProperty ("LetzteBearbeitungsdatum")
  private Long LetzteBearbeitungsdatum;

  @JsonProperty("Auftragsnummer")
  private String auftragsnummer;

  /** has multiple report instances and service requests */
  @JsonProperty("Patientennummer")
  private String patientennummer;

  /** Encounter number may be used over multiple diagnostic report instances */
  @JsonProperty("Fallnummer")
  private String fallnummer;

  /**
   * document type is important for processing logic: initial content, update/replace
   *
   * @apiNote use {@link #getDocType()} instead
   */
  @JsonProperty("Befundtyp")
  private String befundtyp;

  /** Date time when DiagnosticReport has been initially created */
  @JsonProperty("befunddatum")
  @JsonSerialize(using = InstantSerializer.class)
  private Instant befunddatum;


  @JsonProperty("ProbeEinnahmedatum")
  private Long probeEinnahmedatum;


    @JsonProperty("MikroskopischerBefund")
  private String mikroskopischerBefund;

  @JsonProperty("MakroskopischerBefund")
  private String makroskopischerBefund;

  /** cancer icd-o diagnose code */
  @JsonProperty("DiagnoseConclusionBefund")
  private String diagnoseConclusion;

  /** tumor classification */
  @JsonProperty("TNM")
  private String tnm;

    @JsonProperty("ProbeID")
    private String probeID;

    @JsonProperty("Probename")
  private String probeName;

    @JsonProperty("AuftragsgeberFAB")
    private String auftragsgeberFAB;

  @JsonIgnore
  public ReportDocType getDocType() throws IllegalStateException {

    ReportDocType result = ReportDocType.UNKNOWN;
    if (StringUtils.hasText(getBefundtyp())) {
      var trimmed = getBefundtyp().trim();
      switch (trimmed) {
          // fixme: genau prüfen was da so kommen kann, ggf. mit regex arbeiten
        case "Hauptbefund":
          {
            result = ReportDocType.MAIN_REPORT;
            break;
          }
        case "Korrekturbericht 1":
          {
            result = ReportDocType.CORRECTION1;
            break;
          }
        case "Korrekturbericht 2":
          {
            result = ReportDocType.CORRECTION2;
            break;
          }
        case "Zusatzbefund 1":
          {
            result = ReportDocType.ADDITION1;
            break;
          }
        default:
          return ReportDocType.UNKNOWN;
      }
    }

    if (result == ReportDocType.UNKNOWN)
      throw new IllegalStateException(
          String.format(
              "report id %s has an unexpected value at property 'Dokumentart': {%s}",
              this.befundID, this.getBefundtyp()));
    return result;
  }

  @JsonIgnore
  public String getUUID() {
    return getBefundID();
  }

  @Override
  @JsonIgnore
  public boolean isBaseValid() {
    return StringUtils.hasText(auftragsnummer)
        && StringUtils.hasText(fallnummer)
        && StringUtils.hasText(patientennummer);
  }
}
