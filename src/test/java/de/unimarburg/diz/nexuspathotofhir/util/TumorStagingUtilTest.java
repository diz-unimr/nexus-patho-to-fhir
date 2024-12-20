/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2024 */
package de.unimarburg.diz.nexuspathotofhir.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TumorStagingUtilTest {

  @Test
  void getM_Grading() {
    assertThat(TumorStagingUtil.getM_Grading("asb")).isFalse();
    assertThat(TumorStagingUtil.getM_Grading("asbM")).isFalse();
    assertThat(TumorStagingUtil.getM_Grading("asbM1")).isTrue();
  }
}
