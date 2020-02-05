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
package fr.gouv.vitamui.commons.rest;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;

import fr.gouv.vitamui.commons.api.domain.IdDto;

/**
 * To check existence, read, create, update and delete an object with identifier.
 * <br>
 * Method : {@link #getOne(String, Optional)} must be use only by an external endpoints
 *
 *
 */
public interface CrudController<D extends IdDto> {

    /**
     * Get All with criteria.
     * @param criteria
     * @return
     */
    default Collection<D> getAll(final Optional<String> criteria) {
        throw new UnsupportedOperationException("getAll not implemented");
    }

    /**
     * Check Exists with criteria.
     * @param criteria
     * @return
     */
    ResponseEntity<Void> checkExist(String criteria);

    /**
     * GetOne.
     * @param ids
     * @return
     */
    default D getOne(final String id) {
        throw new UnsupportedOperationException("getOne not implemented");
    }

    /**
     * Create an item.
     * @param dto
     * @return
     */
    D create(D dto);

    /**
     * Update an item with its id.
     * @param id
     * @param dto
     * @return
     */
    D update(String id, D dto);

    /**
     * Delete an item.
     * @param id
     */
    default void delete(final String id) {
        throw new UnsupportedOperationException("delete not implemented");
    }

    /**
     * Patch and item using its id.
     * @param id
     * @param updates
     * @return
     */
    default D patch(final String id, final Map<String, Object> updates) {
        throw new UnsupportedOperationException("patch not implemented");
    }
}
