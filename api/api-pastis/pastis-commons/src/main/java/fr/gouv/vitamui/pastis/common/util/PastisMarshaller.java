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

import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.factory.RngTag;
import fr.gouv.vitamui.pastis.common.dto.factory.RngTagFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class PastisMarshaller {

    public static final String CHAR_ESCAPE_HANDLER = "com.sun.xml.bind.marshaller.CharacterEscapeHandler";
    public static final String MARSHALLER_FORMAT = Marshaller.JAXB_FORMATTED_OUTPUT;

    private static final Logger LOGGER = LoggerFactory.getLogger(PastisMarshaller.class);

    public String getMarshalledObject(ElementProperties mappedJson) throws IOException, JAXBException {
        RngTagFactory tagFactory = new RngTagFactory();
        RngTag rngTree = tagFactory.createTag(mappedJson, null, 0);

        JAXBContext contextObj = JAXBContext.newInstance(RngTag.class);
        Marshaller marshallerObj = contextObj.createMarshaller();
        marshallerObj.setProperty(MARSHALLER_FORMAT, true);
        marshallerObj.setProperty(CHAR_ESCAPE_HANDLER, new PastisCustomCharacterEscapeHandler());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        marshallerObj.marshal(rngTree, writer);
        String response = new String(os.toByteArray(), StandardCharsets.UTF_8);
        writer.close();

        String status = !response.isEmpty() ? "Json marshalled successfully" : "Failed to marshall json object";
        LOGGER.debug(status);

        return response;
    }
}
