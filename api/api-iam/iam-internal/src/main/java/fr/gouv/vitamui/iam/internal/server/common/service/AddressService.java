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
package fr.gouv.vitamui.iam.internal.server.common.service;

import static fr.gouv.vitamui.commons.api.CommonConstants.GPDR_DEFAULT_VALUE;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;

public class AddressService {

    public void processPatch(final Address address, final Map<String, Object> partialDto,
            final Collection<EventDiffDto> logbooks, final boolean anonymized) {
        for (final Entry<String, Object> entry : partialDto.entrySet()) {
            String oldValue;
            String newValue = anonymized ? GPDR_DEFAULT_VALUE : CastUtils.toString(entry.getValue());
            switch (entry.getKey()) {
                case "street" :
                    oldValue = anonymized ? GPDR_DEFAULT_VALUE : address.getStreet();
                    logbooks.add(new EventDiffDto(AddressConverter.STREET_KEY, oldValue,
                            newValue));
                    address.setStreet(CastUtils.toString(entry.getValue()));
                    break;
                case "zipCode" :
                    oldValue = anonymized ? GPDR_DEFAULT_VALUE : address.getZipCode();
                    logbooks.add(new EventDiffDto(AddressConverter.ZIP_CODE_KEY, oldValue,
                            newValue));
                    address.setZipCode(CastUtils.toString(entry.getValue()));
                    break;
                case "city" :
                    oldValue = anonymized ? GPDR_DEFAULT_VALUE : address.getCity();
                    logbooks.add(new EventDiffDto(AddressConverter.CITY_KEY, oldValue,
                            newValue));
                    address.setCity(CastUtils.toString(entry.getValue()));
                    break;
                case "country" :
                    oldValue = anonymized ? GPDR_DEFAULT_VALUE : address.getCountry();
                    logbooks.add(new EventDiffDto(AddressConverter.COUNTRY_KEY, oldValue,
                            newValue));
                    address.setCountry(CastUtils.toString(entry.getValue()));
                    break;
                default :
                    throw new IllegalArgumentException(
                            "Unable to patch address " + " key : " + entry.getKey() + " is not allowed");
            }
        }
    }
}
