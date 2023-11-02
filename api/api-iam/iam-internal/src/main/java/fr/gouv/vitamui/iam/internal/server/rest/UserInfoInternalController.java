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
package fr.gouv.vitamui.iam.internal.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInfoInternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.Optional;

/**
 * The controller to check existence, create, read, update and delete the users.
 */
@RestController
@RequestMapping(RestApi.V1_USERS_INFO_URL)
@Getter
@Setter
public class UserInfoInternalController implements CrudController<UserInfoDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserInfoInternalController.class);

    private UserInfoInternalService userInfoInternalService;

    @Autowired
    public UserInfoInternalController(final UserInfoInternalService userInfoInternalService) {
        this.userInfoInternalService = userInfoInternalService;
    }


    @Override
    public ResponseEntity<Void> checkExist(final String criteria) {
        throw new NotImplementedException("checkExist not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping
    public UserInfoDto create(final @Valid @RequestBody UserInfoDto userInfoDto) throws InvalidParseOperationException,
        PreconditionFailedException{
        SanityChecker.sanitizeCriteria(userInfoDto);
        LOGGER.debug("Create {}", userInfoDto);
        return userInfoInternalService.create(userInfoDto);
    }

    @Override
    public UserInfoDto update(final String id, final UserInfoDto dto) {
        throw new NotImplementedException("update not supported");
    }


    /**
     * GetOne with criteria, item id.
     *
     * @param id
     * @param criteria
     * @return
     */
    @GetMapping(CommonConstants.PATH_ID)
    public UserInfoDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> criteria)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.sanitizeCriteria(criteria);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Get {} criteria={}", id, criteria);
        return userInfoInternalService.getOne(id, Optional.empty());
    }

    /**
     * Get user info for  current user .
     *
     * @return
     */
    @GetMapping(CommonConstants.PATH_ME)
    public UserInfoDto getMe() {
        LOGGER.debug("getMe {}");
        return userInfoInternalService.getMe();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    public UserInfoDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(partialDto);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");
        return userInfoInternalService.patch(partialDto);
    }

    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public JsonNode findHistoryById(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for user info with id :{}", id);
        return userInfoInternalService.findHistoryById(id);
    }

}
