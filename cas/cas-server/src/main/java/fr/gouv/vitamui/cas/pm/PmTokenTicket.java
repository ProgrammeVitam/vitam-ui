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
package fr.gouv.vitamui.cas.pm;

import java.time.ZonedDateTime;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;

/**
 * Specific ticket for the password management token.
 *
 *
 */
public class PmTokenTicket implements Ticket, TicketState {

    /**
     *
     */
    private static final long serialVersionUID = 4524446119459217215L;

    public static final String PREFIX = "PM";

    private final String id;

    private final ZonedDateTime creationTime;

    private final String user;

    private final ExpirationPolicy expirationPolicy;

    public PmTokenTicket(final String id, final String user, final int ttlInMinutes) {
        this.id = id;
        this.user = user;
        creationTime = ZonedDateTime.now();
        expirationPolicy = new HardTimeoutExpirationPolicy(ttlInMinutes * 60l);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isExpired() {
        return expirationPolicy.isExpired(this);
    }

    @Override
    public TicketGrantingTicket getTicketGrantingTicket() {
        return null;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public int getCountOfUses() {
        return 0;
    }

    @Override
    public ExpirationPolicy getExpirationPolicy() {
        return expirationPolicy;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public void markTicketExpired() {
        //do nothing
    }

    @Override
    public int compareTo(final Ticket t) {
        return creationTime.compareTo(t.getCreationTime());
    }

    public String getUser() {
        return user;
    }

    @Override
    public ZonedDateTime getLastTimeUsed() {
        return getCreationTime();
    }

    @Override
    public ZonedDateTime getPreviousTimeUsed() {
        return getCreationTime();
    }

    @Override
    public Authentication getAuthentication() {
        return null;
    }

    @Override
    public void update() {
        //do nothing
    }
}
