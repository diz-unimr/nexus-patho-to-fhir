/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2023 */
package de.unimarburg.diz.nexuspathotofhir.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import de.unimarburg.diz.nexuspathotofhir.configuration.CsvMappings;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirConfiguration;
import de.unimarburg.diz.nexuspathotofhir.configuration.FhirProperties;
import de.unimarburg.diz.nexuspathotofhir.model.PathoSpecimen;
import de.unimarburg.diz.nexuspathotofhir.util.DummyDataUtilTest;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Specimen;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
      FhirConfiguration.class,
      FhirProperties.class,
      CsvMappings.class,
      SpecimenMapper.class,
    })
class SpecimenMapperTest extends FhirValidationBase {
  private static final Logger log = LoggerFactory.getLogger(SpecimenMapperTest.class);

  @Autowired SpecimenMapper fixture;

  @Test
  void emptyInput() {
    final PathoSpecimen value = new PathoSpecimen();
    var result = fixture.apply(value);
    assertThat(result).isNull();
  }

  @Test
  void isFhirProfileValid() {

    validator.withResourcesFrom(
        "node_modules//de.medizininformatikinitiative.kerndatensatz.biobank", "*.json");
    validator.withResourcesFrom(
        "node_modules//de.medizininformatikinitiative.kerndatensatz.patho//StructureDefinition-mii-pr-patho-specimen.json");
    validator.withResourcesFrom(
        "node_modules//de.medizininformatikinitiative.kerndatensatz.patho//ValueSet-mii-vs-patho-container-type-snomed-ct.json");
    validator.withResourcesFrom(
        "node_modules//de.medizininformatikinitiative.kerndatensatz.patho//ValueSet-mii-vs-patho-collection-method-snomed-ct.json");
    validator.withResourcesFrom(
        "node_modules//de.medizininformatikinitiative.kerndatensatz.patho//ValueSet-mii-vs-patho-processing-procedure-snomed-ct.json");

    var input = DummyDataUtilTest.getDummySpecimen();
    var result = fixture.map(input);

    var validationResult = validator.validateWithResult(result);

    final List<SingleValidationMessage> validationErrors =
        validationResult.getMessages().stream()
            .filter(m -> m.getSeverity() == ResultSeverityEnum.ERROR)
            .toList();

    var filterValidationErrors =
        validationErrors.stream()
            .filter(
                a ->
                    !a.getMessage()
                        .contains(
                            "None of the codings provided are in the value set 'MII VS Patho Container Type [SNOMED CT]"))
            .toList();
    if (!filterValidationErrors.isEmpty()) {
      log.info("we have validation errors - mapped resource JSON representation:");
      log.info(fhirContext.newJsonParser().encodeResourceToString(result));
    }

    assertThat(filterValidationErrors)
        .as("profile validation error should not be present")
        .isEmpty();
  }

  @Test
  public void identifierStayStableForIdenticalInput() {
    var input = DummyDataUtil.getDummySpecimen();

    var result = fixture.map(input);
    var result2 = fixture.map(input);

    assertThat(result.getIdentifier().size()).as("specimen has only one identifier").isEqualTo(1);
    assertThat(result.getContainer())
        .as("all container components have only one identifier!")
        .allSatisfy(a -> assertThat(a.getIdentifier().size()).isEqualTo(1));

    assertThat(result.getIdentifierFirstRep().getValue())
        .as("Identifier should be created the same way for identical input")
        .isEqualTo(result2.getIdentifierFirstRep().getValue());
    assertThat(result.getContainer())
        .allSatisfy(
            a ->
                assertThat(result2.getContainer())
                    .as("Identifier should be created the same way for identical input")
                    .anySatisfy(
                        b ->
                            b.getIdentifierFirstRep()
                                .getValue()
                                .equals(a.getIdentifierFirstRep().getValue())));
  }

  @Test
  public void specimenSubContainerCountTest() {
    var input = DummyDataUtil.getDummySpecimen();

    var result = fixture.map(input);

    assertThat(result.getContainer().size()).isEqualTo(4);

    assertThat(result.getSubject()).isNotNull();
  }

  @Test
  public void referencesTest() {
    var input = DummyDataUtil.getDummySpecimen();
    var result = fixture.map(input);
    assertThat(result.getSubject()).isNotNull();
  }

  @Test
  void buildBundleComponent() {

    final Specimen mapped =
        new Specimen()
            .addIdentifier(
                new Identifier().setSystem("dummySystem").setValue("specimentIdentifierValue"));
    var result = fixture.buildBundleComponent(mapped, mapped.getIdentifierFirstRep());

    assertThat(result.getResource()).isEqualTo(mapped);
    assertThat(result.getRequest()).isNotNull();
    assertThat(result.getRequest().getMethod()).isEqualTo(Bundle.HTTPVerb.PUT);
    assertThat(result.getRequest().getUrl()).startsWith(mapped.fhirType());
    assertThat(result.getRequest().getUrl()).contains(mapped.getIdentifierFirstRep().getValue());
    assertThat(result.getRequest().getUrl()).contains(mapped.getIdentifierFirstRep().getSystem());
  }

  @Test
  void checkIsValidTest() {
    var input = DummyDataUtil.getDummySpecimen();
    assertThat(fixture.checkInputIsValid(input)).isTrue();
  }
}
