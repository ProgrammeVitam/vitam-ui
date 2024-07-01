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

package fr.gouv.vitamui.pastis.common.dto.factory;

import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.util.RNGConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class RngTagFactory implements AbstractTagFactory<RngTag> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RngTagFactory.class);
    private static final String UNDEFINED = "undefined";

    static RngTag rngTree;

    private ValueTag valueRNG;
    private DataTag dataRNG;
    private RngTag elementOrAttributeRNG;
    private AnnotationTag annotationRNG;
    private DocumentationTag documentationRNG;

    @Override
    public RngTag createTag(ElementProperties node, Tag parentNode, int profondeur) {
        valueRNG = null;
        dataRNG = null;
        CardinalityTag cardinalityRNG = null;
        elementOrAttributeRNG = null;
        annotationRNG = null;
        documentationRNG = null;

        this.checkNode(node);

        // When a value is declared in a profile element, the <rng:data> tag must be suppressed
        // to assure that the generated profile is successfully imported by VITAM
        if (
            null != node.getValueOrData() &&
            !node.getValueOrData().equals(UNDEFINED) &&
            node.getValue() == null &&
            node.getValueOrData().equals("data")
        ) {
            dataRNG = new DataTag();
        }
        // Sets the type of data (if value or data)
        if (null != node.getDataType() && !node.getDataType().equals(UNDEFINED)) {
            if (null != valueRNG) {
                valueRNG.setDataType(node.getDataType());
            } else if (null != dataRNG) {
                dataRNG.setDataType(node.getDataType());
            }
        }
        // Set annotation and documentation tags (if exists)
        this.setElementAnnotationDocumentation(node);

        // Check node's and its children's cardinality
        if (node.getCardinality() != null) {
            cardinalityRNG = this.checkCardinality(node, parentNode, profondeur);
        }

        RngTag currentTag;
        // 1. Once the objects are created, arrange them accordingly
        // 1. Check if it is an element
        currentTag = checkIfElement(
            node,
            parentNode,
            profondeur,
            valueRNG,
            dataRNG,
            cardinalityRNG,
            elementOrAttributeRNG
        );

        // Implement Element according to state
        currentTagImplementationAccordingToState(node, parentNode, profondeur, currentTag);
        return rngTree;
    }

    /**
     * 1. Check if it is an element
     * @param node
     * @param parentNode
     * @param profondeur
     * @param valueRNG
     * @param dataRNG
     * @param cardinalityRNG
     * @param elementOrAttributeRNG
     * @return currentTag
     */
    private RngTag checkIfElement(
        ElementProperties node,
        Tag parentNode,
        int profondeur,
        ValueTag valueRNG,
        DataTag dataRNG,
        CardinalityTag cardinalityRNG,
        RngTag elementOrAttributeRNG
    ) {
        RngTag currentTag = null;
        if (null != elementOrAttributeRNG) {
            if (parentNode != null) LOGGER.debug(
                this.getClass().getName(),
                "Parsing %s",
                elementOrAttributeRNG.getName()
            );
            // 1.1 Check if the element has cardinality
            currentTag = checkCardinalityElement(parentNode, cardinalityRNG, elementOrAttributeRNG);

            // 2. Check data tag
            currentTag = checkDataTag(node, profondeur, dataRNG, currentTag);
            // 3. Check value tag
            checkValueTag(valueRNG, elementOrAttributeRNG, currentTag);
        }
        return currentTag;
    }

    /**
     * Implement current tag according to state
     * @param node
     * @param parentNode
     * @param profondeur
     * @param currentTag
     */
    private void currentTagImplementationAccordingToState(
        ElementProperties node,
        Tag parentNode,
        int profondeur,
        RngTag currentTag
    ) {
        if (null != currentTag) {
            if (null != parentNode) {
                RngTag optionalWithChildren;
                optionalWithChildren = (RngTag) parentNode
                    .getChildren()
                    .stream()
                    .filter(CardinalityTag.class::isInstance)
                    .findAny()
                    .orElse(null);

                boolean optionalHasAlreadyCurrentTag =
                    optionalWithChildren != null && optionalWithChildren.children.contains(currentTag);

                if (!optionalHasAlreadyCurrentTag) {
                    currentTag.setParent(parentNode);
                    parentNode.getChildren().add(currentTag);
                }
            } else {
                RngTagFactory.setRngTree(currentTag);
            }
        }

        this.currentNotNullTagImplementationAccordingToState(node, profondeur, currentTag);
    }

    private static void setRngTree(RngTag currentTag) {
        rngTree = currentTag;
    }

    private void currentNotNullTagImplementationAccordingToState(
        ElementProperties node,
        int profondeur,
        RngTag currentTag
    ) {
        if (currentTag instanceof GrammarTag) {
            this.createTag(node, currentTag.getChildren().get(0), profondeur + 1);
        } else {
            for (ElementProperties next : node.getChildren()) {
                if (currentTag instanceof CardinalityTag) {
                    this.createTag(next, currentTag.getChildren().get(0), profondeur + 1);
                } else {
                    this.createTag(next, currentTag, profondeur + 1);
                }
            }
        }
    }

    /**
     *  1.1 Check if the element has cardinality
     * @param parentNode
     * @param cardinalityRNG
     * @param elementOrAttributeRNG
     * @return
     */
    private RngTag checkCardinalityElement(
        Tag parentNode,
        CardinalityTag cardinalityRNG,
        RngTag elementOrAttributeRNG
    ) {
        RngTag currentTag;
        if (null != cardinalityRNG) {
            cardinalityRNG.getChildren().add(elementOrAttributeRNG);
            elementOrAttributeRNG.setParent(cardinalityRNG);
            currentTag = cardinalityRNG;
        } else {
            currentTag = elementOrAttributeRNG;
            //1.2. Check if it's the first grammar node (Archive transfer)
            if (parentNode == null) {
                GrammarTag grammarTag = new GrammarTag();
                StartTag startTag = new StartTag();
                startTag.setParent(grammarTag);
                grammarTag.getChildren().add(startTag);
                currentTag = grammarTag;
            }
        }
        return currentTag;
    }

    /**
     * 2.Check data tag
     * @param node
     * @param profondeur
     * @param dataRNG
     * @param currentTag
     * @return
     */
    private RngTag checkDataTag(ElementProperties node, int profondeur, DataTag dataRNG, RngTag currentTag) {
        if (null != dataRNG) {
            DataTagFactory dataTagFactory = new DataTagFactory();
            HashMap<RngTag, RngTag> dataAndCurrentTagMap = new HashMap<>(
                dataTagFactory.createTagWithTag(node, dataRNG, currentTag, profondeur)
            );

            currentTag = new ArrayList<>(dataAndCurrentTagMap.values()).get(0);
        }
        return currentTag;
    }

    /**
     * 3. Check value tag
     * @param valueRNG
     * @param elementOrAttributeRNG
     * @param currentTag
     */
    private void checkValueTag(ValueTag valueRNG, RngTag elementOrAttributeRNG, RngTag currentTag) {
        if (null != valueRNG) {
            // If Children is empty
            if (currentTag.getChildren().isEmpty()) {
                if (currentTag instanceof ElementTag) {
                    setProperties(valueRNG, currentTag);
                }
                if (currentTag instanceof AttributeTag) {
                    setProperties(valueRNG, currentTag);
                }
                // If children is Element or Attribute, set  accordingly
            } else if (currentTag instanceof ElementTag) {
                setProperties(valueRNG, currentTag);
            } else if (currentTag instanceof AttributeTag) {
                setProperties(valueRNG, currentTag);
            } else {
                // Set the value to an simple element
                elementOrAttributeRNG.setValueTag(valueRNG);
                valueRNG.setParent(elementOrAttributeRNG);
            }
        }
    }

    private void setProperties(ValueTag valueRNG, RngTag currentTag) {
        currentTag.setValueTag(valueRNG);
        valueRNG.setParent(currentTag);
    }

    private void checkNode(ElementProperties node) {
        // Create objects according to node data; If the node has a value
        if (null != node.getValue() && !node.getValue().equals(UNDEFINED)) {
            valueRNG = new ValueTag();
            valueRNG.setValue(node.getValue());
        }

        if (
            (long) node.getChildren().size() == 0 &&
            valueRNG == null &&
            RNGConstants.getTypesMap().containsKey(node.getName())
        ) {
            dataRNG = new DataTag();
            dataRNG.setDataType(RNGConstants.getTypesMap().get(node.getName()).getLabel());
        }
    }

    private void setElementAnnotationDocumentation(ElementProperties node) {
        // Set annotation and documentation tags (if exists)
        if (null != node.getDocumentation()) {
            annotationRNG = new AnnotationTag();
            documentationRNG = new DocumentationTag();
            documentationRNG.setDocumentation(node.getDocumentation());
            annotationRNG.setDocumentationTag(documentationRNG);
        }
        if (null != node.getType() && !node.getType().equals(UNDEFINED)) {
            if (node.getType().equals("element")) {
                elementOrAttributeRNG = new ElementTag();
            } else if (node.getType().equals("attribute")) {
                elementOrAttributeRNG = new AttributeTag();
            }
            if (null != node.getName() && !node.getName().equals(UNDEFINED) && elementOrAttributeRNG != null) {
                elementOrAttributeRNG.setName(node.getName());
            }
        }

        if (null != documentationRNG && elementOrAttributeRNG != null) {
            elementOrAttributeRNG.getChildren().add(annotationRNG);
            annotationRNG.setParent(elementOrAttributeRNG);
        }
    }

    private CardinalityTag checkCardinality(ElementProperties node, Tag parentNode, int profondeur) {
        // Check node's and its children's cardinality
        CardinalityTagFactory cardinalityFactory = new CardinalityTagFactory();
        CardinalityTag cardinalityTag = cardinalityFactory.createTag(node, parentNode, profondeur);
        LOGGER.debug(this.getClass().getName(), "Parsing %s", cardinalityTag.getTagName());
        return cardinalityTag;
    }
}
