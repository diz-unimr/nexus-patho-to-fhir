/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class PathoReport implements Serializable {

  /** incremental number for this object instance to allow order */
  @JsonProperty("Index_ID")
  private Integer indexID;

  /** is fixes for one diagnostic report episode (including report corrections and additions) */
  @JsonProperty("Auftragnummer")
  private String auftragnummer;

  @JsonProperty("Eingangsdatum")
  // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
  // private ZonedDateTime eingangsdatum;
  private Long eingangsdatum;

  /** internal system identifier of patient @TODO: check if unique per report */
  @JsonProperty("Patient_GUID")
  private String patientGUID;

  /** has multiple report instances and service requests */
  @JsonProperty("Patientennummer")
  private String patientennummer;

  /** Encounter number may be used over multiple diagnostic report instances */
  @JsonProperty("Fallnummer")
  private String fallnummer;

  @JsonProperty("Nachname")
  private String nachname;

  @JsonProperty("Vorname")
  private String vorname;

  @JsonProperty("Geschlecht")
  private String geschlecht;

  @JsonProperty("Geburtsdatum")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
  private LocalDate geburtsdatum;

  @JsonProperty("IstVerstorben")
  private Integer istVerstorben;

  @JsonProperty("Sterbedatum")
  @JsonSerialize(using = InstantSerializer.class)
  private Instant sterbedatum;

  /** Unique UUID */
  @JsonProperty("Pathologie_Befund_Id")
  private String pathologieBefundId;

  /** document type is important for processing logic: initial content, update/replace */
  @JsonProperty("Documentart")
  private String documentart;

  /** Date time when DiagnosticReport has been initially created */
  @JsonProperty("befunddatum")
  @JsonSerialize(using = InstantSerializer.class)
  private Instant befunddatum;

  /**
   * last date time when report content has been modified
   *
   * <p>note that ou need consider {@link #getAuftragnummer()}
   */
  @JsonProperty("Letzte_Bearbeitung")
  @JsonSerialize(using = InstantSerializer.class)
  private Instant letzteBearbeitung;

  /** narrative */
  @JsonProperty("Befund_XML")
  private String befundXML;

  /**
   * subsection of {@link #getBefundXML()} if has value will need create a related macroscopic
   * grouper
   */
  @JsonProperty("Makroskopischer_Befund")
  private String makroskopischer_Befund;

  /**
   * subsection of {@link #getBefundXML()} if has value will need create a related microscopic
   * grouper
   */
  @JsonProperty("Mikroskopischer_Befund")
  private String mikroskopischer_Befund;

  /** cancer icd-o diagnose code */
  @JsonProperty("Diagnose")
  private String diagnose;

  /** specimen extraction location */
  @JsonProperty("Material_Lokalisation")
  private String material_Lokalisation;

  /** tumor classification */
  @JsonProperty("TNM")
  private String tNM;

  @JsonIgnore
  public ReportDocType getDocType() throws IllegalStateException {

    ReportDocType result = ReportDocType.UNKNOWN;
    if (StringUtils.hasText(getDocumentart())) {
      var trimmed = getDocumentart().trim();
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
              this.indexID, this.getDocumentart()));
    return result;
  }
}
