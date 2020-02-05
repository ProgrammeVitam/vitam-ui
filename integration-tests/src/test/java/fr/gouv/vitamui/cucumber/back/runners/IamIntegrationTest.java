package fr.gouv.vitamui.cucumber.back.runners;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import fr.gouv.vitamui.AbstractIntegrationTest;
import net.serenitybdd.cucumber.CucumberWithSerenity;

@RunWith(CucumberWithSerenity.class)
//@formatter:off
@CucumberOptions(
        features = "src/test/resources/features/back/iam",
//        tags = "@Traces",
        glue = { "fr.gouv.vitamui.cucumber.back.steps.iam", "fr.gouv.vitamui.cucumber.back.steps.common" },
        monochrome = true
        )
//@formatter:on
public class IamIntegrationTest extends AbstractIntegrationTest {

    @BeforeClass
    public static void startServersAndSetData() {

    }

    @AfterClass
    public static void someStuff() {

    }

}
