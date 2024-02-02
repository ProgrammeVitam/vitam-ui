/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.archives.search.common.dto.converter;

import fr.gouv.vitam.common.database.builder.query.action.UnsetAction;
import fr.gouv.vitam.common.database.builder.query.action.UpdateActionHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitamui.archives.search.common.exception.DslQueryCreateException;
import fr.gouv.vitamui.archives.search.common.model.JsonPatch;
import fr.gouv.vitamui.archives.search.common.model.PatchCommand;
import fr.gouv.vitamui.archives.search.common.model.PatchOperation;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

@Service
public class JsonPatchToUnsetActionConverter implements Converter<JsonPatch, UnsetAction> {
    private static final VitamUILogger log =
        VitamUILoggerFactory.getInstance(JsonPatchToUnsetActionConverter.class);

    @Override
    public UnsetAction convert(JsonPatch source) {
        try {
            final String[] paths = source.stream()
                .filter(patchCommand -> patchCommand.getOp() == PatchOperation.REMOVE)
                .map(PatchCommand::getPath)
                .toArray(String[]::new);
            if (paths.length > 0) {
                return UpdateActionHelper.unset(paths);
            }
        } catch (InvalidCreateOperationException e) {
            log.error("{}", e);
            throw new DslQueryCreateException(e);
        }
        return null;
    }
}
