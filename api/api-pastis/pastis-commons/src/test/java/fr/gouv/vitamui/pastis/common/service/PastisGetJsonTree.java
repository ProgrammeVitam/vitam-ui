package fr.gouv.vitamui.pastis.common.service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.ElementRNG;
import fr.gouv.vitamui.pastis.common.dto.jaxb.*;
import fr.gouv.vitamui.pastis.common.util.PastisCustomCharacterEscapeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PastisGetJsonTree {

    private static final Logger LOGGER = LoggerFactory.getLogger(PastisGetJsonTree.class);

    // Test an xml to json and a json to xml.
    public String getJsonParsedTree(ElementRNG elementRNGRoot) throws JAXBException, FileNotFoundException {
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
            ChoiceXml.class
        );
        Marshaller marshallerObj = contextObj.createMarshaller();
        marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshallerObj.setProperty(
            "com.sun.xml.bind.marshaller.CharacterEscapeHandler",
            new PastisCustomCharacterEscapeHandler()
        );

        marshallerObj.marshal(eparentRng, new FileOutputStream("generated_test.xml"));

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(eparent);
        } catch (JsonGenerationException e1) {
            LOGGER.debug("JsonGenerationException", e1);
        } catch (JsonMappingException e1) {
            LOGGER.debug("JsonMappingException", e1);
        } catch (IOException e1) {
            LOGGER.debug("IOException", e1);
        }
        return "[" + jsonString + "]";
    }
}
