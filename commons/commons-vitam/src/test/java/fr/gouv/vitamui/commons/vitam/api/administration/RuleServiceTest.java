package fr.gouv.vitamui.commons.vitam.api.administration;

import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.FileRulesModel;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.vitam.api.model.RuleMeasurementEnum;

public class RuleServiceTest {

	@Mock
	private AdminExternalClient adminExternalClient;
	
	@InjectMocks
	private RuleService ruleService;
	
	private static final String RULE_ID = "FC-1";
	
	private static final int TENANT_IDENTIFIER = 9;
	
	private static final Long RULE_DURATION = 10L;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testfindRulesDurationByRuleId() throws VitamClientException, JsonProcessingException, InvalidCreateOperationException {
		// Prepare
		final var fileRule = buildFileRuleModel(RuleMeasurementEnum.YEAR);
        final var requestResponseOk = new RequestResponseOK<FileRulesModel>().addResult(fileRule);
        when(adminExternalClient.findRules(Mockito.any(VitamContext.class), Mockito.any(JsonNode.class))).thenReturn(requestResponseOk);
        // Do
		Optional<Long> ruleDuration = ruleService.findRulesDurationByRuleId(new VitamContext(TENANT_IDENTIFIER), RULE_ID);
		// Verify
        Assertions.assertTrue(ruleDuration.isPresent(), "The rule duration should be present");
        Assertions.assertEquals(RULE_DURATION, ruleDuration.get(), "The rule duration value should match the duration");
	}
	
	@Test
	public void testfindRulesDurationByRuleId_with_rule_duration_in_months() 
			throws VitamClientException, JsonProcessingException, InvalidCreateOperationException {
        // Prepare
		final var fileRule = buildFileRuleModel(RuleMeasurementEnum.MONTH);
        final var requestResponseOk = new RequestResponseOK<FileRulesModel>().addResult(fileRule);
        when(adminExternalClient.findRules(Mockito.any(VitamContext.class), Mockito.any(JsonNode.class))).thenReturn(requestResponseOk);
		// Do and Verify
        var thrownException = Assertions.assertThrows(UnexpectedDataException.class, () -> {
        	ruleService.findRulesDurationByRuleId(new VitamContext(TENANT_IDENTIFIER), RULE_ID);
        });
        Assertions.assertEquals("The rule duration measurement should be in years.", thrownException.getMessage(), 
        		"The exception message should match");
	}
	
	@After
	public void destroy() {
		ruleService = null;
		adminExternalClient = null;
	}
	
	private static FileRulesModel buildFileRuleModel(RuleMeasurementEnum ruleMeasurementEnum) {
        final var rule = new FileRulesModel();
        rule.setRuleId(RULE_ID);
        rule.setRuleDuration(RULE_DURATION.toString());
        rule.setRuleMeasurement(ruleMeasurementEnum.getType());
        return rule;
	}
	
}
