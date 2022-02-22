/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

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
package fr.gouv.vitamui.pastis.common.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.ElementRNG;
import fr.gouv.vitamui.pastis.common.dto.jaxb.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PastisGetXmlJsonTree {

    private static final Logger LOGGER = LoggerFactory.getLogger(PastisGetXmlJsonTree.class);
    //ElementRNG elementRNGRoot;
    public String jsonParsed = "";

    public ElementProperties getJsonParsedTree(ElementRNG elementRNGRoot) {
        ElementRNG.buildElementPropertiesTree(elementRNGRoot, 0, null);
        return ElementRNG.getElementStaticRoot();
    }

    // Test an xml to json and a json to xml.
    public String getJsonParsedTreeTest(ElementRNG elementRNGRoot) throws JAXBException, FileNotFoundException {

        //vWhen the handler is called, the proprities tree (json) is build
        // using its ElementRNG(elementRngRoot) object.
        // The elementRngRoot is filled when the xml file is read, by passing
        // it to the contentHanler of the  Xml reader.
        // The methods used are the 5 main methods of a DefaultHandler type
        // See methods bellow
        ElementRNG.buildElementPropertiesTree(elementRNGRoot, 0, null);
        ElementProperties eparent = ElementRNG.getElementStaticRoot();


        // The eparentRng is an object of type BalizeXML. It is  built using the
        // object eparent (of type ElementProperties) that, in fact, represent the json
        // prouced during the parser's first call.
        BaliseXML.buildBaliseXMLTree(eparent, 0, null);
        BaliseXML eparentRng = BaliseXML.getBaliseXMLStatic();


        // Transforms java objects to Xml file (Marshalling)
        JAXBContext contextObj =
            JAXBContext.newInstance(AttributeXML.class, ElementXML.class, DataXML.class, ValueXML.class,
                OptionalXML.class, OneOrMoreXML.class,
                ZeroOrMoreXML.class, AnnotationXML.class, DocumentationXML.class, ChoiceXml.class);
        Marshaller marshallerObj = contextObj.createMarshaller();
        marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshallerObj.setProperty("com.sun.xml.bind.marshaller.CharacterEscapeHandler",
            new PastisCustomCharacterEscapeHandler());

        marshallerObj.marshal(eparentRng, new FileOutputStream("generated_test.xml"));


        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(eparent);
        } catch (JsonGenerationException e1) {
            LOGGER.info("JsonGenerationException", e1);
        } catch (JsonMappingException e1) {
            LOGGER.info("JsonMappingException", e1);
        } catch (IOException e1) {
            LOGGER.info("IOException", e1);
        }
        return "[" + jsonString + "]";
    }


    public String getXmlParsedTree(String jsonString) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper xmlMapper = new ObjectMapper();
        JsonNode tree = objectMapper.readTree(jsonString);
        return xmlMapper.writeValueAsString(tree);
    }

}
