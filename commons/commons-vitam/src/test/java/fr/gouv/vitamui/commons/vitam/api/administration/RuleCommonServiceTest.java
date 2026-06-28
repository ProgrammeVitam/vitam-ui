package fr.gouv.vitamui.commons.vitam.api.administration;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.FileRulesModel;
import fr.gouv.vitam.common.model.administration.RuleMeasurementEnum;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

import static org.mockito.Mockito.when;

public class RuleCommonServiceTest {

    private AutoCloseable mocks;

    @Mock
    private AdminExternalClient adminExternalClient;

    @InjectMocks
    private RuleCommonService ruleCommonService;

    private static final String RULE_ID = "FC-1";

    private static final int TENANT_IDENTIFIER = 9;

    private static final Long RULE_DURATION = 10L;

    @BeforeEach
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @Test
    void testfindRulesDurationByRuleId()
        throws VitamClientException, JacksonException, InvalidCreateOperationException {
        // Prepare
        final var fileRule = buildFileRuleModel(RuleMeasurementEnum.YEAR);
        final var requestResponseOk = new RequestResponseOK<FileRulesModel>().addResult(fileRule);
        when(adminExternalClient.findRules(Mockito.any(VitamContext.class), Mockito.any(JsonNode.class))).thenReturn(
            requestResponseOk
        );
        // Do
        Optional<Long> ruleDuration = ruleCommonService.findRulesDurationByRuleId(
            new VitamContext(TENANT_IDENTIFIER),
            RULE_ID
        );
        // Verify
        Assertions.assertTrue(ruleDuration.isPresent(), "The rule duration should be present");
        Assertions.assertEquals(RULE_DURATION, ruleDuration.get(), "The rule duration value should match the duration");
    }

    @Test
    void testfindRulesDurationByRuleId_with_rule_duration_in_months()
        throws VitamClientException, JacksonException, InvalidCreateOperationException {
        // Prepare
        final var fileRule = buildFileRuleModel(RuleMeasurementEnum.MONTH);
        final var requestResponseOk = new RequestResponseOK<FileRulesModel>().addResult(fileRule);
        when(adminExternalClient.findRules(Mockito.any(VitamContext.class), Mockito.any(JsonNode.class))).thenReturn(
            requestResponseOk
        );
        // Do and Verify
        var thrownException = Assertions.assertThrows(
            UnexpectedDataException.class,
            () -> ruleCommonService.findRulesDurationByRuleId(new VitamContext(TENANT_IDENTIFIER), RULE_ID)
        );
        Assertions.assertEquals(
            "The rule duration measurement should be in years.",
            thrownException.getMessage(),
            "The exception message should match"
        );
    }

    @AfterEach
    public void destroy() throws Exception {
        ruleCommonService = null;
        adminExternalClient = null;
        mocks.close();
    }

    private static FileRulesModel buildFileRuleModel(RuleMeasurementEnum ruleMeasurementEnum) {
        final var rule = new FileRulesModel();
        rule.setRuleId(RULE_ID);
        rule.setRuleDuration(RULE_DURATION.toString());
        rule.setRuleMeasurement(ruleMeasurementEnum);
        return rule;
    }
}
