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

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitamui.pastis.common.util.RNGConstants;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
public class ElementProperties {
    private boolean additionalProperties;
    private String name;
    private String type;
    private String cardinality;
    private String groupOrChoice;
    private String valueOrData;
    private String dataType;
    private String value;
    private String documentation;
    private String editName;
    @JsonIgnore
    private Object sedaData;
    private int level;
    private Long id;
    private Long parentId;
    @JsonIgnore
    private ElementProperties parent;
    private List<ElementProperties> choices = new ArrayList<>();
    private List<ElementProperties> children = new ArrayList<>();
    private PuaData puaData;

    public void setCardinality(String cardinality) {
        this.cardinality = (null != RNGConstants.getCardinalityMap().get(cardinality) ?
            RNGConstants.getCardinalityMap().get(cardinality) :
            cardinality);
    }

    public String getGroupOrChoice() {
        return groupOrChoice;
    }

    public void setGroupOrChoice(String groupOrChoice) {
        this.groupOrChoice = RNGConstants.getGroupOrChoiceMap().getOrDefault(groupOrChoice, groupOrChoice);
    }

    public void initTree(ElementProperties json) {
        for (ElementProperties child : json.getChildren()) {
            child.setParent(json);
            initTree(child);
        }
    }

    public Stream<ElementProperties> flattened() {
        return Stream.concat(
            Stream.of(this),
            children.stream().flatMap(ElementProperties::flattened));
    }

    /**
     * Finds all elements by name in the tree.
     *
     * @param name The name of element to match.
     * @return All matched elements for the name.
     */
    public List<ElementProperties> findAll(final String name, final Number depth) {
        final List<ElementProperties> list = new ArrayList<>();

        if (this.name.equals(name)) {
            list.add(this);
        }

        if (depth == null) {
            list.addAll(
                this.children.stream()
                    .flatMap(child -> child.findAll(name, null).stream())
                    .collect(Collectors.toList())
            );
        } else if (depth.longValue() > 0L) {
            list.addAll(
                this.children.stream()
                    .flatMap(child -> child.findAll(name, depth.longValue() - 1L).stream())
                    .collect(Collectors.toList())
            );
        }

        return list;
    }

    public List<ElementProperties> findAll(final String name) {
        return this.findAll(name, null);
    }

    public ElementProperties find(final String name) {
        final List<ElementProperties> list = this.findAll(name);

        return list.stream().findFirst().orElse(null);
    }

    /**
     * Apply a function to whole element tree.
     *
     * @param consumer A function to apply on element tree.
     */
    public void applyForAll(Consumer<ElementProperties> consumer) {
        consumer.accept(this);
        this.children.forEach(child -> {
            consumer.accept(child);
            child.applyForAll(consumer);
        });
    }
}
