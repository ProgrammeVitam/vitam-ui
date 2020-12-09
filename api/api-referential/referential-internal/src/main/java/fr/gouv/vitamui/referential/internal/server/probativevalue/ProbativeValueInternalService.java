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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.objectgroup.ObjectGroupResponse;
import fr.gouv.vitam.common.model.objectgroup.QualifiersModel;
import fr.gouv.vitam.common.model.objectgroup.VersionsModel;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ZipUtils;
import fr.gouv.vitamui.commons.utils.PdfFileGenerator;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.referential.common.export.probativevalue.dto.ItemWithLabelDto;
import fr.gouv.vitamui.referential.common.export.probativevalue.dto.ProbativeOperationDto;
import fr.gouv.vitamui.referential.common.export.probativevalue.dto.ProbativeReportDto;
import fr.gouv.vitamui.referential.common.export.probativevalue.dto.ReportEntryDto;
import fr.gouv.vitamui.referential.common.service.VitamBatchReportService;

/**
 * A service to access a probative value report.
 *
 */
@Service
public class ProbativeValueInternalService {

	private static final String TEMPLATE_PROBATIVEVALUEREPORT_ODT = "templates/probativevaluereport.ftl.odt";

	private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProbativeValueInternalService.class);

	final private VitamBatchReportService vitamBatchReportService;

	final private UnitService unitService;

	@Autowired
	ProbativeValueInternalService(VitamBatchReportService vitamBatchReportService, UnitService unitService) {
		this.vitamBatchReportService = vitamBatchReportService;
		this.unitService = unitService;
	}

	/**
	 * Export the report of a defined probative value.
	 *
	 * @param vitamContext           Context of execution
	 * @param operationId            Probative Value operation identifier
	 * @param workspaceOperationPath Workspace directory (it's the caller's
	 *                               responsibility to remove the created files)
	 * @param outputStream           Stream where the ZIP will be store (it's the
	 *                               caller's responsibility to close its stream !).
	 */
	public void exportReport(final VitamContext vitamContext, final String operationId,
			final String workspaceOperationPath, final OutputStream outputStream) {
        LOGGER.info("Export Probative Values EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
		checkWorkspacePath(workspaceOperationPath);
		getProbativeValueReportJson(vitamContext, operationId, workspaceOperationPath);
		generatePDF(vitamContext, operationId, workspaceOperationPath);
		generateZip(operationId, workspaceOperationPath, outputStream);

	}

	private void getProbativeValueReportJson(final VitamContext vitamContext, final String operationId,
			final String workspaceOperationPath) {
		try (InputStream reportStream = vitamBatchReportService.downloadBatchReport(vitamContext, operationId)) {
			File file = new File(workspaceOperationPath, operationId + ".json");
			FileUtils.copyInputStreamToFile(reportStream, file);
		} catch (VitamClientException e) {
			LOGGER.error("Error while getting probative value report from Vitam", e.getMessage());
			throw new InternalServerException("Unable to get Probative Value Report from VITAM", e);
		} catch (IOException e) {
			LOGGER.error("Error on probative value report", e.getMessage());
			throw new InternalServerException("Unable to create JSON from Probative Value Report", e);
		}

	}

	private void generatePDF(final VitamContext vitamContext, final String operationId,
			final String workspaceOperationPath) {

		try {
			File jsonReport = new File(workspaceOperationPath, operationId + ".json");
			ProbativeReportDto report = JsonHandler.getFromFile(jsonReport, ProbativeReportDto.class);
			reportEntriesConsolidation(vitamContext, report);

			File pdffile = new File(workspaceOperationPath, operationId + ".pdf");

			try (InputStream odtTemplate = getClass().getClassLoader()
					.getResourceAsStream(TEMPLATE_PROBATIVEVALUEREPORT_ODT);
					OutputStream pdfOutputStream = new java.io.FileOutputStream(pdffile);) {

				Map<String, Object> dataMap = new HashMap<>();
				dataMap.put("report", report);
				PdfFileGenerator.createPdf(odtTemplate, pdfOutputStream, dataMap);
			} catch (Exception e) {
				LOGGER.error("Unable to create PDF from Probative Value Report template ODT", e.getMessage());
				throw new InternalServerException("Unable to create PDF from Probative Value Report template ODT", e);
			}
		} catch (InvalidParseOperationException | VitamClientException exc) {
			LOGGER.error("Unable to create PDF from Probative Value Report Json value", exc.getMessage());
			throw new InternalServerException("Unable to create PDF from Probative Value Report Json value", exc);
		}

	}

	private void generateZip(final String operationId, final String workspaceOperationPath,
			final OutputStream outputStream) {
		final Map<String, InputStream> streams = new HashMap<>();
		File jsonFile = new File(workspaceOperationPath.toString() + "/" + operationId + ".json");
		File pdfFile = new File(workspaceOperationPath.toString() + "/" + operationId + ".pdf");
		try {
			streams.put(operationId + ".json", new FileInputStream(jsonFile));
			streams.put(operationId + ".pdf", new FileInputStream(pdfFile));
		} catch (FileNotFoundException e) {
			LOGGER.error("Unable to generate ZIP", e.getMessage());
			throw new InternalServerException(String.format("Unable to generate ZIP: %s", e.getMessage()), e);
		}
		ZipUtils.generate(streams, outputStream);

	}

	private void reportEntriesConsolidation(VitamContext vitamContext, ProbativeReportDto report)
			throws VitamClientException, InvalidParseOperationException {
		for (ReportEntryDto reportEntry : report.getReportEntries()) {

			String usage = StringUtils.substringBefore(reportEntry.getUsageVersion(), "_");
			String version = StringUtils.substringAfter(reportEntry.getUsageVersion(), "_");

			// find units labels
			VitamUISearchResponseDto unitResponse = unitService.getByIdIn(reportEntry.getUnitIds(), vitamContext);
			for (String unitId : reportEntry.getUnitIds()) {
				Optional<ResultsDto> resultUnit = unitResponse.getResults().stream()
						.filter(result -> result.getId().equals(unitId)).findFirst();
				ItemWithLabelDto unitWithLabel = new ItemWithLabelDto();
				unitWithLabel.setItem(unitId);
				if (resultUnit.isPresent()) {
					unitWithLabel.setLabel(resultUnit.get().getTitle());
				}
				if (reportEntry.getUnitIdWithLabels() == null) {
					reportEntry.setUnitIdWithLabels(new ArrayList<>());
				}
				reportEntry.getUnitIdWithLabels().add(unitWithLabel);
			}

			// find object label
			RequestResponse<JsonNode> got = unitService.findObjectMetadataById(reportEntry.getUnitIds().get(0),
					vitamContext);
			if (got.isOk()) {

				ObjectGroupResponse objectGroupResponse = JsonHandler.getFromJsonNode(got.toJsonNode().get("$results"),
						ObjectGroupResponse.class);
				Optional<QualifiersModel> qualifierObject = objectGroupResponse.getQualifiers().stream()
						.filter(item -> usage.equals(item.getQualifier())).findFirst();
				if (qualifierObject.isPresent()) {
					Optional<VersionsModel> qualifierVersionObject = qualifierObject.get().getVersions().stream()
							.filter(item -> version.equals(String.valueOf(item.getDataVersion()))).findFirst();
					if (qualifierVersionObject.isPresent()) {
						reportEntry.setObjectLabel(qualifierVersionObject.get().getFileInfoModel().getFilename());
					}
				}
			}

			// extract operations infos from JsonNode RightsStatementIdentifier
			for (ProbativeOperationDto operation : reportEntry.getOperations()) {
				if (operation.getRightsStatementIdentifier() != null) {
					if (operation.getRightsStatementIdentifier().has("ArchivalAgreement")) {
						operation.setArchivalAgreement(
								operation.getRightsStatementIdentifier().get("ArchivalAgreement").asText());
					}
					if (operation.getRightsStatementIdentifier().has("Profil")) {
						operation.setProfil(operation.getRightsStatementIdentifier().get("Profil").asText());
					}
					if (operation.getRightsStatementIdentifier().has("AccessContract")) {
						operation.setAccessContract(
								operation.getRightsStatementIdentifier().get("AccessContract").asText());
					}
				}
			}

		}
	}

	private void checkWorkspacePath(String workspaceOperationPath) {
		Assert.isTrue(StringUtils.isNotBlank(workspaceOperationPath), "No operation workspace path has been set");

		final Path workspaceOperation = Paths.get(workspaceOperationPath);
		if (!Files.exists(workspaceOperation)) {
			final String message = String.format("The following operation workspace does not exists: %s",
					workspaceOperation.toString());
			throw new InternalServerException(message);
		}
		if (!Files.isDirectory(workspaceOperation)) {
			final String message = String.format("The following operation workspace is not a directory: %s",
					workspaceOperation.toString());
			throw new InternalServerException(message);
		}
	}

}
