/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class PathoSpecimen implements PathoInputBase {

  @JsonProperty("Auftragnummer")
  private String auftragnummer;

  @JsonProperty("Patientennummer")
  private String patientennummer;

  @JsonProperty("Fallnummer")
  private String fallnummer;

  /**
   * @apiNote it is a UUID
   */
  @JsonProperty("Probe_id")
  private String probeId;

  @JsonIgnore
  public String getUUID() {
    return getProbeId();
  }

  @Override
  @JsonIgnore
  public boolean isBaseValid() {
    return StringUtils.hasText(auftragnummer)
        && StringUtils.hasText(fallnummer)
        && StringUtils.hasText(patientennummer);
  }

  /**
   * @apiNote value has white space separator (german name, extraction method, side localization) -
   *     if
   *     <p>there are special names of unknown meaning:
   *     <ul>
   *       <li>BRAF (Idylla)
   *       <li>BRCA (nicht OvCa)
   *       <li>BRCA (OcCa)
   *       <li>EGFR (Idylla)
   *       <li>Fusion (Archer)
   *       <li>Fusion (Idylla)
   *       <li>KRAS (Idylla)
   *       <li>low coverage (Häma)
   *       <li>MSI (Idylla)
   *       <li>NGS BRCA (nicht OvCa)
   *       <li>NRAS/BRAF (Idylla)
   *     </ul>
   *     TODO: mapping german names to coding SNOMED CT
   */
  @JsonProperty("Probename")
  private String probename;

  /**
   * @apiNote can be cast to Date since time component is empty
   */
  @JsonProperty("collection_date")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMddHHmm")
  private LocalDateTime specimenCollectionDate;

  /**
   * @apiNote it is a UUID
   */
  @JsonProperty("Container")
  private String container;

  /**
   * @see {@link de.unimarburg.diz.nexuspathotofhir.configuration.PathoLookUps}
   */
  @JsonProperty("ContainerType")
  private Integer containerType;

  /**
   * @apiNote value '-1' -> unknown size fixme: check unit - should be 'cm'
   */
  @JsonProperty("Probe_Laenge")
  private Long probeLaenge;

  /** fixme: what does this mean and do we need it? */
  @JsonProperty("ItemOrder")
  private String itemOrder;

  /** fixme: what does value '-1' mean */
  @JsonProperty("Probemenge")
  private Long probemenge;
}
