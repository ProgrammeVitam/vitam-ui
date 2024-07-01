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
import fr.gouv.vitamui.pastis.common.dto.jaxb.*;
import fr.gouv.vitamui.pastis.common.util.PastisCustomCharacterEscapeHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "/application-test.yml")
public class RNGProfileValidatorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private RNGProfileValidator rngProfileValidator;

    @Value("${json.base.file}")
    private String jsonFileName;

    @Before
    public void init() {
        rngProfileValidator = new RNGProfileValidator();
    }

    @Test
    /**
     * Test s'assurant que la librairie de validation de fichier RNG est opérationnelle
     */
    public void validateRNGProfileOK() throws Exception {
        File fileProfileXsd = PropertiesUtils.getResourceFile("profiles/profile_ok.rng");

        Assert.assertTrue(rngProfileValidator.validateRNG(fileProfileXsd));
    }

    @Test
    /**
     * Test s'assurant que la librairie de validation de fichier RNG est opérationnelle
     */
    public void validateRNGProfileNOK() throws Exception {
        File fileProfileXsd = PropertiesUtils.getResourceFile("profiles/profile_nok.rng");

        Assert.assertFalse(rngProfileValidator.validateRNG(fileProfileXsd));
    }

    @Test
    /**
     * Génère un profil RNG depuis un fichier JSON et valide ce profil
     */
    public void validateGeneratedRNGProfileFromJSON() throws Exception {
        InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream(jsonFileName);
        ObjectMapper objectMapper = new ObjectMapper();
        ElementProperties jsonMap = objectMapper.readValue(jsonInputStream, ElementProperties.class);
        jsonMap.initTree(jsonMap);

        BaliseXML.buildBaliseXMLTree(jsonMap, 0, null);
        BaliseXML eparentRng = BaliseXML.getBaliseXMLStatic();
        JAXBContext contextObj = JAXBContext.newInstance(
            AttributeXML.class,
            ElementXML.class,
            DataXML.class,
            ValueXML.class,
            OptionalXML.class,
            OneOrMoreXML.class,
            ZeroOrMoreXML.class,
            AnnotationXML.class,
            DocumentationXML.class,
            StartXML.class,
            GrammarXML.class,
            ChoiceXml.class
        );
        Marshaller marshallerObj = contextObj.createMarshaller();
        marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshallerObj.setProperty(
            "com.sun.xml.bind.marshaller.CharacterEscapeHandler",
            new PastisCustomCharacterEscapeHandler()
        );

        File rngProfile = tempFolder.newFile();
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(rngProfile), "UTF-8");
        marshallerObj.marshal(eparentRng, writer);
        writer.close();

        Assert.assertTrue(rngProfileValidator.validateRNG(rngProfile));
    }
}
