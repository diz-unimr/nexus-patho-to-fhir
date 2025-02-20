/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class PathoSpecimen implements PathoInputBase {

  /**
   * Pathology id for root specimen
   *
   * <p>e.g. "H20250201S1"
   */
  @JsonProperty("ProbeID")
  private String probeID;

  /**
   * specimen guid list assigned with specimen (ordered list)
   *
   * <p>e.g.: 359027354+dsgkjhdgf,fdlkhj3459+0346uß,3240349f+dgdfsg,235264dsfdsgdfsh+36
   */
  @JsonProperty("ContainerGUIDs")
  private String containerGUIDs;

  /**
   * specimen label list assigned with specimen (ordered list)
   *
   * <p>e.g.: H20250201S1,H20250201S1-1.1, H20250201S1-1.1-1,H20250201S1-1.1-2
   */
  @JsonProperty("ContainerLabels")
  private String containerLabels;

  /**
   * specimen name list assigned with specimen IMPORTANT: here we have applied postprocessing codes
   * as suffix (ordered list)
   *
   * <p>note: '|' is separator in case of multiple postprocessing
   *
   * <p>e.g.: 1,1.1, 1.1-1-FE,1.1-2-HE|AFP
   */
  @JsonProperty("ContainerNames")
  private String containerNames;

  @JsonIgnore
  public int getRootIndex() {
    if (containerTypes == null) return -1;
    final String[] split = containerTypes.split(",");
    var rootIndex = Arrays.stream(split).toList().indexOf("3");
    return rootIndex;
  }

  /**
   * Container storage types which are currently used.
   *
   * <p>e.g. 1,2,2,3
   * <li>3 -> root container (virtual)
   * <li>2 -> microscopic slide
   * <li>1 -> sub container from which slides are created
   */
  @JsonProperty("containerTypes")
  private String containerTypes;

  /**
   * specimen guid list of parent container guids. Root element has no parent therefore it has value
   * 'NA' (ordered list)
   *
   * <p>e.g.: NA,359027354+dsgkjhdgf,fdlkhj3459+0346uß,fdlkhj3459+0346uß
   */
  @JsonProperty("containerParents")
  private String containerParents;

  /**
   * pathology service number
   *
   * <p>e.g. H/2025/1234567
   */
  @JsonProperty("auftragsnummer")
  private String auftragsnummer;

  /**
   * encounter number
   *
   * <p>e.g. 123454567
   */
  @JsonProperty("fallnummer")
  private String fallnummer;

  /** Date of specimen extraction */
  @JsonProperty("probeEntnahmedatum")
  private Long probeEntnahmedatum;

  /**
   * patient number
   *
   * <p>12345678
   */
  @JsonProperty("patientennummer")
  private String patientennummer;

  /**
   * Specimen type/name
   *
   * <p>e.g. Lunge PE
   */
  @JsonProperty("probeName")
  private String probeName;

  /**
   * local organ name of which specimen has been extracted
   *
   * <p>if available
   *
   * <p>e.g. 'Lung'
   */
  @JsonProperty("organ")
  private String organ;

  /**
   * Method of specimen extraction
   *
   * <p>e.g. Biopsie
   */
  @JsonProperty("probeGewinnungsmethode")
  private String probeGewinnungsmethode;

  /**
   * list of sub container names, created from this parent container
   *
   * @implNote comma delimiter list
   */
  @JsonIgnore private List<String> subContainerNames;

  /**
   * Färbungen der Proben
   *
   * @implNote key is container name; value list of postprocessing applied to specimen FIXME: fill
   *     map need implementation
   */
  @JsonIgnore private Map<String, List<String>> postPrecessing;

  /**
   * @implNote key is container name; value container type FIXME: fill map need implementation
   */
  @JsonIgnore private Map<String, Integer> subContainerTypes;

  @Override
  @JsonIgnore
  public boolean isBaseValid() {
    return StringUtils.hasText(auftragsnummer)
        && StringUtils.hasText(fallnummer)
        && StringUtils.hasText(patientennummer);
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    if (!StringUtils.hasText(this.probeID))
      throw new RuntimeException("patho specimen without probeId!");
    return this.probeID;
  }

  @JsonIgnore
  public String[] getContainerLabelsArray() {
    if (this.getContainerLabels() == null) return new String[0];
    return Arrays.stream(this.getContainerLabels().split(","))
        .map(a -> a.trim())
        .toArray(String[]::new);
  }

  @JsonIgnore
  public String[] getContainerGUIDsArray() {
    if (this.getContainerGUIDs() == null) return new String[0];
    return Arrays.stream(this.getContainerGUIDs().split(","))
        .map(a -> a.trim())
        .toArray(String[]::new);
  }

  @JsonIgnore
  public String[] getSubContainerIdsArray() {
    if (this.getContainerParents() == null) return new String[0];
    return Arrays.stream(this.getContainerParents().split(","))
        .map(a -> a.trim())
        .filter(a -> !"NA".equals(a))
        .toArray(String[]::new);
  }

  @JsonIgnore
  public String[] getContainerTypesArray() {
    if (this.getContainerTypes() == null) return new String[0];
    return Arrays.stream(this.getContainerTypes().split(","))
        .map(a -> a.trim())
        .toArray(String[]::new);
  }
}
