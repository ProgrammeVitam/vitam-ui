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
package fr.gouv.vitamui.referential.external.server.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.iam.security.client.AbstractInternalClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.internal.client.UnitInternalRestClient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Service
public class UnitExternalService extends AbstractInternalClientService {

    @Autowired
    private UnitInternalRestClient unitInternalRestClient;

    public UnitExternalService(@Autowired  UnitInternalRestClient unitInternalRestClient, final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.unitInternalRestClient = unitInternalRestClient;
    }

    @Override
    protected UnitInternalRestClient getClient() {
        return unitInternalRestClient;
    }

    public VitamUISearchResponseDto findUnitById(final String id) {
        return getClient().findUnitById(getInternalHttpContext(), id);
    }

    public JsonNode findUnitByDsl(final Optional<String> id, final JsonNode dsl) {
        return getClient().findUnitByDsl(getInternalHttpContext(), id, dsl);
    }
    
    public JsonNode findObjectMetadataById(final String id, final JsonNode dsl) {
        return getClient().findObjectMetadataById(getInternalHttpContext(), id, dsl);
    }

    public VitamUISearchResponseDto getFilingPlan() {
        return getClient().getFilingPlan(getInternalHttpContext());
    }

}
