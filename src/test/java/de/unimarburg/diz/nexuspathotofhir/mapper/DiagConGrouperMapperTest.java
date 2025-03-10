/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierResourceType;

public class DiagConGrouperMapperTest<T extends ToFhirMapper>
    extends BasePathoGrouperMapperTest<T> {

  public DiagConGrouperMapperTest() {
    //noinspection unchecked
    super((Class<T>) DiagnosticConclusionGrouperMapper.class);
  }

  @Override
  public void setBaseIdentifierType() {
    super.baseIdentifierType = PathologyIdentifierResourceType.DIAGNOSTIC_CONCLUSION_GROUPER;
  }
}
