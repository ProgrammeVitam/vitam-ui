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

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import java.io.Serializable;
import java.util.Map;

/**
 * Transient session ticket factory whose ticket ids do not carry the name of the host running CAS.
 * <p>
 * The CAS default factory builds its ids with a {@code HostNameBasedUniqueTicketIdGenerator} without suffix, which
 * falls back on the real host name (and ignores {@code cas.host.name}). The id of such a ticket is the {@code pswdrst}
 * parameter of the password reset URL sent by email, which would then expose the name of the VM.
 * <p>
 * The ids built here carry no suffix, as before CAS 7.3.
 */
public class HostAgnosticTransientSessionTicketFactory extends DefaultTransientSessionTicketFactory {

    private final UniqueTicketIdGenerator ticketIdGenerator = new DefaultUniqueTicketIdGenerator(
        UniqueTicketIdGenerator.TICKET_SIZE,
        StringUtils.EMPTY
    );

    /**
     * @param expirationPolicyBuilder the expiration policy builder of the tickets.
     */
    public HostAgnosticTransientSessionTicketFactory(
        final ExpirationPolicyBuilder<TransientSessionTicket> expirationPolicyBuilder
    ) {
        super(expirationPolicyBuilder);
    }

    @Override
    public TransientSessionTicket create(final Service service, final Map<String, Serializable> properties)
        throws Throwable {
        return create(ticketIdGenerator.getNewTicketId(TransientSessionTicket.PREFIX), service, properties);
    }
}
