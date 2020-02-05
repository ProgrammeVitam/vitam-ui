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
@CucumberOptions(features = "src/test/resources/features/front/user", glue = {
        "fr.gouv.vitamui.cucumber.front.steps.user",
        "fr.gouv.vitamui.cucumber.front.steps.common"},
         monochrome = true)

public class UiUserIntegrationTest {

}
