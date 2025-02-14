/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.util;

import de.unimarburg.diz.nexuspathotofhir.model.ReportDocType;
import org.hl7.fhir.r4.model.Observation;

public class DecideStatusOfBefund {

    public static void setFindingStatus(Observation pathoFinding, ReportDocType befundArt) {

        switch (befundArt) {
            case MAIN_REPORT: {
                pathoFinding.setStatus(Observation.ObservationStatus.FINAL);
                break;
            }
            case CORRECTION1:
            case CORRECTION2:
            case CORRECTION3:
            case CORRECTION4:
            case CORRECTION5:
            {
                pathoFinding.setStatus(Observation.ObservationStatus.CORRECTED);
                break;
            }
            case ADDITION1:
            case ADDITION2:
            case ADDITION3:
            case ADDITION4:
            case ADDITION5:
            case ADDITION6:
            case ADDITION7:
            case ADDITION8:
            case ADDITION9:
            case ADDITION10:
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
