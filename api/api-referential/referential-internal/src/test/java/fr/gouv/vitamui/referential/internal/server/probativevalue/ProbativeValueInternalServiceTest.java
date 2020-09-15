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
package fr.gouv.vitamui.referential.internal.server.probativevalue;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.referential.common.service.VitamBatchReportService;

public class ProbativeValueInternalServiceTest extends AbstractServerIdentityBuilder {

	private ProbativeValueInternalService probativeValueInternalService;

	@Mock
	private VitamBatchReportService vitamProbativeValueService;

	@Mock
	private UnitService unitService;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		probativeValueInternalService = new ProbativeValueInternalService(vitamProbativeValueService, unitService);
	}

	@Test
	public void shoudl_generate_report_on_probativereport() throws JsonParseException, JsonMappingException,
			VitamClientException, IOException, InvalidParseOperationException {
		File workspace = this.folder.newFolder();
		when(vitamProbativeValueService.downloadBatchReport(any(), any()))
				.thenReturn(buildVitamProbativeReport("data/provative_report_WARNING.json"));
		when(unitService.getByIdIn(any(), any()))
				.thenReturn(buildVitamUISearchResponseDto("data/vitam_units_response.json"));
		when(unitService.findObjectMetadataById(any(), any()))
				.thenReturn(buildGotMetadataResponse("data/vitam_got_metadatas_response.json"));
		VitamContext vitamContext = new VitamContext(0);
		String operationId = "Test";

		File zip = new File(workspace.getAbsolutePath(), operationId + ".zip");
		FileOutputStream zipOutputStream = new FileOutputStream(zip);
		probativeValueInternalService.exportReport(vitamContext, operationId, workspace.getAbsolutePath(),
				zipOutputStream);
		zipOutputStream.close();

		assertTrue(zip.exists());
		FileSystem zipFs = FileSystems.newFileSystem(zip.toPath(), null);
		Path zipJsonFile = zipFs.getPath(operationId + ".json");
		assertTrue(Files.exists(zipJsonFile));
		Path zipPdfFile = zipFs.getPath(operationId + ".pdf");
		assertTrue(Files.exists(zipPdfFile));

		File json = new File(workspace.getAbsolutePath(), operationId + ".json");
		assertTrue(json.exists());
		File pdf = new File(workspace.getAbsolutePath(), operationId + ".pdf");
		assertTrue(pdf.exists());
	}

	@Test
	public void shoudl_generate_report_on_probativereport_multiple_entries() throws JsonParseException,
			JsonMappingException, VitamClientException, IOException, InvalidParseOperationException {
		File workspace = this.folder.newFolder();
		when(vitamProbativeValueService.downloadBatchReport(any(), any()))
				.thenReturn(buildVitamProbativeReport("data/provative_report_WARNING_multiple_entries.json"));
		when(unitService.getByIdIn(any(), any()))
				.thenReturn(buildVitamUISearchResponseDto("data/vitam_units_response_multiple_entries.json"));
		when(unitService.findObjectMetadataById(eq("aeaqaaaaaahc23qyabnlmalrcf6o2pyaaaaq"), any()))
				.thenReturn(buildGotMetadataResponse("data/vitam_got_metadatas_response_multiple_entries_1.json"));
		when(unitService.findObjectMetadataById(eq("aeaqaaaaaahc23qyabnlmalrcf6o2tyaaaba"), any()))
				.thenReturn(buildGotMetadataResponse("data/vitam_got_metadatas_response_multiple_entries_2.json"));
		when(unitService.findObjectMetadataById(eq("aeaqaaaaaahmnykxabnagalrcg7u3kiaaaba"), any()))
				.thenReturn(buildGotMetadataResponse("data/vitam_got_metadatas_response_multiple_entries_3.json"));
		when(unitService.findObjectMetadataById(eq("aeaqaaaaaahmnykxabnagalrcg7u3laaaaba"), any()))
				.thenReturn(buildGotMetadataResponse("data/vitam_got_metadatas_response_multiple_entries_4.json"));
		VitamContext vitamContext = new VitamContext(0);
		String operationId = "Test_LONG";

		File zip = new File(workspace.getAbsolutePath(), operationId + ".zip");
		FileOutputStream zipOutputStream = new FileOutputStream(zip);
		probativeValueInternalService.exportReport(vitamContext, operationId, workspace.getAbsolutePath(),
				zipOutputStream);
		zipOutputStream.close();

		assertTrue(zip.exists());
		FileSystem zipFs = FileSystems.newFileSystem(zip.toPath(), null);
		Path zipJsonFile = zipFs.getPath(operationId + ".json");
		assertTrue(Files.exists(zipJsonFile));
		Path zipPdfFile = zipFs.getPath(operationId + ".pdf");
		assertTrue(Files.exists(zipPdfFile));

		File json = new File(workspace.getAbsolutePath(), operationId + ".json");
		assertTrue(json.exists());
		File pdf = new File(workspace.getAbsolutePath(), operationId + ".pdf");
		assertTrue(pdf.exists());
	}

	@Test
	public void shoudl_generate_report_on_probativereport_ko() throws JsonParseException, JsonMappingException,
			VitamClientException, IOException, InvalidParseOperationException {
		File workspace = this.folder.newFolder();
		when(vitamProbativeValueService.downloadBatchReport(any(), any()))
				.thenReturn(buildVitamProbativeReport("data/provative_report_KO.json"));
		when(unitService.getByIdIn(any(), any()))
				.thenReturn(buildVitamUISearchResponseDto("data/vitam_units_response_ko.json"));
		when(unitService.findObjectMetadataById(any(), any()))
				.thenReturn(buildGotMetadataResponse("data/vitam_got_metadatas_response_ko.json"));
		VitamContext vitamContext = new VitamContext(0);
		String operationId = "Test_KO";

		File zip = new File(workspace.getAbsolutePath(), operationId + ".zip");
		FileOutputStream zipOutputStream = new FileOutputStream(zip);
		probativeValueInternalService.exportReport(vitamContext, operationId, workspace.getAbsolutePath(),
				zipOutputStream);
		zipOutputStream.close();

		assertTrue(zip.exists());
		FileSystem zipFs = FileSystems.newFileSystem(zip.toPath(), null);
		Path zipJsonFile = zipFs.getPath(operationId + ".json");
		assertTrue(Files.exists(zipJsonFile));
		Path zipPdfFile = zipFs.getPath(operationId + ".pdf");
		assertTrue(Files.exists(zipPdfFile));

		File json = new File(workspace.getAbsolutePath(), operationId + ".json");
		assertTrue(json.exists());
		File pdf = new File(workspace.getAbsolutePath(), operationId + ".pdf");
		assertTrue(pdf.exists());
	}

	private InputStream buildVitamProbativeReport(String filename)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		InputStream inputStream = ProbativeValueInternalServiceTest.class.getClassLoader()
				.getResourceAsStream(filename);
		return inputStream;
	}

	private VitamUISearchResponseDto buildVitamUISearchResponseDto(String filename)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		InputStream inputStream = ProbativeValueInternalServiceTest.class.getClassLoader()
				.getResourceAsStream(filename);
		return objectMapper.readValue(ByteStreams.toByteArray(inputStream), VitamUISearchResponseDto.class);
	}

	private RequestResponse<JsonNode> buildGotMetadataResponse(String filename)
			throws JsonParseException, JsonMappingException, IOException, InvalidParseOperationException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		InputStream inputStream = ProbativeValueInternalServiceTest.class.getClassLoader()
				.getResourceAsStream(filename);
		return RequestResponseOK
				.getFromJsonNode(objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class));
	}

}
