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
package fr.gouv.vitamui.ui.commons.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.ui.commons.service.ApplicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "applications")
@ResponseBody
@RequestMapping("${ui-prefix}/ui/applications")
public class ApplicationController extends AbstractUiRestController {

    private final ApplicationService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationController.class);

    @Autowired
    public ApplicationController(final ApplicationService service) {
        this.service = service;
    }

    /**
     * Return all applications and categories of VITAMUI
     *
     * @return Map
     */
    @ApiOperation(value = "Return config about applications and categories")
    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> getApplications(@RequestParam(defaultValue = "true") final boolean filterApp) {
        LOGGER.debug("getApplications");
        return service.getApplications(buildUiHttpContext(), filterApp);
    }

    /**
     * Return configuration informations.
     * @return MapwString, String>
     */
    @ApiOperation(value = "Get Server Configuration")
    @GetMapping
    @RequestMapping(method = RequestMethod.GET, value = "/conf")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getConfiguration() {
        LOGGER.info("Get configuration");
        return service.getConf();
    }

    /**
     * Return asset file as base64 data
     * @param fileName the file to get from assets
     * @return the file as base64 string
     */
    @ApiOperation(value = "Get Asset File")
    @GetMapping
    @RequestMapping(method = RequestMethod.GET, value = "/asset")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getAsset(@RequestParam() final String fileName) {
        LOGGER.info("Get Asset {}", fileName);
        Map<String, Object> file = new HashMap<>();
        file.put(fileName, service.getBase64Asset(fileName));
        return file;
    }
}
