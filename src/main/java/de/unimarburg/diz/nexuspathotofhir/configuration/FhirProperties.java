package de.unimarburg.diz.nexuspathotofhir.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(value = "fhir")
@Data
public class FhirProperties {

    @NestedConfigurationProperty
    private FhirSystems systems = new FhirSystems();
    private String specimen;

    @Data
    public static class FhirSystems {
        private String serviceRequestId;
        private String diagnosticReportId;
        private String observationId;
        private String patientId;
        private String encounterId;
        private String assignerId;
        private String assignerCode;
        private String specimenRequestId;

        @Override
        public String toString() {
            return "FhirSystems{" +
                    "serviceRequestId='" + serviceRequestId + '\'' +
                    ", diagnosticReportId='" + diagnosticReportId + '\'' +
                    ", observationId='" + observationId + '\'' +
                    ", patientId='" + patientId + '\'' +
                    ", encounterId='" + encounterId + '\'' +
                    ", assignerId='" + assignerId + '\'' +
                    ", assignerCode='" + assignerCode + '\'' +
                    ", specimenRequestId='" + specimenRequestId + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "FhirProperties{" +
                "systems=" + getSystems() +
                '}';
    }
}
