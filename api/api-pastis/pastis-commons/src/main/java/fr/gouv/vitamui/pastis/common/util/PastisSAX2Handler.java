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

import fr.gouv.vitamui.pastis.common.dto.ElementRNG;
import lombok.Getter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

public class PastisSAX2Handler extends DefaultHandler {
    @Getter private ElementRNG elementRNGRoot;
    boolean isValue;
    private Stack<ElementRNG> stackRNG = new Stack<>();
    private boolean isInDocumentationTag;
    private StringBuilder documentationContent;

    /**
     * BEGIN OF OVERRIDE OF SAX 5 METHODS : startElement, endElement, startDocument, endDocument and characters
     * This method is called everytime the parser gets an open tag
     * Identifies which tag has being opened at time by assiging a new flag
     */
    public void startElement(String nameSpace, String localName, String qName, Attributes attr) {

        //cette variable contient le nom du nœud qui a créé l'événement
        // If node not a grammar tag or start tag
        if (!("grammar".equals(localName) || "start".equals(localName))) {

            // If node is ArchiveTransfer
            if (null != attr.getValue("name") && attr.getValue("name").equals("ArchiveTransfer")) {
                return;
            }
            //If node has documentation
            if ("value".equals(localName) || "documentation".equals(localName)) {
                this.isValue = true;
            }
            // Create a new rng tag element and add it to the stack
            ElementRNG elementRNG = new ElementRNG();
            elementRNG.setName(attr.getValue("name"));
            elementRNG.setType(localName);
            elementRNG.setDataType(attr.getValue("type"));
            if (!stackRNG.isEmpty()) {
                ElementRNG e = stackRNG.lastElement();
                elementRNG.setParent(e);
                e.getChildren().add(elementRNG);
            }
            stackRNG.push(elementRNG);
        }

        documentationContent = new StringBuilder();
        if (qName.equalsIgnoreCase("xsd:documentation")) {
            isInDocumentationTag = true;
        }
    }

    /**
     * Actions à réaliser lors de la détection de la fin d'un élément.
     */
    public void endElement(String nameSpace, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("xsd:documentation")) {
            isInDocumentationTag = false;
        }
        if (!stackRNG.isEmpty()) {
            stackRNG.pop();
        }
    }

    /**
     * Actions à réaliser au début du document.
     */
    public void startDocument() {
        elementRNGRoot = new ElementRNG();
        elementRNGRoot.setName("ArchiveTransfer");
        elementRNGRoot.setType("element");
        stackRNG.push(elementRNGRoot);
    }

    /**
     * Actions to perform when tag content is reached (Data between '< />' )
     */
    @Override
    public void characters(char[] caracteres, int start, int length) throws SAXException {
        if (isInDocumentationTag) {
            documentationContent.append(new String(caracteres, start, length));
            stackRNG.lastElement().setValue(documentationContent.toString());
        }
        if (isValue) {
            String valueContent = new String(caracteres, start, length);
            stackRNG.lastElement().setValue(valueContent);
            this.isValue = false;
        }
    }
}
