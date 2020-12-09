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
package fr.gouv.vitamui.referential.common.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.FileFormatModel;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.referential.common.dto.FileFormatResponseDto;
import fr.gouv.vitamui.referential.common.dto.xml.fileformat.FileFormat;
import fr.gouv.vitamui.referential.common.dto.xml.fileformat.FileFormatCollection;
import fr.gouv.vitamui.referential.common.dto.xml.fileformat.FileFormatXMLRootDto;

public class VitamFileFormatService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamFileFormatService.class);

    private final AdminExternalClient adminExternalClient;

    private final AccessExternalClient accessExternalClient;

    private ObjectMapper objectMapper;

    @Autowired
    public VitamFileFormatService(AdminExternalClient adminExternalClient, ObjectMapper objectMapper, AccessExternalClient accessExternalClient) {
        this.adminExternalClient = adminExternalClient;
        this.objectMapper = objectMapper;
        this.accessExternalClient = accessExternalClient;
    }

    public RequestResponse<FileFormatModel> findFileFormats(final VitamContext vitamContext, final JsonNode select) throws VitamClientException {
        LOGGER.info("File Formats EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        final RequestResponse<FileFormatModel> response = adminExternalClient.findFormats(vitamContext, select);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse<FileFormatModel> findFileFormatById(final VitamContext vitamContext, final String contractId) throws VitamClientException {
        LOGGER.info("File Format EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        final RequestResponse<FileFormatModel> response = adminExternalClient.findFormatById(vitamContext, contractId);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    /**
     * Ignore vitam internal fields (#id, #version, #tenant) and FileFormat non mutable fields (Identifier, Name)
      */
    private void patchFields(FileFormatModel fileFormatToPatch, FileFormatModel fieldsToApply) {
        if (fieldsToApply.getMimeType() != null) {
            fileFormatToPatch.setMimeType(fieldsToApply.getMimeType());
        }

        if (fieldsToApply.getVersion() != null) {
            fileFormatToPatch.setVersion(fieldsToApply.getVersion());
        }

        if (fieldsToApply.getExtensions() != null) {
            fileFormatToPatch.setExtensions(fieldsToApply.getExtensions());
        }
    }

    public RequestResponse<?> patchFileFormat(final VitamContext vitamContext, final String id, FileFormatModel patchFileFormat)
            throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException, JAXBException {

        RequestResponse<FileFormatModel> requestResponse = findFileFormats(vitamContext, new Select().getFinalSelect());
        final List<FileFormatModel> actualFileFormats = objectMapper
                .treeToValue(requestResponse.toJsonNode(), FileFormatResponseDto.class).getResults();

        actualFileFormats.stream()
            .filter( fileFormat -> id.equals(fileFormat.getPuid()) )
            .forEach( fileFormat -> this.patchFields(fileFormat, patchFileFormat) );

        return importFileFormats(vitamContext, actualFileFormats);
    }

    public RequestResponse<?> deleteFileFormat(final VitamContext vitamContext, final String id)
            throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException, JAXBException {

        LOGGER.info("Delete File Format EvIdAppSession : {} " , vitamContext.getApplicationSessionId());

        RequestResponse<FileFormatModel> requestResponse = findFileFormats(vitamContext, new Select().getFinalSelect());
        final List<FileFormatModel> actualFileFormats = objectMapper
                .treeToValue(requestResponse.toJsonNode(), FileFormatResponseDto.class).getResults();

        return importFileFormats(vitamContext, actualFileFormats.stream()
                .filter( fileFormat -> !id.equals(fileFormat.getPuid()) )
                .collect(Collectors.toList()));
    }

    public RequestResponse<?> create(final VitamContext vitamContext, FileFormatModel newFileFormat)
            throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException, JAXBException {

        LOGGER.info("Create File Format EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        RequestResponse<FileFormatModel> requestResponse = findFileFormats(vitamContext, new Select().getFinalSelect());
        final List<FileFormatModel> actualFileFormats = objectMapper
                .treeToValue(requestResponse.toJsonNode(), FileFormatResponseDto.class).getResults();

        LOGGER.debug("Before Add List: {}", actualFileFormats);

        actualFileFormats.add(newFileFormat);

        LOGGER.debug("After Add List: {}", actualFileFormats);

        return importFileFormats(vitamContext, actualFileFormats);
    }
    
    public RequestResponse<?> importFileFormats(VitamContext vitamContext, String fileName, MultipartFile file) 
        	throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
        	LOGGER.debug("Import file format file {}", fileName);
        	return adminExternalClient.createFormats(vitamContext, file.getInputStream(), fileName);
    }

    private RequestResponse<?> importFileFormats(final VitamContext vitamContext, final List<FileFormatModel> fileFormatModels)
            throws InvalidParseOperationException, AccessExternalClientException, IOException, JAXBException {
        try (ByteArrayInputStream byteArrayInputStream = serializeFileFormats(fileFormatModels)) {
            return adminExternalClient.createFormats(vitamContext, byteArrayInputStream, "FileFormats.json");
        }
    }

    private ByteArrayInputStream serializeFileFormats(final List<FileFormatModel> fileFormatDtos) throws IOException, JAXBException {
        final FileFormatXMLRootDto fileFormatXMLDto = convertDtosToXmlDto(fileFormatDtos);
        LOGGER.debug("The dto for creation fileFormats, sent to Vitam {}", fileFormatXMLDto);

        // TODO: Need a function that transform a DTO into an XML !

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            JAXBContext jaxbContext = JAXBContext.newInstance(fileFormatXMLDto.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(fileFormatXMLDto, byteArrayOutputStream);

            LOGGER.debug("XML FileFormat: {}", byteArrayOutputStream.toString());

            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    private FileFormatXMLRootDto convertDtosToXmlDto(List<FileFormatModel> inputFileFormats) {
        /* Process FileFormats */
        Map<String, Integer> puidToId = new HashMap<>();
        Map<String, List<String>> hasPriorityOverFileFormat = new HashMap<>();

        List<FileFormat> fileFormats = new ArrayList<>();
        Integer formatIdentifier = 0;
        for (FileFormatModel inputFileFormat: inputFileFormats) {
            FileFormat fileFormat = new FileFormat();
            fileFormat.setId(formatIdentifier);
            fileFormat.setName(inputFileFormat.getName());
            fileFormat.setPuid(inputFileFormat.getPuid());
            fileFormat.setExtension(inputFileFormat.getExtensions());
            fileFormat.setMimeType(inputFileFormat.getMimeType());
            fileFormat.setVersion(inputFileFormat.getVersion());

            List<String> priorityOverFileFormat = inputFileFormat.getHasPriorityOverFileFormatIDs();
            if (priorityOverFileFormat != null && !priorityOverFileFormat.isEmpty()) {
                hasPriorityOverFileFormat.put(inputFileFormat.getPuid(), priorityOverFileFormat);
            } else {
                hasPriorityOverFileFormat.put(inputFileFormat.getPuid(), new ArrayList<>());
            }

            puidToId.put(inputFileFormat.getPuid(), formatIdentifier);
            formatIdentifier++;

            fileFormats.add(fileFormat);
        }

        for (FileFormat fileFormat: fileFormats) {
            List<Integer> priorityIds = new ArrayList<>();
            for ( String priorityOverFormatId: hasPriorityOverFileFormat.get(fileFormat.getPuid()) ) {
                priorityIds.add( puidToId.get(priorityOverFormatId) );
            }
            fileFormat.setHasPriorityOverFileFormatIDs(priorityIds);
        }

        /* Set root element with file formats */
        FileFormatXMLRootDto root = new FileFormatXMLRootDto();
        root.setVersion(101); // What version should we put ? Could we put nothing ?
        root.setCreatedDate(new Date()); // Date format ? Should we put string ?
        FileFormatCollection fileCollection = new FileFormatCollection();
        fileCollection.setFileFormats(fileFormats);
        root.setFileFormatCollection(fileCollection);

        return root;
    }

    /**
     * check if all conditions are Ok to create an access contract in the tenant
     * @param fileFormats
     * @return true if the format can be created, false if the ile format already exists
     */
    public boolean checkAbilityToCreateFileFormatInVitam(final List<FileFormatModel> fileFormats, VitamContext vitamContext) {

        if (fileFormats != null && !fileFormats.isEmpty()) {
            try {
                // check if tenant exist in Vitam
                final JsonNode select = new Select().getFinalSelect();
                final RequestResponse<FileFormatModel> response = findFileFormats(vitamContext, select);
                if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    throw new PreconditionFailedException("Can't create file format for the tenant : UNAUTHORIZED");
                }
                else if (response.getStatus() != HttpStatus.OK.value()) {
                    throw new UnavailableServiceException("Can't create file format for this tenant, Vitam response code : " + response.getStatus());
                }

                verifyFileFormatExistence(fileFormats, response);
            }
            catch (final VitamClientException e) {
                throw new UnavailableServiceException("Can't create access contracts for this tenant, error while calling Vitam : " + e.getMessage());
            }
            return true;
        }
        throw new BadRequestException("The body is not found");
    }

    /**
     * Check if access contract is not already created in Vitam.
     * @param checkFileFormats
     * @param vitamFileFormats
     */
    private void verifyFileFormatExistence(final List<FileFormatModel> checkFileFormats, final RequestResponse<FileFormatModel> vitamFileFormats) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final FileFormatResponseDto accessContractResponseDto = objectMapper.treeToValue(vitamFileFormats.toJsonNode(), FileFormatResponseDto.class);
            final List<String> formatsNames = checkFileFormats.stream().map(ac -> ac.getName()).collect(Collectors.toList());
            if (accessContractResponseDto.getResults().stream().anyMatch(ac -> formatsNames.contains(ac.getName()))) {
                throw new ConflictException("Can't create file format, a format with the same name already exist in Vitam");
            }
            final List<String> formatsPuids = checkFileFormats.stream().map(ac -> ac.getPuid()).collect(Collectors.toList());
            if (accessContractResponseDto.getResults().stream().anyMatch(ac -> formatsPuids.contains(ac.getPuid()))) {
                throw new ConflictException("Can't create file format, a format with the same puid already exist in Vitam");
            }
        }
        catch (final JsonProcessingException e) {
            throw new UnexpectedDataException("Can't create access contracts, Error while parsing Vitam response : " + e.getMessage());
        }
    }
}
