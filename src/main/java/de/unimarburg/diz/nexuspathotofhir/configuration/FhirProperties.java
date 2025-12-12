/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(value = "fhir")
@Data
public class FhirProperties {

  @NestedConfigurationProperty private FhirSystems systems = new FhirSystems();
  @NestedConfigurationProperty private Profiles profiles = new Profiles();

  @Data
  public static class FhirSystems {
    private String serviceRequestId;
    private String diagnosticReportId;
    private String observationId;
    private String organizationId;
    private String patientId;
    private String encounterId;
    private String specimenId;
    private String specimenRequestId;
    private String specimenContainer;
    private String diagnosticFindingId;
    private String performerId;

    @Override
    public String toString() {
      return "FhirSystems{"
          + "serviceRequestId='"
          + serviceRequestId
          + '\''
          + ", diagnosticReportId='"
          + diagnosticReportId
          + '\''
          + ", observationId='"
          + observationId
          + '\''
          + ", patientId='"
          + patientId
          + '\''
          + ", encounterId='"
          + encounterId
          + '\''
          + ", organizationId='"
          + organizationId
          + '\''
          + ", specimenId='"
          + specimenId
          + '\''
          + ", specimenRequestId='"
          + specimenRequestId
          + '\''
          + ", specimenContainer='"
          + specimenContainer
          + '\''
          + ", diagnosticFindingId='"
          + diagnosticFindingId
          + '\''
          + performerId
          + '\''
          + '}';
    }
  }

  @Data
  public static class Profiles {
    private String specimenProfilePatho;
    private String specimenProfileBioBank;

    @Override
    public String toString() {
      return "Profiles{"
          + "specimenProfilePatho='"
          + specimenProfilePatho
          + '\''
          + ", specimenProfileBioBank='"
          + specimenProfileBioBank
          + '\''
          + '}';
    }
  }

  @Override
  public String toString() {
    return "FhirProperties{" + "systems=" + getSystems() + "profiles" + getProfiles() + '}';
  }
}
