package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.RuleDto;
import fr.gouv.vitamui.referential.internal.client.RuleInternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RuleExternalServiceTest extends ExternalServiceTest {

    @Mock
    private RuleInternalRestClient ruleInternalRestClient;
    @Mock
    private ExternalSecurityService externalSecurityService;

    private RuleExternalService ruleExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        ruleExternalService = new RuleExternalService(externalSecurityService, ruleInternalRestClient);
    }

    @Test
    public void getAll_should_return_RuleDtoList_when_ruleInternalRestClient_return_RuleDtoList() {
        final List<RuleDto> list = new ArrayList<>();
        final RuleDto ruleDto = new RuleDto();
        ruleDto.setTenant(1);
        list.add(ruleDto);

        when(ruleInternalRestClient.getAll(any(InternalHttpContext.class), any(Optional.class)))
            .thenReturn(list);

        assertThatCode(() -> {
            ruleExternalService.getAll(Optional.empty());
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_RuleDto_when_ruleInternalRestClient_return_RuleDto() {
        final RuleDto ruleDto = new RuleDto();
        ruleDto.setTenant(1);

        when(ruleInternalRestClient.create(any(InternalHttpContext.class), any(RuleDto.class)))
            .thenReturn(ruleDto);

        assertThatCode(() -> {
            ruleExternalService.create(new RuleDto());
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_boolean_when_ruleInternalRestClient_return_boolean() {
        when(ruleInternalRestClient.check(any(InternalHttpContext.class), any(RuleDto.class)))
            .thenReturn(true);

        assertThatCode(() -> {
            ruleExternalService.check(new RuleDto());
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_return_ok_when_ruleInternalRestClient_return_ok() {
        doNothing().when(ruleInternalRestClient).delete(any(InternalHttpContext.class), any(String.class));
        String id = "1";

        assertThatCode(() -> {
            ruleExternalService.delete(id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void export_should_return_ok_when_ruleInternalRestClient_return_ok() {
        when(ruleInternalRestClient.export(any(InternalHttpContext.class)))
            .thenReturn(new ResponseEntity<Resource>(HttpStatus.ACCEPTED));

        assertThatCode(() -> {
            ruleExternalService.export();
        }).doesNotThrowAnyException();
    }
}
