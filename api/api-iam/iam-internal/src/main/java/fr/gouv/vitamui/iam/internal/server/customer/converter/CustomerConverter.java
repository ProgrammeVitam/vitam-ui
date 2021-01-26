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
package fr.gouv.vitamui.iam.internal.server.customer.converter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.logbook.util.LogbookUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.customer.domain.GraphicIdentity;
import fr.gouv.vitamui.iam.internal.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;

public class CustomerConverter implements Converter<CustomerDto, Customer> {

    private final OwnerRepository ownerRepository;

    private final OwnerConverter ownerConverter;

    private final AddressConverter addressConverter;

    public static final String CODE_KEY = "Code client";

    public static final String NAME_KEY = "Nom";

    public static final String ENABLED_KEY = "Activé";

    public static final String COMPANY_NAME_KEY = "Raison sociale";

    public static final String PORTAL_TITLE = "Titre du portail";

    public static final String PORTAL_MESSAGE = "Message du portail";

    public static final String LANGUAGE_KEY = "Langue";

    public static final String PASSWORD_RECOVATION_KEY = "Durée de révocation de mot de passe (en mois)";

    public static final String OTP_KEY = "OTP";

    public static final String EMAIL_DOMAINS_KEY = "Domaines";

    public static final String INTERNAL_CODE_KEY = "Code interne";

    public static final String DEFAULT_EMAIL_DOMAIN_KEY = "Domaine par défaut";

    public static final String SUBROGEABLE_KEY = "Subrogeable";

    public static final String CUSTOM_GRAPHIC_IDENTITY_KEY = "Identité graphique personnalisée";

    public static final String GDPR_ALERT_DELAY_KEY = "GDPR Délai d'alerte";

    public static final String GDPR_ALERT_KEY = "GDPR Alerte";

    public CustomerConverter(final AddressConverter addressConverter, final OwnerRepository ownerRepository, final OwnerConverter ownerConverter) {
        this.ownerRepository = ownerRepository;
        this.ownerConverter = ownerConverter;
        this.addressConverter = addressConverter;
    }

    @Override
    public String convertToLogbook(final CustomerDto customer) {
        final Map<String, String> logbookData = new LinkedHashMap<>();
        logbookData.put(CODE_KEY, LogbookUtils.getValue(customer.getCode()));
        logbookData.put(NAME_KEY, LogbookUtils.getValue(customer.getName()));
        logbookData.put(GDPR_ALERT_DELAY_KEY, LogbookUtils.getValue(customer.getGdprAlertDelay()));
        logbookData.put(GDPR_ALERT_KEY, LogbookUtils.getValue(customer.isGdprAlert()));
        logbookData.put(ENABLED_KEY, LogbookUtils.getValue(customer.isEnabled()));
        logbookData.put(COMPANY_NAME_KEY, LogbookUtils.getValue(customer.getCompanyName()));
        final AddressDto address = customer.getAddress() != null ? customer.getAddress() : new AddressDto();
        addressConverter.addAddress(address, logbookData);
        logbookData.put(LANGUAGE_KEY, LogbookUtils.getValue(customer.getLanguage()));
        logbookData.put(PASSWORD_RECOVATION_KEY, LogbookUtils.getValue(customer.getPasswordRevocationDelay()));
        logbookData.put(OTP_KEY, LogbookUtils.getValue(customer.getOtp()));
        logbookData.put(EMAIL_DOMAINS_KEY, customer.getEmailDomains().toString());
        logbookData.put(DEFAULT_EMAIL_DOMAIN_KEY, LogbookUtils.getValue(customer.getDefaultEmailDomain()));
        logbookData.put(SUBROGEABLE_KEY, LogbookUtils.getValue(customer.isSubrogeable()));
        logbookData.put(INTERNAL_CODE_KEY, LogbookUtils.getValue(customer.getInternalCode()));
        logbookData.put(CUSTOM_GRAPHIC_IDENTITY_KEY, LogbookUtils.getValue(customer.isHasCustomGraphicIdentity()));
        return ApiUtils.toJson(logbookData);
    }

    @Override
    public Customer convertDtoToEntity(final CustomerDto dto) {
        final Customer customer = VitamUIUtils.copyProperties(dto, new Customer());

        if (dto.getOtp() != null) {
            customer.setOtp(dto.getOtp());
        }

        if (dto.getLanguage() != null) {
            customer.setLanguage(dto.getLanguage().toString());
        }

        if (dto.getAddress() != null) {
            customer.setAddress(VitamUIUtils.copyProperties(dto.getAddress(), new Address()));
        }

        customer.setGraphicIdentity(new GraphicIdentity());
        customer.getGraphicIdentity().setHasCustomGraphicIdentity(dto.isHasCustomGraphicIdentity());
        customer.getGraphicIdentity().setThemeColors(dto.getThemeColors());
        customer.getGraphicIdentity().setPortalMessage(dto.getPortalMessage());
        customer.getGraphicIdentity().setPortalTitle(dto.getPortalTitle());

        return customer;
    }

    @Override
    public CustomerDto convertEntityToDto(final Customer customer) {
        final CustomerDto dto = VitamUIUtils.copyProperties(customer, new CustomerDto());

        if (customer.getOtp() != null) {
            dto.setOtp(customer.getOtp());
        }

        if (customer.getLanguage() != null) {
            dto.setLanguage(LanguageDto.valueOf(customer.getLanguage()));
        }

        if (customer.getAddress() != null) {
            dto.setAddress(VitamUIUtils.copyProperties(customer.getAddress(), new AddressDto()));
        }

        if (customer.getGraphicIdentity() != null) {
            GraphicIdentity graphicalIdentity = customer.getGraphicIdentity();

            dto.setHasCustomGraphicIdentity(graphicalIdentity.isHasCustomGraphicIdentity());
            dto.setThemeColors(graphicalIdentity.getThemeColors());
            dto.setPortalMessage(graphicalIdentity.getPortalMessage());
            dto.setPortalTitle(graphicalIdentity.getPortalTitle());
        }

        final List<Owner> owners = ownerRepository.findAll(Query.query(Criteria.where("customerId").is(dto.getId())));
        if (CollectionUtils.isNotEmpty(owners)) {
            dto.setOwners(owners.stream().map(o -> ownerConverter.convertEntityToDto(o)).collect(Collectors.toList()));
        }
        return dto;
    }

}
