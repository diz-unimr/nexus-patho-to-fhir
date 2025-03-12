/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

class MicroscopicFindingMapperTest<T extends ToFhirMapper> extends BasePathoFindingMapperTest<T> {

  public MicroscopicFindingMapperTest() {
    //noinspection unchecked
    super((Class<T>) MicroscopicGrouperMapper.class);
  }
}
