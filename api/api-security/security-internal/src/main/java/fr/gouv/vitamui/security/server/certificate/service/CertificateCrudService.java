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
package fr.gouv.vitamui.security.server.certificate.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.security.common.dto.CertificateDto;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import fr.gouv.vitamui.security.server.certificate.dao.CertificateRepository;
import fr.gouv.vitamui.security.server.certificate.domain.Certificate;
import fr.gouv.vitamui.security.server.context.service.ContextService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the certificates.
 *
 *
 */
@Getter
@Setter
public class CertificateCrudService extends VitamUICrudService<CertificateDto, Certificate> {

    private final CertificateRepository certificateRepository;

    private final ContextService contextCrudService;

    @Autowired
    public CertificateCrudService(
            final CustomSequenceRepository sequenceRepository,
            final CertificateRepository certificateRepository,
            final ContextService contextCrudService) {
        super(sequenceRepository);
        this.certificateRepository = certificateRepository;
        this.contextCrudService = contextCrudService;
    }

    @Override
    protected void beforeCreate(final CertificateDto dto) {
        final String contextId = dto.getContextId();
        final List<ContextDto> contexts = contextCrudService.getMany(contextId);
        Assert.isTrue(contexts != null && !contexts.isEmpty(), "The context: " + contextId + " does not exist.");
    }

    @Override
    protected void beforeUpdate(final CertificateDto dto) {
        final String contextId = dto.getContextId();
        final List<ContextDto> contexts = contextCrudService.getMany(contextId);
        Assert.isTrue(contexts != null && !contexts.isEmpty(), "The context: " + contextId + " does not exist.");
    }

    @Override
    protected Certificate internalConvertFromDtoToEntity(final CertificateDto dto) {
        final Certificate certificate = new Certificate();
        certificate.setId(dto.getId());
        certificate.setSubjectDN(dto.getSubjectDN());
        certificate.setContextId(dto.getContextId());
        certificate.setSerialNumber(dto.getSerialNumber());
        certificate.setIssuerDN(dto.getIssuerDN());
        // clean the certificate before saving it
        final String data = dto.getData().replaceAll("\\n", "").replaceFirst("-----BEGIN CERTIFICATE-----", "")
                .replaceFirst("-----END CERTIFICATE-----", "");
        certificate.setData(data);
        return certificate;
    }

    @Override
    protected CertificateDto internalConvertFromEntityToDto(final Certificate certificate) {
        final CertificateDto dto = new CertificateDto();
        dto.setId(certificate.getId());
        dto.setSubjectDN(certificate.getSubjectDN());
        dto.setContextId(certificate.getContextId());
        dto.setSerialNumber(certificate.getSerialNumber());
        dto.setIssuerDN(certificate.getIssuerDN());
        dto.setData(certificate.getData());
        return dto;
    }

    @Override
    protected CertificateRepository getRepository() {
        return certificateRepository;
    }

    @Override
    protected String getObjectName() {
        return "certificate";
    }

    @Override
    protected Class<Certificate> getEntityClass() {
        return Certificate.class;
    }
}
