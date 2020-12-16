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
package fr.gouv.vitamui.iam.internal.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.gouv.vitamui.iam.internal.server.application.converter.ApplicationConverter;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.customer.converter.CustomerConverter;
import fr.gouv.vitamui.iam.internal.server.externalParameters.converter.ExternalParametersConverter;
import fr.gouv.vitamui.iam.internal.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.idp.converter.IdentityProviderConverter;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.internal.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.internal.server.tenant.converter.TenantConverter;
import fr.gouv.vitamui.iam.internal.server.user.converter.UserConverter;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;

@Configuration
public class ConverterConfig {

    @Bean
    public UserConverter userConverter(final GroupRepository groupRepository, final AddressConverter addressConverter) {
        return new UserConverter(groupRepository, addressConverter);
    }

    @Bean
    public TenantConverter tenantConverter(final OwnerRepository ownerRepository) {
        return new TenantConverter(ownerRepository);
    }

    @Bean
    public AddressConverter addressConverter() {
        return new AddressConverter();
    }

    @Bean
    public OwnerConverter ownerConverter(final AddressConverter addressConverter) {
        return new OwnerConverter(addressConverter);
    }

    @Bean
    public ProfileConverter profileConverter() {
        return new ProfileConverter();
    }

    @Bean
    public GroupConverter groupConverter(final ProfileRepository profileRepository) {
        return new GroupConverter(profileRepository);
    }

    @Bean
    public CustomerConverter customerConverter(final OwnerRepository ownerRepository, final AddressConverter addressConverter,
            final OwnerConverter ownerConverter) {
        return new CustomerConverter(addressConverter, ownerRepository, ownerConverter);
    }

    @Bean
    public IdentityProviderConverter identityProviderConverter(final SpMetadataGenerator spMetadataGenerator) {
        return new IdentityProviderConverter(spMetadataGenerator);
    }

    @Bean
    public SubrogationConverter subrogationConverter(final UserRepository userRepository) {
        return new SubrogationConverter(userRepository);
    }

    @Bean
    public ApplicationConverter applicationConverter() {
        return new ApplicationConverter();
    }
    
    @Bean
    public ExternalParametersConverter externalParametersConverter() {
        return new ExternalParametersConverter();
    }

    @Bean
    public Converters converters(final UserConverter userConverter, final TenantConverter tenantConverter, final OwnerConverter ownerConverter,
            final ProfileConverter profileConverter, final ApplicationConverter applicationConverter, final GroupConverter groupConverter,
            final CustomerConverter customerConverter, final IdentityProviderConverter identityProviderConverter,
            final SubrogationConverter subrogationConverter, final ExternalParametersConverter externalParametersConverter) {
        return new Converters(userConverter, tenantConverter, ownerConverter, profileConverter, applicationConverter, groupConverter, customerConverter, identityProviderConverter,
                subrogationConverter, externalParametersConverter);
    }

}
