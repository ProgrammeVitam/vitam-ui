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

package fr.gouv.vitamui.commons.api.identity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Defines minimal common configurations for server identity configuration properties.
 * Server Identity containing ServerName, ServerRole, Global PlatformId
 * Where name, role and platformID comes from a configuration file for instance.
 *
 * Inspired from fr.gouv.vitam.common.ServerIdentity
 */

@ToString
@Component
@ConfigurationProperties(prefix = "server-identity", ignoreUnknownFields = false)
public class ServerIdentityConfiguration {

    private final StringBuilder preMessage = new StringBuilder();

    @Getter
    private int globalPlatformId;

    @Getter
    private int identityServerId;

    @Getter
    private int identitySiteId;

    @Getter
    private String identityName;

    @Getter
    private String identityRole;

    private String preMessageString;

    /**
     * The name of the Server.
     *
     * @param name the name of the Server to set
     * @throws IllegalArgumentException
     */
    public void setIdentityName(final String name) {
        ParameterChecker.checkParameter("Name", name);
        identityName = name;
        initializeCommentFormat();
    }

    /**
     * The role of the Server.
     *
     * @param role the role of the Server to set
     */
    public void setIdentityRole(final String role) {
        ParameterChecker.checkParameter("Role", role);
        identityRole = role;
        initializeCommentFormat();
    }

    /**
     * Site Id.
     *
     * @param siteId the siteId to set
     */
    public void setIdentitySiteId(final int siteId) {
        ParameterChecker.checkValue("siteID", siteId, 0);
        identitySiteId = siteId;
    }

    /**
     * The PlatformId is a unique name per site.
     *
     * @param serverId the platformId of the VitamUI Platform to set
     */
    public void setIdentityServerId(final int serverId) {
        ParameterChecker.checkValue("server", serverId, 0);
        identityServerId = serverId;
        calculateGlobalPlatformId();
    }

    /**
     * @return the Logger Message Prepend.
     */
    @JsonIgnore
    public final String getLoggerMessagePrepend() {
        return preMessageString;
    }

    /**
     * Calculate GlobaPlatformId.
     */
    private final void calculateGlobalPlatformId() {
        globalPlatformId = ((getIdentityServerId() & 0x0F) << 27) + (getIdentityServerId() & 0x07FFFFFF);
        initializeCommentFormat();
    }

    /**
     * Initialize after each configuration change the Logger pre-message.
     */
    private void initializeCommentFormat() {
        preMessage.setLength(0);
        preMessage
            .append('[')
            .append(getIdentityName())
            .append(':')
            .append(getIdentityRole())
            .append(':')
            .append(getGlobalPlatformId())
            .append("] ");
        preMessageString = preMessage.toString();
    }
}
