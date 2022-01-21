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

package fr.gouv.vitamui.pastis.common.dto.jaxb;

import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.util.RNGConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
@Data
@NoArgsConstructor
public class BaliseXML {


    private static final Logger LOGGER = LoggerFactory.getLogger(BaliseXML.class);
    public static BaliseXML baliseXMLStatic;
    String name;
    String dataType;
    String cardinality;
    String groupOrChoice;
    BaliseXML parent;
    ValueXML valueXML;
    List<BaliseXML> children = new ArrayList<>();

    /**
     * @param node node représentant l'arbre ElementProperties correspondant au json reçu du front
     * @param profondeur profondeur du noeud utile pour le front angular
     * @param parentNode noeud parent utilisé dans la récursivité pour lié parent & children
     */
    public static void buildBaliseXMLTree(ElementProperties node, int profondeur, BaliseXML parentNode) {

        if (node.getName() != null) {
            ValueXML valueRNG = null;
            DataXML dataRNG = null;
            BaliseXML cardinalityRNG = null;
            BaliseXML elementOrAttributeRNG = null;
            AnnotationXML annotationXML = null;
            DocumentationXML documentationXML = null;
            BaliseXML groupOrChoice = null;

            // If the node has a value
            if (null != node.getValue() && !node.getValue().equals("undefined")) {
                valueRNG = new ValueXML();
                valueRNG.setValue(node.getValue());
            }

            // When a value is declared in a profile element, the <rng:data> tag must be suppressed
            // to assure that the generated profile is successfully imported by VITAM
            if (null != node.getValueOrData() && !node.getValueOrData().equals("undefined") &&
                node.getValue() == null) {
                if (node.getValueOrData().equals("data")) {
                    dataRNG = new DataXML();
                }
            }

            if (node.getName() != null && (node.getName().equals("CodeListVersions") ||
                node.getChildren().stream().filter(c -> c.getType().equals(RNGConstants.MetadaDataType.attribute))
                    .count() == 0)) {
                if (valueRNG == null && RNGConstants.TypesMap.containsKey(node.getName())) {
                    dataRNG = new DataXML();
                    dataRNG.setDataType(RNGConstants.TypesMap.get(node.getName()).getLabel());
                }
            }

            // Sets the type of data (if value or data)
            if (null != node.getDataType() && !node.getDataType().equals("undefined")) {
                if (null != valueRNG) {
                    valueRNG.setDataType(node.getDataType());
                } else if (null != dataRNG) {
                    dataRNG.setDataType(node.getDataType());
                }
            }
            // Set annotation and documentation tags (if exists)
            if (null != node.getDocumentation()) {
                annotationXML = new AnnotationXML();
                documentationXML = new DocumentationXML();
                documentationXML.setDocumentation(node.getDocumentation());
                annotationXML.setDocumentationXML(documentationXML);
            }

            if (null != node.getType() && !node.getType().equals("undefined")) {
                if (node.getType().equals("element")) {
                    elementOrAttributeRNG = new ElementXML();
                } else if (node.getType().equals("attribute")) {
                    elementOrAttributeRNG = new AttributeXML();
                }
                if (null != node.getName() && !node.getName().equals("undefined")) {
                    elementOrAttributeRNG.setName(node.getName());
                }
            }

            if (null != documentationXML) {
                elementOrAttributeRNG.getChildren().add(annotationXML);
                annotationXML.setParent(elementOrAttributeRNG);
            }

            // Check node's and its children's cardinality
            if (node.getCardinality() != null) {

                if (node.getCardinality().equals(RNGConstants.Cardinality.zeroOrMore.getLabel())) {
                    cardinalityRNG = new ZeroOrMoreXML();
                    if (elementOrAttributeRNG != null) {
                        elementOrAttributeRNG.setCardinality(RNGConstants.Cardinality.zeroOrMore);
                    }
                } else if (node.getCardinality().equals(RNGConstants.Cardinality.oneOrMore.getLabel())) {
                    cardinalityRNG = new OneOrMoreXML();
                    if (elementOrAttributeRNG != null) {
                        elementOrAttributeRNG.setCardinality(RNGConstants.Cardinality.oneOrMore);
                    }
                } else if (node.getCardinality().equals(RNGConstants.Cardinality.optional.getLabel())) {
                    cardinalityRNG = new OptionalXML();
                    if (elementOrAttributeRNG != null) {
                        elementOrAttributeRNG.setCardinality(RNGConstants.Cardinality.optional);
                    }
                }
            }

            BaliseXML currentXmlTag = null;

            // 1. Check if it is an element
            if (null != elementOrAttributeRNG) {

                LOGGER.info("Parsing " + elementOrAttributeRNG.getName());
                // 1.1 Check if the element has cardinality
                if (null != cardinalityRNG) {
                    cardinalityRNG.getChildren().add(elementOrAttributeRNG);
                    elementOrAttributeRNG.setParent(cardinalityRNG);
                    currentXmlTag = cardinalityRNG;

                } else {
                    currentXmlTag = elementOrAttributeRNG;
                    //1.2. Check if it's the first grammarnode (Archive transfer)
                    if (parentNode == null) {
                        GrammarXML grammar = new GrammarXML();
                        StartXML start = new StartXML();
                        start.setParent(grammar);
                        grammar.getChildren().add(start);
                        currentXmlTag = grammar;
                    }
                }

                // 2. Check data tag
                if (null != dataRNG) {
                    if (!currentXmlTag.getChildren().isEmpty() &&
                        (currentXmlTag.getChildren().get(0) instanceof ElementXML
                            || currentXmlTag.getChildren().get(0) instanceof AttributeXML)) {
                        currentXmlTag.getChildren().get(0).getChildren().add(dataRNG);
                        dataRNG.setParent(currentXmlTag);

                    } else {
                        currentXmlTag.getChildren().add(dataRNG);
                        dataRNG.setParent(currentXmlTag);
                    }
                }
                // 3. Check value tag
                if (null != valueRNG) {
                    // If Children is empty
                    if (currentXmlTag.getChildren().isEmpty()) {
                        if (currentXmlTag instanceof ElementXML) {
                            ((ElementXML) currentXmlTag).setValueXML(valueRNG);
                            valueRNG.setParent(currentXmlTag);

                        } else if (currentXmlTag instanceof AttributeXML) {
                            ((AttributeXML) currentXmlTag).setValueXML(valueRNG);
                            valueRNG.setParent(currentXmlTag);
                        }
                        // If children is Element or Attribute, set  accordingly
                    } else if (currentXmlTag instanceof ElementXML) {
                        ((ElementXML) currentXmlTag).setValueXML(valueRNG);
                        valueRNG.setParent(currentXmlTag);

                    } else if (currentXmlTag instanceof AttributeXML) {
                        ((AttributeXML) currentXmlTag).setValueXML(valueRNG);
                        valueRNG.setParent(currentXmlTag);
                    } else {
                        // Set the value to an simple element
                        elementOrAttributeRNG.setValueXML(valueRNG);
                        valueRNG.setParent(elementOrAttributeRNG);
                    }
                }
            }

            if (null != currentXmlTag) {

                if (null != parentNode) {
                    BaliseXML optionalWithChildren = parentNode.getChildren()
                        .stream().filter(cardinality -> cardinality instanceof OptionalXML
                            || cardinality instanceof ZeroOrMoreXML
                            || cardinality instanceof OneOrMoreXML)
                        .findAny()
                        .orElse(null);

                    Boolean optionalHasAlreadyCurrentTag = optionalWithChildren == null
                        ? false : optionalWithChildren.children.contains(currentXmlTag);

                    if (!optionalHasAlreadyCurrentTag) {
                        currentXmlTag.setParent(parentNode);
                        parentNode.getChildren().add(currentXmlTag);
                    }


                } else {
                    baliseXMLStatic = currentXmlTag;
                }
            }

            if (currentXmlTag instanceof GrammarXML) {
                buildBaliseXMLTree(node, profondeur + 1, currentXmlTag.getChildren().get(0));
            } else {
                for (ElementProperties next : node.getChildren()) {
                    if (currentXmlTag instanceof OptionalXML || currentXmlTag instanceof OneOrMoreXML
                        || currentXmlTag instanceof ZeroOrMoreXML) {
                        buildBaliseXMLTree(next, profondeur + 1, currentXmlTag.getChildren().get(0));
                    } else {
                        buildBaliseXMLTree(next, profondeur + 1, currentXmlTag);
                    }
                }
            }
        }

    }

    public static void addRecipTags() {

		/* Add this arboresenc to current json tree
            <rng:zeroOrMore>
                <rng:attribute>
                    <rng:anyName>
                        <rng:except>
                            <rng:nsName/>
                            <rng:nsName ns=""/>
                        </rng:except>
                    </rng:anyName>
                </rng:attribute>
            </rng:zeroOrMore>

     */

        ZeroOrMoreXML zeroOrMoreRNG = new ZeroOrMoreXML();
        AttributeXML attributeRNG = new AttributeXML();
        AnyNameXML anyNameRNG = new AnyNameXML();
        ExceptXML exceptRNG = new ExceptXML();
        NsNameXML nsNameRNG = new NsNameXML();
        NsNameXML nsNameRNGNs = new NsNameXML();
        nsNameRNGNs.setNs("");
        attributeRNG.setCardinality(RNGConstants.Cardinality.zeroOrMore);
        exceptRNG.getChildren().add(nsNameRNG);
        exceptRNG.getChildren().add(nsNameRNGNs);
        anyNameRNG.getChildren().add(exceptRNG);
        attributeRNG.getChildren().add(anyNameRNG);
        zeroOrMoreRNG.getChildren().add(attributeRNG);
        baliseXMLStatic.getChildren().get(0).getChildren().get(0).getChildren().add(0, zeroOrMoreRNG);
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlAttribute(name = "type")
    public String getDataType() {
        return dataType;
    }

    //@XmlTransient to avoid circular loop parent <-> child
    @XmlTransient
    public BaliseXML getParent() {
        return parent;
    }

    // XmlAnyElement pour etre le plus generique
    @XmlAnyElement
    public List<BaliseXML> getChildren() {
        return children;
    }

    public String setCardinality(RNGConstants.Cardinality cardinality) {
        return this.cardinality = cardinality.getLabel();
    }

    @XmlElement(name = "rng:value")
    public ValueXML getValueXML() {
        return valueXML;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.name;
    }



}
