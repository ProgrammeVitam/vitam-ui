package fr.gouv.vitamui.iam.internal.server.tenant.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.logbook.util.LogbookUtils;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;

public class TenantConverterTest {

    private final OwnerRepository ownerRepository = Mockito.mock(OwnerRepository.class);

    private final TenantConverter tenantConverter = new TenantConverter(ownerRepository);

    @Test
    public void testConvertEntityToDto() {
        Tenant tenant = new Tenant();
        tenant.setAccessContractHoldingIdentifier("AC-000002");
        tenant.setAccessContractLogbookIdentifier("AC-00001");
        tenant.setCustomerId("customerId");
        tenant.setEnabled(true);
        tenant.setId("id");
        tenant.setIdentifier(10);
        tenant.setIngestContractHoldingIdentifier("IC-000001");
        tenant.setName("name");
        tenant.setOwnerId("ownerId");
        tenant.setProof(true);
        tenant.setReadonly(false);
        TenantDto res = tenantConverter.convertEntityToDto(tenant);
        assertThat(res).isEqualToComparingFieldByField(tenant);
    }

    @Test
    public void testConvertToToEntity() {
        TenantDto tenantDto = IamDtoBuilder.buildTenantDto("id", "name", 10, "ownerId", "customerId");
        Tenant res = tenantConverter.convertDtoToEntity(tenantDto);
        assertThat(res).isEqualToComparingFieldByField(tenantDto);
    }

    @Test
    public void testConvertToLogbook() throws InvalidParseOperationException {
        TenantDto tenantDto = IamDtoBuilder.buildTenantDto("id", "name", 10, "ownerId", "customerId");
        Owner owner = new Owner();
        tenantDto.setAccessContractHoldingIdentifier("AC-000001");
        tenantDto.setAccessContractLogbookIdentifier("AC-000002");
        tenantDto.setIngestContractHoldingIdentifier("IC-00002");
        Mockito.when(ownerRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(owner));
        String json = tenantConverter.convertToLogbook(tenantDto);
        assertThat(json).isNotBlank();

        JsonNode jsonNode = JsonHandler.getFromString(json);
        assertThat(jsonNode.get(TenantConverter.ACCESS_CONTRACT_HOLDING_IDENTIFIER_KEY)).isNotNull();
        assertThat(jsonNode.get(TenantConverter.ACCESS_CONTRACT_LOGBOOK_IDENTIFIER_KEY)).isNotNull();
        assertThat(jsonNode.get(TenantConverter.INGEST_CONTRACT_HOLDING_IDENTIFIER_KEY)).isNotNull();

        tenantDto.setAccessContractHoldingIdentifier(null);
        json = tenantConverter.convertToLogbook(tenantDto);
        jsonNode = JsonHandler.getFromString(json);
        assertThat(jsonNode.get(TenantConverter.ACCESS_CONTRACT_HOLDING_IDENTIFIER_KEY)).isNotNull();

        tenantDto.setAccessContractLogbookIdentifier(null);;
        json = tenantConverter.convertToLogbook(tenantDto);
        jsonNode = JsonHandler.getFromString(json);
        assertThat(jsonNode.get(TenantConverter.ACCESS_CONTRACT_LOGBOOK_IDENTIFIER_KEY)).isNotNull();

        tenantDto.setIngestContractHoldingIdentifier(null);
        json = tenantConverter.convertToLogbook(tenantDto);
        jsonNode = JsonHandler.getFromString(json);
        assertThat(jsonNode.get(TenantConverter.INGEST_CONTRACT_HOLDING_IDENTIFIER_KEY)).isNotNull();

        tenantDto.setItemIngestContractIdentifier(null);
        json = tenantConverter.convertToLogbook(tenantDto);
        jsonNode = JsonHandler.getFromString(json);
        assertThat(jsonNode.get(TenantConverter.ITEM_INGEST_CONTRACT_IDENTIFIER_KEY)).isNotNull();
    }

}
