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
package fr.gouv.vitamui.iam.external.server.rest;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.UserInfoExternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping(RestApi.V1_USERS_INFO_URL)
@Getter
@Setter
public class UserInfoExternalController implements CrudController<UserInfoDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserInfoExternalController.class);

    private final UserInfoExternalService userInfoExternalService;

    @Autowired
    public UserInfoExternalController(final UserInfoExternalService userInfoExternalService) {
        this.userInfoExternalService = userInfoExternalService;
    }


    @Override
    @PostMapping
    @Secured(ServicesData.ROLE_CREATE_USER_INFOS)
    public UserInfoDto create(final @Valid @RequestBody UserInfoDto dto) throws InvalidParseOperationException,
        PreconditionFailedException {

        SanityChecker.sanitizeCriteria(dto);
        LOGGER.debug("Create {}", dto);
        return userInfoExternalService.create(dto);
    }

    @Override
    public UserInfoDto update(final String id, final UserInfoDto dto) {
        throw new NotImplementedException("update not supported");
    }

    @Override
    public void delete(final String id) {
        throw new NotImplementedException("delete not supported");
    }

    @Override
    public Collection<UserInfoDto> getAll(final Optional<String> criteria) {
        throw new NotImplementedException("getAll not supported");
    }

    @Override
    public ResponseEntity<Void> checkExist(final String criteria) {
        throw new NotImplementedException("checkExist not supported");
    }

    @Override
    @GetMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_GET_USER_INFOS)
    public UserInfoDto getOne(final @PathVariable("id") String id) throws InvalidParseOperationException,
        PreconditionFailedException {

        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Get {}", id);
        return userInfoExternalService.getOne(id);
    }

    /**
     * Get user info for  current user .
     *
     * @return
     */
    @GetMapping(CommonConstants.PATH_ME)
    public UserInfoDto getMe() {
        LOGGER.debug("getMe {}");
        return userInfoExternalService.getMe();
    }

    @PatchMapping(CommonConstants.PATH_ME)
    public UserInfoDto patchMe(@RequestBody final Map<String, Object> partialDto)
        throws InvalidParseOperationException, PreconditionFailedException {

        SanityChecker.sanitizeCriteria(partialDto);
        LOGGER.debug("Patch me with {}", partialDto);
        return userInfoExternalService.patchMe(partialDto);
    }

    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_USER_INFOS)
    public UserInfoDto patch(final @PathVariable("id") String id, final @RequestBody Map<String, Object> partialDto)
        throws InvalidParseOperationException, PreconditionFailedException {

        SanityChecker.sanitizeCriteria(partialDto);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Patch User {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "Unable to patch user : the DTO id must match the path id");
        return userInfoExternalService.patch(partialDto);
    }

    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable("id") String id) {
        LOGGER.debug("get logbook for user info with id :{}", id);
        return userInfoExternalService.findHistoryById(id);
    }

}
