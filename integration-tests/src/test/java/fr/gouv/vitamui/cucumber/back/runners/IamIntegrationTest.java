package fr.gouv.vitamui.cucumber.back.runners;

import fr.gouv.vitamui.AbstractIntegrationTest;
import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
//@formatter:off
@CucumberOptions(
        plugin = { "pretty", "json:target/cucumber-report.json", "html:target/html"},
        features = "src/test/resources/features/back/iam",
        //        tags = "@Traces",
        glue = {
            "fr.gouv.vitamui.cucumber.back.steps.iam",
            "fr.gouv.vitamui.cucumber.back.steps.common",
            "fr.gouv.vitamui.cucumber.common"
        },
        monochrome = true
)
//@formatter:on
public class IamIntegrationTest extends AbstractIntegrationTest {

    @BeforeClass
    public static void startServersAndSetData() {
        // add temporary profile to admin_group to be able to tets owner features
        //BaseIntegration.updateGroupAddProfile(BaseIntegration.ADMIN_USER_GROUP, BaseIntegration.SYSTEM_CUSTOMER_PROFILE, MongoActionOperation.INSERT);
    }

    @AfterClass
    public static void someStuff() {
        // example of cleaning tests after tests
        //BaseIntegration.updateGroupAddProfile(BaseIntegration.ADMIN_USER_GROUP, BaseIntegration.SYSTEM_CUSTOMER_PROFILE, MongoActionOperation.REMOVE);

        // TODO: Cleaning: differentiate data created automatically by FactoryDtos, and clean them by a simple select
        //  request over each collection !
    }

}
