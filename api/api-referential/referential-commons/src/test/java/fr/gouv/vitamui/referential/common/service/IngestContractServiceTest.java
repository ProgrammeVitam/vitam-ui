package fr.gouv.vitamui.referential.common.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.mock;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;

public class IngestContractServiceTest {

	private AdminExternalClient adminExternalClient;
	private IngestContractService ingestContractService;

	@Before
	public void setUp() {
		adminExternalClient = mock(AdminExternalClient.class);
		ingestContractService = new IngestContractService(adminExternalClient);
	}

	@Test
	public void findIngestContract_should_return_ingestContracts_when_vitamclient_ok() throws VitamClientException {
		VitamContext vitamContext = new VitamContext(0);
		String contractId = "IC-0";
		
		expect(adminExternalClient.findIngestContractById(vitamContext, contractId))
				.andReturn(new RequestResponseOK<IngestContractModel>().setHttpCode(200));
		EasyMock.replay(adminExternalClient);
		
		assertThatCode(() -> {
			ingestContractService.findIngestContractById(vitamContext, contractId);
		}).doesNotThrowAnyException();

	}


	@Test
	public void findIngestContract_should_throw_VitamClienException_when_vitamclient_returns_400() throws VitamClientException {
		VitamContext vitamContext = new VitamContext(1);
		String contractId = "IC-1";
		
		expect(adminExternalClient.findIngestContractById(vitamContext, contractId))
				.andReturn(new RequestResponseOK<IngestContractModel>().setHttpCode(400));
		EasyMock.replay(adminExternalClient);
		
		assertThatThrownBy(() -> {
			ingestContractService.findIngestContractById(vitamContext, contractId);
		}).isInstanceOf(InternalServerException.class);

	}


	@Test
	public void findIngestContract_should_throw_VitamClienException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
		VitamContext vitamContext = new VitamContext(1);
		String contractId = "IC-2";
		
		expect(adminExternalClient.findIngestContractById(vitamContext, contractId))
				.andThrow(new VitamClientException("Exception thrown by Vitam"));
		EasyMock.replay(adminExternalClient);
		
		assertThatThrownBy(() -> {
			ingestContractService.findIngestContractById(vitamContext, contractId);
		}).isInstanceOf(VitamClientException.class);

	}
	
}
