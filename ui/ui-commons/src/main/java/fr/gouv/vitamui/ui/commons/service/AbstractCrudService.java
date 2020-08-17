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
package fr.gouv.vitamui.ui.commons.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.rest.client.BaseCrudRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract class for CRUD calls for UI.
 *
 *
 * @param <T>
 */
public abstract class AbstractCrudService<T extends IdDto> {

    protected Collection<T> getAll(final ExternalHttpContext context, final Optional<String> criteria) {
        return getClient().getAll(context, criteria);
    }

    protected Collection<T> getAll(final ExternalHttpContext context, final Optional<String> criteria, final Optional<String> embedded) {
        return getClient().getAll(context, criteria, embedded);
    }

    public T create(final ExternalHttpContext c, final T dto) {
        beforeCreate(dto);
        return getClient().create(c, dto);
    }

    protected void beforeCreate(final T dto) {
        Assert.isTrue(StringUtils.isBlank(dto.getId()), "The DTO identifier must be null for create");
        ApiUtils.checkValidity(dto);
    }

    public T update(final ExternalHttpContext c, final T dto) {
        beforeUpdate(dto);
        return getClient().update(c, dto);
    }

    protected void beforeUpdate(final T dto) {
        Assert.isTrue(StringUtils.isNotBlank(dto.getId()), "The DTO identifier must be not null for update.");
        ApiUtils.checkValidity(dto);
    }

    public T patch(final ExternalHttpContext c, final Map<String, Object> partialDto, final String id) {
        SanityChecker.check(id);
        beforePatch(partialDto, id);
        return getClient().patch(c, partialDto);
    }

    public T patchWithDto(final ExternalHttpContext c, final T partialDto, final String id) {
        return getClient().patchWithDto(c, partialDto);
    }

    protected void beforePatch(final Map<String, Object> updates, final String id) {
        Assert.isTrue(StringUtils.equals(id, (String) updates.get("id")), "The DTO identifier must match the path identifier for patch.");
    }

    public T getOne(final ExternalHttpContext context, final String id) {
        SanityChecker.check(id);
        Assert.isTrue(!id.contains(","), "No comma must be contains");
        final T entity = getClient().getOne(context, id);
        if (entity == null) {
            throw new NotFoundException("No entities founds with id : " + id);
        }
        else {
            return entity;
        }
    }

    public T getOne(final ExternalHttpContext context, final String id, final Optional<String> embedded) {
        SanityChecker.check(id);
        Assert.isTrue(!id.contains(","), "No comma must be contains");
        final T entity = getClient().getOne(context, id, Optional.empty(), embedded);
        if (entity == null) {
            throw new NotFoundException("No entities founds with id : " + id);
        }
        else {
            return entity;
        }
    }

    public boolean checkExist(final ExternalHttpContext context, final String criteria) {
        return getClient().checkExist(context, criteria);
    }

    public void delete(final ExternalHttpContext context, final String id) {
        SanityChecker.check(id);
        getClient().delete(context, id);
    }

    public abstract <C extends ExternalHttpContext> BaseCrudRestClient<T, C> getClient();

    /**
     * Find collection history.
     * @param context
     * @param id
     * @return
     */
    public LogbookOperationsResponseDto findHistoryById(final ExternalHttpContext context, final String id) {
        SanityChecker.check(id);
        final JsonNode body = getClient().findHistoryById(context, id);
        try {
            return JsonUtils.treeToValue(body, LogbookOperationsResponseDto.class, false);
        }
        catch (final JsonProcessingException e) {
            throw new InternalServerException(VitamRestUtils.PARSING_ERROR_MSG, e);
        }
    }

}
