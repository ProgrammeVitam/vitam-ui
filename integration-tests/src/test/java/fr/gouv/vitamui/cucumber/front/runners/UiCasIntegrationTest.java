package fr.gouv.vitamui.cucumber.front.runners;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;

/**
 * Test UI Portal.
 *
 *
 */

@RunWith(CucumberWithSerenity.class)
//@formatter:off
@CucumberOptions(
        features = "src/test/resources/features/front/cas",
        glue = {
        "fr.gouv.vitamui.cucumber.front.steps.cas",
        "fr.gouv.vitamui.cucumber.front.steps.portal",
        "fr.gouv.vitamui.cucumber.front.steps.common" },
        monochrome = true)
//@formatter:on
public class UiCasIntegrationTest {

}
