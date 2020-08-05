package fr.gouv.vitamui.cucumber.front.runners;

import org.junit.runner.RunWith;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;

/**
 * Test UI Portal.
 */

@RunWith(CucumberWithSerenity.class)
//@formatter:off
@CucumberOptions(
        plugin = { "pretty" },
        features = "src/test/resources/features/front/user",
        glue = {
            "fr.gouv.vitamui.cucumber.front.steps.user",
            "fr.gouv.vitamui.cucumber.front.steps.common",
            "fr.gouv.vitamui.cucumber.common"
        },
        monochrome = true
)
//@formatter:on
public class UiUserIntegrationTest {

}
