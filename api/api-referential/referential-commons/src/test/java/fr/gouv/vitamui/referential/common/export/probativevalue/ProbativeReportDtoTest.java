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
package fr.gouv.vitamui.referential.common.export.probativevalue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

import fr.gouv.vitamui.referential.common.export.probativevalue.dto.ProbativeReportDto;

public class ProbativeReportDtoTest {

    @Test
    public void testProbativeReportDtoFromJsonWARNING() throws JsonParseException, JsonMappingException, IOException {
    	
    	ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = ProbativeReportDtoTest.class.getClassLoader().getResourceAsStream("provative_report_WARNING.json");
        ProbativeReportDto report = objectMapper.readValue(ByteStreams.toByteArray(inputStream), ProbativeReportDto.class);
        assertNotNull(report);
        assertEquals(1, report.getReportEntries().size());
    }

    @Test
    public void testProbativeReportDtoFromJsonKO() throws JsonParseException, JsonMappingException, IOException {
    	
    	ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = ProbativeReportDtoTest.class.getClassLoader().getResourceAsStream("provative_report_KO.json");
        ProbativeReportDto report = objectMapper.readValue(ByteStreams.toByteArray(inputStream), ProbativeReportDto.class);
        assertNotNull(report);
        assertEquals(1, report.getReportEntries().size());
    }
	
}

