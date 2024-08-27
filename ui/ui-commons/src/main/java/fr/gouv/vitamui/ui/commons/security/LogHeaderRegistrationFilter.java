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
package fr.gouv.vitamui.ui.commons.security;

import fr.gouv.vitamui.commons.api.CommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

/**
 * Filter allowing to register information from the original request into <code>MDC</code>.
 *
 *
 */
public class LogHeaderRegistrationFilter extends GenericFilterBean {

    /**
     * Method allowing to register an header from the request to the <code>MDC</code>.
     * @param request The received request.
     * @param header Header to register.
     */
    protected void registerHeader(final HttpServletRequest request, final String header) {
        final Optional<String> result = extractHeader(request, header).filter(StringUtils::isNotEmpty);
        result.ifPresent(value -> MDC.put(header, value));
    }

    /**
     * Method allowing to extract an header from the request to the <code>MDC</code>.
     * @param request The received request.
     * @param header Header to register.
     * @return The value of the header, if it exists.
     */
    protected Optional<String> extractHeader(final HttpServletRequest request, final String header) {
        return Optional.ofNullable(request.getHeader(header));
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        registerHeader(httpServletRequest, CommonConstants.X_REQUEST_ID_HEADER);
        registerHeader(httpServletRequest, CommonConstants.X_APPLICATION_ID_HEADER);
        chain.doFilter(request, response);
    }
}