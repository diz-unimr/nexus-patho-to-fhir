/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class PathoSpecimen implements PathoInputBase {

  @JsonProperty("ContainerGUID")
  private String containerGUID;

  @JsonProperty("ContainerID")
  private String containerID;

  @JsonProperty("ContainerName")
  private String containerName;

  @JsonProperty("ProbeID")
  private String probeID;

  @JsonProperty("ProbeLaenge")
  private String probeLaenge;

  @JsonProperty("ParentContainer")
  private String parentContainer;

  @JsonProperty("Auftragsnummer")
  private String auftragsnummer;

  /**
   * @apiNote value '-1' -> unknown size fixme: check unit - should be 'cm'
   */
  @JsonProperty("Probemenge")
  private Long probemenge;

  @JsonProperty("ProbeName")
  private String probeName;

  @JsonProperty("ProbeEinnahmedatum")
  private Long probeEinnahmedatum;

  @JsonProperty("ProbeGewinningsmethode")
  private String probeGewinningsmethode;

  @JsonProperty("Eingangsdatum")
  private Long eingangsdatum;

  @JsonProperty("Patientennummer")
  private String patientennummer;

  @JsonProperty("Fallnummer")
  private String fallnummer;

  /**
   * @apiNote it is a UUID
   */
  @JsonIgnore
  public String getUUID() {
    return getContainerGUID();
  }

  @Override
  @JsonIgnore
  public boolean isBaseValid() {
    return StringUtils.hasText(auftragsnummer)
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

  //  /**
  //   * @apiNote can be cast to Date since time component is empty
  //   */
  //  @JsonProperty("collection_date")
  //  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMddHHmm")
  //  private LocalDateTime specimenCollectionDate;
  //
  //  /**
  //   * @apiNote it is a UUID
  //   */
  //  @JsonProperty("Container")
  //  private String container;
  //
  @JsonProperty("ContainerTyp")
  private Integer containerType;
}
