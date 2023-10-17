/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;

class MacroscopicGrouperMapperTest<T extends ToFhirMapper> extends BasePathoGrouperMapperTest<T> {

  public MacroscopicGrouperMapperTest() {
    //noinspection unchecked
    super((Class<T>) MacroscopicGrouperMapper.class);
  }

  @Override
  public void setBaseIdentifierType() {
    super.baseIdentifierType = PathologyIdentifierType.MACROSCOPIC_GROUPER;
  }
}
