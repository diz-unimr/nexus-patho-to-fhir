/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.model;

import java.util.Arrays;
import java.util.Optional;

public enum SpecimenContainerTyp {
  SUB_CONTAINER(1),
  MICROSCOPE_SLIDE(2),
  ROOT_CONTAINER(3);

  private final int value;

  SpecimenContainerTyp(int value) {
    this.value = value;
  }

  public static SpecimenContainerTyp valueOf(int value) {
    final Optional<SpecimenContainerTyp> first =
        Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    if (first.isPresent()) return first.get();
    else {
      throw new IllegalArgumentException(
          "container typ %s is not supported, yet.".formatted(value));
    }
  }
}
