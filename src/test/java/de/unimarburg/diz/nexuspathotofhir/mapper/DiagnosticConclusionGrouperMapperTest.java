/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierResourceType;

class DiagnosticConclusionGrouperMapperTest<T extends ToFhirMapper>
    extends BasePathoGrouperMapperTest<T> {

  public DiagnosticConclusionGrouperMapperTest() {
    //noinspection unchecked
    super((Class<T>) DiagnosticConclusionGrouperMapper.class);
  }

  @Override
  public void setBaseIdentifierType() {
    super.baseIdentifierType = PathologyIdentifierResourceType.DIAGNOSTIC_CONCLUSION_GROUPER;
  }
}
