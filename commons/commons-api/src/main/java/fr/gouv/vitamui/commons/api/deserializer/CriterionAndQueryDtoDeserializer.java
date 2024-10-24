/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.commons.api.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CriterionAndQueryDtoDeserializer extends StdDeserializer<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CriterionAndQueryDtoDeserializer.class);

    public CriterionAndQueryDtoDeserializer() {
        this(null);
    }

    public CriterionAndQueryDtoDeserializer(Class<?> vc) {
        super(vc);
    }

    private static final long serialVersionUID = 1052745550909875288L;

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Object result = null;
        JsonNode node = p.getCodec().readTree(p);

        // TODO refactor with a list of allowed types [Criterion.class, QueryDto.class]
        try {
            result = JsonUtils.treeToValue(node, Criterion.class);
        } catch (IOException e) {
            LOGGER.debug("Node is not a " + Criterion.class, e);
        }

        if (result == null) {
            try {
                result = JsonUtils.treeToValue(node, QueryDto.class);
            } catch (IOException e) {
                LOGGER.debug("Node is not a " + QueryDto.class, e);
            }
        }

        if (result == null || node.isNull()) {
            throw new IOException("Parsing error : node should be of type Criterion or Query : invalid node : " + node);
        }

        return result;
    }
}
