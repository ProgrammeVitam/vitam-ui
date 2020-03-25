package fr.gouv.vitamui.cucumber.front.steps.common;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.cucumber.common.CommonSteps;
import fr.gouv.vitamui.cucumber.front.context.ContextFront;
import fr.gouv.vitamui.cucumber.front.context.ContextFrontConfiguration;
import fr.gouv.vitamui.cucumber.front.pages.PreLoginPage;
import fr.gouv.vitamui.cucumber.front.utils.UserEnum;
import fr.gouv.vitamui.utils.TestConstants;
import lombok.Getter;

@ContextConfiguration(classes = ContextFrontConfiguration.class)
@Getter
public class CommonStepDefinitions extends CommonSteps {

	protected PreLoginPage preLoginPage;

    // --------------------------- Application URls -----------------------------------------------

    @Value("${ui-portal.base-url}")
    protected String portalUrl;

    // --------------------------- EMAIL -----------------------------------------------
    @Value("${user.admin.email}")
    protected String adminEmail;

    @Value("${user.demo.email}")
    protected String demoEmail;

    private final String unknowUser = "unknow_user@test.fr";
    // --------------------------- PASSWORRD -----------------------------------------------

    @Value("${user.admin.password}")
    protected String adminPassword;

    @Value("${user.demo.password}")
    protected String demoPassword;

    // --------------------------- ENVIRONNEMENT -------------------------------------------

    @Value("${environnement}")
    protected String environnement;

    @Autowired
    public ContextFront context;

    protected String getEmailByUser(final UserEnum user) {
        String email = "";
        switch (user) {
            case ADMIN :
                email = adminEmail;
                break;
            case DEMO :
                email = demoEmail;
                break;
            case UNKNOW :
                email = unknowUser;
                break;
            default :
                break;
        }
        return email;
    }

    protected String getPasswordByUser(final UserEnum user) {
        String password = "";
        switch (user) {
            case ADMIN :
                password = adminPassword;
                break;
            case DEMO :
                password = demoPassword;
                break;
            default :
                break;
        }
        return password;
    }

    protected String getCurrentUserEmail() {
        return getEmailByUser(context.getCurrentUser());
    }

    protected void saveCurrentUser(final UserEnum user) {
        context.setCurrentUser(user);
    }

    public void checkTraceIsPresentForCurrentUser(final String eventType) {
        final UserDto basicUserDto = getCurrentUser();
        super.testTrace(basicUserDto.getCustomerId(), basicUserDto.getIdentifier(), "users", eventType);
    }

    private UserDto getCurrentUser() {
        final ExternalHttpContext extneralHttpContext = getContext(casTenantIdentifier,
                TestConstants.TOKEN_USER_CAS);
        final UserDto basicUserDto = getCasRestClient(false, new Integer[] { casTenantIdentifier },
                new String[] { ServicesData.ROLE_CAS_USERS }).getUserByEmail(extneralHttpContext, getCurrentUserEmail(),
                        Optional.empty());
        return basicUserDto;
    }

    public void waitForPreLoginPage() {
    	preLoginPage.waitForTitleToAppear();
    }

    public ContextFront getContext() {
    	return context;
    }

}
