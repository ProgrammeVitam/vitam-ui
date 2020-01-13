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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.rest.client.BaseCrudWebClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;

/**
 * Abstract Class for WebClient calls for UI.
 *
 *
 * @param <T>
 */
public abstract class AbstractCrudWebService<T extends IdDto> {

    public List<T> getAll(final ExternalHttpContext context, final Optional<String> criteria) {
        return getClient().getAll(context, criteria);
    }

    public T create(final ExternalHttpContext c, final T dto) {
        beforeCreate(dto);
        return getClient().create(c, dto);
    }

    protected void beforeCreate(final T dto) {
        Assert.isTrue(StringUtils.isBlank(dto.getId()), "The DTO identifier must be null for create");
        ApiUtils.checkValidity(dto);
    }

    protected void beforeUpdate(final T dto) {
        Assert.isTrue(StringUtils.isNotBlank(dto.getId()), "The DTO identifier must be not null for update.");
        ApiUtils.checkValidity(dto);
    }

    public T patch(final ExternalHttpContext c, final Map<String, Object> partialDto, final String id) {
        beforePatch(partialDto, id);
        return getClient().patch(c, id, partialDto);
    }

    protected void beforePatch(final Map<String, Object> updates, final String id) {
        Assert.isTrue(StringUtils.equals(id, (String) updates.get("id")), "The DTO identifier must match the path identifier for patch.");
    }

    public T getOne(final ExternalHttpContext context, final String id) {
        Assert.isTrue(!id.contains(","), "No comma must be contains");
        final T entity = getClient().getOne(context, id, Optional.empty());
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
        getClient().delete(context, id);
    }

    public abstract <C extends ExternalHttpContext> BaseCrudWebClient<C, T> getClient();

}
