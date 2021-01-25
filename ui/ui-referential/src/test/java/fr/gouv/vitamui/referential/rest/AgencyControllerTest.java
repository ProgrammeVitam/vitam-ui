/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.rest;

import static org.mockito.ArgumentMatchers.any;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableMap;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.AgencyDto;
import fr.gouv.vitamui.referential.service.AgencyService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = { AgencyController.class })
public class AgencyControllerTest extends UiReferentialRestControllerTest<AgencyDto> {

	@Value("${ui-referential.prefix}")
	protected String apiUrl;

	@MockBean
	private AgencyService service;

	private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AgencyController.class);

	private static final String PREFIX = "/agency";

	@Test
	public void testCreateAgency() {
		super.testCreateEntity();
	}

	@Test
	public void testGetAllAgencies() {
		super.testGetAllEntityWithCriteria();
	}

    @Test
    public void testGetAllPaginatedAgencies() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.X_TENANT_ID_HEADER, "1");
        super.performGet("/", ImmutableMap.of("page", 1, "size", 20, "orderBy", "id"), headers);
    }

	@Test
	public void testPatchAgency() {
		super.testPatchEntity();
	}

	@Test
	public void testCheckExistByName() {
		Mockito.when(service.checkExist(any(), any())).thenReturn(true);
		final QueryDto criteria = QueryDto.criteria().addCriterion("name", "agencyName",
				CriterionOperator.EQUALS);
		super.performHead(CommonConstants.PATH_CHECK, ImmutableMap.of("criteria", criteria.toJson()));
	}

	@Test
	public void testFindHistoryById() {
		Mockito.when(service.findHistoryById(any(ExternalHttpContext.class), any(String.class))).thenReturn(new LogbookOperationsResponseDto());
		super.performGet("/1/history");
	}

	@Test
	public void testGetAgencyById() {
		super.testGetEntityById();
	}

    @Test
    public void testDeleteAgency() {
    	super.performDelete("/1");
    }

	@Override
	protected String getRessourcePrefix() {
		return "/" + apiUrl + PREFIX;
	}

	@Override
	protected Class<AgencyDto> getDtoClass() {
		return AgencyDto.class;
	}

	@Override
	protected AgencyDto buildDto() {
		final AgencyDto dto = new AgencyDto();
		return dto;
	}

	@Override
	protected VitamUILogger getLog() {
		return LOGGER;
	}

	@Override
	protected void preparedServices() {
		Mockito.when(service.create(any(), any(AgencyDto.class))).thenReturn(new AgencyDto());
		Mockito.when(service.update(any(), any(AgencyDto.class))).thenReturn(new AgencyDto());
	}
}
