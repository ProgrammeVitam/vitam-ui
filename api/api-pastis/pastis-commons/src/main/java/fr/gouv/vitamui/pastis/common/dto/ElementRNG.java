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
package fr.gouv.vitamui.pastis.common.dto;

import fr.gouv.vitamui.pastis.common.util.RNGConstants;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Paulo Pimenta <pimenta@cines.fr>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class ElementRNG {


    private static ElementProperties elementStatic = new ElementProperties();
    @Getter
    private static ElementProperties elementStaticRoot = new ElementProperties();
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementRNG.class);
    private static long idCounter = 0;
    String name;
    String type;
    String dataType;
    String value;
    ElementRNG parent;
    List<ElementRNG> children = new ArrayList<>();

    public static void setDataForParentElementOrAttribute(ElementProperties parentNode, ElementRNG node) {
        if (null != parentNode.getType() && (RNGConstants.MetadaDataType.ELEMENT.getLabel().equals(parentNode.getType())
            || RNGConstants.MetadaDataType.ATTRIBUTE.getLabel().equals(parentNode.getType()))) {
            parentNode.setValueOrData(node.getType());
            if (RNGConstants.getTypesMap().containsKey(parentNode.getName())) {
                parentNode.setDataType(RNGConstants.getTypesMap().get(parentNode.getName()).getLabel());
            }
            parentNode.setValue(node.getValue());
        } else {
            setDataForParentElementOrAttribute(parentNode.getParent(), node);
        }
    }

    public static void setDocumentationForParentElement(ElementProperties parentNode, ElementRNG node) {
        if (null != parentNode.getType() &&
            RNGConstants.MetadaDataType.ELEMENT.getLabel().equals(parentNode.getType())) {
            if(parentNode.getName() != null && parentNode.getName().equals("ArchiveUnit")
                && node.getValue().contains("Commentaire : ")){
                parentNode.setDocumentation(node.getValue().replace("Commentaire : ", ""));
                if(parentNode.getDocumentation() != null ){
                    parentNode.setEditName(parentNode.getDocumentation());
                }
            }else{
                if(parentNode.getDocumentation() != null ){
                    parentNode.setEditName(node.getValue());
                }else{
                    parentNode.setDocumentation(node.getValue());
                }
            }
        } else {
            setDocumentationForParentElement(parentNode.getParent(), node);
        }
    }

    // Build the a tree of properties given :
    // a node
    //the level of the node
    //the parent of the node
    public static void buildElementPropertiesTree(ElementRNG node, int profondeur,
        ElementProperties parentNode) {
        ElementProperties local = new ElementProperties();
        LOGGER.trace("Generating JSON element {}", node.getName());
        if (null != node.getType() && RNGConstants.MetadaDataType.ELEMENT.getLabel().equals(node.getType())
            || RNGConstants.MetadaDataType.ATTRIBUTE.getLabel().equals(node.getType())) {

            local.setCardinality(elementStatic.getCardinality());
            local.setGroupOrChoice(elementStatic.getGroupOrChoice());
            local.setName(node.getName());
            local.setType(node.getType());
            local.setLevel(profondeur);
            local.setValue(node.getValue());


            elementStatic = new ElementProperties();

            if (null != parentNode) {
                local.setParent(parentNode);
                local.setParentId(parentNode.getId());
                local.setId(ElementRNG.idCounter++);
                parentNode.getChildren().add(local);
            } else {
                local.setId(ElementRNG.idCounter++);
                local.setParentId(null);
                elementStaticRoot = local;
            }
        } else {

            if (RNGConstants.isValueOrData(node.getType())) {
                setDataForParentElementOrAttribute(parentNode, node);
            } else if (RNGConstants.isCardinality(node.getType())) {
                elementStatic.setCardinality(node.getType());
            } else if (RNGConstants.hasGroupOrChoice(node.getType())) {
                elementStatic.setGroupOrChoice(node.getType());
            } else if ( "documentation".equals(node.getType()) && null != node.getValue()) {
                setDocumentationForParentElement(parentNode, node);
            }

            local = parentNode;
        }
        buildTree(node, profondeur, local);
    }

    private static void buildTree(ElementRNG node, int profondeur, ElementProperties local){
        for (ElementRNG next : node.getChildren()) {
            if (null != next.getType() && (RNGConstants.MetadaDataType.ELEMENT.getLabel().equals(next.getType())
                || RNGConstants.MetadaDataType.ATTRIBUTE.getLabel().equals(next.getType()))) {
                buildElementPropertiesTree(next, profondeur + 1, local);
            } else {
                buildElementPropertiesTree(next, profondeur, local);
            }
        }
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlElement
    public String getType() {
        return type;
    }

    @XmlAttribute(name = "type")
    public String getDataType() {
        return dataType;
    }

    @XmlElement(name = "rng:value")
    public String getValue() {
        return value;
    }

    @XmlTransient
    public ElementRNG getParent() {
        return parent;
    }

    @XmlAnyElement
    public List<ElementRNG> getChildren() {
        return children;
    }
}
