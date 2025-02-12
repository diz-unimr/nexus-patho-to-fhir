package de.unimarburg.diz.nexuspathotofhir.util;

import de.unimarburg.diz.nexuspathotofhir.model.ReportDocType;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;

public class DecideStatusOfBefund {

    public static void setFindingStatus(Observation pathoFinding, ReportDocType befundArt) {

        switch (befundArt) {
            case MAIN_REPORT:
            {
                pathoFinding.setStatus(Observation.ObservationStatus.FINAL);
                break;
            }
            case CORRECTION1:
            case CORRECTION2:
            case CORRECTION3:
            case CORRECTION4:
            case CORRECTION5:
            case CORRECTION6:
            {
                pathoFinding.setStatus(Observation.ObservationStatus.CORRECTED);
                break;
            }

            case ADDITION1:
            case ADDITION2:
            case ADDITION3:
            {
                pathoFinding.setStatus(Observation.ObservationStatus.AMENDED);
                break;
            }
            default:
                // fixme Zusatzbericht?
                pathoFinding.setStatus(Observation.ObservationStatus.UNKNOWN);
        }
    }


}
