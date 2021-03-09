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
package fr.gouv.vitamui.iam.internal.server.application.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.TenantInformationDto;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.CriteriaUtils;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.server.application.converter.ApplicationConverter;
import fr.gouv.vitamui.iam.internal.server.application.dao.ApplicationRepository;
import fr.gouv.vitamui.iam.internal.server.application.domain.Application;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the applications.
 *
 *
 */
@Getter
@Setter
public class ApplicationInternalService extends VitamUICrudService<ApplicationDto, Application> {
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationInternalService.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationConverter applicationConverter;
    private final InternalSecurityService internalSecurityService;

    @Autowired
    public ApplicationInternalService(final CustomSequenceRepository sequenceRepository, final ApplicationRepository applicationRepository,
        final ApplicationConverter applicationConverter, final InternalSecurityService internalSecurityService) {
        super(sequenceRepository);
        this.applicationRepository = applicationRepository;
        this.applicationConverter = applicationConverter;
        this.internalSecurityService = internalSecurityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ApplicationDto> getAll(Optional<String> criteria, Optional<String> embedded) {

        Boolean filterApp = true;

        if (criteria.isPresent()) {
            final QueryDto queryDto = QueryDto.fromJson(criteria);
            List<Criterion> criterions = queryDto.getCriterionList();
            Optional<Criterion> findFilterApp = criterions.stream()
                    .filter(criterion -> "filterApp".equals(criterion.getKey()))
                    .findFirst();

            if (findFilterApp.isPresent()) {
                Criterion filterAppCriterion = findFilterApp.get();
                filterApp = (Boolean) filterAppCriterion.getValue();

                criterions.remove(filterAppCriterion);
                criteria = Optional.of(CriteriaUtils.toJson(queryDto));
            }
        }

        List<ApplicationDto> apps = super.getAll(criteria, embedded);
        if (filterApp) {
            filterApp(apps);
        }
        return apps;
    }


    /**
     * Filter application for logger user permission
     * @param apps initial app list
     */
    private void filterApp(final Collection<ApplicationDto> apps) {
        final AuthUserDto user = internalSecurityService.getUser();
        if (user == null) {
            throw new UnAuthorizedException("No authenticated user");
        }
        List<TenantInformationDto> tenantsByApp = user.getTenantsByApp();
        if (CollectionUtils.isEmpty(user.getTenantsByApp())) {
            tenantsByApp = new ArrayList<>();
        }
        final Collection<String> filter = tenantsByApp.stream().map(p -> p.getName()).collect(Collectors.toList());
        final Predicate<ApplicationDto> predicate =
                a -> !filter.contains(a.getIdentifier());
        apps.removeIf(predicate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ApplicationRepository getRepository() {
        return applicationRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<Application> getEntityClass() {
        return Application.class;
    }

    @Override
    protected Converter<ApplicationDto, Application> getConverter() {
        return applicationConverter;
    }

}
