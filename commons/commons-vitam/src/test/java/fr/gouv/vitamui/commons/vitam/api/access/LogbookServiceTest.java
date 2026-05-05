package fr.gouv.vitamui.commons.vitam.api.access;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import jakarta.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class LogbookServiceTest {

    public static final String MASTER_DATA = "MASTERDATA";

    public static final String INGEST = "INGEST";

    public static final String DIP_EXPORT = "DIP_EXPORT";

    public static final String OTHER = "OTHER";

    private LogbookService logbookService;

    @Mock
    private AccessExternalClient accessExternalClient;

    @Mock
    private IngestExternalClient ingestExternalClient;

    @Mock
    private AdminExternalClient adminExternalClient;

    @BeforeEach
    public void setup() {
        logbookService = new LogbookService(accessExternalClient, ingestExternalClient, adminExternalClient);
    }

    @Test
    void testDownloadManifest_whenIngestOperation() throws VitamClientException {
        logbookService = spy(logbookService);
        final LogbookOperation operation = new LogbookOperation();
        operation.setEvTypeProc(INGEST);
        final RequestResponseOK<LogbookOperation> operationResponse = new RequestResponseOK<>();
        operationResponse.addResult(operation);
        Mockito.doReturn(operationResponse)
            .when(logbookService)
            .selectOperationbyId(ArgumentMatchers.any(), ArgumentMatchers.any());

        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        Mockito.when(mockResponse.getHeaders()).thenReturn(new jakarta.ws.rs.core.MultivaluedHashMap<>());
        Mockito.when(
            ingestExternalClient.downloadObjectAsync(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            )
        ).thenReturn(mockResponse);

        final Response response = logbookService.downloadManifest("vitamId", new VitamContext(10));
        VitamRestUtils.checkResponse(response, Response.Status.OK.getStatusCode());
    }

    @Test
    void testDownloadAtr_whenIngestOperation() throws VitamClientException {
        logbookService = spy(logbookService);
        final LogbookOperation operation = new LogbookOperation();
        operation.setEvTypeProc(INGEST);
        final RequestResponseOK<LogbookOperation> operationResponse = new RequestResponseOK<>();
        operationResponse.addResult(operation);
        Mockito.doReturn(operationResponse)
            .when(logbookService)
            .selectOperationbyId(ArgumentMatchers.any(), ArgumentMatchers.any());

        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        Mockito.when(mockResponse.getHeaders()).thenReturn(new jakarta.ws.rs.core.MultivaluedHashMap<>());
        Mockito.when(
            ingestExternalClient.downloadObjectAsync(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            )
        ).thenReturn(mockResponse);

        final Response response = logbookService.downloadAtr("vitamId", new VitamContext(10));
        VitamRestUtils.checkResponse(response, Response.Status.OK.getStatusCode());
    }

    @Test
    void testDownloadAtr_whenNotIngestOperation() {
        assertThrows(IllegalArgumentException.class, () -> {
            logbookService = spy(logbookService);
            final LogbookOperation operation = new LogbookOperation();
            operation.setEvTypeProc(OTHER);
            final RequestResponseOK<LogbookOperation> operationResponse = new RequestResponseOK<>();
            operationResponse.addResult(operation);
            Mockito.doReturn(operationResponse)
                .when(logbookService)
                .selectOperationbyId(ArgumentMatchers.any(), ArgumentMatchers.any());

            logbookService.downloadAtr("vitamId", new VitamContext(10));
        });
    }

    @Test
    void testDownloadAtr_whenNoOperation() {
        assertThrows(IllegalArgumentException.class, () -> {
            logbookService = spy(logbookService);
            final RequestResponseOK<LogbookOperation> operationResponse = new RequestResponseOK<>();
            Mockito.doReturn(operationResponse)
                .when(logbookService)
                .selectOperationbyId(ArgumentMatchers.any(), ArgumentMatchers.any());

            logbookService.downloadAtr("vitamId", new VitamContext(10));
        });
    }

    @Test
    void testDownloadAtr_whenVitamException() {
        assertThrows(ApplicationServerException.class, () -> {
            logbookService = spy(logbookService);
            Mockito.doThrow(new VitamClientException("error"))
                .when(logbookService)
                .selectOperationbyId(ArgumentMatchers.any(), ArgumentMatchers.any());

            logbookService.downloadAtr("vitamId", new VitamContext(10));
        });
    }

    @Test
    void testDownloadDip_whenExportIsSuccess() throws Exception {
        logbookService = spy(logbookService);
        final LogbookOperation operation = new LogbookOperation();
        operation.setEvTypeProc(DIP_EXPORT);

        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        Mockito.when(mockResponse.readEntity(ByteArrayInputStream.class)).thenReturn(
            new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8))
        );
        Mockito.when(accessExternalClient.getDIPById(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
            mockResponse
        );

        final Response response = logbookService.downloadReport(
            "aeeaaaaaaggtywctaanl4al3q2moiyyaaaaq",
            "dip",
            new VitamContext(10)
        );
        VitamRestUtils.checkResponse(response, Response.Status.OK.getStatusCode());

        String reportContent = IOUtils.toString(
            response.readEntity(ByteArrayInputStream.class),
            StandardCharsets.UTF_8
        );
        Assertions.assertThat(reportContent).isEqualTo("test");
    }

    @Test
    void testDownloadAtr_whenMasterDataOperation() throws VitamClientException {
        logbookService = spy(logbookService);
        final LogbookOperation operation = new LogbookOperation();
        operation.setEvTypeProc(MASTER_DATA);
        final RequestResponseOK<LogbookOperation> operationResponse = new RequestResponseOK<>();
        operationResponse.addResult(operation);
        Mockito.doReturn(operationResponse)
            .when(logbookService)
            .selectOperationbyId(ArgumentMatchers.any(), ArgumentMatchers.any());

        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        Mockito.when(mockResponse.getHeaders()).thenReturn(new jakarta.ws.rs.core.MultivaluedHashMap<>());
        Mockito.when(
            ingestExternalClient.downloadObjectAsync(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            )
        ).thenReturn(mockResponse);

        final Response response = logbookService.downloadAtr("vitamId", new VitamContext(10));
        VitamRestUtils.checkResponse(response, Response.Status.OK.getStatusCode());
    }

    @Test
    void toHistoryEvents_should_return_ok_when_vitamclient_ok() {
        assertThatCode(
            () -> logbookService.toHistoryEvents(new RequestResponseOK<LogbookOperation>().setHttpCode(200), null)
        ).doesNotThrowAnyException();
    }

    @Test
    void toHistoryEvents_should_return_ok_when_vitamclient_400() {
        assertThatCode(
            () -> logbookService.toHistoryEvents(new RequestResponseOK<LogbookOperation>().setHttpCode(400), null)
        ).doesNotThrowAnyException();
    }
}
