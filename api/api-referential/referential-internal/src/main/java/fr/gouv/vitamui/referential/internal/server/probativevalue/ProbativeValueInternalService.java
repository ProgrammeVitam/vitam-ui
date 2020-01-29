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

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

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
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.referential.common.export.probativevalue.dto.ItemWithLabelDto;
import fr.gouv.vitamui.referential.common.export.probativevalue.dto.ProbativeReportDto;
import fr.gouv.vitamui.referential.common.export.probativevalue.dto.ReportEntryDto;
import fr.gouv.vitamui.referential.common.service.VitamBatchReportService;

/**
 * A service to access a probative value report.
 *
 */
@Service
public class ProbativeValueInternalService {

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
		checkWorkspacePath(workspaceOperationPath);
		getProbativeValueReportJson(vitamContext, operationId, workspaceOperationPath);
		generateXml(vitamContext, operationId, workspaceOperationPath);
		generatePDF(vitamContext, operationId, workspaceOperationPath);
		generateZip(operationId, workspaceOperationPath, outputStream);

	}

	private void getProbativeValueReportJson(final VitamContext vitamContext, final String operationId,
			final String workspaceOperationPath) {
		try (InputStream reportStream = vitamBatchReportService.downloadBatchReport(vitamContext,
				operationId)) {
			File file = new File(workspaceOperationPath, operationId + ".json");
			FileUtils.copyInputStreamToFile(reportStream, file);
		} catch (VitamClientException e) {
			LOGGER.error(e.getMessage());
			throw new InternalServerException("Unable to get Probative Value Report from VITAM", e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new InternalServerException("Unable to create JSON from Probative Value Report", e);
		}

	}

	private void generateXml(final VitamContext vitamContext, final String operationId,
			final String workspaceOperationPath) {
		try {
			File jsonReport = new File(workspaceOperationPath, operationId + ".json");
			File xmlReport = new File(workspaceOperationPath, operationId + ".xml");
			ProbativeReportDto report = JsonHandler.getFromFile(jsonReport, ProbativeReportDto.class);
			reportEntriesConsolidation(vitamContext, report);

			XmlMapper xmlMapper = new XmlMapper();
			xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
			xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
			xmlMapper.writer().withRootName("report").writeValue(xmlReport, report);

		} catch (VitamClientException | InvalidParseOperationException | IOException e) {
			LOGGER.error(e.getMessage());
			throw new InternalServerException("Unable to create XML data from JSON Probative Value Report", e);
		}
	}

	private void generatePDF(final VitamContext vitamContext, final String operationId,
			final String workspaceOperationPath) {

		File xmlfile = new File(workspaceOperationPath, operationId + ".xml");

		try (InputStream fopconfigfile = getClass().getClassLoader().getResourceAsStream("fop/fop-config.xml");
			InputStream xsltfile = getClass().getClassLoader().getResourceAsStream("fop/probativevaluereport.xsl");){

			File pdffile = new File(workspaceOperationPath, operationId + ".pdf");

			// configure fopFactory as desired
			
			final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI(), fopconfigfile);

			FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
			// configure foUserAgent as desired

			// Setup output
			try (OutputStream out = new java.io.FileOutputStream(pdffile)) {

				// Construct fop with desired output format
				Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

				// Setup XSLT
				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer transformer = factory.newTransformer(new StreamSource(xsltfile));

				// Set the value of a <param> in the stylesheet
				transformer.setParameter("versionParam", "2.0");

				// Setup input for XSLT transformation
				Source src = new StreamSource(xmlfile);

				// Resulting SAX events (the generated FO) must be piped through to FOP
				Result res = new SAXResult(fop.getDefaultHandler());

				// Start XSLT transformation and FOP processing
				transformer.transform(src, res);
			}

		} catch (TransformerException | IOException | SAXException e) {
			LOGGER.error(e.getMessage());
			throw new InternalServerException("Unable to create PDF from Probative Value Report XML", e);
		} finally {
			FileUtils.deleteQuietly(xmlfile);
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
				Optional<VersionsModel> qualifierVersionObject = qualifierObject.get().getVersions().stream()
						.filter(item -> version.equals(String.valueOf(item.getDataVersion()))).findFirst();
				reportEntry.setObjectLabel(qualifierVersionObject.get().getFileInfoModel().getFilename());
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
