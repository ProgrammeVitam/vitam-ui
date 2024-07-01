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
package fr.gouv.vitamui.identity.service;

import fr.gouv.vitamui.commons.security.client.logout.CasLogoutUrl;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import fr.gouv.vitamui.ui.commons.service.ApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Service(value = "applicationService")
public class UiIdentityApplicationService extends ApplicationService {

    public static final String MAX_STREET_LENGTH = "MAX_STREET_LENGTH";
    public static final String PORTAL_TITLE = "PORTAL_TITLE";
    public static final String PORTAL_MESSAGE = "PORTAL_MESSAGE";

    @Value("${address.max-street-length}")
    @NotNull
    private Integer maxStreetLength;

    @Value("${portal.title}")
    @NotNull
    private String portalTitle;

    @Value("${portal.message}")
    @NotNull
    private String portalMessage;

    public UiIdentityApplicationService(
        final UIProperties properties,
        final CasLogoutUrl casLogoutUrl,
        final IamExternalRestClientFactory factory,
        final BuildProperties buildProperties
    ) {
        super(properties, casLogoutUrl, factory, buildProperties);
    }

    @Override
    public Map<String, Object> getConf() {
        final Map<String, Object> configurationData = new HashMap<>(super.getConf());
        configurationData.put(MAX_STREET_LENGTH, maxStreetLength);
        configurationData.put(PORTAL_TITLE, portalTitle);
        configurationData.put(PORTAL_MESSAGE, portalMessage);
        return configurationData;
    }
}
