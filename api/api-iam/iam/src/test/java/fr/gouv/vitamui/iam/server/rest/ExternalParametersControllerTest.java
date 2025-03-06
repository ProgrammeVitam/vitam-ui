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
package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.enums.Application;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.externalParameters.converter.ExternalParametersConverter;
import fr.gouv.vitamui.iam.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ExternalParametersController}.
 *
 *
 */
public class ExternalParametersControllerTest {

    private ExternalParametersController controller;

    private ExternalParametersService externalParametersService;

    @Mock
    private ExternalParametersRepository externalParametersRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private ExternalParametersConverter externalParametersConverter;

    @Mock
    private SecurityService securityService;

    @Mock
    private IamLogbookService iamLogbookService;

    private static final String PARAMETER_ID = "1";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(externalParametersConverter.convertDtoToEntity(ArgumentMatchers.any())).thenCallRealMethod();
        Mockito.when(externalParametersConverter.convertEntityToDto(ArgumentMatchers.any())).thenCallRealMethod();

        externalParametersService = new ExternalParametersService(
            sequenceGeneratorService,
            externalParametersRepository,
            externalParametersConverter,
            securityService,
            iamLogbookService
        );

        controller = new ExternalParametersController(externalParametersService);
    }

    @Test
    public void testGetMyExternalParameters() {
        ProfileDto profile = new ProfileDto();
        profile.setApplicationName(Application.EXTERNAL_PARAMS.toString());
        profile.setExternalParamId(PARAMETER_ID);
        profile.setTenantIdentifier(1);
        List<ProfileDto> profiles = new ArrayList<ProfileDto>();
        profiles.add(profile);
        GroupDto group = new GroupDto();
        group.setProfiles(profiles);

        AuthUserDto user = new AuthUserDto();
        user.setProfileGroup(group);

        ExternalParameters result = new ExternalParameters();
        result.setId(PARAMETER_ID);

        when(securityService.getUser()).thenReturn(user);
        when(securityService.getTenantIdentifier()).thenReturn(1);
        when(externalParametersRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.of(result));
        Map<String, String> callResult = controller.getMyExternalParameters();

        assertNotNull(callResult);
    }
}
