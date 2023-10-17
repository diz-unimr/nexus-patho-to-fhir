/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;

class MicroscopicGrouperMapperTest<T extends ToFhirMapper> extends BasePathoGrouperMapperTest<T> {

  public MicroscopicGrouperMapperTest() {
    //noinspection unchecked
    super((Class<T>) MicroscopicGrouperMapper.class);
  }

  @Override
  public void setBaseIdentifierType() {
    baseIdentifierType = PathologyIdentifierType.MICROSCOPIC_GROUPER;
  }
}
