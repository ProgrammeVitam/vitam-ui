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
package fr.gouv.vitamui.iam.internal.server.owner.converter;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.logbook.util.LogbookUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;

public class OwnerConverter implements Converter<OwnerDto, Owner> {

    public static final String CODE_KEY = "Code propriétaire";

    public static final String NAME_KEY = "Nom";

    public static final String COMPANY_NAME_KEY = "Raison sociale";

    public static final String INTERNAL_CODE_KEY = "Code interne";

    private final AddressConverter addressConverter;

    public OwnerConverter(final AddressConverter addressConverter) {
        this.addressConverter = addressConverter;
    }

    @Override
    public String convertToLogbook(final OwnerDto owner) {
        final Map<String, String> ownerLogbookData = new LinkedHashMap<>();
        ownerLogbookData.put(CODE_KEY, LogbookUtils.getValue(owner.getCode()));
        ownerLogbookData.put(NAME_KEY, LogbookUtils.getValue(owner.getName()));
        ownerLogbookData.put(COMPANY_NAME_KEY, LogbookUtils.getValue(owner.getCompanyName()));
        ownerLogbookData.put(INTERNAL_CODE_KEY, LogbookUtils.getValue(owner.getInternalCode()));
        AddressDto address = owner.getAddress() != null ? owner.getAddress() : new AddressDto();
        addressConverter.addAddress(address, ownerLogbookData);
        return ApiUtils.toJson(ownerLogbookData);
    }

    @Override
    public Owner convertDtoToEntity(final OwnerDto dto) {
        final Owner owner = new Owner();
        VitamUIUtils.copyProperties(dto, owner);
        if (dto.getAddress() != null) {
            owner.setAddress(addressConverter.convertDtoToEntity(dto.getAddress()));
        }
        return owner;
    }

    @Override
    public OwnerDto convertEntityToDto(final Owner owner) {
        final OwnerDto dto = new OwnerDto();
        VitamUIUtils.copyProperties(owner, dto);
        if (owner.getAddress() != null) {
            dto.setAddress(addressConverter.convertEntityToDto(owner.getAddress()));
        }
        return dto;
    }
}
