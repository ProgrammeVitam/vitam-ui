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
package fr.gouv.vitamui.iam.server.application.service;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.IdentifierNameDto;
import fr.gouv.vitamui.commons.api.domain.TenantInformationDto;
import fr.gouv.vitamui.commons.api.domain.configuration.TenantId;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.vitam.api.administration.ConfigurationService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.application.converter.ApplicationConverter;
import fr.gouv.vitamui.iam.server.application.dao.ApplicationRepository;
import fr.gouv.vitamui.iam.server.application.domain.Application;
import fr.gouv.vitamui.iam.server.security.AbstractResourceClientService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The service to read, create, update and delete the applications.
 *
 *
 */
@Getter
@Setter
@Service
public class ApplicationService extends AbstractResourceClientService<ApplicationDto, Application> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationConverter applicationConverter;
    private final SecurityService securityService;

    private final ConfigurationService configurationService;

    @Autowired
    public ApplicationService(
        final SequenceGeneratorService sequenceGeneratorService,
        final ApplicationRepository applicationRepository,
        final ApplicationConverter applicationConverter,
        final SecurityService securityService,
        final ConfigurationService configurationService
    ) {
        super(sequenceGeneratorService, securityService);
        this.applicationRepository = applicationRepository;
        this.applicationConverter = applicationConverter;
        this.securityService = securityService;
        this.configurationService = configurationService;
    }

    /**
     * {@inheritDoc}
     */
    public List<ApplicationDto> getAllFilteredByUser(Optional<String> criteria) {
        List<ApplicationDto> apps = super.getAll(criteria);
        return filterApp(apps); // Apps are filtered depending on user permissions
    }

    /**
     * Returns all applications names and identifiers, without filtering on user permissions (useful to have a list of all applications without leaking secured information)
     * @return unfiltered application list, but only with their identifier and name
     */
    public List<IdentifierNameDto> listNames() {
        return super.getAll(Optional.empty())
            .stream()
            .map(app -> new IdentifierNameDto(app.getIdentifier(), app.getName()))
            .toList();
    }

    public boolean isApplicationExternalIdentifierEnabled(String applicationId) {
        final TenantId tenantId = new TenantId(securityService.getTenantIdentifier());

        return configurationService
            .getPlatformConfiguration()
            .externalReferentialIdentifiersByTenant()
            .getOrDefault(tenantId, Collections.emptyList())
            .contains(applicationId);
    }

    /**
     * Filter application for logger user permission
     *
     * @param apps initial app list
     * @return filtered application list
     */
    private List<ApplicationDto> filterApp(final Collection<ApplicationDto> apps) {
        final AuthUserDto user = Optional.ofNullable(securityService.getUser()).orElseThrow(
            () -> new UnAuthorizedException("No authenticated user")
        );
        final List<TenantInformationDto> tenantsByApp = Optional.ofNullable(user.getTenantsByApp()).orElse(
            new ArrayList<>()
        );
        final Collection<String> filter = tenantsByApp.stream().map(TenantInformationDto::getName).toList();
        final Predicate<ApplicationDto> predicate = app -> filter.contains(app.getIdentifier());

        return apps.stream().filter(predicate).toList();
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

    public Map<String, String> findApplicationByIdentifier(List<String> identifiers) {
        var applications = this.applicationRepository.findAllByIdentifierIn(identifiers);
        return applications.stream().collect(Collectors.toMap(Application::getIdentifier, Application::getName));
    }

    @Override
    protected Collection<String> getAllowedKeys() {
        return Arrays.asList("identifier", "url", "_id", "category");
    }

    @Override
    protected Collection<String> getRestrictedKeys() {
        return Collections.emptyList();
    }

    @Override
    protected String getVersionApiCriteria() {
        return CRITERIA_VERSION_V1;
    }
}
