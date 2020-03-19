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
package fr.gouv.vitamui.cas.ticket;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.*;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.factory.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitamui.commons.api.CommonConstants.*;

/**
 * Dynamic TGT factory.
 *
 *
 */
public class DynamicTicketGrantingTicketFactory extends DefaultTicketGrantingTicketFactory {

    @Autowired
    private Utils utils;

    public DynamicTicketGrantingTicketFactory(final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator,
                                              final ExpirationPolicyBuilder<TicketGrantingTicket> ticketGrantingTicketExpirationPolicy,
                                              final CipherExecutor<Serializable, String> cipherExecutor) {
        super(ticketGrantingTicketUniqueTicketIdGenerator, ticketGrantingTicketExpirationPolicy, cipherExecutor);
    }

    @Override
    protected <T extends TicketGrantingTicket> T produceTicket(final Authentication authentication,
                                                               final String tgtId, final Class<T> clazz) {
        final Principal principal = authentication.getPrincipal();
        final Map<String, List<Object>> attributes = principal.getAttributes();
        final String superUser = (String) utils.getAttributeValue(attributes, SUPER_USER_ATTRIBUTE);
        final UserTypeEnum type = (UserTypeEnum) utils.getAttributeValue(attributes, TYPE_ATTRIBUTE);
        if (superUser != null && type == UserTypeEnum.GENERIC) {
            return (T) new TicketGrantingTicketImpl(
                tgtId, authentication, new HardTimeoutExpirationPolicy(170 * 60));
        } else {
            return super.produceTicket(authentication, tgtId, clazz);
        }
    }
}
