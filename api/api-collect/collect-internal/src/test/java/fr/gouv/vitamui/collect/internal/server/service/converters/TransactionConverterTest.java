package fr.gouv.vitamui.collect.internal.server.service.converters;

import fr.gouv.vitam.collect.external.dto.TransactionDto;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionConverterTest {

    @Test
    public void convertTransactionDtotoVitamUiDto() {
        TransactionDto transactionDto = buildTransactionDto();
        CollectTransactionDto collectTransactionDto = TransactionConverter.toVitamUiDto(transactionDto);
        assertNotNull(collectTransactionDto);
        assertEquals(transactionDto.getId(), collectTransactionDto.getId());
        assertEquals(transactionDto.getStatus(), collectTransactionDto.getStatus());
        assertEquals(transactionDto.getName(), collectTransactionDto.getName());
    }

    @Test
    public void convertTransactionDtotoVitamDto() {
        CollectTransactionDto collectTransactionDto = buildCollectTransactionDto();
        TransactionDto transactionDto = TransactionConverter.toVitamDto(collectTransactionDto);
        assertNotNull(transactionDto);
        assertEquals(collectTransactionDto.getId(), transactionDto.getId());
        assertEquals(collectTransactionDto.getStatus(), transactionDto.getStatus());
        assertEquals(collectTransactionDto.getName(), transactionDto.getName());
    }

    @Test
    public void convertListTransactionDtotoVitamUiDtos() {
        TransactionDto transactionDto1 = buildTransactionDto();
        TransactionDto transactionDto2 = buildTransactionDto();
        List<TransactionDto> transactionDtos = List.of(transactionDto1, transactionDto2);
        List<CollectTransactionDto> collectTransactionDtos = TransactionConverter.toVitamuiDtos(transactionDtos);

        assertNotNull(collectTransactionDtos);
        assertEquals(collectTransactionDtos.size(), 2);
        assertEquals(collectTransactionDtos.get(0).getId(), transactionDto1.getId());
        assertEquals(collectTransactionDtos.get(0).getStatus(), transactionDto1.getStatus());
    }


    private TransactionDto buildTransactionDto() {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setArchivalAgencyIdentifier("archivalAgency");
        transactionDto.setArchivalAgreement("archivalAgreement");
        transactionDto.setComment("comment");
        transactionDto.setArchivalProfile("archivalProfile");
        transactionDto.setId("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");
        transactionDto.setMessageIdentifier("messageIdentifier");
        transactionDto.setOriginatingAgencyIdentifier("originatingAgencyIdentifier");
        transactionDto.setSubmissionAgencyIdentifier("submissionIdentifier");
        transactionDto.setTransferringAgencyIdentifier("TransferringAgencyIdentifier");
        transactionDto.setAcquisitionInformation("AcquisitionInformation");
        transactionDto.setLegalStatus("OPEN");
        transactionDto.setCreationDate("2022-10-24T16:40:59.821");
        transactionDto.setLastUpdate("2022-10-24T16:40:59.821");
        transactionDto.setProjectId("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaabq");
        return transactionDto;
    }

    private CollectTransactionDto buildCollectTransactionDto() {
        CollectTransactionDto collectTransactionDto = new CollectTransactionDto();
        collectTransactionDto.setArchivalAgencyIdentifier("archivalAgency");
        collectTransactionDto.setArchivalAgreement("archivalAgreement");
        collectTransactionDto.setComment("comment");
        collectTransactionDto.setArchiveProfile("archivalProfile");
        collectTransactionDto.setId("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");
        collectTransactionDto.setMessageIdentifier("messageIdentifier");
        collectTransactionDto.setOriginatingAgencyIdentifier("originatingAgencyIdentifier");
        collectTransactionDto.setSubmissionAgencyIdentifier("submissionIdentifier");
        collectTransactionDto.setTransferringAgencyIdentifier("TransferringAgencyIdentifier");
        collectTransactionDto.setAcquisitionInformation("AcquisitionInformation");
        collectTransactionDto.setLegalStatus("OPEN");
        collectTransactionDto.setCreationDate("2022-10-24T16:40:59.821");
        collectTransactionDto.setLastUpdate("2022-10-24T16:40:59.821");
        collectTransactionDto.setProjectId("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaabq");
        return collectTransactionDto;
    }
}
