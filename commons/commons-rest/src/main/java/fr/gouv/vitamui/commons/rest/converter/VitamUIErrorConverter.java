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
package fr.gouv.vitamui.commons.rest.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.exception.TooManyRequestsException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.dto.VitamUIError;

/**
 *
 * VITAMUI Error to VITAMUI Exception.
 *
 *
 */
public class VitamUIErrorConverter implements Converter<VitamUIError, VitamUIException> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamUIErrorConverter.class);

    @Override
    public VitamUIException convert(final VitamUIError source) {
        VitamUIException exception = null;
        final HttpStatus status = HttpStatus.valueOf(source.getStatus());
        switch (status) {
            case INTERNAL_SERVER_ERROR :
                exception = new InternalServerException(source.getMessage(), source.getError());
                break;
            case BAD_REQUEST :
                exception = new InvalidFormatException(source.getMessage(), source.getError(), source.getArgs());
                break;
            case NOT_FOUND :
                exception = new NotFoundException(source.getMessage(), source.getError());
                break;
            case FORBIDDEN :
                exception = new ForbiddenException(source.getMessage(), source.getError());
                break;
            case UNAUTHORIZED :
                exception = new InvalidAuthenticationException(source.getMessage(), source.getError());
                break;
            case NOT_IMPLEMENTED :
                exception = new NotImplementedException(source.getMessage(), source.getError());
                break;
            case TOO_MANY_REQUESTS :
                exception = new TooManyRequestsException(source.getMessage(), source.getError());
                break;
            case SERVICE_UNAVAILABLE :
                exception = new UnavailableServiceException(source.getMessage(), source.getError());
                break;
            case CONFLICT :
                exception = new ConflictException(source.getMessage(), source.getError());
                break;
            case PRECONDITION_FAILED :
                exception = new PreconditionFailedException(source.getMessage(), source.getError());
                break;
            default :
                LOGGER.error("Error Source {}", source);
                exception = new ApplicationServerException(source.getMessage());
                break;
        }
        return exception;
    }

}
