/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

class MacroscopicFindingMapperTest<T extends ToFhirMapper> extends BasePathoFindingMapperTest<T> {

  public MacroscopicFindingMapperTest() {
    //noinspection unchecked
    super((Class<T>) MacroscopicGrouperMapper.class);
  }
}
