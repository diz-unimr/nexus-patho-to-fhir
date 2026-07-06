/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class PathoSpecimenTest {
  @Test
  public void invalidSpecimenGetRootIndex() {
    var fix = new PathoSpecimen();
    assertThat(fix.getRootIndex()).isEqualTo(-1);
  }

  @Test
  public void invalidSpecimenGetArrays() {
    var fix = new PathoSpecimen();
    assertThat(fix.getContainerTypesArray()).isEmpty();
    assertThat(fix.getContainerLabelsArray()).isEmpty();
    assertThat(fix.getSubContainerIdsArray()).isEmpty();
    assertThat(fix.getContainerGUIDsArray()).isEmpty();
  }

  @Test
  public void specimenGetRootIndex() {
    var fix = new PathoSpecimen();
    fix.setContainerTyps("1, 1,1,2, 3, 2");
    assertThat(fix.getRootIndex()).isEqualTo(4);
  }
}
