/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.collect.external.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.external.server.service.ProjectExternalService;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = {ProjectExternalController.class})
public class ProjectExternalControllerTest extends ApiCollectExternalControllerTest<IdDto> {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ProjectExternalControllerTest.class);

    @MockBean
    private ProjectExternalService projectExternalService;

    private ProjectExternalController projectExternalController;

    @BeforeEach
    public void setUp() {
        projectExternalController = new ProjectExternalController(
            projectExternalService);
    }


    @Test
    void when_abortTransaction_ok() throws InvalidParseOperationException {

        PaginatedValuesDto<CollectTransactionDto> listTransactions = new PaginatedValuesDto<>();
        CollectTransactionDto transactionDto = CollectTransactionDto
            .builder().id("transactionId").projectId("projectId").build();
        listTransactions.setValues(List.of(transactionDto));
        Mockito
            .when(projectExternalService.getTransactionsByProjectPaginated(0, 10, Optional.empty(), Optional.of("id"),
                Optional.of(DirectionDto.ASC), "projectId"))
            .thenReturn(listTransactions);

        PaginatedValuesDto<CollectTransactionDto> transactionsReturned= this.projectExternalController.getTransactionsByProjectPaginated(0, 10, Optional.empty(), Optional.of("id"),
            Optional.of(DirectionDto.ASC), "projectId");
        Assertions.assertEquals(transactionsReturned, listTransactions);
    }



    @Override
    protected String[] getServices() {
        return new String[] {ServicesData.TRANSACTIONS};
    }

    @Override
    protected Class<IdDto> getDtoClass() {
        return IdDto.class;
    }

    @Override
    protected IdDto buildDto() {
        return null;
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {

    }

    @Override
    protected String getRessourcePrefix() {
        return null;
    }
}
