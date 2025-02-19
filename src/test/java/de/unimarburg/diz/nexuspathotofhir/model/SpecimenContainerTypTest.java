/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class SpecimenContainerTypTest {
  @Test
  public void initNewTypeInt() {
    var s1 = SpecimenContainerTyp.valueOf(1);
    assertThat(s1).isEqualTo(SpecimenContainerTyp.SUB_CONTAINER);
    var s2 = SpecimenContainerTyp.valueOf(2);
    assertThat(s2).isEqualTo(SpecimenContainerTyp.MICROSCOPE_SLIDE);
    var s3 = SpecimenContainerTyp.valueOf(3);
    assertThat(s3).isEqualTo(SpecimenContainerTyp.ROOT_CONTAINER);
  }
}
