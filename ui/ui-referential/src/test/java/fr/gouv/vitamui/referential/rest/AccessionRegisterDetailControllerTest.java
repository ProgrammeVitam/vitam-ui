package fr.gouv.vitamui.referential.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterDetailDto;
import fr.gouv.vitamui.referential.service.AccessionRegisterDetailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value =  AccessionRegisterDetailController.class)
class AccessionRegisterDetailControllerTest extends UiReferentialRestControllerTest<AccessionRegisterDetailDto> {

    @MockBean
    private AccessionRegisterDetailService service;

    @Test
    void should_call_the_corresponding_service_once_when_paginated_api_is_called() throws Exception {
        //Given //When
        ResultActions resultActions = super.performGet("/", ImmutableMap.of("page", 1, "size", 20), super.getHeaders());

        //Then
        resultActions.andExpect(status().isOk());
        verify(service, times(1)).getAllPaginated(any(), any(), any(), any(), any(), any());
    }

    @Override
    protected Class<AccessionRegisterDetailDto> getDtoClass() {
        return AccessionRegisterDetailDto.class;
    }

    @Override
    protected AccessionRegisterDetailDto buildDto() {
        return new AccessionRegisterDetailDto();
    }

    @Override
    protected VitamUILogger getLog() {
        return VitamUILoggerFactory.getInstance(AccessionRegisterDetailControllerTest.class);
    }

    @Override
    protected void preparedServices() {
        PaginatedValuesDto<AccessionRegisterDetailDto> response = new PaginatedValuesDto<>();
        when(service.getAllPaginated(any(), any(), any(), any(), any(), any())).thenReturn(response);
    }

    @Override
    protected String getRessourcePrefix() {
        return "/referential-api/accessionregisterdetails";
    }
}
