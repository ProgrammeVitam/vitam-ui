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
package fr.gouv.vitamui.commons.rest.util;

import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.CriteriaUtils;
import fr.gouv.vitamui.commons.rest.enums.ContentDispositionType;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ContentDisposition.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.util.Assert;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public final class RestUtils {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(RestUtils.class);

    private static final String HTTPS_SCHEME = "https://";

    private static final String HTTP_SCHEME = "http://";

    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static final String FORM_DATA = "form-data";

    public static final String NAME = "name";

    public static final String FILENAME = "filename";

    private RestUtils() {
        // do nothing
    }

    /**
     *
     * @param isSecure
     * @return the name of the scheme used to make a secured/unsecured http request
     */
    public static final String getScheme(final boolean isSecure) {
        return isSecure ? HTTPS_SCHEME : HTTP_SCHEME;
    }

    /**
     * Method for build entity from boolean value
     * @param exist
     * @return
     */
    public static ResponseEntity<Void> buildBooleanResponse(final boolean exist) {
        return new ResponseEntity<>(exist ? HttpStatus.OK : HttpStatus.NO_CONTENT);
    }

    /**
     * Check criteria
     * @param criteria
     */
    public static void checkCriteria(final Optional<String> criteria) {
        SanityChecker.sanitizeCriteria(criteria);
        criteria.ifPresent(c -> CriteriaUtils.checkFormat(c));
    }

    /**
     * Check criteria
     * @param criteriaDto criteria query DTO
     */
    public static void checkCriteriaDto(final Optional<QueryDto> criteriaDto) {
        criteriaDto.ifPresent(c -> CriteriaUtils.checkFormat(c));
    }

    /**
     * Build the response to get a file, based on the api response.
     * @param getFileResponse
     * @param disposition
     * @return
     * @throws IOException
     */
    public static ResponseEntity<Resource> buildFileResponse(final ResponseEntity<Resource> getFileResponse, Optional<ContentDispositionType> disposition,
            final Optional<String> filename) {
        // Sets default disposition
        if (!disposition.isPresent()) {
            disposition = Optional.of(ContentDispositionType.ATTACHMENT);
        }
        Assert.notNull(getFileResponse, "File response cannot be null");
        final BodyBuilder builder = ResponseEntity.ok();
        InputStream fileStream;
        try {
            fileStream = getFileResponse.getBody().getInputStream();
        }
        catch (final IOException e) {
            throw new InternalServerException("An error occured while opening the response stream", e);
        }
        final InputStreamResource fileStreamRessource = new InputStreamResource(fileStream);

        final HttpHeaders responseHeaders = getFileResponse.getHeaders();

        // Set the content type
        final String contentType = responseHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
        if (StringUtils.isNotBlank(contentType)) {
            builder.contentType(MediaType.parseMediaType(contentType));
        }
        else {
            LOGGER.debug("No content-type in response : {}", getFileResponse);
        }

        // Set the content disposition as inline
        final Builder contentDispositionBuilder = ContentDisposition.builder(disposition.get().getValue());
        final String contentDispositionStr = responseHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        if (filename.isPresent()) {
            contentDispositionBuilder.filename(filename.get());
        }
        else if (StringUtils.isNotBlank(contentDispositionStr)) {
            final ContentDisposition contentDisposition = ContentDisposition.parse(contentDispositionStr);
            if (StringUtils.isNotBlank(contentDisposition.getFilename())) {
                contentDispositionBuilder.filename(contentDisposition.getFilename());
            }
        }
        builder.header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionBuilder.build().toString());

        return builder.body(fileStreamRessource);
    }

    /**
     * Build the resource response, based on the resource.
     * @param disposition
     * @param resource
     * @return
     * @throws IOException
     */
    public static ResponseEntity<Resource> buildResourceResponse(final Optional<ContentDispositionType> disposition, final Resource resource,
            final Optional<String> optFileName) {

        final StringBuilder contentDisposition = new StringBuilder();
        contentDisposition.append(disposition.orElse(ContentDispositionType.ATTACHMENT).getValue() + ";");

        final String fileName = optFileName.orElse(resource.getFilename());
        contentDisposition.append("filename=" + fileName);

        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.isNotBlank(resource.getFilename())) {
            String mimeType;
            try {
                mimeType = Files.probeContentType(Paths.get(resource.getFile().getAbsolutePath()));
                mediaType = MediaType.valueOf(mimeType);
            }
            catch (final IOException | InvalidMediaTypeException e) {
                // use the default mediaType
                LOGGER.error("Cannot find mimetype for {}", resource.getFilename(), e);
            }
        }

        try {
            return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength()).contentType(mediaType).body(resource);
        }
        catch (final IOException e) {
            throw new ApplicationServerException(e.getMessage(), e);
        }
    }

    /**
     * Read {@link HttpServletRequest} and get a {@link FileItemIterator}.
     * @param factory : a factory for disk-based file items
     * @param request
     * @return
     * @throws FileUploadException
     * @throws IOException
     */
    public static FileItemIterator getFileItemIterator(final DiskFileItemFactory factory, final HttpServletRequest request)
            throws FileUploadException, IOException {
        // Configure a repository (to ensure a secure temp location is used)
        final ServletContext servletContext = request.getServletContext();
        final File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        factory.setRepository(repository);

        // Create a new file upload handler
        final ServletFileUpload upload = new ServletFileUpload(factory);
        //
        upload.setFileSizeMax(-1);
        upload.setSizeMax(-1);

        LOGGER.debug("Request: {}", request);
        LOGGER.debug("ContentLength: {}", request.getContentLength());

        return upload.getItemIterator(request);
    }

}
