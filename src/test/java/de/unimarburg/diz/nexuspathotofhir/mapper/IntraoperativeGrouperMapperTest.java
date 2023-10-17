/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import de.unimarburg.diz.nexuspathotofhir.util.PathologyIdentifierType;

class IntraoperativeGrouperMapperTest<T extends ToFhirMapper>
    extends BasePathoGrouperMapperTest<T> {

  public IntraoperativeGrouperMapperTest() {
    //noinspection unchecked
    super((Class<T>) IntraoperativeGrouperMapper.class);
  }

  @Override
  public void setBaseIdentifierType() {
    baseIdentifierType = PathologyIdentifierType.INTERAOPERATIVE_GROUPER;
  }
}
