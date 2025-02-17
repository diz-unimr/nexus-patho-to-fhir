/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class PathoReport implements PathoInputBase {

  /** incremental number for this object instance to allow order */
  @JsonProperty("befundID")
  private String befundID;

  /**
   * is fixes for one diagnostic report episode (including report corrections and additions)
   *
   * @implNote also called 'Journal Nummer' in german
   */
  @JsonProperty("befundErstellungsdatum")
  private Long befundErstellungsdatum;

  // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
  // private ZonedDateTime eingangsdatum;
  /**
   * last date time when report content has been modified
   *
   * <p>note that ou need consider {@link #getAuftragsnummer()}
   */
  @JsonProperty("letzteBearbeitungsdatum")
  private Long letzteBearbeitungsdatum;

  @JsonProperty("auftragsnummer")
  private String auftragsnummer;

  /** has multiple report instances and service requests */
  @JsonProperty("patientennummer")
  private String patientennummer;

  /** Encounter number may be used over multiple diagnostic report instances */
  @JsonProperty("fallnummer")
  private String fallnummer;

  /**
   * document type is important for processing logic: initial content, update/replace
   *
   * @apiNote use {@link #getDocType()} instead
   */
  @JsonProperty("befundtyp")
  private String befundtyp;

  @Nullable @JsonProperty("mikroskopischerBefund")
  private String mikroskopischerBefund;

  @Nullable @JsonProperty("makroskopischerBefund")
  private String makroskopischerBefund;

  /** cancer icd-o diagnose code */
  @Nullable @JsonProperty("diagnoseConclusionBefund")
  private String diagnoseConclusion;

  /** tumor classification */
  @Nullable @JsonProperty("tNM")
  private String tnm;

  @Nullable @JsonProperty("sonstigesBefund")
  private String sonstigesBefund;

  @JsonProperty("probename")
  private String probeName;

  @JsonProperty("probeID")
  private String probeID;

  @JsonProperty("probeEntnahmedatum")
  private Long probeEntnahmedatum;

  @JsonProperty("auftragsgeberFABCode")
  private String auftragsgeberFABCode;

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
        case "Korrekturbericht 3":
          {
            result = ReportDocType.CORRECTION3;
            break;
          }
        case "Korrekturbericht 4":
          {
            result = ReportDocType.CORRECTION4;
            break;
          }

        case "Korrekturbericht 5":
          {
            result = ReportDocType.CORRECTION5;
            break;
          }
        case "Zusatzbefund 1":
          {
            result = ReportDocType.ADDITION1;
            break;
          }
        case "Zusatzbefund 2":
          {
            result = ReportDocType.ADDITION2;
            break;
          }
        case "Zusatzbefund 3":
          {
            result = ReportDocType.ADDITION3;
            break;
          }
        case "Zusatzbefund 4":
          {
            result = ReportDocType.ADDITION4;
            break;
          }
        case "Zusatzbefund 5":
          {
            result = ReportDocType.ADDITION5;
            break;
          }
        case "Zusatzbefund 6":
          {
            result = ReportDocType.ADDITION6;
            break;
          }
        case "Zusatzbefund 7":
          {
            result = ReportDocType.ADDITION7;
            break;
          }
        case "Zusatzbefund 8":
          {
            result = ReportDocType.ADDITION8;
            break;
          }
        case "Zusatzbefund 9":
          {
            result = ReportDocType.ADDITION9;
            break;
          }
        case "Zusatzbefund 10":
          {
            result = ReportDocType.ADDITION10;
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
