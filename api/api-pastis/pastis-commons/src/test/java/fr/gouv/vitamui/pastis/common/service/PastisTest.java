/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
package fr.gouv.vitamui.pastis.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.util.PastisMarshaller;
import fr.gouv.vitamui.pastis.common.util.PastisSAX2Handler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "/application-test.yml")
public class PastisTest {

    public PastisMarshaller pastisMarshaller = new PastisMarshaller();

    @Value("${rng.base.file}")
    private String rngFileName;

    @Value("${json.base.file}")
    private String jsonFileName;

    @Test
    public void testIfRngIsPresent() throws FileNotFoundException {
        InputStream os = getClass().getClassLoader().getResourceAsStream(this.rngFileName);
    }

    @Test
    public void testIfRngCanBeGenerated() throws IOException, JAXBException {
        // Map a json from file to ElementProperties object
        InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(jsonFileName);
        ObjectMapper objectMapper = new ObjectMapper();
        ElementProperties mappedJson = objectMapper.readValue(jsonInputStream, ElementProperties.class);
        mappedJson.initTree(mappedJson);

        String responseFromMarshaller = pastisMarshaller.getMarshalledObject(mappedJson);
        Assert.assertFalse("RNG profile generated successfully", responseFromMarshaller.isEmpty());
    }

    @Test
    public void testIfJSONCanBeGenerated() throws IOException, JAXBException, URISyntaxException, SAXException {
        PastisSAX2Handler handler = new PastisSAX2Handler();
        PastisGetJsonTree getJson = new PastisGetJsonTree();

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setContentHandler(handler);

        ClassLoader loader = ClassLoader.getSystemClassLoader();

        xmlReader.parse(loader.getResource(this.rngFileName).toURI().toString());
        String jsonTree = getJson.getJsonParsedTree(handler.getElementRNGRoot());

        Assert.assertNotNull("JSON profile generated successfully", jsonTree);
    }
}
