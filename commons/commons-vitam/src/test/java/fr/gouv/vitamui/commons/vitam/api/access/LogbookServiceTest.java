package fr.gouv.vitamui.commons.vitam.api.access;

import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AccessExternalClientFactory;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClientFactory;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitam.ingest.external.client.IngestExternalClientFactory;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;

@RunWith(MockitoJUnitRunner.class)
public class LogbookServiceTest {

    private LogbookService logbookService;

    private AccessExternalClient accessExternalClient;

    private IngestExternalClient ingestExternalClient;

    private AdminExternalClient adminExternalClient;

    @Before
    public void setup() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);

        accessExternalClient = AccessExternalClientFactory.getInstance().getClient();
        ingestExternalClient = IngestExternalClientFactory.getInstance().getClient();
        adminExternalClient = AdminExternalClientFactory.getInstance().getClient();
        logbookService = new LogbookService(accessExternalClient, ingestExternalClient, adminExternalClient);
    }

    @Test
    public void testDownloadManifest_whenIngestOperation() throws VitamClientException {
        logbookService = spy(logbookService);
        final LogbookOperation operation = new LogbookOperation();
        operation.setEvTypeProc("INGEST");
        final RequestResponseOK<LogbookOperation> operationResponse = new RequestResponseOK<>();
        operationResponse.addResult(operation);
        Mockito.when(logbookService.selectOperationbyId(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(operationResponse);

        final Response response = logbookService.downloadManifest("vitamId", new VitamContext(10));
        VitamRestUtils.checkResponse(response, Response.Status.OK.getStatusCode());
    }

    @Test
    public void testDownloadAtr_whenIngestOperation() throws VitamClientException {
        logbookService = spy(logbookService);
        final LogbookOperation operation = new LogbookOperation();
        operation.setEvTypeProc("INGEST");
        final RequestResponseOK<LogbookOperation> operationResponse = new RequestResponseOK<>();
        operationResponse.addResult(operation);
        Mockito.when(logbookService.selectOperationbyId(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(operationResponse);

        final Response response = logbookService.downloadAtr("vitamId", new VitamContext(10));
        VitamRestUtils.checkResponse(response, Response.Status.OK.getStatusCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDownloadAtr_whenNotIngestOperation() throws VitamClientException {
        logbookService = spy(logbookService);
        final LogbookOperation operation = new LogbookOperation();
        operation.setEvTypeProc("OTHER");
        final RequestResponseOK<LogbookOperation> operationResponse = new RequestResponseOK<>();
        operationResponse.addResult(operation);
        Mockito.when(logbookService.selectOperationbyId(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(operationResponse);

        logbookService.downloadAtr("vitamId", new VitamContext(10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDownloadAtr_whenNoOperation() throws VitamClientException {
        logbookService = spy(logbookService);
        final RequestResponseOK<LogbookOperation> operationResponse = new RequestResponseOK<>();
        Mockito.when(logbookService.selectOperationbyId(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(operationResponse);

        logbookService.downloadAtr("vitamId", new VitamContext(10));
    }

    @Test(expected = ApplicationServerException.class)
    public void testDownloadAtr_whenVitamException() throws VitamClientException {
        logbookService = spy(logbookService);
        Mockito.doThrow(new VitamClientException("error")).when(logbookService).selectOperationbyId(ArgumentMatchers.any(), ArgumentMatchers.any());

        logbookService.downloadAtr("vitamId", new VitamContext(10));
    }

}
