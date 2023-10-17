/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.util;

public enum PathologyIdentifierType {

  /** used once per report id */
  DIAGNOSTIC_REPORT,
  /** used once */
  MICROSCOPIC_GROUPER,
  /** used once per report id */
  MACROSCOPIC_GROUPER,

  /** used once per report id */
  DIAGNOSTIC_CONCLUSION_GROUPER,

  /** used once per report id */
  ADDITIONAL_SPECIFIED_GROUPER,

  /** used once per report id */
  INTERAOPERATIVE_GROUPER,

  /**
   * used multiple times per grouper
   *
   * @implNote additional generation algorithm needed based on grouper and content (@fixme update
   *     and delete for npl needs cleanup process)
   */
  FINDING,

  /**
   * used multiple instances per SERVICE_REQUEST
   *
   * @apiNote has unique probe_id guid, reference to encounter, patient and service request also an
   *     unique container guid is provided
   */
  SPECIMEN,

  /**
   * Association to an encounter but not unique to an encounter
   *
   * @apiNote has unique patient guid
   */
  SERVICE_REQUEST,

  /**
   * Can have multiple reports and service requests
   *
   * @apiNote there is a patient GUID within report data which is unique to a service request
   */
  PATIENT
}
