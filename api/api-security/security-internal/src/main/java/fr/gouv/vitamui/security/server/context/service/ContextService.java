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
package fr.gouv.vitamui.security.server.context.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import fr.gouv.vitamui.security.server.certificate.dao.CertificateRepository;
import fr.gouv.vitamui.security.server.certificate.domain.Certificate;
import fr.gouv.vitamui.security.server.context.dao.ContextRepository;
import fr.gouv.vitamui.security.server.context.domain.Context;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the application contexts.
 *
 *
 */
@Getter
@Setter
public class ContextService extends VitamUICrudService<ContextDto, Context> {

    private final ContextRepository contextRepository;

    private final CertificateRepository certificateRepository;

    @Autowired
    public ContextService(final CustomSequenceRepository sequenceRepository, final ContextRepository contextRepository,
            final CertificateRepository certificateRepository) {
        super(sequenceRepository);
        this.contextRepository = contextRepository;
        this.certificateRepository = certificateRepository;
    }

    public ContextDto findByCertificate(final String data) {

        final Certificate certificate = certificateRepository.findByData(data);
        if (certificate == null) {
            throw new NotFoundException("Certificate not found");
        }
        else {

            final String contextId = certificate.getContextId();
            final List<ContextDto> contexts = getMany(contextId);

            if (contexts == null) {
                throw new InternalServerException("No context was found with id: " + certificate.getContextId());
            }
            if (contexts.size() != 1) {
                throw new InternalServerException("Unable to find only one context with id " + certificate.getContextId() + " for certificate");
            }
            else {
                return contexts.get(0);
            }
        }
    }

    /**
     * Add Tenant to Context tenants.
     * @param contextId
     * @param tenantIdentifier
     * @return
     */
    public ContextDto addTenant(final String contextId, final Integer tenantIdentifier) {
        final ContextDto contextDto = getOne(contextId, Optional.empty(), Optional.empty());
        if (contextDto == null) {
            throw new NotFoundException("Context not found");
        }
        else {
            contextDto.getTenants().add(tenantIdentifier);
            return update(contextDto);
        }
    }

    @Override
    protected void beforeCreate(final ContextDto dto) {
        super.beforeCreate(dto);
        final List<String> roleNames = dto.getRoleNames();
        Assert.isTrue(ServicesData.checkIfRoleNameExists(roleNames), "Some of the rolenames: " + roleNames + " are not allowed");
    }

    @Override
    protected void beforeUpdate(final ContextDto dto) {
        super.beforeUpdate(dto);
        final List<String> roleNames = dto.getRoleNames();
        Assert.isTrue(ServicesData.checkIfRoleNameExists(roleNames), "Some of the rolenames: " + roleNames + " are not allowed");
    }

    @Override
    protected Context internalConvertFromDtoToEntity(final ContextDto dto) {
        final Context context = new Context();
        context.setId(dto.getId());
        context.setName(dto.getName());
        context.setFullAccess(dto.isFullAccess());
        context.setTenants(dto.getTenants());
        context.setRoleNames(dto.getRoleNames());
        return context;
    }

    @Override
    protected ContextDto internalConvertFromEntityToDto(final Context context) {
        final ContextDto dto = new ContextDto();
        dto.setId(context.getId());
        dto.setName(context.getName());
        dto.setFullAccess(context.isFullAccess());
        dto.setTenants(context.getTenants());
        dto.setRoleNames(context.getRoleNames());
        return dto;
    }

    @Override
    protected ContextRepository getRepository() {
        return contextRepository;
    }

    @Override
    protected String getObjectName() {
        return "context";
    }

    @Override
    protected Class<Context> getEntityClass() {
        return Context.class;
    }
}
