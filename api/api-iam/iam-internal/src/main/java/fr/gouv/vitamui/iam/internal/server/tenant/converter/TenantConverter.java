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
package fr.gouv.vitamui.iam.internal.server.tenant.converter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.logbook.util.LogbookUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;

public class TenantConverter implements Converter<TenantDto, Tenant> {

    public static final String NAME_KEY = "Nom";

    public static final String PROOF_KEY = "Éléments de preuve";

    public static final String ENABLED_KEY = "Activé";

    public static final String OWNER_ID_KEY = "Identifiant du propriétaire";

    public static final String INGEST_CONTRACT_HOLDING_IDENTIFIER_KEY = "Identifiant du contrat d'entrée pour l'arbre";

    public static final String ITEM_INGEST_CONTRACT_IDENTIFIER_KEY = "Identifiant du contrat d'entrée pour les bordereaux";

    public static final String ACCESS_CONTRACT_HOLDING_IDENTIFIER_KEY = "Identifiant du contrat d'accès pour l'arbre";

    public static final String ACCESS_CONTRACT_LOGBOOK_IDENTIFIER_KEY = "Identifiant du contrat d'accès pour le logbook";

    private final OwnerRepository ownerRepository;

    public TenantConverter(final OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public String convertToLogbook(final TenantDto tenant) {
        final Map<String, String> logbookData = new LinkedHashMap<>();
        logbookData.put(NAME_KEY, LogbookUtils.getValue(tenant.getName()));
        logbookData.put(PROOF_KEY, LogbookUtils.getValue(tenant.isProof()));
        logbookData.put(ENABLED_KEY, LogbookUtils.getValue(tenant.getEnabled()));
        Optional<Owner> owner = ownerRepository.findById(tenant.getOwnerId());
        owner.ifPresent(o -> logbookData.put(OWNER_ID_KEY, o.getIdentifier()));
        logbookData.put(ACCESS_CONTRACT_HOLDING_IDENTIFIER_KEY, LogbookUtils.getValue(tenant.getAccessContractHoldingIdentifier()));
        logbookData.put(ACCESS_CONTRACT_LOGBOOK_IDENTIFIER_KEY, LogbookUtils.getValue(tenant.getAccessContractLogbookIdentifier()));
        logbookData.put(INGEST_CONTRACT_HOLDING_IDENTIFIER_KEY, LogbookUtils.getValue(tenant.getIngestContractHoldingIdentifier()));
        logbookData.put(ITEM_INGEST_CONTRACT_IDENTIFIER_KEY, LogbookUtils.getValue(tenant.getItemIngestContractIdentifier()));

        return ApiUtils.toJson(logbookData);
    }

    @Override
    public Tenant convertDtoToEntity(final TenantDto dto) {
        return VitamUIUtils.copyProperties(dto, new Tenant());
    }

    @Override
    public TenantDto convertEntityToDto(final Tenant tenant) {
        return VitamUIUtils.copyProperties(tenant, new TenantDto());
    }
}
