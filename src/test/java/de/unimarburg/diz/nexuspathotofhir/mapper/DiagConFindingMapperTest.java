/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

public class DiagConFindingMapperTest<T extends ToFhirMapper>
    extends BasePathoFindingMapperTest<T> {

  public DiagConFindingMapperTest() {
    //noinspection unchecked
    super((Class<T>) PathoFindingDiagConclusionMapper.class);
  }
}
