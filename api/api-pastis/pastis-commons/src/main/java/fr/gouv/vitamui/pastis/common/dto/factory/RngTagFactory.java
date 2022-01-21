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
import java.util.Map;

public class RngTagFactory implements AbstractTagFactory<RngTag> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RngTagFactory.class);

    static RngTag rngTree;


    @Override
    public RngTag createTag(ElementProperties node, Tag parentNode, int profondeur) {

        ValueTag valueRNG = null;
        DataTag dataRNG = null;
        CardinalityTag cardinalityRNG = null;
        RngTag elementOrAttributeRNG = null;
        AnnotationTag annotationRNG = null;
        DocumentationTag documentationRNG = null;
        GroupTag groupTag = null;
        ChoiceTag choiceTag = null;

        // 0 . Create objects according to node data;
        // If the node has a value
        if (null != node.getValue() && !node.getValue().equals("undefined")) {
            valueRNG = new ValueTag();
            valueRNG.setValue(node.getValue());
        }

        if (node.getChildren().stream().filter(c -> !c.getType().equals(RNGConstants.MetadaDataType.element)).count() ==
            0) {
            if (valueRNG == null && RNGConstants.TypesMap.containsKey(node.getName())) {
                dataRNG = new DataTag();
                dataRNG.setDataType(RNGConstants.TypesMap.get(node.getName()).getLabel());
            }
        }

        // When a value is declared in a profile element, the <rng:data> tag must be suppressed
        // to assure that the generated profile is successfully imported by VITAM
        if (null != node.getValueOrData() && !node.getValueOrData().equals("undefined") && node.getValue() == null) {
            if (node.getValueOrData().equals("data")) {
                dataRNG = new DataTag();
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
            annotationRNG = new AnnotationTag();
            documentationRNG = new DocumentationTag();
            documentationRNG.setDocumentation(node.getDocumentation());
            annotationRNG.setDocumentationTag(documentationRNG);
        }

        if (null != node.getType() && !node.getType().equals("undefined")) {
            if (node.getType().equals("element")) {
                elementOrAttributeRNG = new ElementTag();
            } else if (node.getType().equals("attribute")) {
                elementOrAttributeRNG = new AttributeTag();
            }
            if (null != node.getName() && !node.getName().equals("undefined")) {
                elementOrAttributeRNG.setName(node.getName());
            }
        }

        if (null != documentationRNG) {
            elementOrAttributeRNG.getChildren().add(annotationRNG);
            annotationRNG.setParent(elementOrAttributeRNG);
        }

        // Check node's and its children's cardinality
        if (node.getCardinality() != null) {
            CardinalityTagFactory cardinalityFactory = new CardinalityTagFactory();
            CardinalityTag cardinalityTag = cardinalityFactory.createTag(node, parentNode, profondeur);
            LOGGER.info("Parsing " + cardinalityTag.getTagName());
            cardinalityRNG = cardinalityTag;
        }

        RngTag currentTag = null;
        // 1. Once the objects are created, arrange them accordingly
        // 1. Check if it is an element
        if (null != elementOrAttributeRNG) {

            if (parentNode != null)
                LOGGER.info("Parsing " + elementOrAttributeRNG.getName());
            // 1.1 Check if the element has cardinality
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

            // 2. Check data tag
            if (null != dataRNG) {
                DataTagFactory dataTagFactory = new DataTagFactory();
                HashMap dataAndCurrentTagMap =
                    new HashMap(dataTagFactory.createTagWithTag(node, dataRNG, currentTag, profondeur));

                currentTag = (RngTag) new ArrayList(dataAndCurrentTagMap.values()).get(0);

            }
            // 3. Check value tag
            if (null != valueRNG) {
                // If Children is empty
                if (currentTag.getChildren().isEmpty()) {
                    if (currentTag instanceof ElementTag) {
                        currentTag.setValueTag(valueRNG);
                        valueRNG.setParent(currentTag);

                    } else if (currentTag instanceof AttributeTag) {
                        currentTag.setValueTag(valueRNG);
                        valueRNG.setParent(currentTag);
                    }
                    // If children is Element or Attribute, set  accordingly
                } else if (currentTag instanceof ElementTag) {
                    currentTag.setValueTag(valueRNG);
                    valueRNG.setParent(currentTag);

                } else if (currentTag instanceof AttributeTag) {
                    currentTag.setValueTag(valueRNG);
                    valueRNG.setParent(currentTag);
                } else {
                    // Set the value to an simple element
                    elementOrAttributeRNG.setValueTag(valueRNG);
                    valueRNG.setParent(elementOrAttributeRNG);
                }
            }
        }

        if (null != currentTag) {

            if (null != parentNode) {
                RngTag optionalWithChildren;
                optionalWithChildren = (RngTag) parentNode.getChildren()
                    .stream().filter(cardinality -> cardinality instanceof CardinalityTag)
                    .findAny()
                    .orElse(null);

                Boolean optionalHasAlreadyCurrentTag = optionalWithChildren == null
                    ? false : optionalWithChildren.children.contains(currentTag);

                if (!optionalHasAlreadyCurrentTag) {
                    currentTag.setParent(parentNode);
                    parentNode.getChildren().add(currentTag);
                }

            } else {
                rngTree = currentTag;
            }
        }

        if (currentTag instanceof GrammarTag) {
            this.createTag(node, (RngTag) currentTag.getChildren().get(0), profondeur + 1);
        } else {
            for (ElementProperties next : node.getChildren()) {
                if (currentTag instanceof CardinalityTag) {
                    this.createTag(next, (RngTag) currentTag.getChildren().get(0), profondeur + 1);
                } else {
                    this.createTag(next, currentTag, profondeur + 1);
                }
            }
        }
        return rngTree;
    }

    @Override
    public Map<RngTag, RngTag> createTagWithTag(ElementProperties node, RngTag tag, RngTag currentTag, int level) {
        return null;
    }

}
