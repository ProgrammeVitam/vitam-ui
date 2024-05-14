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

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.xerces.util.XMLCatalogResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ManifestValidator {

    public static final String RNG_FACTORY = "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory";
    public static final String RNG_PROPERTY_KEY = "javax.xml.validation.SchemaFactory:" + XMLConstants.RELAXNG_NS_URI;
    public static final String HTTP_WWW_W3_ORG_XML_XML_SCHEMA_V1_1 = "http://www.w3.org/XML/XMLSchema/v1.1";
    /**
     * Filename of the catalog file ; should be found in the classpath.
     */
    public static final String CATALOG_FILENAME = "xsd_validation/catalog.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestValidator.class);
    private static final String RNG_SUFFIX = ".rng";

    /**
     * @param manifestFile
     * @param rngFile
     * @return true if validated
     * @throws SAXException
     * @throws IOException
     */
    public boolean checkFileRNG(InputStream manifestFile, File rngFile) {
        try {
            if (rngFile.length() > 0) {
                final Schema schema = getSchema(rngFile);
                final Validator validator = schema.newValidator();
                validator.validate(new StreamSource(manifestFile));
                return true;
            }
            LOGGER.error("Le fichier RNG est vide");
            return false;
        } catch (SAXException | IOException e) {
            LOGGER.error("Erreur validation du manifest", e);
            return false;
        } finally {
            IOUtils.closeQuietly(manifestFile);
        }
    }

    private Schema getSchema(File file) throws SAXException {
        SchemaFactory factory;
        if (file.getName().endsWith(RNG_SUFFIX)) {
            System.setProperty(RNG_PROPERTY_KEY, RNG_FACTORY);
            factory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);
        } else {
            factory = SchemaFactory.newInstance(HTTP_WWW_W3_ORG_XML_XML_SCHEMA_V1_1);
        }

        // Load catalog to resolve external schemas even offline.
        final URL catalogUrl = ManifestValidator.class.getClassLoader().getResource(CATALOG_FILENAME);
        factory.setResourceResolver(new XMLCatalogResolver(new String[] { catalogUrl.toString() }, false));

        return factory.newSchema(file);
    }
}
