/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Provider } from '@angular/core';
import { TenantApiService } from './customer/tenant-api.service';
import { UserApiService } from './core/api/user-api.service';
import { CustomerApiService } from './core/api/customer-api.service';
import { ProfileValidators as IdentityProfileValidators } from './hierarchy/profile.validators';
import { ProfileService } from './profile/profile.service';
import { SubrogationService } from './subrogation/subrogation.service';
import { GroupApiService } from './core/api/group-api.service';
import { CustomerService } from './core/customer.service';
import { CustomerCreateValidators } from './customer/customer-create/customer-create.validators';
import { CustomerListService } from './customer/customer-list/customer-list.service';
import { IdentityProviderService } from './customer/customer-preview/sso-tab/identity-provider.service';
import { ProviderApiService } from './customer/customer-preview/sso-tab/provider-api.service';
import { CustomerResolver } from './customer/customer-resolver.service';
import { OwnerApiService } from './customer/owner-api.service';
import { OwnerFormValidators } from './customer/owner-form/owner-form.validators';
import { OwnerResolver } from './customer/owner-resolver.service';
import { OwnerService } from './customer/owner.service';
import { TenantFormValidators } from './customer/tenant-create/tenant-form.validators';
import { TenantResolver } from './customer/tenant-resolver.service';
import { TenantService } from './customer/tenant.service';
import { ExternalParamProfileService } from './external-param-profile/external-param-profile.service';
import { ExternalParamProfileValidators } from './external-param-profile/external-param-profile.validators';
import { GroupResolver } from './group/group-resolver.service';
import { GroupService } from './group/group.service';
import { GroupValidators } from './group/group.validators';
import { HierarchyResolver } from './hierarchy/hierarchy-resolver.service';
import { HierarchyService } from './hierarchy/hierarchy.service';
import { ProfileResolver } from './profile/profile-resolver.service';
import { ProfileValidators } from './profile/profile.validators';
import { CustomerSelectService } from './subrogation/customer-select.service';
import { SubrogationResolver } from './subrogation/subrogation-resolver.service';
import { UserGenericApiService } from './subrogation/user-generic-api.service';
import { UserCreateValidators } from './user/user-create/user-create.validators';
import { UserInfoService } from './user/user-info.service';
import { UserResolver } from './user/user-resolver.service';
import { UserService } from './user/user.service';

/**
 * Services of the identity feature whose HTTP calls depend on BASE_URL, directly or through another service.
 * They are not provided in root: they must be provided with the feature's BASE_URL (see provideVitamuiFeature).
 */
export function provideIdentityServices(): Provider[] {
  return [
    TenantApiService,
    UserApiService,
    CustomerApiService,
    IdentityProfileValidators,
    ProfileService,
    SubrogationService,
    CustomerCreateValidators,
    CustomerListService,
    CustomerResolver,
    CustomerSelectService,
    CustomerService,
    ExternalParamProfileService,
    ExternalParamProfileValidators,
    GroupApiService,
    GroupResolver,
    GroupService,
    GroupValidators,
    HierarchyResolver,
    HierarchyService,
    IdentityProviderService,
    OwnerApiService,
    OwnerFormValidators,
    OwnerResolver,
    OwnerService,
    ProfileResolver,
    ProfileValidators,
    ProviderApiService,
    SubrogationResolver,
    TenantFormValidators,
    TenantResolver,
    TenantService,
    UserCreateValidators,
    UserGenericApiService,
    UserInfoService,
    UserResolver,
    UserService,
  ];
}
